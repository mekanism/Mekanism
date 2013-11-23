package mekanism.common.tileentity;

import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.common.HashList;
import mekanism.common.IActiveState;
import mekanism.common.IAdvancedBoundingBlock;
import mekanism.common.IRedstoneControl;
import mekanism.common.IUpgradeTile;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.TileComponentUpgrade;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.miner.MItemStackFilter;
import mekanism.common.miner.MOreDictFilter;
import mekanism.common.miner.MinerFilter;
import mekanism.common.miner.ThreadMinerSearch;
import mekanism.common.miner.ThreadMinerSearch.State;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.transporter.InvStack;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MinerUtils;
import mekanism.common.util.TransporterUtils;
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

public class TileEntityDigitalMiner extends TileEntityElectricBlock implements IPeripheral, IEnergySink, IStrictEnergyAcceptor, IUpgradeTile, IRedstoneControl, IActiveState, IAdvancedBoundingBlock
{
	public List<Object3D> oresToMine = new ArrayList<Object3D>();
	
	public HashList<MinerFilter> filters = new HashList<MinerFilter>();
	
	public ThreadMinerSearch searcher = new ThreadMinerSearch(this);
	
	public final double ENERGY_USAGE = 100;
	
	public int radius;
	
	public int minY = 0;
	public int maxY = 60;
	
	public boolean doEject = false;
	public boolean doPull = false;
	
	public int delay;
	
	public int clientToMine;
	
	public ItemStack replaceStack;
	
	public boolean isActive;
	public boolean clientActive;
	
	public boolean silkTouch;
	
	public boolean running;
	
	public double prevEnergy;
	
	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
	public TileComponentUpgrade upgradeComponent = new TileComponentUpgrade(this, 28);
	
