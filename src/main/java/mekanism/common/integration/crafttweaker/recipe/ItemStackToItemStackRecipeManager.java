package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.ingredient.CrTItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.CrushingIRecipe;
import mekanism.common.recipe.impl.EnrichingIRecipe;
import mekanism.common.recipe.impl.SmeltingIRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_ITEM_STACK)
public abstract class ItemStackToItemStackRecipeManager extends MekanismRecipeManager<ItemStackToItemStackRecipe> {

    protected ItemStackToItemStackRecipeManager(MekanismRecipeType<ItemStackToItemStackRecipe> recipeType) {
        super(recipeType);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTItemStackIngredient input, IItemStack output) {
        addRecipe(makeRecipe(getAndValidateName(name), input.getInternal(), getAndValidateNotEmpty(output)));
    }

    protected abstract ItemStackToItemStackRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output);

    @Override
    protected ActionAddMekanismRecipe getAction(ItemStackToItemStackRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(getRecipe().getOutputDefinition(), MCItemStackMutable::new);
            }
        };
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_CRUSHING)
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
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ENRICHING)
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
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_SMELTING)
    public static class EnergizedSmelterRecipeManager extends ItemStackToItemStackRecipeManager {

        public static final EnergizedSmelterRecipeManager INSTANCE = new EnergizedSmelterRecipeManager();

        private EnergizedSmelterRecipeManager() {
            super(MekanismRecipeType.SMELTING);
            //TODO: Allow removing regular smelting recipes from the energized smelter just like we allowed in 1.12
        }

        @Override
        protected ItemStackToItemStackRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output) {
            return new SmeltingIRecipe(id, input, output);
        }
    }
}