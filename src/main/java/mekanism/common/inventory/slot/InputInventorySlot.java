package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import net.minecraft.item.ItemStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InputInventorySlot extends BasicInventorySlot {

    public static InputInventorySlot at(@Nullable IMekanismInventory inventory, int x, int y) {
        return at(alwaysTrue, inventory, x, y);
    }

    public static InputInventorySlot at(Predicate<@NonNull ItemStack> isItemValid, @Nullable IMekanismInventory inventory, int x, int y) {
        return at(alwaysTrue, isItemValid, inventory, x, y);
    }

    public static InputInventorySlot at(Predicate<@NonNull ItemStack> insertPredicate, Predicate<@NonNull ItemStack> isItemValid, @Nullable IMekanismInventory inventory,
          int x, int y) {
        Objects.requireNonNull(insertPredicate, "Insertion check cannot be null");
        Objects.requireNonNull(isItemValid, "Item validity check cannot be null");
        return new InputInventorySlot(insertPredicate, isItemValid, inventory, x, y);
    }

    protected InputInventorySlot(Predicate<@NonNull ItemStack> insertPredicate, Predicate<@NonNull ItemStack> isItemValid, @Nullable IMekanismInventory inventory, int x,
          int y) {
        //TODO: Instead of always being false, should we make it be isItemValid.negate(), just to allow for extracting if something went wrong
        //TODO: Re-evaluate the insertion predicate
        super(notExternal, (stack, automationType) -> insertPredicate.test(stack), isItemValid, inventory, x, y);
        setSlotType(ContainerSlotType.INPUT);
    }
}