package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.fluid.MCFluidStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.RotaryIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ROTARY)
public class RotaryRecipeManager extends MekanismRecipeManager<RotaryRecipe> {

    public static final RotaryRecipeManager INSTANCE = new RotaryRecipeManager();

    private RotaryRecipeManager() {
        super(MekanismRecipeType.ROTARY);
    }

    /**
     * Adds a rotary recipe that converts a fluid into a gas. Rotary Condensers set to Decondensentrating can process this recipe type.
     *
     * @param name       Name of the new recipe.
     * @param fluidInput {@link FluidStackIngredient} representing the input of the recipe.
     * @param gasOutput  {@link ICrTGasStack} representing the output of the recipe.
     *
     * @apiNote It is recommended to use {@link #addRecipe(String, FluidStackIngredient, GasStackIngredient, ICrTGasStack, IFluidStack)} over this method in combination
     * with {@link #addRecipe(String, GasStackIngredient, IFluidStack)} if the conversion will be possible in both directions.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, FluidStackIngredient fluidInput, ICrTGasStack gasOutput) {
        addRecipe(makeRecipe(getAndValidateName(name), fluidInput, gasOutput));
    }

    /**
     * Adds a rotary recipe that converts a gas into a fluid. Rotary Condensers set to Condensentrating can process this recipe type.
     *
     * @param name        Name of the new recipe.
     * @param gasInput    {@link GasStackIngredient} representing the input of the recipe.
     * @param fluidOutput {@link IFluidStack} representing the output of the recipe.
     *
     * @apiNote It is recommended to use {@link #addRecipe(String, FluidStackIngredient, GasStackIngredient, ICrTGasStack, IFluidStack)} over this method in combination
     * with {@link #addRecipe(String, FluidStackIngredient, ICrTGasStack)} if the conversion will be possible in both directions.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, GasStackIngredient gasInput, IFluidStack fluidOutput) {
        addRecipe(makeRecipe(getAndValidateName(name), gasInput, fluidOutput));
    }

    /**
     * Adds a rotary recipe that is capable of converting a fluid into a gas and a gas into a fluid. Rotary Condensers can process this recipe type, converting from fluid
     * to gas when set to Decondensentrating and converting from gas to fluid when set to Condensentrating.
     *
     * @param name        Name of the new recipe.
     * @param fluidInput  {@link FluidStackIngredient} representing the input of the recipe when converting from a fluid to a gas.
     * @param gasInput    {@link GasStackIngredient} representing the input of the recipe when converting from a gas to a fluid.
     * @param gasOutput   {@link ICrTGasStack} representing the output of the recipe when converting from a fluid to a gas.
     * @param fluidOutput {@link IFluidStack} representing the output of the recipe when converting from a gas to a fluid.
     *
     * @apiNote It is recommended to use this method over using {@link #addRecipe(String, FluidStackIngredient, ICrTGasStack)} and
     * {@link #addRecipe(String, GasStackIngredient, IFluidStack)} in combination if the conversion will be possible in both directions.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, FluidStackIngredient fluidInput, GasStackIngredient gasInput, ICrTGasStack gasOutput, IFluidStack fluidOutput) {
        addRecipe(makeRecipe(getAndValidateName(name), fluidInput, gasInput, gasOutput, fluidOutput));
    }

    /**
     * Creates a rotary recipe that converts a fluid into a gas.
     *
     * @param id         Name of the new recipe.
     * @param fluidInput {@link FluidStackIngredient} representing the input of the recipe.
     * @param gasOutput  {@link ICrTGasStack} representing the output of the recipe. Will be validated as not empty.
     */
    public final RotaryRecipe makeRecipe(ResourceLocation id, FluidStackIngredient fluidInput, ICrTGasStack gasOutput) {
        return new RotaryIRecipe(id, fluidInput, getAndValidateNotEmpty(gasOutput));
    }

    /**
     * Creates a rotary recipe that converts a gas into a fluid.
     *
     * @param id          Name of the new recipe.
     * @param gasInput    {@link GasStackIngredient} representing the input of the recipe.
     * @param fluidOutput {@link IFluidStack} representing the output of the recipe. Will be validated as not empty.
     */
    public final RotaryRecipe makeRecipe(ResourceLocation id, GasStackIngredient gasInput, IFluidStack fluidOutput) {
        return new RotaryIRecipe(id, gasInput, getAndValidateNotEmpty(fluidOutput));
    }

    /**
     * Creates a rotary recipe that is capable of converting a fluid into a gas and a gas into a fluid.
     *
     * @param id          Name of the new recipe.
     * @param fluidInput  {@link FluidStackIngredient} representing the input of the recipe when converting from a fluid to a gas.
     * @param gasInput    {@link GasStackIngredient} representing the input of the recipe when converting from a gas to a fluid.
     * @param gasOutput   {@link ICrTGasStack} representing the output of the recipe when converting from a fluid to a gas. Will be validated as not empty.
     * @param fluidOutput {@link IFluidStack} representing the output of the recipe when converting from a gas to a fluid. Will be validated as not empty.
     */
    public final RotaryRecipe makeRecipe(ResourceLocation id, FluidStackIngredient fluidInput, GasStackIngredient gasInput, ICrTGasStack gasOutput, IFluidStack fluidOutput) {
        return new RotaryIRecipe(id, fluidInput, gasInput, getAndValidateNotEmpty(gasOutput), getAndValidateNotEmpty(fluidOutput));
    }

    @Override
    protected ActionAddMekanismRecipe getAction(RotaryRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                StringBuilder builder = new StringBuilder();
                if (recipe.hasFluidToGas()) {
                    builder.append(CrTUtils.describeOutputs(recipe.getGasOutputDefinition()))
                          .append(" for fluid to gas");
                }
                if (recipe.hasGasToFluid()) {
                    if (recipe.hasFluidToGas()) {
                        builder.append(" and ");
                    }
                    builder.append(CrTUtils.describeOutputs(recipe.getFluidOutputDefinition(), MCFluidStack::new))
                          .append(" for gas to fluid");
                }
                return builder.toString();
            }
        };
    }
}