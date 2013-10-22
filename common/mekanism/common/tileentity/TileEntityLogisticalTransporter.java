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
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.TransporterUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityLogisticalTransporter extends TileEntityTransmitter<InventoryNetwork> implements ITileNetwork
{
	public Set<TransporterStack> transit = new HashSet<TransporterStack>();
	
	public boolean needsSync = false;
	
	@Override
	public void updateEntity()
	{
		if(worldObj.isRemote)
		{
			for(TransporterStack stack : transit)
			{
				if(stack.clientFirstTick)
				{
					stack.clientFirstTick = false;
				}
				else {
					stack.progress++;
				}
			}
		}
		else {
			Set<TransporterStack> remove = new HashSet<TransporterStack>();
			
			for(TransporterStack stack : transit)
			{
				if(!stack.initiatedPath)
				{
					System.out.println("Initiating path");
					if(!recalculate(stack))
					{
						remove.add(stack);
						continue;
					}
				}
				
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
								needsSync = true;
								TileEntityLogisticalTransporter nextTile = (TileEntityLogisticalTransporter)next.getTileEntity(worldObj);
								nextTile.entityEntering(stack);
								remove.add(stack);
								
								continue;
							}
						}
						else {
							if(!stack.noTarget)
							{
								if(next != null && next.getTileEntity(worldObj) instanceof IInventory)
								{
									needsSync = true;
									IInventory inventory = (IInventory)next.getTileEntity(worldObj);
									
									if(inventory != null)
									{
										ItemStack rejected = TransporterUtils.putInInventory(inventory, stack.itemStack);
										
										if(rejected == null)
										{
											remove.add(stack);
											continue;
										}
										else {
											stack.itemStack = rejected;
										}
									}
								}
							}
						}
					}
					
					System.out.println("high progress");
					if(!recalculate(stack))
					{
						remove.add(stack);
						continue;
					}
					else {
						stack.progress = 50;
					}
				}
				else if(stack.progress == 50)
				{
					if(stack.isFinal(this))
					{
						if(!TransporterUtils.canInsert(stack.getDest().getTileEntity(worldObj), stack.itemStack) && !stack.noTarget)
						{
							System.out.println("final, has target, cant insert dest");
							if(!recalculate(stack))
							{
								remove.add(stack);
								continue;
							}
						}
						else if(stack.noTarget)
						{
							System.out.println("reached final with no target, recalculating");
							if(!recalculate(stack))
							{
								remove.add(stack);
								continue;
							}
						}
					}
					else {
						if(!(stack.getNext(this).getTileEntity(worldObj) instanceof TileEntityLogisticalTransporter))
						{
							System.out.println("reached half, not final, next not transport");
							if(!recalculate(stack))
							{
								remove.add(stack);
								continue;
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
			
			if(needsSync)
			{
				PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())), Object3D.get(this), 50D);
				needsSync = false;
			}
		}
	}
	
	private boolean recalculate(TransporterStack stack)
	{
		needsSync = true;
		
		if(!stack.recalculatePath(this))
		{
			stack.calculateIdle(this);
		}
		
		if(!stack.hasPath())
		{
			//drop
			return false;
		}
		
		return true;
	}
	
	public boolean insert(Object3D original, ItemStack itemStack)
	{
		needsSync = true;
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
		PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())), Object3D.get(this), 50D);
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
		transit.clear();
		
		int amount = dataStream.readInt();
		
		for(int i = 0; i < amount; i++)
		{
			TransporterStack stack = new TransporterStack();
			stack.read(dataStream);
			
			transit.add(stack);
		}
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(transit.size());
		
		for(TransporterStack stack : transit)
		{
			stack.write(this, data);
		}
		
		return data;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
    	if(nbtTags.hasKey("stacks"))
    	{
    		NBTTagList tagList = nbtTags.getTagList("stacks");
    		
    		for(int i = 0; i < tagList.tagCount(); i++)
    		{
    			transit.add(TransporterStack.read((NBTTagCompound)tagList.tagAt(i)));
    		}
    	}
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        NBTTagList stacks = new NBTTagList();
        
        for(TransporterStack stack : transit)
        {
        	NBTTagCompound tagCompound = new NBTTagCompound();
        	stack.write(tagCompound);
        	stacks.appendTag(tagCompound);
        }
        
        if(stacks.tagCount() != 0)
        {
        	nbtTags.setTag("stacks", stacks);
        }
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
