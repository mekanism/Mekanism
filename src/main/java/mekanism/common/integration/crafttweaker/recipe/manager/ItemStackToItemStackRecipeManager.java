package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.CrushingIRecipe;
import mekanism.common.recipe.impl.EnrichingIRecipe;
import mekanism.common.recipe.impl.SmeltingIRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ITEM_STACK_TO_ITEM_STACK)
public abstract class ItemStackToItemStackRecipeManager extends MekanismRecipeManager<ItemStackToItemStackRecipe> {

    protected ItemStackToItemStackRecipeManager(IMekanismRecipeTypeProvider<ItemStackToItemStackRecipe, ?> recipeType) {
        super(recipeType);
    }

    /**
     * Adds a recipe that converts an item into another item.
     * <br>
     * If this is called from the crushing recipe manager, this will be a crushing recipe. Crushers and Crushing Factories can process this recipe type.
     * <br>
     * If this is called from the enriching recipe manager, this will be an enriching recipe. Enrichment Chambers and Enriching Factories can process this recipe type.
     * <br>
     * If this is called from the smelting recipe manager, this will be a smelting recipe. Energized Smelters, Smelting Factories, and Robits can process this recipe
     * type.
     *
     * @param name   Name of the new recipe.
     * @param input  {@link ItemStackIngredient} representing the input of the recipe.
     * @param output {@link IItemStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient input, IItemStack output) {
        addRecipe(makeRecipe(getAndValidateName(name), input, output));
    }

    /**
     * Creates a recipe that converts an item into another item.
     *
     * @param id     Name of the new recipe.
     * @param input  {@link ItemStackIngredient} representing the input of the recipe.
     * @param output {@link IItemStack} representing the output of the recipe. Will be validated as not empty.
     */
    public final ItemStackToItemStackRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, IItemStack output) {
        return makeRecipe(id, input, getAndValidateNotEmpty(output));
    }

    protected abstract ItemStackToItemStackRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output);

    @Override
    protected ActionAddMekanismRecipe getAction(ItemStackToItemStackRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(recipe.getOutputDefinition(), ItemStackUtil::getCommandString);
            }
        };
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_CRUSHING)
    public static class CrusherRecipeManager extends ItemStackToItemStackRecipeManager {

        public static final CrusherRecipeManager INSTANCE = new CrusherRecipeManager();

        private CrusherRecipeManager() {
            super(MekanismRecipeType.CRUSHING);
        }

        @Override
        protected ItemStackToItemStackRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output) {
            return new CrushingIRecipe(id, input, output);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ENRICHING)
    public static class EnrichmentChamberRecipeManager extends ItemStackToItemStackRecipeManager {

        public static final EnrichmentChamberRecipeManager INSTANCE = new EnrichmentChamberRecipeManager();

        private EnrichmentChamberRecipeManager() {
            super(MekanismRecipeType.ENRICHING);
        }

        @Override
        protected ItemStackToItemStackRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output) {
            return new EnrichingIRecipe(id, input, output);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_SMELTING)
    public static class EnergizedSmelterRecipeManager extends ItemStackToItemStackRecipeManager {

        public static final EnergizedSmelterRecipeManager INSTANCE = new EnergizedSmelterRecipeManager();

        private EnergizedSmelterRecipeManager() {
            super(MekanismRecipeType.SMELTING);
        }

        @Override
        protected ItemStackToItemStackRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output) {
            return new SmeltingIRecipe(id, input, output);
        }
    }
}