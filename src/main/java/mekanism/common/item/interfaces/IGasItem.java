package mekanism.common.item.interfaces;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IGasItem {

    @NotNull
    default ChemicalStack useGas(ItemStack stack, long amount) {
        IChemicalHandler gasHandlerItem = Capabilities.CHEMICAL.getCapability(stack);
        if (gasHandlerItem != null) {
            if (gasHandlerItem instanceof IMekanismChemicalHandler gasHandler) {
                //TODO: If we end up having more tanks than one in any IGasItem's just kill off this if branch
                IChemicalTank gasTank = gasHandler.getChemicalTank(0, null);
                if (gasTank != null) {
                    //Should always reach here
                    return gasTank.extract(amount, Action.EXECUTE, AutomationType.MANUAL);
                }
            }
            return gasHandlerItem.extractChemical(amount, Action.EXECUTE);
        }
        return ChemicalStack.EMPTY;
    }

    default boolean hasGas(ItemStack stack) {
        return ChemicalUtil.hasAnyChemical(stack);
    }
}