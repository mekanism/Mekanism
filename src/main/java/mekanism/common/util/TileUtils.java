package mekanism.common.util;

import java.util.EnumSet;
import mekanism.api.Action;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

//TODO: Move this and factor out the parts into proper classes. This is mainly just temp to make organization not as needed
public class TileUtils {

    //TODO: when removing receiveGas and drawGas also remove the no longer used methods from GasUtils
    //Returns true if it entered the if statement, basically for use by TileEntityGasTank
    public static boolean receiveGas(ItemStack stack, GasTank tank) {
        if (!stack.isEmpty() && (tank.isEmpty() || tank.getStored() < tank.getCapacity())) {
            tank.fill(GasUtils.removeGas(stack, tank.getType(), tank.getNeeded()), Action.EXECUTE);
            return true;
        }
        return false;
    }

    /**
     * @return True if gas was removed
     */
    public static boolean drawGas(ItemStack stack, GasTank tank, Action action) {
        if (!stack.isEmpty() && !tank.isEmpty()) {
            return !tank.drain(GasUtils.addGas(stack, tank.getStack()), action).isEmpty();
        }
        return false;
    }

    public static void emitGas(TileEntityMekanism tile, GasTank tank, int gasOutput, Direction facing) {
        if (!tank.isEmpty()) {
            GasStack toSend = new GasStack(tank.getStack(), Math.min(tank.getStored(), gasOutput));
            tank.drain(GasUtils.emit(toSend, tile, EnumSet.of(facing)), Action.EXECUTE);
        }
    }
}