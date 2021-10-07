package mekanism.common.tile.laser;

import javax.annotation.Nonnull;
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
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

public class TileEntityLaserAmplifier extends TileEntityLaserReceptor implements IHasMode {

    private static final FloatingLong MAX = FloatingLong.createConst(5_000_000_000L);
    private FloatingLong minThreshold = FloatingLong.ZERO;
    private FloatingLong maxThreshold = MAX;
    private int ticks = 0;
    private int delay = 0;
    private boolean emittingRedstone;
    private RedstoneOutput outputMode = RedstoneOutput.OFF;

    public TileEntityLaserAmplifier() {
        super(MekanismBlocks.LASER_AMPLIFIER);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
    }

    @Override
    protected void addInitialEnergyContainers(EnergyContainerHelper builder) {
        builder.addContainer(energyContainer = LaserEnergyContainer.create(BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.internalOnly, this));
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
    protected void notifyComparatorChange() {
        //Notify neighbors instead of just comparators as we also allow for direct redstone levels
        level.updateNeighborsAt(getBlockPos(), getBlockType());
    }

    public void setDelay(int delay) {
        delay = Math.max(0, delay);
        if (this.delay != delay) {
            this.delay = delay;
            markDirty(false);
        }
    }

    @Override
    public void nextMode() {
        outputMode = outputMode.getNext();
        markDirty(false);
    }

    public void setMinThresholdFromPacket(FloatingLong floatingLong) {
        FloatingLong maxEnergy = energyContainer.getMaxEnergy();
        FloatingLong threshold = maxEnergy.greaterThan(floatingLong) ? floatingLong : maxEnergy.copyAsConst();
        if (!minThreshold.equals(threshold)) {
            minThreshold = threshold;
            markDirty(false);
        }
    }

    public void setMaxThresholdFromPacket(FloatingLong floatingLong) {
        FloatingLong maxEnergy = energyContainer.getMaxEnergy();
        FloatingLong threshold = maxEnergy.greaterThan(floatingLong) ? floatingLong : maxEnergy.copyAsConst();
        if (!maxThreshold.equals(threshold)) {
            maxThreshold = threshold;
            markDirty(false);
        }
    }

    @Override
    protected void loadGeneralPersistentData(CompoundNBT data) {
        super.loadGeneralPersistentData(data);
        NBTUtils.setFloatingLongIfPresent(data, NBTConstants.MIN, value -> minThreshold = value);
        NBTUtils.setFloatingLongIfPresent(data, NBTConstants.MAX, value -> maxThreshold = value);
        NBTUtils.setIntIfPresent(data, NBTConstants.TIME, value -> delay = value);
        NBTUtils.setEnumIfPresent(data, NBTConstants.OUTPUT_MODE, RedstoneOutput::byIndexStatic, mode -> outputMode = mode);
    }

    @Override
    protected void addGeneralPersistentData(CompoundNBT data) {
        super.addGeneralPersistentData(data);
        data.putString(NBTConstants.MIN, minThreshold.toString());
        data.putString(NBTConstants.MAX, maxThreshold.toString());
        data.putInt(NBTConstants.TIME, delay);
        data.putInt(NBTConstants.OUTPUT_MODE, outputMode.ordinal());
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
            markDirty(false);
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