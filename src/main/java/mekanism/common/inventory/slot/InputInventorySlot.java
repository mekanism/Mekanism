package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NonNull;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import net.minecraft.item.ItemStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InputInventorySlot extends BasicInventorySlot {

    public static InputInventorySlot at(@Nullable IContentsListener listener, int x, int y) {
        return at(alwaysTrue, listener, x, y);
    }

    public static InputInventorySlot at(Predicate<@NonNull ItemStack> isItemValid, @Nullable IContentsListener listener, int x, int y) {
        return at(alwaysTrue, isItemValid, listener, x, y);
    }

    public static InputInventorySlot at(Predicate<@NonNull ItemStack> insertPredicate, Predicate<@NonNull ItemStack> isItemValid, @Nullable IContentsListener listener,
          int x, int y) {
        Objects.requireNonNull(insertPredicate, "Insertion check cannot be null");
        Objects.requireNonNull(isItemValid, "Item validity check cannot be null");
        return new InputInventorySlot(insertPredicate, isItemValid, listener, x, y);
    }

    protected InputInventorySlot(Predicate<@NonNull ItemStack> insertPredicate, Predicate<@NonNull ItemStack> isItemValid, @Nullable IContentsListener listener, int x, int y) {
        super(notExternal, (stack, automationType) -> insertPredicate.test(stack), isItemValid, listener, x, y);
        setSlotType(ContainerSlotType.INPUT);
    }
}