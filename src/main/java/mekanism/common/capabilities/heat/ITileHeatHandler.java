package mekanism.common.capabilities.heat;

import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ITileHeatHandler {

    /// Gets the [IHeatHandler] adjacent to this [ITileHeatHandler].
    ///
    /// @param side The side of this [ITileHeatHandler] to look on.
    ///
    /// @return The [IHeatHandler] adjacent to this [ITileHeatHandler], otherwise returns `null`.
    ///
    /// @implSpec If this method is called it can be assumed the handler has been checked as supporting heat.
    @Nullable
    default IHeatHandler getAdjacent(Direction side) {
        return null;
    }

    /// {@return the [IHeatCapacitor] on the given side}
    ///
    /// @param side The side we are interacting with the handler from (null for internal).
    @Nullable
    IHeatCapacitor getHeatCapacitor(@Nullable Direction side);

    /// Simulate heat transfers
    default HeatTransfer simulate(TransactionContext transaction) {
        return new HeatTransfer(simulateAdjacent(transaction), simulateEnvironment(transaction));
    }

    default double getAmbientTemperature(Direction side) {
        return HeatAPI.AMBIENT_TEMP;
    }

    default double simulateEnvironment(TransactionContext transaction) {
        double environmentTransfer = 0;
        for (Direction side : EnumUtils.DIRECTIONS) {
            IHeatCapacitor heatCapacitor = getHeatCapacitor(side);
            if (heatCapacitor == null) {
                continue;
            }
            double heatCapacity = heatCapacitor.getHeatCapacity();
            //transfer to air otherwise
            double invConduction = HeatAPI.AIR_INVERSE_COEFFICIENT + heatCapacitor.getInverseInsulation() + heatCapacitor.getInverseConduction();
            //transfer heat difference based on environment temperature (ambient)
            double tempToTransfer = (heatCapacitor.getTemperature() - getAmbientTemperature(side)) / invConduction;
            heatCapacitor.handleHeat(-tempToTransfer * heatCapacity, transaction);
            if (tempToTransfer > 0) {
                //Only count it towards environmental loss if it is hotter than the ambient temperature
                environmentTransfer += tempToTransfer;
            }
        }
        return environmentTransfer;
    }

    default double simulateAdjacent(TransactionContext transaction) {
        double adjacentTransfer = 0;
        for (Direction side : EnumUtils.DIRECTIONS) {
            IHeatCapacitor heatCapacitor = getHeatCapacitor(side);
            if (heatCapacitor == null) {
                continue;
            }
            //Note: We can safely call getAdjacent as we know we supuport heat transfers on the side due to having a heat capacitor
            IHeatHandler sink = getAdjacent(side);
            if (sink == null) {
                continue;
            }
            double temp = heatCapacitor.getTemperature();
            double sinkTemp = sink.getTemperature();
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
            double heatCapacity = heatCapacitor.getHeatCapacity();
            double sinkHeatCapacity = sink.getHeatCapacity();
            //Calculate the target temperature using calorimetry
            double finalTemp = (temp * heatCapacity + sinkTemp * sinkHeatCapacity) / (heatCapacity + sinkHeatCapacity);
            double invConduction = sink.getInverseConduction() + heatCapacitor.getInverseConduction();
            double tempToTransfer = (temp - finalTemp) / invConduction;
            double heatToTransfer = tempToTransfer * heatCapacity;
            heatCapacitor.handleHeat(-heatToTransfer, transaction);
            sink.handleHeat(heatToTransfer, transaction);
            if (tempToTransfer != 0 && countsAsAdjacent(side)) {
                adjacentTransfer += tempToTransfer;
            }
        }
        return adjacentTransfer;
    }

    default boolean countsAsAdjacent(Direction side) {
        return true;
    }
}