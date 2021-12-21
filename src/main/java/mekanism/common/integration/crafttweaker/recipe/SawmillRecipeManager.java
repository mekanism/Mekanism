package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.helper.ItemStackHelper;
import com.blamejared.crafttweaker.impl.item.MCWeightedItemStack;
import java.util.List;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.SawmillIRecipe;
import mekanism.common.util.text.TextUtils;
import net.minecraft.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_SAWING)
public class SawmillRecipeManager extends MekanismRecipeManager<SawmillRecipe> {

    public static final SawmillRecipeManager INSTANCE = new SawmillRecipeManager();

    private SawmillRecipeManager() {
        super(MekanismRecipeType.SAWING);
    }

    /**
     * Adds a sawing recipe that converts an item into another item with no secondary output. Precision Sawmills and Sawing Factories can process this recipe type.
     *
     * @param name       Name of the new recipe.
     * @param input      {@link ItemStackIngredient} representing the input of the recipe.
     * @param mainOutput {@link IItemStack} representing the main output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient input, IItemStack mainOutput) {
        addRecipe(name, input, getAndValidateNotEmpty(mainOutput), ItemStack.EMPTY, 0);
    }

    /**
     * Adds a sawing recipe that converts an item into a chance based item output. Precision Sawmills and Sawing Factories can process this recipe type.
     *
     * @param name            Name of the new recipe.
     * @param input           {@link ItemStackIngredient} representing the input of the recipe.
     * @param secondaryOutput {@link MCWeightedItemStack} representing the secondary chance based output of the recipe and the chance that it is produced.
     *
     * @apiNote If the weight is 100%, {@link #addRecipe(String, ItemStackIngredient, IItemStack)} must be used instead.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient input, MCWeightedItemStack secondaryOutput) {
        addRecipe(name, input, secondaryOutput.getItemStack(), secondaryOutput.getWeight());
    }

    /**
     * Adds a sawing recipe that converts an item into a chance based item output. Precision Sawmills and Sawing Factories can process this recipe type.
     *
     * @param name            Name of the new recipe.
     * @param input           {@link ItemStackIngredient} representing the input of the recipe.
     * @param secondaryOutput {@link IItemStack} representing the secondary chance based output of the recipe.
     * @param secondaryChance Chance of the secondary output being produced. This must be a number greater than zero and at most one.
     *
     * @apiNote If the secondary chance is one (100%), {@link #addRecipe(String, ItemStackIngredient, IItemStack)} must be used instead.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient input, IItemStack secondaryOutput, double secondaryChance) {
        addRecipe(name, input, ItemStack.EMPTY, getAndValidateNotEmpty(secondaryOutput), getAndValidateSecondaryChance(secondaryChance, false));
    }

    /**
     * Adds a sawing recipe that converts an item into another item and a chance based secondary item. Precision Sawmills and Sawing Factories can process this recipe
     * type.
     *
     * @param name            Name of the new recipe.
     * @param input           {@link ItemStackIngredient} representing the input of the recipe.
     * @param mainOutput      {@link IItemStack} representing the main output of the recipe.
     * @param secondaryOutput {@link MCWeightedItemStack} representing the secondary chance based output of the recipe and the chance that it is produced.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient input, IItemStack mainOutput, MCWeightedItemStack secondaryOutput) {
        addRecipe(name, input, mainOutput, secondaryOutput.getItemStack(), secondaryOutput.getWeight());
    }

    /**
     * Adds a sawing recipe that converts an item into another item and a chance based secondary item. Precision Sawmills and Sawing Factories can process this recipe
     * type.
     *
     * @param name            Name of the new recipe.
     * @param input           {@link ItemStackIngredient} representing the input of the recipe.
     * @param mainOutput      {@link IItemStack} representing the main output of the recipe.
     * @param secondaryOutput {@link IItemStack} representing the secondary chance based output of the recipe.
     * @param secondaryChance Chance of the secondary output being produced. This must be a number greater than zero and at most one.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient input, IItemStack mainOutput, IItemStack secondaryOutput, double secondaryChance) {
        addRecipe(name, input, getAndValidateNotEmpty(mainOutput), getAndValidateNotEmpty(secondaryOutput), getAndValidateSecondaryChance(secondaryChance, true));
    }

    private void addRecipe(String name, ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance) {
        addRecipe(new SawmillIRecipe(getAndValidateName(name), input, mainOutput, secondaryOutput, secondaryChance));
    }

    private double getAndValidateSecondaryChance(double secondaryChance, boolean hasMain) {
        if (hasMain) {
            if (secondaryChance <= 0 || secondaryChance > 1) {
                throw new IllegalArgumentException("This sawing recipe requires a secondary output chance greater than zero and at most one.");
            }
        } else if (secondaryChance == 1) {
            throw new IllegalArgumentException("This sawing recipe should use a main output recipe instead of a secondary output chance based recipe.");
        } else if (secondaryChance <= 0 || secondaryChance >= 1) {
            throw new IllegalArgumentException("This sawing recipe requires a secondary output chance greater than zero and less than one.");
        }
        return secondaryChance;
    }

    @Override
    protected ActionAddMekanismRecipe getAction(SawmillRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                SawmillRecipe recipe = getRecipe();
                StringBuilder builder = new StringBuilder();
                List<ItemStack> mainOutputs = recipe.getMainOutputDefinition();
                if (!mainOutputs.isEmpty()) {
                    builder.append("main: ").append(CrTUtils.describeOutputs(mainOutputs, ItemStackHelper::getCommandString));
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
                    builder.append(CrTUtils.describeOutputs(recipe.getSecondaryOutputDefinition(), ItemStackHelper::getCommandString));
                }
                return builder.toString();
            }
        };
    }
}