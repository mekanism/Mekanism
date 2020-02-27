package mekanism.common.capabilities.holder.slot;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.util.Direction;

public interface IInventorySlotHolder extends IHolder {

    @Nonnull
    List<IInventorySlot> getInventorySlots(@Nullable Direction side);
}