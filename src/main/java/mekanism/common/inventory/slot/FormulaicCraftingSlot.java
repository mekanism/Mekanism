package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IMekanismInventory;

public class FormulaicCraftingSlot extends BasicInventorySlot {

    public static FormulaicCraftingSlot at(BooleanSupplier autoModeSupplier, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(autoModeSupplier, "Auto mode supplier cannot be null");
        return new FormulaicCraftingSlot(autoModeSupplier, inventory, x, y);
    }

    private FormulaicCraftingSlot(BooleanSupplier autoModeSupplier, @Nullable IMekanismInventory inventory, int x, int y) {
        super(alwaysTrueBi, (stack, automationType) -> automationType == AutomationType.INTERNAL || !autoModeSupplier.getAsBoolean(), alwaysTrue, inventory, x, y);
    }
}