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
        return Math.min(getEnergy(), MekanismConfig.usage.laser.get());
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return side == getOppositeDirection();
    }
}