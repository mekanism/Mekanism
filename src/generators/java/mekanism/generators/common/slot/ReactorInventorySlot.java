package mekanism.generators.common.slot;

import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ReactorInventorySlot extends BasicInventorySlot {

    public static ReactorInventorySlot at(Predicate<@NotNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(validator, "Item validity check cannot be null");
        return new ReactorInventorySlot(validator, listener, x, y);
    }

    protected ReactorInventorySlot(Predicate<@NotNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        super(ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), validator, listener, x, y);
    }
}