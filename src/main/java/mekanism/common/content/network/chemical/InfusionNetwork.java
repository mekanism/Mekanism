package mekanism.common.content.network.chemical;

import java.util.Collection;
import java.util.UUID;
import mekanism.api.chemical.infuse.BasicInfusionTank;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionHandler.IMekanismInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.chemical.variable.VariableCapacityInfusionTank;
import mekanism.common.content.network.transmitter.chemical.InfusionPressurizedTube;
import mekanism.common.lib.transmitter.TransmissionType;

public class InfusionNetwork extends ChemicalNetwork<InfuseType, InfusionStack, IInfusionHandler, IInfusionTank, InfusionNetwork, InfusionPressurizedTube>
      implements IMekanismInfusionHandler {

    public InfusionNetwork() {
    }

    public InfusionNetwork(UUID networkID) {
        super(networkID);
    }

    public InfusionNetwork(Collection<InfusionNetwork> networks) {
        super(networks);
    }

    @Override
    protected IInfusionTank createTank() {
        return VariableCapacityInfusionTank.create(this::getCapacity, BasicInfusionTank.alwaysTrueBi, BasicInfusionTank.alwaysTrueBi, BasicInfusionTank.alwaysTrue, this);
    }

    @Override
    protected InfusionTransferEvent getTransferEvent() {
        return new InfusionTransferEvent(this, lastChemical);
    }

    @Override
    protected ILangEntry getNetworkName() {
        return MekanismLang.INFUSION_NETWORK;
    }

    @Override
    protected String getNetworkNameRaw() {
        return TransmissionType.INFUSION.getName();
    }

    public static class InfusionTransferEvent extends ChemicalTransferEvent<InfuseType, InfusionNetwork> {

        public InfusionTransferEvent(InfusionNetwork network, InfuseType type) {
            super(network, type);
        }
    }
}