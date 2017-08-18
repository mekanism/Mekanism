package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.mtlib.helpers.InputHelper;
import com.blamejared.mtlib.helpers.LogHelper;
import com.blamejared.mtlib.helpers.StackHelper;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IngredientAny;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.CrusherRecipe;
import mekanism.common.recipe.machines.MachineRecipe;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;
import java.util.Map;

@ZenClass("mods.mekanism.enrichment")
@ModOnly("mtlib")
@ZenRegister
public class Enrichment
{
    public static final String NAME = "Mekanism Enrichment Chamber";

    @ZenMethod
    public static void addRecipe(IItemStack itemInput, IItemStack itemOutput)
    {
        if (itemInput == null || itemOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s recipe.", NAME));
            return;
        }

        CrusherRecipe recipe = new CrusherRecipe(InputHelper.toStack(itemInput), InputHelper.toStack(itemOutput));

        CraftTweakerAPI.apply(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.ENRICHMENT_CHAMBER.get(), recipe));
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemOutput, @Optional IIngredient itemInput)
    {
        if(itemOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s recipe.", NAME));
        }

        if(itemInput == null) itemInput = IngredientAny.INSTANCE;

        Map<MachineInput, MachineRecipe> recipes = new HashMap<>();

        for(Map.Entry<ItemStackInput, CrusherRecipe> entry : ((Map<ItemStackInput, CrusherRecipe>) RecipeHandler.Recipe.ENRICHMENT_CHAMBER.get()).entrySet())
        {
            IItemStack inputItem = InputHelper.toIItemStack(entry.getKey().ingredient);
            IItemStack outputItem = InputHelper.toIItemStack(entry.getValue().recipeOutput.output);

            if(!StackHelper.matches(itemOutput, outputItem)) continue;
            if(!StackHelper.matches(itemInput, inputItem)) continue;

            recipes.put(entry.getKey(), entry.getValue());
        }

        if(!recipes.isEmpty())
        {
            CraftTweakerAPI.apply(new RemoveMekanismRecipe(NAME, RecipeHandler.Recipe.CRUSHER.get(), recipes));
        }
        else
        {
            LogHelper.logWarning(String.format("No %s recipe found for %s and %s. Command ignored!", NAME, itemInput.toString(), itemOutput.toString()));
        }
    }
}
