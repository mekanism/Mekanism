package mekanism.common.content.network.transmitter.chemical;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.pigment.BasicPigmentTank;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentHandler.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.content.network.chemical.PigmentNetwork;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

public class PigmentPressurizedTube extends PressurizedTube<Pigment, PigmentStack, IPigmentHandler, IPigmentTank, PigmentNetwork, PigmentPressurizedTube>
      implements IMekanismPigmentHandler {

    public PigmentPressurizedTube(IBlockProvider blockProvider, TileEntityTransmitter tile) {
        super(blockProvider, tile);
    }

    @Override
    protected IPigmentTank initBuffer() {
        return BasicPigmentTank.create(getCapacity(), BasicPigmentTank.alwaysFalse, BasicPigmentTank.alwaysTrue, BasicPigmentTank.alwaysTrue, this);
    }

    @Override
    public String getStoredKey() {
        return NBTConstants.PIGMENT_STORED;
    }

    @Override
    protected PigmentStack readFromNBT(@Nullable CompoundNBT nbtTags) {
        return PigmentStack.readFromNBT(nbtTags);
    }

    @Override
    public PigmentNetwork createEmptyNetwork() {
        return new PigmentNetwork();
    }

    @Override
    public PigmentNetwork createEmptyNetworkWithID(UUID networkID) {
        return new PigmentNetwork(networkID);
    }

    @Override
    public PigmentNetwork createNetworkByMerging(Collection<PigmentNetwork> toMerge) {
        return new PigmentNetwork(toMerge);
    }

    @Override
    protected boolean isTubeSameType(PressurizedTube<?, ?, ?, ?, ?, ?> tube) {
        return tube instanceof PigmentPressurizedTube;
    }

    @Override
    protected void handleContentsUpdateTag(@Nonnull PigmentNetwork network, @Nonnull CompoundNBT tag) {
        super.handleContentsUpdateTag(network, tag);
        NBTUtils.setPigmentIfPresent(tag, getStoredKey(), network::setLastChemical);
    }
}