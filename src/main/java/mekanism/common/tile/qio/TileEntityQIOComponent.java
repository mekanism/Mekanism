package mekanism.common.tile.qio;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.content.qio.IQIOFrequencyHolder;
import mekanism.common.content.qio.QIOFrequency;
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
        WorldUtils.updateBlock(getWorld(), getPos());
    }
}
