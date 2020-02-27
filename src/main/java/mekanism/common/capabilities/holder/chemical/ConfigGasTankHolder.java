package mekanism.common.capabilities.holder.chemical;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.slot.GasSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import net.minecraft.util.Direction;

public class ConfigGasTankHolder extends ConfigChemicalTankHolder<Gas, GasStack> {

    public ConfigGasTankHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        super(facingSupplier, configSupplier);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.GAS;
    }

    @Nonnull
    @Override
    public List<? extends IChemicalTank<Gas, GasStack>> getTanks(@Nullable Direction direction) {
        if (direction == null) {
            //If we want the internal, give all of our slots
            return tanks;
        }
        TileComponentConfig config = configSupplier.get();
        if (config == null) {
            //If we don't have a config (most likely case is it hasn't been setup yet), just return all slots
            return tanks;
        }
        ConfigInfo configInfo = config.getConfig(getTransmissionType());
        if (configInfo == null) {
            //We don't support gases in our configuration at all so just return all
            return tanks;
        }
        RelativeSide side = RelativeSide.fromDirections(facingSupplier.get(), direction);
        ISlotInfo slotInfo = configInfo.getSlotInfo(side);
        if (slotInfo instanceof GasSlotInfo && slotInfo.isEnabled()) {
            return ((GasSlotInfo) slotInfo).getTanks();
        }
        return Collections.emptyList();
    }
}