package mekanism.common.content.network.chemical;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasHandler.IMekanismGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.chemical.variable.VariableCapacityGasTank;
import mekanism.common.content.network.transmitter.chemical.GasPressurizedTube;
import mekanism.common.lib.transmitter.TransmissionType;

public class GasNetwork extends ChemicalNetwork<Gas, GasStack, IGasHandler, IGasTank, GasNetwork, GasPressurizedTube> implements IMekanismGasHandler {

    public GasNetwork() {
    }

    public GasNetwork(UUID networkID) {
        super(networkID);
    }

    public GasNetwork(Collection<GasNetwork> networks) {
        super(networks);
    }

    @Override
    protected IGasTank createTank() {
        return VariableCapacityGasTank.create(this::getCapacity, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, this);
    }

    @Override
    protected void disperse(@Nonnull GasPressurizedTube triggerTransmitter, GasStack gas) {
        if (gas.has(GasAttributes.Radiation.class)) {
            // Handle radiation leakage
            double radioactivity = gas.get(GasAttributes.Radiation.class).getRadioactivity();
            Mekanism.radiationManager.radiate(new Coord4D(triggerTransmitter.getTilePos(), triggerTransmitter.getTileWorld()), gas.getAmount() * radioactivity);
        }
    }

    @Override
    protected GasTransferEvent getTransferEvent() {
        return new GasTransferEvent(this, lastChemical);
    }

    @Override
    protected ILangEntry getNetworkName() {
        return MekanismLang.GAS_NETWORK;
    }

    @Override
    protected String getNetworkNameRaw() {
        return TransmissionType.GAS.getName();
    }

    public static class GasTransferEvent extends ChemicalTransferEvent<Gas, GasNetwork> {

        public GasTransferEvent(GasNetwork network, Gas type) {
            super(network, type);
        }
    }
}