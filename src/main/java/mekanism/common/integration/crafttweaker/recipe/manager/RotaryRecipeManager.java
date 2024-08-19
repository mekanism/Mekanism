package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.basic.BasicRotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.RotaryRecipeInput;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.MekanismRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ROTARY)
public class RotaryRecipeManager extends MekanismRecipeManager<RotaryRecipeInput, RotaryRecipe> {

    public static final RotaryRecipeManager INSTANCE = new RotaryRecipeManager();

    private RotaryRecipeManager() {
        super(MekanismRecipeType.ROTARY);
    }

    /**
     * Adds a rotary recipe that converts a fluid into a chemical. Rotary Condensers set to Decondensentrating can process this recipe type.
     *
     * @param name           Name of the new recipe.
     * @param fluidInput     {@link CTFluidIngredient} representing the input of the recipe.
     * @param chemicalOutput {@link ICrTChemicalStack} representing the output of the recipe.
     *
     * @apiNote It is recommended to use {@link #addRecipe(String, CTFluidIngredient, ChemicalStackIngredient, ICrTChemicalStack, IFluidStack)} over this method in
     * combination with {@link #addRecipe(String, ChemicalStackIngredient, IFluidStack)} if the conversion will be possible in both directions.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, CTFluidIngredient fluidInput, ICrTChemicalStack chemicalOutput) {
        addRecipe(name, makeRecipe(fluidInput, chemicalOutput));
    }

    /**
     * Adds a rotary recipe that converts a chemical into a fluid. Rotary Condensers set to Condensentrating can process this recipe type.
     *
     * @param name          Name of the new recipe.
     * @param chemicalInput {@link CTFluidIngredient} representing the input of the recipe.
     * @param fluidOutput   {@link IFluidStack} representing the output of the recipe.
     *
     * @apiNote It is recommended to use {@link #addRecipe(String, CTFluidIngredient, ChemicalStackIngredient, ICrTChemicalStack, IFluidStack)} over this method in
     * combination with {@link #addRecipe(String, CTFluidIngredient, ICrTChemicalStack)} if the conversion will be possible in both directions.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ChemicalStackIngredient chemicalInput, IFluidStack fluidOutput) {
        addRecipe(name, makeRecipe(chemicalInput, fluidOutput));
    }

    /**
     * Adds a rotary recipe that is capable of converting a fluid into a chemical and a chemical into a fluid. Rotary Condensers can process this recipe type, converting
     * from fluid to chemical when set to Decondensentrating and converting from chemical to fluid when set to Condensentrating.
     *
     * @param name           Name of the new recipe.
     * @param fluidInput     {@link CTFluidIngredient} representing the input of the recipe when converting from a fluid to a chemical.
     * @param chemicalInput  {@link ChemicalStackIngredient} representing the input of the recipe when converting from a chemical to a fluid.
     * @param chemicalOutput {@link ICrTChemicalStack} representing the output of the recipe when converting from a fluid to a chemical.
     * @param fluidOutput    {@link IFluidStack} representing the output of the recipe when converting from a chemical to a fluid.
     *
     * @apiNote It is recommended to use this method over using {@link #addRecipe(String, CTFluidIngredient, ICrTChemicalStack)} and
     * {@link #addRecipe(String, ChemicalStackIngredient, IFluidStack)} in combination if the conversion will be possible in both directions.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, CTFluidIngredient fluidInput, ChemicalStackIngredient chemicalInput, ICrTChemicalStack chemicalOutput, IFluidStack fluidOutput) {
        addRecipe(name, makeRecipe(fluidInput, chemicalInput, chemicalOutput, fluidOutput));
    }

    /**
     * Creates a rotary recipe that converts a fluid into a chemical.
     *
     * @param fluidInput     {@link CTFluidIngredient} representing the input of the recipe.
     * @param chemicalOutput {@link ICrTChemicalStack} representing the output of the recipe. Will be validated as not empty.
     */
    public final RotaryRecipe makeRecipe(CTFluidIngredient fluidInput, ICrTChemicalStack chemicalOutput) {
        return new BasicRotaryRecipe(CrTUtils.fromCrT(fluidInput), getAndValidateNotEmpty(chemicalOutput));
    }

    /**
     * Creates a rotary recipe that converts a chemical into a fluid.
     *
     * @param chemicalInput {@link ChemicalStackIngredient} representing the input of the recipe.
     * @param fluidOutput   {@link IFluidStack} representing the output of the recipe. Will be validated as not empty.
     */
    public final RotaryRecipe makeRecipe(ChemicalStackIngredient chemicalInput, IFluidStack fluidOutput) {
        return new BasicRotaryRecipe(chemicalInput, getAndValidateNotEmpty(fluidOutput));
    }

    /**
     * Creates a rotary recipe that is capable of converting a fluid into a chemical and a chemical into a fluid.
     *
     * @param fluidInput     {@link CTFluidIngredient} representing the input of the recipe when converting from a fluid to a chemical.
     * @param chemicalInput  {@link ChemicalStackIngredient} representing the input of the recipe when converting from a chemical to a fluid.
     * @param chemicalOutput {@link ICrTChemicalStack} representing the output of the recipe when converting from a fluid to a chemical. Will be validated as not empty.
     * @param fluidOutput    {@link IFluidStack} representing the output of the recipe when converting from a chemical to a fluid. Will be validated as not empty.
     */
    public final RotaryRecipe makeRecipe(CTFluidIngredient fluidInput, ChemicalStackIngredient chemicalInput, ICrTChemicalStack chemicalOutput, IFluidStack fluidOutput) {
        return new BasicRotaryRecipe(CrTUtils.fromCrT(fluidInput), chemicalInput, getAndValidateNotEmpty(chemicalOutput), getAndValidateNotEmpty(fluidOutput));
    }

    @Override
    protected String describeOutputs(RotaryRecipe recipe) {
        StringBuilder builder = new StringBuilder();
        if (recipe.hasFluidToChemical()) {
            builder.append(CrTUtils.describeOutputs(recipe.getChemicalOutputDefinition()))
                  .append(" for fluid to chemical");
        }
        if (recipe.hasChemicalToFluid()) {
            if (recipe.hasFluidToChemical()) {
                builder.append(" and ");
            }
            builder.append(CrTUtils.describeOutputs(recipe.getFluidOutputDefinition(), IFluidStack::of))
                  .append(" for chemical to fluid");
        }
        return builder.toString();
    }
}