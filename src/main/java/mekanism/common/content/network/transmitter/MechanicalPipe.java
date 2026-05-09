package mekanism.common.content.network.transmitter;

import com.google.common.primitives.Ints;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.lib.transmitter.CompatibleTransmitterValidator;
import mekanism.common.lib.transmitter.CompatibleTransmitterValidator.CompatibleFluidTransmitterValidator;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.AbstractAcceptorCache;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.tier.PipeTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.MechanicalPipeUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MechanicalPipe extends BufferedTransmitter<ResourceHandler<FluidResource>, FluidNetwork, FluidStack, MechanicalPipe> implements IContentsListener,
      IUpgradeableTransmitter<MechanicalPipeUpgradeData> {

    public final PipeTier tier;
    @NotNull
    public FluidStack saveShare = FluidStack.EMPTY;
    private final List<IFluidTank> tanks;
    public final BasicFluidTank buffer;

    public MechanicalPipe(Holder<Block> blockProvider, TileEntityTransmitter tile) {
        super(tile, TransmissionType.FLUID);
        this.tier = Attribute.getTier(blockProvider, PipeTier.class);
        //TODO - 26.1: If we make fluids support longs then adjust this
        buffer = BasicFluidTank.create(Ints.saturatedCast(getCapacity()), ConstantPredicates.alwaysFalse(), ConstantPredicates.alwaysTrue(), this);
        tanks = Collections.singletonList(buffer);
    }

    @Override
    protected AbstractAcceptorCache<ResourceHandler<FluidResource>, ?> createAcceptorCache() {
        return new AcceptorCache<>(getTransmitterTile(), Capabilities.FLUID.block());
    }

    @Override
    @SuppressWarnings("unchecked")
    public AcceptorCache<ResourceHandler<FluidResource>> getAcceptorCache() {
        return (AcceptorCache<ResourceHandler<FluidResource>>) super.getAcceptorCache();
    }

    @Override
    public PipeTier getTier() {
        return tier;
    }

    @Override
    public void pullFromAcceptors() {
        if (!hasPullSide || getAvailablePull() <= 0) {
            return;
        }
        AcceptorCache<ResourceHandler<FluidResource>> acceptorCache = getAcceptorCache();
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (!isConnectionType(side, ConnectionType.PULL)) {
                continue;
            }
            ResourceHandler<FluidResource> connectedAcceptor = acceptorCache.getConnectedAcceptor(side);
            if (connectedAcceptor != null) {
                //Note: We recheck the buffer each time in case we ended up accepting chemical somewhere
                // and our buffer changed and is no longer empty
                FluidStack bufferWithFallback = getBufferWithFallback();
                pullFromAcceptor(connectedAcceptor, bufferWithFallback, bufferWithFallback.isEmpty());
            }
        }
    }

    private boolean pullFromAcceptor(ResourceHandler<FluidResource> connectedAcceptor, FluidStack bufferWithFallback, boolean bufferIsEmpty) {
        if (connectedAcceptor == null) {
            return false;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            FluidResource receivedType;
            if (bufferIsEmpty) {
                //If we don't have a chemical stored try pulling as much as we are able to
                receivedType = ResourceHandlerUtil.findExtractableResource(connectedAcceptor, ConstantPredicates.alwaysTrue(), transaction);
            } else {
                //Otherwise, try draining the same type of chemical we have stored requesting up to as much as we are able to pull
                // We do this to better support multiple tanks in case the chemical we have stored we could pull out of a block's
                // second tank but just asking to drain a specific amount
                receivedType = FluidResource.of(bufferWithFallback);
            }
            if (receivedType == null || receivedType.isEmpty()) {
                return false;
            }
            int extracted = connectedAcceptor.extract(receivedType, getAvailablePull(), transaction);
            int inserted = getFluidTank().insert(receivedType, extracted, transaction, AutomationType.INTERNAL);
            if (inserted < extracted) {
                return false;
            }
            //If we received some chemical and are able to insert it all, then actually extract it and insert it into our thing.
            // Note: We extract first after simulating ourselves because if the target gave a faulty simulation value, we want to handle it properly
            // and not accidentally dupe anything, and we know our simulation we just performed on taking it is valid
            transaction.commit();
            return true;
        }
    }

    private int getAvailablePull() {
        if (hasTransmitterNetwork()) {
            return Math.min(tier.getPipePullAmount(), getTransmitterNetwork().fluidTank.getNeeded());
        }
        return Math.min(tier.getPipePullAmount(), buffer.getNeeded());
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
        getFluidTank().insert(data.contents, Action.EXECUTE, AutomationType.INTERNAL);
    }

    @Override
    public void read(@NotNull ValueInput input) {
        super.read(input);
        saveShare = input.read(SerializationConstants.FLUID, FluidStack.CODEC).orElse(FluidStack.EMPTY);
        buffer.setStack(saveShare);
    }

    @Override
    public void write(@NotNull ValueOutput output) {
        super.write(output);
        if (hasTransmitterNetwork()) {
            getTransmitterNetwork().validateSaveShares(this);
        }
        if (saveShare.isEmpty()) {
            output.discard(SerializationConstants.FLUID);
        } else {
            output.store(SerializationConstants.FLUID, FluidStack.CODEC, saveShare);
        }
    }

    @Override
    public CompatibleTransmitterValidator<ResourceHandler<FluidResource>, FluidNetwork, MechanicalPipe> getNewOrphanValidator() {
        return new CompatibleFluidTransmitterValidator(this);
    }

    @Override
    public boolean isValidTransmitter(TileEntityTransmitter transmitter, Direction side) {
        if (super.isValidTransmitter(transmitter, side) && transmitter.getTransmitter() instanceof MechanicalPipe other) {
            FluidStack buffer = getBufferWithFallback();
            if (buffer.isEmpty() && hasTransmitterNetwork() && getTransmitterNetwork().getPrevTransferAmount() > 0) {
                buffer = getTransmitterNetwork().lastFluid;
            }
            FluidStack otherBuffer = other.getBufferWithFallback();
            if (otherBuffer.isEmpty() && other.hasTransmitterNetwork() && other.getTransmitterNetwork().getPrevTransferAmount() > 0) {
                otherBuffer = other.getTransmitterNetwork().lastFluid;
            }
            return buffer.isEmpty() || otherBuffer.isEmpty() || FluidStack.isSameFluidSameComponents(buffer, otherBuffer);
        }
        return false;
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
    protected boolean canHaveIncompatibleNetworks() {
        return true;
    }

    @Override
    public long getCapacity() {
        return tier.getPipeCapacity();
    }

    @NotNull
    @Override
    public FluidStack releaseShare() {
        FluidStack ret = buffer.getFluid();
        buffer.setEmpty();
        return ret;
    }

    @Override
    public boolean noBufferOrFallback() {
        return getBufferWithFallback().isEmpty();
    }

    @NotNull
    @Override
    public FluidStack getBufferWithFallback() {
        FluidStack buffer = getShare();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer.isEmpty() && hasTransmitterNetwork()) {
            return getTransmitterNetwork().getBuffer();
        }
        return buffer;
    }

    @NotNull
    @Override
    public FluidStack getShare() {
        return buffer.getFluid();
    }

    @Override
    public void takeShare() {
        if (hasTransmitterNetwork()) {
            FluidNetwork network = getTransmitterNetwork();
            if (!network.fluidTank.isEmpty() && !saveShare.isEmpty()) {
                int amount = saveShare.amount();
                MekanismUtils.logMismatchedStackSize(network.fluidTank.shrinkStack(amount, Action.EXECUTE), amount);
                buffer.setStack(saveShare);
            }
        }
    }

    @NotNull
    public List<IFluidTank> getFluidTanks() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getFluidTanks();
        }
        return tanks;
    }

    @Override
    public void onContentsChanged() {
        getTransmitterTile().setChanged();
    }

    @Override
    protected void handleContentsUpdateTag(@NotNull FluidNetwork network, @NotNull ValueInput input) {
        super.handleContentsUpdateTag(network, input);
        network.setLastFluid(input.read(SerializationConstants.FLUID, FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY));
        network.currentScale = input.getFloatOr(SerializationConstants.SCALE, network.currentScale);
    }

    private IFluidTank getFluidTank() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().fluidTank : buffer;
    }
}