package mekanism.common.inventory;

import mekanism.common.base.ISustainedInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Hand;

public class InventoryPersonalChest extends InventoryBasic {

    private final ItemStack itemStack;
    private boolean reading;
    public Hand currentHand;

    public InventoryPersonalChest(ItemStack stack, Hand hand) {
        super("PersonalChest", false, 55);
        itemStack = stack;
        currentHand = hand;
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
    public void openInventory(PlayerEntity player) {
        read();
    }

    @Override
    public void closeInventory(PlayerEntity player) {
        write();
    }

    public void write() {
        NBTTagList tagList = new NBTTagList();
        for (int slotCount = 0; slotCount < getSizeInventory(); slotCount++) {
            if (!getStackInSlot(slotCount).isEmpty()) {
                CompoundNBT tagCompound = new CompoundNBT();
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
                CompoundNBT tagCompound = tagList.getCompoundTagAt(tagCount);
                byte slotID = tagCompound.getByte("Slot");
                if (slotID >= 0 && slotID < getSizeInventory()) {
                    setInventorySlotContents(slotID, new ItemStack(tagCompound));
                }
            }
        }
        reading = false;
    }

    public ItemStack getStack() {
        return itemStack;
    }
}