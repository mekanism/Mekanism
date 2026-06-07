package mekanism.common.inventory.slot;

import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.transaction.RateLimitTracker;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class OutputInventorySlot extends BasicInventorySlot {

    public static OutputInventorySlot at(@Nullable IContentsListener listener, int x, int y) {
        return new OutputInventorySlot(null, null, listener, x, y);
    }

    private OutputInventorySlot(@Nullable RateLimitTracker insertionRateLimiter, @Nullable RateLimitTracker extractionRateLimiter, @Nullable IContentsListener listener, int x, int y) {
        super(ConstantPredicates.alwaysTrueBi(), ConstantPredicates.internalOnly(), ConstantPredicates.alwaysTrue(), insertionRateLimiter, extractionRateLimiter, listener, x, y);
        setSlotType(ContainerSlotType.OUTPUT);
    }
}