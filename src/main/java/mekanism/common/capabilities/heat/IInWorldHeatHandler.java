package mekanism.common.capabilities.heat;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IHeatTransfer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

//TODO: Maybe make come up with a better name
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IInWorldHeatHandler extends IMekanismHeatHandler {

    default void update(@Nullable Direction side) {
        for (IHeatCapacitor capacitor : getCapacitors(side)) {
            if (capacitor instanceof BasicHeatCapacitor) {
                ((BasicHeatCapacitor) capacitor).update();
            }
        }
    }

    @Nullable
    default IHeatTransfer getAdjacent(@Nullable Direction side) {
        return null;
    }

    default void simulate() {
        //TODO: Move some of this simulation logic into BasicHeatCapacitor?
        FloatingLong adjacentTransfer = FloatingLong.ZERO;
        FloatingLong environmentTransfer = FloatingLong.ZERO;
        for (Direction side : EnumUtils.DIRECTIONS) {
            IHeatTransfer sink = getAdjacent(side);
            if (sink != null) {
                double invConduction = sink.getInverseConductionCoefficient() + getInverseConductionCoefficient();
                double heatToTransfer = source.getTemp() / invConduction;
                source.transferHeatTo(-heatToTransfer);
                sink.transferHeatTo(heatToTransfer);
                if (!(sink instanceof ICapabilityProvider) || !CapabilityUtils.getCapability((ICapabilityProvider) sink, Capabilities.GRID_TRANSMITTER_CAPABILITY, null)
                      .filter(transmitter -> TransmissionType.checkTransmissionType(transmitter, TransmissionType.HEAT)).isPresent()) {
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