package mekanism.common.tile.qio;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.content.qio.IQIOFrequencyHolder;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;

public class TileEntityQIOComponent extends TileEntityMekanism implements IQIOFrequencyHolder {

    private EnumColor lastColor;

    public TileEntityQIOComponent(IBlockProvider blockProvider) {
        super(blockProvider);
        frequencyComponent.track(FrequencyType.QIO, true, true, true);
    }

    public EnumColor getColor() {
        return lastColor;
    }

    @Override
    public void onUpdateServer() {
        super.onUpdateServer();
        EnumColor prev = lastColor;
        lastColor = getQIOFrequency() != null ? getQIOFrequency().getColor() : null;
        if (prev != lastColor) {
            sendUpdatePacket();
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
        EnumColor prev = lastColor;
        if (tag.contains(NBTConstants.COLOR)) {
            lastColor = EnumColor.byIndexStatic(tag.getInt(NBTConstants.COLOR));
        } else {
            lastColor = null;
        }
        if (prev != lastColor) {
            MekanismUtils.updateBlock(getWorld(), getPos());
        }
    }
}
