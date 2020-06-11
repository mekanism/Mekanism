package mekanism.common.content.network.transmitter.chemical;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.slurry.BasicSlurryTank;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryHandler.IMekanismSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.content.network.chemical.SlurryNetwork;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

public class SlurryPressurizedTube extends PressurizedTube<Slurry, SlurryStack, ISlurryHandler, ISlurryTank, SlurryNetwork, SlurryPressurizedTube>
      implements IMekanismSlurryHandler {

    public SlurryPressurizedTube(IBlockProvider blockProvider, TileEntityTransmitter tile) {
        super(blockProvider, tile);
    }

    @Override
    protected ISlurryTank initBuffer() {
        return BasicSlurryTank.create(getCapacity(), BasicSlurryTank.alwaysFalse, BasicSlurryTank.alwaysTrue, BasicSlurryTank.alwaysTrue, this);
    }

    @Override
    public String getStoredKey() {
        return NBTConstants.SLURRY_STORED;
    }

    @Override
    protected SlurryStack readFromNBT(@Nullable CompoundNBT nbtTags) {
        return SlurryStack.readFromNBT(nbtTags);
    }

    @Override
    public SlurryNetwork createEmptyNetwork() {
        return new SlurryNetwork();
    }

    @Override
    public SlurryNetwork createEmptyNetworkWithID(UUID networkID) {
        return new SlurryNetwork(networkID);
    }

    @Override
    public SlurryNetwork createNetworkByMerging(Collection<SlurryNetwork> toMerge) {
        return new SlurryNetwork(toMerge);
    }

    @Override
    protected boolean isTubeSameType(PressurizedTube<?, ?, ?, ?, ?, ?> tube) {
        return tube instanceof SlurryPressurizedTube;
    }

    @Override
    protected void handleContentsUpdateTag(@Nonnull SlurryNetwork network, @Nonnull CompoundNBT tag) {
        super.handleContentsUpdateTag(network, tag);
        NBTUtils.setSlurryIfPresent(tag, getStoredKey(), network::setLastChemical);
    }
}