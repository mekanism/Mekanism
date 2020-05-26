package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NonNull;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.item.ItemCraftingFormula;
import net.minecraft.item.ItemStack;

public class FormulaInventorySlot extends BasicInventorySlot {

    private static final Predicate<@NonNull ItemStack> validator = stack -> stack.getItem() instanceof ItemCraftingFormula;

    public static FormulaInventorySlot at(@Nullable IContentsListener listener, int x, int y) {
        return new FormulaInventorySlot(listener, x, y);
    }

    private FormulaInventorySlot(@Nullable IContentsListener listener, int x, int y) {
        super(manualOnly, alwaysTrueBi, validator, listener, x, y);
        setSlotOverlay(SlotOverlay.FORMULA);
    }
}