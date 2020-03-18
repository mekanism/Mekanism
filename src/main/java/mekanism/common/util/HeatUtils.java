package mekanism.common.util;

import mekanism.api.IHeatTransfer;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class HeatUtils {

    public static double[] simulate(IHeatTransfer source) {
        double[] heatTransferred = new double[]{0, 0};
        for (Direction side : EnumUtils.DIRECTIONS) {
            IHeatTransfer sink = source.getAdjacent(side);
            if (sink != null) {
                double invConduction = sink.getInverseConductionCoefficient() + source.getInverseConductionCoefficient();
                double heatToTransfer = source.getTemp() / invConduction;
                source.transferHeatTo(-heatToTransfer);
                sink.transferHeatTo(heatToTransfer);
                if (!(sink instanceof ICapabilityProvider) || !CapabilityUtils.getCapability((ICapabilityProvider) sink, Capabilities.GRID_TRANSMITTER_CAPABILITY,
                      null).filter(transmitter -> TransmissionType.checkTransmissionType(transmitter, TransmissionType.HEAT)).isPresent()) {
                    heatTransferred[0] += heatToTransfer;
                }
                continue;
            }

            //Transfer to air otherwise
            double invConduction = IHeatTransfer.AIR_INVERSE_COEFFICIENT + source.getInsulationCoefficient(side) + source.getInverseConductionCoefficient();
            double heatToTransfer = source.getTemp() / invConduction;
            source.transferHeatTo(-heatToTransfer);
            heatTransferred[1] += heatToTransfer;
        }
        return heatTransferred;
    }
}