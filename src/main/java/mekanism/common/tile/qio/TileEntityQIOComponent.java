package mekanism.common.tile.qio;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Map;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.content.qio.IQIOFrequencyHolder;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityQIOComponent extends TileEntityMekanism implements IQIOFrequencyHolder, ISustainedData {

    @Nullable
    private EnumColor lastColor;

    public TileEntityQIOComponent(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        frequencyComponent.track(FrequencyType.QIO, true, true, true);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));
    }

    @Nullable
    public EnumColor getColor() {
        return lastColor;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        EnumColor prev = lastColor;
        QIOFrequency frequency = getQIOFrequency();
        lastColor = frequency == null ? null : frequency.getColor();
        if (prev != lastColor) {
            sendUpdatePacket();
        }
        if (level.getGameTime() % 10 == 0) {
            setActive(frequency != null);
        }
    }

    @Override
    public void writeSustainedData(CompoundTag dataMap) {
        if (lastColor != null) {
            NBTUtils.writeEnum(dataMap, NBTConstants.COLOR, lastColor);
        }
    }

    @Override
    public void readSustainedData(CompoundTag dataMap) {
        EnumColor color = dataMap.contains(NBTConstants.COLOR, Tag.TAG_INT) ? EnumColor.byIndexStatic(dataMap.getInt(NBTConstants.COLOR)) : null;
        if (lastColor != color) {
            lastColor = color;
        }
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put(NBTConstants.COLOR, NBTConstants.COLOR);
        return remap;
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag() {
        CompoundTag updateTag = super.getReducedUpdateTag();
        if (lastColor != null) {
            NBTUtils.writeEnum(updateTag, NBTConstants.COLOR, lastColor);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag) {
        super.handleUpdateTag(tag);
        EnumColor color = tag.contains(NBTConstants.COLOR, Tag.TAG_INT) ? EnumColor.byIndexStatic(tag.getInt(NBTConstants.COLOR)) : null;
        if (lastColor != color) {
            lastColor = color;
            WorldUtils.updateBlock(getLevel(), getBlockPos(), getBlockState());
        }
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private Collection<QIOFrequency> getFrequencies() {
        return FrequencyType.QIO.getManagerWrapper().getPublicManager().getFrequencies();
    }

    @ComputerMethod
    private boolean hasFrequency() {
        QIOFrequency frequency = getQIOFrequency();
        return frequency != null && frequency.isValid();
    }

    @ComputerMethod(nameOverride = "getFrequency")
    protected QIOFrequency computerGetFrequency() throws ComputerException {
        QIOFrequency frequency = getQIOFrequency();
        if (frequency == null || !frequency.isValid()) {
            throw new ComputerException("No frequency is currently selected.");
        }
        return frequency;
    }

    @ComputerMethod
    private void setFrequency(String name) throws ComputerException {
        validateSecurityIsPublic();
        QIOFrequency frequency = FrequencyType.QIO.getManagerWrapper().getPublicManager().getFrequency(name);
        if (frequency == null) {
            throw new ComputerException("No public QIO frequency with name '%s' found.", name);
        }
        setFrequency(FrequencyType.QIO, frequency.getIdentity(), getOwnerUUID());
    }

    @ComputerMethod
    private void createFrequency(String name) throws ComputerException {
        validateSecurityIsPublic();
        QIOFrequency frequency = FrequencyType.QIO.getManagerWrapper().getPublicManager().getFrequency(name);
        if (frequency != null) {
            throw new ComputerException("Unable to create public QIO frequency with name '%s' as one already exists.", name);
        }
        setFrequency(FrequencyType.QIO, new FrequencyIdentity(name, true), getOwnerUUID());
    }

    @ComputerMethod
    private EnumColor getFrequencyColor() throws ComputerException {
        return computerGetFrequency().getColor();
    }

    @ComputerMethod
    private void setFrequencyColor(EnumColor color) throws ComputerException {
        validateSecurityIsPublic();
        computerGetFrequency().setColor(color);
    }

    @ComputerMethod
    private void incrementFrequencyColor() throws ComputerException {
        validateSecurityIsPublic();
        QIOFrequency frequency = computerGetFrequency();
        frequency.setColor(frequency.getColor().getNext());
    }

    @ComputerMethod
    private void decrementFrequencyColor() throws ComputerException {
        validateSecurityIsPublic();
        QIOFrequency frequency = computerGetFrequency();
        frequency.setColor(frequency.getColor().getPrevious());
    }
    //End methods IComputerTile
}