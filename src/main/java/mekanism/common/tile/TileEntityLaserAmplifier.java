package mekanism.common.tile;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IIncrementalEnum;
import mekanism.api.NBTConstants;
import mekanism.api.TileNetworkList;
import mekanism.api.energy.IStrictEnergyOutputter;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.client.ClientLaserManager;
import mekanism.common.LaserManager;
import mekanism.common.LaserManager.LaserInfo;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityLaserAmplifier extends TileEntityMekanism implements ILaserReceptor, IStrictEnergyOutputter, IStrictEnergyStorage {

    public static final double MAX_ENERGY = 5E9;
    public double collectedEnergy = 0;
    private double lastFired = 0;
    public double minThreshold = 0;
    public double maxThreshold = 5E9;
    public int ticks = 0;
    public int time = 0;
    public boolean on = false;
    private Coord4D digging;
    private double diggingProgress;
    public boolean emittingRedstone;
    public RedstoneOutput outputMode = RedstoneOutput.OFF;

    public TileEntityLaserAmplifier() {
        super(MekanismBlocks.LASER_AMPLIFIER);
    }

    @Override
    public void receiveLaserEnergy(double energy, Direction side) {
        setEnergy(getEnergy() + energy);
    }

    @Override
    public boolean canLasersDig() {
        return false;
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (on) {
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
                        diggingProgress += lastFired;
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
        boolean prevRedstone = emittingRedstone;
        emittingRedstone = false;
        if (ticks < time) {
            ticks++;
        } else {
            ticks = 0;
        }

        if (toFire() > 0) {
            double firing = toFire();
            if (!on || firing != lastFired) {
                on = true;
                lastFired = firing;
                Mekanism.packetHandler.sendUpdatePacket(this);
            }

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
                            LaserManager.breakBlock(hitCoord, true, world, pos);
                            diggingProgress = 0;
                        }
                        //TODO: Else tell client to spawn hit effect, instead of having there be client side onUpdate code for TileEntityLaser
                    }
                }
            }
            emittingRedstone = info.foundEntity;
            setEnergy(getEnergy() - firing);
        } else if (on) {
            on = false;
            diggingProgress = 0;
            Mekanism.packetHandler.sendUpdatePacket(this);
        }

        if (outputMode != RedstoneOutput.ENTITY_DETECTION) {
            emittingRedstone = false;
        }
        if (emittingRedstone != prevRedstone) {
            world.notifyNeighborsOfStateChange(getPos(), getBlockType());
        }
    }

    @Override
    public double pullEnergy(Direction side, double amount, boolean simulate) {
        double toGive = Math.min(getEnergy(), amount);
        if (toGive < 0.0001) {
            return 0;
        }
        if (!simulate) {
            setEnergy(getEnergy() - toGive);
        }
        return toGive;
    }

    @Override
    public double getEnergy() {
        return collectedEnergy;
    }

    @Override
    public void setEnergy(double energy) {
        collectedEnergy = Math.max(0, Math.min(energy, MAX_ENERGY));
    }

    private boolean shouldFire() {
        return collectedEnergy >= minThreshold && ticks >= time && MekanismUtils.canFunction(this);
    }

    private double toFire() {
        return shouldFire() ? Math.min(collectedEnergy, maxThreshold) : 0;
    }

    @Override
    public int getRedstoneLevel() {
        //TODO: Do we have to keep track of this and when it changes notify the comparator the level changed. Probably
        if (outputMode == RedstoneOutput.ENERGY_CONTENTS) {
            return MekanismUtils.redstoneLevelFromContents(getEnergy(), getMaxEnergy());
        }
        return emittingRedstone ? 15 : 0;
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(on);
        data.add(collectedEnergy);
        data.add(lastFired);
        data.add(emittingRedstone);
        return data;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            switch (dataStream.readInt()) {
                case 0:
                    minThreshold = Math.min(MAX_ENERGY, MekanismUtils.convertToJoules(dataStream.readDouble()));
                    break;
                case 1:
                    maxThreshold = Math.min(MAX_ENERGY, MekanismUtils.convertToJoules(dataStream.readDouble()));
                    break;
                case 2:
                    time = dataStream.readInt();
                    break;
                case 3:
                    outputMode = outputMode.getNext();
                    break;
            }
            return;
        }

        super.handlePacketData(dataStream);

        if (isRemote()) {
            on = dataStream.readBoolean();
            collectedEnergy = dataStream.readDouble();
            lastFired = dataStream.readDouble();
            emittingRedstone = dataStream.readBoolean();
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        on = nbtTags.getBoolean(NBTConstants.RUNNING);
        minThreshold = nbtTags.getDouble(NBTConstants.MIN);
        maxThreshold = nbtTags.getDouble(NBTConstants.MAX);
        time = nbtTags.getInt(NBTConstants.TIME);
        //TODO: Replace collected energy with allowing it to be energy stored in TileEntityMekanism
        collectedEnergy = nbtTags.getDouble(NBTConstants.ENERGY_STORED);
        lastFired = nbtTags.getDouble(NBTConstants.LAST_FIRED);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.OUTPUT_MODE, RedstoneOutput::byIndexStatic, mode -> outputMode = mode);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.RUNNING, on);
        nbtTags.putDouble(NBTConstants.MIN, minThreshold);
        nbtTags.putDouble(NBTConstants.MAX, maxThreshold);
        nbtTags.putInt(NBTConstants.TIME, time);
        nbtTags.putDouble(NBTConstants.ENERGY_STORED, collectedEnergy);
        nbtTags.putDouble(NBTConstants.LAST_FIRED, lastFired);
        nbtTags.putInt(NBTConstants.OUTPUT_MODE, outputMode.ordinal());
        return nbtTags;
    }

    @Override
    public boolean canPulse() {
        return true;
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        return true;
    }

    @Override
    public double getMaxEnergy() {
        return MAX_ENERGY;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.ENERGY_STORAGE_CAPABILITY) {
            return Capabilities.ENERGY_STORAGE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY) {
            return Capabilities.ENERGY_OUTPUTTER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.LASER_RECEPTOR_CAPABILITY) {
            return Capabilities.LASER_RECEPTOR_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(() -> collectedEnergy, value -> collectedEnergy = value));
        container.track(SyncableDouble.create(() -> minThreshold, value -> minThreshold = value));
        container.track(SyncableDouble.create(() -> maxThreshold, value -> maxThreshold = value));
        container.track(SyncableInt.create(() -> time, value -> time = value));
        container.track(SyncableEnum.create(RedstoneOutput::byIndexStatic, RedstoneOutput.OFF, () -> outputMode, value -> outputMode = value));
    }

    public enum RedstoneOutput implements IIncrementalEnum<RedstoneOutput>, IHasTranslationKey {
        OFF(MekanismLang.OFF),
        ENTITY_DETECTION(MekanismLang.ENTITY_DETECTION),
        ENERGY_CONTENTS(MekanismLang.ENERGY_CONTENTS);

        private static final RedstoneOutput[] MODES = values();
        private final ILangEntry langEntry;

        RedstoneOutput(ILangEntry langEntry) {
            this.langEntry = langEntry;
        }

        @Override
        public String getTranslationKey() {
            return langEntry.getTranslationKey();
        }

        @Nonnull
        @Override
        public RedstoneOutput byIndex(int index) {
            return byIndexStatic(index);
        }

        public static RedstoneOutput byIndexStatic(int index) {
            //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
            return MODES[Math.floorMod(index, MODES.length)];
        }
    }
}