package mekanism.api.heat;

import mekanism.api.math.FloatingLong;

public interface IHeatHandler {

    int getHeatCapacitorCount();

    FloatingLong getTemperature();

    double getInverseConductionCoefficient();

    void handleHeatChange(HeatPacket transfer);
}