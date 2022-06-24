package mekanism.common.capabilities.holder.slot;

import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public <SLOT extends IInventorySlot> SLOT addSlot(@NotNull SLOT slot) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof InventorySlotHolder slotHolder) {
            slotHolder.addSlot(slot);
        } else if (slotHolder instanceof ReadOnlyInventorySlotHolder slotHolder) {
            slotHolder.addSlot(slot);
        } else if (slotHolder instanceof ConfigInventorySlotHolder slotHolder) {
            slotHolder.addSlot(slot);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add slots");
        }
        return slot;
    }

    public <SLOT extends IInventorySlot> SLOT addSlot(@NotNull SLOT slot, RelativeSide... sides) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof InventorySlotHolder slotHolder) {
            slotHolder.addSlot(slot, sides);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add slots on specific sides");
        }
        return slot;
    }

    public IInventorySlotHolder build() {
        built = true;
        return slotHolder;
    }
}