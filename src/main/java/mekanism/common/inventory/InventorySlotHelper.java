package mekanism.common.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.inventory.slot.IInventorySlot;
import net.minecraft.util.Direction;

public class InventorySlotHelper implements IInventorySlotHolder {

    private final Map<RelativeSide, List<IInventorySlot>> directionalSlots = new EnumMap<>(RelativeSide.class);
    private final List<IInventorySlot> inventorySlots = new ArrayList<>();
    private final Supplier<Direction> facingSupplier;
    //TODO: Allow declaring that some sides will be the same, so can just be the same list in memory??
    //TODO: Also allow for relative sides??

    private InventorySlotHelper(Supplier<Direction> facingSupplier) {
        this.facingSupplier = facingSupplier;
    }

    private void addSlot(@Nonnull IInventorySlot slot, RelativeSide... sides) {
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
            //TODO: Go through the code and make sure nothing is getting missed due to this returning an empty list
            return Collections.emptyList();
        }
        return slots;
    }

    public static class Builder {

        private final InventorySlotHelper helper;
        private boolean built;

        private Builder(Supplier<Direction> facingSupplier) {
            helper = new InventorySlotHelper(facingSupplier);
        }

        public static Builder forSide(Supplier<Direction> facingSupplier) {
            return new Builder(facingSupplier);
        }

        public void addSlot(@Nonnull IInventorySlot slot, RelativeSide... sides) {
            if (built) {
                throw new RuntimeException("Builder has already built.");
            }
            helper.addSlot(slot, sides);
        }

        public InventorySlotHelper build() {
            built = true;
            return helper;
        }
    }

}