package mekanism.common.content.network.chemical;

import java.util.Collection;
import java.util.UUID;
import mekanism.api.chemical.slurry.BasicSlurryTank;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryHandler.IMekanismSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.chemical.variable.VariableCapacitySlurryTank;
import mekanism.common.content.network.transmitter.chemical.SlurryPressurizedTube;
import mekanism.common.lib.transmitter.TransmissionType;

public class SlurryNetwork extends ChemicalNetwork<Slurry, SlurryStack, ISlurryHandler, ISlurryTank, SlurryNetwork, SlurryPressurizedTube>
      implements IMekanismSlurryHandler {

    public SlurryNetwork() {
    }

    public SlurryNetwork(UUID networkID) {
        super(networkID);
    }

    public SlurryNetwork(Collection<SlurryNetwork> networks) {
        super(networks);
    }

    @Override
    protected ISlurryTank createTank() {
        return VariableCapacitySlurryTank.create(this::getCapacity, BasicSlurryTank.alwaysTrueBi, BasicSlurryTank.alwaysTrueBi, BasicSlurryTank.alwaysTrue, this);
    }

    @Override
    protected SlurryTransferEvent getTransferEvent() {
        return new SlurryTransferEvent(this, lastChemical);
    }

    @Override
    protected ILangEntry getNetworkName() {
        return MekanismLang.SLURRY_NETWORK;
    }

    @Override
    protected String getNetworkNameRaw() {
        return TransmissionType.SLURRY.getName();
    }

    public static class SlurryTransferEvent extends ChemicalTransferEvent<Slurry, SlurryNetwork> {

        public SlurryTransferEvent(SlurryNetwork network, Slurry type) {
            super(network, type);
        }
    }
}