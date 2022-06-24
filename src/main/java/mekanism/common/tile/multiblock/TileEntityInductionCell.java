package mekanism.common.tile.multiblock;

import mekanism.api.IContentsListener;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityInductionCell extends TileEntityInternalMultiblock {

    private MachineEnergyContainer<TileEntityInductionCell> energyContainer;
    public InductionCellTier tier;

    public TileEntityInductionCell(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        //Never externally expose the energy capability
        addDisabledCapabilities(EnergyCompatUtils.getEnabledEnergyCapabilities());
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.internal(this, listener));
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
