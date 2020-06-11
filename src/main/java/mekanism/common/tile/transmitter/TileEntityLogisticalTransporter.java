package mekanism.common.tile.transmitter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.content.network.transmitter.LogisticalTransporter;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.upgrade.transmitter.LogisticalTransporterUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import net.minecraft.block.BlockState;

public class TileEntityLogisticalTransporter extends TileEntityLogisticalTransporterBase {

    public TileEntityLogisticalTransporter(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected LogisticalTransporter createTransmitter(IBlockProvider blockProvider) {
        return new LogisticalTransporter(blockProvider, this);
    }

    @Override
    public LogisticalTransporter getTransmitter() {
        return (LogisticalTransporter) super.getTransmitter();
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.LOGISTICAL_TRANSPORTER;
    }

    @Override
    protected void updateModelData(TransmitterModelData modelData) {
        super.updateModelData(modelData);
        modelData.setHasColor(getTransmitter().getColor() != null);
    }

    @Override
    protected boolean canUpgrade(AlloyTier alloyTier) {
        return alloyTier.getBaseTier().ordinal() == getTransmitter().getTier().getBaseTier().ordinal() + 1;
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
        LogisticalTransporter transmitter = getTransmitter();
        return new LogisticalTransporterUpgradeData(transmitter.redstoneReactive, transmitter.connectionTypes, transmitter.getColor(), transmitter.writeStackToNBT());
    }

    @Override
    protected void parseUpgradeData(@Nonnull TransmitterUpgradeData upgradeData) {
        if (upgradeData instanceof LogisticalTransporterUpgradeData) {
            LogisticalTransporterUpgradeData data = (LogisticalTransporterUpgradeData) upgradeData;
            LogisticalTransporter transmitter = getTransmitter();
            transmitter.redstoneReactive = data.redstoneReactive;
            transmitter.connectionTypes = data.connectionTypes;
            transmitter.setColor(data.color);
            transmitter.readStacksFromNBT(data.stacks);
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }
}