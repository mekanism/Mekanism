package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.basic.BasicPressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.vanilla_input.ReactionRecipeInput;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_REACTION)
public class PressurizedReactionRecipeManager extends MekanismRecipeManager<ReactionRecipeInput, PressurizedReactionRecipe> {

    //TODO: If https://github.com/ZenCodeLang/ZenCode/issues/31 gets fixed switch the addRecipe methods in this class to using ZC optional
    public static final PressurizedReactionRecipeManager INSTANCE = new PressurizedReactionRecipeManager();

    private PressurizedReactionRecipeManager() {
        super(MekanismRecipeType.REACTION);
    }

    /**
     * Adds a reaction recipe that converts an item, fluid, and gas into another item. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param name           Name of the new recipe.
     * @param inputSolid     {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param inputFluid     {@link CTFluidIngredient} representing the fluid input of the recipe.
     * @param inputGas       {@link GasStackIngredient} representing the gas input of the recipe.
     * @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem     {@link IItemStack} representing the item output of the recipe.
     * @param energyRequired Value representing how much "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the
     *                       recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount inputSolid, CTFluidIngredient inputFluid, GasStackIngredient inputGas, int duration, IItemStack outputItem,
          long energyRequired) {
        addRecipe(name, inputSolid, inputFluid, inputGas, duration, getAndValidateNotEmpty(outputItem), GasStack.EMPTY, energyRequired);
    }

    /**
     * Adds a reaction recipe that converts an item, fluid, and gas into another item. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param name       Name of the new recipe.
     * @param inputSolid {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param inputFluid {@link CTFluidIngredient} representing the fluid input of the recipe.
     * @param inputGas   {@link GasStackIngredient} representing the gas input of the recipe.
     * @param duration   Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem {@link IItemStack} representing the item output of the recipe.
     *
     * @apiNote {@code energyRequired} (the amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe),
     * will default to zero.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount inputSolid, CTFluidIngredient inputFluid, GasStackIngredient inputGas, int duration, IItemStack outputItem) {
        addRecipe(name, inputSolid, inputFluid, inputGas, duration, getAndValidateNotEmpty(outputItem), GasStack.EMPTY, 0);
    }

    /**
     * Adds a reaction recipe that converts an item, fluid, and gas into another gas. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param name           Name of the new recipe.
     * @param inputSolid     {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param inputFluid     {@link CTFluidIngredient} representing the fluid input of the recipe.
     * @param inputGas       {@link GasStackIngredient} representing the gas input of the recipe.
     * @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputGas      {@link ICrTGasStack} representing the gas output of the recipe.
     * @param energyRequired Value representing how much "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the
     *                       recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount inputSolid, CTFluidIngredient inputFluid, GasStackIngredient inputGas, int duration, ICrTGasStack outputGas,
          long energyRequired) {
        addRecipe(name, inputSolid, inputFluid, inputGas, duration, ItemStack.EMPTY, getAndValidateNotEmpty(outputGas), energyRequired);
    }

    /**
     * Adds a reaction recipe that converts an item, fluid, and gas into another gas. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param name       Name of the new recipe.
     * @param inputSolid {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param inputFluid {@link CTFluidIngredient} representing the fluid input of the recipe.
     * @param inputGas   {@link GasStackIngredient} representing the gas input of the recipe.
     * @param duration   Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputGas  {@link ICrTGasStack} representing the gas output of the recipe.
     *
     * @apiNote {@code energyRequired} (the amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe),
     * will default to zero.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount inputSolid, CTFluidIngredient inputFluid, GasStackIngredient inputGas, int duration, ICrTGasStack outputGas) {
        addRecipe(name, inputSolid, inputFluid, inputGas, duration, ItemStack.EMPTY, getAndValidateNotEmpty(outputGas), 0);
    }

    /**
     * Adds a reaction recipe that converts an item, fluid, and gas into another item and gas. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param name           Name of the new recipe.
     * @param inputSolid     {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param inputFluid     {@link CTFluidIngredient} representing the fluid input of the recipe.
     * @param inputGas       {@link GasStackIngredient} representing the gas input of the recipe.
     * @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem     {@link IItemStack} representing the item output of the recipe.
     * @param outputGas      {@link ICrTGasStack} representing the gas output of the recipe.
     * @param energyRequired Value representing how much "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the
     *                       recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount inputSolid, CTFluidIngredient inputFluid, GasStackIngredient inputGas, int duration, IItemStack outputItem,
          ICrTGasStack outputGas, long energyRequired) {
        addRecipe(name, inputSolid, inputFluid, inputGas, duration, getAndValidateNotEmpty(outputItem), getAndValidateNotEmpty(outputGas), energyRequired);
    }

    /**
     * Adds a reaction recipe that converts an item, fluid, and gas into another item and gas. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param name       Name of the new recipe.
     * @param inputSolid {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param inputFluid {@link CTFluidIngredient} representing the fluid input of the recipe.
     * @param inputGas   {@link GasStackIngredient} representing the gas input of the recipe.
     * @param duration   Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem {@link IItemStack} representing the item output of the recipe.
     * @param outputGas  {@link ICrTGasStack} representing the gas output of the recipe.
     *
     * @apiNote {@code energyRequired} (the amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe),
     * will default to zero.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount inputSolid, CTFluidIngredient inputFluid, GasStackIngredient inputGas, int duration, IItemStack outputItem,
          ICrTGasStack outputGas) {
        addRecipe(name, inputSolid, inputFluid, inputGas, duration, getAndValidateNotEmpty(outputItem), getAndValidateNotEmpty(outputGas), 0);
    }

    private void addRecipe(String name, IIngredientWithAmount inputSolid, CTFluidIngredient inputFluid, GasStackIngredient inputGas, int duration, ItemStack outputItem,
          GasStack outputGas, long energyRequired) {
        addRecipe(name, makeRecipe(inputSolid, inputFluid, inputGas, duration, outputItem, outputGas, energyRequired));
    }

    /**
     * Creates a reaction recipe that converts an item, fluid, and gas into another item and gas. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param inputSolid     {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param inputFluid     {@link CTFluidIngredient} representing the fluid input of the recipe.
     * @param inputGas       {@link GasStackIngredient} representing the gas input of the recipe.
     * @param duration       Base duration in ticks that this recipe takes to complete. Will be validated as being greater than zero.
     * @param outputItem     {@link IItemStack} representing the item output of the recipe. It will be validated that at least one of this and outputGas is not empty.
     * @param outputGas      {@link ICrTGasStack} representing the gas output of the recipe. It will be validated that at least one of this and outputItem is not empty.
     * @param energyRequired Value representing how much "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the
     *                       recipe.
     */
    public PressurizedReactionRecipe makeRecipe(IIngredientWithAmount inputSolid, CTFluidIngredient inputFluid, GasStackIngredient inputGas,
          int duration, ItemStack outputItem, GasStack outputGas, long energyRequired) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive! Duration: " + duration);
        }
        return new BasicPressurizedReactionRecipe(CrTUtils.fromCrT(inputSolid), CrTUtils.fromCrT(inputFluid), inputGas, energyRequired, duration,
              outputItem, outputGas);
    }

    @Override
    protected String describeOutputs(PressurizedReactionRecipe recipe) {
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
}