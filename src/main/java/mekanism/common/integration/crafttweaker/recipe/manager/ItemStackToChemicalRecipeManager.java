package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ITEM_STACK_TO_CHEMICAL)
public abstract class ItemStackToChemicalRecipeManager<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>, RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>> extends MekanismRecipeManager<RECIPE> {

    protected ItemStackToChemicalRecipeManager(IMekanismRecipeTypeProvider<RECIPE, ?> recipeType) {
        super(recipeType);
    }

    /**
     * Adds a recipe that an item into a chemical.
     * <br>
     * If this is called from the gas conversion recipe manager, this will be a gas conversion recipe and be able to be used in any slots in Mekanism machines that are
     * able to convert items to gases, for example in the Osmium Compressor and a variety of other machines.
     * <br>
     * If this is called from the oxidizing recipe manager, this will be an oxidizing recipe. Chemical Oxidizers can process this recipe type.
     * <br>
     * If this is called from the infusion conversion recipe manager, this will be an infusion conversion recipe and be able to be used in any slots in Mekanism machines
     * that are able to convert items to infuse types, for example in the Metallurgic Infuser and in Infusing Factories.
     * <br>
     * If this is called from the pigment extracting recipe manager, this will be a pigment extracting recipe. Pigment Extractors can process this recipe type.
     *
     * @param name   Name of the new recipe.
     * @param input  {@link ItemStackIngredient} representing the input of the recipe.
     * @param output Chemical stack representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient input, CRT_STACK output) {
        addRecipe(makeRecipe(getAndValidateName(name), input, output));
    }

    /**
     * Creates a recipe that an item into a chemical.
     *
     * @param id     Name of the new recipe.
     * @param input  {@link ItemStackIngredient} representing the input of the recipe.
     * @param output Chemical stack representing the output of the recipe. Will be validated as not empty.
     */
    public final RECIPE makeRecipe(ResourceLocation id, ItemStackIngredient input, CRT_STACK output) {
        return makeRecipe(id, input, getAndValidateNotEmpty(output));
    }

    protected abstract RECIPE makeRecipe(ResourceLocation id, ItemStackIngredient input, STACK output);

    @Override
    protected ActionAddMekanismRecipe getAction(RECIPE recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(recipe.getOutputDefinition());
            }
        };
    }
}