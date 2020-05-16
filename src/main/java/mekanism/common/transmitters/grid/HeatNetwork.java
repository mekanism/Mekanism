package mekanism.common.transmitters.grid;

import java.util.Collection;
import java.util.UUID;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.transmitters.TransmitterImpl;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.util.text.ITextComponent;

public class HeatNetwork extends DynamicNetwork<IHeatHandler, HeatNetwork, Void> {

    private double meanTemp = HeatAPI.AMBIENT_TEMP;
    private double heatLost;
    private double heatTransferred;

    public HeatNetwork() {
    }

    public HeatNetwork(UUID networkID) {
        super(networkID);
    }

    public HeatNetwork(Collection<HeatNetwork> networks) {
        for (HeatNetwork net : networks) {
            if (net != null) {
                adoptTransmittersAndAcceptorsFrom(net);
                net.deregister();
            }
        }
        register();
    }

    @Override
    public ITextComponent getNeededInfo() {
        return MekanismLang.NOT_APPLICABLE.translate();
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
    public void absorbBuffer(IGridTransmitter<IHeatHandler, HeatNetwork, Void> transmitter) {
    }

    @Override
    public void clampBuffer() {
    }

    @Override
    protected synchronized void updateCapacity(IGridTransmitter<IHeatHandler, HeatNetwork, Void> transmitter) {
        //The capacity is always zero so no point in doing calculations.
    }

    @Override
    public synchronized void updateCapacity() {
        //The capacity is always zero so no point in doing calculations.
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isRemote()) {
            double newSumTemp = 0, newHeatLost = 0, newHeatTransferred = 0;
            for (IGridTransmitter<IHeatHandler, HeatNetwork, Void> transmitter : transmitters) {
                if (transmitter instanceof TransmitterImpl) {
                    // change this when we re-integrate with multipart
                    if (((TransmitterImpl<?, ?, ?>) transmitter).containingTile instanceof ITileHeatHandler) {
                        ITileHeatHandler heatTile = (ITileHeatHandler) ((TransmitterImpl<?, ?, ?>) transmitter).containingTile;
                        HeatTransfer transfer = heatTile.simulate();
                        heatTile.updateHeatCapacitors(null);
                        newHeatTransferred += transfer.getAdjacentTransfer();
                        newHeatLost += transfer.getEnvironmentTransfer();
                        newSumTemp += heatTile.getTotalTemperature();
                    }
                }
            }
            heatLost = newHeatLost;
            heatTransferred = newHeatTransferred;
            meanTemp = newSumTemp / transmitters.size();
        }
    }

    @Override
    public String toString() {
        return "[HeatNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
    }

    @Override
    public ITextComponent getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.HEAT_NETWORK, transmitters.size(), possibleAcceptors.size());
    }
}