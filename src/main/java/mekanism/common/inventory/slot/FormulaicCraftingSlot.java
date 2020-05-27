package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.AutomationType;
import mekanism.common.inventory.container.slot.ContainerSlotType;

public class FormulaicCraftingSlot extends BasicInventorySlot {

    public static FormulaicCraftingSlot at(BooleanSupplier autoModeSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(autoModeSupplier, "Auto mode supplier cannot be null");
        return new FormulaicCraftingSlot(autoModeSupplier, listener, x, y);
    }

    private FormulaicCraftingSlot(BooleanSupplier autoModeSupplier, @Nullable IContentsListener listener, int x, int y) {
        super(alwaysTrueBi, (stack, automationType) -> automationType == AutomationType.INTERNAL || !autoModeSupplier.getAsBoolean(), alwaysTrue, listener, x, y);
        setSlotType(ContainerSlotType.VALIDITY);
    }
}