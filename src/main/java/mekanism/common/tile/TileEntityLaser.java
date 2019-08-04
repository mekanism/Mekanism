package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.common.LaserManager;
import mekanism.common.LaserManager.LaserInfo;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.prefab.TileEntityEffectsBlock;
import mekanism.common.util.InventoryUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityLaser extends TileEntityEffectsBlock {

    public Coord4D digging;
    public double diggingProgress;

    public TileEntityLaser() {
        super("machine.laser", "Laser", MekanismConfig.current().storage.laser.val());
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) {
            if (isActive) {
                RayTraceResult mop = LaserManager.fireLaserClient(this, getDirection(), MekanismConfig.current().usage.laser.val(), world);
                Coord4D hitCoord = mop == null ? null : new Coord4D(mop, world);
                if (hitCoord == null || !hitCoord.equals(digging)) {
                    digging = hitCoord;
                    diggingProgress = 0;
                }
                if (hitCoord != null) {
                    IBlockState blockHit = hitCoord.getBlockState(world);
                    TileEntity tileHit = hitCoord.getTileEntity(world);
                    float hardness = blockHit.getBlockHardness(world, hitCoord.getPos());
                    if (!(hardness < 0 || (LaserManager.isReceptor(tileHit, mop.sideHit) && !LaserManager.getReceptor(tileHit, mop.sideHit).canLasersDig()))) {
                        diggingProgress += MekanismConfig.current().usage.laser.val();
                        if (diggingProgress < hardness * MekanismConfig.current().general.laserEnergyNeededPerHardness.val()) {
                            Mekanism.proxy.addHitEffects(hitCoord, mop);
                        }
                    }
                }
            }
        } else {
            if (getEnergy() >= MekanismConfig.current().usage.laser.val()) {
                setActive(true);
                LaserInfo info = LaserManager.fireLaser(this, getDirection(), MekanismConfig.current().usage.laser.val(), world);
                Coord4D hitCoord = info.movingPos == null ? null : new Coord4D(info.movingPos, world);

                if (hitCoord == null || !hitCoord.equals(digging)) {
                    digging = hitCoord;
                    diggingProgress = 0;
                }
                if (hitCoord != null) {
                    IBlockState blockHit = hitCoord.getBlockState(world);
                    TileEntity tileHit = hitCoord.getTileEntity(world);
                    float hardness = blockHit.getBlockHardness(world, hitCoord.getPos());
                    if (!(hardness < 0 || (LaserManager.isReceptor(tileHit, info.movingPos.sideHit) && !LaserManager.getReceptor(tileHit, info.movingPos.sideHit).canLasersDig()))) {
                        diggingProgress += MekanismConfig.current().usage.laser.val();
                        if (diggingProgress >= hardness * MekanismConfig.current().general.laserEnergyNeededPerHardness.val()) {
                            LaserManager.breakBlock(hitCoord, true, world, pos);
                            diggingProgress = 0;
                        }
                    }
                }
                setEnergy(getEnergy() - MekanismConfig.current().usage.laser.val());
            } else {
                setActive(false);
                diggingProgress = 0;
            }
        }
    }

    @Override
    public boolean canReceiveEnergy(EnumFacing side) {
        return side == getOppositeDirection();
    }

    @Override
    public boolean renderUpdate() {
        return false;
    }

    @Override
    public boolean lightUpdate() {
        return false;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return InventoryUtils.EMPTY;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }
}