package mekanism.common.tile.laser;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.api.IContentsListener;
import mekanism.api.IIncrementalEnum;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.text.IHasTranslationKey.IHasEnumNameTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.LaserEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Redstone;
import org.jetbrains.annotations.NotNull;

public class TileEntityLaserAmplifier extends TileEntityLaserReceptor implements IHasMode {

    private long minThreshold = 0L;
    private long maxThreshold = MekanismConfig.storage.laserAmplifier.get();
    private int ticks = 0;
    private int delay = 0;
    private boolean emittingRedstone;
    private RedstoneOutput outputMode = RedstoneOutput.OFF;

    public TileEntityLaserAmplifier(BlockPos pos, BlockState state) {
        super(MekanismBlocks.LASER_AMPLIFIER, pos, state);
    }

    @Override
    protected void addInitialEnergyContainers(EnergyContainerHelper builder, IContentsListener listener) {
        builder.addContainer(energyContainer = LaserEnergyContainer.create(ConstantPredicates.alwaysTrue(), BasicEnergyContainer.internalOnly, this, listener));
    }

    @Override
    protected boolean onUpdateServer() {
        setEmittingRedstone(false);
        if (ticks < delay) {
            ticks++;
        } else {
            ticks = 0;
        }
        boolean sendUpdatePacket = super.onUpdateServer();
        if (outputMode != RedstoneOutput.ENTITY_DETECTION) {
            setEmittingRedstone(false);
        }
        return sendUpdatePacket;
    }

    @Override
    protected void setEmittingRedstone(boolean foundEntity) {
        emittingRedstone = foundEntity;
    }

    private boolean shouldFire() {
        return ticks >= delay && energyContainer.getEnergy() >= minThreshold && canFunction();
    }

    @Override
    protected long toFire() {
        return shouldFire() ? Math.min(super.toFire(), maxThreshold) : 0L;
    }

    @Override
    public int getRedstoneLevel() {
        if (outputMode == RedstoneOutput.ENERGY_CONTENTS) {
            return MekanismUtils.redstoneLevelFromContents(energyContainer.getEnergy(), energyContainer.getMaxEnergy());
        }
        return emittingRedstone ? Redstone.SIGNAL_MAX : Redstone.SIGNAL_NONE;
    }

