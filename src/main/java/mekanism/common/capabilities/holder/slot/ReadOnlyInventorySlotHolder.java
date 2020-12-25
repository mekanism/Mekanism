package mekanism.common.capabilities.holder.slot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.util.Direction;

public class ReadOnlyInventorySlotHolder implements IInventorySlotHolder {

    private final List<IInventorySlot> inventorySlots = new ArrayList<>();

    ReadOnlyInventorySlotHolder() {
    }

    void addSlot(@Nonnull IInventorySlot slot) {
        inventorySlots.add(slot);
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction direction) {
        //Only expose the slots if it is internal
        return direction == null ? inventorySlots : Collections.emptyList();
    }

    @Override
    public boolean canInsert(@Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canExtract(@Nullable Direction direction) {
        return false;
    }
}