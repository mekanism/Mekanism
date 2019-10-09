package mekanism.common.inventory.container.slot;

import mekanism.common.util.ChargeUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public abstract class SlotEnergy extends Slot {

    protected SlotEnergy(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    public static class SlotCharge extends SlotEnergy {

        public SlotCharge(IInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack itemstack) {
            return ChargeUtils.canBeCharged(itemstack);
        }
    }

    public static class SlotDischarge extends SlotEnergy {

        public SlotDischarge(IInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack itemstack) {
            return ChargeUtils.canBeDischarged(itemstack);
        }
    }
}