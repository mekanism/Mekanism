package mekanism.common.capabilities.holder.slot;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.holder.ConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigInventorySlotHolder extends ConfigHolder<IInventorySlot> implements IInventorySlotHolder {

    ConfigInventorySlotHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        super(facingSupplier, configSupplier);
    }

    void addSlot(@NotNull IInventorySlot slot) {
        slots.add(slot);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.ITEM;
    }

    @NotNull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction direction) {
        return getSlots(direction, slotInfo -> slotInfo instanceof InventorySlotInfo info ? info.getSlots() : Collections.emptyList());
    }
}