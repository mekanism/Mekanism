package mekanism.common.tile.qio;

import java.util.Collection;
import mekanism.api.SerializationConstants;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.common.content.qio.IQIOFrequencyHolder;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.ValueUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class TileEntityQIOComponent extends TileEntityMekanism implements IQIOFrequencyHolder {

    @Nullable
    private EnumColor lastColor;

    public TileEntityQIOComponent(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        frequencyComponent.track(FrequencyTypes.QIO, true, true, true);
    }

    @Nullable
    public EnumColor getColor() {
        return lastColor;
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        sendUpdatePacket |= onUpdateServer(level, getQIOFrequency());
        return sendUpdatePacket;
    }

    protected boolean onUpdateServer(ServerLevel level, @Nullable QIOFrequency frequency) {
        EnumColor prev = lastColor;
        lastColor = frequency == null ? null : frequency.getColor();
        boolean needsUpdate = prev != lastColor;
        if (level.getGameTime() % MekanismUtils.TICKS_PER_HALF_SECOND == 0) {
            setActive(frequency != null);
        }
        return needsUpdate;
    }

    @Override
    public void writeSustainedData(ValueOutput output) {
        super.writeSustainedData(output);
        if (lastColor != null) {
            ValueUtils.writeEnum(output, SerializationConstants.COLOR, lastColor);
        }
    }

    @Override
    public void readSustainedData(ValueInput input) {
        super.readSustainedData(input);
        lastColor = ValueUtils.getEnum(input, SerializationConstants.COLOR, EnumColor.BY_ID);
    }

    @Override
    public void writeReducedUpdatedTag(ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        if (lastColor != null) {
            ValueUtils.writeEnum(output, SerializationConstants.COLOR, lastColor);
        }
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        super.handleUpdateTag(input);
        EnumColor color = ValueUtils.getEnum(input, SerializationConstants.COLOR, EnumColor.BY_ID);
        if (lastColor != color) {
            lastColor = color;
            WorldUtils.updateBlock(level, getBlockPos(), getBlockState());
        }
    }

    //Methods relating to IComputerTile
    @ComputerMethod(methodDescription = "Lists public frequencies")
    Collection<QIOFrequency> getFrequencies() {
        return FrequencyTypes.QIO.getController().getPublicLookup().getFrequencies();
    }

    @ComputerMethod
    boolean hasFrequency() {
        QIOFrequency frequency = getQIOFrequency();
        return frequency != null && frequency.isValid() && !frequency.isRemoved();
    }

    @ComputerMethod(nameOverride = "getFrequency", methodDescription = "Requires a frequency to be selected")
    QIOFrequency computerGetFrequency() throws ComputerException {
        QIOFrequency frequency = getQIOFrequency();
        if (frequency == null || !frequency.isValid() || frequency.isRemoved()) {
            throw new ComputerException("No frequency is currently selected.");
        }
        return frequency;
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires a public frequency to exist")
    void setFrequency(String name) throws ComputerException {
        validateSecurityIsPublic();
        QIOFrequency frequency = FrequencyTypes.QIO.getController().getPublicLookup().getFrequency(name);
        if (frequency == null) {
            throw new ComputerException("No public QIO frequency with name '%s' found.", name);
        }
        setFrequency(FrequencyTypes.QIO, frequency.getIdentity(), getOwnerUUID());
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires frequency to not already exist and for it to be public so that it can make it as the player who owns the block. Also sets the frequency after creation")
    void createFrequency(String name) throws ComputerException {
        validateSecurityIsPublic();
        QIOFrequency frequency = FrequencyTypes.QIO.getController().getPublicLookup().getFrequency(name);
        if (frequency != null) {
            throw new ComputerException("Unable to create public QIO frequency with name '%s' as one already exists.", name);
        }
        setFrequency(FrequencyTypes.QIO, new FrequencyIdentity(name, SecurityMode.PUBLIC, getOwnerUUID()), getOwnerUUID());
    }

    @ComputerMethod(methodDescription = "Requires a frequency to be selected")
    EnumColor getFrequencyColor() throws ComputerException {
        return computerGetFrequency().getColor();
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires a frequency to be selected")
    void setFrequencyColor(EnumColor color) throws ComputerException {
        validateSecurityIsPublic();
        computerGetFrequency().setColor(color);
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires a frequency to be selected")
    void incrementFrequencyColor() throws ComputerException {
        validateSecurityIsPublic();
        QIOFrequency frequency = computerGetFrequency();
        frequency.setColor(frequency.getColor().getNext());
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires a frequency to be selected")
    void decrementFrequencyColor() throws ComputerException {
        validateSecurityIsPublic();
        QIOFrequency frequency = computerGetFrequency();
        frequency.setColor(frequency.getColor().getPrevious());
    }

    //End methods IComputerTile
}
