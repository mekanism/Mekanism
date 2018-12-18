package mekanism.common.transmitters.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.Mekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;

import org.apache.commons.lang3.tuple.Pair;

public class FluidNetwork extends DynamicNetwork<IFluidHandler, FluidNetwork>
{
	public int transferDelay = 0;

	public boolean didTransfer;
	public boolean prevTransfer;

	public float fluidScale;

	public Fluid refFluid;

	public FluidStack buffer;
	public int prevStored;

	public int prevTransferAmount = 0;

	public FluidNetwork() {}

	public FluidNetwork(Collection<FluidNetwork> networks)
	{
		for(FluidNetwork net : networks)
		{
			if(net != null)
			{
				if(net.buffer != null)
				{
					if(buffer == null)
					{
						buffer = net.buffer.copy();
					}
					else {
						if(buffer.getFluid() == net.buffer.getFluid())
						{
							buffer.amount += net.buffer.amount;
						}
						else if(net.buffer.amount > buffer.amount)
						{
							buffer = net.buffer.copy();
						}

					}

					net.buffer = null;
				}

				adoptTransmittersAndAcceptorsFrom(net);
				net.deregister();
			}
		}

		fluidScale = getScale();

		register();
	}

	@Override
	public void absorbBuffer(IGridTransmitter<IFluidHandler, FluidNetwork> transmitter)
	{
		Object b = transmitter.getBuffer();

		if(!(b instanceof FluidStack) || ((FluidStack)b).getFluid() == null || ((FluidStack)b).amount == 0)
		{
			return;
		}

		FluidStack fluid = (FluidStack)b;

		if(buffer == null || buffer.getFluid() == null || buffer.amount == 0)
		{
			buffer = fluid.copy();
            fluid.amount = 0;
			return;
		}
		
		//TODO better multiple buffer impl
		if(buffer.isFluidEqual(fluid))
		{
			buffer.amount += fluid.amount;
		}

        fluid.amount = 0;
	}

	@Override
	public void clampBuffer()
	{
		if(buffer != null && buffer.amount > getCapacity())
		{
			buffer.amount = capacity;
		}
	}

	@Override
	protected void updateMeanCapacity()
	{
		int numCables = transmitters.size();
		double sum = 0;

		for(IGridTransmitter<IFluidHandler, FluidNetwork> pipe : transmitters)
		{
			sum += pipe.getCapacity();
		}

		meanCapacity = sum / (double)numCables;
	}

	public int getFluidNeeded()
	{
		return getCapacity()-(buffer != null ? buffer.amount : 0);
	}

	public int tickEmit(FluidStack fluidToSend, boolean doTransfer)
	{
		List<Pair<Coord4D, IFluidHandler>> availableAcceptors = new ArrayList<>(getAcceptors(fluidToSend));

		Collections.shuffle(availableAcceptors);

		int fluidSent = 0;

		if(!availableAcceptors.isEmpty())
		{
			int divider = availableAcceptors.size();
			int remaining = fluidToSend.amount % divider;
			int sending = (fluidToSend.amount-remaining)/divider;

			for(Pair<Coord4D, IFluidHandler> pair : availableAcceptors)
			{
				int currentSending = sending;
				IFluidHandler acceptor = pair.getRight();
				EnumSet<EnumFacing> sides = acceptorDirections.get(pair.getLeft());

				if(remaining > 0)
				{
					currentSending++;
					remaining--;
				}

				for(EnumFacing side : sides)
				{
					int prev = fluidSent;

					if(acceptor != null && fluidToSend != null)
					{
						fluidSent += acceptor.fill(PipeUtils.copy(fluidToSend, currentSending), doTransfer);
					}

					if(fluidSent > prev)
					{
						break;
					}
				}
			}
		}

		if(doTransfer && fluidSent > 0 && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			didTransfer = true;
			transferDelay = 2;
		}

		if (fluidSent > fluidToSend.amount){
			Mekanism.logger.error("Some fluid handler took more fluid than we gave it?!");
			Mekanism.logger.error("Handler list:");
			for(Pair<Coord4D, IFluidHandler> pair : availableAcceptors){
				Mekanism.logger.error(pair.getRight().toString());
			}
			fluidSent = fluidToSend.amount;
		}

		return fluidSent;
	}

