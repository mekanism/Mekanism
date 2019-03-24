package mekanism.common.base;

import javax.annotation.Nonnull;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class ItemHandlerWrapper extends SidedInvWrapper {

    public ItemHandlerWrapper(ISidedInventory inv, EnumFacing side) {
        super(inv, side);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        int sl = getSlot(inv, slot, side);
        return sl != -1 && inv.isItemValidForSlot(sl, stack);
    }
}