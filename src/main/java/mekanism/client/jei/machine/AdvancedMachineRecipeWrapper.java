package mekanism.client.jei.machine;

import java.util.List;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.util.GasUtils;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

public class AdvancedMachineRecipeWrapper implements IRecipeWrapper {

    private final AdvancedMachineRecipe recipe;

    public AdvancedMachineRecipeWrapper(AdvancedMachineRecipe r) {
        recipe = r;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, ((AdvancedMachineInput) recipe.getInput()).itemStack);
        ingredients.setInput(MekanismJEI.TYPE_GAS, new GasStack(((AdvancedMachineInput) recipe.getInput()).gasType,
              TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED
                    * TileEntityAdvancedElectricMachine.BASE_GAS_PER_TICK));
        ingredients.setOutput(VanillaTypes.ITEM, ((ItemStackOutput) recipe.getOutput()).output);
    }

    public List<ItemStack> getFuelStacks(Gas gasType) {
        return GasUtils.getStacksForGas(gasType);
    }

    public AdvancedMachineRecipe getRecipe() {
        return recipe;
    }
}