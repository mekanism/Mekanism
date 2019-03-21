package mekanism.common.integration.crafttweaker.handlers;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.IngredientWrapper;
import mekanism.common.integration.crafttweaker.util.RemoveAllMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.infuser")
@ZenRegister
public class Infuser {

    public static final String NAME = Mekanism.MOD_NAME + " Metallurgic Infuser";

    @ZenMethod
    public static void addRecipe(String infuseType, int infuseAmount, IItemStack itemInput, IItemStack itemOutput) {
        if (infuseType == null || infuseType.isEmpty()) {
            CraftTweakerAPI.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }
        if (IngredientHelper.checkNotNull(NAME, itemInput, itemOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS
                  .add(new AddMekanismRecipe<>(NAME, Recipe.METALLURGIC_INFUSER,
                        new MetallurgicInfuserRecipe(new InfusionInput(InfuseRegistry.get(infuseType), infuseAmount,
                              IngredientHelper.toStack(itemInput)),
                              new ItemStackOutput(IngredientHelper.toStack(itemOutput)))));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemOutput, @Optional IIngredient itemInput,
          @Optional String infuseType) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS
                  .add(new RemoveMekanismRecipe<>(NAME, Recipe.METALLURGIC_INFUSER, new IngredientWrapper(itemOutput),
                        new IngredientWrapper(itemInput, infuseType)));
        }
    }

    @ZenMethod
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.CHEMICAL_CRYSTALLIZER));
    }
}