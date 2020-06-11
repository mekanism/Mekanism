package mekanism.common.content.transmitter;

import java.util.Collection;
import java.util.UUID;
import mekanism.api.chemical.pigment.BasicPigmentTank;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentHandler.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.chemical.variable.VariableCapacityPigmentTank;
import mekanism.common.lib.transmitter.TransmissionType;

public class PigmentNetwork extends ChemicalNetwork<Pigment, PigmentStack, IPigmentHandler, IPigmentTank, PigmentNetwork> implements IMekanismPigmentHandler {

    public PigmentNetwork() {
    }

    public PigmentNetwork(UUID networkID) {
        super(networkID);
    }

    public PigmentNetwork(Collection<PigmentNetwork> networks) {
        super(networks);
    }

    @Override
    protected IPigmentTank createTank() {
        return VariableCapacityPigmentTank.create(this::getCapacity, BasicPigmentTank.alwaysTrueBi, BasicPigmentTank.alwaysTrueBi, BasicPigmentTank.alwaysTrue, this);
    }

    @Override
    protected PigmentTransferEvent getTransferEvent() {
        return new PigmentTransferEvent(this, lastChemical);
    }

    @Override
    protected ILangEntry getNetworkName() {
        return MekanismLang.PIGMENT_NETWORK;
    }

    @Override
    protected String getNetworkNameRaw() {
        return TransmissionType.PIGMENT.getName();
    }

    public static class PigmentTransferEvent extends ChemicalTransferEvent<Pigment, PigmentNetwork> {

        public PigmentTransferEvent(PigmentNetwork network, Pigment type) {
            super(network, type);
        }
    }
}