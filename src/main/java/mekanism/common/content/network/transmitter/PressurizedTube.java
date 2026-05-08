package mekanism.common.content.network.transmitter;

import com.google.common.primitives.Ints;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.network.ChemicalNetwork;
import mekanism.common.lib.transmitter.CompatibleTransmitterValidator;
import mekanism.common.lib.transmitter.CompatibleTransmitterValidator.CompatibleChemicalTransmitterValidator;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.AbstractAcceptorCache;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.PressurizedTubeUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PressurizedTube extends BufferedTransmitter<ResourceHandler<ChemicalResource>, ChemicalNetwork, ChemicalStack, PressurizedTube> implements IContentsListener,
      IUpgradeableTransmitter<PressurizedTubeUpgradeData> {

    public final TubeTier tier;
    public final IChemicalTank buffer;
    private final List<IChemicalTank> chemicalTanks;
    @NotNull
    public ChemicalStack saveShare = ChemicalStack.EMPTY;

    public PressurizedTube(Holder<Block> blockProvider, TileEntityTransmitter tile) {
        super(tile, TransmissionType.CHEMICAL);
        this.tier = Attribute.getTier(blockProvider, TubeTier.class);
        buffer = BasicChemicalTank.createAllValid(getCapacity(), this);
        chemicalTanks = Collections.singletonList(buffer);
    }

    @Override
    protected AbstractAcceptorCache<ResourceHandler<ChemicalResource>, ?> createAcceptorCache() {
        return new AcceptorCache<>(getTransmitterTile(), Capabilities.CHEMICAL.block());
    }

    @Override
    @SuppressWarnings("unchecked")
    public AcceptorCache<ResourceHandler<ChemicalResource>> getAcceptorCache() {
        return (AcceptorCache<ResourceHandler<ChemicalResource>>) super.getAcceptorCache();
    }

    @Override
    public TubeTier getTier() {
        return tier;
    }

    @Override
    public void pullFromAcceptors() {
        if (!hasPullSide || getAvailablePull() <= 0) {
            return;
        }
        AcceptorCache<ResourceHandler<ChemicalResource>> acceptorCache = getAcceptorCache();
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (!isConnectionType(side, ConnectionType.PULL)) {
                continue;
            }
            ResourceHandler<ChemicalResource> connectedAcceptor = acceptorCache.getConnectedAcceptor(side);
            if (connectedAcceptor != null) {
                //Note: We recheck the buffer each time in case we ended up accepting chemical somewhere
                // and our buffer changed and is no longer empty
                ChemicalStack bufferWithFallback = getBufferWithFallback();
                pullFromAcceptor(connectedAcceptor, bufferWithFallback, bufferWithFallback.isEmpty());
            }
        }
    }

    /**
     * @param connectedAcceptor  The acceptor
     * @param bufferWithFallback The buffer of the network
     * @param bufferIsEmpty      {@code true} if the buffer is empty, {@code false} otherwise
     *
     * @return {@code true} if we successfully pulled a chemical, {@code false} if we were unable to pull a chemical.
     */
    private boolean pullFromAcceptor(ResourceHandler<ChemicalResource> connectedAcceptor, ChemicalStack bufferWithFallback, boolean bufferIsEmpty) {
        if (connectedAcceptor == null) {
            return false;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            ChemicalResource receivedType;
            if (bufferIsEmpty) {
                //If we don't have a chemical stored try pulling as much as we are able to
                receivedType = ResourceHandlerUtil.findExtractableResource(connectedAcceptor, ConstantPredicates.alwaysTrue(), transaction);
            } else {
                //Otherwise, try draining the same type of chemical we have stored requesting up to as much as we are able to pull
                // We do this to better support multiple tanks in case the chemical we have stored we could pull out of a block's
                // second tank but just asking to drain a specific amount
                receivedType = ChemicalResource.of(bufferWithFallback);
            }
            if (receivedType == null || receivedType.isEmpty()) {
                return false;
            }
            //TODO - 26.1: Make available pull use ints natively
            int extracted = connectedAcceptor.extract(receivedType, Ints.saturatedCast(getAvailablePull()), transaction);
            int inserted = getChemicalTank().insert(receivedType, extracted, transaction, AutomationType.INTERNAL);
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

    private long getAvailablePull() {
        if (hasTransmitterNetwork()) {
            return Math.min(tier.getTubePullAmount(), getTransmitterNetwork().chemicalTank.getNeededAsLong());
        }
        return Math.min(tier.getTubePullAmount(), buffer.getNeededAsLong());
    }

    @Nullable
    @Override
    public PressurizedTubeUpgradeData getUpgradeData() {
        return new PressurizedTubeUpgradeData(redstoneReactive, getConnectionTypesRaw(), getShare());
    }

    @Override
    public boolean dataTypeMatches(@NotNull TransmitterUpgradeData data) {
        return data instanceof PressurizedTubeUpgradeData;
    }

    @Override
    public void parseUpgradeData(@NotNull PressurizedTubeUpgradeData data) {
        redstoneReactive = data.redstoneReactive;
        setConnectionTypesRaw(data.connectionTypes);
        getChemicalTank().insert(data.contents, Action.EXECUTE, AutomationType.INTERNAL);
    }

    @Override
    public void read(@NotNull ValueInput input) {
        super.read(input);
        saveShare = input.read(SerializationConstants.BOXED_CHEMICAL, ChemicalStack.CODEC).orElse(ChemicalStack.EMPTY);
        buffer.setStack(saveShare);
    }

    @Override
    public void write(@NotNull ValueOutput output) {
        super.write(output);
        if (hasTransmitterNetwork()) {
            getTransmitterNetwork().validateSaveShares(this);
        }
        if (saveShare.isEmpty()) {
            output.discard(SerializationConstants.BOXED_CHEMICAL);
        } else {
            output.store(SerializationConstants.BOXED_CHEMICAL, ChemicalStack.CODEC, saveShare);
        }
    }

    @Override
    public ChemicalNetwork createEmptyNetworkWithID(UUID networkID) {
        return new ChemicalNetwork(networkID);
    }

    @Override
    public ChemicalNetwork createNetworkByMerging(Collection<ChemicalNetwork> toMerge) {
        return new ChemicalNetwork(toMerge);
    }

    @Override
    public CompatibleTransmitterValidator<ResourceHandler<ChemicalResource>, ChemicalNetwork, PressurizedTube> getNewOrphanValidator() {
        return new CompatibleChemicalTransmitterValidator(this);
    }

    @Override
    public boolean isValidTransmitter(TileEntityTransmitter transmitter, Direction side) {
        if (super.isValidTransmitter(transmitter, side) && transmitter.getTransmitter() instanceof PressurizedTube other) {
            ChemicalResource buffer = getBufferOrFallback(this);
            if (buffer.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
                return true;
            }
            ChemicalResource otherBuffer = getBufferOrFallback(other);
            return otherBuffer.isEmpty() || buffer.equals(otherBuffer);
        }
        return false;
    }

    private static ChemicalResource getBufferOrFallback(PressurizedTube tube) {
        ChemicalResource buffer = ChemicalResource.of(tube.getBufferWithFallback());
        if (buffer.isEmpty() && tube.hasTransmitterNetwork() && tube.getTransmitterNetwork().getPrevTransferAmount() > 0) {
            return tube.getTransmitterNetwork().lastChemical;
        }
        return buffer;
    }

    @Override
    protected boolean canHaveIncompatibleNetworks() {
        return true;
    }

    @Override
    public long getCapacity() {
        return tier.getTubeCapacity();
    }

    @NotNull
    @Override
    public ChemicalStack releaseShare() {
        if (buffer.isEmpty()) {
            return ChemicalStack.EMPTY;
        }
        ChemicalStack ret = buffer.getStack();
        buffer.setEmpty();
        return ret;
    }

    @NotNull
    @Override
    public ChemicalStack getShare() {
        return buffer.getStack();
    }

    @Override
    public boolean noBufferOrFallback() {
        return getBufferWithFallback().isEmpty();
    }

    @NotNull
    @Override
    public ChemicalStack getBufferWithFallback() {
        ChemicalStack buffer = getShare();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer.isEmpty() && hasTransmitterNetwork()) {
            return getTransmitterNetwork().getBuffer();
        }
        return buffer;
    }

    @Override
    public void takeShare() {
        if (hasTransmitterNetwork()) {
            ChemicalNetwork transmitterNetwork = getTransmitterNetwork();
            if (!transmitterNetwork.chemicalTank.isEmpty() && !saveShare.isEmpty()) {
                ChemicalStack chemicalStack = saveShare;
                long amount = chemicalStack.amount();
                MekanismUtils.logMismatchedStackSize(transmitterNetwork.chemicalTank.shrinkStack(amount, Action.EXECUTE), amount);
                buffer.setStack(chemicalStack);
            }
        }
    }

    @NotNull
    public List<IChemicalTank> getChemicalTanks() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getChemicalTanks();
        }
        return chemicalTanks;
    }

    @Override
    public void onContentsChanged() {
        getTransmitterTile().setChanged();
    }

    @Override
    protected void handleContentsUpdateTag(@NotNull ChemicalNetwork network, @NotNull ValueInput input) {
        super.handleContentsUpdateTag(network, input);
        network.currentScale = input.getFloatOr(SerializationConstants.SCALE, network.currentScale);
        network.setLastChemical(input.read(SerializationConstants.CHEMICAL, ChemicalResource.CODEC).orElse(ChemicalResource.EMPTY));
    }

    public IChemicalTank getChemicalTank() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().chemicalTank : buffer;
    }

}