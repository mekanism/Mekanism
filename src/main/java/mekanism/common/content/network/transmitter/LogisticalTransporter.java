package mekanism.common.content.network.transmitter;

import mekanism.api.SerializationConstants;
import mekanism.api.WrenchResult;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.LogisticalTransporterUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WrenchUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LogisticalTransporter extends LogisticalTransporterBase implements IUpgradeableTransmitter<LogisticalTransporterUpgradeData> {

    @Nullable
    private EnumColor color;

    public LogisticalTransporter(Holder<Block> blockProvider, TileEntityTransmitter tile) {
        super(tile, Attribute.getTier(blockProvider, TransporterTier.class));
    }

    @Override
    public TransporterTier getTier() {
        return tier;
    }

    @Nullable
    @Override
    public EnumColor getColor() {
        return color;
    }

    public void setColor(@Nullable EnumColor c) {
        color = c;
    }

    private WrenchResult onConfigureColor(ConfigureContext context) {
        if (!context.is(ConfigureAction.COLOR)) {
            return WrenchResult.PASS;
        }
        setColor(TransporterUtils.increment(getColor()));
        PathfinderCache.onChanged(getTransmitterNetwork());
        getTransmitterTile().sendUpdatePacket();
        EnumColor color = getColor();
        context.player().displayClientMessage(MekanismLang.TOGGLE_COLOR.translateColored(EnumColor.GRAY, color == null ? MekanismLang.NONE.translateColored(EnumColor.WHITE) : color.getColoredName()), true);
        return WrenchResult.CONFIGURED;
    }

    private WrenchResult onConfigureProbe(ConfigureContext context) {
        if (context.is(ConfigureAction.PROBE)) {
            EnumColor color = getColor();
            context.player().displayClientMessage(MekanismLang.CURRENT_COLOR.translateColored(EnumColor.GRAY, color == null ? MekanismLang.NONE.translateColored(EnumColor.WHITE) : color.getColoredName()), true);
        }
        return WrenchResult.PASS; //Defer to super::onConfigure
    }

    @Override
    public WrenchResult onConfigure(ConfigureContext context) {
        if (!WrenchUtils.checkType(context, TransmissionType.ITEM)) {
            return WrenchResult.PASS;
        }
        return context.chain(this::onConfigureColor, this::onConfigureProbe, super::onConfigure);
    }

    @Nullable
    @Override
    public LogisticalTransporterUpgradeData getUpgradeData() {
        return new LogisticalTransporterUpgradeData(redstoneReactive, getConnectionTypesRaw(), getColor(), transit, needsSync, nextId, delay, delayCount);
    }

    @Override
    public boolean dataTypeMatches(@NotNull TransmitterUpgradeData data) {
        return data instanceof LogisticalTransporterUpgradeData;
    }

    @Override
    public void parseUpgradeData(@NotNull LogisticalTransporterUpgradeData data) {
        redstoneReactive = data.redstoneReactive;
        setConnectionTypesRaw(data.connectionTypes);
        setColor(data.color);
        transit.putAll(data.transit);
        needsSync.putAll(data.needsSync);
        nextId = data.nextId;
        delay = data.delay;
        delayCount = data.delayCount;
    }

    @Override
    protected void readFromNBT(HolderLookup.Provider provider, CompoundTag nbtTags) {
        super.readFromNBT(provider, nbtTags);
        setColor(NBTUtils.getEnum(nbtTags, SerializationConstants.COLOR, EnumColor.BY_ID));
    }

    @Override
    public void writeToNBT(HolderLookup.Provider provider, CompoundTag nbtTags) {
        super.writeToNBT(provider, nbtTags);
        if (getColor() != null) {
            NBTUtils.writeEnum(nbtTags, SerializationConstants.COLOR, getColor());
        }
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider, CompoundTag updateTag) {
        updateTag = super.getReducedUpdateTag(provider, updateTag);
        if (getColor() != null) {
            NBTUtils.writeEnum(updateTag, SerializationConstants.COLOR, getColor());
        }
        return updateTag;
    }

    @Override
    public boolean handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        boolean refreshModelData = super.handleUpdateTag(tag, provider);
        EnumColor color = NBTUtils.getEnum(tag, SerializationConstants.COLOR, EnumColor.BY_ID);
        if (this.color != color) {
            setColor(color);
            //Color changed, mark the model data as needing to be refreshed
            refreshModelData = true;
        }
        return refreshModelData;
    }
}