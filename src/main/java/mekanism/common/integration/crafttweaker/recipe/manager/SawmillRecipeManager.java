package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import com.blamejared.crafttweaker.api.util.random.Percentaged;
import java.util.List;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.basic.BasicSawmillRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.text.TextUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_SAWING)
public class SawmillRecipeManager extends MekanismRecipeManager<SingleRecipeInput, SawmillRecipe> {

    public static final SawmillRecipeManager INSTANCE = new SawmillRecipeManager();

    private SawmillRecipeManager() {
        super(MekanismRecipeType.SAWING);
    }

    /**
     * Adds a sawing recipe that converts an item into a chance based item output. If the weight is 100%, then it will add it as a main output, if the weight is less than
     * 100% then it adds it as a secondary chance, if the weight is over 100% (must be below 200%) it will add it with a main output and a secondary chance based output.
     * Precision Sawmills and Sawing Factories can process this recipe type.
     *
     * @param name   Name of the new recipe.
     * @param input  {@link IIngredientWithAmount} representing the input of the recipe.
     * @param output {@link Percentaged<IItemStack>} representing the secondary chance based output of the recipe and the chance that it is produced.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount input, Percentaged<IItemStack> output) {
        addRecipe(name, input, output.getData(), output.getPercentage());
    }

    /**
     * Adds a sawing recipe that converts an item into a chance based item output. If the weight is 100%, then it will add it as a main output, if the weight is less than
     * 100% then it adds it as a secondary chance, if the weight is over 100% (must be below 200%) it will add it with a main output and a secondary chance based output.
     * Precision Sawmills and Sawing Factories can process this recipe type.
     *
     * @param name   Name of the new recipe.
     * @param input  {@link IIngredientWithAmount} representing the input of the recipe.
     * @param output {@link IItemStack} representing the secondary chance based output of the recipe.
     * @param chance Chance of the secondary output being produced. This must be a number greater than zero and at most one.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount input, IItemStack output, double chance) {
        if (chance < 1) {
            addRecipe(name, makeRecipe(input, output, chance));
        } else if (chance == 1) {
            addRecipe(name, makeRecipe(input, output));
        } else if (chance < 2) {
            addRecipe(name, makeRecipe(input, output, output.copy(), chance - 1));
        } else {
            //Fail as they should just increase the amount
            throw new IllegalArgumentException("This sawing recipe should just have the amount increased or explicitly use the two output method.");
        }
    }

    /**
     * Adds a sawing recipe that converts an item into another item and a chance based secondary item. Precision Sawmills and Sawing Factories can process this recipe
     * type.
     *
     * @param name            Name of the new recipe.
     * @param input           {@link IIngredientWithAmount} representing the input of the recipe.
     * @param mainOutput      {@link IItemStack} representing the main output of the recipe.
     * @param secondaryOutput {@link Percentaged<IItemStack>} representing the secondary chance based output of the recipe and the chance that it is produced.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount input, IItemStack mainOutput, Percentaged<IItemStack> secondaryOutput) {
        addRecipe(name, input, mainOutput, secondaryOutput.getData(), secondaryOutput.getPercentage());
    }

    /**
     * Adds a sawing recipe that converts an item into another item and a chance based secondary item. Precision Sawmills and Sawing Factories can process this recipe
     * type.
     *
     * @param name            Name of the new recipe.
     * @param input           {@link IIngredientWithAmount} representing the input of the recipe.
     * @param mainOutput      {@link IItemStack} representing the main output of the recipe.
     * @param secondaryOutput {@link IItemStack} representing the secondary chance based output of the recipe.
     * @param secondaryChance Chance of the secondary output being produced. This must be a number greater than zero and at most one.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount input, IItemStack mainOutput, IItemStack secondaryOutput, double secondaryChance) {
        addRecipe(name, makeRecipe(input, mainOutput, secondaryOutput, secondaryChance));
    }

    /**
     * Creates a sawing recipe that converts an item into another item.
     *
     * @param input      {@link IIngredientWithAmount} representing the input of the recipe.
     * @param mainOutput {@link IItemStack} representing the main output of the recipe. Will be validated as not empty.
     */
    public final SawmillRecipe makeRecipe(IIngredientWithAmount input, IItemStack mainOutput) {
        return new BasicSawmillRecipe(CrTUtils.fromCrT(input), getAndValidateNotEmpty(mainOutput), ItemStack.EMPTY, 0);
    }

    /**
     * Creates a sawing recipe that converts an item into a chance based secondary item.
     *
     * @param input           {@link IIngredientWithAmount} representing the input of the recipe.
     * @param secondaryOutput {@link IItemStack} representing the secondary chance based output of the recipe. Will be validated as not empty.
     * @param secondaryChance Chance of the secondary output being produced. Will be validated to be a number greater than zero and at most one.
     */
    public final SawmillRecipe makeRecipe(IIngredientWithAmount input, IItemStack secondaryOutput, double secondaryChance) {
        return new BasicSawmillRecipe(CrTUtils.fromCrT(input), ItemStack.EMPTY, getAndValidateNotEmpty(secondaryOutput), getAndValidateSecondaryChance(secondaryChance));
    }

    /**
     * Creates a sawing recipe that converts an item into another item and a chance based secondary item.
     *
     * @param input           {@link IIngredientWithAmount} representing the input of the recipe.
     * @param mainOutput      {@link IItemStack} representing the main output of the recipe. Will be validated as not empty.
     * @param secondaryOutput {@link IItemStack} representing the secondary chance based output of the recipe. Will be validated as not empty.
     * @param secondaryChance Chance of the secondary output being produced. Will be validated to be a number greater than zero and at most one.
     */
    public final SawmillRecipe makeRecipe(IIngredientWithAmount input, IItemStack mainOutput, IItemStack secondaryOutput, double secondaryChance) {
        return new BasicSawmillRecipe(CrTUtils.fromCrT(input), getAndValidateNotEmpty(mainOutput), getAndValidateNotEmpty(secondaryOutput),
              getAndValidateSecondaryChance(secondaryChance));
    }

    private double getAndValidateSecondaryChance(double secondaryChance) {
        if (secondaryChance <= 0 || secondaryChance > 1) {
            throw new IllegalArgumentException("This sawing recipe requires a secondary output chance greater than zero and at most one.");
        }
        return secondaryChance;
    }

    @Override
    protected String describeOutputs(SawmillRecipe recipe) {
        StringBuilder builder = new StringBuilder();
        List<ItemStack> mainOutputs = recipe.getMainOutputDefinition();
        if (!mainOutputs.isEmpty()) {
            builder.append("main: ").append(CrTUtils.describeOutputs(mainOutputs, ItemStackUtil::getCommandString));
        }
        if (recipe.getSecondaryChance() > 0) {
            if (!mainOutputs.isEmpty()) {
                builder.append("; ");
            }
            if (recipe.getSecondaryChance() == 1) {
                builder.append("secondary: ");
            } else {
                builder.append("secondary with chance ")
                      .append(TextUtils.getPercent(recipe.getSecondaryChance()))
                      .append(": ");
            }
            builder.append(CrTUtils.describeOutputs(recipe.getSecondaryOutputDefinition(), ItemStackUtil::getCommandString));
        }
        return builder.toString();
    }
}