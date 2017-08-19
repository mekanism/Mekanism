package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.mtlib.helpers.InputHelper;
import com.blamejared.mtlib.helpers.LogHelper;
import com.blamejared.mtlib.helpers.StackHelper;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IngredientAny;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;
import java.util.Map;

@ZenClass("mods.mekanism.infuser")
@ModOnly("mtlib")
@ZenRegister
public class Infuser
{
    public static final String NAME = "Mekanism Metallurgic Infuser";

    @ZenMethod
    public static void addRecipe(String infuseType, int infuseAmount, IItemStack itemInput, IItemStack itemOutput)
    {
        if (itemInput == null || itemOutput == null || infuseType == null || infuseType.isEmpty())
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        InfusionInput input = new InfusionInput(InfuseRegistry.get(infuseType), infuseAmount, InputHelper.toStack(itemInput));
        ItemStackOutput output = new ItemStackOutput(InputHelper.toStack(itemOutput));

        MetallurgicInfuserRecipe recipe = new MetallurgicInfuserRecipe(input, output);

        CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.METALLURGIC_INFUSER.get(), recipe));
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemOutput, @Optional IIngredient itemInput, @Optional String infuseType)
    {
        if (itemOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        if (itemInput == null)
            itemInput = IngredientAny.INSTANCE;
        if (infuseType == null)
            infuseType = "";

        CrafttweakerIntegration.LATE_REMOVALS.add(new Remove(NAME, RecipeHandler.Recipe.METALLURGIC_INFUSER.get(), itemOutput, itemInput, infuseType));
    }

    private static class Remove extends RemoveMekanismRecipe
    {
        private IIngredient itemOutput;
        private IIngredient itemInput;
        private String infuseType;

        public Remove(String name, Map<MachineInput, MachineRecipe> map, IIngredient itemOutput, IIngredient itemInput, String infuseType)
        {
            super(name, map);

            this.itemOutput = itemOutput;
            this.itemInput = itemInput;
            this.infuseType = infuseType;
        }

        @Override
        public void addRecipes()
        {
            Map<MachineInput, MachineRecipe> recipesToRemove = new HashMap<>();

            for (Map.Entry<InfusionInput, MetallurgicInfuserRecipe> entry : ((Map<InfusionInput, MetallurgicInfuserRecipe>) RecipeHandler.Recipe.METALLURGIC_INFUSER.get()).entrySet())
            {
                IItemStack inputItem = InputHelper.toIItemStack(entry.getKey().inputStack);
                String typeInfuse = entry.getKey().infuse.type.name;
                IItemStack outputItem = InputHelper.toIItemStack(entry.getValue().recipeOutput.output);

                if (!StackHelper.matches(itemOutput, outputItem))
                    continue;
                if (!StackHelper.matches(itemInput, inputItem))
                    continue;
                if (!infuseType.isEmpty() && !infuseType.equalsIgnoreCase(typeInfuse))
                    continue;

                recipesToRemove.put(entry.getKey(), entry.getValue());
            }

            if (!recipesToRemove.isEmpty())
            {
                recipes.putAll(recipesToRemove);
            }
            else
            {
                LogHelper.logWarning(String.format("No %s recipe found for %s and %s. Command ignored!", NAME, itemInput.toString(), itemOutput.toString()));
            }
        }
    }
}
