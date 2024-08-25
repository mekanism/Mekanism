package mekanism.common.capabilities.holder.chemical;

import java.util.Collections;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.capabilities.holder.ConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigChemicalTankHolder extends ConfigHolder<IChemicalTank> implements IChemicalTankHolder {

    protected ConfigChemicalTankHolder(ISideConfiguration sideConfiguration) {
        super(sideConfiguration);
    }

    void addTank(IChemicalTank tank) {
        slots.add(tank);
    }

    @NotNull
    @Override
    public List<IChemicalTank> getTanks(@Nullable Direction direction) {
        return getSlots(direction, slotInfo -> slotInfo instanceof ChemicalSlotInfo info ? info.getTanks() : Collections.emptyList());
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.CHEMICAL;
    }
}