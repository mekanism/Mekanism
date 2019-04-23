package mekanism.common.tile.prefab;

import javax.annotation.Nonnull;
import mekanism.common.Upgrade;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ItemHandlerWrapper;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.capabilities.IToggleableCapability;
import mekanism.common.util.LangUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public abstract class TileEntityContainerBlock extends TileEntityBasicBlock implements ISidedInventory,
      ISustainedInventory, ITickable, IToggleableCapability {

    /**
     * The inventory slot itemstacks used by this block.
     */
    public NonNullList<ItemStack> inventory;

    /**
     * The full name of this machine.
     */
    public String fullName;
    private CapabilityWrapperManager<ISidedInventory, ItemHandlerWrapper> itemManager = new CapabilityWrapperManager<>(
          ISidedInventory.class, ItemHandlerWrapper.class);
    /**
     * Read only itemhandler for the null facing.
     */
    private IItemHandler nullHandler = new InvWrapper(this) {
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            //no
        }
    };

    /**
     * A simple tile entity with a container and facing state.
     *
     * @param name - full name of this tile entity
     */
    public TileEntityContainerBlock(String name) {
        fullName = name;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : getInventory()) {
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        if (handleInventory()) {
            NBTTagList tagList = nbtTags.getTagList("Items", NBT.TAG_COMPOUND);
            inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);

            for (int tagCount = 0; tagCount < tagList.tagCount(); tagCount++) {
                NBTTagCompound tagCompound = tagList.getCompoundTagAt(tagCount);
                byte slotID = tagCompound.getByte("Slot");

                if (slotID >= 0 && slotID < getSizeInventory()) {
                    setInventorySlotContents(slotID, new ItemStack(tagCompound));
                }
            }
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        if (handleInventory()) {
            NBTTagList tagList = new NBTTagList();

            for (int slotCount = 0; slotCount < getSizeInventory(); slotCount++) {
                ItemStack stackInSlot = getStackInSlot(slotCount);
                if (!stackInSlot.isEmpty()) {
                    NBTTagCompound tagCompound = new NBTTagCompound();
                    tagCompound.setByte("Slot", (byte) slotCount);
                    stackInSlot.writeToNBT(tagCompound);
                    tagList.appendTag(tagCompound);
                }
            }

            nbtTags.setTag("Items", tagList);
        }

        return nbtTags;
    }

    protected NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    @Override
    public int getSizeInventory() {
        return getInventory() != null ? getInventory().size() : 0;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slotID) {
        return getInventory() != null ? getInventory().get(slotID) : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int slotID, int amount) {
        if (getInventory() == null) {
            return ItemStack.EMPTY;
        }

        return ItemStackHelper.getAndSplit(getInventory(), slotID, amount);
    }

    @Nonnull
    @Override
    public ItemStack removeStackFromSlot(int slotID) {
        if (getInventory() == null) {
            return ItemStack.EMPTY;
        }

        return ItemStackHelper.getAndRemove(getInventory(), slotID);
    }

    @Override
    public void setInventorySlotContents(int slotID, @Nonnull ItemStack itemstack) {
        getInventory().set(slotID, itemstack);

        if (!itemstack.isEmpty() && itemstack.getCount() > getInventoryStackLimit()) {
            itemstack.setCount(getInventoryStackLimit());
        }

        markDirty();
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer entityplayer) {
        return !isInvalid() && this.world
              .isBlockLoaded(this.pos);//prevent Containers from remaining valid after the chunk has unloaded;
    }

    @Nonnull
    @Override
    public String getName() {
        return LangUtils.localize(getBlockType().getTranslationKey() + "." + fullName + ".name");
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {
    }

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        return true;
    }

    @Override
    public boolean canInsertItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        return isItemValidForSlot(slotID, itemstack);
    }

    @Nonnull
    @Override
    public abstract int[] getSlotsForFace(@Nonnull EnumFacing side);

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        return true;
    }

    @Override
    public void setInventory(NBTTagList nbtTags, Object... data) {
        if (nbtTags == null || nbtTags.tagCount() == 0 || !handleInventory()) {
            return;
        }

        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);

        for (int slots = 0; slots < nbtTags.tagCount(); slots++) {
            NBTTagCompound tagCompound = nbtTags.getCompoundTagAt(slots);
            byte slotID = tagCompound.getByte("Slot");

            if (slotID >= 0 && slotID < inventory.size()) {
                inventory.set(slotID, new ItemStack(tagCompound));
            }
        }
    }

    @Override
    public NBTTagList getInventory(Object... data) {
        NBTTagList tagList = new NBTTagList();

        if (handleInventory()) {
            for (int slots = 0; slots < inventory.size(); slots++) {
                if (!inventory.get(slots).isEmpty()) {
                    NBTTagCompound tagCompound = new NBTTagCompound();
                    tagCompound.setByte("Slot", (byte) slots);
                    inventory.get(slots).writeToNBT(tagCompound);
                    tagList.appendTag(tagCompound);
                }
            }
        }

        return tagList;
    }

    public boolean handleInventory() {
        return true;
    }

    public void recalculateUpgradables(Upgrade upgradeType) {
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        }
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(getItemHandler(side));
        }

        return super.getCapability(capability, facing);
    }

    protected IItemHandler getItemHandler(EnumFacing side) {
        return side == null ? nullHandler : itemManager.getWrapper(this, side);
    }
}
