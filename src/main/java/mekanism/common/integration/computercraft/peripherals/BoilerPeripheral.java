package mekanism.common.integration.computercraft.peripherals;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.util.HeatUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoilerPeripheral implements IPeripheral {
    private BoilerMultiblockData boiler;

    public BoilerPeripheral(BoilerMultiblockData boiler) {
        this.boiler = boiler;
    }

    public BoilerMultiblockData getBoiler() {
        return boiler;
    }

    @LuaFunction
    public long getHeatedCoolantCapacity(){
        return boiler.superheatedCoolantTank.getCapacity();
    }

    @LuaFunction
    public long getHeatedCoolantAmount(){
        return boiler.superheatedCoolantTank.getStored();
    }

    @LuaFunction
    public long getCoolantCapacity(){
        return boiler.cooledCoolantTank.getCapacity();
    }

    @LuaFunction
    public long getCoolantAmount(){
        return boiler.cooledCoolantTank.getStored();
    }

    @LuaFunction
    public long getWaterCapacity(){
        return boiler.waterTank.getCapacity();
    }

    @LuaFunction
    public int getWaterAmount(){
        return boiler.waterTank.getFluidAmount();
    }

    @LuaFunction
    public long getSteamCapacity(){
        return boiler.steamTank.getCapacity();
    }

    @LuaFunction
    public long getSteamAmount(){
        return boiler.steamTank.getStored();
    }

    @LuaFunction
    public double getTemperature(){
        return boiler.getTotalTemperature();
    }

    @LuaFunction
    public double getBoilRate(){
        return boiler.lastBoilRate;
    }

    @LuaFunction
    public double getMaxBoil(){
        return boiler.lastMaxBoil;
    }

    @LuaFunction
    public double getBoilCapacity(){
        return (MekanismConfig.general.superheatingHeatTransfer.get() * boiler.superheatingElements / HeatUtils.getWaterThermalEnthalpy()) * HeatUtils.getSteamEnergyEfficiency();
    }

    @NotNull
    @Override
    public String getType() {
        return "boiler";
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if(iPeripheral instanceof BoilerPeripheral){
            BoilerPeripheral boilerPeripheral = (BoilerPeripheral) iPeripheral;
            return boilerPeripheral.getBoiler().equals(this.boiler);
        } else return false;
    }
}
