package mekanism.common.transmitters.grid;

import java.util.Collection;
import mekanism.api.IHeatTransfer;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.MekanismLang;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.transmitters.TransmitterImpl;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.util.text.ITextComponent;

public class HeatNetwork extends DynamicNetwork<IHeatTransfer, HeatNetwork, Void> {

    public double meanTemp = 0;

    public double heatLost = 0;
    public double heatTransferred = 0;

    public HeatNetwork() {
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
        return MekanismLang.HEAT_NETWORK_STORED.translate(MekanismUtils.getTemperatureDisplay(meanTemp, TemperatureUnit.KELVIN));
    }

    @Override
    public ITextComponent getFlowInfo() {
        ITextComponent transferred = MekanismUtils.getTemperatureDisplay(heatTransferred, TemperatureUnit.KELVIN);
        ITextComponent lost = MekanismUtils.getTemperatureDisplay(heatLost, TemperatureUnit.KELVIN);
        return heatTransferred + heatLost == 0 ? MekanismLang.HEAT_NETWORK_FLOW.translate(transferred, lost)
               : MekanismLang.HEAT_NETWORK_FLOW_EFFICIENCY.translate(transferred, lost, heatTransferred / (heatTransferred + heatLost) * 100);
    }

    @Override
    public void absorbBuffer(IGridTransmitter<IHeatTransfer, HeatNetwork, Void> transmitter) {
    }

    @Override
    public void clampBuffer() {
    }

    @Override
    public void updateCapacity() {
        //The capacity is always zero so no point in doing calculations.
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        double newSumTemp = 0;
        double newHeatLost = 0;
        double newHeatTransferred = 0;

        if (!isRemote()) {
            for (IGridTransmitter<IHeatTransfer, HeatNetwork, Void> transmitter : transmitters) {
                if (transmitter instanceof TransmitterImpl) {
                    LazyOptionalHelper<IHeatTransfer> capabilityHelper = CapabilityUtils.getCapabilityHelper(((TransmitterImpl<?, ?, ?>) transmitter).getTileEntity(),
                          Capabilities.HEAT_TRANSFER_CAPABILITY, null);
                    if (capabilityHelper.isPresent()) {
                        IHeatTransfer heatTransmitter = capabilityHelper.getValue();
                        double[] d = heatTransmitter.simulateHeat();
                        newHeatTransferred += d[0];
                        newHeatLost += d[1];
                        newSumTemp += heatTransmitter.applyTemperatureChange();
                    }
                }
            }
        }
        heatLost = newHeatLost;
        heatTransferred = newHeatTransferred;
        meanTemp = newSumTemp / transmitters.size();
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