package mekanism.common.capabilities.holder.slot;

import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IInventorySlotHolder extends IHolder {

    @NotNull
    List<IInventorySlot> getInventorySlots(@Nullable Direction side);
}