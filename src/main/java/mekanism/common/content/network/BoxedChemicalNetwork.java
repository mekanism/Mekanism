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
import mekanism.common.capabilities.chemical.variable.VariableCapacityChemicalTankBuilder;
import mekanism.common.content.network.distribution.BoxedChemicalTransmitterSaveTarget;
import mekanism.common.content.network.distribution.ChemicalHandlerTarget;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A DynamicNetwork extension created specifically for the transfer of Chemicals.
 */
public class BoxedChemicalNetwork extends DynamicBufferedNetwork<IChemicalHandler, BoxedChemicalNetwork, ChemicalStack, BoxedPressurizedTube>
      implements IChemicalTracker {

    public final IChemicalTank chemicalTank;
    private final List<IChemicalTank> chemicalTanks;
    public Chemical lastChemical = MekanismAPI.EMPTY_CHEMICAL;
    private long prevTransferAmount;

    public BoxedChemicalNetwork(UUID networkID) {
        super(networkID);
        chemicalTank = VariableCapacityChemicalTankBuilder.INSTANCE.createAllValid(this::getCapacity, this);
        chemicalTanks = Collections.singletonList(chemicalTank);
    }

    public BoxedChemicalNetwork(Collection<BoxedChemicalNetwork> networks) {
        this(UUID.randomUUID());
        adoptAllAndRegister(networks);
    }

    public boolean isTankEmpty() {
        return chemicalTank.isEmpty();
    }

    public IChemicalTank getChemicalTank() {
        return chemicalTank;
    }

    @Override
    protected void forceScaleUpdate() {
        if (!isTankEmpty() && getCapacity() > 0) {
            currentScale = (float) Math.min(1, getChemicalTank().getStored() / (double) getCapacity());
        } else {
            currentScale = 0;
        }
    }

    @Override
    public List<BoxedPressurizedTube> adoptTransmittersAndAcceptorsFrom(BoxedChemicalNetwork net) {
        float oldScale = currentScale;
        long oldCapacity = getCapacity();
        List<BoxedPressurizedTube> transmittersToUpdate = super.adoptTransmittersAndAcceptorsFrom(net);
        //Merge the chemical scales
        long capacity = getCapacity();
        currentScale = Math.min(1, capacity == 0 ? 0 : (currentScale * oldCapacity + net.currentScale * net.capacity) / capacity);
        if (isRemote()) {
            if (isTankEmpty()) {
                adoptBuffer(net);
            }
        } else {
            if (!net.isTankEmpty()) {
                if (isTankEmpty()) {
                    adoptBuffer(net);
                } else {
                    // compare the chemicals themselves
                    IChemicalTank tank = this.chemicalTank;
                    IChemicalTank netTank = net.chemicalTank;
                    if (tank.getType() == netTank.getType()) {
                        long amount = netTank.getStored();
                        MekanismUtils.logMismatchedStackSize(tank.growStack(amount, Action.EXECUTE), amount);
                    } else {
                        Mekanism.logger.error("Incompatible chemical networks merged: {}, {}.", tank.getStack(), netTank.getStack());
                    }
                    netTank.setEmpty();
                }
            }
            if (oldScale != currentScale) {
                //We want to make sure we update to the scale change
                needsUpdate = true;
            }
        }
        return transmittersToUpdate;
    }

    private void adoptBuffer(BoxedChemicalNetwork net) {
        IChemicalTank other = net.getChemicalTank();
        ChemicalStack stack = other.getStack();
        getChemicalTank().setStack(stack.copy());
        other.setEmpty();
    }

    @NotNull
    @Override
    public ChemicalStack getBuffer() {
        return chemicalTank.getStack().copy();
    }

    @Override
    public void absorbBuffer(BoxedPressurizedTube transmitter) {
        ChemicalStack transmitterReleased = transmitter.releaseShare();
        if (!transmitterReleased.isEmpty()) {
            if (isTankEmpty()) {
                ChemicalStack stack = transmitterReleased.copy();
                chemicalTank.setStack(stack);
            } else {
                IChemicalTank tank = chemicalTank;
                if (transmitterReleased.getChemical() == tank.getType()) {
                    long amount = transmitterReleased.getAmount();
                    MekanismUtils.logMismatchedStackSize(tank.growStack(amount, Action.EXECUTE), amount);
                }
            }
        }
    }

    @Override
    public void clampBuffer() {
        if (!isTankEmpty()) {
            long capacity = getCapacity();
            if (chemicalTank.getStored() > capacity) {
                MekanismUtils.logMismatchedStackSize(chemicalTank.setStackSize(capacity, Action.EXECUTE), capacity);
            }
        }
    }

    @Override
    protected void updateSaveShares(@Nullable BoxedPressurizedTube triggerTransmitter) {
        super.updateSaveShares(triggerTransmitter);
        if (!isEmpty()) {
            updateSaveShares(triggerTransmitter, getChemicalTank().getStack());
        }
    }

    private void updateSaveShares(@Nullable BoxedPressurizedTube triggerTransmitter,
          ChemicalStack chemical) {
        ChemicalStack empty = ChemicalStack.EMPTY;
        BoxedChemicalTransmitterSaveTarget saveTarget = new BoxedChemicalTransmitterSaveTarget(empty, chemical, getTransmitters());
        long sent = EmitUtils.sendToAcceptors(saveTarget, chemical.getAmount(), chemical);
        if (triggerTransmitter != null && sent < chemical.getAmount()) {
            disperse(triggerTransmitter, chemical.copyWithAmount(chemical.getAmount() - sent));
        }
        saveTarget.saveShare();
    }

    @Override
    protected void onLastTransmitterRemoved(@NotNull BoxedPressurizedTube triggerTransmitter) {
        if (!isTankEmpty()) {
            disperse(triggerTransmitter, chemicalTank.getStack());
        }
    }

    protected void disperse(@NotNull BoxedPressurizedTube triggerTransmitter, ChemicalStack stack) {
        // Handle radiation leakage
        IRadiationManager.INSTANCE.dumpRadiation(triggerTransmitter.getTileGlobalPos(), stack);
    }

    private long tickEmit(@NotNull ChemicalStack stack) {
        Collection<Map<Direction, IChemicalHandler>> acceptorValues = acceptorCache.getAcceptorValues();
        ChemicalHandlerTarget target = new ChemicalHandlerTarget(stack, acceptorValues.size() * 2);
        for (Map<Direction, IChemicalHandler> acceptors : acceptorValues) {
            for (IChemicalHandler handler : acceptors.values()) {
                if (handler != null && ChemicalUtil.canInsert(handler, stack)) {
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
        if (isTankEmpty()) {
            prevTransferAmount = 0;
        } else {
            prevTransferAmount = tickEmit(chemicalTank.getStack());
            MekanismUtils.logMismatchedStackSize(chemicalTank.shrinkStack(prevTransferAmount, Action.EXECUTE), prevTransferAmount);
        }
    }

    @Override
    protected float computeContentScale() {
        float scale = (float) (getChemicalTank().getStored() / (double) getCapacity());
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
        return TextComponentUtil.build(getChemicalTank().getNeeded());
    }

    @Override
    public Component getStoredInfo() {
        if (isTankEmpty()) {
            return MekanismLang.NONE.translate();
        }
        IChemicalTank tank = getChemicalTank();
        return MekanismLang.NETWORK_MB_STORED.translate(tank.getStack(), tank.getStored());
    }

    @Override
    public Component getFlowInfo() {
        return MekanismLang.NETWORK_MB_PER_TICK.translate(prevTransferAmount);
    }

    @Override
    public boolean isCompatibleWith(BoxedChemicalNetwork other) {
        if (super.isCompatibleWith(other)) {
            if (isTankEmpty()) {
                return true;
            }
            return other.isTankEmpty() || chemicalTank.getType() == other.chemicalTank.getType();
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
        Chemical type = chemicalTank.getType();
        if (!lastChemical.equals(type)) {
            //If the chemical type does not match update it, and mark that we need an update
            if (!type.isEmptyType()) {
                lastChemical = type;
            }
            needsUpdate = true;
        }
    }

    public void setLastChemical(@NotNull Chemical chemical) {
        if (chemical.isEmptyType()) {
            if (!isTankEmpty()) {
                chemicalTank.setEmpty();
            }
        } else {
            lastChemical = chemical;
            chemicalTank.setStack(lastChemical.getChemical().getStack(1));
        }
    }

    @Override
    public List<IChemicalTank> getChemicalTanks(@Nullable Direction side) {
        return chemicalTanks;
    }

    public static class ChemicalTransferEvent extends TransferEvent<BoxedChemicalNetwork> {

        public final Chemical transferType;

        public ChemicalTransferEvent(BoxedChemicalNetwork network, Chemical type) {
            super(network);
            transferType = type;
        }
    }
}