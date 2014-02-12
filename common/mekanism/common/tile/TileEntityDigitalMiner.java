package mekanism.common.tile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.common.HashList;
import mekanism.common.IActiveState;
import mekanism.common.IAdvancedBoundingBlock;
import mekanism.common.IBoundingBlock;
import mekanism.common.ILogisticalTransporter;
import mekanism.common.IRedstoneControl;
import mekanism.common.IUpgradeTile;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.miner.MItemStackFilter;
import mekanism.common.miner.MOreDictFilter;
import mekanism.common.miner.MinerFilter;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.transporter.InvStack;
import mekanism.common.transporter.TransporterManager;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MinerUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
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
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;

public class TileEntityDigitalMiner extends TileEntityElectricBlock implements IPeripheral, IUpgradeTile, IRedstoneControl, IActiveState, IAdvancedBoundingBlock
{
	public static int[] EJECT_INV;
	
	public int searched = 0;
	
	public HashList<MinerFilter> filters = new HashList<MinerFilter>();
	
	public Coord4D currentNode;
	
	public final double ENERGY_USAGE = Mekanism.digitalMinerUsage;
	
	public int radius;
	
	public boolean inverse;
	
	public int minY = 0;
	public int maxY = 60;
	
	public boolean doEject = false;
	public boolean doPull = false;
	
	public int delay;
	
	public ItemStack replaceStack;
	
	public boolean isActive;
	public boolean clientActive;
	
	public boolean silkTouch;
	
	public boolean running;
	
	public double prevEnergy;
	
	public int delayTicks;
	
	public int numPowering;
	
	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
	public TileComponentUpgrade upgradeComponent = new TileComponentUpgrade(this, 28);
	
	public TileEntityDigitalMiner()
	{
		super("DigitalMiner", MachineType.DIGITAL_MINER.baseEnergy);
		inventory = new ItemStack[29];
		radius = 10;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(getActive())
		{
			for(EntityPlayer player : playersUsing)
			{
				if(player.openContainer instanceof ContainerNull || player.openContainer instanceof ContainerFilter)
				{
					player.closeScreen();
				}
			}
		}
		
		if(!worldObj.isRemote)
		{
			ChargeUtils.discharge(27, this);
			
			if(MekanismUtils.canFunction(this) && running && getEnergy() >= getPerTick())
			{
				setActive(true);
				
				if(delay > 0)
				{
					delay--;
				}
				
				setEnergy(getEnergy()-getPerTick());
				
				if(delay == 0)
				{
					currentNode = getNextBlock();
					
					if(currentNode != null)
					{
						searched++;
						
						if(!currentNode.isAirBlock(worldObj) && !Coord4D.get(this).equals(currentNode) && !(currentNode.getTileEntity(worldObj) instanceof IBoundingBlock))
						{
							int id = currentNode.getBlockId(worldObj);
							int meta = currentNode.getMetadata(worldObj);
							
							boolean hasFilter = false;
							
							for(MinerFilter filter : filters)
							{
								if(filter.canFilter(new ItemStack(id, 1, meta)))
								{
									hasFilter = true;
									break;
								}
							}
							
							if(inverse ? !hasFilter : hasFilter)
							{
								List<ItemStack> drops = MinerUtils.getDrops(worldObj, currentNode, silkTouch);
								
								if(canInsert(drops))
								{
									add(drops);
									
									setReplace(currentNode);
									
									worldObj.playAuxSFXAtEntity(null, 2001, currentNode.xCoord, currentNode.yCoord, currentNode.zCoord, id + (meta << 12));
									
									delay = getDelay();
								}
							}
						}
					}
					else {
						searched = 0;
						stop();
					}
				}
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}
			
			if(doEject && delayTicks == 0 && getTopEject(false, null) != null && getEjectInv() != null)
			{
				if(getEjectInv() instanceof IInventory)
				{
					ItemStack remains = InventoryUtils.putStackInInventory((IInventory)getEjectInv(), getTopEject(false, null), ForgeDirection.getOrientation(facing).getOpposite().ordinal(), false);
					
					getTopEject(true, remains);
				}
				else if(getEjectInv() instanceof ILogisticalTransporter)
				{
					ItemStack rejected = TransporterUtils.insert(getEjectTile(), (ILogisticalTransporter)getEjectInv(), getTopEject(false, null), null, true, 0);
					
					if(TransporterManager.didEmit(getTopEject(false, null), rejected))
					{
						getTopEject(true, rejected);
					}
				}
				
				delayTicks = 10;
			}
			else if(delayTicks > 0)
			{
				delayTicks--;
			}
			
			if(playersUsing.size() > 0)
			{
				for(EntityPlayer player : playersUsing)
				{
					PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Coord4D.get(this), getSmallPacket(new ArrayList())), player);
				}
			}
			
