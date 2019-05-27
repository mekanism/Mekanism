package mekanism.client.jei.machine;

import java.util.List;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.GasConversionHandler;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.item.ItemStack;

public class AdvancedMachineRecipeWrapper<RECIPE extends AdvancedMachineRecipe<RECIPE>> extends MekanismRecipeWrapper<RECIPE> {

    public AdvancedMachineRecipeWrapper(RECIPE recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, recipe.getInput().itemStack);
        ingredients.setInput(MekanismJEI.TYPE_GAS, new GasStack(recipe.getInput().gasType,
              TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED * TileEntityAdvancedElectricMachine.BASE_GAS_PER_TICK));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput().output);
    }

    public List<ItemStack> getFuelStacks(Gas gasType) {
        return GasConversionHandler.getStacksForGas(gasType);
    }
}