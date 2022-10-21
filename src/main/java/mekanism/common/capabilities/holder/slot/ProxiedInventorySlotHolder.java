package mekanism.common.capabilities.holder.slot;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.holder.ProxiedHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @NotNull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return slotFunction.apply(side);
    }
}