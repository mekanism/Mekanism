package mekanism.common.tile.multiblock;

import mekanism.api.IContentsListener;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TileEntityInductionCell extends TileEntityInternalMultiblock {

    private MachineEnergyContainer<TileEntityInductionCell> energyContainer;
    public InductionCellTier tier;

    public TileEntityInductionCell(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    protected @Nullable IEnergyContainerHolder getInitialEnergyContainer(IContentsListener listener) {
        energyContainer = MachineEnergyContainer.internal(this, listener);
        return _ -> energyContainer;
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        tier = Attribute.getTier(getBlockHolder(), InductionCellTier.class);
    }

    public MachineEnergyContainer<TileEntityInductionCell> energyContainer() {
        return energyContainer;
    }
}
