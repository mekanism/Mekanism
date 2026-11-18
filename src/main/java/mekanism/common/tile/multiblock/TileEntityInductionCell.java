package mekanism.common.tile.multiblock;

import mekanism.api.IContentsListener;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.UnknownNullability;

public class TileEntityInductionCell extends TileEntityInternalMultiblock {

    @UnknownNullability//Initialized via getInitialEnergyContainer
    private VariableCapacityEnergyContainer energyContainer;
    public final InductionCellTier tier;

    public TileEntityInductionCell(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        tier = Attribute.getTierNN(blockProvider, InductionCellTier.class);
        super(blockProvider, pos, state);
    }

    @Override
    protected ISingleContainerHolder<IEnergyContainer> getInitialEnergyContainer(IContentsListener listener) {
        energyContainer = VariableCapacityEnergyContainer.create(tier::getMaxEnergy, BasicEnergyContainer.internalOnly, BasicEnergyContainer.internalOnly, listener);
        return _ -> energyContainer;
    }

    public VariableCapacityEnergyContainer energyContainer() {
        return energyContainer;
    }
}
