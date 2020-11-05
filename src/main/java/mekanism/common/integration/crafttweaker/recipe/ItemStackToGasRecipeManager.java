package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_GAS)
public abstract class ItemStackToGasRecipeManager implements IRecipeManager {

    protected ItemStackToGasRecipeManager() {
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_GAS_CONVERSION)
    public static class GasConversionRecipeManager extends ItemStackToGasRecipeManager {

        public static final GasConversionRecipeManager INSTANCE = new GasConversionRecipeManager();

        private GasConversionRecipeManager() {
        }

        @Override
        public IRecipeType<ItemStackToGasRecipe> getRecipeType() {
            return MekanismRecipeType.GAS_CONVERSION;
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_OXIDIZING)
    public static class ChemicalOxidizerRecipeManager extends ItemStackToGasRecipeManager {

        public static final ChemicalOxidizerRecipeManager INSTANCE = new ChemicalOxidizerRecipeManager();

        private ChemicalOxidizerRecipeManager() {
        }

        @Override
        public IRecipeType<ItemStackToGasRecipe> getRecipeType() {
            return MekanismRecipeType.OXIDIZING;
        }
    }
}