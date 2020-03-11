package mekanism.common.tile.laser;

import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.util.Direction;

public class TileEntityLaser extends TileEntityBasicLaser {

    public TileEntityLaser() {
        super(MekanismBlocks.LASER);
    }

    @Override
    protected double toFire() {
        return MekanismConfig.usage.laser.get();
    }

    @Override
    protected double getLastFired() {
        return MekanismConfig.usage.laser.get();
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return side == getOppositeDirection();
    }

    @Override
    protected void checkLastFired(double firing) {
        if (!getActive()) {
            setActive(true);
        }
    }
}