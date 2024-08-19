package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.basic.BasicPressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.ReactionRecipeInput;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
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
     * Adds a reaction recipe that converts an item, fluid, and chemical into another item. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param name           Name of the new recipe.
     * @param inputSolid     {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param inputFluid     {@link CTFluidIngredient} representing the fluid input of the recipe.
     * @param inputChemical  {@link ChemicalStackIngredient} representing the chemical input of the recipe.
     * @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem     {@link IItemStack} representing the item output of the recipe.
     * @param energyRequired Value representing how much "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the
     *                       recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount inputSolid, CTFluidIngredient inputFluid, ChemicalStackIngredient inputChemical, int duration,
          IItemStack outputItem, long energyRequired) {
        addRecipe(name, inputSolid, inputFluid, inputChemical, duration, getAndValidateNotEmpty(outputItem), ChemicalStack.EMPTY, energyRequired);
    }

    /**
     * Adds a reaction recipe that converts an item, fluid, and chemical into another item. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param name          Name of the new recipe.
     * @param inputSolid    {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param inputFluid    {@link CTFluidIngredient} representing the fluid input of the recipe.
     * @param inputChemical {@link ChemicalStackIngredient} representing the chemical input of the recipe.
     * @param duration      Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem    {@link IItemStack} representing the item output of the recipe.
     *
     * @apiNote {@code energyRequired} (the amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe),
     * will default to zero.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount inputSolid, CTFluidIngredient inputFluid, ChemicalStackIngredient inputChemical, int duration,
          IItemStack outputItem) {
        addRecipe(name, inputSolid, inputFluid, inputChemical, duration, getAndValidateNotEmpty(outputItem), ChemicalStack.EMPTY, 0);
    }

    /**
     * Adds a reaction recipe that converts an item, fluid, and chemical into another chemical. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param name           Name of the new recipe.
     * @param inputSolid     {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param inputFluid     {@link CTFluidIngredient} representing the fluid input of the recipe.
     * @param inputChemical  {@link ChemicalStackIngredient} representing the chemical input of the recipe.
     * @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputChemical {@link ICrTChemicalStack} representing the chemical output of the recipe.
     * @param energyRequired Value representing how much "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the
     *                       recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount inputSolid, CTFluidIngredient inputFluid, ChemicalStackIngredient inputChemical, int duration,
          ICrTChemicalStack outputChemical, long energyRequired) {
        addRecipe(name, inputSolid, inputFluid, inputChemical, duration, ItemStack.EMPTY, getAndValidateNotEmpty(outputChemical), energyRequired);
    }

    /**
     * Adds a reaction recipe that converts an item, fluid, and chemical into another chemical. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param name           Name of the new recipe.
     * @param inputSolid     {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param inputFluid     {@link CTFluidIngredient} representing the fluid input of the recipe.
     * @param inputChemical  {@link ChemicalStackIngredient} representing the chemical input of the recipe.
     * @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputChemical {@link ICrTChemicalStack} representing the chemical output of the recipe.
     *
     * @apiNote {@code energyRequired} (the amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe),
     * will default to zero.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount inputSolid, CTFluidIngredient inputFluid, ChemicalStackIngredient inputChemical, int duration,
          ICrTChemicalStack outputChemical) {
        addRecipe(name, inputSolid, inputFluid, inputChemical, duration, ItemStack.EMPTY, getAndValidateNotEmpty(outputChemical), 0);
    }

    /**
     * Adds a reaction recipe that converts an item, fluid, and chemical into another item and chemical. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param name           Name of the new recipe.
     * @param inputSolid     {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param inputFluid     {@link CTFluidIngredient} representing the fluid input of the recipe.
     * @param inputChemical  {@link ChemicalStackIngredient} representing the chemical input of the recipe.
     * @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem     {@link IItemStack} representing the item output of the recipe.
     * @param outputChemical {@link ICrTChemicalStack} representing the chemical output of the recipe.
     * @param energyRequired Value representing how much "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the
     *                       recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount inputSolid, CTFluidIngredient inputFluid, ChemicalStackIngredient inputChemical, int duration,
          IItemStack outputItem, ICrTChemicalStack outputChemical, long energyRequired) {
        addRecipe(name, inputSolid, inputFluid, inputChemical, duration, getAndValidateNotEmpty(outputItem), getAndValidateNotEmpty(outputChemical), energyRequired);
    }

    /**
     * Adds a reaction recipe that converts an item, fluid, and chemical into another item and chemical. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param name           Name of the new recipe.
     * @param inputSolid     {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param inputFluid     {@link CTFluidIngredient} representing the fluid input of the recipe.
     * @param inputChemical  {@link ChemicalStackIngredient} representing the chemical input of the recipe.
     * @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem     {@link IItemStack} representing the item output of the recipe.
     * @param outputChemical {@link ICrTChemicalStack} representing the chemical output of the recipe.
     *
     * @apiNote {@code energyRequired} (the amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe),
     * will default to zero.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount inputSolid, CTFluidIngredient inputFluid, ChemicalStackIngredient inputChemical, int duration,
          IItemStack outputItem, ICrTChemicalStack outputChemical) {
        addRecipe(name, inputSolid, inputFluid, inputChemical, duration, getAndValidateNotEmpty(outputItem), getAndValidateNotEmpty(outputChemical), 0);
    }

    private void addRecipe(String name, IIngredientWithAmount inputSolid, CTFluidIngredient inputFluid, ChemicalStackIngredient inputChemical, int duration,
          ItemStack outputItem, ChemicalStack outputChemical, long energyRequired) {
        addRecipe(name, makeRecipe(inputSolid, inputFluid, inputChemical, duration, outputItem, outputChemical, energyRequired));
    }

    /**
     * Creates a reaction recipe that converts an item, fluid, and chemical into another item and chemical. Pressurized Reaction Chambers can process this recipe type.
     *
     * @param inputSolid     {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param inputFluid     {@link CTFluidIngredient} representing the fluid input of the recipe.
     * @param inputChemical  {@link ChemicalStackIngredient} representing the chemical input of the recipe.
     * @param duration       Base duration in ticks that this recipe takes to complete. Will be validated as being greater than zero.
     * @param outputItem     {@link IItemStack} representing the item output of the recipe. It will be validated that at least one of this and outputChemical is not
     *                       empty.
     * @param outputChemical {@link ICrTChemicalStack} representing the chemical output of the recipe. It will be validated that at least one of this and outputItem is
     *                       not empty.
     * @param energyRequired Value representing how much "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the
     *                       recipe.
     */
    public PressurizedReactionRecipe makeRecipe(IIngredientWithAmount inputSolid, CTFluidIngredient inputFluid, ChemicalStackIngredient inputChemical,
          int duration, ItemStack outputItem, ChemicalStack outputChemical, long energyRequired) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive! Duration: " + duration);
        }
        return new BasicPressurizedReactionRecipe(CrTUtils.fromCrT(inputSolid), CrTUtils.fromCrT(inputFluid), inputChemical, energyRequired, duration,
              outputItem, outputChemical);
    }

    @Override
    protected String describeOutputs(PressurizedReactionRecipe recipe) {
        return CrTUtils.describeOutputs(recipe.getOutputDefinition(), output -> {
            StringBuilder builder = new StringBuilder();
            ItemStack itemOutput = output.item();
            if (!itemOutput.isEmpty()) {
                builder.append(ItemStackUtil.getCommandString(itemOutput));
            }
            ChemicalStack chemicalOutput = output.chemical();
            if (!chemicalOutput.isEmpty()) {
                if (!itemOutput.isEmpty()) {
                    builder.append(" and ");
                }
                builder.append(new CrTChemicalStack(chemicalOutput));
            }
            return builder.toString();
        });
    }
}