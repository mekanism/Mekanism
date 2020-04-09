package mekanism.common.capabilities.heat;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.HeatPacket;
import mekanism.api.heat.HeatPacket.TransferType;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
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
public interface ITileHeatHandler extends IMekanismHeatHandler {

    default void update(@Nullable Direction side) {
        for (IHeatCapacitor capacitor : getHeatCapacitors(side)) {
            if (capacitor instanceof BasicHeatCapacitor) {
                ((BasicHeatCapacitor) capacitor).update();
            }
        }
    }

    @Nullable
    default IHeatHandler getAdjacent(@Nullable Direction side) {
        return null;
    }

    default HeatTransfer simulate() {
        //TODO: Move some of this simulation logic into BasicHeatCapacitor?
        FloatingLong adjacentTransfer = FloatingLong.ZERO;
        FloatingLong environmentTransfer = FloatingLong.ZERO;
        for (Direction side : EnumUtils.DIRECTIONS) {
            IHeatHandler sink = getAdjacent(side);
            if (sink != null) {
                FloatingLong invConduction = sink.getTotalInverseConductionCoefficient().add(getTotalInverseConductionCoefficient());
                FloatingLong heatToTransfer = getTotalTemperature().divide(invConduction);
                handleHeatChange(new HeatPacket(TransferType.EMIT, heatToTransfer));
                sink.handleHeatChange(new HeatPacket(TransferType.ABSORB, heatToTransfer));
                if (!(sink instanceof ICapabilityProvider) || !CapabilityUtils.getCapability((ICapabilityProvider) sink, Capabilities.GRID_TRANSMITTER_CAPABILITY, null)
                      .filter(transmitter -> TransmissionType.checkTransmissionType(transmitter, TransmissionType.HEAT)).isPresent()) {
                    adjacentTransfer = adjacentTransfer.plusEqual(heatToTransfer);
                }
                continue;
            }

            //Transfer to air otherwise
            FloatingLong invConduction = HeatAPI.AIR_INVERSE_COEFFICIENT.plusEqual(getTotalInverseInsulation()).plusEqual(getTotalInverseConductionCoefficient());
            FloatingLong heatToTransfer = getTotalTemperature().divide(invConduction);
            handleHeatChange(new HeatPacket(TransferType.EMIT, heatToTransfer));
            environmentTransfer = environmentTransfer.plusEqual(heatToTransfer);
        }
        return new HeatTransfer(adjacentTransfer, environmentTransfer);
    }
}