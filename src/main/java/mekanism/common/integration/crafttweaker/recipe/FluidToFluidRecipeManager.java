package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.impl.fluid.MCFluidStack;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.ingredient.CrTFluidStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.FluidToFluidIRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_FLUID_TO_FLUID)
public abstract class FluidToFluidRecipeManager extends MekanismRecipeManager<FluidToFluidRecipe> {

    protected FluidToFluidRecipeManager(MekanismRecipeType<FluidToFluidRecipe> recipeType) {
        super(recipeType);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTFluidStackIngredient fluidInput, IFluidStack output) {
        addRecipe(makeRecipe(getAndValidateName(name), fluidInput.getInternal(), getAndValidateNotEmpty(output)));
    }

    protected abstract FluidToFluidIRecipe makeRecipe(ResourceLocation id, FluidStackIngredient input, FluidStack output);

    @Override
    protected ActionAddMekanismRecipe getAction(FluidToFluidRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return new MCFluidStack(getRecipe().getOutputRepresentation()).toString();
            }
        };
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_EVAPORATING)
    public static class EvaporatingRecipeManager extends FluidToFluidRecipeManager {

        public static final EvaporatingRecipeManager INSTANCE = new EvaporatingRecipeManager();

        private EvaporatingRecipeManager() {
            super(MekanismRecipeType.EVAPORATING);
        }

        @Override
        protected FluidToFluidIRecipe makeRecipe(ResourceLocation id, FluidStackIngredient input, FluidStack output) {
            return new FluidToFluidIRecipe(id, input, output);
        }
    }
}