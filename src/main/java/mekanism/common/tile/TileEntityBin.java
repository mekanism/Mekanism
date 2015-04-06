package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.Range4D;
import mekanism.api.util.StackUtils;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.IActiveState;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.base.ITransporterTile;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.Optional.Interface;

import io.netty.buffer.ByteBuf;

import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;

@Interface(iface = "powercrystals.minefactoryreloaded.api.IDeepStorageUnit", modid = "MineFactoryReloaded")
public class TileEntityBin extends TileEntityBasicBlock implements ISidedInventory, IActiveState, IDeepStorageUnit, IConfigurable
{
	public boolean isActive;

	public boolean clientActive;

	public final int MAX_DELAY = 10;

	public int addTicks = 0;

	public int delayTicks;

	public int cacheCount;

	public final int MAX_STORAGE = 4096;

	public ItemStack itemType;

	public ItemStack topStack;
	public ItemStack bottomStack;

	public int prevCount;

	public int clientAmount;

	public void sortStacks()
	{
		if(getItemCount() == 0 || itemType == null)
		{
			itemType = null;
			topStack = null;
			bottomStack = null;
			cacheCount = 0;

			return;
		}

		int count = getItemCount();
		int remain = MAX_STORAGE-count;

		if(remain >= itemType.getMaxStackSize())
		{
			topStack = null;
		}
		else {
			topStack = itemType.copy();
			topStack.stackSize = itemType.getMaxStackSize()-remain;
		}

		count -= StackUtils.getSize(topStack);

		bottomStack = itemType.copy();
		bottomStack.stackSize = Math.min(itemType.getMaxStackSize(), count);

		count -= StackUtils.getSize(bottomStack);

		cacheCount = count;
	}

	public boolean isValid(ItemStack stack)
	{
		if(stack == null || stack.stackSize <= 0)
		{
			return false;
		}

		if(stack.getItem() instanceof ItemBlockBasic && stack.getItemDamage() == 6)
		{
			return false;
		}

		if(itemType == null)
		{
			return true;
		}

		if(!stack.isItemEqual(itemType) || !ItemStack.areItemStackTagsEqual(stack, itemType))
		{
			return false;
		}

		return true;
	}

	public ItemStack add(ItemStack stack)
	{
		if(isValid(stack) && getItemCount() != MAX_STORAGE)
		{
			if(itemType == null)
			{
				setItemType(stack);
			}

			if(getItemCount() + stack.stackSize <= MAX_STORAGE)
			{
				setItemCount(getItemCount() + stack.stackSize);
				return null;
			}
			else {
				ItemStack rejects = itemType.copy();
				rejects.stackSize = (getItemCount()+stack.stackSize) - MAX_STORAGE;

				setItemCount(MAX_STORAGE);

				return rejects;
			}
		}

		return stack;
	}

	public ItemStack removeStack()
	{
		if(getItemCount() == 0)
		{
			return null;
		}

		return remove(bottomStack.stackSize);
	}

	public ItemStack remove(int amount)
	{
		if(getItemCount() == 0)
		{
			return null;
		}

		ItemStack ret = itemType.copy();
		ret.stackSize = Math.min(Math.min(amount, itemType.getMaxStackSize()), getItemCount());

		setItemCount(getItemCount() - ret.stackSize);

		return ret;
	}

	public int getItemCount()
	{
		return StackUtils.getSize(bottomStack) + cacheCount + StackUtils.getSize(topStack);
	}

