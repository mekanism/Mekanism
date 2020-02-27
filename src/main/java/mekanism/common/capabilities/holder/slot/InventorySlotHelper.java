package mekanism.common.capabilities.holder.slot;

import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.RelativeSide;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.util.Direction;

//TODO: Replace the builder with this and then have two IInventorySlotHolder implementations
public class InventorySlotHelper {

    private final IInventorySlotHolder slotHolder;
    private boolean built;

    private InventorySlotHelper(IInventorySlotHolder slotHolder) {
        this.slotHolder = slotHolder;
    }

    public static InventorySlotHelper forSide(Supplier<Direction> facingSupplier) {
        return forSide(facingSupplier, side -> true, side -> true);
    }

    public static InventorySlotHelper forSide(Supplier<Direction> facingSupplier, Predicate<RelativeSide> insertPredicate, Predicate<RelativeSide> extractPredicate) {
        return new InventorySlotHelper(new InventorySlotHolder(facingSupplier, insertPredicate, extractPredicate));
    }

    public static InventorySlotHelper forSideWithConfig(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        return new InventorySlotHelper(new ConfigInventorySlotHolder(facingSupplier, configSupplier));
    }

    public void addSlot(@Nonnull IInventorySlot slot) {
        if (built) {
            throw new RuntimeException("Builder has already built.");
        }
        if (slotHolder instanceof InventorySlotHolder) {
            ((InventorySlotHolder) slotHolder).addSlot(slot);
        } else if (slotHolder instanceof ConfigInventorySlotHolder) {
            ((ConfigInventorySlotHolder) slotHolder).addSlot(slot);
        }
        //TODO: Else warning?
    }

    public void addSlot(@Nonnull IInventorySlot slot, RelativeSide... sides) {
        if (built) {
            throw new RuntimeException("Builder has already built.");
        }
        if (slotHolder instanceof InventorySlotHolder) {
            ((InventorySlotHolder) slotHolder).addSlot(slot, sides);
        }
        //TODO: Else warning?
    }

    public IInventorySlotHolder build() {
        built = true;
        return slotHolder;
    }
}