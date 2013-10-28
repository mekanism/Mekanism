package mekanism.common.tileentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.EnumColor;
import mekanism.api.Object3D;
import mekanism.common.HashList;
import mekanism.common.ITileNetwork;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.transporter.TransporterStack;
import mekanism.common.util.TransporterUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityLogisticalTransporter extends TileEntity implements ITileNetwork
{
	public static final int SPEED = 5;
	
	public EnumColor color;
	
	public HashList<TransporterStack> transit = new HashList<TransporterStack>();
	
	public Set<TransporterStack> needsSync = new HashSet<TransporterStack>();
	
	@Override
	public void updateEntity()
	{
		if(worldObj.isRemote)
		{
			for(TransporterStack stack : transit)
			{
				stack.progress = Math.min(100, stack.progress+SPEED);
			}
		}
		else {
			Set<TransporterStack> remove = new HashSet<TransporterStack>();
			
			for(TransporterStack stack : transit)
			{
				if(!stack.initiatedPath)
				{
					if(!recalculate(stack))
					{
						remove.add(stack);
						continue;
					}
				}
				
				stack.progress += SPEED;
				
				if(stack.progress > 100)
				{
					if(stack.hasPath())
					{
						int currentIndex = stack.pathToTarget.indexOf(Object3D.get(this));
						Object3D next = stack.pathToTarget.get(currentIndex-1);
						
						if(!stack.isFinal(this))
						{
							if(next != null && stack.canInsert(stack.getNext(this).getTileEntity(worldObj)))
							{
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
									needsSync.add(stack);
									IInventory inventory = (IInventory)next.getTileEntity(worldObj);
									
									if(inventory != null)
									{
										ItemStack rejected = TransporterUtils.putStackInInventory(inventory, stack.itemStack, stack.getSide(this));
										
										if(rejected == null)
										{
											remove.add(stack);
											continue;
										}
										else {
											needsSync.add(stack);
											stack.itemStack = rejected;
										}
									}
								}
							}
						}
					}
					
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
						if(!TransporterUtils.canInsert(stack.getDest().getTileEntity(worldObj), stack.itemStack, stack.getSide(this)) && !stack.noTarget)
						{
							if(!recalculate(stack))
							{
								remove.add(stack);
								continue;
							}
						}
						else if(stack.noTarget)
						{
							if(!recalculate(stack))
							{
								remove.add(stack);
								continue;
							}
						}
					}
					else {
						if(!stack.canInsert(stack.getNext(this).getTileEntity(worldObj)))
						{
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
				PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Object3D.get(this), getSyncPacket(stack, true)), Object3D.get(this), 50D);
				transit.remove(stack);
			}
			
			for(TransporterStack stack : needsSync)
			{
				if(transit.contains(stack))
				{
					PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Object3D.get(this), getSyncPacket(stack, false)), Object3D.get(this), 50D);
				}
			}
			
			needsSync.clear();
		}
	}
	
	private boolean recalculate(TransporterStack stack)
	{
		needsSync.add(stack);
		
		if(!stack.recalculatePath(this))
		{
			stack.calculateIdle(this);
		}
		
		if(!stack.hasPath())
		{
			TransporterUtils.drop(this, stack);
			return false;
		}
		
		return true;
	}
	
	public boolean insert(Object3D original, ItemStack itemStack, EnumColor color)
	{
		TransporterStack stack = new TransporterStack();
		stack.itemStack = itemStack;
		stack.originalLocation = original;
		stack.color = color;
		
		if(!stack.canInsert(this))
		{
			return false;
		}
		
		if(stack.recalculatePath(this))
		{
			transit.add(stack);
			PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Object3D.get(this), getSyncPacket(stack, false)), Object3D.get(this), 50D);
			return true;
		}
		
		return false;
	}
	
	public void entityEntering(TransporterStack stack)
	{
		stack.progress = 0;
		transit.add(stack);
		PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Object3D.get(this), getSyncPacket(stack, false)), Object3D.get(this), 50D);
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
		int type = dataStream.readInt();
		
		if(type == 0)
		{
			int c = dataStream.readInt();
			
			if(c != -1)
			{
				color = TransporterUtils.colors.get(c);
			}
			else {
				color = null;
			}
			
			transit.clear();
			
			int amount = dataStream.readInt();
			
			for(int i = 0; i < amount; i++)
			{
				transit.add(TransporterStack.readFromPacket(dataStream));
			}
		}
		else if(type == 1)
		{
			boolean kill = dataStream.readBoolean();
			int index = dataStream.readInt();
			
			if(kill)
			{
				transit.remove(index);
			}
			else {
				TransporterStack stack = TransporterStack.readFromPacket(dataStream);
				
				if(stack.progress == 0)
				{
					stack.progress = 5;
				}
				
				transit.replace(index, stack);
			}
		}
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(0);
		
		if(color != null)
		{
			data.add(TransporterUtils.colors.indexOf(color));
		}
		else {
			data.add(-1);
		}
		
		data.add(transit.size());
		
		for(TransporterStack stack : transit)
		{
			stack.write(this, data);
		}
		
		return data;
	}
	
	public ArrayList getSyncPacket(TransporterStack stack, boolean kill)
	{
		ArrayList data = new ArrayList();
		
		data.add(1);
		data.add(kill);
		data.add(transit.indexOf(stack));
		
		if(!kill)
		{
			stack.write(this, data);
		}
		
		return data;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        if(nbtTags.hasKey("color"))
        {
        	color = TransporterUtils.colors.get(nbtTags.getInteger("color"));
        }
        
    	if(nbtTags.hasKey("stacks"))
    	{
    		NBTTagList tagList = nbtTags.getTagList("stacks");
    		
    		for(int i = 0; i < tagList.tagCount(); i++)
    		{
    			transit.add(TransporterStack.readFromNBT((NBTTagCompound)tagList.tagAt(i)));
    		}
    	}
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        if(color != null)
        {
        	nbtTags.setInteger("color", TransporterUtils.colors.indexOf(color));
        }
        
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
}
