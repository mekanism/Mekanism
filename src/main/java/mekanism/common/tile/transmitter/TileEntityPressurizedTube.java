package mekanism.common.tile.transmitter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.IGasHandler.IMekanismGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler.IMekanismInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler.IMekanismPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler.IMekanismSlurryHandler;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.transmitter.ChemicalTransmitterWrapper.GasTransmitterWrapper;
import mekanism.common.capabilities.chemical.transmitter.ChemicalTransmitterWrapper.InfusionTransmitterWrapper;
import mekanism.common.capabilities.chemical.transmitter.ChemicalTransmitterWrapper.PigmentTransmitterWrapper;
import mekanism.common.capabilities.chemical.transmitter.ChemicalTransmitterWrapper.SlurryTransmitterWrapper;
import mekanism.common.capabilities.proxy.ProxyChemicalHandler.ProxyGasHandler;
import mekanism.common.capabilities.proxy.ProxyChemicalHandler.ProxyInfusionHandler;
import mekanism.common.capabilities.proxy.ProxyChemicalHandler.ProxyPigmentHandler;
import mekanism.common.capabilities.proxy.ProxyChemicalHandler.ProxySlurryHandler;
import mekanism.common.capabilities.resolver.advanced.AdvancedCapabilityResolver;
import mekanism.common.content.network.BoxedChemicalNetwork;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.upgrade.transmitter.PressurizedTubeUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;

public class TileEntityPressurizedTube extends TileEntityTransmitter {

    public TileEntityPressurizedTube(IBlockProvider blockProvider) {
        super(blockProvider);
        BoxedPressurizedTube tube = getTransmitter();
        IMekanismGasHandler gasHandler = new GasTransmitterWrapper(tube, tube::getGasTanks);
        IMekanismInfusionHandler infusionHandler = new InfusionTransmitterWrapper(tube, tube::getInfusionTanks);
        IMekanismPigmentHandler pigmentHandler = new PigmentTransmitterWrapper(tube, tube::getPigmentTanks);
        IMekanismSlurryHandler slurryHandler = new SlurryTransmitterWrapper(tube, tube::getSlurryTanks);
        addCapabilityResolver(AdvancedCapabilityResolver.readOnly(Capabilities.GAS_HANDLER_CAPABILITY, gasHandler,
              () -> new ProxyGasHandler(gasHandler, null, null)));
        addCapabilityResolver(AdvancedCapabilityResolver.readOnly(Capabilities.INFUSION_HANDLER_CAPABILITY, infusionHandler,
              () -> new ProxyInfusionHandler(infusionHandler, null, null)));
        addCapabilityResolver(AdvancedCapabilityResolver.readOnly(Capabilities.PIGMENT_HANDLER_CAPABILITY, pigmentHandler,
              () -> new ProxyPigmentHandler(pigmentHandler, null, null)));
        addCapabilityResolver(AdvancedCapabilityResolver.readOnly(Capabilities.SLURRY_HANDLER_CAPABILITY, slurryHandler,
              () -> new ProxySlurryHandler(slurryHandler, null, null)));
    }

    @Override
    protected BoxedPressurizedTube createTransmitter(IBlockProvider blockProvider) {
        return new BoxedPressurizedTube(blockProvider, this);
    }

    @Override
    public BoxedPressurizedTube getTransmitter() {
        return (BoxedPressurizedTube) super.getTransmitter();
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
        return TransmitterType.PRESSURIZED_TUBE;
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
                return BlockStateHelper.copyStateData(current, MekanismBlocks.BASIC_PRESSURIZED_TUBE.getBlock().getDefaultState());
            case ADVANCED:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ADVANCED_PRESSURIZED_TUBE.getBlock().getDefaultState());
            case ELITE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ELITE_PRESSURIZED_TUBE.getBlock().getDefaultState());
            case ULTIMATE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE.getBlock().getDefaultState());
        }
        return current;
    }

    @Nullable
    @Override
    protected PressurizedTubeUpgradeData getUpgradeData() {
        BoxedPressurizedTube transmitter = getTransmitter();
        return new PressurizedTubeUpgradeData(transmitter.redstoneReactive, transmitter.connectionTypes, transmitter.getShare());
    }

    @Override
    protected void parseUpgradeData(@Nonnull TransmitterUpgradeData upgradeData) {
        if (upgradeData instanceof PressurizedTubeUpgradeData) {
            PressurizedTubeUpgradeData data = (PressurizedTubeUpgradeData) upgradeData;
            BoxedPressurizedTube transmitter = getTransmitter();
            transmitter.redstoneReactive = data.redstoneReactive;
            transmitter.connectionTypes = data.connectionTypes;
            transmitter.takeChemical(data.contents, Action.EXECUTE);
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
            BoxedChemicalNetwork network = getTransmitter().getTransmitterNetwork();
            updateTag.put(NBTConstants.BOXED_CHEMICAL, network.lastChemical.write(new CompoundNBT()));
            updateTag.putFloat(NBTConstants.SCALE, network.currentScale);
        }
        return updateTag;
    }
}