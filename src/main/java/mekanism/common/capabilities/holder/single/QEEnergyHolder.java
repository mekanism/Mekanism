package mekanism.common.capabilities.holder.single;

import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;
import org.jspecify.annotations.Nullable;

public class QEEnergyHolder extends QESingleContainerHolder<IEnergyContainer> {

    private final Lazy<IEnergyContainer> clientContainer = Lazy.of(() -> VariableCapacityEnergyContainer.create(MekanismConfig.general.entangloporterEnergyBuffer, null));

    public QEEnergyHolder(TileEntityQuantumEntangloporter entangloporter) {
        super(entangloporter, TransmissionType.ENERGY, SingleConfigHolder.ENERGY_SLOT_PARSER, InventoryFrequency::getEnergyContainer);
    }

    @Nullable
    @Override
    public IEnergyContainer getContainer(@Nullable Direction side) {
        IEnergyContainer container = super.getContainer(side);
        if (container == null) {
            Level level = entangloporter.getLevel();
            if (level != null && level.isClientSide()) {
                return clientContainer.get();
            }
        }
        return container;
    }
}