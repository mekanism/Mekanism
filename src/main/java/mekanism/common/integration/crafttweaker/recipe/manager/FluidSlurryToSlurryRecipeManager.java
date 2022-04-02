package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTSlurryStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.FluidSlurryToSlurryIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_FLUID_SLURRY_TO_SLURRY)
public abstract class FluidSlurryToSlurryRecipeManager extends MekanismRecipeManager<FluidSlurryToSlurryRecipe> {

    protected FluidSlurryToSlurryRecipeManager(IMekanismRecipeTypeProvider<FluidSlurryToSlurryRecipe, ?> recipeType) {
        super(recipeType);
    }

    /**
     * Adds a recipe that converts a fluid and slurry to another slurry.
     * <br>
     * If this is called from the washing recipe manager, this will be a washing recipe and able to be processed in a chemical washer.
     *
     * @param name        Name of the new recipe.
     * @param fluidInput  {@link FluidStackIngredient} representing the fluid input of the recipe.
     * @param slurryInput {@link SlurryStackIngredient} representing the slurry input of the recipe.
     * @param output      {@link ICrTSlurryStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, FluidStackIngredient fluidInput, SlurryStackIngredient slurryInput, ICrTSlurryStack output) {
        addRecipe(makeRecipe(getAndValidateName(name), fluidInput, slurryInput, getAndValidateNotEmpty(output)));
    }

    protected abstract FluidSlurryToSlurryRecipe makeRecipe(ResourceLocation id, FluidStackIngredient fluidInput, SlurryStackIngredient slurryInput, SlurryStack output);

    @Override
    protected ActionAddMekanismRecipe getAction(FluidSlurryToSlurryRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(recipe.getOutputDefinition(), CrTSlurryStack::new);
            }
        };
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_WASHING)
    public static class ChemicalWasherRecipeManager extends FluidSlurryToSlurryRecipeManager {

        public static final ChemicalWasherRecipeManager INSTANCE = new ChemicalWasherRecipeManager();

        private ChemicalWasherRecipeManager() {
            super(MekanismRecipeType.WASHING);
        }

        @Override
        protected FluidSlurryToSlurryRecipe makeRecipe(ResourceLocation id, FluidStackIngredient fluidInput, SlurryStackIngredient slurryInput, SlurryStack output) {
            return new FluidSlurryToSlurryIRecipe(id, fluidInput, slurryInput, output);
        }
    }
}