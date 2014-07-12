package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.HashList;
import mekanism.common.IActiveState;
import mekanism.common.IAdvancedBoundingBlock;
import mekanism.common.ILogisticalTransporter;
import mekanism.common.IRedstoneControl;
import mekanism.common.IUpgradeTile;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.miner.MItemStackFilter;
import mekanism.common.miner.MOreDictFilter;
import mekanism.common.miner.MinerFilter;
import mekanism.common.miner.ThreadMinerSearch;
import mekanism.common.miner.ThreadMinerSearch.State;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
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
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
public class TileEntityDigitalMiner extends TileEntityElectricBlock implements IPeripheral, IUpgradeTile, IRedstoneControl, IActiveState, IAdvancedBoundingBlock
{
	public static int[] EJECT_INV;

	public BitSet oresToMine = new BitSet();

	public HashList<MinerFilter> filters = new HashList<MinerFilter>();

	public ThreadMinerSearch searcher = new ThreadMinerSearch(this);

	public final double ENERGY_USAGE = Mekanism.digitalMinerUsage;

	public int radius;

	public boolean inverse;

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

	public int delayTicks;

	public boolean initCalc = false;

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

			if(MekanismUtils.canFunction(this) && running && getEnergy() >= getPerTick() && searcher.state == State.FINISHED && oresToMine.cardinality() > 0)
			{
				setActive(true);

				if(delay > 0)
				{
					delay--;
				}

				setEnergy(getEnergy()-getPerTick());

				if(delay == 0)
				{
					Set<Integer> toRemove = new HashSet<Integer>();

					int next = 0;

					while(true)
					{
						int index = oresToMine.nextSetBit(next);
						Coord4D coord = getCoordFromIndex(index);

						if(index == -1)
						{
							break;
						}

						if(!coord.exists(worldObj))
						{
							toRemove.add(index);
							next = index + 1;
							continue;
						}

						Block block = coord.getBlock(worldObj);
						int meta = coord.getMetadata(worldObj);

						if(block == null || coord.isAirBlock(worldObj))
						{
							toRemove.add(index);
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
							toRemove.add(index);
							next = index + 1;
							continue;
						}

						List<ItemStack> drops = MinerUtils.getDrops(worldObj, coord, silkTouch);

						if(canInsert(drops))
						{
							add(drops);

							setReplace(coord);
							toRemove.add(index);

							worldObj.playAuxSFXAtEntity(null, 2001, coord.xCoord, coord.yCoord, coord.zCoord, Block.getIdFromBlock(block) + (meta << 12));

							delay = getDelay();
						}

						break;
					}

					for(Integer i : toRemove)
					{
						oresToMine.clear(i);
					}
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
					Mekanism.packetHandler.sendTo(new TileEntityMessage(Coord4D.get(this), getSmallPacket(new ArrayList())), (EntityPlayerMP)player);
				}
			}

			prevEnergy = getEnergy();
		}
	}

	public double getPerTick()
	{
		double ret = MekanismUtils.getEnergyPerTick(this, ENERGY_USAGE);

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
		return MekanismUtils.getTicks(this, 80);
	}

	public void setReplace(Coord4D obj)
	{
		ItemStack stack = getReplace();

		if(stack != null)
		{
			worldObj.setBlock(obj.xCoord, obj.yCoord, obj.zCoord, Block.getBlockFromItem(replaceStack.getItem()), replaceStack.getItemDamage(), 3);

			if(obj.getBlock(worldObj) != null && !obj.getBlock(worldObj).canBlockStay(worldObj, obj.xCoord, obj.yCoord, obj.zCoord))
			{
				obj.getBlock(worldObj).dropBlockAsItem(worldObj, obj.xCoord, obj.yCoord, obj.zCoord, obj.getMetadata(worldObj), 1);
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

		if(nbtTags.hasKey("replaceStack"))
		{
			replaceStack = ItemStack.loadItemStackFromNBT(nbtTags.getCompoundTag("replaceStack"));
		}

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

		if(replaceStack != null)
		{
			nbtTags.setTag("replaceStack", replaceStack.writeToNBT(new NBTTagCompound()));
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
				boolean doNull = dataStream.readBoolean();

				if(!doNull)
				{
					replaceStack = new ItemStack(Block.getBlockById(dataStream.readInt()), 1, dataStream.readInt());
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

			if(dataStream.readBoolean())
			{
				replaceStack = new ItemStack(Block.getBlockById(dataStream.readInt()), 1, dataStream.readInt());
			}
			else {
				replaceStack = null;
			}

			clientToMine = dataStream.readInt();
			controlType = RedstoneControl.values()[dataStream.readInt()];
			inverse = dataStream.readBoolean();

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

			if(dataStream.readBoolean())
			{
				replaceStack = new ItemStack(Block.getBlockById(dataStream.readInt()), 1, dataStream.readInt());
			}
			else {
				replaceStack = null;
			}

			clientToMine = dataStream.readInt();
			controlType = RedstoneControl.values()[dataStream.readInt()];
			inverse = dataStream.readBoolean();
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
		data.add(numPowering);
		data.add(searcher.state.ordinal());

		if(replaceStack != null)
		{
			data.add(true);
			data.add(MekanismUtils.getID(replaceStack));
			data.add(replaceStack.getItemDamage());
		}
		else {
			data.add(false);
		}

		if(searcher.state == State.SEARCHING)
		{
			data.add(searcher.found);
		}
		else {
			data.add(oresToMine.cardinality());
		}

		data.add(controlType.ordinal());
		data.add(inverse);

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
			data.add(oresToMine.cardinality());
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

		if(replaceStack != null)
		{
			data.add(true);
			data.add(MekanismUtils.getID(replaceStack));
			data.add(replaceStack.getItemDamage());
		}
		else {
			data.add(false);
		}

		if(searcher.state == State.SEARCHING)
		{
			data.add(searcher.found);
		}
		else {
			data.add(oresToMine.cardinality());
		}

		data.add(controlType.ordinal());
		data.add(inverse);

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
	public double getMaxEnergy()
	{
		return MekanismUtils.getMaxEnergy(this, MAX_ELECTRICITY);
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
			Mekanism.packetHandler.sendToAll(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())));

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
					worldObj.notifyBlocksOfNeighborChange(x, y, z, Mekanism.BoundingBlock);
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
	@Method(modid = "ComputerCraft")
	public String getType()
	{
		return getInventoryName();
	}

	public String[] names = {"setRadius", "setMin", "setMax", "setReplace", "addFilter", "removeFilter", "addOreFilter", "removeOreFilter", "reset", "start", "stop"};

	@Override
	@Method(modid = "ComputerCraft")
	public String[] getMethodNames()
	{
		return names;
	}

	@Override
	@Method(modid = "ComputerCraft")
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

					replaceStack = new ItemStack(Item.getItemById(num), 1, meta);
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

					filters.add(new MItemStackFilter(new ItemStack(Item.getItemById(num), 1, meta)));
				}
				else if(method == 5)
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

		if(replaceStack != null)
		{
			nbtTags.setTag("replaceStack", replaceStack.writeToNBT(new NBTTagCompound()));
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

		if(nbtTags.hasKey("replaceStack"))
		{
			replaceStack = ItemStack.loadItemStackFromNBT(nbtTags.getCompoundTag("replaceStack"));
		}

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
}