	@Override
	public void onUpdate()
	{
		if(!worldObj.isRemote)
		{
			addTicks = Math.max(0, addTicks-1);
			delayTicks = Math.max(0, delayTicks-1);

			sortStacks();

			if(getItemCount() != prevCount)
			{
				markDirty();
				MekanismUtils.saveChunk(this);
			}

			if(delayTicks == 0)
			{
				if(bottomStack != null && isActive)
				{
					TileEntity tile = Coord4D.get(this).getFromSide(ForgeDirection.getOrientation(0)).getTileEntity(worldObj);

					if(tile instanceof ITransporterTile)
					{
						ILogisticalTransporter transporter = ((ITransporterTile)tile).getTransmitter();

						ItemStack rejects = TransporterUtils.insert(this, transporter, bottomStack, null, true, 0);

						if(TransporterManager.didEmit(bottomStack, rejects))
						{
							setInventorySlotContents(0, rejects);
						}
					}
					else if(tile instanceof IInventory)
					{
						setInventorySlotContents(0, InventoryUtils.putStackInInventory((IInventory)tile, bottomStack, 0, false));
					}

					delayTicks = 10;
				}
			}
			else {
				delayTicks--;
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("isActive", isActive);
		nbtTags.setInteger("itemCount", cacheCount);

		if(bottomStack != null)
		{
			nbtTags.setTag("bottomStack", bottomStack.writeToNBT(new NBTTagCompound()));
		}

		if(topStack != null)
		{
			nbtTags.setTag("topStack", topStack.writeToNBT(new NBTTagCompound()));
		}

		if(getItemCount() > 0)
		{
			nbtTags.setTag("itemType", itemType.writeToNBT(new NBTTagCompound()));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		isActive = nbtTags.getBoolean("isActive");
		cacheCount = nbtTags.getInteger("itemCount");

		bottomStack = ItemStack.loadItemStackFromNBT(nbtTags.getCompoundTag("bottomStack"));
		topStack = ItemStack.loadItemStackFromNBT(nbtTags.getCompoundTag("topStack"));

		if(getItemCount() > 0)
		{
			itemType = ItemStack.loadItemStackFromNBT(nbtTags.getCompoundTag("itemType"));
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(isActive);
		data.add(getItemCount());

		if(getItemCount() > 0)
		{
			data.add(itemType);
		}

		return data;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);

		isActive = dataStream.readBoolean();
		clientAmount = dataStream.readInt();

		if(clientAmount > 0)
		{
			itemType = PacketHandler.readStack(dataStream);
		}
		else {
			itemType = null;
		}

		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public ItemStack getStackInSlot(int slotID)
	{
		if(slotID == 1)
		{
			return topStack;
		}
		else {
			return bottomStack;
		}
	}

	@Override
	public ItemStack decrStackSize(int slotID, int amount)
	{
		if(slotID == 1)
		{
			return null;
		}
		else if(slotID == 0)
		{
			int toRemove = Math.min(getItemCount(), amount);

			if(toRemove > 0)
			{
				ItemStack ret = itemType.copy();
				ret.stackSize = toRemove;

				setItemCount(getItemCount()-toRemove);

				return ret;
			}
		}

		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotID)
	{
		return getStackInSlot(slotID);
	}

	@Override
	public int getSizeInventory()
	{
		return 2;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		if(i == 0)
		{
			if(getItemCount() == 0)
			{
				return;
			}

			if(itemstack == null)
			{
				setItemCount(getItemCount() - bottomStack.stackSize);
			}
			else {
				setItemCount(getItemCount() - (bottomStack.stackSize-itemstack.stackSize));
			}
		}
		else if(i == 1)
		{
			if(itemstack == null)
			{
				topStack = null;
			}
			else {
				if(isValid(itemstack) && itemstack.stackSize > StackUtils.getSize(topStack))
				{
					add(StackUtils.size(itemstack, itemstack.stackSize-StackUtils.getSize(topStack)));
				}
			}
		}
	}

	@Override
	public void markDirty()
	{
		super.markDirty();

		if(!worldObj.isRemote)
		{
			MekanismUtils.saveChunk(this);
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
			prevCount = getItemCount();
			sortStacks();
		}
	}

	public void setItemType(ItemStack stack)
	{
		if(stack == null)
		{
			itemType = null;
			cacheCount = 0;
			topStack = null;
			bottomStack = null;
			return;
		}

		ItemStack ret = stack.copy();
		ret.stackSize = 1;
		itemType = ret;
	}

	public void setItemCount(int count)
	{
		cacheCount = Math.max(0, count);
		topStack = null;
		bottomStack = null;

		if(count == 0)
		{
			setItemType(null);
		}

		markDirty();
	}

	@Override
	public String getInventoryName()
	{
		return MekanismUtils.localize("tile.BasicBlock.Bin.name");
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return true;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return i == 1 ? isValid(itemstack) : false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(side == 1)
		{
			return new int[] {1};
		}
		else if(side == 0)
		{
			return new int[] {0};
		}

		return InventoryUtils.EMPTY;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return isItemValidForSlot(i, itemstack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return i == 0 ? isValid(itemstack) : false;
	}

	@Override
	public boolean canSetFacing(int facing)
	{
		return facing != 0 && facing != 1;
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
		return true;
	}

	@Override
	public boolean lightUpdate()
	{
		return true;
	}

	@Override
	public ItemStack getStoredItemType()
	{
		if(itemType == null)
		{
			return null;
		}

		return MekanismUtils.size(itemType, getItemCount());
	}

	@Override
	public void setStoredItemCount(int amount)
	{
		if(amount == 0)
		{
			setStoredItemType(null, 0);
		}

		setItemCount(amount);
	}

	@Override
	public void setStoredItemType(ItemStack type, int amount)
	{
		itemType = type;
		cacheCount = amount;

		topStack = null;
		bottomStack = null;

		markDirty();
	}

	@Override
	public int getMaxStoredCount()
	{
		return MAX_STORAGE;
	}

	@Override
	public boolean onSneakRightClick(EntityPlayer player, int side)
	{
		setActive(!getActive());
		worldObj.playSoundEffect(xCoord, yCoord, zCoord, "random.click", 0.3F, 1);
		return true;
	}

	@Override
	public boolean onRightClick(EntityPlayer player, int side)
	{
		return false;
	}
}