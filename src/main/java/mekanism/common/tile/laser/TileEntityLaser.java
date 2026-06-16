package mekanism.common.tile.laser;

import mekanism.api.IContentsListener;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.LaserEnergyContainer;
import mekanism.common.capabilities.holder.single.BasicSingleHolder;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityLaser extends TileEntityBasicLaser {

    public TileEntityLaser(BlockPos pos, BlockState state) {
        super(MekanismBlocks.LASER, pos, state);
    }

    @Override
    protected ISingleContainerHolder<IEnergyContainer> getInitialEnergyContainer(IContentsListener listener) {
        energyContainer = LaserEnergyContainer.create(BasicEnergyContainer.notExternal, ConstantPredicates.alwaysTrue(), this, listener);
        return new BasicSingleHolder<>(energyContainer, facingSupplier, BACK_ONLY);
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