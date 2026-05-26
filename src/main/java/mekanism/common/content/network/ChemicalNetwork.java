package mekanism.common.content.network;

import java.util.Collection;
import java.util.UUID;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.attachments.containers.type.ResourceContainerType;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.content.network.transmitter.PressurizedTube;
import mekanism.common.lib.transmitter.DynamicBufferedResourceNetwork;
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
    protected void disperse(PressurizedTube triggerTransmitter, ChemicalResource resource, long amount) {
        // Handle radiation leakage
        IRadiationManager.INSTANCE.dumpRadiation(triggerTransmitter.getLevel(), triggerTransmitter.getBlockPos(), resource, amount);
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
        return TextComponentUtil.build(container.getNeededAsLong(container.resource()));
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
    protected ResourceContainerType<ChemicalResource, IChemicalTank> containerType() {
        return ContainerType.CHEMICAL;
    }

    public static class ChemicalTransferEvent extends TransferEvent<ChemicalNetwork> {

        public final ChemicalResource transferType;

        public ChemicalTransferEvent(ChemicalNetwork network, ChemicalResource type) {
            super(network);
            transferType = type;
        }
    }
}