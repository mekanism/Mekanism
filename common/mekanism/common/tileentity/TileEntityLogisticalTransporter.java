package mekanism.common.tileentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.EnumColor;
import mekanism.api.Coord4D;
import mekanism.common.HashList;
import mekanism.common.IConfigurable;
import mekanism.common.ILogisticalTransporter;
import mekanism.common.ITileNetwork;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.transporter.TransporterManager;
import mekanism.common.transporter.TransporterStack;
import mekanism.common.transporter.TransporterStack.Path;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityLogisticalTransporter extends TileEntity implements ITileNetwork, ILogisticalTransporter, IPipeTile, IConfigurable
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
				if(stack != null)
				{
					stack.progress = Math.min(100, stack.progress+SPEED);
				}
			}
		}
		else {
			Set<TransporterStack> remove = new HashSet<TransporterStack>();
			
			for(TransporterStack stack : transit)
			{
				if(!stack.initiatedPath)
				{
					if(!recalculate(stack, null))
					{
						remove.add(stack);
						continue;
					}
				}
				
				stack.progress += SPEED;
				
				if(stack.progress > 100)
				{
					Coord4D prevSet = null;
					
					if(stack.hasPath())
					{
						int currentIndex = stack.pathToTarget.indexOf(Coord4D.get(this));
						Coord4D next = stack.pathToTarget.get(currentIndex-1);
						
						if(!stack.isFinal(this))
						{
							if(next != null && stack.canInsertToTransporter(stack.getNext(this).getTileEntity(worldObj), ForgeDirection.getOrientation(stack.getSide(this))))
							{
								ILogisticalTransporter nextTile = (ILogisticalTransporter)next.getTileEntity(worldObj);
								nextTile.entityEntering(stack);
								remove.add(stack);
								
								continue;
							}
							else if(next != null)
							{
								prevSet = next;
							}
						}
						else {
							if(stack.pathType != Path.NONE)
							{
								if(next != null && next.getTileEntity(worldObj) instanceof IInventory)
								{
									needsSync.add(stack);
									IInventory inventory = (IInventory)next.getTileEntity(worldObj);
									
									if(inventory != null)
									{
										ItemStack rejected = InventoryUtils.putStackInInventory(inventory, stack.itemStack, stack.getSide(this), stack.pathType == Path.HOME);
										
										if(rejected == null)
										{
											TransporterManager.remove(stack);
											remove.add(stack);
											continue;
										}
										else {
											needsSync.add(stack);
											stack.itemStack = rejected;
											
											prevSet = next;
										}
									}
								}
							}
						}
					}
					
					if(!recalculate(stack, prevSet))
					{
						remove.add(stack);
						continue;
					}
					else {
						if(prevSet != null)
						{
							stack.progress = 0;
						}
						else {
							stack.progress = 50;
						}
					}
				}
				else if(stack.progress == 50)
				{
					if(stack.isFinal(this))
					{
						if(stack.pathType == Path.DEST && !InventoryUtils.canInsert(stack.getDest().getTileEntity(worldObj), stack.color, stack.itemStack, stack.getSide(this), false))
						{
							if(!recalculate(stack, null))
							{
								remove.add(stack);
								continue;
							}
						}
						else if(stack.pathType == Path.HOME && !InventoryUtils.canInsert(stack.getDest().getTileEntity(worldObj), stack.color, stack.itemStack, stack.getSide(this), true))
						{
							if(!recalculate(stack, null))
							{
								remove.add(stack);
								continue;
							}
						}
						else if(stack.pathType == Path.NONE)
						{
							if(!recalculate(stack, null))
							{
								remove.add(stack);
								continue;
							}
						}
					}
					else {
						TileEntity next = stack.getNext(this).getTileEntity(worldObj);
						boolean recalculate = false;
						
						if(!stack.canInsertToTransporter(next, ForgeDirection.getOrientation(stack.getSide(this))))
						{
							recalculate = true;
						}
						
						if(recalculate)
						{
							if(!recalculate(stack, null))
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
				PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Coord4D.get(this), getSyncPacket(stack, true)), Coord4D.get(this), 50D);
				transit.remove(stack);
				MekanismUtils.saveChunk(this);
			}
			
			for(TransporterStack stack : needsSync)
			{
				if(transit.contains(stack))
				{
					PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Coord4D.get(this), getSyncPacket(stack, false)), Coord4D.get(this), 50D);
				}
			}
			
			needsSync.clear();
		}
	}
	
	private boolean recalculate(TransporterStack stack, Coord4D from)
	{
		needsSync.add(stack);
		
		if(!TransporterManager.didEmit(stack.itemStack, stack.recalculatePath(this, 0)))
		{
			if(!stack.calculateIdle(this))
			{
				TransporterUtils.drop(this, stack);
				return false;
			}
		}
		
		if(from != null)
		{
			stack.originalLocation = from;
		}
		
		return true;
	}
	
	@Override
	public ItemStack insert(Coord4D original, ItemStack itemStack, EnumColor color, boolean doEmit, int min)
	{
		TransporterStack stack = new TransporterStack();
		stack.itemStack = itemStack;
		stack.originalLocation = original;
		stack.homeLocation = original;
		stack.color = color;
		
		if(!canReceiveFrom(original.getTileEntity(worldObj), ForgeDirection.getOrientation(stack.getSide(this))) || !stack.canInsertToTransporter(this, ForgeDirection.getOrientation(stack.getSide(this))))
		{
			return itemStack;
		}
		
		ItemStack rejected = stack.recalculatePath(this, min);
		
		if(TransporterManager.didEmit(stack.itemStack, rejected))
		{
			stack.itemStack = TransporterManager.getToUse(stack.itemStack, rejected);
			
			transit.add(stack);
			TransporterManager.add(stack);
			PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Coord4D.get(this), getSyncPacket(stack, false)), Coord4D.get(this), 50D);
			MekanismUtils.saveChunk(this);
			return rejected;
		}
		
		return itemStack;
	}
	
	@Override
	public ItemStack insertRR(TileEntityLogisticalSorter outputter, ItemStack itemStack, EnumColor color, boolean doEmit, int min)
	{
		TransporterStack stack = new TransporterStack();
		stack.itemStack = itemStack;
		stack.originalLocation = Coord4D.get(outputter);
		stack.homeLocation = Coord4D.get(outputter);
		stack.color = color;
		
		if(!canReceiveFrom(outputter, ForgeDirection.getOrientation(stack.getSide(this))) || !stack.canInsertToTransporter(this, ForgeDirection.getOrientation(stack.getSide(this))))
		{
			return itemStack;
		}
		
		ItemStack rejected = stack.recalculateRRPath(outputter, this, min);
		
		if(TransporterManager.didEmit(stack.itemStack, rejected))
		{
			stack.itemStack = TransporterManager.getToUse(stack.itemStack, rejected);
			
			transit.add(stack);
			TransporterManager.add(stack);
			PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Coord4D.get(this), getSyncPacket(stack, false)), Coord4D.get(this), 50D);
			MekanismUtils.saveChunk(this);
			return rejected;
		}
		
		return itemStack;
	}
	
	@Override
	public void entityEntering(TransporterStack stack)
	{
		stack.progress = 0;
		transit.add(stack);
		PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Coord4D.get(this), getSyncPacket(stack, false)), Coord4D.get(this), 50D);
		MekanismUtils.saveChunk(this);
	}
	
	@Override
	public void validate()
	{
		super.validate();
		
		if(worldObj.isRemote)
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketDataRequest().setParams(Coord4D.get(this)));
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
    			TransporterStack stack = TransporterStack.readFromNBT((NBTTagCompound)tagList.tagAt(i));
    			
    			transit.add(stack);
    			TransporterManager.add(stack);
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

	@Override
	public boolean isSolidOnSide(ForgeDirection side)
	{
		return true;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) 
	{
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) 
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) 
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) 
	{
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) 
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) 
	{
		return null;
	}

	@Override
	public IPipe getPipe() 
	{
		return null;
	}

	@Override
	public PipeType getPipeType() 
	{
		return PipeType.ITEM;
	}

	@Override
	public int injectItem(ItemStack stack, boolean doAdd, ForgeDirection from) 
	{
		if(doAdd)
		{
			TileEntity tile = Coord4D.get(this).getFromSide(from).getTileEntity(worldObj);
			
			ItemStack rejects = TransporterUtils.insert(tile, this, stack, null, true, 0);
			return TransporterManager.getToUse(stack, rejects).stackSize;
		}
		
		return 0;
	}

	@Override
	public boolean isPipeConnected(ForgeDirection with) 
	{
		return true;
	}

	@Override
	public boolean onSneakRightClick(EntityPlayer player, int side)
	{
		TransporterUtils.incrementColor(this);
		PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Coord4D.get(this), getNetworkedData(new ArrayList())), Coord4D.get(this), 50D);
		player.sendChatToPlayer(ChatMessageComponent.createFromText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.configurator.toggleColor") + ": " + (color != null ? color.getName() : EnumColor.BLACK + MekanismUtils.localize("gui.none"))));
		
		return true;
	}

	@Override
	public boolean onRightClick(EntityPlayer player, int side)
	{
		return false;
	}
	
	@Override
	public EnumColor getColor()
	{
		return color;
	}
	
	@Override
	public void setColor(EnumColor c)
	{
		color = c;
	}
	
	@Override
	public TileEntity getTile()
	{
		return this;
	}
	
	@Override
	public boolean canTransporterConnect(TileEntity tileEntity, ForgeDirection side)
	{
		return true;
	}
	
	@Override
	public boolean canTransporterConnectMutual(TileEntity tileEntity, ForgeDirection side)
	{
		return true;
	}
	
	@Override
	public boolean canEmitTo(TileEntity tileEntity, ForgeDirection side)
	{
		if(!canTransporterConnect(tileEntity, side))
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean canReceiveFrom(TileEntity tileEntity, ForgeDirection side)
	{
		if(!canTransporterConnect(tileEntity, side))
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public int getPriority()
	{
		return 1;
	}
}
