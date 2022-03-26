package mekanism.common.tile.laser;

import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.CreativeLaserEnergyContainer;
import mekanism.common.capabilities.energy.LaserEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCreativeLaser extends TileEntityBasicLaser {

    public static final long AMPLIFICATION = 1_000;

    public TileEntityCreativeLaser(BlockPos pos, BlockState state) {
        super(MekanismBlocks.CREATIVE_LASER, pos, state);
    }

    @Override
    protected void addInitialEnergyContainers(EnergyContainerHelper builder, IContentsListener listener) {
        builder.addContainer(energyContainer = CreativeLaserEnergyContainer.create(BasicEnergyContainer.notExternal, BasicEnergyContainer.alwaysTrue, this, listener), RelativeSide.BACK);
    }

    @Override
    protected FloatingLong toFire() {
        return MekanismConfig.usage.laser.get().multiply(AMPLIFICATION);
    }
}
