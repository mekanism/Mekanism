package mekanism.common.inventory;

import java.util.Collections;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.item.block.ItemBlockBin;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BinMekanismInventory extends ItemStackMekanismInventory {

    private BinInventorySlot binSlot;

    private BinMekanismInventory(@NotNull ItemStack stack) {
        super(stack);
    }

    @NotNull
    @Override
    protected List<IInventorySlot> getInitialInventory() {
        binSlot = BinInventorySlot.create(this, ((ItemBlockBin) stack.getItem()).getTier());
        return Collections.singletonList(binSlot);
    }

    @Nullable
    public static BinMekanismInventory create(@NotNull ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemBlockBin) {
            return new BinMekanismInventory(stack);
        }
        return null;
    }

    public BinInventorySlot getBinSlot() {
        return binSlot;
    }
}