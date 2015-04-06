package mekanism.common.tile;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.Range4D;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.Upgrade;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IAdvancedBoundingBlock;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITransporterTile;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.content.miner.MItemStackFilter;
import mekanism.common.content.miner.MOreDictFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.ThreadMinerSearch;
import mekanism.common.content.miner.ThreadMinerSearch.State;
import mekanism.common.content.transporter.InvStack;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MinerUtils;
import mekanism.common.util.TransporterUtils;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import io.netty.buffer.ByteBuf;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
public class TileEntityDigitalMiner extends TileEntityElectricBlock implements IPeripheral, IUpgradeTile, IRedstoneControl, IActiveState, ISustainedData, IAdvancedBoundingBlock
{
	public static int[] EJECT_INV;

	public Map<Chunk3D, BitSet> oresToMine = new HashMap<Chunk3D, BitSet>();
	public Map<Integer, MinerFilter> replaceMap = new HashMap<Integer, MinerFilter>();

	public HashList<MinerFilter> filters = new HashList<MinerFilter>();

	public ThreadMinerSearch searcher = new ThreadMinerSearch(this);

	public final double BASE_ENERGY_USAGE = usage.digitalMinerUsage;

	public double energyUsage = usage.digitalMinerUsage;

	public int radius;

	public boolean inverse;

	public int minY = 0;
	public int maxY = 60;

	public boolean doEject = false;
	public boolean doPull = false;
	
	public ItemStack missingStack = null;
	
	public int BASE_DELAY = 80;

	public int delay;

	public int delayLength = BASE_DELAY;

	public int clientToMine;

	public boolean isActive;
	public boolean clientActive;

	public boolean silkTouch;

	public boolean running;

	public double prevEnergy;

	public int delayTicks;

	public boolean initCalc = false;

	public int numPowering;
	
