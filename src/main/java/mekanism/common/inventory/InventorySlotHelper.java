package mekanism.common.inventory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.util.MekanismUtils;
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
        if (direction == null) {
            return inventorySlots;
        }
        //TODO: Get the relative side
        RelativeSide side = RelativeSide.fromDirection(facingSupplier.get(), direction);
        List<IInventorySlot> slots = directionalSlots.get(side);
        if (slots == null) {
            //TODO: Should this default to empty instead of returning all?
            // It would probably make more sense for this to be the case, though we need to double check nothing breaks that way
            // NOTE: The cases we would want it to still return ALL of them, is when/if an inventory does not have ANY direction markings
            // as we then assume they are available on all sides?
            return inventorySlots;
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

    public enum RelativeSide {
        DOWN,
        UP,
        FRONT,
        BACK,
        RIGHT,
        LEFT;

        public static RelativeSide fromDirection(@Nonnull Direction facing, @Nonnull Direction direction) {
            if (direction == Direction.DOWN) {
                return DOWN;
            } else if (direction == Direction.UP) {
                return UP;
            } else if (direction == facing) {
                return FRONT;
            } else if (direction == facing.getOpposite()) {
                return BACK;
            } else if (direction == MekanismUtils.getRight(facing)) {
                return RIGHT;
            } else if (direction == MekanismUtils.getLeft(facing)) {
                return LEFT;
            }
            //Fall back to front, should never get here
            return FRONT;
        }
    }
}