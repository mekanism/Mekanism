package mekanism.common.capabilities.heat;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface ITileHeatHandler extends IMekanismHeatHandler {

    default void updateHeatCapacitors(@Nullable Direction side) {
        for (IHeatCapacitor capacitor : getHeatCapacitors(side)) {
            if (capacitor instanceof BasicHeatCapacitor heatCapacitor) {
                heatCapacitor.update();
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
    default IHeatHandler getAdjacent(Direction side) {
        return null;
    }

    /**
     * Simulate heat transfers
     */
    default HeatTransfer simulate() {
        return new HeatTransfer(simulateAdjacent(), simulateEnvironment());
    }

    default double getAmbientTemperature(Direction side) {
        return HeatAPI.AMBIENT_TEMP;
    }

    default double simulateEnvironment() {
        double environmentTransfer = 0;
        for (Direction side : EnumUtils.DIRECTIONS) {
            double heatCapacity = getTotalHeatCapacity(side);
            //transfer to air otherwise
            double invConduction = HeatAPI.AIR_INVERSE_COEFFICIENT + getTotalInverseInsulation(side) + getTotalInverseConductionCoefficient(side);
            //transfer heat difference based on environment temperature (ambient)
            double tempToTransfer = (getTotalTemperature(side) - getAmbientTemperature(side)) / invConduction;
            handleHeat(-tempToTransfer * heatCapacity, side);
            if (tempToTransfer > 0) {
                //Only count it towards environmental loss if it is hotter than the ambient temperature
                environmentTransfer += tempToTransfer;
            }
        }
        return environmentTransfer;
    }

    default double simulateAdjacent() {
        double adjacentTransfer = 0;
        for (Direction side : EnumUtils.DIRECTIONS) {
            IHeatHandler sink = getAdjacent(side);
            if (sink != null) {
                double heatCapacity = getTotalHeatCapacity(side);
                double invConduction = sink.getTotalInverseConduction() + getTotalInverseConductionCoefficient(side);
                double tempToTransfer = (getTotalTemperature(side) - getAmbientTemperature(side)) / invConduction;
                //TODO - 1.18: Try and figure out how to do this properly/I believe the below is correct
                // but it seems to nerf the heat system quite a bit so needs more review than being able
                // to be done just before a release is made
                /*double temp = getTotalTemperature(side);
                double sinkTemp = sink.getTotalTemperature();
                if (temp <= sinkTemp) {
                    //If our temperature is lower than the sink, we skip calculating what the adjacent loss to the sink
                    // is as if the sink is able to have heat transferred away from it (which is a bit of a weird concept
                    // in relation to thermodynamics, but makes some sense with our implementation), it will be handled by
                    // the sink when the sink simulates adjacent heat transfers. This also prevents us from having heat
                    // transfers effectively happen "twice" per tick rather than just once
                    // Note: We also skip if our temp is equal to the sink's temperature so that we can short circuit
                    // past the following logic
                    continue;
                }
                double heatCapacity = getTotalHeatCapacity(side);
                double sinkHeatCapacity = sink.getTotalHeatCapacity();
                //Calculate the target temperature using calorimetry
                double finalTemp = (temp * heatCapacity + sinkTemp * sinkHeatCapacity) / (heatCapacity + sinkHeatCapacity);
                double invConduction = sink.getTotalInverseConduction() + getTotalInverseConductionCoefficient(side);
                double tempToTransfer = (temp - finalTemp) / invConduction;*/
                double heatToTransfer = tempToTransfer * heatCapacity;
                handleHeat(-heatToTransfer, side);
                //Note: Our sinks in mek are "lazy" but they will update the next tick if needed
                sink.handleHeat(heatToTransfer);
                adjacentTransfer = incrementAdjacentTransfer(adjacentTransfer, tempToTransfer, side);
            }
        }
        return adjacentTransfer;
    }

    default double incrementAdjacentTransfer(double currentAdjacentTransfer, double tempToTransfer, Direction side) {
        return currentAdjacentTransfer + tempToTransfer;
    }
}