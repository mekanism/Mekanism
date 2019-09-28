package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.common.Mekanism;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.infuser")
public class Infuser {

    public static final String NAME = Mekanism.MOD_NAME + " Metallurgic Infuser";

    //TODO: CrT Integration
    /*@ZenCodeType.Method
    public static void addRecipe(String infuseType, int infuseAmount, IIngredient ingredientInput, IItemStack itemOutput) {
        if (infuseType == null || infuseType.isEmpty()) {
            CraftTweakerAPI.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, itemOutput)) {
            //TODO: Use bracket handler instead of string for infuseType, and then clean this stuff up
            InfuseType type = InfuseType.getFromRegistry(new ResourceLocation(infuseType));
            if (!type.isEmptyType()) {
                CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.METALLURGIC_INFUSER, new MetallurgicInfuserRecipe(
                      IngredientHelper.toIngredient(ingredientInput), InfusionIngredient.from(type, infuseAmount), IngredientHelper.getItemStack(itemOutput))));
            }
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
    }*/
}