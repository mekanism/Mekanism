package mekanism.common.content.network;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.chemical.IChemicalTracker;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.content.network.distribution.ChemicalHandlerTarget;
import mekanism.common.content.network.distribution.ChemicalTransmitterSaveTarget;
import mekanism.common.content.network.transmitter.PressurizedTube;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A DynamicNetwork extension created specifically for the transfer of Chemicals.
 */
public class ChemicalNetwork extends DynamicBufferedNetwork<IChemicalHandler, ChemicalNetwork, ChemicalStack, PressurizedTube> implements IChemicalTracker {

    public final IChemicalTank chemicalTank;
    private final List<IChemicalTank> chemicalTanks;
    public Holder<Chemical> lastChemical = MekanismAPI.EMPTY_CHEMICAL_HOLDER;
    private long prevTransferAmount;

    public ChemicalNetwork(UUID networkID) {
        super(networkID);
        chemicalTank = VariableCapacityChemicalTank.createAllValid(this::getCapacity, this);
        chemicalTanks = Collections.singletonList(chemicalTank);
    }

    public ChemicalNetwork(Collection<ChemicalNetwork> networks) {
        this(UUID.randomUUID());
        adoptAllAndRegister(networks);
    }

    public IChemicalTank getChemicalTank() {
        return chemicalTank;
    }

    @Override
    protected void forceScaleUpdate() {
        if (!chemicalTank.isEmpty() && getCapacity() > 0) {
            currentScale = (float) Math.min(1, chemicalTank.getStored() / (double) getCapacity());
        } else {
            currentScale = 0;
        }
    }

    @Override
    public List<PressurizedTube> adoptTransmittersAndAcceptorsFrom(ChemicalNetwork net) {
        float oldScale = currentScale;
        long oldCapacity = getCapacity();
        List<PressurizedTube> transmittersToUpdate = super.adoptTransmittersAndAcceptorsFrom(net);
        //Merge the chemical scales
        long capacity = getCapacity();
        currentScale = Math.min(1, capacity == 0 ? 0 : (currentScale * oldCapacity + net.currentScale * net.capacity) / capacity);
        if (isRemote()) {
            if (chemicalTank.isEmpty()) {
                adoptBuffer(net);
            }
        } else {
            if (!net.chemicalTank.isEmpty()) {
                if (chemicalTank.isEmpty()) {
                    adoptBuffer(net);
                } else {
                    // compare the chemicals themselves
                    if (this.chemicalTank.isTypeEqual(net.chemicalTank.getStack())) {
                        long amount = net.chemicalTank.getStored();
                        MekanismUtils.logMismatchedStackSize(this.chemicalTank.growStack(amount, Action.EXECUTE), amount);
                    } else {
                        Mekanism.logger.error("Incompatible chemical networks merged: {}, {}.", this.chemicalTank.getStack(), net.chemicalTank.getStack());
                    }
                    net.chemicalTank.setEmpty();
                }
            }
            if (oldScale != currentScale) {
                //We want to make sure we update to the scale change
                needsUpdate = true;
            }
        }
        return transmittersToUpdate;
    }

    private void adoptBuffer(ChemicalNetwork net) {
        IChemicalTank other = net.getChemicalTank();
        chemicalTank.setStack(other.getStack().copy());
        other.setEmpty();
    }

    @NotNull
    @Override
    public ChemicalStack getBuffer() {
        return chemicalTank.getStack().copy();
    }

    @Override
    public void absorbBuffer(PressurizedTube transmitter) {
        ChemicalStack transmitterReleased = transmitter.releaseShare();
        if (!transmitterReleased.isEmpty()) {
            if (chemicalTank.isEmpty()) {
                chemicalTank.setStack(transmitterReleased.copy());
            } else if (chemicalTank.isTypeEqual(transmitterReleased)) {
                long amount = transmitterReleased.getAmount();
                MekanismUtils.logMismatchedStackSize(chemicalTank.growStack(amount, Action.EXECUTE), amount);
            }
        }
    }

    @Override
    public void clampBuffer() {
        if (!chemicalTank.isEmpty()) {
            long capacity = getCapacity();
            if (chemicalTank.getStored() > capacity) {
                MekanismUtils.logMismatchedStackSize(chemicalTank.setStackSize(capacity, Action.EXECUTE), capacity);
            }
        }
    }

    @Override
    protected void updateSaveShares(@Nullable PressurizedTube triggerTransmitter) {
        super.updateSaveShares(triggerTransmitter);
        if (!isEmpty()) {
            updateSaveShares(triggerTransmitter, chemicalTank.getStack());
        }
    }

    private void updateSaveShares(@Nullable PressurizedTube triggerTransmitter, ChemicalStack chemical) {
        ChemicalTransmitterSaveTarget saveTarget = new ChemicalTransmitterSaveTarget(getTransmitters());
        long sent = EmitUtils.sendToAcceptors(saveTarget, chemical.getAmount(), chemical);
        if (triggerTransmitter != null && sent < chemical.getAmount()) {
            disperse(triggerTransmitter, chemical.copyWithAmount(chemical.getAmount() - sent));
        }
        saveTarget.saveShare();
    }

