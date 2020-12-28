package mekanism.common.integration.computercraft.peripherals;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import mekanism.common.content.tank.TankMultiblockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DynamicTankPeripheral implements IPeripheral {
    private TankMultiblockData tank;

    public DynamicTankPeripheral(TankMultiblockData tank) {
        this.tank = tank;
    }

    public TankMultiblockData getTank() {
        return tank;
    }

    @LuaFunction
    public int getCapacity() {
        return tank.getTankCapacity();
    }

    @LuaFunction
    public long getAmount() {
        switch (tank.mergedTank.getCurrentType()) {
            case FLUID:
                return tank.getFluidTank().getFluidAmount();
            case GAS:
                return tank.getGasTank().getStored();
            case INFUSION:
                return tank.getInfusionTank().getStored();
            case PIGMENT:
                return tank.getPigmentTank().getStored();
            case SLURRY:
                return tank.getSlurryTank().getStored();
        }
        return 0;
    }

    @NotNull
    @Override
    public String getType() {
        return "dynamic_tank";
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if(iPeripheral instanceof DynamicTankPeripheral){
            DynamicTankPeripheral tankPeripheral = (DynamicTankPeripheral) iPeripheral;
            return tankPeripheral.getTank().equals(this.tank);
        } else return false;
    }
}
