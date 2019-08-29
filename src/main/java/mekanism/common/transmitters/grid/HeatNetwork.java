package mekanism.common.transmitters.grid;

import java.util.Collection;
import mekanism.api.IHeatTransfer;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.transmitters.TransmitterImpl;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.TextComponentUtil;
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
        //TODO: Lang string
        return TextComponentUtil.build("Not Applicable");
    }

    @Override
    public ITextComponent getStoredInfo() {
        //TODO: Lang String
        return TextComponentUtil.build(MekanismUtils.getTemperatureDisplay(meanTemp, TemperatureUnit.KELVIN), " above ambient");
    }

    @Override
    public ITextComponent getFlowInfo() {
        //TODO: Lang Strings
        return TextComponentUtil.build(MekanismUtils.getTemperatureDisplay(heatTransferred, TemperatureUnit.KELVIN), " transferred to acceptors, ",
              MekanismUtils.getTemperatureDisplay(heatLost, TemperatureUnit.KELVIN),
              " lost to environment, " + (heatTransferred + heatLost == 0 ? "" : heatTransferred / (heatTransferred + heatLost) * 100 + "% efficiency"));
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
                    //TODO: Capability fix this as it is casting when it shouldn't be because it returns a LazyOptional
                    IHeatTransfer heatTransmitter = (IHeatTransfer) ((TransmitterImpl) transmitter).getTileEntity().getCapability(Capabilities.HEAT_TRANSFER_CAPABILITY);
                    if (heatTransmitter != null) {
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
}