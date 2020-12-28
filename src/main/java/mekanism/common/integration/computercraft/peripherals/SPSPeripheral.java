package mekanism.common.integration.computercraft.peripherals;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.content.tank.TankMultiblockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SPSPeripheral implements IPeripheral {
    private SPSMultiblockData sps;

    public SPSPeripheral(SPSMultiblockData sps) {
        this.sps = sps;
    }

    public SPSMultiblockData getSPS() {
        return sps;
    }

    @LuaFunction
    public long getInputCapacity() {
        return sps.inputTank.getCapacity();
    }

    @LuaFunction
    public long getOutputCapacity() {
        return sps.outputTank.getCapacity();
    }

    @LuaFunction
    public long getInputAmount() {
        return sps.inputTank.getStored();
    }

    @LuaFunction
    public long getOutputAmount() {
        return sps.outputTank.getStored();
    }

    @LuaFunction
    public double getProgress(){
        return sps.progress;
    }

    @NotNull
    @Override
    public String getType() {
        return "sps";
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if(iPeripheral instanceof SPSPeripheral){
            SPSPeripheral spsPeripheral = (SPSPeripheral) iPeripheral;
            return spsPeripheral.getSPS().equals(sps);
        } else return false;
    }
}
