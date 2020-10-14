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
import net.minecraft.util.text.ITextComponent;

public class HeatNetwork extends DynamicNetwork<IHeatHandler, HeatNetwork, ThermodynamicConductor> {

    private double meanTemp = HeatAPI.AMBIENT_TEMP;
    private double heatLost;
    private double heatTransferred;

    public HeatNetwork() {
    }

    public HeatNetwork(UUID networkID) {
        super(networkID);
    }

    public HeatNetwork(Collection<HeatNetwork> networks) {
        adoptAllAndRegister(networks);
    }

    @Override
    public ITextComponent getStoredInfo() {
        return MekanismLang.HEAT_NETWORK_STORED.translate(MekanismUtils.getTemperatureDisplay(meanTemp, TemperatureUnit.KELVIN, true));
    }

    @Override
    public ITextComponent getFlowInfo() {
        ITextComponent transferred = MekanismUtils.getTemperatureDisplay(heatTransferred, TemperatureUnit.KELVIN, false);
        ITextComponent lost = MekanismUtils.getTemperatureDisplay(heatLost, TemperatureUnit.KELVIN, false);
        return heatTransferred + heatLost == 0 ? MekanismLang.HEAT_NETWORK_FLOW.translate(transferred, lost)
                                               : MekanismLang.HEAT_NETWORK_FLOW_EFFICIENCY.translate(transferred, lost,
                                                     (Math.round(heatTransferred / (heatTransferred + heatLost) * 10_000) / 100F) + "%");
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        double newSumTemp = 0, newHeatLost = 0, newHeatTransferred = 0;
        for (ThermodynamicConductor transmitter : transmitters) {
            // change this when we re-integrate with multipart
            HeatTransfer transfer = transmitter.simulate();
            transmitter.updateHeatCapacitors(null);
            newHeatTransferred += transfer.getAdjacentTransfer();
            newHeatLost += transfer.getEnvironmentTransfer();
            newSumTemp += transmitter.getTotalTemperature();
        }
        heatLost = newHeatLost;
        heatTransferred = newHeatTransferred;
        meanTemp = newSumTemp / transmitters.size();
    }

    @Override
    public String toString() {
        return "[HeatNetwork] " + transmitters.size() + " transmitters, " + getAcceptorCount() + " acceptors.";
    }

    @Override
    public ITextComponent getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.HEAT_NETWORK, transmitters.size(), getAcceptorCount());
    }
}