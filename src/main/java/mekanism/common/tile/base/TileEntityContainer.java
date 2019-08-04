package mekanism.common.tile.base;

import javax.annotation.Nonnull;
import mekanism.common.base.IBlockProvider;
import mekanism.common.base.ItemHandlerWrapper;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.capabilities.IToggleableCapability;
import mekanism.common.tile.interfaces.ITileContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

//TODO: Ideally we want this to not have to be Directional and can just extend TileEntityMekanism
public abstract class TileEntityContainer extends TileEntityMekanism implements ITileContainer, IToggleableCapability {

    /**
     * The inventory slot itemstacks used by this block.
     */
    public NonNullList<ItemStack> inventory;

    private CapabilityWrapperManager<ISidedInventory, ItemHandlerWrapper> itemManager = new CapabilityWrapperManager<>(ISidedInventory.class, ItemHandlerWrapper.class);
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

    public TileEntityContainer(IBlockProvider blockProvider) {
        super(blockProvider);
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

    @Nonnull
    @Override
    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    @Override
    public void setInventorySlotContents(int slotID, @Nonnull ItemStack itemstack) {
        if (hasInventory()) {
            getInventory().set(slotID, itemstack);
            if (!itemstack.isEmpty() && itemstack.getCount() > getInventoryStackLimit()) {
                itemstack.setCount(getInventoryStackLimit());
            }
            markDirty();
        }
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer entityplayer) {
        return hasInventory() && !isInvalid() && this.world.isBlockLoaded(this.pos);//prevent Containers from remaining valid after the chunk has unloaded;
    }

    @Nonnull
    @Override
    public abstract int[] getSlotsForFace(@Nonnull EnumFacing side);

    @Override
    public void setInventory(NBTTagList nbtTags, Object... data) {
        if (nbtTags == null || nbtTags.tagCount() == 0 || !handleInventory()) {
            return;
        }
        NonNullList<ItemStack>  inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        for (int slots = 0; slots < nbtTags.tagCount(); slots++) {
            NBTTagCompound tagCompound = nbtTags.getCompoundTagAt(slots);
            byte slotID = tagCompound.getByte("Slot");
            if (slotID >= 0 && slotID < inventory.size()) {
                inventory.set(slotID, new ItemStack(tagCompound));
            }
        }
        this.inventory = inventory;
    }

    @Override
    public NBTTagList getInventory(Object... data) {
        NBTTagList tagList = new NBTTagList();
        if (handleInventory()) {
            NonNullList<ItemStack> inventory = getInventory();
            for (int slots = 0; slots < inventory.size(); slots++) {
                ItemStack itemStack = inventory.get(slots);
                if (!itemStack.isEmpty()) {
                    NBTTagCompound tagCompound = new NBTTagCompound();
                    tagCompound.setByte("Slot", (byte) slots);
                    itemStack.writeToNBT(tagCompound);
                    tagList.appendTag(tagCompound);
                }
            }
        }
        return tagList;
    }

    public boolean handleInventory() {
        return hasInventory();
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
        return super.getCapability(capability, side);
    }

    protected IItemHandler getItemHandler(EnumFacing side) {
        return side == null ? nullHandler : itemManager.getWrapper(this, side);
    }
}