package mekanism.common.tile.transmitter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.resolver.advanced.AdvancedEnergyCapabilityResolver;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.upgrade.transmitter.UniversalCableUpgradeData;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;

public class TileEntityUniversalCable extends TileEntityTransmitter {

    public TileEntityUniversalCable(IBlockProvider blockProvider) {
        super(blockProvider);
        addCapabilityResolver(new AdvancedEnergyCapabilityResolver(getTransmitter()));
    }

    @Override
    protected UniversalCable createTransmitter(IBlockProvider blockProvider) {
        return new UniversalCable(blockProvider, this);
    }

    @Override
    public UniversalCable getTransmitter() {
        return (UniversalCable) super.getTransmitter();
    }

    @Override
    public void tick() {
        if (!isRemote()) {
            getTransmitter().pullFromAcceptors();
        }
        super.tick();
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.UNIVERSAL_CABLE;
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
                return BlockStateHelper.copyStateData(current, MekanismBlocks.BASIC_UNIVERSAL_CABLE.getBlock().getDefaultState());
            case ADVANCED:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ADVANCED_UNIVERSAL_CABLE.getBlock().getDefaultState());
            case ELITE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ELITE_UNIVERSAL_CABLE.getBlock().getDefaultState());
            case ULTIMATE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE.getBlock().getDefaultState());
        }
        return current;
    }

    @Nullable
    @Override
    protected UniversalCableUpgradeData getUpgradeData() {
        UniversalCable transmitter = getTransmitter();
        return new UniversalCableUpgradeData(transmitter.redstoneReactive, transmitter.connectionTypes, transmitter.buffer);
    }

    @Override
    protected void parseUpgradeData(@Nonnull TransmitterUpgradeData upgradeData) {
        if (upgradeData instanceof UniversalCableUpgradeData) {
            UniversalCableUpgradeData data = (UniversalCableUpgradeData) upgradeData;
            UniversalCable transmitter = getTransmitter();
            transmitter.redstoneReactive = data.redstoneReactive;
            transmitter.connectionTypes = data.connectionTypes;
            transmitter.buffer.setEnergy(data.buffer.getEnergy());
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        //Note: We add the stored information to the initial update tag and not to the one we sync on side changes which uses getReducedUpdateTag
        CompoundNBT updateTag = super.getUpdateTag();
        if (getTransmitter().hasTransmitterNetwork()) {
            EnergyNetwork network = getTransmitter().getTransmitterNetwork();
            updateTag.putString(NBTConstants.ENERGY_STORED, network.energyContainer.getEnergy().toString());
            updateTag.putFloat(NBTConstants.SCALE, network.currentScale);
        }
        return updateTag;
    }
}