package mekanism.common.multipart;

import java.util.Collection;

import mekanism.api.transmitters.TransmissionType;
import mekanism.api.util.CapabilityUtils;
import mekanism.common.FluidNetwork;
import mekanism.common.Tier;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.PipeTier;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.util.PipeUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class PartMechanicalPipe extends PartTransmitter<IFluidHandler, FluidNetwork> implements IFluidHandlerWrapper
{
	public Tier.PipeTier tier;
	
	public float currentScale;

	public FluidTank buffer = new FluidTank(Fluid.BUCKET_VOLUME);

	public FluidStack lastWrite;

	public PartMechanicalPipe(Tier.PipeTier pipeTier)
	{
		super();
		tier = pipeTier;
		buffer.setCapacity(getCapacity());
	}

	@Override
	public void update()
	{
		if(!getWorld().isRemote)
		{
            updateShare();
            
			IFluidHandler[] connectedAcceptors = PipeUtils.getConnectedAcceptors(getPos(), getWorld());

			for(EnumFacing side : getConnections(ConnectionType.PULL))
			{
				if(connectedAcceptors[side.ordinal()] != null)
				{
					IFluidHandler container = connectedAcceptors[side.ordinal()];

					if(container != null)
					{
						FluidStack received = container.drain(getPullAmount(), false);

						if(received != null && received.amount != 0)
						{
							container.drain(takeFluid(received, true), true);
						}
					}
				}
			}
		}

		super.update();
	}

    @Override
    public void updateShare()
    {
        if(getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetworkSize() > 0)
        {
            FluidStack last = getSaveShare();

            if((last != null && !(lastWrite != null && lastWrite.amount == last.amount && lastWrite.getFluid() == last.getFluid())) || (last == null && lastWrite != null))
            {
                lastWrite = last;
                markDirty();
            }
        }
    }

	private FluidStack getSaveShare()
	{
		if(getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetwork().buffer != null)
		{
			int remain = getTransmitter().getTransmitterNetwork().buffer.amount%getTransmitter().getTransmitterNetwork().transmitters.size();
			int toSave = getTransmitter().getTransmitterNetwork().buffer.amount/getTransmitter().getTransmitterNetwork().transmitters.size();

			if(getTransmitter().getTransmitterNetwork().transmitters.iterator().next().equals(getTransmitter()))
			{
				toSave += remain;
			}

			return PipeUtils.copy(getTransmitter().getTransmitterNetwork().buffer, toSave);
		}

		return null;
	}

	@Override
	public void onUnloaded()
	{
		if(!getWorld().isRemote && getTransmitter().hasTransmitterNetwork())
		{
			if(lastWrite != null && getTransmitter().getTransmitterNetwork().buffer != null)
			{
				getTransmitter().getTransmitterNetwork().buffer.amount -= lastWrite.amount;

				if(getTransmitter().getTransmitterNetwork().buffer.amount <= 0)
				{
					getTransmitter().getTransmitterNetwork().buffer = null;
				}
			}
		}

		super.onUnloaded();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);
		
		if(nbtTags.hasKey("tier")) tier = Tier.PipeTier.values()[nbtTags.getInteger("tier")];
		buffer.setCapacity(getCapacity());

		if(nbtTags.hasKey("cacheFluid"))
		{
			buffer.setFluid(FluidStack.loadFluidStackFromNBT(nbtTags.getCompoundTag("cacheFluid")));
		}
        else {
            buffer.setFluid(null);
        }
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		if(lastWrite != null && lastWrite.amount > 0)
		{
			nbtTags.setTag("cacheFluid", lastWrite.writeToNBT(new NBTTagCompound()));
		}
        else {
            nbtTags.removeTag("cacheFluid");
        }

		nbtTags.setInteger("tier", tier.ordinal());
		
		return nbtTags;
	}

	@Override
	public ResourceLocation getType()
	{
		return new ResourceLocation("mekanism:mechanical_pipe_" + tier.name().toLowerCase());
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.FLUID;
	}

	@Override
	public TransmitterType getTransmitterType()
	{ 
		return tier.type; 
	}

	@Override
	public boolean isValidAcceptor(TileEntity acceptor, EnumFacing side)
	{
		return PipeUtils.isValidAcceptorOnSide(acceptor, side);
	}

	@Override
	public FluidNetwork createNewNetwork()
	{
		return new FluidNetwork();
	}

	@Override
	public FluidNetwork createNetworkByMerging(Collection<FluidNetwork> networks)
	{
		return new FluidNetwork(networks);
	}

	@Override
	public int getCapacity()
	{
		return tier.pipeCapacity;
	}

	@Override
	public FluidStack getBuffer()
	{
		return buffer == null ? null : buffer.getFluid();
	}

	@Override
	public void takeShare()
	{
		if(getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetwork().buffer != null && lastWrite != null)
		{
			getTransmitter().getTransmitterNetwork().buffer.amount -= lastWrite.amount;
			buffer.setFluid(lastWrite);
		}
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		if(getConnectionType(from) == ConnectionType.NORMAL)
		{
			return takeFluid(resource, doFill);
		}

		return 0;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return getConnectionType(from) == ConnectionType.NORMAL;
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		if(getConnectionType(from) != ConnectionType.NONE)
		{
			return new FluidTankInfo[] {buffer.getInfo()};
		}

		return new FluidTankInfo[0];
	}

	public int getPullAmount()
	{
		return tier.pipePullAmount;
	}

	@Override
	public IFluidHandler getCachedAcceptor(EnumFacing side)
	{
		TileEntity tile = getCachedTile(side);
		
		if(CapabilityUtils.hasCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()))
		{
			return CapabilityUtils.getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
		}
		
		return null;
	}
	
	public int takeFluid(FluidStack fluid, boolean doEmit)
	{
		if(getTransmitter().hasTransmitterNetwork())
		{
			return getTransmitter().getTransmitterNetwork().emit(fluid, doEmit);
		}
		else {
			return buffer.fill(fluid, doEmit);
		}
	}
	
	@Override
	public boolean upgrade(int tierOrdinal)
	{
		if(tier.ordinal() < BaseTier.ULTIMATE.ordinal() && tierOrdinal == tier.ordinal()+1)
		{
			tier = PipeTier.values()[tier.ordinal()+1];
			
			markDirtyTransmitters();
			sendDesc = true;
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public void readUpdatePacket(PacketBuffer packet)
	{
		tier = PipeTier.values()[packet.readInt()];
		
		super.readUpdatePacket(packet);
	}

	@Override
	public void writeUpdatePacket(PacketBuffer packet)
	{
		packet.writeInt(tier.ordinal());
		
		super.writeUpdatePacket(packet);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side)
	{
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, side);
	}
	
	public CapabilityWrapperManager manager = new CapabilityWrapperManager(IFluidHandlerWrapper.class, FluidHandlerWrapper.class);

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side)
	{
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
		{
			return (T)manager.getWrapper(this, side);
		}
		
		return super.getCapability(capability, side);
	}
}