	public boolean clientRendering = false;

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
			for(EntityPlayer player : (HashSet<EntityPlayer>)playersUsing.clone())
			{
				if(player.openContainer instanceof ContainerNull || player.openContainer instanceof ContainerFilter)
				{
					player.closeScreen();
				}
			}
		}

		if(!worldObj.isRemote)
		{
			if(!initCalc)
			{
				if(searcher.state == State.FINISHED)
				{
					reset();
					start();
				}

				initCalc = true;
			}

			ChargeUtils.discharge(27, this);

			if(MekanismUtils.canFunction(this) && running && getEnergy() >= getPerTick() && searcher.state == State.FINISHED && oresToMine.size() > 0)
			{
				setActive(true);

				if(delay > 0)
				{
					delay--;
				}

				setEnergy(getEnergy()-getPerTick());

				if(delay == 0)
				{
					Set<Chunk3D> toRemove = new HashSet<Chunk3D>();
					boolean did = false;
					
					for(Chunk3D chunk : oresToMine.keySet())
					{
						BitSet set = oresToMine.get(chunk);
						int next = 0;
	
						while(true)
						{
							int index = set.nextSetBit(next);
							Coord4D coord = getCoordFromIndex(index);
	
							if(index == -1)
							{
								toRemove.add(chunk);
								break;
							}
	
							if(!coord.exists(worldObj))
							{
								set.clear(index);
								
								if(set.cardinality() == 0)
								{
									toRemove.add(chunk);
								}
								
								next = index + 1;
								continue;
							}
	
							Block block = coord.getBlock(worldObj);
							int meta = coord.getMetadata(worldObj);
	
							if(block == null || coord.isAirBlock(worldObj))
							{
								set.clear(index);
								
								if(set.cardinality() == 0)
								{
									toRemove.add(chunk);
								}
								
								next = index + 1;
								continue;
							}
	
							boolean hasFilter = false;
	
							for(MinerFilter filter : filters)
							{
								if(filter.canFilter(new ItemStack(block, 1, meta)))
								{
									hasFilter = true;
									break;
								}
							}
	
							if(inverse ? hasFilter : !hasFilter)
							{
								set.clear(index);
								
								if(set.cardinality() == 0)
								{
									toRemove.add(chunk);
									break;
								}
								
								next = index + 1;
								continue;
							}
	
							List<ItemStack> drops = MinerUtils.getDrops(worldObj, coord, silkTouch);
	
							if(canInsert(drops) && setReplace(coord, index))
							{
								did = true;
								add(drops);
								set.clear(index);
								
								if(set.cardinality() == 0)
								{
									toRemove.add(chunk);
								}
	
								worldObj.playAuxSFXAtEntity(null, 2001, coord.xCoord, coord.yCoord, coord.zCoord, Block.getIdFromBlock(block) + (meta << 12));
	
								missingStack = null;
							}
	
							break;
						}
						
						if(did)
						{
							break;
						}
					}
					
					for(Chunk3D chunk : toRemove)
					{
						oresToMine.remove(chunk);
					}
					
					delay = getDelay();
				}
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}

			if(doEject && delayTicks == 0 && getTopEject(false, null) != null && getEjectInv() != null && getEjectTile() != null)
			{
				if(getEjectInv() instanceof IInventory)
				{
					ItemStack remains = InventoryUtils.putStackInInventory((IInventory)getEjectInv(), getTopEject(false, null), ForgeDirection.getOrientation(facing).getOpposite().ordinal(), false);

					getTopEject(true, remains);
				}
				else if(getEjectInv() instanceof ITransporterTile)
				{
					ItemStack rejected = TransporterUtils.insert(getEjectTile(), ((ITransporterTile)getEjectInv()).getTransmitter(), getTopEject(false, null), null, true, 0);

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
					Mekanism.packetHandler.sendTo(new TileEntityMessage(Coord4D.get(this), getSmallPacket(new ArrayList())), (EntityPlayerMP)player);
				}
			}

			prevEnergy = getEnergy();
		}
	}

	public double getPerTick()
	{
		double ret = energyUsage;

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
		return delayLength;
	}

	/*
	 * returns false if unsuccessful
	 */
	public boolean setReplace(Coord4D obj, int index)
	{
		ItemStack stack = getReplace(index);
		 
		if(stack != null)
		{
			worldObj.setBlock(obj.xCoord, obj.yCoord, obj.zCoord, Block.getBlockFromItem(stack.getItem()), stack.getItemDamage(), 3);

			if(obj.getBlock(worldObj) != null && !obj.getBlock(worldObj).canBlockStay(worldObj, obj.xCoord, obj.yCoord, obj.zCoord))
			{
				obj.getBlock(worldObj).dropBlockAsItem(worldObj, obj.xCoord, obj.yCoord, obj.zCoord, obj.getMetadata(worldObj), 1);
				worldObj.setBlockToAir(obj.xCoord, obj.yCoord, obj.zCoord);
			}
			
			return true;
		}
		else {
			MinerFilter filter = replaceMap.get(index);
			
			if(filter == null || (filter.replaceStack == null || !filter.requireStack))
			{
				worldObj.setBlockToAir(obj.xCoord, obj.yCoord, obj.zCoord);
				
				return true;
			}
			
			missingStack = filter.replaceStack;
			
			return false;
		}
	}

	public ItemStack getReplace(int index)
	{
		MinerFilter filter = replaceMap.get(index);
		
		if(filter == null || filter.replaceStack == null)
		{
			return null;
		}

		for(int i = 0; i < 27; i++)
		{
			if(inventory[i] != null && inventory[i].isItemEqual(filter.replaceStack))
			{
				inventory[i].stackSize--;

				if(inventory[i].stackSize == 0)
				{
					inventory[i] = null;
				}

				return MekanismUtils.size(filter.replaceStack, 1);
			}
		}

		if(doPull && getPullInv() instanceof IInventory)
		{
			InvStack stack = InventoryUtils.takeDefinedItem((IInventory)getPullInv(), 1, filter.replaceStack.copy(), 1, 1);

			if(stack != null)
			{
				stack.use();
				return MekanismUtils.size(filter.replaceStack, 1);
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
				if(isReplaceStack(stack))
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
			if(stack == null)
			{
				continue;
			}
			
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
		replaceMap.clear();
		missingStack = null;

		MekanismUtils.saveChunk(this);
	}
	
	public boolean isReplaceStack(ItemStack stack)
	{
		for(MinerFilter filter : filters)
		{
			if(filter.replaceStack != null && filter.replaceStack.isItemEqual(stack))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public int getSize()
	{
		int size = 0;
		
		for(Chunk3D chunk : oresToMine.keySet())
		{
			size += oresToMine.get(chunk).cardinality();
		}
		
		return size;
	}

	@Override
	public void openInventory()
	{
		super.openInventory();

		if(!worldObj.isRemote)
		{
			for(EntityPlayer player : playersUsing)
			{
				Mekanism.packetHandler.sendTo(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), (EntityPlayerMP)player);
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
		searcher.state = State.values()[nbtTags.getInteger("state")];
		controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
		inverse = nbtTags.getBoolean("inverse");

		if(nbtTags.hasKey("filters"))
		{
			NBTTagList tagList = nbtTags.getTagList("filters", NBT.TAG_COMPOUND);

			for(int i = 0; i < tagList.tagCount(); i++)
			{
				filters.add(MinerFilter.readFromNBT((NBTTagCompound)tagList.getCompoundTagAt(i)));
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
		nbtTags.setInteger("numPowering", numPowering);
		nbtTags.setInteger("state", searcher.state.ordinal());
		nbtTags.setInteger("controlType", controlType.ordinal());
		nbtTags.setBoolean("inverse", inverse);

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
	public void handlePacketData(ByteBuf dataStream)
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
				//Unneeded at the moment
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
			else if(type == 11)
			{
				// Move filter up
				int filterIndex = dataStream.readInt();
				filters.swap( filterIndex, filterIndex - 1 );
				openInventory();
			}
			else if(type == 12)
			{
				// Move filter down
				int filterIndex = dataStream.readInt();
				filters.swap( filterIndex, filterIndex + 1 );
				openInventory();
			}
			
			MekanismUtils.saveChunk(this);

			for(EntityPlayer player : playersUsing)
			{
				Mekanism.packetHandler.sendTo(new TileEntityMessage(Coord4D.get(this), getGenericPacket(new ArrayList())), (EntityPlayerMP)player);
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
			searcher.state = State.values()[dataStream.readInt()];
			clientToMine = dataStream.readInt();
			controlType = RedstoneControl.values()[dataStream.readInt()];
			inverse = dataStream.readBoolean();
			
			if(dataStream.readBoolean())
			{
				missingStack = new ItemStack(Item.getItemById(dataStream.readInt()), 1, dataStream.readInt());
			}
			else {
				missingStack = null;
			}

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
			searcher.state = State.values()[dataStream.readInt()];
			clientToMine = dataStream.readInt();
			controlType = RedstoneControl.values()[dataStream.readInt()];
			inverse = dataStream.readBoolean();
			
			if(dataStream.readBoolean())
			{
				missingStack = new ItemStack(Item.getItemById(dataStream.readInt()), 1, dataStream.readInt());
			}
			else {
				missingStack = null;
			}
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
			
			if(dataStream.readBoolean())
			{
				missingStack = new ItemStack(Item.getItemById(dataStream.readInt()), 1, dataStream.readInt());
			}
			else {
				missingStack = null;
			}
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
		data.add(searcher.state.ordinal());
		
		if(searcher.state == State.SEARCHING)
		{
			data.add(searcher.found);
		}
		else {
			data.add(getSize());
		}

		data.add(controlType.ordinal());
		data.add(inverse);
		
		if(missingStack != null)
		{
			data.add(true);
			data.add(MekanismUtils.getID(missingStack));
			data.add(missingStack.getItemDamage());
		}
		else {
			data.add(false);
		}

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

		if(searcher.state == State.SEARCHING)
		{
			data.add(searcher.found);
		}
		else {
			data.add(getSize());
		}
		
		if(missingStack != null)
		{
			data.add(true);
			data.add(MekanismUtils.getID(missingStack));
			data.add(missingStack.getItemDamage());
		}
		else {
			data.add(false);
		}

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
		data.add(searcher.state.ordinal());

		if(searcher.state == State.SEARCHING)
		{
			data.add(searcher.found);
		}
		else {
			data.add(getSize());
		}

		data.add(controlType.ordinal());
		data.add(inverse);
		
		if(missingStack != null)
		{
			data.add(true);
			data.add(MekanismUtils.getID(missingStack));
			data.add(missingStack.getItemDamage());
		}
		else {
			data.add(false);
		}

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

	public int getTotalSize()
	{
		return getDiameter()*getDiameter()*(maxY-minY+1);
	}

	public int getDiameter()
	{
		return (radius*2)+1;
	}

	public Coord4D getStartingCoord()
	{
		return new Coord4D(xCoord-radius, minY, zCoord-radius, worldObj.provider.dimensionId);
	}

	public Coord4D getCoordFromIndex(int index)
	{
		int diameter = getDiameter();
		Coord4D start = getStartingCoord();

		int x = start.xCoord+index%diameter;
		int z = start.zCoord+(index/diameter)%diameter;
		int y = start.yCoord+(index/diameter/diameter);

		return new Coord4D(x, y, z, worldObj.provider.dimensionId);
	}

	@Override
	public boolean isPowered()
	{
		return redstone || numPowering > 0;
	}

	@Override
	public boolean canPulse()
	{
		return false;
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
	public void setActive(boolean active)
	{
		isActive = active;

		if(clientActive != active)
		{
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));

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
		            worldObj.func_147453_f(x, y, z, getBlockType());
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
			if(itemstack != null && isReplaceStack(itemstack))
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
			if(itemstack != null && isReplaceStack(itemstack))
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
	@Method(modid = "ComputerCraft")
	public String getType()
	{
		return getInventoryName();
	}

	public String[] names = {"setRadius", "setMin", "setMax", "addFilter", "removeFilter", "addOreFilter", "removeOreFilter", "reset", "start", "stop"};

	@Override
	@Method(modid = "ComputerCraft")
	public String[] getMethodNames()
	{
		return names;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
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
							meta = ((Double)arguments[1]).intValue();
						}
						else if(arguments[1] instanceof String)
						{
							meta = Integer.parseInt((String)arguments[1]);
						}
					}

					filters.add(new MItemStackFilter(new ItemStack(Item.getItemById(num), 1, meta)));
				}
				else if(method == 4)
				{
					Iterator<MinerFilter> iter = filters.iterator();

					while(iter.hasNext())
					{
						MinerFilter filter = iter.next();

						if(filter instanceof MItemStackFilter)
						{
							if(MekanismUtils.getID(((MItemStackFilter)filter).itemType) == num)
							{
								iter.remove();
							}
						}
					}
				}
				else if(method == 5)
				{
					String ore = (String)arguments[0];
					MOreDictFilter filter = new MOreDictFilter();

					filter.oreDictName = ore;
					filters.add(filter);
				}
				else if(method == 6)
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
				else if(method == 7)
				{
					reset();
				}
				else if(method == 8)
				{
					start();
				}
				else if(method == 9)
				{
					stop();
				}
			}
		}

		for(EntityPlayer player : playersUsing)
		{
			Mekanism.packetHandler.sendTo(new TileEntityMessage(Coord4D.get(this), getGenericPacket(new ArrayList())), (EntityPlayerMP)player);
		}

		return null;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public boolean equals(IPeripheral other)
	{
		return this == other;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public void attach(IComputerAccess computer) {}

	@Override
	@Method(modid = "ComputerCraft")
	public void detach(IComputerAccess computer) {}

	@Override
	public NBTTagCompound getFilterData(NBTTagCompound nbtTags)
	{
		nbtTags.setInteger("radius", radius);
		nbtTags.setInteger("minY", minY);
		nbtTags.setInteger("maxY", maxY);
		nbtTags.setBoolean("doEject", doEject);
		nbtTags.setBoolean("doPull", doPull);
		nbtTags.setBoolean("silkTouch", silkTouch);
		nbtTags.setInteger("controlType", controlType.ordinal());
		nbtTags.setBoolean("inverse", inverse);

		NBTTagList filterTags = new NBTTagList();

		for(MinerFilter filter : filters)
		{
			filterTags.appendTag(filter.write(new NBTTagCompound()));
		}

		if(filterTags.tagCount() != 0)
		{
			nbtTags.setTag("filters", filterTags);
		}
		
		return nbtTags;
	}

	@Override
	public void setFilterData(NBTTagCompound nbtTags)
	{
		radius = nbtTags.getInteger("radius");
		minY = nbtTags.getInteger("minY");
		maxY = nbtTags.getInteger("maxY");
		doEject = nbtTags.getBoolean("doEject");
		doPull = nbtTags.getBoolean("doPull");
		silkTouch = nbtTags.getBoolean("silkTouch");
		controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
		inverse = nbtTags.getBoolean("inverse");

		if(nbtTags.hasKey("filters"))
		{
			NBTTagList tagList = nbtTags.getTagList("filters", NBT.TAG_COMPOUND);

			for(int i = 0; i < tagList.tagCount(); i++)
			{
				filters.add(MinerFilter.readFromNBT((NBTTagCompound)tagList.getCompoundTagAt(i)));
			}
		}
	}

	@Override
	public String getDataType()
	{
		return "tooltip.filterCard.digitalMiner";
	}
	
	public void writeSustainedData(ItemStack itemStack) 
	{
		itemStack.stackTagCompound.setBoolean("hasMinerConfig", true);

		itemStack.stackTagCompound.setInteger("radius", radius);
		itemStack.stackTagCompound.setInteger("minY", minY);
		itemStack.stackTagCompound.setInteger("maxY", maxY);
		itemStack.stackTagCompound.setBoolean("doEject", doEject);
		itemStack.stackTagCompound.setBoolean("doPull", doPull);
		itemStack.stackTagCompound.setBoolean("silkTouch", silkTouch);
		itemStack.stackTagCompound.setBoolean("inverse", inverse);

		NBTTagList filterTags = new NBTTagList();

		for(MinerFilter filter : filters)
		{
			filterTags.appendTag(filter.write(new NBTTagCompound()));
		}

		if(filterTags.tagCount() != 0)
		{
			itemStack.stackTagCompound.setTag("filters", filterTags);
		}
	}

	@Override
	public void readSustainedData(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound.hasKey("hasMinerConfig"))
		{
			radius = itemStack.stackTagCompound.getInteger("radius");
			minY = itemStack.stackTagCompound.getInteger("minY");
			maxY = itemStack.stackTagCompound.getInteger("maxY");
			doEject = itemStack.stackTagCompound.getBoolean("doEject");
			doPull = itemStack.stackTagCompound.getBoolean("doPull");
			silkTouch = itemStack.stackTagCompound.getBoolean("silkTouch");
			inverse = itemStack.stackTagCompound.getBoolean("inverse");

			if(itemStack.stackTagCompound.hasKey("filters"))
			{
				NBTTagList tagList = itemStack.stackTagCompound.getTagList("filters", NBT.TAG_COMPOUND);

				for(int i = 0; i < tagList.tagCount(); i++)
				{
					filters.add(MinerFilter.readFromNBT((NBTTagCompound)tagList.getCompoundTagAt(i)));
				}
			}
		}
	}

	@Override
	public void recalculateUpgradables(Upgrade upgrade)
	{
		super.recalculateUpgradables(upgrade);

		switch(upgrade)
		{
			case SPEED:
				delayLength = MekanismUtils.getTicks(this, BASE_DELAY);
			case ENERGY:
				energyUsage = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_USAGE);
				maxEnergy = MekanismUtils.getMaxEnergy(this, BASE_MAX_ENERGY);
			default:
				break;
		}
	}
}
