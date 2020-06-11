package mekanism.common.content.network.transmitter.chemical;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.infuse.BasicInfusionTank;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionHandler.IMekanismInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.content.network.chemical.InfusionNetwork;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

public class InfusionPressurizedTube extends PressurizedTube<InfuseType, InfusionStack, IInfusionHandler, IInfusionTank, InfusionNetwork, InfusionPressurizedTube>
      implements IMekanismInfusionHandler {

    public InfusionPressurizedTube(IBlockProvider blockProvider, TileEntityTransmitter tile) {
        super(blockProvider, tile);
    }

    @Override
    protected IInfusionTank initBuffer() {
        return BasicInfusionTank.create(getCapacity(), BasicInfusionTank.alwaysFalse, BasicInfusionTank.alwaysTrue, BasicInfusionTank.alwaysTrue, this);
    }

    @Override
    public String getStoredKey() {
        return NBTConstants.INFUSE_TYPE_STORED;
    }

    @Override
    protected InfusionStack readFromNBT(@Nullable CompoundNBT nbtTags) {
        return InfusionStack.readFromNBT(nbtTags);
    }

    @Override
    public InfusionNetwork createEmptyNetwork() {
        return new InfusionNetwork();
    }

    @Override
    public InfusionNetwork createEmptyNetworkWithID(UUID networkID) {
        return new InfusionNetwork(networkID);
    }

    @Override
    public InfusionNetwork createNetworkByMerging(Collection<InfusionNetwork> toMerge) {
        return new InfusionNetwork(toMerge);
    }

    @Override
    protected boolean isTubeSameType(PressurizedTube<?, ?, ?, ?, ?, ?> tube) {
        return tube instanceof InfusionPressurizedTube;
    }

    @Override
    protected void handleContentsUpdateTag(@Nonnull InfusionNetwork network, @Nonnull CompoundNBT tag) {
        super.handleContentsUpdateTag(network, tag);
        NBTUtils.setInfuseTypeIfPresent(tag, getStoredKey(), network::setLastChemical);
    }
}