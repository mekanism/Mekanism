package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.NucleosynthesizingIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_NUCLEOSYNTHESIZING)
public class NucleosynthesizingRecipeManager extends MekanismRecipeManager<NucleosynthesizingRecipe> {

    public static final NucleosynthesizingRecipeManager INSTANCE = new NucleosynthesizingRecipeManager();

    private NucleosynthesizingRecipeManager() {
        super(MekanismRecipeType.NUCLEOSYNTHESIZING);
    }

    /**
     * Adds a nucleosynthesizing recipe that uses a gas and massive amounts of energy to convert an item into another item. Antiprotonic Nucleosynthesizers can process
     * this recipe type.
     *
     * @param name      Name of the new recipe.
     * @param itemInput {@link ItemStackIngredient} representing the item input of the recipe.
     * @param gasInput  {@link GasStackIngredient} representing the gas input of the recipe.
     * @param output    {@link IItemStack} representing the output of the recipe.
     * @param duration  Duration in ticks that it takes the recipe to complete. Must be greater than zero.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient itemInput, GasStackIngredient gasInput, IItemStack output, int duration) {
        addRecipe(makeRecipe(getAndValidateName(name), itemInput, gasInput, output, duration));
    }

    /**
     * Creates a nucleosynthesizing recipe that uses a gas and massive amounts of energy to convert an item into another item.
     *
     * @param id        Name of the new recipe.
     * @param itemInput {@link ItemStackIngredient} representing the item input of the recipe.
     * @param gasInput  {@link GasStackIngredient} representing the gas input of the recipe.
     * @param output    {@link IItemStack} representing the output of the recipe. Will be validated as not empty.
     * @param duration  Duration in ticks that it takes the recipe to complete. Will be validated as being greater than zero.
     */
    public final NucleosynthesizingRecipe makeRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, IItemStack output, int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be a number greater than zero! Duration: " + duration);
        }
        return new NucleosynthesizingIRecipe(id, itemInput, gasInput, getAndValidateNotEmpty(output), duration);
    }

    @Override
    protected ActionAddMekanismRecipe getAction(NucleosynthesizingRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(recipe.getOutputDefinition(), ItemStackUtil::getCommandString);
            }
        };
    }
}