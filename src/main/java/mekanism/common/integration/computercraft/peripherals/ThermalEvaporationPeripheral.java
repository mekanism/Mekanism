package mekanism.common.integration.computercraft.peripherals;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThermalEvaporationPeripheral implements IPeripheral {
    private EvaporationMultiblockData thermalEvaporation;

    public ThermalEvaporationPeripheral(EvaporationMultiblockData thermalEvaporation) {
        this.thermalEvaporation = thermalEvaporation;
    }

    public EvaporationMultiblockData getThermalEvaporation() {
        return thermalEvaporation;
    }

    @LuaFunction
    public double getTemperature(){
        return thermalEvaporation.getTemp();
    }

    @LuaFunction
    public int getInputCapacity() {
        return thermalEvaporation.inputTank.getCapacity();
    }

    @LuaFunction
    public int getOutputCapacity() {
        return thermalEvaporation.outputTank.getCapacity();
    }

    @LuaFunction
    public int getInputAmount() {
        return thermalEvaporation.inputTank.getFluidAmount();
    }

    @LuaFunction
    public int getOutputAmount() {
        return thermalEvaporation.outputTank.getFluidAmount();
    }

    @LuaFunction
    public double getProductionRate() {
        return thermalEvaporation.lastGain;
    }

    @NotNull
    @Override
    public String getType() {
        return "thermal_evaporation";
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if(iPeripheral instanceof ThermalEvaporationPeripheral){
            ThermalEvaporationPeripheral thermalEvaporationPeripheral = (ThermalEvaporationPeripheral) iPeripheral;
            return thermalEvaporationPeripheral.getThermalEvaporation().equals(thermalEvaporation);
        } else return false;
    }
}
