package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_INFUSE_TYPE)
public abstract class ItemStackToInfuseTypeRecipeManager implements IRecipeManager {

    protected ItemStackToInfuseTypeRecipeManager() {
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_INFUSION_CONVERSION)
    public static class InfusionConversionRecipeManager extends ItemStackToInfuseTypeRecipeManager {

        public static final InfusionConversionRecipeManager INSTANCE = new InfusionConversionRecipeManager();

        private InfusionConversionRecipeManager() {
        }

        @Override
        public IRecipeType<ItemStackToInfuseTypeRecipe> getRecipeType() {
            return MekanismRecipeType.INFUSION_CONVERSION;
        }
    }
}