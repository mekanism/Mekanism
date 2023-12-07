package mekanism.common.content.network;

import java.util.Collection;
import java.util.UUID;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.MekanismLang;
import mekanism.common.content.network.transmitter.ThermodynamicConductor;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class HeatNetwork extends DynamicNetwork<IHeatHandler, HeatNetwork, ThermodynamicConductor> {

    private double meanTemp = HeatAPI.AMBIENT_TEMP;
    private double heatLost;
    private double heatTransferred;

    public HeatNetwork(UUID networkID) {
        super(networkID);
    }

    public HeatNetwork(Collection<HeatNetwork> networks) {
        this(UUID.randomUUID());
        adoptAllAndRegister(networks);
    }

    @Override
    public Component getStoredInfo() {
        return MekanismLang.HEAT_NETWORK_STORED.translate(MekanismUtils.getTemperatureDisplay(meanTemp, TemperatureUnit.KELVIN, true));
    }

    @Override
    public Component getFlowInfo() {
        Component transferred = MekanismUtils.getTemperatureDisplay(heatTransferred, TemperatureUnit.KELVIN, false);
        Component lost = MekanismUtils.getTemperatureDisplay(heatLost, TemperatureUnit.KELVIN, false);
        if (heatTransferred + heatLost == 0) {
            return MekanismLang.HEAT_NETWORK_FLOW.translate(transferred, lost);
        }
        return MekanismLang.HEAT_NETWORK_FLOW_EFFICIENCY.translate(transferred, lost,
              MekanismLang.GENERIC_PERCENT.translate(Math.round(heatTransferred / (heatTransferred + heatLost) * 10_000) / 100F));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        double newSumTemp = 0, newHeatLost = 0, newHeatTransferred = 0;
        for (ThermodynamicConductor transmitter : getTransmitters()) {
            HeatTransfer transfer = transmitter.simulate();
            newHeatTransferred += transfer.adjacentTransfer();
            newHeatLost += transfer.environmentTransfer();
        }
        //After we updated the heat values of all the transmitters, we need to update the temperatures
        // we do this after instead of when iterating initially so that if heat is transferred from one
        // conductor to one we already updated then we want it to have the proper total temperature
        for (ThermodynamicConductor transmitter : getTransmitters()) {
            transmitter.updateHeatCapacitors(null);
            newSumTemp += transmitter.getTotalTemperature();
        }
        heatLost = newHeatLost;
        heatTransferred = newHeatTransferred;
        meanTemp = newSumTemp / transmittersSize();
    }

    @Override
    public String toString() {
        return "[HeatNetwork] " + transmittersSize() + " transmitters, " + getAcceptorCount() + " acceptors.";
    }

    @NotNull
    @Override
    public Component getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.HEAT_NETWORK, transmittersSize(), getAcceptorCount());
    }
}