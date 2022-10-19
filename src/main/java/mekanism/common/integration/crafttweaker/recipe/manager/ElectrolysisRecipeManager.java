package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.ElectrolysisIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_SEPARATING)
public class ElectrolysisRecipeManager extends MekanismRecipeManager<ElectrolysisRecipe> {

    public static final ElectrolysisRecipeManager INSTANCE = new ElectrolysisRecipeManager();

    private ElectrolysisRecipeManager() {
        super(MekanismRecipeType.SEPARATING);
    }

    /**
     * Adds a separating recipe that separates a fluid into two gases. Electrolytic Separators can process this recipe type.
     *
     * @param name             Name of the new recipe.
     * @param input            {@link FluidStackIngredient} representing the input of the recipe.
     * @param leftGasOutput    {@link ICrTGasStack} representing the left output of the recipe.
     * @param rightGasOutput   {@link ICrTGasStack} representing the right output of the recipe.
     * @param energyMultiplier Optional value representing the multiplier to the energy cost in relation to the configured hydrogen separating energy cost. If this is not
     *                         specified it will default to one. If this value is specified it must be greater than or equal to one.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, FluidStackIngredient input, ICrTGasStack leftGasOutput, ICrTGasStack rightGasOutput,
          @ZenCodeType.Optional("1 as " + CrTConstants.CLASS_FLOATING_LONG) FloatingLong energyMultiplier) {
        addRecipe(makeRecipe(getAndValidateName(name), input, leftGasOutput, rightGasOutput, energyMultiplier));
    }

    /**
     * Creates a separating recipe that separates a fluid into two gases.
     *
     * @param id               Name of the new recipe.
     * @param input            {@link FluidStackIngredient} representing the input of the recipe.
     * @param leftGasOutput    {@link ICrTGasStack} representing the left output of the recipe. Will be validated as not empty.
     * @param rightGasOutput   {@link ICrTGasStack} representing the right output of the recipe. Will be validated as not empty.
     * @param energyMultiplier Value representing the multiplier to the energy cost in relation to the configured hydrogen separating energy cost. Will be validated to be
     *                         greater than or equal to one.
     */
    public final ElectrolysisRecipe makeRecipe(ResourceLocation id, FluidStackIngredient input, ICrTGasStack leftGasOutput, ICrTGasStack rightGasOutput,
          FloatingLong energyMultiplier) {
        if (energyMultiplier.smallerThan(FloatingLong.ONE)) {
            throw new IllegalArgumentException("Energy multiplier must be at least one! Multiplier: " + energyMultiplier);
        }
        return new ElectrolysisIRecipe(id, input, energyMultiplier.copyAsConst(), getAndValidateNotEmpty(leftGasOutput), getAndValidateNotEmpty(rightGasOutput));
    }

    @Override
    protected ActionAddMekanismRecipe getAction(ElectrolysisRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(recipe.getOutputDefinition(), output -> new CrTGasStack(output.left()) + " and " + new CrTGasStack(output.right()));
            }
        };
    }
}