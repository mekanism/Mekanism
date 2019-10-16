package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import mekanism.api.annotations.NonNull;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.item.ItemCraftingFormula;
import net.minecraft.item.ItemStack;

public class FormulaInventorySlot extends BasicInventorySlot {

    private static final Predicate<@NonNull ItemStack> validator = stack -> stack.getItem() instanceof ItemCraftingFormula;

    public static FormulaInventorySlot at(int x, int y) {
        return new FormulaInventorySlot(x, y);
    }

    private FormulaInventorySlot(int x, int y) {
        super(alwaysFalse, alwaysTrue, validator, x, y);
    }

    @Override
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.OUTPUT;
    }
}