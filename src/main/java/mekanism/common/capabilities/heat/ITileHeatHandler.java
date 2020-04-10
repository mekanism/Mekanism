package mekanism.common.capabilities.heat;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

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

    /**
     * Gets the {@link IHeatHandler} adjacent to this {@link ITileHeatHandler}.
     *
     * @param side The side of this {@link ITileHeatHandler} to look on.
     *
     * @return The {@link IHeatHandler} adjacent to this {@link ITileHeatHandler}, otherwise returns {@code null}.
     */
    @Nullable
    default IHeatHandler getAdjacent(@Nullable Direction side) {
        return null;
    }

    /**
     * Simulate heat transfers
     */
    default HeatTransfer simulate() {
        double adjacentTransfer = 0, environmentTransfer = 0;

        for (Direction side : EnumUtils.DIRECTIONS) {
            IHeatHandler sink = getAdjacent(side);
            if (sink != null) {
                double invConduction = sink.getTotalInverseConduction() + getTotalInverseConductionCoefficient(side);
                double heatToTransfer = (getTotalTemperature(side) - HeatAPI.AMBIENT_TEMP) / invConduction;
                handleHeat(-heatToTransfer, side);
                sink.handleHeat(heatToTransfer);
                if (!(sink instanceof ICapabilityProvider) || !CapabilityUtils.getCapability((ICapabilityProvider) sink, Capabilities.GRID_TRANSMITTER_CAPABILITY, null)
                      .filter(transmitter -> TransmissionType.checkTransmissionType(transmitter, TransmissionType.HEAT)).isPresent()) {
                    adjacentTransfer += heatToTransfer;
                }
                continue;
            }
            //Transfer to air otherwise
            double invConduction = HeatAPI.AIR_INVERSE_COEFFICIENT + getTotalInverseInsulation(side) + getTotalInverseConductionCoefficient(side);
            //Transfer heat difference based on environment temperature (ambient)
            double heatToTransfer = (getTotalTemperature(side) - HeatAPI.AMBIENT_TEMP) / invConduction;
            handleHeat(-heatToTransfer, side);
            environmentTransfer += heatToTransfer;
        }
        return new HeatTransfer(adjacentTransfer, environmentTransfer);
    }
}