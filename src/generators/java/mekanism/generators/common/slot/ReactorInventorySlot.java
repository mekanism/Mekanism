package mekanism.generators.common.slot;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ReactorInventorySlot extends BasicInventorySlot {

    //TODO: Make the slot not render in the gui if the reactor is not fully formed
    public static ReactorInventorySlot at(Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(validator, "Item validity check cannot be null");
        return new ReactorInventorySlot(validator, inventory, x, y);
    }

    protected ReactorInventorySlot(Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        super(notExternal, alwaysTrueBi, validator, inventory, x, y);
    }
}