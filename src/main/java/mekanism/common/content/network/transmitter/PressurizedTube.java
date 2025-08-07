package mekanism.common.content.network.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.IChemicalTracker;
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
import mekanism.common.util.NBTUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PressurizedTube extends BufferedTransmitter<IChemicalHandler, ChemicalNetwork, ChemicalStack, PressurizedTube> implements IChemicalTracker,
      IUpgradeableTransmitter<PressurizedTubeUpgradeData> {

    public final TubeTier tier;
    public final IChemicalTank chemicalTank;
    private final List<IChemicalTank> chemicalTanks;
    @NotNull
    public ChemicalStack saveShare = ChemicalStack.EMPTY;

    public PressurizedTube(Holder<Block> blockProvider, TileEntityTransmitter tile) {
        super(tile, TransmissionType.CHEMICAL);
        this.tier = Attribute.getTier(blockProvider, TubeTier.class);
        chemicalTank = BasicChemicalTank.createAllValid(getCapacity(), this);
        chemicalTanks = Collections.singletonList(chemicalTank);
    }

    @Override
    protected AbstractAcceptorCache<IChemicalHandler, ?> createAcceptorCache() {
        return new AcceptorCache<>(getTransmitterTile(), Capabilities.CHEMICAL.block());
    }

    @Override
    @SuppressWarnings("unchecked")
    public AcceptorCache<IChemicalHandler> getAcceptorCache() {
        return (AcceptorCache<IChemicalHandler>) super.getAcceptorCache();
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
        AcceptorCache<IChemicalHandler> acceptorCache = getAcceptorCache();
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (!isConnectionType(side, ConnectionType.PULL)) {
                continue;
            }
            IChemicalHandler connectedAcceptor = acceptorCache.getConnectedAcceptor(side);
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
    private boolean pullFromAcceptor(IChemicalHandler connectedAcceptor, ChemicalStack bufferWithFallback, boolean bufferIsEmpty) {
        if (connectedAcceptor == null) {
            return false;
        }
        long availablePull = getAvailablePull();
        ChemicalStack received;
        if (bufferIsEmpty) {
            //If we don't have a chemical stored try pulling as much as we are able to
            received = connectedAcceptor.extractChemical(availablePull, Action.SIMULATE);
        } else {
            //Otherwise, try draining the same type of chemical we have stored requesting up to as much as we are able to pull
            // We do this to better support multiple tanks in case the chemical we have stored we could pull out of a block's
            // second tank but just asking to drain a specific amount
            received = connectedAcceptor.extractChemical(bufferWithFallback.copyWithAmount(availablePull), Action.SIMULATE);
        }
        if (!received.isEmpty() && takeChemical(received, Action.SIMULATE).isEmpty()) {
            //If we received some chemical and are able to insert it all, then actually extract it and insert it into our thing.
            // Note: We extract first after simulating ourselves because if the target gave a faulty simulation value, we want to handle it properly
            // and not accidentally dupe anything, and we know our simulation we just performed on taking it is valid
            takeChemical(connectedAcceptor.extractChemical(received, Action.EXECUTE), Action.EXECUTE);
            return true;
        }
        return false;
    }

    private long getAvailablePull() {
        if (hasTransmitterNetwork()) {
            return Math.min(tier.getTubePullAmount(), getTransmitterNetwork().chemicalTank.getNeeded());
        }
        return Math.min(tier.getTubePullAmount(), chemicalTank.getNeeded());
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
        takeChemical(data.contents, Action.EXECUTE);
    }

    @Override
    public void read(HolderLookup.Provider provider, @NotNull CompoundTag nbtTags) {
        super.read(provider, nbtTags);
        if (nbtTags.contains(SerializationConstants.BOXED_CHEMICAL, Tag.TAG_COMPOUND)) {
            saveShare = ChemicalStack.parseOptional(provider, nbtTags.getCompound(SerializationConstants.BOXED_CHEMICAL));
        } else {
            saveShare = ChemicalStack.EMPTY;
        }
        setStackClearOthers(saveShare, chemicalTank);
    }

    private void setStackClearOthers(ChemicalStack stack, IChemicalTank tank) {
        tank.setStack(stack);
    }

    @NotNull
    @Override
    public CompoundTag write(HolderLookup.Provider provider, @NotNull CompoundTag nbtTags) {
        super.write(provider, nbtTags);
        if (hasTransmitterNetwork()) {
            getTransmitterNetwork().validateSaveShares(this);
        }
        if (saveShare.isEmpty()) {
            nbtTags.remove(SerializationConstants.BOXED_CHEMICAL);
        } else {
            nbtTags.put(SerializationConstants.BOXED_CHEMICAL, saveShare.save(provider));
        }
        return nbtTags;
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
    public CompatibleTransmitterValidator<IChemicalHandler, ChemicalNetwork, PressurizedTube> getNewOrphanValidator() {
        return new CompatibleChemicalTransmitterValidator(this);
    }

    @Override
    public boolean isValidTransmitter(TileEntityTransmitter transmitter, Direction side) {
        if (super.isValidTransmitter(transmitter, side) && transmitter.getTransmitter() instanceof PressurizedTube other) {
            Holder<Chemical> buffer = getBufferOrFallback(this);
            if (buffer.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
                return true;
            }
            Holder<Chemical> otherBuffer = getBufferOrFallback(other);
            return otherBuffer.is(MekanismAPI.EMPTY_CHEMICAL_KEY) || buffer.is(otherBuffer);
        }
        return false;
    }

    private static Holder<Chemical> getBufferOrFallback(PressurizedTube tube) {
        Holder<Chemical> buffer = tube.getBufferWithFallback().getChemicalHolder();
        if (buffer.is(MekanismAPI.EMPTY_CHEMICAL_KEY) && tube.hasTransmitterNetwork() && tube.getTransmitterNetwork().getPrevTransferAmount() > 0) {
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
        if (chemicalTank.isEmpty()) {
            return ChemicalStack.EMPTY;
        }
        ChemicalStack ret = chemicalTank.getStack();
        chemicalTank.setEmpty();
        return ret;
    }

    @NotNull
    @Override
    public ChemicalStack getShare() {
        return chemicalTank.getStack();
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
                long amount = chemicalStack.getAmount();
                MekanismUtils.logMismatchedStackSize(transmitterNetwork.chemicalTank.shrinkStack(amount, Action.EXECUTE), amount);
                setStackClearOthers(chemicalStack, chemicalTank);
            }
        }
    }

    private ChemicalStack takeChemical(ChemicalStack stack, Action action) {
        IChemicalTank tank;
        if (hasTransmitterNetwork()) {
            tank = getTransmitterNetwork().chemicalTank;
        } else {
            tank = chemicalTank;
        }
        return tank.insert(stack, action, AutomationType.INTERNAL);
    }

    @NotNull
    @Override
    public List<IChemicalTank> getChemicalTanks(@Nullable Direction side) {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getChemicalTanks(side);
        }
        return chemicalTanks;
    }

    @Override
    public void onContentsChanged() {
        getTransmitterTile().setChanged();
    }

    @Override
    protected void handleContentsUpdateTag(@NotNull ChemicalNetwork network, @NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleContentsUpdateTag(network, tag, provider);
        NBTUtils.setFloatIfPresent(tag, SerializationConstants.SCALE, scale -> network.currentScale = scale);
        if (tag.contains(SerializationConstants.CHEMICAL, Tag.TAG_STRING)) {
            network.setLastChemical(Chemical.parseOptionalHolder(provider, tag.getString(SerializationConstants.CHEMICAL)));
        } else {
            network.setLastChemical(MekanismAPI.EMPTY_CHEMICAL_HOLDER);
        }
    }

    public IChemicalTank getChemicalTank() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().chemicalTank : chemicalTank;
    }

}