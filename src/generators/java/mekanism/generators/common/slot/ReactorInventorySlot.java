package mekanism.generators.common.slot;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ReactorInventorySlot extends BasicInventorySlot {

    public static ReactorInventorySlot at(Predicate<@NonNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(validator, "Item validity check cannot be null");
        return new ReactorInventorySlot(validator, listener, x, y);
    }

    protected ReactorInventorySlot(Predicate<@NonNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        super(notExternal, alwaysTrueBi, validator, listener, x, y);
    }
}