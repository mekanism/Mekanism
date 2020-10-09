package mekanism.common.capabilities.holder.slot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.util.Direction;

public class InventorySlotHolder implements IInventorySlotHolder {

    private final Map<RelativeSide, List<IInventorySlot>> directionalSlots = new EnumMap<>(RelativeSide.class);
    private final List<IInventorySlot> inventorySlots = new ArrayList<>();
    private final Supplier<Direction> facingSupplier;
    @Nullable
    private final Predicate<RelativeSide> insertPredicate;
    @Nullable
    private final Predicate<RelativeSide> extractPredicate;
    //TODO: Allow declaring that some sides will be the same, so can just be the same list in memory??

    InventorySlotHolder(Supplier<Direction> facingSupplier, @Nullable Predicate<RelativeSide> insertPredicate, @Nullable Predicate<RelativeSide> extractPredicate) {
        this.facingSupplier = facingSupplier;
        this.insertPredicate = insertPredicate;
        this.extractPredicate = extractPredicate;
    }

    void addSlot(@Nonnull IInventorySlot slot, RelativeSide... sides) {
        inventorySlots.add(slot);
        for (RelativeSide side : sides) {
            directionalSlots.computeIfAbsent(side, k -> new ArrayList<>()).add(slot);
        }
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction direction) {
        if (direction == null || directionalSlots.isEmpty()) {
            //If we want the internal OR we have no side specification, give all of our slots
            return inventorySlots;
        }
        RelativeSide side = RelativeSide.fromDirections(facingSupplier.get(), direction);
        List<IInventorySlot> slots = directionalSlots.get(side);
        if (slots == null) {
            return Collections.emptyList();
        }
        return slots;
    }

    @Override
    public boolean canInsert(@Nullable Direction direction) {
        //If the insert predicate is null then we can insert from any side, don't bother looking up our facing
        return direction != null && (insertPredicate == null || insertPredicate.test(RelativeSide.fromDirections(facingSupplier.get(), direction)));
    }

    @Override
    public boolean canExtract(@Nullable Direction direction) {
        //If the extract predicate is null then we can extract from any side, don't bother looking up our facing
        return direction != null && (extractPredicate == null || extractPredicate.test(RelativeSide.fromDirections(facingSupplier.get(), direction)));
    }
}