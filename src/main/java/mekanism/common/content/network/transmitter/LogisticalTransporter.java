package mekanism.common.content.network.transmitter;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;

public class LogisticalTransporter extends LogisticalTransporterBase {

    private EnumColor color;

    public LogisticalTransporter(IBlockProvider blockProvider, TileEntityTransmitter tile) {
        super(tile, Attribute.getTier(blockProvider.getBlock(), TransporterTier.class));
    }

    public TransporterTier getTier() {
        return tier;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    public void setColor(EnumColor c) {
        color = c;
    }

    @Override
    public ActionResultType onConfigure(PlayerEntity player, Direction side) {
        TransporterUtils.incrementColor(this);
        PathfinderCache.onChanged(getTransmitterNetwork());
        getTransmitterTile().sendUpdatePacket();
        EnumColor color = getColor();
        player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
              MekanismLang.TOGGLE_COLOR.translateColored(EnumColor.GRAY, color != null ? color.getColoredName() : MekanismLang.NONE)), Util.DUMMY_UUID);
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        EnumColor color = getColor();
        player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
              MekanismLang.CURRENT_COLOR.translateColored(EnumColor.GRAY, color != null ? color.getColoredName() : MekanismLang.NONE)), Util.DUMMY_UUID);
        return super.onRightClick(player, side);
    }

    @Override
    protected void readFromNBT(CompoundNBT nbtTags) {
        super.readFromNBT(nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.COLOR, TransporterUtils::readColor, this::setColor);
    }

    @Override
    public void writeToNBT(CompoundNBT nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.putInt(NBTConstants.COLOR, TransporterUtils.getColorIndex(getColor()));
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag(CompoundNBT updateTag) {
        updateTag = super.getReducedUpdateTag(updateTag);
        updateTag.putInt(NBTConstants.COLOR, TransporterUtils.getColorIndex(getColor()));
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setEnumIfPresent(tag, NBTConstants.COLOR, TransporterUtils::readColor, this::setColor);
    }
}