	public int emit(FluidStack fluidToSend, boolean doTransfer)
	{
		if(fluidToSend == null || (buffer != null && buffer.getFluid() != fluidToSend.getFluid()))
		{
			return 0;
		}

		int toUse = Math.min(getFluidNeeded(), fluidToSend.amount);

		if(doTransfer)
		{
			if(buffer == null)
			{
				buffer = fluidToSend.copy();
				buffer.amount = toUse;
			}
			else {
				buffer.amount += toUse;
			}
		}

		return toUse;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			prevTransferAmount = 0;

			if(transferDelay == 0)
			{
				didTransfer = false;
			}
			else {
				transferDelay--;
			}

			int stored = buffer != null ? buffer.amount : 0;

			if(stored != prevStored)
			{
				needsUpdate = true;
			}

			prevStored = stored;

			if(didTransfer != prevTransfer || needsUpdate)
			{
				MinecraftForge.EVENT_BUS.post(new FluidTransferEvent(this, buffer, didTransfer));
				needsUpdate = false;
			}

			prevTransfer = didTransfer;

			if(buffer != null)
			{
				prevTransferAmount = tickEmit(buffer, true);
				
				if(buffer != null)
				{
					buffer.amount -= prevTransferAmount;

					if(buffer.amount <= 0)
					{
						buffer = null;
					}
				}
			}
		}
	}

	@Override
	public void clientTick()
	{
		super.clientTick();

		fluidScale = Math.max(fluidScale, getScale());

		if(didTransfer && fluidScale < 1)
		{
			fluidScale = Math.max(getScale(), Math.min(1, fluidScale+0.02F));
		}
		else if(!didTransfer && fluidScale > 0)
		{
			fluidScale = getScale();

			if(fluidScale == 0)
			{
				buffer = null;
			}
		}
	}

	@Override
	public Set<Pair<Coord4D, IFluidHandler>> getAcceptors(Object data)
	{
		FluidStack fluidToSend = (FluidStack)data;
		Set<Pair<Coord4D, IFluidHandler>> toReturn = new HashSet<>();
		
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			return toReturn;
		}

		for(Coord4D coord : possibleAcceptors.keySet())
		{
			EnumSet<EnumFacing> sides = acceptorDirections.get(coord);
			TileEntity tile = coord.getTileEntity(getWorld());

			if(sides == null || sides.isEmpty())
			{
				continue;
			}

			for(EnumFacing side : sides)
			{
				if(!CapabilityUtils.hasCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side))
				{
					continue;
				}
				
				IFluidHandler acceptor = CapabilityUtils.getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
						
				if(acceptor != null && PipeUtils.canFill(acceptor, fluidToSend))
				{
					toReturn.add(Pair.of(coord, acceptor));
					break;
				}
			}
		}

		return toReturn;
	}

	public static class FluidTransferEvent extends Event
	{
		public final FluidNetwork fluidNetwork;

		public final FluidStack fluidType;
		public final boolean didTransfer;

		public FluidTransferEvent(FluidNetwork network, FluidStack type, boolean did)
		{
			fluidNetwork = network;
			fluidType = type;
			didTransfer = did;
		}
	}

	public float getScale()
	{
		return Math.min(1, (buffer == null || getCapacity() == 0 ? 0 : (float)buffer.amount/getCapacity()));
	}

	@Override
	public String toString()
	{
		return "[FluidNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
	}

	@Override
	public String getNeededInfo()
	{
		return (float)getFluidNeeded()/1000F + " buckets";
	}

	@Override
	public String getStoredInfo()
	{
		return buffer != null ? LangUtils.localizeFluidStack(buffer) + " (" + buffer.amount + " mB)" : "None";
	}

	@Override
	public String getFlowInfo()
	{
		return prevTransferAmount + " mB/t";
	}
}