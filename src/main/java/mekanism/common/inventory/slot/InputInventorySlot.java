package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class InputInventorySlot extends BasicInventorySlot {

    public static InputInventorySlot at(@Nullable IContentsListener listener, int x, int y) {
        return at(ConstantPredicates.alwaysTrue(), listener, x, y);
    }

    public static InputInventorySlot at(Predicate<ItemResource> isItemValid, @Nullable IContentsListener listener, int x, int y) {
        return at(ConstantPredicates.alwaysTrue(), isItemValid, listener, x, y);
    }

    public static InputInventorySlot at(Predicate<ItemResource> insertPredicate, Predicate<ItemResource> isItemValid, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(insertPredicate, "Insertion check cannot be null");
        Objects.requireNonNull(isItemValid, "Item validity check cannot be null");
        return new InputInventorySlot(insertPredicate, isItemValid, listener, x, y);
    }

    protected InputInventorySlot(Predicate<ItemResource> insertPredicate, Predicate<ItemResource> isItemValid, @Nullable IContentsListener listener, int x, int y) {
        super(ConstantPredicates.notExternal(), (itemType, _) -> insertPredicate.test(itemType), isItemValid, listener, x, y);
        setSlotType(ContainerSlotType.INPUT);
    }
}