package mekanism.common.tile.laser;

import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.LaserEnergyContainer;
import mekanism.common.capabilities.holder.energy.BasicEnergyHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class TileEntityLaser extends TileEntityBasicLaser {

    public TileEntityLaser(BlockPos pos, BlockState state) {
        super(MekanismBlocks.LASER, pos, state);
    }

    @Override
    protected @Nullable IEnergyContainerHolder getInitialEnergyContainer(IContentsListener listener) {
        energyContainer = LaserEnergyContainer.create(BasicEnergyContainer.notExternal, ConstantPredicates.alwaysTrue(), this, listener);
        return new BasicEnergyHolder(energyContainer, facingSupplier, BACK_ONLY);
    }

    @Override
    protected int toFire() {
        return MekanismConfig.usage.laser.get();
    }

    @Override
    public float getInitialVolume() {
        return 0.3f;
    }
}