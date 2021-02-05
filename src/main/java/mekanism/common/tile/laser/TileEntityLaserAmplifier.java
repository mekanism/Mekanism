package mekanism.common.tile.laser;

import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
import mekanism.api.NBTConstants;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.LaserEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;

public class TileEntityLaserAmplifier extends TileEntityLaserReceptor implements IHasMode {

    private static final FloatingLong MAX = FloatingLong.createConst(5_000_000_000L);
    public FloatingLong minThreshold = FloatingLong.ZERO;
    public FloatingLong maxThreshold = MAX;
    public int ticks = 0;
    public int time = 0;
    public boolean emittingRedstone;
    public RedstoneOutput outputMode = RedstoneOutput.OFF;

    public TileEntityLaserAmplifier() {
        super(MekanismBlocks.LASER_AMPLIFIER);
    }

    @Override
    protected void addInitialEnergyContainers(EnergyContainerHelper builder) {
        builder.addContainer(energyContainer = LaserEnergyContainer.create(BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.internalOnly, this));
    }

    @Override
    protected void onUpdateServer() {
        setEmittingRedstone(false);
        if (ticks < time) {
            ticks++;
        } else {
            ticks = 0;
        }
        super.onUpdateServer();
        if (outputMode != RedstoneOutput.ENTITY_DETECTION) {
            setEmittingRedstone(false);
        }
    }

    @Override
    protected void setEmittingRedstone(boolean foundEntity) {
        emittingRedstone = foundEntity;
    }

    private boolean shouldFire() {
        return ticks >= time && energyContainer.getEnergy().compareTo(minThreshold) >= 0 && MekanismUtils.canFunction(this);
    }

    @Override
    protected FloatingLong toFire() {
        return shouldFire() ? super.toFire().min(maxThreshold) : FloatingLong.ZERO;
    }

    @Override
    public int getRedstoneLevel() {
        if (outputMode == RedstoneOutput.ENERGY_CONTENTS) {
            return MekanismUtils.redstoneLevelFromContents(energyContainer.getEnergy(), energyContainer.getMaxEnergy());
        }
        return emittingRedstone ? 15 : 0;
    }

    @Override
    protected void notifyComparatorChange() {
        //Notify neighbors instead of just comparators as we also allow for direct redstone levels
        world.notifyNeighborsOfStateChange(getPos(), getBlockType());
    }

    public void setTime(int time) {
        this.time = Math.max(0, time);
        markDirty(false);
    }

    @Override
    public void nextMode() {
        outputMode = outputMode.getNext();
        markDirty(false);
    }

    public void setMinThresholdFromPacket(FloatingLong floatingLong) {
        FloatingLong maxEnergy = energyContainer.getMaxEnergy();
        minThreshold = maxEnergy.greaterThan(floatingLong) ? floatingLong : maxEnergy.copyAsConst();
        markDirty(false);
    }

    public void setMaxThresholdFromPacket(FloatingLong floatingLong) {
        FloatingLong maxEnergy = energyContainer.getMaxEnergy();
        maxThreshold = maxEnergy.greaterThan(floatingLong) ? floatingLong : maxEnergy.copyAsConst();
        markDirty(false);
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);
        NBTUtils.setFloatingLongIfPresent(nbtTags, NBTConstants.MIN, value -> minThreshold = value);
        NBTUtils.setFloatingLongIfPresent(nbtTags, NBTConstants.MAX, value -> maxThreshold = value);
        NBTUtils.setIntIfPresent(nbtTags, NBTConstants.TIME, value -> time = value);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.OUTPUT_MODE, RedstoneOutput::byIndexStatic, mode -> outputMode = mode);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putString(NBTConstants.MIN, minThreshold.toString());
        nbtTags.putString(NBTConstants.MAX, maxThreshold.toString());
        nbtTags.putInt(NBTConstants.TIME, time);
        nbtTags.putInt(NBTConstants.OUTPUT_MODE, outputMode.ordinal());
        return nbtTags;
    }

    @Override
    public boolean canPulse() {
        return true;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFloatingLong.create(() -> minThreshold, value -> minThreshold = value));
        container.track(SyncableFloatingLong.create(() -> maxThreshold, value -> maxThreshold = value));
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
            return MathUtils.getByIndexMod(MODES, index);
        }
    }
}