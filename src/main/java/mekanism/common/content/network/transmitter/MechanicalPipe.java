package mekanism.common.content.network.transmitter;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.UUID;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.lib.transmitter.CompatibleTransmitterValidator;
import mekanism.common.lib.transmitter.CompatibleTransmitterValidator.CompatibleFluidTransmitterValidator;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.tier.PipeTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.ResourceTransmitterUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class MechanicalPipe extends BufferedResourceTransmitter<FluidResource, IFluidTank, FluidNetwork, MechanicalPipe> {

    public final PipeTier tier;

    public MechanicalPipe(Holder<Block> blockProvider, TileEntityTransmitter tile) {
        this.tier = Attribute.getTierNN(blockProvider, PipeTier.class);
        super(tile, BasicFluidTank::create, TransmissionType.FLUID);
    }

    @Override
    protected AcceptorCache<ResourceHandler<FluidResource>> createAcceptorCache() {
        return new AcceptorCache<>(getTransmitterTile(), Capabilities.FLUID.block());
    }

    @Override
    protected Codec<FluidResource> resourceCodec() {
        return FluidResource.CODEC;
    }

    @Override
    public PipeTier getTier() {
        return tier;
    }

    @Override
    public boolean dataTypeMatches(TransmitterUpgradeData data) {
        return data instanceof ResourceTransmitterUpgradeData<?> upgradeData && upgradeData.buffer.stackHelper() == LargeResourceStack.FLUID_HELPER;
    }

    @Override
    public CompatibleTransmitterValidator<ResourceHandler<FluidResource>, FluidNetwork, MechanicalPipe> getNewOrphanValidator() {
        return new CompatibleFluidTransmitterValidator(this);
    }

    @Override
    public boolean isValidTransmitter(TileEntityTransmitter transmitter, Direction side) {
        return super.isValidTransmitter(transmitter, side) && transmitter.getTransmitter() instanceof MechanicalPipe other && isValidTransmitter(other);
    }

    @Override
    public FluidNetwork createEmptyNetworkWithID(UUID networkID) {
        return new FluidNetwork(networkID);
    }

    @Override
    public FluidNetwork createNetworkByMerging(Collection<FluidNetwork> networks) {
        return new FluidNetwork(networks);
    }
}