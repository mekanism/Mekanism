package mekanism.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

/*Copied & modified from net.minecraft.inventory.InventoryBasic */
public class InventoryList implements IInventory {

    private final int slotsCount;
    private final NonNullList<ItemStack> inventoryContents;
    private final TileEntity te;
    private String inventoryTitle;

    public InventoryList(NonNullList<ItemStack> list, TileEntity parent) {
        this.inventoryTitle = "Proxy";
        this.slotsCount = list.size();
        this.inventoryContents = list;
        this.te = parent;
    }

    /**
     * Returns the stack in the given slot.
     */
    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < this.inventoryContents.size() ? (ItemStack) this.inventoryContents.get(index)
              : ItemStack.EMPTY;
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventoryContents, index, count);

        if (!itemstack.isEmpty()) {
            this.markDirty();
        }

        return itemstack;
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    public ItemStack removeStackFromSlot(int index) {
        ItemStack itemstack = (ItemStack) this.inventoryContents.get(index);

        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.inventoryContents.set(index, ItemStack.EMPTY);
            return itemstack;
        }
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.inventoryContents.set(index, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        this.markDirty();
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSizeInventory() {
        return this.slotsCount;
    }

    public boolean isEmpty() {
        for (ItemStack itemstack : this.inventoryContents) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    public String getName() {
        return this.inventoryTitle;
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName() {
        return false;
    }

    /**
     * Sets the name of this inventory. This is displayed to the client on opening.
     */
    public void setCustomName(String inventoryTitleIn) {

    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    public ITextComponent getDisplayName() {
        return (ITextComponent) (this.hasCustomName() ? new TextComponentString(this.getName())
              : new TextComponentTranslation(this.getName(), new Object[0]));
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    public int getInventoryStackLimit() {
        return 64;
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    public void markDirty() {
        this.te.markDirty();
    }

    /**
     * Don't rename this method to canInteractWith due to conflicts with Container
     */
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    public void openInventory(EntityPlayer player) {
    }

    public void closeInventory(EntityPlayer player) {
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
     * guis use Slot.isItemValid
     */
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    public int getField(int id) {
        return 0;
    }

    public void setField(int id, int value) {
    }

    public int getFieldCount() {
        return 0;
    }

    public void clear() {
        this.inventoryContents.clear();
    }
}