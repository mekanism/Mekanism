package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_ITEM_STACK)
public abstract class ItemStackToItemStackRecipeManager implements IRecipeManager {

    protected ItemStackToItemStackRecipeManager() {
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_CRUSHING)
    public static class CrusherRecipeManager extends ItemStackToItemStackRecipeManager {

        public static final CrusherRecipeManager INSTANCE = new CrusherRecipeManager();

        private CrusherRecipeManager() {
        }

        @Override
        public IRecipeType<ItemStackToItemStackRecipe> getRecipeType() {
            return MekanismRecipeType.CRUSHING;
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ENRICHING)
    public static class EnrichmentChamberRecipeManager extends ItemStackToItemStackRecipeManager {

        public static final EnrichmentChamberRecipeManager INSTANCE = new EnrichmentChamberRecipeManager();

        private EnrichmentChamberRecipeManager() {
        }

        @Override
        public IRecipeType<ItemStackToItemStackRecipe> getRecipeType() {
            return MekanismRecipeType.ENRICHING;
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_SMELTING)
    public static class EnergizedSmelterRecipeManager extends ItemStackToItemStackRecipeManager {

        public static final EnergizedSmelterRecipeManager INSTANCE = new EnergizedSmelterRecipeManager();

        private EnergizedSmelterRecipeManager() {
        }

        @Override
        public IRecipeType<ItemStackToItemStackRecipe> getRecipeType() {
            return MekanismRecipeType.SMELTING;
        }
    }
}