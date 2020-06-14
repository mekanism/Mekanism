package mekanism.common.content.network.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.merged.BoxedChemical;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.merged.MergedChemicalTank.Current;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.inventory.AutomationType;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.chemical.BoxedChemicalHandler;
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.content.network.BoxedChemicalNetwork;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.BoxedChemicalAcceptorCache;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;

public class BoxedPressurizedTube extends BufferedTransmitter<BoxedChemicalHandler, BoxedChemicalNetwork, BoxedChemicalStack, BoxedPressurizedTube>
      implements IGasTracker, IInfusionTracker, IPigmentTracker, ISlurryTracker {

    public final TubeTier tier;
    public final MergedChemicalTank chemicalTank;
    private final List<IGasTank> gasTanks;
    private final List<IInfusionTank> infusionTanks;
    private final List<IPigmentTank> pigmentTanks;
    private final List<ISlurryTank> slurryTanks;
    @Nonnull
    public BoxedChemicalStack saveShare = BoxedChemicalStack.EMPTY;

    public BoxedPressurizedTube(IBlockProvider blockProvider, TileEntityTransmitter tile) {
        super(tile, TransmissionType.GAS, TransmissionType.INFUSION, TransmissionType.PIGMENT, TransmissionType.SLURRY);
        this.tier = Attribute.getTier(blockProvider.getBlock(), TubeTier.class);
        chemicalTank = MergedChemicalTank.create(
              ChemicalTankBuilder.GAS.createAllValid(getCapacity(), this),
              ChemicalTankBuilder.INFUSION.createAllValid(getCapacity(), this),
              ChemicalTankBuilder.PIGMENT.createAllValid(getCapacity(), this),
              ChemicalTankBuilder.SLURRY.createAllValid(getCapacity(), this)
        );
        gasTanks = Collections.singletonList(chemicalTank.getGasTank());
        infusionTanks = Collections.singletonList(chemicalTank.getInfusionTank());
        pigmentTanks = Collections.singletonList(chemicalTank.getPigmentTank());
        slurryTanks = Collections.singletonList(chemicalTank.getSlurryTank());
    }

    @Override
    protected BoxedChemicalAcceptorCache createAcceptorCache() {
        return new BoxedChemicalAcceptorCache(this, getTransmitterTile());
    }

    @Override
    public BoxedChemicalAcceptorCache getAcceptorCache() {
        return (BoxedChemicalAcceptorCache) super.getAcceptorCache();
    }

    public TubeTier getTier() {
        return tier;
    }

    @Override
    public void pullFromAcceptors() {
        Set<Direction> connections = getConnections(ConnectionType.PULL);
        if (!connections.isEmpty()) {
            for (BoxedChemicalHandler connectedAcceptor : getAcceptorCache().getConnectedAcceptors(connections)) {
                //Note: We recheck the buffer each time in case we ended up accepting chemical somewhere
                // and our buffer changed and is no longer empty
                BoxedChemicalStack bufferWithFallback = getBufferWithFallback();
                if (bufferWithFallback.isEmpty()) {
                    //If the buffer is empty we need to try against each chemical type
                    for (ChemicalType chemicalType : EnumUtils.CHEMICAL_TYPES) {
                        if (pullFromAcceptor(connectedAcceptor, chemicalType, bufferWithFallback, true)) {
                            //If we successfully pulled into this tube, don't bother checking the other chemical types
                            break;
                        }
                    }
                } else {
                    pullFromAcceptor(connectedAcceptor, bufferWithFallback.getChemicalType(), bufferWithFallback, false);
                }
            }
        }
    }

    private boolean pullFromAcceptor(BoxedChemicalHandler acceptor, ChemicalType chemicalType, BoxedChemicalStack bufferWithFallback, boolean bufferIsEmpty) {
        IChemicalHandler<?, ?> handler = acceptor.getHandlerFor(chemicalType);
        if (handler != null) {
            return pullFromAcceptor(handler, bufferWithFallback, chemicalType, bufferIsEmpty);
        }
        return false;
    }

    /**
     * @param connectedAcceptor  The acceptor
     * @param bufferWithFallback The buffer of the network
     * @param chemicalType       The chemical type of the buffer
     * @param bufferIsEmpty      {@code true} if the buffer is empty, {@code false} otherwise
     *
     * @return {@code true} if we successfully pulled a chemical, {@code false} if we were unable to pull a chemical.
     */
    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>>
    boolean pullFromAcceptor(HANDLER connectedAcceptor, BoxedChemicalStack bufferWithFallback, ChemicalType chemicalType, boolean bufferIsEmpty) {
        long availablePull = getAvailablePull(chemicalType);
        STACK received;
        if (bufferIsEmpty) {
            //If we don't have a chemical stored try pulling as much as we are able to
            received = connectedAcceptor.extractChemical(availablePull, Action.SIMULATE);
        } else {
            //Otherwise try draining the same type of chemical we have stored requesting up to as much as we are able to pull
            // We do this to better support multiple tanks in case the chemical we have stored we could pull out of a block's
            // second tank but just asking to drain a specific amount
            received = connectedAcceptor.extractChemical(ChemicalUtil.copyWithAmount((STACK) bufferWithFallback.getChemicalStack(), availablePull), Action.SIMULATE);
        }
        if (!received.isEmpty() && takeChemical(chemicalType, received, Action.SIMULATE).isEmpty()) {
            //If we received some chemical and are able to insert it all
            STACK remainder = takeChemical(chemicalType, received, Action.EXECUTE);
            connectedAcceptor.extractChemical(ChemicalUtil.copyWithAmount(received, received.getAmount() - remainder.getAmount()), Action.EXECUTE);
            return true;
        }
        return false;
    }

    private long getAvailablePull(ChemicalType chemicalType) {
        if (hasTransmitterNetwork()) {
            return Math.min(tier.getTubePullAmount(), getTransmitterNetwork().chemicalTank.getTankForType(chemicalType).getNeeded());
        }
        return Math.min(tier.getTubePullAmount(), chemicalTank.getTankForType(chemicalType).getNeeded());
    }

    @Override
    public void read(@Nonnull CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (nbtTags.contains(NBTConstants.BOXED_CHEMICAL, NBT.TAG_COMPOUND)) {
            saveShare = BoxedChemicalStack.read(nbtTags.getCompound(NBTConstants.BOXED_CHEMICAL));
        } else {
            saveShare = BoxedChemicalStack.EMPTY;
        }
        setStackClearOthers(saveShare.getChemicalStack(), chemicalTank.getTankForType(saveShare.getChemicalType()));
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void setStackClearOthers(STACK stack, IChemicalTank<?, ?> tank) {
        ((IChemicalTank<CHEMICAL, STACK>) tank).setStack(stack);
        for (IChemicalTank<?, ?> tankToClear : chemicalTank.getAllTanks()) {
            if (tank != tankToClear) {
                tankToClear.setEmpty();
            }
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (hasTransmitterNetwork()) {
            getTransmitterNetwork().validateSaveShares(getTransmitter());
        }
        if (saveShare.isEmpty()) {
            nbtTags.remove(NBTConstants.BOXED_CHEMICAL);
        } else {
            nbtTags.put(NBTConstants.BOXED_CHEMICAL, saveShare.write(new CompoundNBT()));
        }
        return nbtTags;
    }

    @Override
    public boolean isValidAcceptor(TileEntity tile, Direction side) {
        return super.isValidAcceptor(tile, side) && getAcceptorCache().isChemicalAcceptorAndListen(tile, side);
    }

    @Override
    public BoxedChemicalNetwork createEmptyNetwork() {
        return new BoxedChemicalNetwork();
    }

    @Override
    public BoxedChemicalNetwork createEmptyNetworkWithID(UUID networkID) {
        return new BoxedChemicalNetwork(networkID);
    }

    @Override
    public BoxedChemicalNetwork createNetworkByMerging(Collection<BoxedChemicalNetwork> toMerge) {
        return new BoxedChemicalNetwork(toMerge);
    }

    @Override
    public boolean isValidTransmitter(Transmitter<?, ?, ?> transmitter) {
        if (super.isValidTransmitter(transmitter) && transmitter instanceof BoxedPressurizedTube) {
            BoxedChemical buffer = getBufferWithFallback().getType();
            if (buffer.isEmpty() && hasTransmitterNetwork() && getTransmitterNetwork().getPrevTransferAmount() > 0) {
                buffer = getTransmitterNetwork().lastChemical;
            }
            BoxedPressurizedTube other = (BoxedPressurizedTube) transmitter;
            BoxedChemical otherBuffer = other.getBufferWithFallback().getType();
            if (otherBuffer.isEmpty() && other.hasTransmitterNetwork() && other.getTransmitterNetwork().getPrevTransferAmount() > 0) {
                otherBuffer = other.getTransmitterNetwork().lastChemical;
            }
            return buffer.isEmpty() || otherBuffer.isEmpty() || buffer.equals(otherBuffer);
        }
        return false;
    }

    @Override
    protected boolean canHaveIncompatibleNetworks() {
        return true;
    }

    @Override
    public long getCapacity() {
        return tier.getTubeCapacity();
    }

    @Nonnull
    @Override
    public BoxedChemicalStack releaseShare() {
        BoxedChemicalStack ret;
        Current current = chemicalTank.getCurrent();
        if (current == Current.EMPTY) {
            ret = BoxedChemicalStack.EMPTY;
        } else {
            IChemicalTank<?, ?> tank = chemicalTank.getTankFromCurrent(current);
            ret = BoxedChemicalStack.box(tank.getStack());
            tank.setEmpty();
        }
        return ret;
    }

    @Nonnull
    @Override
    public BoxedChemicalStack getShare() {
        Current current = chemicalTank.getCurrent();
        if (current == Current.EMPTY) {
            return BoxedChemicalStack.EMPTY;
        }
        return BoxedChemicalStack.box(chemicalTank.getTankFromCurrent(current).getStack());
    }

    @Override
    public boolean noBufferOrFallback() {
        return getBufferWithFallback().isEmpty();
    }

    @Nonnull
    @Override
    public BoxedChemicalStack getBufferWithFallback() {
        BoxedChemicalStack buffer = getShare();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer.isEmpty() && hasTransmitterNetwork()) {
            return getTransmitterNetwork().getBuffer();
        }
        return buffer;
    }

    @Override
    public void takeShare() {
        if (hasTransmitterNetwork()) {
            BoxedChemicalNetwork transmitterNetwork = getTransmitterNetwork();
            Current networkCurrent = transmitterNetwork.chemicalTank.getCurrent();
            if (networkCurrent != Current.EMPTY && !saveShare.isEmpty()) {
                ChemicalStack<?> chemicalStack = saveShare.getChemicalStack();
                long amount = chemicalStack.getAmount();
                MekanismUtils.logMismatchedStackSize(transmitterNetwork.chemicalTank.getTankFromCurrent(networkCurrent).shrinkStack(amount, Action.EXECUTE), amount);
                setStackClearOthers(chemicalStack, chemicalTank.getTankFromCurrent(networkCurrent));
            }
        }
    }

    public void takeChemical(BoxedChemicalStack stack, Action action) {
        takeChemical(stack.getChemicalType(), stack.getChemicalStack(), action);
    }

    /**
     * @return remainder
     */
    @Nonnull
    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK takeChemical(ChemicalType type, STACK stack, Action action) {
        IChemicalTank<CHEMICAL, STACK> tank;
        if (hasTransmitterNetwork()) {
            tank = (IChemicalTank<CHEMICAL, STACK>) getTransmitterNetwork().chemicalTank.getTankForType(type);
        } else {
            tank = (IChemicalTank<CHEMICAL, STACK>) chemicalTank.getTankForType(type);
        }
        return tank.insert(stack, action, AutomationType.INTERNAL);
    }

    @Nonnull
    @Override
    public List<IGasTank> getGasTanks(@Nullable Direction side) {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getGasTanks(side);
        }
        return gasTanks;
    }

    @Nonnull
    @Override
    public List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getInfusionTanks(side);
        }
        return infusionTanks;
    }

    @Nonnull
    @Override
    public List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getPigmentTanks(side);
        }
        return pigmentTanks;
    }

    @Nonnull
    @Override
    public List<ISlurryTank> getSlurryTanks(@Nullable Direction side) {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getSlurryTanks(side);
        }
        return slurryTanks;
    }

    @Override
    public void onContentsChanged() {
        getTransmitterTile().markDirty(false);
    }

    @Override
    protected void handleContentsUpdateTag(@Nonnull BoxedChemicalNetwork network, @Nonnull CompoundNBT tag) {
        super.handleContentsUpdateTag(network, tag);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> network.currentScale = scale);
        NBTUtils.setBoxedChemicalIfPresent(tag, NBTConstants.BOXED_CHEMICAL, network::setLastChemical);
    }

    public IGasTank getGasTank() {
        return chemicalTank.getGasTank();
    }

    public IInfusionTank getInfusionTank() {
        return chemicalTank.getInfusionTank();
    }

    public IPigmentTank getPigmentTank() {
        return chemicalTank.getPigmentTank();
    }

    public ISlurryTank getSlurryTank() {
        return chemicalTank.getSlurryTank();
    }
}