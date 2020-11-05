package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_ENERGY)
public abstract class ItemStackToEnergyRecipeManager implements IRecipeManager {

    protected ItemStackToEnergyRecipeManager() {
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ENERGY_CONVERSION)
    public static class EnergyConversionRecipeManager extends ItemStackToEnergyRecipeManager {

        public static final EnergyConversionRecipeManager INSTANCE = new EnergyConversionRecipeManager();

        private EnergyConversionRecipeManager() {
        }

        @Override
        public IRecipeType<ItemStackToEnergyRecipe> getRecipeType() {
            return MekanismRecipeType.ENERGY_CONVERSION;
        }
    }
}