package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.basic.BasicCompressingRecipe;
import mekanism.api.recipes.basic.BasicInjectingRecipe;
import mekanism.api.recipes.basic.BasicMetallurgicInfuserRecipe;
import mekanism.api.recipes.basic.BasicPaintingRecipe;
import mekanism.api.recipes.basic.BasicPurifyingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ITEM_STACK_CHEMICAL_TO_ITEM_STACK)
public abstract class ItemStackChemicalToItemStackRecipeManager extends
      MekanismRecipeManager<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe> {

    protected ItemStackChemicalToItemStackRecipeManager(IMekanismRecipeTypeProvider<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, ?> recipeType) {
        super(recipeType);
    }

    /**
     * Adds a recipe that converts an item and a chemical into an item.
     * <br>
     * If this is called from the compressing recipe manager, this will be a compressing recipe and the chemical input must be a {@link ChemicalStackIngredient} that will
     * be used at a constant rate over the duration of the recipe. Osmium Compressors and Compressing Factories can process this recipe type.
     * <br>
     * If this is called from the injecting recipe manager, this will be an injecting recipe and the chemical input must be a {@link ChemicalStackIngredient} that will be
     * used at a near constant rate over the duration of the recipe. Chemical Injection Chambers and Injecting Factories can process this recipe type.
     * <br>
     * If this is called from the purifying recipe manager, this will be a purifying recipe and the chemical input must be a {@link ChemicalStackIngredient} that will be
     * used at a near constant rate over the duration of the recipe. Purification Chambers and Purifying Factories can process this recipe type.
     * <br>
     * If this is called from the metallurgic infusing recipe manager, this will be a metallurgic infusing recipe and the chemical input must be an
     * {@link ChemicalStackIngredient} that will be consumed at the end along with the item input. Metallurgic Infusers and Infusing Factories can process this recipe
     * type.
     * <br>
     * If this is called from the painting recipe manager, this will be a painting recipe and the chemical input must be a {@link ChemicalStackIngredient} that will be
     * consumed at the end along with the item input. Painting Machines can process this recipe type.
     *
     * @param name          Name of the new recipe.
     * @param itemInput     {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param chemicalInput {@link ChemicalStackIngredient} representing the chemical input of the recipe. The type of this chemical depends on the recipe manager it is
     *                      called from.
     * @param output        {@link IItemStack} representing the output of the recipe.
     * @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount itemInput, ChemicalStackIngredient chemicalInput, IItemStack output, boolean perTickUsage) {
        addRecipe(name, makeRecipe(itemInput, chemicalInput, output, perTickUsage));
    }

    /**
     * Creates a recipe that converts an item and a chemical into an item.
     *
     * @param itemInput     {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param chemicalInput {@link ChemicalStackIngredient} representing the chemical input of the recipe. The type of this chemical depends on the recipe manager it is
     *                      called from.
     * @param output        {@link IItemStack} representing the output of the recipe. Will be validated as not empty.
     * @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
     */
    public final ItemStackChemicalToItemStackRecipe makeRecipe(IIngredientWithAmount itemInput, ChemicalStackIngredient chemicalInput, IItemStack output, boolean perTickUsage) {
        return makeRecipe(itemInput, chemicalInput, getAndValidateNotEmpty(output), perTickUsage);
    }

    protected abstract ItemStackChemicalToItemStackRecipe makeRecipe(IIngredientWithAmount itemInput, ChemicalStackIngredient chemicalInput, ItemStack output, boolean perTickUsage);

    @Override
    protected String describeOutputs(ItemStackChemicalToItemStackRecipe recipe) {
        return CrTUtils.describeOutputs(recipe.getOutputDefinition(), ItemStackUtil::getCommandString);
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_COMPRESSING)
    public static class OsmiumCompressorRecipeManager extends ItemStackChemicalToItemStackRecipeManager {

        public static final OsmiumCompressorRecipeManager INSTANCE = new OsmiumCompressorRecipeManager();

        private OsmiumCompressorRecipeManager() {
            super(MekanismRecipeType.COMPRESSING);
        }

        @Override
        protected ItemStackChemicalToItemStackRecipe makeRecipe(IIngredientWithAmount itemInput, ChemicalStackIngredient chemicalInput, ItemStack output, boolean perTickUsage) {
            return new BasicCompressingRecipe(CrTUtils.fromCrT(itemInput), chemicalInput, output, perTickUsage);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_INJECTING)
    public static class ChemicalInjectionRecipeManager extends ItemStackChemicalToItemStackRecipeManager {

        public static final ChemicalInjectionRecipeManager INSTANCE = new ChemicalInjectionRecipeManager();

        private ChemicalInjectionRecipeManager() {
            super(MekanismRecipeType.INJECTING);
        }

        @Override
        protected ItemStackChemicalToItemStackRecipe makeRecipe(IIngredientWithAmount itemInput, ChemicalStackIngredient chemicalInput, ItemStack output, boolean perTickUsage) {
            return new BasicInjectingRecipe(CrTUtils.fromCrT(itemInput), chemicalInput, output, perTickUsage);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_PURIFYING)
    public static class PurificationRecipeManager extends ItemStackChemicalToItemStackRecipeManager {

        public static final PurificationRecipeManager INSTANCE = new PurificationRecipeManager();

        private PurificationRecipeManager() {
            super(MekanismRecipeType.PURIFYING);
        }

        @Override
        protected ItemStackChemicalToItemStackRecipe makeRecipe(IIngredientWithAmount itemInput, ChemicalStackIngredient chemicalInput, ItemStack output, boolean perTickUsage) {
            return new BasicPurifyingRecipe(CrTUtils.fromCrT(itemInput), chemicalInput, output, perTickUsage);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_METALLURGIC_INFUSING)
    public static class MetallurgicInfuserRecipeManager extends ItemStackChemicalToItemStackRecipeManager {

        public static final MetallurgicInfuserRecipeManager INSTANCE = new MetallurgicInfuserRecipeManager();

        private MetallurgicInfuserRecipeManager() {
            super(MekanismRecipeType.METALLURGIC_INFUSING);
        }

        @Override
        protected ItemStackChemicalToItemStackRecipe makeRecipe(IIngredientWithAmount itemInput, ChemicalStackIngredient chemicalInput, ItemStack output, boolean perTickUsage) {
            return new BasicMetallurgicInfuserRecipe(CrTUtils.fromCrT(itemInput), chemicalInput, output, perTickUsage);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_PAINTING)
    public static class PaintingRecipeManager extends ItemStackChemicalToItemStackRecipeManager {

        public static final PaintingRecipeManager INSTANCE = new PaintingRecipeManager();

        private PaintingRecipeManager() {
            super(MekanismRecipeType.PAINTING);
        }

        @Override
        protected ItemStackChemicalToItemStackRecipe makeRecipe(IIngredientWithAmount itemInput, ChemicalStackIngredient chemicalInput, ItemStack output, boolean perTickUsage) {
            return new BasicPaintingRecipe(CrTUtils.fromCrT(itemInput), chemicalInput, output, perTickUsage);
        }
    }
}