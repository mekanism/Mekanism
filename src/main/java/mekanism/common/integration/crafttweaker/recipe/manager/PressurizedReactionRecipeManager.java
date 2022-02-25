package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.PressurizedReactionIRecipe;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_REACTION)
public class PressurizedReactionRecipeManager extends MekanismRecipeManager<PressurizedReactionRecipe> {

    public static final PressurizedReactionRecipeManager INSTANCE = new PressurizedReactionRecipeManager();

    private PressurizedReactionRecipeManager() {
        super(MekanismRecipeType.REACTION);
    }

    /**
     * Adds a reaction recipe that converts an item, fluid, and gas into another item. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param name           Name of the new recipe.
     * @param inputSolid     {@link ItemStackIngredient} representing the item input of the recipe.
     * @param inputFluid     {@link FluidStackIngredient} representing the fluid input of the recipe.
     * @param inputGas       {@link GasStackIngredient} representing the gas input of the recipe.
     * @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem     {@link IItemStack} representing the item output of the recipe.
     * @param energyRequired Optional value representing how much "extra" energy this recipe requires, compared to the base energy requirements of the machine performing
     *                       the recipe. Defaults to zero.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, int duration,
          IItemStack outputItem, @ZenCodeType.Optional("0 as " + CrTConstants.CLASS_FLOATING_LONG) FloatingLong energyRequired) {
        addRecipe(name, inputSolid, inputFluid, inputGas, duration, getAndValidateNotEmpty(outputItem), GasStack.EMPTY, energyRequired);
    }

    /**
     * Adds a reaction recipe that converts an item, fluid, and gas into another gas. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param name           Name of the new recipe.
     * @param inputSolid     {@link ItemStackIngredient} representing the item input of the recipe.
     * @param inputFluid     {@link FluidStackIngredient} representing the fluid input of the recipe.
     * @param inputGas       {@link GasStackIngredient} representing the gas input of the recipe.
     * @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputGas      {@link ICrTGasStack} representing the gas output of the recipe.
     * @param energyRequired Optional value representing how much "extra" energy this recipe requires, compared to the base energy requirements of the machine performing
     *                       the recipe. Defaults to zero.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, int duration,
          ICrTGasStack outputGas, @ZenCodeType.Optional("0 as " + CrTConstants.CLASS_FLOATING_LONG) FloatingLong energyRequired) {
        addRecipe(name, inputSolid, inputFluid, inputGas, duration, ItemStack.EMPTY, getAndValidateNotEmpty(outputGas), energyRequired);
    }

    /**
     * Adds a reaction recipe that converts an item, fluid, and gas into another item and gas. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param name           Name of the new recipe.
     * @param inputSolid     {@link ItemStackIngredient} representing the item input of the recipe.
     * @param inputFluid     {@link FluidStackIngredient} representing the fluid input of the recipe.
     * @param inputGas       {@link GasStackIngredient} representing the gas input of the recipe.
     * @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem     {@link IItemStack} representing the item output of the recipe.
     * @param outputGas      {@link ICrTGasStack} representing the gas output of the recipe.
     * @param energyRequired Optional value representing how much "extra" energy this recipe requires, compared to the base energy requirements of the machine performing
     *                       the recipe. Defaults to zero.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, int duration,
          IItemStack outputItem, ICrTGasStack outputGas, @ZenCodeType.Optional("0 as " + CrTConstants.CLASS_FLOATING_LONG) FloatingLong energyRequired) {
        addRecipe(name, inputSolid, inputFluid, inputGas, duration, getAndValidateNotEmpty(outputItem), getAndValidateNotEmpty(outputGas), energyRequired);
    }

    private void addRecipe(String name, ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, int duration,
          ItemStack outputItem, GasStack outputGas, FloatingLong energyRequired) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive! Duration: " + duration);
        }
        addRecipe(new PressurizedReactionIRecipe(getAndValidateName(name), inputSolid, inputFluid, inputGas, energyRequired.copyAsConst(), duration, outputItem, outputGas));
    }

    @Override
    protected ActionAddMekanismRecipe getAction(PressurizedReactionRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(recipe.getOutputDefinition(), output -> {
                    StringBuilder builder = new StringBuilder();
                    ItemStack itemOutput = output.item();
                    if (!itemOutput.isEmpty()) {
                        builder.append(ItemStackUtil.getCommandString(itemOutput));
                    }
                    GasStack gasOutput = output.gas();
                    if (!gasOutput.isEmpty()) {
                        if (!itemOutput.isEmpty()) {
                            builder.append(" and ");
                        }
                        builder.append(new CrTGasStack(gasOutput));
                    }
                    return builder.toString();
                });
            }
        };
    }
}