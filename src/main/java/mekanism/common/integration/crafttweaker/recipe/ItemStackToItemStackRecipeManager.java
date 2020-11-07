package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_ITEM_STACK)
public abstract class ItemStackToItemStackRecipeManager extends MekanismRecipeManager<ItemStackToItemStackRecipe> {

    protected ItemStackToItemStackRecipeManager(MekanismRecipeType<ItemStackToItemStackRecipe> recipeType) {
        super(recipeType);
    }

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
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ENRICHING)
    public static class EnrichmentChamberRecipeManager extends ItemStackToItemStackRecipeManager {

        public static final EnrichmentChamberRecipeManager INSTANCE = new EnrichmentChamberRecipeManager();

        private EnrichmentChamberRecipeManager() {
            super(MekanismRecipeType.ENRICHING);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_SMELTING)
    public static class EnergizedSmelterRecipeManager extends ItemStackToItemStackRecipeManager {

        public static final EnergizedSmelterRecipeManager INSTANCE = new EnergizedSmelterRecipeManager();

        private EnergizedSmelterRecipeManager() {
            super(MekanismRecipeType.SMELTING);
        }
    }
}