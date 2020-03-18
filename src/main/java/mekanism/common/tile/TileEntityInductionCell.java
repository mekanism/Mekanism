package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.basic.BlockInductionCell;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.integration.EnergyCompatUtils;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityInductionCell extends TileEntityMekanism {

    private MachineEnergyContainer<TileEntityInductionCell> energyContainer;
    public InductionCellTier tier;

    public TileEntityInductionCell(IBlockProvider blockProvider) {
        super(blockProvider);
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
        tier = Attribute.getTier(getBlockType(), InductionCellTier.class);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        //Never externally expose the energy capability
        return EnergyCompatUtils.isEnergyCapability(capability) || super.isCapabilityDisabled(capability, side);
    }

    public MachineEnergyContainer<TileEntityInductionCell> getEnergyContainer() {
        return energyContainer;
    }
}
