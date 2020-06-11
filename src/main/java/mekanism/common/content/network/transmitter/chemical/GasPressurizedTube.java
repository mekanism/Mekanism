package mekanism.common.content.network.transmitter.chemical;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasHandler.IMekanismGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.content.network.chemical.GasNetwork;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

public class GasPressurizedTube extends PressurizedTube<Gas, GasStack, IGasHandler, IGasTank, GasNetwork, GasPressurizedTube> implements IMekanismGasHandler {

    public GasPressurizedTube(IBlockProvider blockProvider, TileEntityTransmitter tile) {
        super(blockProvider, tile);
    }

    @Override
    protected IGasTank initBuffer() {
        return BasicGasTank.create(getCapacity(), BasicGasTank.alwaysFalse, BasicGasTank.alwaysTrue, BasicGasTank.alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, this);
    }

    @Override
    public String getStoredKey() {
        return NBTConstants.GAS_STORED;
    }

    @Override
    protected GasStack readFromNBT(@Nullable CompoundNBT nbtTags) {
        return GasStack.readFromNBT(nbtTags);
    }

    @Override
    public GasNetwork createEmptyNetwork() {
        return new GasNetwork();
    }

    @Override
    public GasNetwork createEmptyNetworkWithID(UUID networkID) {
        return new GasNetwork(networkID);
    }

    @Override
    public GasNetwork createNetworkByMerging(Collection<GasNetwork> toMerge) {
        return new GasNetwork(toMerge);
    }

    @Override
    protected boolean isTubeSameType(PressurizedTube<?, ?, ?, ?, ?, ?> tube) {
        return tube instanceof GasPressurizedTube;
    }

    @Override
    protected void handleContentsUpdateTag(@Nonnull GasNetwork network, @Nonnull CompoundNBT tag) {
        super.handleContentsUpdateTag(network, tag);
        NBTUtils.setGasIfPresent(tag, getStoredKey(), network::setLastChemical);
    }
}