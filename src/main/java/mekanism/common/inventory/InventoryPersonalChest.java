package mekanism.common.inventory;

import mekanism.common.base.ISustainedInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;

public class InventoryPersonalChest extends InventoryBasic {

    public EntityPlayer entityPlayer;
    public ItemStack itemStack = ItemStack.EMPTY;

    public boolean reading;

    public EnumHand currentHand = EnumHand.MAIN_HAND;

    public InventoryPersonalChest(EntityPlayer player, EnumHand hand) {
        super("PersonalChest", false, 55);
        entityPlayer = player;
        currentHand = hand;

        read();
    }

    public InventoryPersonalChest(ItemStack stack) {
        super("PersonalChest", false, 55);
        itemStack = stack;

        read();
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (!reading) {
            write();
        }
    }

    @Override
    public void openInventory(EntityPlayer player) {
        read();
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        write();
    }

    public void write() {
        NBTTagList tagList = new NBTTagList();

        for (int slotCount = 0; slotCount < getSizeInventory(); slotCount++) {
            if (!getStackInSlot(slotCount).isEmpty()) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte) slotCount);
                getStackInSlot(slotCount).writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }

        if (!getStack().isEmpty()) {
            ((ISustainedInventory) getStack().getItem()).setInventory(tagList, getStack());
        }
    }

    public void read() {
        if (reading) {
            return;
        }

        reading = true;

        NBTTagList tagList = ((ISustainedInventory) getStack().getItem()).getInventory(getStack());

        if (tagList != null) {
            for (int tagCount = 0; tagCount < tagList.tagCount(); tagCount++) {
                NBTTagCompound tagCompound = tagList.getCompoundTagAt(tagCount);
                byte slotID = tagCompound.getByte("Slot");

                if (slotID >= 0 && slotID < getSizeInventory()) {
                    setInventorySlotContents(slotID, new ItemStack(tagCompound));
                }
            }
        }

        reading = false;
    }

    public ItemStack getStack() {
        return !itemStack.isEmpty() ? itemStack : entityPlayer.getHeldItem(currentHand);
    }
}
