package mekanism.common.capabilities.holder.slot;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.holder.ProxiedHolder;
import net.minecraft.util.Direction;

public class ProxiedInventorySlotHolder extends ProxiedHolder implements IInventorySlotHolder {

    private final Function<Direction, List<IInventorySlot>> slotFunction;

    public static ProxiedInventorySlotHolder create(Predicate<Direction> insertPredicate, Predicate<Direction> extractPredicate,
          Function<Direction, List<IInventorySlot>> slotFunction) {
        return new ProxiedInventorySlotHolder(insertPredicate, extractPredicate, slotFunction);
    }

    private ProxiedInventorySlotHolder(Predicate<Direction> insertPredicate, Predicate<Direction> extractPredicate,
          Function<Direction, List<IInventorySlot>> slotFunction) {
        super(insertPredicate, extractPredicate);
        this.slotFunction = slotFunction;
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return slotFunction.apply(side);
    }
}