	public TileEntityDigitalMiner()
	{
		super("Digital Miner", MachineType.DIGITAL_MINER.baseEnergy);
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
			
			if(running && getEnergy() >= getPerTick() && searcher.state == State.FINISHED && oresToMine.size() > 0)
			{
				setActive(true);
				
				if(delay > 0)
				{
					delay--;
				}
				
				setEnergy(getEnergy()-getPerTick());
				
				if(delay == 0)
				{
					Set<Object3D> toRemove = new HashSet<Object3D>();
					
					for(Object3D obj : oresToMine)
					{
						int id = obj.getBlockId(worldObj);
						int meta = obj.getMetadata(worldObj);
						
						if(id == 0)
						{
							toRemove.add(obj);
							continue;
						}
						
						boolean hasFilter = false;
						
						for(MinerFilter filter : filters)
						{
							if(filter.canFilter(new ItemStack(id, 1, meta)))
							{
								hasFilter = true;
								break;
							}
						}
						
						if(!hasFilter)
						{
							toRemove.add(obj);
							continue;
						}
						
						List<ItemStack> drops = MinerUtils.getDrops(worldObj, obj, silkTouch);
						
						if(canInsert(drops))
						{
							add(drops);
							
							setReplace(obj);
							toRemove.add(obj);
							
							worldObj.playAuxSFXAtEntity(null, 2001, obj.xCoord, obj.yCoord, obj.zCoord, id + (meta << 12));
							
							delay = getDelay();
							
							break;
						}
					}
					
					for(Object3D obj : toRemove)
					{
						oresToMine.remove(obj);
					}
				}
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}
			
			if(doEject && getTopEject(false, null) != null && getEjectInv() != null)
			{
				if(getEjectInv() instanceof IInventory)
				{
					ItemStack remains = InventoryUtils.putStackInInventory((IInventory)getEjectInv(), getTopEject(false, null), ForgeDirection.getOrientation(facing).getOpposite().ordinal(), false);
					
					getTopEject(true, remains);
				}
				else if(getEjectInv() instanceof TileEntityLogisticalTransporter)
				{
					if(TransporterUtils.insert(getEjectTile(), (TileEntityLogisticalTransporter)getEjectInv(), getTopEject(false, null), null))
					{
						getTopEject(true, null);
					}
				}
			}
			
			if(playersUsing.size() > 0)
			{
				for(EntityPlayer player : playersUsing)
				{
					PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Object3D.get(this), getSmallPacket(new ArrayList())), player);
				}
			}
			
			prevEnergy = getEnergy();
		}
	}
	
	public double getPerTick()
	{
		double ret = MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_USAGE);
		
		if(silkTouch)
		{
			ret *= 6;
		}
		
		return ret;
	}
	
	public int getDelay()
	{
		return (int)Math.pow((9-getSpeedMultiplier()), 2);
	}
	
	public void setReplace(Object3D obj)
	{		
		ItemStack stack = getReplace();
		
		if(stack != null)
		{
			worldObj.setBlock(obj.xCoord, obj.yCoord, obj.zCoord, replaceStack.itemID, replaceStack.getItemDamage(), 3);
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
		return new Object3D(xCoord, yCoord+2, zCoord).getTileEntity(worldObj);
	}
	
	public TileEntity getEjectInv()
	{
		ForgeDirection side = ForgeDirection.getOrientation(facing).getOpposite();
		
		return new Object3D(xCoord+(side.offsetX*2), yCoord+1, zCoord+(side.offsetZ*2), worldObj.provider.dimensionId).getTileEntity(worldObj);
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
		if(searcher.state == State.IDLE)
		{
			searcher.start();
		}
		
		running = true;
		
		MekanismUtils.saveChunk(this);
	}
	
	public void stop()
	{
		if(searcher.state == State.SEARCHING)
		{
			searcher.interrupt();
			reset();
			
			return;
		}
		else if(searcher.state == State.FINISHED)
		{
			running = false;
		}
		
		MekanismUtils.saveChunk(this);
	}
	
	public void reset()
	{
		searcher = new ThreadMinerSearch(this);
		running = false;
		oresToMine.clear();
		
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
				PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())), player);
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
        searcher.state = State.values()[nbtTags.getInteger("state")];
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
        
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
    	
    	if(nbtTags.hasKey("oresToMine"))
    	{
    		NBTTagList tagList = nbtTags.getTagList("oresToMine");
    		
    		for(int i = 0; i < tagList.tagCount(); i++)
    		{
    			oresToMine.add(Object3D.read((NBTTagCompound)tagList.tagAt(i)));
    		}
    	}
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        if(searcher.state == State.SEARCHING)
        {
        	reset();
        }
        
        nbtTags.setInteger("radius", radius);
        nbtTags.setInteger("minY", minY);
        nbtTags.setInteger("maxY", maxY);
        nbtTags.setBoolean("doEject", doEject);
        nbtTags.setBoolean("doPull", doPull);
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setBoolean("running", running);
        nbtTags.setInteger("delay", delay);
        nbtTags.setBoolean("silkTouch", silkTouch);
        nbtTags.setInteger("state", searcher.state.ordinal());
        nbtTags.setInteger("controlType", controlType.ordinal());
        
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
        
        NBTTagList miningOreTags = new NBTTagList();
        
        for(Object3D obj : oresToMine)
        {
        	miningOreTags.appendTag(obj.write(new NBTTagCompound()));
        }
        
        if(miningOreTags.tagCount() != 0)
        {
        	nbtTags.setTag("oresToMine", miningOreTags);
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
			
			for(EntityPlayer player : playersUsing)
			{
				PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Object3D.get(this), getGenericPacket(new ArrayList())), player);
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
			searcher.state = State.values()[dataStream.readInt()];
			
			if(dataStream.readBoolean())
			{
				replaceStack = new ItemStack(dataStream.readInt(), 1, dataStream.readInt());
			}
			else {
				replaceStack = null;
			}
			
			clientToMine = dataStream.readInt();
			controlType = RedstoneControl.values()[dataStream.readInt()];
			
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
			searcher.state = State.values()[dataStream.readInt()];
			
			if(dataStream.readBoolean())
			{
				replaceStack = new ItemStack(dataStream.readInt(), 1, dataStream.readInt());
			}
			else {
				replaceStack = null;
			}
			
			clientToMine = dataStream.readInt();
			controlType = RedstoneControl.values()[dataStream.readInt()];
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
			clientToMine = dataStream.readInt();
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
		data.add(searcher.state.ordinal());
		
		if(replaceStack != null)
		{
			data.add(true);
			data.add(replaceStack.itemID);
			data.add(replaceStack.getItemDamage());
		}
		else {
			data.add(false);
		}
		
		data.add(oresToMine.size());
		data.add(controlType.ordinal());
		
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
		data.add(oresToMine.size());
		
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
		data.add(searcher.state.ordinal());
		
		if(replaceStack != null)
		{
			data.add(true);
			data.add(replaceStack.itemID);
			data.add(replaceStack.getItemDamage());
		}
		else {
			data.add(false);
		}
		
		data.add(oresToMine.size());
		data.add(controlType.ordinal());
		
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
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return true;
	}
	
	public double demandedEnergyUnits()
	{
		return (getMaxEnergy() - getEnergy())*Mekanism.TO_IC2;
	}

	@Override
	public double injectEnergyUnits(ForgeDirection direction, double amount)
	{
		if(Object3D.get(this).getFromSide(direction).getTileEntity(worldObj) instanceof TileEntityUniversalCable)
		{
			return amount;
		}
		
		double givenEnergy = amount*Mekanism.FROM_IC2;
    	double rejects = 0;
    	double neededEnergy = getMaxEnergy()-getEnergy();
    	
    	if(givenEnergy < neededEnergy)
    	{
    		electricityStored += givenEnergy;
    	}
    	else if(givenEnergy > neededEnergy)
    	{
    		electricityStored += neededEnergy;
    		rejects = givenEnergy-neededEnergy;
    	}
    	
    	return rejects*Mekanism.TO_IC2;
	}

	@Override
	public int getMaxSafeInput()
	{
		return 2048;
	}
	
	@Override
	public double transferEnergyToAcceptor(double amount)
	{
    	double rejects = 0;
    	double neededGas = getMaxEnergy()-getEnergy();
    	
    	if(amount <= neededGas)
    	{
    		electricityStored += amount;
    	}
    	else {
    		electricityStored += neededGas;
    		rejects = amount-neededGas;
    	}
    	
    	return rejects;
	}
	
	@Override
	public double getMaxEnergy() 
	{
		return MekanismUtils.getEnergy(getEnergyMultiplier(), MAX_ELECTRICITY);
	}
	
	@Override
	public boolean canReceiveEnergy(ForgeDirection side)
	{
		return true;
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
    		PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
    		
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
    	return true;
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
					
					MekanismUtils.makeAdvancedBoundingBlock(worldObj, x, y, z, Object3D.get(this));
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
			for(int y = yCoord; y <= yCoord+2; y++)
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
		return null;
	}
	
	public TileEntity getEjectTile()
	{
		ForgeDirection side = ForgeDirection.getOrientation(facing).getOpposite();
		return new Object3D(xCoord+side.offsetX, yCoord+1, zCoord+side.offsetZ, worldObj.provider.dimensionId).getTileEntity(worldObj);
	}

	@Override
	public int[] getBoundSlots(Object3D location, int side)
	{
		ForgeDirection dir = ForgeDirection.getOrientation(facing).getOpposite();
		
		Object3D eject = new Object3D(xCoord+dir.offsetX, yCoord+1, zCoord+dir.offsetZ, worldObj.provider.dimensionId);
		Object3D pull = new Object3D(xCoord, yCoord+1, zCoord);
		
		if((location.equals(eject) && side == dir.ordinal()) || (location.equals(pull) && side == 1))
		{
			int[] ret = new int[27];
			
			for(int i = 0; i < ret.length; i++)
			{
				ret[i] = i;
			}
			
			return ret;
		}
		
		return null;
	}

	@Override
	public boolean canBoundInsert(Object3D location, int i, ItemStack itemstack) 
	{
		ForgeDirection side = ForgeDirection.getOrientation(facing).getOpposite();
		
		Object3D eject = new Object3D(xCoord+side.offsetX, yCoord+1, zCoord+side.offsetZ, worldObj.provider.dimensionId);
		Object3D pull = new Object3D(xCoord, yCoord+1, zCoord);
		
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
	public boolean canBoundExtract(Object3D location, int i, ItemStack itemstack, int j)
	{
		ForgeDirection side = ForgeDirection.getOrientation(facing).getOpposite();
		
		Object3D eject = new Object3D(xCoord+side.offsetX, yCoord+1, zCoord+side.offsetZ, worldObj.provider.dimensionId);
		Object3D pull = new Object3D(xCoord, yCoord+1, zCoord);
		
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
	public String getType() {
		return "Digital Miner";
	}
	public String[] names = 
        {
                "setRadius",
                "setMin",
                "setMax",
                "setReplace",
                "addFilter",
                "removeFilter",
                "addOreFilter",
                "removeOreFilter"
        };
	@Override
	public String[] getMethodNames() {
		return names;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,
			int method, Object[] arguments) throws Exception {
		if(arguments.length>0)
        {
                int num = 0;
                             
                if(arguments[0] instanceof Double)
                {
                        num = ((Double)arguments[0]).intValue();
                }
                if(arguments[0] instanceof String&&(method!=6&&method!=7))
                {
                        num = Integer.parseInt((String)arguments[0]);
                }
                
                if(num!=0)
                {
                        if(method==0)
                        {
                                this.radius=num;
                        }
                        if(method==1)
                        {
                                this.minY=num;
                        }
                        if(method==2)
                        {
                                this.maxY=num;
                        }
                        if(method==3){
                        	//replace
                        	int meta=0;
                        	if(arguments.length>1){
                        		if(arguments[1] instanceof Double)
                        		{
                        			num = ((Double)arguments[1]).intValue();
                        		}
                        		if(arguments[1] instanceof String)
                        		{
                        			meta = Integer.parseInt((String)arguments[1]);
                        		}
                        	}
                        	this.replaceStack=new ItemStack(num,1,meta);
                        }
                        if(method==4){
                        	int meta=0;

                        	if(arguments.length>1){
                        		if(arguments[1] instanceof Double)
                        		{
                        			meta = ((Double)arguments[1]).intValue();
                        		}
                        		if(arguments[1] instanceof String)
                        		{
                        			meta = Integer.parseInt((String)arguments[1]);
                        		}
                        	}
                        	this.filters.add(new MItemStackFilter(new ItemStack(num,1,meta)));
                        }
                        if(method==5){
                        	Iterator<MinerFilter> iter=this.filters.iterator();
                        	while(iter.hasNext()){
                        		MinerFilter filter=iter.next();
                        		if(filter instanceof MItemStackFilter){
                        			if(((MItemStackFilter) filter).itemType.itemID==num){
                        				iter.remove();
                        			}
                        		}
                        	}
                        }
                        if(method==6){
                        	String ore=(String) arguments[0];
                        	MOreDictFilter filter=new MOreDictFilter();
                        	filter.oreDictName=ore;
                        	filters.add(filter);
                        }
                        if(method==7){

                        	String ore=(String) arguments[0];
                        	Iterator<MinerFilter> iter=this.filters.iterator();
                        	while(iter.hasNext()){
                        		MinerFilter filter=iter.next();
                        		if(filter instanceof MOreDictFilter){
                        			if(((MOreDictFilter) filter).oreDictName==ore){
                        				iter.remove();
                        			}
                        		}
                        	}
                        }
                }
        }
		for(EntityPlayer player : playersUsing)
		{
			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Object3D.get(this), getGenericPacket(new ArrayList())), player);
		}
        
        
        return null;
	}

	@Override
	public boolean canAttachToSide(int side) {
		return true;
	}

	@Override
	public void attach(IComputerAccess computer) {
		
	}

	@Override
	public void detach(IComputerAccess computer) {
		
	}
}
