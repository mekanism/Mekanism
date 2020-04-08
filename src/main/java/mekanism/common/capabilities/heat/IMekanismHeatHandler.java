package mekanism.common.capabilities.heat;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import mekanism.api.IHeatTransfer;
import mekanism.api.heat.HeatPacket;
import mekanism.api.heat.ISidedHeatHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface IMekanismHeatHandler extends ISidedHeatHandler {

    default boolean canHandleHeat() {
        return true;
    }

    List<BasicHeatCapacitor> getCapacitors(@Nullable Direction side);

    @Override
    default FloatingLong getTemperature(@Nullable Direction side) {
        FloatingLong temp = FloatingLong.ZERO;
        double totalHeatCapacity = getHeatCapacity(side);
        for (BasicHeatCapacitor capacitor : getCapacitors(side)) {
            temp = temp.add(capacitor.getTemperature().multiply(capacitor.getHeatCapacity() / totalHeatCapacity));
        }
        return temp;
    }

    @Override
    default double getInverseConductionCoefficient(@Nullable Direction side) {
        return getCapacitors(side).stream().map(c -> c.getInverseConductionCoefficient()).collect(Collectors.summingDouble(Double::doubleValue));
    }

    @Override
    default void handleHeatChange(HeatPacket transfer, @Nullable Direction side) {
        double totalHeatCapacity = getHeatCapacity(side);
        for (BasicHeatCapacitor capacitor : getCapacitors(side)) {
            capacitor.handleHeatChange(transfer.split(capacitor.getHeatCapacity() / totalHeatCapacity));
        }
    }

    default void update(@Nullable Direction side) {
        getCapacitors(side).forEach(c -> c.update());
    }

    default double getHeatCapacity(@Nullable Direction side) {
        return getCapacitors(side).stream().map(c -> c.getHeatCapacity()).collect(Collectors.summingDouble(Double::doubleValue));
    }

    default IHeatTransfer getAdjacent(Direction side) {
        return null;
    }

    default void simulate() {
        FloatingLong adjacentTransfer = FloatingLong.ZERO;
        FloatingLong environmentTransfer = FloatingLong.ZERO;

        for (Direction side : EnumUtils.DIRECTIONS) {
            IHeatTransfer sink = getAdjacent(side);
            if (sink != null) {
                double invConduction = sink.getInverseConductionCoefficient() + getInverseConductionCoefficient();
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
