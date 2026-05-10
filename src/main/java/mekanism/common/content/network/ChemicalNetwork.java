package mekanism.common.content.network;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.content.network.transmitter.PressurizedTube;
import mekanism.common.lib.transmitter.DynamicBufferedResourceNetwork;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

/**
 * A DynamicNetwork extension created specifically for the transfer of Chemicals.
 */
public class ChemicalNetwork extends DynamicBufferedResourceNetwork<ChemicalResource, IChemicalTank, ChemicalNetwork, PressurizedTube> {

    public ChemicalNetwork(UUID networkID) {
        super(networkID, VariableCapacityChemicalTank::createAllValid);
    }

    public ChemicalNetwork(Collection<ChemicalNetwork> networks) {
        this(UUID.randomUUID());
        adoptAllAndRegister(networks);
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
            if (container.isEmpty()) {
                adoptBuffer(net);
            }
        } else {
            if (!net.container.isEmpty()) {
                if (container.isEmpty()) {
                    adoptBuffer(net);
                } else {
                    // compare the chemicals themselves
                    if (this.container.getResource().equals(net.container.getResource())) {
                        long amount = net.container.amountAsLong();
                        MekanismUtils.logMismatchedStackSize(this.container.growStack(amount, Action.EXECUTE), amount);
                    } else {
                        Mekanism.logger.error("Incompatible chemical networks merged: {}, {}.", this.container.getStack(), net.container.getStack());
                    }
                    net.container.setEmpty();
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
        IChemicalTank other = net.getContainer();
        container.setContents(other.getResource(), other.amountAsLong());
        other.setEmpty();
    }

    @Override
    protected void disperse(PressurizedTube triggerTransmitter, ChemicalResource resource, long amount) {
        // Handle radiation leakage
        IRadiationManager.INSTANCE.dumpRadiation(triggerTransmitter.getLevel(), triggerTransmitter.getBlockPos(), resource.toStack(amount));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (needsUpdate) {
            NeoForge.EVENT_BUS.post(new ChemicalTransferEvent(this, getLastType()));
            needsUpdate = false;
        }
        tickEmit();
    }

    @Override
    public Component getNeededInfo() {
        return TextComponentUtil.build(container.getNeededAsLong());
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
    protected ChemicalResource getEmptyType() {
        return ChemicalResource.EMPTY;
    }

    public static class ChemicalTransferEvent extends TransferEvent<ChemicalNetwork> {

        public final ChemicalResource transferType;

        public ChemicalTransferEvent(ChemicalNetwork network, ChemicalResource type) {
            super(network);
            transferType = type;
        }
    }
}