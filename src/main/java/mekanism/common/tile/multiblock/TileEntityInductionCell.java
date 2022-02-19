package mekanism.common.tile.multiblock;

import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.base.TileEntityMekanism;

public class TileEntityInductionCell extends TileEntityMekanism {

    private MachineEnergyContainer<TileEntityInductionCell> energyContainer;
    public InductionCellTier tier;

    public TileEntityInductionCell(IBlockProvider blockProvider) {
        super(blockProvider);
        //Never externally expose the energy capability
        addDisabledCapabilities(EnergyCompatUtils.getEnabledEnergyCapabilities());
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.internal(this));
        return builder.build();
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        tier = Attribute.getTier(getBlockType(), InductionCellTier.class);
    }

    public MachineEnergyContainer<TileEntityInductionCell> getEnergyContainer() {
        return energyContainer;
    }
}
