package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import mekanism.api.annotations.NonNull;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;

public class FuelInventorySlot extends BasicInventorySlot {

    private static final Predicate<@NonNull ItemStack> validator = item -> ForgeHooks.getBurnTime(item) > 0;
    private static final Predicate<@NonNull ItemStack> extractPredicate = item -> ForgeHooks.getBurnTime(item) == 0;

    public static FuelInventorySlot at(int x, int y) {
        return new FuelInventorySlot(x, y);
    }

    private FuelInventorySlot(int x, int y) {
        //Only allow extraction if after a reload the item no longer has a burn time
        super(extractPredicate, alwaysTrue, validator, x, y);
    }

    @Override
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.OUTPUT;
    }
}