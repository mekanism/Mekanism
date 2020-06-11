package mekanism.common.tile.transmitter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.proxy.ProxyFluidHandler;
import mekanism.common.capabilities.resolver.advanced.AdvancedCapabilityResolver;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.upgrade.transmitter.MechanicalPipeUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntityMechanicalPipe extends TileEntityTransmitter {

    public TileEntityMechanicalPipe(IBlockProvider blockProvider) {
        super(blockProvider);
        IMekanismFluidHandler handler = getTransmitter();
        addCapabilityResolver(AdvancedCapabilityResolver.readOnly(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, handler,
              () -> new ProxyFluidHandler(handler, null, null)));
    }

    @Override
    protected MechanicalPipe createTransmitter(IBlockProvider blockProvider) {
        return new MechanicalPipe(blockProvider, this);
    }

    @Override
    public MechanicalPipe getTransmitter() {
        return (MechanicalPipe) super.getTransmitter();
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
        return TransmitterType.MECHANICAL_PIPE;
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
                return BlockStateHelper.copyStateData(current, MekanismBlocks.BASIC_MECHANICAL_PIPE.getBlock().getDefaultState());
            case ADVANCED:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ADVANCED_MECHANICAL_PIPE.getBlock().getDefaultState());
            case ELITE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ELITE_MECHANICAL_PIPE.getBlock().getDefaultState());
            case ULTIMATE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ULTIMATE_MECHANICAL_PIPE.getBlock().getDefaultState());
        }
        return current;
    }

    @Nullable
    @Override
    protected MechanicalPipeUpgradeData getUpgradeData() {
        MechanicalPipe transmitter = getTransmitter();
        return new MechanicalPipeUpgradeData(transmitter.redstoneReactive, transmitter.connectionTypes, transmitter.getShare());
    }

    @Override
    protected void parseUpgradeData(@Nonnull TransmitterUpgradeData upgradeData) {
        if (upgradeData instanceof MechanicalPipeUpgradeData) {
            MechanicalPipeUpgradeData data = (MechanicalPipeUpgradeData) upgradeData;
            MechanicalPipe transmitter = getTransmitter();
            transmitter.redstoneReactive = data.redstoneReactive;
            transmitter.connectionTypes = data.connectionTypes;
            transmitter.takeFluid(data.contents, Action.EXECUTE);
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
            FluidNetwork network = getTransmitter().getTransmitterNetwork();
            updateTag.put(NBTConstants.FLUID_STORED, network.lastFluid.writeToNBT(new CompoundNBT()));
            updateTag.putFloat(NBTConstants.SCALE, network.currentScale);
        }
        return updateTag;
    }
}