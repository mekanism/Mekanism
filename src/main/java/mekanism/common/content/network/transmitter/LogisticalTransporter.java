package mekanism.common.content.network.transmitter;

import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.content.network.InventoryNetwork;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.LogisticalTransporterUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.ValueUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class LogisticalTransporter extends LogisticalTransporterBase implements IUpgradeableTransmitter<LogisticalTransporterUpgradeData> {

    @Nullable
    private EnumColor color;

    public LogisticalTransporter(Holder<Block> blockProvider, TileEntityTransmitter tile) {
        super(tile, Attribute.getTierNN(blockProvider, TransporterTier.class));
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

    @Override
    public InteractionResult onConfigure(Player player, Direction side) {
        setColor(TransporterUtils.increment(getColor()));
        InventoryNetwork network = getTransmitterNetwork();
        if (network != null) {//Should not be null, but double check
            PathfinderCache.onChanged(network);
        }
        TileEntityTransmitter tile = getTransmitterTile();
        tile.markForSave();
        notifyTileChange();
        tile.sendUpdatePacket();
        EnumColor color = getColor();
        player.sendOverlayMessage(MekanismLang.TOGGLE_COLOR.translateColored(EnumColor.GRAY, color == null ? MekanismLang.NONE.translateColored(EnumColor.WHITE) : color.getColoredName()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onRightClick(Level level, Player player, Direction side) {
        EnumColor color = getColor();
        player.sendOverlayMessage(MekanismLang.CURRENT_COLOR.translateColored(EnumColor.GRAY, color == null ? MekanismLang.NONE.translateColored(EnumColor.WHITE) : color.getColoredName()));
        return super.onRightClick(level, player, side);
    }

    @Override
    public LogisticalTransporterUpgradeData getUpgradeData() {
        return new LogisticalTransporterUpgradeData(redstoneReactive, getConnectionTypesRaw(), getColor(), transit, needsSync, nextId, delay, delayCount);
    }

    @Override
    public boolean dataTypeMatches(TransmitterUpgradeData data) {
        return data instanceof LogisticalTransporterUpgradeData;
    }

    @Override
    public void parseUpgradeData(LogisticalTransporterUpgradeData data, TransactionContext transaction) {
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
    public void read(ValueInput input) {
        super.read(input);
        setColor(ValueUtils.getEnum(input, SerializationConstants.COLOR, EnumColor.BY_ID));
    }

    @Override
    public void write(ValueOutput output) {
        super.write(output);
        if (getColor() != null) {
            ValueUtils.writeEnum(output, SerializationConstants.COLOR, getColor());
        }
    }

    @Override
    public void writeReducedUpdatedTag(ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        if (getColor() != null) {
            ValueUtils.writeEnum(output, SerializationConstants.COLOR, getColor());
        }
    }

    @Override
    public boolean handleUpdateTag(ValueInput input) {
        boolean refreshModelData = super.handleUpdateTag(input);
        EnumColor color = ValueUtils.getEnum(input, SerializationConstants.COLOR, EnumColor.BY_ID);
        if (this.color != color) {
            setColor(color);
            //Color changed, mark the model data as needing to be refreshed
            refreshModelData = true;
        }
        return refreshModelData;
    }
}