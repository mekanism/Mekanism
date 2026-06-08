package mekanism.common.capabilities.holder.energy;

import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.holder.QEConfigHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;
import org.jspecify.annotations.Nullable;

public class QEEnergyHolder extends QEConfigHolder<@Nullable IEnergyContainer> implements IEnergyContainerHolder {

    private final Lazy<IEnergyContainer> clientContainer = Lazy.of(() -> BasicEnergyContainer.create(MekanismConfig.general.entangloporterEnergyBuffer.getAsLong(), null));

    public QEEnergyHolder(TileEntityQuantumEntangloporter entangloporter) {
        super(entangloporter, TransmissionType.ENERGY, EnergyConfigHolder.SLOT_PARSER, InventoryFrequency::getEnergyContainer);
    }

    @Nullable
    @Override
    public IEnergyContainer getContainer(@Nullable Direction side) {
        IEnergyContainer container = getData(side);
        if (container == null) {
            Level level = entangloporter.getLevel();
            if (level != null && level.isClientSide()) {
                return clientContainer.get();
            }
        }
        return container;
    }

    @Override
    protected @Nullable IEnergyContainer defaultValue() {
        return null;
    }
}