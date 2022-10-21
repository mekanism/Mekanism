package mekanism.common.capabilities.holder.slot;

import java.util.Collections;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.holder.QuantumEntangloporterConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuantumEntangloporterInventorySlotHolder extends QuantumEntangloporterConfigHolder<IInventorySlot> implements IInventorySlotHolder {

    public QuantumEntangloporterInventorySlotHolder(TileEntityQuantumEntangloporter entangloporter) {
        super(entangloporter);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.ITEM;
    }

    @NotNull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return entangloporter.hasFrequency() && entangloporter.hasInventory() ? entangloporter.getFreq().getInventorySlots(side) : Collections.emptyList();
    }
}