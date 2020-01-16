package mekanism.common.tile;

import mekanism.api.Coord4D;
import mekanism.client.ClientLaserManager;
import mekanism.common.LaserManager;
import mekanism.common.LaserManager.LaserInfo;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;

public class TileEntityLaser extends TileEntityMekanism {

    public Coord4D digging;
    public double diggingProgress;

    public TileEntityLaser() {
        super(MekanismBlocks.LASER);
    }

    @Override
    public void onUpdate() {
        if (isRemote()) {
            if (getActive()) {
                BlockRayTraceResult mop = ClientLaserManager.fireLaserClient(this, getDirection(), world);
                Coord4D hitCoord = new Coord4D(mop, world);
                if (!hitCoord.equals(digging)) {
                    digging = mop.getType() == Type.MISS ? null : hitCoord;
                    digging = hitCoord;
                    diggingProgress = 0;
                }
                if (mop.getType() != Type.MISS) {
                    BlockState blockHit = world.getBlockState(hitCoord.getPos());
                    TileEntity tileHit = MekanismUtils.getTileEntity(world, hitCoord.getPos());
                    float hardness = blockHit.getBlockHardness(world, hitCoord.getPos());
                    if (hardness >= 0 && !CapabilityUtils.getCapabilityHelper(tileHit, Capabilities.LASER_RECEPTOR_CAPABILITY, mop.getFace()).matches(receptor -> !receptor.canLasersDig())) {
                        diggingProgress += MekanismConfig.usage.laser.get();
                        if (diggingProgress < hardness * MekanismConfig.general.laserEnergyNeededPerHardness.get()) {
                            Mekanism.proxy.addHitEffects(hitCoord, mop);
                        }
                    }
                }
            }
        } else {
            if (getEnergy() >= MekanismConfig.usage.laser.get()) {
                setActive(true);
                LaserInfo info = LaserManager.fireLaser(this, getDirection(), MekanismConfig.usage.laser.get(), world);
                Coord4D hitCoord = new Coord4D(info.movingPos, world);
                if (!hitCoord.equals(digging)) {
                    digging = info.movingPos.getType() == Type.MISS ? null : hitCoord;
                    diggingProgress = 0;
                }
                if (info.movingPos.getType() != Type.MISS) {
                    BlockState blockHit = world.getBlockState(hitCoord.getPos());
                    TileEntity tileHit = MekanismUtils.getTileEntity(world, hitCoord.getPos());
                    float hardness = blockHit.getBlockHardness(world, hitCoord.getPos());
                    if (hardness >= 0 && !CapabilityUtils.getCapabilityHelper(tileHit, Capabilities.LASER_RECEPTOR_CAPABILITY, info.movingPos.getFace())
                          .matches(receptor -> !receptor.canLasersDig())) {
                        diggingProgress += MekanismConfig.usage.laser.get();
                        if (diggingProgress >= hardness * MekanismConfig.general.laserEnergyNeededPerHardness.get()) {
                            LaserManager.breakBlock(hitCoord, true, world, pos);
                            diggingProgress = 0;
                        }
                        //TODO: Else tell client to spawn hit effect, instead of having there be client side onUpdate code for TileEntityLaser
                    }
                }
                setEnergy(getEnergy() - MekanismConfig.usage.laser.get());
            } else {
                setActive(false);
                diggingProgress = 0;
            }
        }
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return side == getOppositeDirection();
    }
}