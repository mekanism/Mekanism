package mekanism.common.tile.laser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.api.IIncrementalEnum;
import mekanism.api.NBTConstants;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.LaserEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityLaserAmplifier extends TileEntityLaserReceptor implements IHasMode {

    private FloatingLong minThreshold = FloatingLong.ZERO;
    private FloatingLong maxThreshold = MekanismConfig.storage.laserAmplifier.get();
    private int ticks = 0;
    private int delay = 0;
    private boolean emittingRedstone;
    private RedstoneOutput outputMode = RedstoneOutput.OFF;

    public TileEntityLaserAmplifier(BlockPos pos, BlockState state) {
        super(MekanismBlocks.LASER_AMPLIFIER, pos, state);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
    }

    @Override
    protected void addInitialEnergyContainers(EnergyContainerHelper builder, IContentsListener listener) {
        builder.addContainer(energyContainer = LaserEnergyContainer.create(BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.internalOnly, this, listener));
    }

    @Override
    protected void onUpdateServer() {
        setEmittingRedstone(false);
        if (ticks < delay) {
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
        return ticks >= delay && energyContainer.getEnergy().compareTo(minThreshold) >= 0 && MekanismUtils.canFunction(this);
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
    protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
        return type == SubstanceType.ENERGY;
    }

    @Override
    protected void notifyComparatorChange() {
        //Notify neighbors instead of just comparators as we also allow for direct redstone levels
        level.updateNeighborsAt(getBlockPos(), getBlockType());
    }

    public void setDelay(int delay) {
        delay = Math.max(0, delay);
        if (this.delay != delay) {
            this.delay = delay;
            markForSave();
        }
    }

    @Override
    public void nextMode() {
        outputMode = outputMode.getNext();
        setChanged();
    }

    public void setMinThresholdFromPacket(FloatingLong target) {
        if (updateMinThreshold(target)) {
            markForSave();
        }
    }

    public void setMaxThresholdFromPacket(FloatingLong target) {
        if (updateMaxThreshold(target)) {
            markForSave();
        }
    }

    private boolean updateMinThreshold(FloatingLong target) {
        FloatingLong threshold = getThreshold(target);
        if (!minThreshold.equals(threshold)) {
            minThreshold = threshold;
            //If the min threshold is greater than the max threshold, update max threshold
            if (minThreshold.greaterThan(maxThreshold)) {
                maxThreshold = minThreshold;
            }
            return true;
        }
        return false;
    }

    private boolean updateMaxThreshold(FloatingLong target) {
        //Cap threshold at max energy capacity
        FloatingLong threshold = getThreshold(target);
        if (!maxThreshold.equals(threshold)) {
            maxThreshold = threshold;
            //If the max threshold is smaller than the min threshold, update min threshold
            if (maxThreshold.smallerThan(minThreshold)) {
                minThreshold = maxThreshold;
            }
            return true;
        }
        return false;
    }

    private FloatingLong getThreshold(FloatingLong target) {
        FloatingLong maxEnergy = energyContainer.getMaxEnergy();
        return target.smallerOrEqual(maxEnergy) ? target : maxEnergy.copyAsConst();
    }

    @Override
    protected void loadGeneralPersistentData(CompoundTag data) {
        super.loadGeneralPersistentData(data);
        NBTUtils.setFloatingLongIfPresent(data, NBTConstants.MIN, this::updateMinThreshold);
        NBTUtils.setFloatingLongIfPresent(data, NBTConstants.MAX, this::updateMaxThreshold);
        NBTUtils.setIntIfPresent(data, NBTConstants.TIME, value -> delay = value);
        NBTUtils.setEnumIfPresent(data, NBTConstants.OUTPUT_MODE, RedstoneOutput::byIndexStatic, mode -> outputMode = mode);
    }

    @Override
    protected void addGeneralPersistentData(CompoundTag data) {
        super.addGeneralPersistentData(data);
        data.putString(NBTConstants.MIN, minThreshold.toString());
        data.putString(NBTConstants.MAX, maxThreshold.toString());
        data.putInt(NBTConstants.TIME, delay);
        NBTUtils.writeEnum(data, NBTConstants.OUTPUT_MODE, outputMode);
    }

    @Override
    public boolean canPulse() {
        return true;
    }

    @ComputerMethod(nameOverride = "getRedstoneOutputMode")
    public RedstoneOutput getOutputMode() {
        return outputMode;
    }

    @ComputerMethod
    public int getDelay() {
        return delay;
    }

    @ComputerMethod
    public FloatingLong getMinThreshold() {
        return minThreshold;
    }

    @ComputerMethod
    public FloatingLong getMaxThreshold() {
        return maxThreshold;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFloatingLong.create(this::getMinThreshold, value -> minThreshold = value));
        container.track(SyncableFloatingLong.create(this::getMaxThreshold, value -> maxThreshold = value));
        container.track(SyncableInt.create(this::getDelay, value -> delay = value));
        container.track(SyncableEnum.create(RedstoneOutput::byIndexStatic, RedstoneOutput.OFF, this::getOutputMode, value -> outputMode = value));
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private void setRedstoneOutputMode(RedstoneOutput mode) throws ComputerException {
        validateSecurityIsPublic();
        if (outputMode != mode) {
            outputMode = mode;
            setChanged();
        }
    }

    @ComputerMethod(nameOverride = "setDelay")
    private void computerSetDelay(int delay) throws ComputerException {
        validateSecurityIsPublic();
        if (delay < 0) {
            throw new ComputerException("Delay cannot be negative. Received: %d", delay);
        }
        setDelay(delay);
    }

    @ComputerMethod
    private void setMinThreshold(FloatingLong threshold) throws ComputerException {
        validateSecurityIsPublic();
        setMinThresholdFromPacket(threshold);
    }

    @ComputerMethod
    private void setMaxThreshold(FloatingLong threshold) throws ComputerException {
        validateSecurityIsPublic();
        setMaxThresholdFromPacket(threshold);
    }
    //End methods IComputerTile

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