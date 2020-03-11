package mekanism.common.tile.laser;

import java.util.Optional;
import mekanism.api.Coord4D;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.providers.IBlockProvider;
import mekanism.client.ClientLaserManager;
import mekanism.common.LaserManager;
import mekanism.common.LaserManager.LaserInfo;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;

public abstract class TileEntityBasicLaser extends TileEntityMekanism {

    protected Coord4D digging;
    protected double diggingProgress;

    public TileEntityBasicLaser(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (getActive()) {
            BlockRayTraceResult mop = ClientLaserManager.fireLaserClient(this, getDirection(), world);
            Coord4D hitCoord = new Coord4D(mop, world);
            if (!hitCoord.equals(digging)) {
                digging = mop.getType() == Type.MISS ? null : hitCoord;
                diggingProgress = 0;
            }
            if (mop.getType() != Type.MISS) {
                BlockState blockHit = world.getBlockState(hitCoord.getPos());
                TileEntity tileHit = MekanismUtils.getTileEntity(world, hitCoord.getPos());
                float hardness = blockHit.getBlockHardness(world, hitCoord.getPos());
                if (hardness >= 0) {
                    Optional<ILaserReceptor> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tileHit, Capabilities.LASER_RECEPTOR_CAPABILITY, mop.getFace()));
                    if (!capability.isPresent() || capability.get().canLasersDig()) {
                        diggingProgress += getLastFired();
                        if (diggingProgress < hardness * MekanismConfig.general.laserEnergyNeededPerHardness.get()) {
                            Mekanism.proxy.addHitEffects(hitCoord, mop);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        double firing = toFire();
        if (firing > 0) {
            checkLastFired(firing);
            LaserInfo info = LaserManager.fireLaser(this, getDirection(), firing, world);
            Coord4D hitCoord = new Coord4D(info.movingPos, world);
            if (!hitCoord.equals(digging)) {
                digging = info.movingPos.getType() == Type.MISS ? null : hitCoord;
                diggingProgress = 0;
            }
            if (info.movingPos.getType() != Type.MISS) {
                BlockState blockHit = world.getBlockState(hitCoord.getPos());
                TileEntity tileHit = MekanismUtils.getTileEntity(world, hitCoord.getPos());
                float hardness = blockHit.getBlockHardness(world, hitCoord.getPos());
                if (hardness >= 0) {
                    Optional<ILaserReceptor> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tileHit, Capabilities.LASER_RECEPTOR_CAPABILITY, info.movingPos.getFace()));
                    if (!capability.isPresent() || capability.get().canLasersDig()) {
                        diggingProgress += firing;
                        if (diggingProgress >= hardness * MekanismConfig.general.laserEnergyNeededPerHardness.get()) {
                            handleBreakBlock(hitCoord);
                            diggingProgress = 0;
                        }
                        //TODO: Else tell client to spawn hit effect, instead of having there be client side onUpdate code for TileEntityLaser
                    }
                }
            }
            setEnergy(getEnergy() - firing);
            setEmittingRedstone(info.foundEntity);
        } else if (getActive()) {
            setActive(false);
            diggingProgress = 0;
        }
    }

    protected void setEmittingRedstone(boolean foundEntity) {
    }

    protected void checkLastFired(double firing) {
    }

    protected void handleBreakBlock(Coord4D coord) {
        LaserManager.breakBlock(coord, true, world, pos);
    }

    protected double toFire() {
        return getEnergy();
    }

    protected abstract double getLastFired();

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return false;
    }
}