package mekanism.common.util;

import java.util.EnumSet;
import mekanism.api.Action;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.inventory.AutomationType;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

//TODO: Move this and factor out the parts into proper classes. This is mainly just temp to make organization not as needed
//TODO: GasHandler - when evaluating usages of insert and extract kill off this class
@Deprecated
public class TileUtils {

    //TODO: when removing receiveGas and drawGas also remove the no longer used methods from GasUtils
    //Returns true if it entered the if statement, basically for use by TileEntityGasTank
    @Deprecated
    public static boolean receiveGas(ItemStack stack, IChemicalTank<Gas, GasStack> tank) {
        if (!stack.isEmpty() && (tank.isEmpty() || tank.getStored() < tank.getCapacity())) {
            tank.insert(GasUtils.removeGas(stack, tank.getType(), tank.getNeeded()), Action.EXECUTE, AutomationType.INTERNAL);
            return true;
        }
        return false;
    }

    /**
     * @return True if gas was removed
     */
    @Deprecated
    public static boolean drawGas(ItemStack stack, IChemicalTank<Gas, GasStack> tank, Action action) {
        if (!stack.isEmpty() && !tank.isEmpty()) {
            return !tank.extract(GasUtils.addGas(stack, tank.getStack()), action, AutomationType.INTERNAL).isEmpty();
        }
        return false;
    }

    @Deprecated
    public static void emitGas(TileEntityMekanism tile, IChemicalTank<Gas, GasStack> tank, int gasOutput, Direction facing) {
        if (!tank.isEmpty()) {
            GasStack toSend = new GasStack(tank.getStack(), Math.min(tank.getStored(), gasOutput));
            tank.extract(GasUtils.emit(toSend, tile, EnumSet.of(facing)), Action.EXECUTE, AutomationType.INTERNAL);
        }
    }
}