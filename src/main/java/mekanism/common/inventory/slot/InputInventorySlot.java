package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import net.minecraft.item.ItemStack;

public class InputInventorySlot extends BasicInventorySlot {

    public static InputInventorySlot at(IMekanismInventory inventory, int x, int y) {
        return at(alwaysTrue, inventory, x, y);
    }

    public static InputInventorySlot at(Predicate<@NonNull ItemStack> isItemValid, IMekanismInventory inventory, int x, int y) {
        return at(alwaysTrue, isItemValid, inventory, x, y);
    }

    public static InputInventorySlot at(Predicate<@NonNull ItemStack> insertPredicate, Predicate<@NonNull ItemStack> isItemValid, IMekanismInventory inventory, int x, int y) {
        return new InputInventorySlot(insertPredicate, isItemValid, inventory, x, y);
    }

    protected InputInventorySlot(Predicate<@NonNull ItemStack> insertPredicate, Predicate<@NonNull ItemStack> isItemValid, IMekanismInventory inventory, int x, int y) {
        //TODO: Instead of always being false, should we make it be isItemValid.negate(), just to allow for extracting if something went wrong
        super(alwaysFalse, insertPredicate, isItemValid, inventory, x, y);
    }

    @Override
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.INPUT;
    }
}