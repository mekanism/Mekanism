package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.infuse.InfuseType;
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
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.infuser")
public class Infuser {

    public static final String NAME = Mekanism.MOD_NAME + " Metallurgic Infuser";

    @ZenCodeType.Method
    public static void addRecipe(String infuseType, int infuseAmount, IIngredient ingredientInput, IItemStack itemOutput) {
        if (infuseType == null || infuseType.isEmpty()) {
            CraftTweakerAPI.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, itemOutput)) {
            //TODO: Use bracket handler instead of string for infuseType
            InfuseType type = MekanismAPI.INFUSE_TYPE_REGISTRY.getValue(new ResourceLocation(infuseType));
            ItemStackOutput output = new ItemStackOutput(IngredientHelper.getItemStack(itemOutput));
            List<MetallurgicInfuserRecipe> recipes = new ArrayList<>();
            for (IItemStack stack : ingredientInput.getItems()) {
                recipes.add(new MetallurgicInfuserRecipe(new InfusionInput(type, infuseAmount, stack.getInternal()), output));
            }
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.METALLURGIC_INFUSER, recipes));
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient itemOutput, @ZenCodeType.Optional IIngredient itemInput, @ZenCodeType.Optional String infuseType) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.METALLURGIC_INFUSER, new IngredientWrapper(itemOutput),
                  new IngredientWrapper(itemInput, infuseType)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.METALLURGIC_INFUSER));
    }
}