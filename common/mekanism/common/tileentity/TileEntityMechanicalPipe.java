package mekanism.common.tileentity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import mekanism.api.Object3D;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.common.FluidNetwork;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.PipeUtils;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityMechanicalPipe extends TileEntityTransmitter<FluidNetwork> implements IFluidHandler
{
	/** The fake tank used for fluid transfer calculations. */
	public FluidTank dummyTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
	
	/** This pipe's active state. */
	public boolean isActive = false;
	
	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.FLUID;
	}
	
	@Override
	public FluidNetwork getTransmitterNetwork(boolean createIfNull)
	{
		if(theNetwork == null && createIfNull)
		{
			TileEntity[] adjacentPipes = PipeUtils.getConnectedPipes(this);
			HashSet<FluidNetwork> connectedNets = new HashSet<FluidNetwork>();
			
			for(TileEntity pipe : adjacentPipes)
			{
				if(TransmissionType.checkTransmissionType(pipe, getTransmissionType()) && ((ITransmitter<FluidNetwork>)pipe).getTransmitterNetwork(false) != null)
				{
					connectedNets.add(((ITransmitter<FluidNetwork>)pipe).getTransmitterNetwork());
				}
			}
			
			if(connectedNets.size() == 0)
			{
				theNetwork = new FluidNetwork(this);
			}
			else if(connectedNets.size() == 1)
			{
				theNetwork = connectedNets.iterator().next();
				theNetwork.transmitters.add(this);
			}
			else {
				theNetwork = new FluidNetwork(connectedNets);
				theNetwork.transmitters.add(this);
			}
		}
		
		return theNetwork;
	}

	@Override
	public void fixTransmitterNetwork()
	{
		getTransmitterNetwork().fixMessedUpNetwork(this);
	}
	
	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		
		getTransmitterNetwork().split(this);
	}
	
	@Override
	public void invalidate()
	{
		getTransmitterNetwork().split(this);
		
		if(!worldObj.isRemote)
		{
			TransmitterNetworkRegistry.getInstance().pruneEmptyNetworks();
		}
		
		super.invalidate();
	}
	
	@Override
	public void removeFromTransmitterNetwork()
	{
		if(theNetwork != null)
		{
			theNetwork.removeTransmitter(this);
		}
	}

	@Override
	public void refreshTransmitterNetwork() 
	{
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = Object3D.get(this).getFromSide(side).getTileEntity(worldObj);
			
			if(TransmissionType.checkTransmissionType(tileEntity, getTransmissionType()))
			{
				getTransmitterNetwork().merge(((ITransmitter<FluidNetwork>)tileEntity).getTransmitterNetwork());
			}
		}
		
		getTransmitterNetwork().refresh();
	}
	
	@Override
	public void updateEntity()
	{
		if(!worldObj.isRemote)
		{
			if(isActive)
			{
				IFluidHandler[] connectedAcceptors = PipeUtils.getConnectedAcceptors(this);
				
				for(IFluidHandler container : connectedAcceptors)
				{
					ForgeDirection side = ForgeDirection.getOrientation(Arrays.asList(connectedAcceptors).indexOf(container));
					
					if(container != null)
					{
						FluidStack received = container.drain(side.getOpposite(), 100, false);
						
						if(received != null && received.amount != 0)
						{
							container.drain(side.getOpposite(), getTransmitterNetwork().emit(received, true), true);
						}
					}
				}
			}
		}
	}
	
	@Override
	public boolean canUpdate()
	{
		return true;
	}
	
	@Override
	public void validate()
	{
		super.validate();
		
		if(worldObj.isRemote)
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketDataRequest().setParams(Object3D.get(this)));
		}
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		isActive = dataStream.readBoolean();
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(isActive);
		return data;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        isActive = nbtTags.getBoolean("isActive");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setBoolean("isActive", isActive);
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(!isActive)
		{
			return getTransmitterNetwork().emit(resource, doFill);
		}
		
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) 
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) 
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) 
	{
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) 
	{
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) 
	{
		return new FluidTankInfo[] {dummyTank.getInfo()};
	}
	
	@Override
	public int getTransmitterNetworkSize()
	{
		return getTransmitterNetwork().getSize();
	}

	@Override
	public int getTransmitterNetworkAcceptorSize()
	{
		return getTransmitterNetwork().getAcceptorSize();
	}

	@Override
	public String getTransmitterNetworkNeeded()
	{
		return getTransmitterNetwork().getNeeded();
	}

	@Override
	public String getTransmitterNetworkFlow()
	{
		return getTransmitterNetwork().getFlow();
	}
	
	@Override
	public boolean onSneakRightClick(EntityPlayer player, int side)
	{
		isActive = !isActive;
		refreshTransmitterNetwork();
		PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
		
		return true;
	}

    @Override
    public int getCapacity()
    {
        return 1000;
    }
}
