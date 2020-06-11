package mekanism.common.tile.transmitter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.proxy.ProxyHeatHandler;
import mekanism.common.capabilities.resolver.advanced.AdvancedCapabilityResolver;
import mekanism.common.content.network.transmitter.ThermodynamicConductor;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.upgrade.transmitter.ThermodynamicConductorUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import net.minecraft.block.BlockState;

public class TileEntityThermodynamicConductor extends TileEntityTransmitter {

    public TileEntityThermodynamicConductor(IBlockProvider blockProvider) {
        super(blockProvider);
        IMekanismHeatHandler handler = getTransmitter();
        addCapabilityResolver(AdvancedCapabilityResolver.readOnly(Capabilities.HEAT_HANDLER_CAPABILITY, handler,
              () -> new ProxyHeatHandler(handler, null, null)));
    }

    @Override
    protected ThermodynamicConductor createTransmitter(IBlockProvider blockProvider) {
        return new ThermodynamicConductor(blockProvider, this);
    }

    @Override
    public ThermodynamicConductor getTransmitter() {
        return (ThermodynamicConductor) super.getTransmitter();
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.THERMODYNAMIC_CONDUCTOR;
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
                return BlockStateHelper.copyStateData(current, MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR.getBlock().getDefaultState());
            case ADVANCED:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR.getBlock().getDefaultState());
            case ELITE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR.getBlock().getDefaultState());
            case ULTIMATE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR.getBlock().getDefaultState());
        }
        return current;
    }

    @Nullable
    @Override
    protected ThermodynamicConductorUpgradeData getUpgradeData() {
        ThermodynamicConductor transmitter = getTransmitter();
        return new ThermodynamicConductorUpgradeData(transmitter.redstoneReactive, transmitter.connectionTypes, transmitter.buffer.getHeat());
    }

    @Override
    protected void parseUpgradeData(@Nonnull TransmitterUpgradeData upgradeData) {
        if (upgradeData instanceof ThermodynamicConductorUpgradeData) {
            ThermodynamicConductorUpgradeData data = (ThermodynamicConductorUpgradeData) upgradeData;
            ThermodynamicConductor transmitter = getTransmitter();
            transmitter.redstoneReactive = data.redstoneReactive;
            transmitter.connectionTypes = data.connectionTypes;
            transmitter.buffer.setHeat(data.heat);
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }
}