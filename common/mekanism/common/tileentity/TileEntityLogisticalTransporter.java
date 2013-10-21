package mekanism.common.tileentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.ITileNetwork;
import mekanism.common.InventoryNetwork;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.TransporterStack;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityLogisticalTransporter extends TileEntityTransmitter<InventoryNetwork> implements ITileNetwork
{
	/** This transporter's active state. */
	public boolean isActive = false;
	
	public Set<TransporterStack> transit = new HashSet<TransporterStack>();
	
	@Override
	public void updateEntity()
	{
		if(!worldObj.isRemote)
		{
			Set<TransporterStack> remove = new HashSet<TransporterStack>();
			
			for(TransporterStack stack : transit)
			{
				stack.progress++;
				
				if(stack.progress > 100)
				{
					if(stack.hasPath())
					{
						int currentIndex = stack.pathToTarget.indexOf(Object3D.get(this));
						Object3D next = stack.pathToTarget.get(currentIndex-1);
						
						if(!stack.isFinal(this))
						{
							if(next != null && next.getTileEntity(worldObj) instanceof TileEntityLogisticalTransporter)
							{
								TileEntityLogisticalTransporter nextTile = (TileEntityLogisticalTransporter)next.getTileEntity(worldObj);
								nextTile.entityEntering(stack);
								remove.add(stack);
								
								continue;
							}
						}
						else {
							if(!stack.goingHome)
							{
								
							}
							else {
								
							}
						}
					}
					
					stack.sendHome(this);
					
					if(!stack.hasPath())
					{
						//drop
						remove.add(stack);
					}
				}
				else if(stack.progress == 50)
				{
					if(stack.isFinal(this))
					{
						if(!TransporterUtils.canInsert(stack.getDest().getTileEntity(worldObj), stack.itemStack) && !stack.goingHome)
						{
							stack.sendHome(this);
							
							if(!stack.hasPath())
							{
								//drop
								remove.add(stack);
							}
						}
					}
					else {
						if(!(stack.getNext(this).getTileEntity(worldObj) instanceof TileEntityLogisticalTransporter))
						{
							stack.sendHome(this);
							
							if(!stack.hasPath())
							{
								//drop
								remove.add(stack);
							}
						}
					}
				}
			}
			
			for(TransporterStack stack : remove)
			{
				transit.remove(stack);
			}
			
			for(TransporterStack stack : transit)
			{
				System.out.println(Object3D.get(this) + " " + stack.progress);
			}
		}
	}
	
	public boolean insert(Object3D original, ItemStack itemStack)
	{
		TransporterStack stack = new TransporterStack();
		stack.itemStack = itemStack;
		stack.originalLocation = original;
		stack.recalculatePath(this);
		
		if(stack.hasPath())
		{
			transit.add(stack);
			return true;
		}
		
		return false;
	}
	
	public void entityEntering(TransporterStack stack)
	{
		stack.progress = 0;
		transit.add(stack);
	}
	
	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.ITEM;
	}
	
	@Override
	public InventoryNetwork getTransmitterNetwork(boolean createIfNull)
	{
		if(theNetwork == null && createIfNull)
		{
			TileEntity[] adjacentTransporters = TransporterUtils.getConnectedTransporters(this);
			
			HashSet<InventoryNetwork> connectedNets = new HashSet<InventoryNetwork>();
			
			for(TileEntity transporter : adjacentTransporters)
			{
				if(TransmissionType.checkTransmissionType(transporter, getTransmissionType()) && ((ITransmitter<InventoryNetwork>)transporter).getTransmitterNetwork(false) != null)
				{
					connectedNets.add(((ITransmitter<InventoryNetwork>)transporter).getTransmitterNetwork());
				}
			}
			
			if(connectedNets.size() == 0 || worldObj.isRemote)
			{
				theNetwork = new InventoryNetwork(this);
			}
			else if(connectedNets.size() == 1)
			{
				theNetwork = connectedNets.iterator().next();
				theNetwork.transmitters.add(this);
			}
			else {
				theNetwork = new InventoryNetwork(connectedNets);
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
	public void invalidate()
	{
		if(!worldObj.isRemote)
		{
			getTransmitterNetwork().split(this);
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
		if(!worldObj.isRemote)
		{
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tileEntity = Object3D.get(this).getFromSide(side).getTileEntity(worldObj);
				
				if(TransmissionType.checkTransmissionType(tileEntity, getTransmissionType()))
				{
					getTransmitterNetwork().merge(((ITransmitter<InventoryNetwork>)tileEntity).getTransmitterNetwork());
				}
			}
			
			getTransmitterNetwork().refresh();
		}
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
}
