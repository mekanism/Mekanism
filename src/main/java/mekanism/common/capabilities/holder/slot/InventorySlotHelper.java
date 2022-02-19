package mekanism.common.capabilities.holder.slot;

import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.util.Direction;

public class InventorySlotHelper {

    private final IInventorySlotHolder slotHolder;
    private boolean built;

    private InventorySlotHelper(IInventorySlotHolder slotHolder) {
        this.slotHolder = slotHolder;
    }

    public static InventorySlotHelper readOnly() {
        return new InventorySlotHelper(new ReadOnlyInventorySlotHolder());
    }

    public static InventorySlotHelper forSide(Supplier<Direction> facingSupplier) {
        return forSide(facingSupplier, null, null);
    }

    public static InventorySlotHelper forSide(Supplier<Direction> facingSupplier, @Nullable Predicate<RelativeSide> insertPredicate,
          @Nullable Predicate<RelativeSide> extractPredicate) {
        return new InventorySlotHelper(new InventorySlotHolder(facingSupplier, insertPredicate, extractPredicate));
    }

    public static InventorySlotHelper forSideWithConfig(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        return new InventorySlotHelper(new ConfigInventorySlotHolder(facingSupplier, configSupplier));
    }

    public void addSlot(@Nonnull IInventorySlot slot) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof InventorySlotHolder) {
            ((InventorySlotHolder) slotHolder).addSlot(slot);
        } else if (slotHolder instanceof ReadOnlyInventorySlotHolder) {
            ((ReadOnlyInventorySlotHolder) slotHolder).addSlot(slot);
        } else if (slotHolder instanceof ConfigInventorySlotHolder) {
            ((ConfigInventorySlotHolder) slotHolder).addSlot(slot);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add slots");
        }
    }

    public void addSlot(@Nonnull IInventorySlot slot, RelativeSide... sides) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof InventorySlotHolder) {
            ((InventorySlotHolder) slotHolder).addSlot(slot, sides);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add slots on specific sides");
        }
    }

    public IInventorySlotHolder build() {
        built = true;
        return slotHolder;
    }
}