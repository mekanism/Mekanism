package mekanism.common.tile.transmitter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.TransporterTier;
import mekanism.common.upgrade.transmitter.LogisticalTransporterUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;

public class TileEntityLogisticalTransporter extends TileEntityLogisticalTransporterBase {

    private EnumColor color;

    public TileEntityLogisticalTransporter(IBlockProvider blockProvider) {
        super(blockProvider, Attribute.getTier(blockProvider.getBlock(), TransporterTier.class));
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.LOGISTICAL_TRANSPORTER;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    public void setColor(EnumColor c) {
        color = c;
    }

    @Override
    protected ActionResultType onConfigure(PlayerEntity player, Direction side) {
        TransporterUtils.incrementColor(this);
        PathfinderCache.onChanged(getTransmitterNetwork());
        sendUpdatePacket();
        EnumColor color = getColor();
        player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
              MekanismLang.TOGGLE_COLOR.translateColored(EnumColor.GRAY, color != null ? color.getColoredName() : MekanismLang.NONE)));
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        EnumColor color = getColor();
        player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
              MekanismLang.CURRENT_COLOR.translateColored(EnumColor.GRAY, color != null ? color.getColoredName() : MekanismLang.NONE)));
        return super.onRightClick(player, side);
    }

    @Override
    protected void updateModelData(TransmitterModelData modelData) {
        super.updateModelData(modelData);
        modelData.setHasColor(getColor() != null);
    }

    @Override
    protected boolean canUpgrade(AlloyTier alloyTier) {
        return alloyTier.getBaseTier().ordinal() == tier.getBaseTier().ordinal() + 1;
    }

    @Nonnull
    @Override
    protected BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
        switch (tier) {
            case BASIC:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().getDefaultState());
            case ADVANCED:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER.getBlock().getDefaultState());
            case ELITE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER.getBlock().getDefaultState());
            case ULTIMATE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER.getBlock().getDefaultState());
        }
        return current;
    }

    @Nullable
    @Override
    protected LogisticalTransporterUpgradeData getUpgradeData() {
        return new LogisticalTransporterUpgradeData(redstoneReactive, connectionTypes, getColor(), writeStackToNBT());
    }

    @Override
    protected void parseUpgradeData(@Nonnull TransmitterUpgradeData upgradeData) {
        if (upgradeData instanceof LogisticalTransporterUpgradeData) {
            LogisticalTransporterUpgradeData data = (LogisticalTransporterUpgradeData) upgradeData;
            redstoneReactive = data.redstoneReactive;
            connectionTypes = data.connectionTypes;
            color = data.color;
            readStacksFromNBT(data.stacks);
        } else {
            super.parseUpgradeData(upgradeData);
        }
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
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.putInt(NBTConstants.COLOR, TransporterUtils.getColorIndex(getColor()));
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setEnumIfPresent(tag, NBTConstants.COLOR, TransporterUtils::readColor, this::setColor);
    }
}