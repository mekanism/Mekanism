package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import mekanism.api.annotations.NonNull;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import net.minecraft.item.ItemStack;

//TODO: Switch things over to using this rather than BasicInventorySlot
public class InputInventorySlot extends BasicInventorySlot {

    public static InputInventorySlot at(int x, int y) {
        return at(alwaysTrue, x, y);
    }

    public static InputInventorySlot at(Predicate<@NonNull ItemStack> isItemValid, int x, int y) {
        return at(alwaysTrue, isItemValid, x, y);
    }

    public static InputInventorySlot at(Predicate<@NonNull ItemStack> insertPredicate, Predicate<@NonNull ItemStack> isItemValid, int x, int y) {
        return new InputInventorySlot(insertPredicate, isItemValid, x, y);
    }

    private InputInventorySlot(Predicate<@NonNull ItemStack> insertPredicate, Predicate<@NonNull ItemStack> isItemValid, int x, int y) {
        //TODO: Instead of always being false, should we make it be isItemValid.negate(), just to allow for extracting if something went wrong
        super(alwaysFalse, insertPredicate, isItemValid, x, y);
    }

    @Override
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.INPUT;
    }
}