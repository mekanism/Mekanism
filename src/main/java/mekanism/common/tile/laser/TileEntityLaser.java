package mekanism.common.tile.laser;

import mekanism.api.RelativeSide;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.LaserEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;

public class TileEntityLaser extends TileEntityBasicLaser {

    public TileEntityLaser() {
        super(MekanismBlocks.LASER);
    }

    @Override
    protected void addInitialEnergyContainers(EnergyContainerHelper builder) {
        builder.addContainer(energyContainer = LaserEnergyContainer.create(BasicEnergyContainer.notExternal, BasicEnergyContainer.alwaysTrue, this), RelativeSide.BACK);
    }

    @Override
    protected FloatingLong toFire() {
        return MekanismConfig.usage.laser.get();
    }
}