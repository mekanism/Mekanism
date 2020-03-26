package mekanism.common.tile.laser;

import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.api.math.FloatingLong;
import mekanism.api.inventory.AutomationType;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.providers.IBlockProvider;
import mekanism.client.ClientLaserManager;
import mekanism.common.LaserManager;
import mekanism.common.LaserManager.LaserInfo;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.LaserEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;

public abstract class TileEntityBasicLaser extends TileEntityMekanism {

    protected LaserEnergyContainer energyContainer;
    private Coord4D digging;
    private FloatingLong diggingProgress = FloatingLong.ZERO;
    private FloatingLong lastFired = FloatingLong.ZERO;

    public TileEntityBasicLaser(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        addInitialEnergyContainers(builder);
        return builder.build();
    }

    protected abstract void addInitialEnergyContainers(EnergyContainerHelper builder);

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (getActive()) {
            BlockRayTraceResult mop = ClientLaserManager.fireLaserClient(this, getDirection(), world);
            Coord4D hitCoord = new Coord4D(mop, world);
            if (!hitCoord.equals(digging)) {
                digging = mop.getType() == Type.MISS ? null : hitCoord;
                diggingProgress = FloatingLong.ZERO;
            }
            if (mop.getType() != Type.MISS) {
                BlockState blockHit = world.getBlockState(hitCoord.getPos());
                TileEntity tileHit = MekanismUtils.getTileEntity(world, hitCoord.getPos());
                float hardness = blockHit.getBlockHardness(world, hitCoord.getPos());
                if (hardness >= 0) {
                    Optional<ILaserReceptor> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tileHit, Capabilities.LASER_RECEPTOR_CAPABILITY, mop.getFace()));
                    if (!capability.isPresent() || capability.get().canLasersDig()) {
                        diggingProgress = diggingProgress.plusEqual(lastFired);
                        if (diggingProgress.smallerThan(MekanismConfig.general.laserEnergyNeededPerHardness.get().multiply(hardness))) {
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
        FloatingLong firing = energyContainer.extract(toFire(), Action.SIMULATE, AutomationType.INTERNAL);
        if (!firing.isZero()) {
            if (!firing.equals(lastFired) || !getActive()) {
                setActive(true);
                lastFired = firing;
                sendUpdatePacket();
            }
            LaserInfo info = LaserManager.fireLaser(this, getDirection(), firing, world);
            Coord4D hitCoord = new Coord4D(info.movingPos, world);
            if (!hitCoord.equals(digging)) {
                digging = info.movingPos.getType() == Type.MISS ? null : hitCoord;
                diggingProgress = FloatingLong.ZERO;
            }
            if (info.movingPos.getType() != Type.MISS) {
                BlockState blockHit = world.getBlockState(hitCoord.getPos());
                TileEntity tileHit = MekanismUtils.getTileEntity(world, hitCoord.getPos());
                float hardness = blockHit.getBlockHardness(world, hitCoord.getPos());
                if (hardness >= 0) {
                    Optional<ILaserReceptor> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tileHit, Capabilities.LASER_RECEPTOR_CAPABILITY, info.movingPos.getFace()));
                    if (!capability.isPresent() || capability.get().canLasersDig()) {
                        diggingProgress = diggingProgress.plusEqual(firing);
                        if (diggingProgress.compareTo(MekanismConfig.general.laserEnergyNeededPerHardness.get().multiply(hardness)) >= 0) {
                            handleBreakBlock(hitCoord);
                            diggingProgress = FloatingLong.ZERO;
                        }
                        //TODO: Else tell client to spawn hit effect, instead of having there be client side onUpdate code for TileEntityLaser
                    }
                }
            }
            energyContainer.extract(firing, Action.EXECUTE, AutomationType.INTERNAL);
            setEmittingRedstone(info.foundEntity);
        } else if (getActive()) {
            setActive(false);
            if (!diggingProgress.isZero()) {
                diggingProgress = FloatingLong.ZERO;
            }
            if (!lastFired.isZero()) {
                lastFired = FloatingLong.ZERO;
                sendUpdatePacket();
            }
        }
    }

    protected void setEmittingRedstone(boolean foundEntity) {
    }

    protected void handleBreakBlock(Coord4D coord) {
        LaserManager.breakBlock(coord, true, world, pos);
    }

    protected FloatingLong toFire() {
        return FloatingLong.MAX_VALUE;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        NBTUtils.setFloatingLongIfPresent(nbtTags, NBTConstants.LAST_FIRED, value -> lastFired = value);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putString(NBTConstants.LAST_FIRED, lastFired.toString());
        return nbtTags;
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT updateTag = super.getUpdateTag();
        updateTag.putString(NBTConstants.LAST_FIRED, lastFired.toString());
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setFloatingLongIfPresent(tag, NBTConstants.LAST_FIRED, fired -> lastFired = fired);
    }

    public LaserEnergyContainer getEnergyContainer() {
        return energyContainer;
    }
}