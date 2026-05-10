package mekanism.common.content.network.transmitter;

import java.util.Collection;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.lib.transmitter.CompatibleTransmitterValidator;
import mekanism.common.lib.transmitter.CompatibleTransmitterValidator.CompatibleFluidTransmitterValidator;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.AbstractAcceptorCache;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.tier.PipeTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.MechanicalPipeUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MechanicalPipe extends BufferedResourceTransmitter<FluidResource, IFluidTank, FluidNetwork, MechanicalPipe>
      implements IUpgradeableTransmitter<MechanicalPipeUpgradeData> {

    public final PipeTier tier;

    public MechanicalPipe(Holder<Block> blockProvider, TileEntityTransmitter tile) {
        this.tier = Attribute.getTier(blockProvider, PipeTier.class);
        super(tile, SerializerHelper.FLUID_RESOURCE_STACK_CODEC,
              (capacity, listener) -> BasicFluidTank.create(capacity, ConstantPredicates.alwaysFalse(), ConstantPredicates.alwaysTrue(), listener), TransmissionType.FLUID);
    }

    @Override
    protected AbstractAcceptorCache<ResourceHandler<FluidResource>, ?> createAcceptorCache() {
        return new AcceptorCache<>(getTransmitterTile(), Capabilities.FLUID.block());
    }

    @Override
    public FluidResource getEmptyResource() {
        return FluidResource.EMPTY;
    }

    @Override
    public PipeTier getTier() {
        return tier;
    }

    @Override
    protected int getAvailablePull() {
        return Math.min(tier.getPipePullAmount(), getContainer().getNeeded());
    }

    @Nullable
    @Override
    public MechanicalPipeUpgradeData getUpgradeData() {
        return new MechanicalPipeUpgradeData(redstoneReactive, getConnectionTypesRaw(), getShare());
    }

    @Override
    public boolean dataTypeMatches(@NotNull TransmitterUpgradeData data) {
        return data instanceof MechanicalPipeUpgradeData;
    }

    @Override
    public void parseUpgradeData(@NotNull MechanicalPipeUpgradeData data) {
        redstoneReactive = data.redstoneReactive;
        setConnectionTypesRaw(data.connectionTypes);
        getContainer().setContentsUnchecked(data.contents.resource(), data.contents.amount());
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

    @Override
    public long getCapacity() {
        return tier.getPipeCapacity();
    }

    @Override
    protected void handleContentsUpdateTag(@NotNull FluidNetwork network, @NotNull ValueInput input) {
        super.handleContentsUpdateTag(network, input);
        network.setLastType(input.read(SerializationConstants.FLUID, FluidResource.CODEC).orElse(FluidResource.EMPTY));
        network.currentScale = input.getFloatOr(SerializationConstants.SCALE, network.currentScale);
    }
}