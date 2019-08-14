package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.gas.GasStack;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.integration.crafttweaker.helpers.GasHelper;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.IngredientWrapper;
import mekanism.common.integration.crafttweaker.util.RemoveAllMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.PressurizedInput;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mekanism.common.recipe.outputs.PressurizedOutput;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.reaction")
public class Reaction {

    public static final String NAME = Mekanism.MOD_NAME + " Reaction";

    @ZenCodeType.Method
    public static void addRecipe(IIngredient ingredientInput, ILiquidStack liquidInput, IGasStack gasInput, IItemStack itemOutput, IGasStack gasOutput, double energy, int duration) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, liquidInput, gasInput, itemOutput, gasOutput)) {
            FluidStack fluidInput = IngredientHelper.toFluid(liquidInput);
            GasStack gas = GasHelper.toGas(gasInput);
            PressurizedOutput output = new PressurizedOutput(IngredientHelper.getItemStack(itemOutput), GasHelper.toGas(gasOutput));
            List<PressurizedRecipe> recipes = new ArrayList<>();
            for (IItemStack stack : ingredientInput.getItems()) {
                recipes.add(new PressurizedRecipe(new PressurizedInput(stack.getInternal(), fluidInput, gas), output, energy, duration));
            }
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.PRESSURIZED_REACTION_CHAMBER, recipes));
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient itemOutput, IIngredient gasOutput, @ZenCodeType.Optional IIngredient itemInput, @ZenCodeType.Optional IIngredient liquidInput,
          @ZenCodeType.Optional IIngredient gasInput) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput, gasOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.PRESSURIZED_REACTION_CHAMBER, new IngredientWrapper(itemOutput, gasOutput),
                  new IngredientWrapper(itemInput, liquidInput, gasInput)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.PRESSURIZED_REACTION_CHAMBER));
    }
}