package mekanism.common.tile.laser;

import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
import mekanism.api.NBTConstants;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.base.ITileNetwork;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;

public class TileEntityLaserAmplifier extends TileEntityLaserReceptor implements ITileNetwork {

    public double minThreshold;
    public double maxThreshold = 5E9;
    public int ticks = 0;
    public int time = 0;
    public boolean emittingRedstone;
    public RedstoneOutput outputMode = RedstoneOutput.OFF;

    public TileEntityLaserAmplifier() {
        super(MekanismBlocks.LASER_AMPLIFIER);
    }

    @Override
    protected void onUpdateServer() {
        boolean prevRedstone = emittingRedstone;
        emittingRedstone = false;
        if (ticks < time) {
            ticks++;
        } else {
            ticks = 0;
        }
        super.onUpdateServer();
        if (outputMode != RedstoneOutput.ENTITY_DETECTION) {
            emittingRedstone = false;
        }
        if (emittingRedstone != prevRedstone) {
            world.notifyNeighborsOfStateChange(getPos(), getBlockType());
        }
    }

    @Override
    protected void setEmittingRedstone(boolean foundEntity) {
        emittingRedstone = foundEntity;
    }

    private boolean shouldFire() {
        return ticks >= time && getEnergy() >= minThreshold && MekanismUtils.canFunction(this);
    }

    @Override
    protected double toFire() {
        return shouldFire() ? Math.min(getEnergy(), maxThreshold) : 0;
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
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            switch (dataStream.readInt()) {
                case 0:
                    minThreshold = Math.min(getMaxEnergy(), MekanismUtils.convertToJoules(dataStream.readDouble()));
                    break;
                case 1:
                    maxThreshold = Math.min(getMaxEnergy(), MekanismUtils.convertToJoules(dataStream.readDouble()));
                    break;
                case 2:
                    time = dataStream.readInt();
                    break;
                case 3:
                    outputMode = outputMode.getNext();
                    break;
            }
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        minThreshold = nbtTags.getDouble(NBTConstants.MIN);
        maxThreshold = nbtTags.getDouble(NBTConstants.MAX);
        time = nbtTags.getInt(NBTConstants.TIME);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.OUTPUT_MODE, RedstoneOutput::byIndexStatic, mode -> outputMode = mode);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putDouble(NBTConstants.MIN, minThreshold);
        nbtTags.putDouble(NBTConstants.MAX, maxThreshold);
        nbtTags.putInt(NBTConstants.TIME, time);
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
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
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