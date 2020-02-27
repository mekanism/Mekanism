package mekanism.common.inventory.slot.holder;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.util.Direction;

@FunctionalInterface
public interface IInventorySlotHolder {

    @Nonnull
    List<IInventorySlot> getInventorySlots(@Nullable Direction side);
}