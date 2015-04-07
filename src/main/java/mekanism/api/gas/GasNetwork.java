package mekanism.api.gas;

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

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * A DynamicNetwork extension created specifically for the transfer of Gasses. By default this is server-only, but if ticked on
 * the client side and if it's posted events are handled properly, it has the capability to visually display gasses network-wide.
 * @author aidancbrady
 *
 */
public class GasNetwork extends DynamicNetwork<IGasHandler, GasNetwork>
{
	public int transferDelay = 0;

	public boolean didTransfer;
	public boolean prevTransfer;

	public float gasScale;

	public Gas refGas;

	public GasStack buffer;
	public int prevStored;

	public int prevTransferAmount = 0;

	public GasNetwork() {}

	public GasNetwork(Collection<GasNetwork> networks)
	{
		for(GasNetwork net : networks)
		{
			if(net != null)
			{
				if(FMLCommonHandler.instance().getEffectiveSide().isClient())
				{
					if(net.refGas != null && net.gasScale > gasScale)
					{
						gasScale = net.gasScale;
						refGas = net.refGas;
						buffer = net.buffer;

						net.gasScale = 0;
						net.refGas = null;
						net.buffer = null;
					}
				} else
				{
					if(net.buffer != null)
					{
						if(buffer == null)
						{
							buffer = net.buffer.copy();
						} else
						{
							if(buffer.isGasEqual(net.buffer))
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
				}

				adoptTransmittersAndAcceptorsFrom(net);
				net.deregister();
			}
		}

		gasScale = getScale();

		register();
	}

	@Override
	public void absorbBuffer(IGridTransmitter<IGasHandler, GasNetwork> transmitter)
	{
		Object b = transmitter.getBuffer();
		if(!(b instanceof GasStack) || ((GasStack)b).getGas() == null || ((GasStack)b).amount == 0)
		{
			return;
		}

		GasStack gas = (GasStack)b;

		if(buffer == null || buffer.getGas() == null || buffer.amount == 0)
		{
			buffer = gas.copy();
			return;
		}

//		if(!gas.isGasEqual(buffer)) Mekanism.logger.warn("Gas type " + gas.getGas().getName() + " of buffer doesn't match type " + buffer.getGas().getName() + " of absorbing network");

		buffer.amount += gas.amount;
		gas.amount = 0;
	}

	@Override
	public void clampBuffer()
	{
		if(buffer != null && buffer.amount > getCapacity())
		{
			buffer.amount = capacity;
		}
	}

	public int getGasNeeded()
	{
		return getCapacity()-(buffer != null ? buffer.amount : 0);
	}

	public int tickEmit(GasStack stack)
	{
		List<IGasHandler> availableAcceptors = new ArrayList<>();

		availableAcceptors.addAll(getAcceptors(stack.getGas()));

		Collections.shuffle(availableAcceptors);

		int toSend = stack.amount;
		int prevSending = toSend;

		if(!availableAcceptors.isEmpty())
		{
			int divider = availableAcceptors.size();
			int remaining = toSend % divider;
			int sending = (toSend-remaining)/divider;

			for(IGasHandler acceptor : availableAcceptors)
			{
				int currentSending = sending;
				EnumSet<ForgeDirection> sides = acceptorDirections.get(Coord4D.get((TileEntity)acceptor));

				if(remaining > 0)
				{
					currentSending++;
					remaining--;
				}

				for(ForgeDirection side : sides)
				{
					int prev = toSend;

					toSend -= acceptor.receiveGas(side, new GasStack(stack.getGas(), currentSending), true);

					if(toSend < prev)
					{
						break;
					}
				}
			}
		}

		int sent = prevSending-toSend;

		if(sent > 0 && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			didTransfer = true;
			transferDelay = 2;
		}

		return sent;
	}

	public int emit(GasStack stack, boolean doTransfer)
	{
		if(buffer != null && buffer.getGas() != stack.getGas())
		{
			return 0;
		}

		int toUse = Math.min(getGasNeeded(), stack.amount);

		if(doTransfer)
		{
			if(buffer == null)
			{
				buffer = stack.copy();
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
				MinecraftForge.EVENT_BUS.post(new GasTransferEvent(this, buffer, didTransfer));
				needsUpdate = false;
			}

			prevTransfer = didTransfer;

			if(buffer != null)
			{
				prevTransferAmount = tickEmit(buffer);
				buffer.amount -= prevTransferAmount;

				if(buffer.amount <= 0)
				{
					buffer = null;
				}
			}
		}
	}

	@Override
	public void clientTick()
	{
		super.clientTick();

		gasScale = Math.max(gasScale, getScale());

		if(didTransfer && gasScale < 1)
		{
			gasScale = Math.max(getScale(), Math.min(1, gasScale+0.02F));
		}
		else if(!didTransfer && gasScale > 0)
		{
			gasScale = Math.max(getScale(), Math.max(0, gasScale-0.02F));

			if(gasScale == 0)
			{
				buffer = null;
			}
		}
	}

	@Override
	public Set<IGasHandler> getAcceptors(Object data)
	{
		Gas type = (Gas)data;
		Set<IGasHandler> toReturn = new HashSet<IGasHandler>();
		
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			return toReturn;
		}

		for(Coord4D coord : possibleAcceptors.keySet())
		{
			EnumSet<ForgeDirection> sides = acceptorDirections.get(coord);
			IGasHandler acceptor = (IGasHandler)coord.getTileEntity(getWorld());
			
			if(sides == null || sides.isEmpty())
			{
				continue;
			}

			for(ForgeDirection side : sides)
			{
				if(acceptor != null && acceptor.canReceiveGas(side, type))
				{
					toReturn.add(acceptor);
					break;
				}
			}
		}

		return toReturn;
	}

	public static class GasTransferEvent extends Event
	{
		public final GasNetwork gasNetwork;

		public final GasStack transferType;
		public final boolean didTransfer;

		public GasTransferEvent(GasNetwork network, GasStack type, boolean did)
		{
			gasNetwork = network;
			transferType = type;
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
		return "[GasNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
	}

	@Override
	public String getNeededInfo()
	{
		return Integer.toString(getGasNeeded());
	}

	@Override
	public String getStoredInfo()
	{
		return buffer != null ? buffer.getGas().getLocalizedName() + " (" + buffer.amount + ")" : "None";
	}

	@Override
	public String getFlowInfo()
	{
		return Integer.toString(prevTransferAmount) + "/t";
	}
}
