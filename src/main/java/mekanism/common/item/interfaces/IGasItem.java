package mekanism.common.item.interfaces;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasHandler.IMekanismGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IGasItem {

    @NotNull
    default GasStack useGas(ItemStack stack, long amount) {
        IGasHandler gasHandlerItem = Capabilities.GAS.getCapability(stack);
        if (gasHandlerItem != null) {
            if (gasHandlerItem instanceof IMekanismGasHandler gasHandler) {
                //TODO: If we end up having more tanks than one in any IGasItem's just kill off this if branch
                IGasTank gasTank = gasHandler.getChemicalTank(0, null);
                if (gasTank != null) {
                    //Should always reach here
                    return gasTank.extract(amount, Action.EXECUTE, AutomationType.MANUAL);
                }
            }
            return gasHandlerItem.extractChemical(amount, Action.EXECUTE);
        }
        return GasStack.EMPTY;
    }

    default boolean hasGas(ItemStack stack) {
        return ChemicalUtil.hasGas(stack);
    }
}