    @Override
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return type == ContainerType.ENERGY;
    }

    @Override
    protected void notifyComparatorChange() {
        //Notify neighbors instead of just comparators as we also allow for direct redstone levels
        level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
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

    @Override
    public void previousMode() {
        outputMode = outputMode.getPrevious();
        setChanged();
    }

    public void setMinThresholdFromPacket(long target) {
        if (updateMinThreshold(target)) {
            markForSave();
        }
    }

    public void setMaxThresholdFromPacket(long target) {
        if (updateMaxThreshold(target)) {
            markForSave();
        }
    }

    private boolean updateMinThreshold(long target) {
        long threshold = getThreshold(target);
        if (minThreshold != threshold) {
            minThreshold = threshold;
            //If the min threshold is greater than the max threshold, update max threshold
            if (minThreshold > maxThreshold) {
                maxThreshold = minThreshold;
            }
            return true;
        }
        return false;
    }

    private boolean updateMaxThreshold(long target) {
        //Cap threshold at max energy capacity
        long threshold = getThreshold(target);
        if (maxThreshold != threshold) {
            maxThreshold = threshold;
            //If the max threshold is smaller than the min threshold, update min threshold
            if (maxThreshold < minThreshold) {
                minThreshold = maxThreshold;
            }
            return true;
        }
        return false;
    }

    private long getThreshold(long target) {
        return Math.min(target, energyContainer.getMaxEnergy());
    }

    @Override
    public void readSustainedData(HolderLookup.Provider provider, @NotNull CompoundTag data) {
        super.readSustainedData(provider, data);
        NBTUtils.setLegacyEnergyIfPresent(data, SerializationConstants.MIN, this::updateMinThreshold);
        NBTUtils.setLegacyEnergyIfPresent(data, SerializationConstants.MAX, this::updateMaxThreshold);
        NBTUtils.setIntIfPresent(data, SerializationConstants.TIME, value -> delay = value);
        NBTUtils.setEnumIfPresent(data, SerializationConstants.OUTPUT_MODE, RedstoneOutput.BY_ID, mode -> outputMode = mode);
    }

    @Override
    public void writeSustainedData(HolderLookup.Provider provider, CompoundTag data) {
        super.writeSustainedData(provider, data);
        data.putLong(SerializationConstants.MIN, minThreshold);
        data.putLong(SerializationConstants.MAX, maxThreshold);
        data.putInt(SerializationConstants.TIME, delay);
        NBTUtils.writeEnum(data, SerializationConstants.OUTPUT_MODE, outputMode);
    }

    @Override
    protected void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        updateMinThreshold(input.getOrDefault(MekanismDataComponents.MIN_THRESHOLD, minThreshold));
        updateMaxThreshold(input.getOrDefault(MekanismDataComponents.MAX_THRESHOLD, maxThreshold));
        setDelay(input.getOrDefault(MekanismDataComponents.DELAY, delay));
        outputMode = input.getOrDefault(MekanismDataComponents.REDSTONE_OUTPUT, outputMode);
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MekanismDataComponents.MIN_THRESHOLD, minThreshold);
        builder.set(MekanismDataComponents.MAX_THRESHOLD, maxThreshold);
        builder.set(MekanismDataComponents.DELAY, delay);
        builder.set(MekanismDataComponents.REDSTONE_OUTPUT, outputMode);
    }

    @Override
    public boolean supportsMode(RedstoneControl mode) {
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
    public long getMinThreshold() {
        return minThreshold;
    }

    @ComputerMethod
    public long getMaxThreshold() {
        return maxThreshold;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableLong.create(this::getMinThreshold, value -> minThreshold = value));
        container.track(SyncableLong.create(this::getMaxThreshold, value -> maxThreshold = value));
        container.track(SyncableInt.create(this::getDelay, value -> delay = value));
        container.track(SyncableEnum.create(RedstoneOutput.BY_ID, RedstoneOutput.OFF, this::getOutputMode, value -> outputMode = value));
    }

    //Methods relating to IComputerTile
    @ComputerMethod(requiresPublicSecurity = true)
    void setRedstoneOutputMode(RedstoneOutput mode) throws ComputerException {
        validateSecurityIsPublic();
        if (outputMode != mode) {
            outputMode = mode;
            setChanged();
        }
    }

    @ComputerMethod(nameOverride = "setDelay", requiresPublicSecurity = true)
    void computerSetDelay(int delay) throws ComputerException {
        validateSecurityIsPublic();
        if (delay < 0) {
            throw new ComputerException("Delay cannot be negative. Received: %d", delay);
        }
        setDelay(delay);
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void setMinThreshold(long threshold) throws ComputerException {
        validateSecurityIsPublic();
        setMinThresholdFromPacket(threshold);
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void setMaxThreshold(long threshold) throws ComputerException {
        validateSecurityIsPublic();
        setMaxThresholdFromPacket(threshold);
    }
    //End methods IComputerTile

    @NothingNullByDefault
    public enum RedstoneOutput implements IIncrementalEnum<RedstoneOutput>, IHasEnumNameTranslationKey, StringRepresentable {
        OFF(MekanismLang.OFF),
        ENTITY_DETECTION(MekanismLang.ENTITY_DETECTION),
        ENERGY_CONTENTS(MekanismLang.ENERGY_CONTENTS);

        public static final Codec<RedstoneOutput> CODEC = StringRepresentable.fromEnum(RedstoneOutput::values);
        public static final IntFunction<RedstoneOutput> BY_ID = ByIdMap.continuous(RedstoneOutput::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, RedstoneOutput> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, RedstoneOutput::ordinal);

        private final String serializedName;
        private final ILangEntry langEntry;

        RedstoneOutput(ILangEntry langEntry) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.langEntry = langEntry;
        }

        @Override
        public String getTranslationKey() {
            return langEntry.getTranslationKey();
        }

        @Override
        public RedstoneOutput byIndex(int index) {
            return BY_ID.apply(index);
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
