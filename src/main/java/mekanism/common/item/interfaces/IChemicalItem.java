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

public interface IChemicalItem {

    @NotNull
    default ChemicalStack useChemical(ItemStack stack, long amount) {
        IChemicalHandler chemicalHandlerItem = Capabilities.CHEMICAL.getCapability(stack);
        if (chemicalHandlerItem != null) {
            if (chemicalHandlerItem instanceof IMekanismChemicalHandler chemicalHandler) {
                //TODO: If we end up having more tanks than one in any IChemicalItem's just kill off this if branch
                IChemicalTank chemicalTank = chemicalHandler.getChemicalTank(0, null);
                if (chemicalTank != null) {
                    //Should always reach here
                    return chemicalTank.extract(amount, Action.EXECUTE, AutomationType.MANUAL);
                }
            }
            return chemicalHandlerItem.extractChemical(amount, Action.EXECUTE);
        }
        return ChemicalStack.EMPTY;
    }

    default boolean hasChemical(ItemStack stack) {
        return ChemicalUtil.hasAnyChemical(stack);
    }
}