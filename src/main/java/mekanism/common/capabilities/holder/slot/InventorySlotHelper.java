package mekanism.common.capabilities.holder.slot;

import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.tile.interfaces.ISideConfiguration;
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

    public static InventorySlotHelper forSideWithConfig(ISideConfiguration sideConfiguration) {
        return new InventorySlotHelper(new ConfigInventorySlotHolder(sideConfiguration));
    }

    public <SLOT extends IInventorySlot> SLOT addSlot(@NotNull SLOT slot) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        switch (slotHolder) {
            case InventorySlotHolder inventorySlotHolder -> inventorySlotHolder.addSlot(slot);
            case ReadOnlyInventorySlotHolder inventorySlotHolder -> inventorySlotHolder.addSlot(slot);
            case ConfigInventorySlotHolder inventorySlotHolder -> inventorySlotHolder.addSlot(slot);
            default -> throw new IllegalArgumentException("Holder does not know how to add slots");
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