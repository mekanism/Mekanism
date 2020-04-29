package mekanism.common.inventory.slot;

import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.content.qio.IQIODriveHolder;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.QIODriveData.QIODriveKey;
import net.minecraft.item.ItemStack;

public class QIODriveSlot extends BasicInventorySlot {

    private IQIODriveHolder driveHolder;
    private int slot;

    public <TILE extends IMekanismInventory & IQIODriveHolder> QIODriveSlot(TILE inventory, int slot, int x, int y) {
        super((stack, automationType) -> automationType != AutomationType.EXTERNAL,
              (stack, automationType) -> automationType != AutomationType.EXTERNAL,
              (stack) -> stack.getItem() instanceof IQIODriveItem,
              inventory, x, y);
        this.driveHolder = inventory;
        this.slot = slot;
    }

    @Override
    public void setStack(ItemStack stack) {
        super.setStack(stack);
        if (getStack().isEmpty()) {
            removeDrive();
        } else {
            addDrive(stack);
        }
    }

    @Override
    public ItemStack insertItem(ItemStack stack, Action action, AutomationType automationType) {
        ItemStack ret = super.insertItem(stack, action, automationType);
        if (action.execute() && ret.isEmpty()) {
            addDrive(stack);
        }
        return ret;
    }

    @Override
    public ItemStack extractItem(int amount, Action action, AutomationType automationType) {
        ItemStack ret = super.extractItem(amount, action, automationType);
        if (action.execute() && !ret.isEmpty()) {
            removeDrive();
        }
        return ret;
    }

    private void addDrive(ItemStack stack) {
        if (driveHolder.getQIOFrequency() != null)
            driveHolder.getQIOFrequency().addDrive(new QIODriveKey(driveHolder, slot));
    }

    private void removeDrive() {
        if (driveHolder.getQIOFrequency() != null)
            driveHolder.getQIOFrequency().removeDrive(new QIODriveKey(driveHolder, slot), true);
    }
}
