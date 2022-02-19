package mekanism.common.tile.qio;

import java.util.Collection;
import javax.annotation.Nonnull;
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
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityQIOComponent extends TileEntityMekanism implements IQIOFrequencyHolder {

    private EnumColor lastColor;

    public TileEntityQIOComponent(IBlockProvider blockProvider) {
        super(blockProvider);
        frequencyComponent.track(FrequencyType.QIO, true, true, true);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
    }

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

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        if (lastColor != null) {
            updateTag.putInt(NBTConstants.COLOR, lastColor.ordinal());
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        if (tag.contains(NBTConstants.COLOR, NBT.TAG_INT)) {
            lastColor = EnumColor.byIndexStatic(tag.getInt(NBTConstants.COLOR));
        } else {
            lastColor = null;
        }
        WorldUtils.updateBlock(getLevel(), getBlockPos(), getBlockState());
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private Collection<QIOFrequency> getFrequencies() {
        return FrequencyType.QIO.getManagerWrapper().getPublicManager().getFrequencies();
    }

    @ComputerMethod
    private boolean hasFrequency() {
        QIOFrequency frequency = getFrequency(FrequencyType.QIO);
        return frequency != null && frequency.isValid();
    }

    @ComputerMethod(nameOverride = "getFrequency")
    protected QIOFrequency computerGetFrequency() throws ComputerException {
        QIOFrequency frequency = getFrequency(FrequencyType.QIO);
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