    @Override
    protected void onLastTransmitterRemoved(@NotNull PressurizedTube triggerTransmitter) {
        if (!chemicalTank.isEmpty()) {
            disperse(triggerTransmitter, chemicalTank.getStack());
        }
    }

    protected void disperse(@NotNull PressurizedTube triggerTransmitter, ChemicalStack stack) {
        // Handle radiation leakage
        IRadiationManager.INSTANCE.dumpRadiation(triggerTransmitter.getLevel(), triggerTransmitter.getBlockPos(), stack);
    }

    private long tickEmit(@NotNull ChemicalStack stack) {
        Collection<Map<Direction, IChemicalHandler>> acceptorValues = acceptorCache.getAcceptorValues();
        ChemicalHandlerTarget target = null;
        for (Map<Direction, IChemicalHandler> acceptors : acceptorValues) {
            for (IChemicalHandler handler : acceptors.values()) {
                if (ChemicalUtil.canInsert(handler, stack)) {
                    if (target == null) {
                        //Lazily initialize the target, which allows us to also skip attempting to start emitting
                        target = new ChemicalHandlerTarget(acceptorValues.size() * 2);
                    }
                    target.addHandler(handler);
                }
            }
        }
        return EmitUtils.sendToAcceptors(target, stack.getAmount(), stack);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (needsUpdate) {
            NeoForge.EVENT_BUS.post(new ChemicalTransferEvent(this, lastChemical));
            needsUpdate = false;
        }
        if (chemicalTank.isEmpty()) {
            prevTransferAmount = 0;
        } else {
            prevTransferAmount = tickEmit(chemicalTank.getStack());
            MekanismUtils.logMismatchedStackSize(chemicalTank.shrinkStack(prevTransferAmount, Action.EXECUTE), prevTransferAmount);
        }
    }

    @Override
    protected float computeContentScale() {
        float scale = (float) (chemicalTank.getStored() / (double) getCapacity());
        float ret = Math.max(currentScale, scale);
        if (prevTransferAmount > 0 && ret < 1) {
            ret = Math.min(1, ret + 0.02F);
        } else if (prevTransferAmount <= 0 && ret > 0) {
            ret = Math.max(scale, ret - 0.02F);
        }
        return ret;
    }

    public long getPrevTransferAmount() {
        return prevTransferAmount;
    }

    @Override
    public Component getNeededInfo() {
        return TextComponentUtil.build(chemicalTank.getNeeded());
    }

    @Override
    public Component getStoredInfo() {
        if (chemicalTank.isEmpty()) {
            return MekanismLang.NONE.translate();
        }
        return MekanismLang.NETWORK_MB_STORED.translate(chemicalTank.getStack(), chemicalTank.getStored());
    }

    @Override
    public Component getFlowInfo() {
        return MekanismLang.NETWORK_MB_PER_TICK.translate(prevTransferAmount);
    }

    @Override
    public boolean isCompatibleWith(ChemicalNetwork other) {
        if (super.isCompatibleWith(other)) {
            if (chemicalTank.isEmpty()) {
                return true;
            }
            return other.chemicalTank.isEmpty() || chemicalTank.isTypeEqual(other.chemicalTank.getStack());
        }
        return false;
    }

    @NotNull
    @Override
    public Component getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.CHEMICAL_NETWORK, transmittersSize(), getAcceptorCount());
    }

    @Override
    public String toString() {
        return "[ChemicalNetwork] " + transmittersSize() + " transmitters, " + getAcceptorCount() + " acceptors.";
    }

    @Override
    public void onContentsChanged() {
        markDirty();
        if (!chemicalTank.isTypeEqual(lastChemical)) {
            //If the chemical type does not match update it, and mark that we need an update
            if (!chemicalTank.isEmpty()) {
                lastChemical = chemicalTank.getTypeHolder();
            }
            needsUpdate = true;
        }
    }

    public void setLastChemical(@NotNull Holder<Chemical> chemical) {
        if (chemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
            if (!chemicalTank.isEmpty()) {
                chemicalTank.setEmpty();
            }
        } else {
            lastChemical = chemical;
            chemicalTank.setStack(new ChemicalStack(lastChemical, 1));
        }
    }

    @NotNull
    @Override
    public List<IChemicalTank> getChemicalTanks(@Nullable Direction side) {
        return chemicalTanks;
    }

    public static class ChemicalTransferEvent extends TransferEvent<ChemicalNetwork> {

        public final Holder<Chemical> transferType;

        public ChemicalTransferEvent(ChemicalNetwork network, Holder<Chemical> type) {
            super(network);
            transferType = type;
        }
    }
}