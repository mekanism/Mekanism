package mekanism.generators.common.slot;

import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ReactorInventorySlot extends BasicInventorySlot {

    public static ReactorInventorySlot at(Predicate<ItemResource> validator, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(validator, "Item validity check cannot be null");
        return new ReactorInventorySlot(validator, listener, x, y);
    }

    protected ReactorInventorySlot(Predicate<ItemResource> validator, @Nullable IContentsListener listener, int x, int y) {
        super(ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), validator, listener, x, y);
    }
}