			prevEnergy = getEnergy();
		}
	}
	
	public Coord4D getNextBlock()
	{
		if(currentNode == null)
		{
			return new Coord4D(xCoord-radius, maxY, zCoord-radius, worldObj.provider.dimensionId);
		}
		
		if(currentNode.xCoord < xCoord+radius)
		{
			return currentNode.getFromSide(ForgeDirection.EAST);
		}
		else if(currentNode.zCoord < zCoord+radius)
		{
			return new Coord4D(xCoord-radius, currentNode.yCoord, currentNode.zCoord+1);
		}
		else if(currentNode.yCoord > minY)
		{
			return new Coord4D(xCoord-radius, currentNode.yCoord-1, zCoord-radius);
		}
		else {
			return null;
		}
	}
	
	public double getPerTick()
	{
		double ret = MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_USAGE);
		
		if(silkTouch)
		{
			ret *= 6F;
		}
		
		int baseRad = Math.max(radius-10, 0);
		ret *= (1 + ((float)baseRad/22F));
		
		int baseHeight = Math.max((maxY-minY)-60, 0);
		ret *= (1 + ((float)baseHeight/195F));
		
		return ret;
	}
	
	public int getDelay()
	{
		return MekanismUtils.getTicks(getSpeedMultiplier(), 60);
	}
	
	public void setReplace(Coord4D obj)
	{
		ItemStack stack = getReplace();
		
		if(stack != null)
		{
			worldObj.setBlock(obj.xCoord, obj.yCoord, obj.zCoord, replaceStack.itemID, replaceStack.getItemDamage(), 3);
			
			if(Block.blocksList[obj.getBlockId(worldObj)] != null && !Block.blocksList[obj.getBlockId(worldObj)].canBlockStay(worldObj, obj.xCoord, obj.yCoord, obj.zCoord))
			{
				Block.blocksList[obj.getBlockId(worldObj)].dropBlockAsItem(worldObj, obj.xCoord, obj.yCoord, obj.zCoord, obj.getMetadata(worldObj), 1);
				worldObj.setBlockToAir(obj.xCoord, obj.yCoord, obj.zCoord);
			}
		}
		else {
			worldObj.setBlockToAir(obj.xCoord, obj.yCoord, obj.zCoord);
		}
	}
	
	public ItemStack getReplace()
	{
		if(replaceStack == null)
		{
			return null;
		}
		
		for(int i = 0; i < 27; i++)
		{
			if(inventory[i] != null && inventory[i].isItemEqual(replaceStack))
			{
				inventory[i].stackSize--;
				
				if(inventory[i].stackSize == 0)
				{
					inventory[i] = null;
				}
				
				return MekanismUtils.size(replaceStack, 1);
			}
		}
		
		if(doPull && getPullInv() instanceof IInventory)
		{
			InvStack stack = InventoryUtils.takeDefinedItem((IInventory)getPullInv(), 1, replaceStack.copy(), 1, 1);
			
			if(stack != null)
			{
				stack.use();
				return MekanismUtils.size(replaceStack, 1);
			}
		}
		
		return null;
	}
	
	public ItemStack[] copy(ItemStack[] stacks)
	{
		ItemStack[] toReturn = new ItemStack[stacks.length];
		
		for(int i = 0; i < stacks.length; i++)
		{
			toReturn[i] = stacks[i] != null ? stacks[i].copy() : null;
		}
		
		return toReturn;
	}
	
	public ItemStack getTopEject(boolean remove, ItemStack reject)
	{
		for(int i = 27-1; i >= 0; i--)
		{
			ItemStack stack = inventory[i];
			
			if(stack != null)
			{
				if(replaceStack != null && replaceStack.isItemEqual(stack))
				{
					continue;
				}
				
				if(remove)
				{
					inventory[i] = reject;
				}
				
				return stack;
			}
		}
		
		return null;
	}
	
	public boolean canInsert(List<ItemStack> stacks)
	{
		if(stacks.isEmpty())
		{
			return true;
		}
		
		ItemStack[] testInv = copy(inventory);
		
		int added = 0;
		
		stacks:
		for(ItemStack stack : stacks)
		{
			for(int i = 0; i < 27; i++)
			{
				if(testInv[i] == null)
				{
					testInv[i] = stack;
					added++;
					
					continue stacks;
				}
				else if(testInv[i].isItemEqual(stack) && testInv[i].stackSize+stack.stackSize <= stack.getMaxStackSize())
				{
					testInv[i].stackSize += stack.stackSize;
					added++;
					
					continue stacks;
				}
			}
		}
		
		if(added == stacks.size())
		{
			return true;
		}
		
		return false;
	}
	
	public TileEntity getPullInv()
	{
		return Coord4D.get(this).translate(0, 2, 0).getTileEntity(worldObj);
	}
	
	public TileEntity getEjectInv()
	{
		ForgeDirection side = ForgeDirection.getOrientation(facing).getOpposite();
		
		return new Coord4D(xCoord+(side.offsetX*2), yCoord+1, zCoord+(side.offsetZ*2), worldObj.provider.dimensionId).getTileEntity(worldObj);
	}
	
	public void add(List<ItemStack> stacks)
	{
		if(stacks.isEmpty())
		{
			return;
		}
		
		stacks:
		for(ItemStack stack : stacks)
		{
			for(int i = 0; i < 27; i++)
			{
				if(inventory[i] == null)
				{
					inventory[i] = stack;
					
					continue stacks;
				}
				else if(inventory[i].isItemEqual(stack) && inventory[i].stackSize+stack.stackSize <= stack.getMaxStackSize())
				{
					inventory[i].stackSize += stack.stackSize;
					
					continue stacks;
				}
			}
		}
	}
	
	public void start()
	{
		running = true;
		
		MekanismUtils.saveChunk(this);
	}
	
	public void stop()
	{
		running = false;
		
		MekanismUtils.saveChunk(this);
	}
	
	public void reset()
	{
		searched = 0;
		currentNode = null;
		running = false;
		
		MekanismUtils.saveChunk(this);
	}
	
	@Override
	public void openChest()
	{
		super.openChest();
		
		if(!worldObj.isRemote)
		{
			for(EntityPlayer player : playersUsing)
			{
				PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Coord4D.get(this), getNetworkedData(new ArrayList())), player);
			}
		}
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        radius = nbtTags.getInteger("radius");
        minY = nbtTags.getInteger("minY");
        maxY = nbtTags.getInteger("maxY");
        doEject = nbtTags.getBoolean("doEject");
        doPull = nbtTags.getBoolean("doPull");
        isActive = nbtTags.getBoolean("isActive");
        running = nbtTags.getBoolean("running");
        delay = nbtTags.getInteger("delay");
        silkTouch = nbtTags.getBoolean("silkTouch");
        numPowering = nbtTags.getInteger("numPowering");
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
        inverse = nbtTags.getBoolean("inverse");
        searched = nbtTags.getInteger("searched");
        
        if(nbtTags.hasKey("currentNode"))
        {
        	currentNode = Coord4D.read(nbtTags.getCompoundTag("currentNode"));
        }
        
        if(nbtTags.hasKey("replaceStack"))
        {
        	replaceStack = ItemStack.loadItemStackFromNBT(nbtTags.getCompoundTag("replaceStack"));
        }
        
    	if(nbtTags.hasKey("filters"))
    	{
    		NBTTagList tagList = nbtTags.getTagList("filters");
    		
    		for(int i = 0; i < tagList.tagCount(); i++)
    		{
    			filters.add(MinerFilter.readFromNBT((NBTTagCompound)tagList.tagAt(i)));
    		}
    	}
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("radius", radius);
        nbtTags.setInteger("minY", minY);
        nbtTags.setInteger("maxY", maxY);
        nbtTags.setBoolean("doEject", doEject);
        nbtTags.setBoolean("doPull", doPull);
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setBoolean("running", running);
        nbtTags.setInteger("delay", delay);
        nbtTags.setBoolean("silkTouch", silkTouch);
        nbtTags.setInteger("numPowering", numPowering);
        nbtTags.setInteger("controlType", controlType.ordinal());
        nbtTags.setBoolean("inverse", inverse);
        nbtTags.setInteger("searched", searched);
        
        if(currentNode != null)
        {
        	nbtTags.setCompoundTag("currentNode", currentNode.write(new NBTTagCompound()));
        }
        
        if(replaceStack != null)
        {
        	nbtTags.setCompoundTag("replaceStack", replaceStack.writeToNBT(new NBTTagCompound()));
        }
        
        NBTTagList filterTags = new NBTTagList();
        
        for(MinerFilter filter : filters)
        {
        	filterTags.appendTag(filter.write(new NBTTagCompound()));
        }
        
        if(filterTags.tagCount() != 0)
        {
        	nbtTags.setTag("filters", filterTags);
        }
    }
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		if(!worldObj.isRemote)
		{
			int type = dataStream.readInt();
			
			if(type == 0)
			{
				doEject = !doEject;
			}
			else if(type == 1)
			{
				doPull = !doPull;
			}
			else if(type == 2)
			{
				boolean doNull = dataStream.readBoolean();
				
				if(!doNull)
				{
					replaceStack = new ItemStack(dataStream.readInt(), 1, dataStream.readInt());
				}
				else {
					replaceStack = null;
				}
			}
			else if(type == 3)
			{
				start();
			}
			else if(type == 4)
			{
				stop();
			}
			else if(type == 5)
			{
				reset();
			}
			else if(type == 6)
			{
				radius = dataStream.readInt();
			}
			else if(type == 7)
			{
				minY = dataStream.readInt();
			}
			else if(type == 8)
			{
				maxY = dataStream.readInt();
			}
			else if(type == 9)
			{
				silkTouch = !silkTouch;
			}
			else if(type == 10)
			{
				inverse = !inverse;
			}
			
			for(EntityPlayer player : playersUsing)
			{
				PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Coord4D.get(this), getGenericPacket(new ArrayList())), player);
			}
			
			return;
		}
		
		super.handlePacketData(dataStream);
		
		int type = dataStream.readInt();
		
		if(type == 0)
		{
			radius = dataStream.readInt();
			minY = dataStream.readInt();
			maxY = dataStream.readInt();
			doEject = dataStream.readBoolean();
			doPull = dataStream.readBoolean();
			isActive = dataStream.readBoolean();
			running = dataStream.readBoolean();
			silkTouch = dataStream.readBoolean();
			numPowering = dataStream.readInt();
			
			if(dataStream.readBoolean())
			{
				replaceStack = new ItemStack(dataStream.readInt(), 1, dataStream.readInt());
			}
			else {
				replaceStack = null;
			}
			
			controlType = RedstoneControl.values()[dataStream.readInt()];
			inverse = dataStream.readBoolean();
			searched = dataStream.readInt();
			
			filters.clear();
			
			int amount = dataStream.readInt();
			
			for(int i = 0; i < amount; i++)
			{
				filters.add(MinerFilter.readFromPacket(dataStream));
			}
		}
		else if(type == 1)
		{
			radius = dataStream.readInt();
			minY = dataStream.readInt();
			maxY = dataStream.readInt();
			doEject = dataStream.readBoolean();
			doPull = dataStream.readBoolean();
			isActive = dataStream.readBoolean();
			running = dataStream.readBoolean();
			silkTouch = dataStream.readBoolean();
			numPowering = dataStream.readInt();
			
			if(dataStream.readBoolean())
			{
				replaceStack = new ItemStack(dataStream.readInt(), 1, dataStream.readInt());
			}
			else {
				replaceStack = null;
			}
			
			controlType = RedstoneControl.values()[dataStream.readInt()];
			inverse = dataStream.readBoolean();
			searched = dataStream.readInt();
		}
		else if(type == 2)
		{
			filters.clear();
			
			int amount = dataStream.readInt();
			
			for(int i = 0; i < amount; i++)
			{
				filters.add(MinerFilter.readFromPacket(dataStream));
			}
		}
		else if(type == 3)
		{
			isActive = dataStream.readBoolean();
			running = dataStream.readBoolean();
			searched = dataStream.readInt();
		}
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(0);
		
		data.add(radius);
		data.add(minY);
		data.add(maxY);
		data.add(doEject);
		data.add(doPull);
		data.add(isActive);
		data.add(running);
		data.add(silkTouch);
		data.add(numPowering);
		
		if(replaceStack != null)
		{
			data.add(true);
			data.add(replaceStack.itemID);
			data.add(replaceStack.getItemDamage());
		}
		else {
			data.add(false);
		}
		
		data.add(controlType.ordinal());
		data.add(inverse);
		data.add(searched);
		
		data.add(filters.size());
		
		for(MinerFilter filter : filters)
		{
			filter.write(data);
		}
		
		return data;
	}
	
	public ArrayList getSmallPacket(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(3);
		
		data.add(isActive);
		data.add(running);
		data.add(searched);
		
		return data;
	}
	
	public ArrayList getGenericPacket(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(1);
		
		data.add(radius);
		data.add(minY);
		data.add(maxY);
		data.add(doEject);
		data.add(doPull);
		data.add(isActive);
		data.add(running);
		data.add(silkTouch);
		data.add(numPowering);
		
		if(replaceStack != null)
		{
			data.add(true);
			data.add(replaceStack.itemID);
			data.add(replaceStack.getItemDamage());
		}
		else {
			data.add(false);
		}
		
		data.add(controlType.ordinal());
		data.add(inverse);
		data.add(searched);
		
		return data;
	}
	
	public ArrayList getFilterPacket(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(2);
		
		data.add(filters.size());
		
		for(MinerFilter filter : filters)
		{
			filter.write(data);
		}
		
		return data;
	}
	
	@Override
	public double getMaxEnergy() 
	{
		return MekanismUtils.getMaxEnergy(getEnergyMultiplier(), MAX_ELECTRICITY);
	}
	
	@Override
	public boolean isPowered()
	{
		return redstone || numPowering > 0;
	}
	
	@Override
	public RedstoneControl getControlType() 
	{
		return controlType;
	}

	@Override
	public void setControlType(RedstoneControl type) 
	{
		controlType = type;
		MekanismUtils.saveChunk(this);
	}
	
	@Override
	public TileComponentUpgrade getComponent()
	{
		return upgradeComponent;
	}
	
	@Override
	public int getEnergyMultiplier(Object... data) 
	{
		return upgradeComponent.energyMultiplier;
	}

	@Override
	public void setEnergyMultiplier(int multiplier, Object... data) 
	{
		upgradeComponent.energyMultiplier = multiplier;
		MekanismUtils.saveChunk(this);
	}

	@Override
	public int getSpeedMultiplier(Object... data) 
	{
		return upgradeComponent.speedMultiplier;
	}

	@Override
	public void setSpeedMultiplier(int multiplier, Object... data) 
	{
		upgradeComponent.speedMultiplier = multiplier;
		MekanismUtils.saveChunk(this);
	}
	
	@Override
	public boolean supportsUpgrades(Object... data)
	{
		return true;
	}
	
	@Override
    public void setActive(boolean active)
    {
    	isActive = active;
    	
    	if(clientActive != active)
    	{
    		PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Coord4D.get(this), getNetworkedData(new ArrayList())));
    		
    		clientActive = active;
    	}
    }
    
    @Override
    public boolean getActive()
    {
    	return isActive;
    }
    
    @Override
    public boolean renderUpdate()
    {
    	return false;
    }
    
    @Override
    public boolean lightUpdate()
    {
    	return true;
    }
    
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public void onPlace()
	{
		for(int x = xCoord-1; x <= xCoord+1; x++)
		{
			for(int y = yCoord; y <= yCoord+1; y++)
			{
				for(int z = zCoord-1; z <= zCoord+1; z++)
				{
					if(x == xCoord && y == yCoord && z == zCoord)
					{
						continue;
					}
					
					MekanismUtils.makeAdvancedBoundingBlock(worldObj, x, y, z, Coord4D.get(this));
					worldObj.notifyBlocksOfNeighborChange(x, y, z, Mekanism.BoundingBlock.blockID);
				}
			}
		}
	}
	
	@Override
	public boolean canSetFacing(int side)
	{
		return side != 0 && side != 1;
	}

	@Override
	public void onBreak()
	{
		for(int x = xCoord-1; x <= xCoord+1; x++)
		{
			for(int y = yCoord; y <= yCoord+1; y++)
			{
				for(int z = zCoord-1; z <= zCoord+1; z++)
				{
					worldObj.setBlockToAir(x, y, z);
				}
			}
		}
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return InventoryUtils.EMPTY;
	}
	
	public TileEntity getEjectTile()
	{
		ForgeDirection side = ForgeDirection.getOrientation(facing).getOpposite();
		return new Coord4D(xCoord+side.offsetX, yCoord+1, zCoord+side.offsetZ, worldObj.provider.dimensionId).getTileEntity(worldObj);
	}

	@Override
	public int[] getBoundSlots(Coord4D location, int side)
	{
		ForgeDirection dir = ForgeDirection.getOrientation(facing).getOpposite();
		
		Coord4D eject = Coord4D.get(this).translate(dir.offsetX, 1, dir.offsetZ);
		Coord4D pull = Coord4D.get(this).translate(0, 1, 0);
		
		if((location.equals(eject) && side == dir.ordinal()) || (location.equals(pull) && side == 1))
		{
			if(EJECT_INV == null)
			{
				EJECT_INV = new int[27];
				
				for(int i = 0; i < EJECT_INV.length; i++)
				{
					EJECT_INV[i] = i;
				}
			}
			
			return EJECT_INV;
		}
		
		return InventoryUtils.EMPTY;
	}

	@Override
	public boolean canBoundInsert(Coord4D location, int i, ItemStack itemstack) 
	{
		ForgeDirection side = ForgeDirection.getOrientation(facing).getOpposite();
		
		Coord4D eject = Coord4D.get(this).translate(side.offsetX, 1, side.offsetZ);
		Coord4D pull = Coord4D.get(this).translate(0, 1, 0);
		
		if(location.equals(eject))
		{
			return false;
		}
		else if(location.equals(pull))
		{
			if(itemstack != null && replaceStack != null && itemstack.isItemEqual(replaceStack))
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean canBoundExtract(Coord4D location, int i, ItemStack itemstack, int j)
	{
		ForgeDirection side = ForgeDirection.getOrientation(facing).getOpposite();
		
		Coord4D eject = new Coord4D(xCoord+side.offsetX, yCoord+1, zCoord+side.offsetZ, worldObj.provider.dimensionId);
		Coord4D pull = new Coord4D(xCoord, yCoord+1, zCoord, worldObj.provider.dimensionId);
		
		if(location.equals(eject))
		{
			if(itemstack != null && replaceStack != null && itemstack.isItemEqual(replaceStack))
			{
				return false;
			}
			
			return true;
		}
		else if(location.equals(pull))
		{
			return false;
		}
		
		return false;
	}
	
	@Override
	public void onPower()
	{
		numPowering++;
	}
	
	@Override
	public void onNoPower()
	{
		numPowering--;
	}

	@Override
	public String getType()
	{
		return getInvName();
	}
	
	public String[] names = {"setRadius", "setMin", "setMax", "setReplace", "addFilter", "removeFilter", "addOreFilter", "removeOreFilter", "reset", "start", "stop"};
	
	@Override
	public String[] getMethodNames()
	{
		return names;
	}
	
	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
	{
		if(arguments.length > 0)
		{
			int num = 0;
			
			if(arguments[0] instanceof Double)
			{
				num = ((Double)arguments[0]).intValue();
			}
			else if(arguments[0] instanceof String && (method != 6 && method != 7))
			{
				num = Integer.parseInt((String)arguments[0]);
			}
			
			if(num != 0)
			{
				if(method == 0)
				{
					radius = num;
				}
				else if(method == 1)
				{
					minY = num;
				}
				else if(method == 2)
				{
					maxY = num;
				}
				else if(method == 3)
				{
					int meta = 0;
					
					if(arguments.length > 1)
					{
						if(arguments[1] instanceof Double)
						{
							num = ((Double)arguments[1]).intValue();
						}
						else if(arguments[1] instanceof String)
						{
							meta = Integer.parseInt((String)arguments[1]);
						}
					}
					
					replaceStack = new ItemStack(num, 1, meta);
				}
				else if(method == 4)
				{
					int meta = 0;
					
					if(arguments.length > 1)
					{
						if(arguments[1] instanceof Double)
						{
							meta = ((Double)arguments[1]).intValue();
						}
						else if(arguments[1] instanceof String)
						{
							meta = Integer.parseInt((String)arguments[1]);
						}
					}
					
					filters.add(new MItemStackFilter(new ItemStack(num, 1, meta)));
				}
				else if(method == 5)
				{
					Iterator<MinerFilter> iter = filters.iterator();
					
					while(iter.hasNext())
					{
						MinerFilter filter = iter.next();
						
						if(filter instanceof MItemStackFilter)
						{
							if(((MItemStackFilter)filter).itemType.itemID == num)
							{
								iter.remove();
							}
						}
					}
				}
				else if(method == 6)
				{
					String ore = (String)arguments[0];
					MOreDictFilter filter = new MOreDictFilter();
					
					filter.oreDictName = ore;
					filters.add(filter);
				}
				else if(method == 7)
				{
					String ore = (String)arguments[0];
					Iterator<MinerFilter> iter = filters.iterator();
					
					while(iter.hasNext())
					{
						MinerFilter filter = iter.next();
						
						if(filter instanceof MOreDictFilter)
						{
							if(((MOreDictFilter)filter).oreDictName == ore)
							{
								iter.remove();
							}
						}
					}
				}
				else if(method == 8)
				{
					reset();
				}
				else if(method == 9)
				{
					start();
				}
				else if(method == 10)
				{
					stop();
				}
			}
		}
		
		for(EntityPlayer player : playersUsing)
		{
			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Coord4D.get(this), getGenericPacket(new ArrayList())), player);
		}
		
		return null;
	}
	
	@Override
	public boolean canAttachToSide(int side)
	{
		return true;
	}
	
	@Override
	public void attach(IComputerAccess computer) {}
	
	@Override
	public void detach(IComputerAccess computer) {}
}
