package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.transaction.RateLimitTracker;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class InputInventorySlot extends BasicInventorySlot {

    public static InputInventorySlot at(@Nullable IContentsListener listener, int x, int y) {
        return at(ConstantPredicates.alwaysTrue(), listener, x, y);
    }

    public static InputInventorySlot at(@Range(from = 1, to = Long.MAX_VALUE) long capacity, Predicate<ItemResource> isItemValid, @Nullable IContentsListener listener, int x, int y) {
        return at(capacity, ConstantPredicates.alwaysTrueBi(), isItemValid, listener, x, y);
    }

    public static InputInventorySlot at(Predicate<ItemResource> isItemValid, @Nullable IContentsListener listener, int x, int y) {
        return at(ConstantPredicates.alwaysTrueBi(), isItemValid, listener, x, y);
    }

    public static InputInventorySlot at(BiPredicate<ItemResource, AutomationType> insertPredicate, Predicate<ItemResource> isItemValid, @Nullable IContentsListener listener, int x, int y) {
        return at(Item.ABSOLUTE_MAX_STACK_SIZE, insertPredicate, isItemValid, listener, x, y);
    }

    public static InputInventorySlot at(@Range(from = 1, to = Long.MAX_VALUE) long capacity, BiPredicate<ItemResource, AutomationType> insertPredicate,
          Predicate<ItemResource> isItemValid, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(insertPredicate, "Insertion check cannot be null");
        Objects.requireNonNull(isItemValid, "Item validity check cannot be null");
        if (capacity < 1) {
            throw new IllegalArgumentException("Slots with a custom capacity must allow at least one item");
        }
        return new InputInventorySlot(capacity, insertPredicate, isItemValid, null, null, listener, x, y);
    }

    protected InputInventorySlot(@Range(from = 1, to = Long.MAX_VALUE) long capacity, BiPredicate<ItemResource, AutomationType> insertPredicate,
          Predicate<ItemResource> isItemValid, @Nullable RateLimitTracker insertionRateLimiter, @Nullable RateLimitTracker extractionRateLimiter,
          @Nullable IContentsListener listener, int x, int y) {
        super(capacity, ConstantPredicates.notExternal(), insertPredicate, isItemValid, insertionRateLimiter, extractionRateLimiter, listener, x, y);
        setSlotType(ContainerSlotType.INPUT);
    }
}