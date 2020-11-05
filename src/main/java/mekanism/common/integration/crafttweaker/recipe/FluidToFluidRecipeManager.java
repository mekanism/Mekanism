package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_FLUID_TO_FLUID)
public abstract class FluidToFluidRecipeManager implements IRecipeManager {

    protected FluidToFluidRecipeManager() {
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_EVAPORATING)
    public static class EvaporatingRecipeManager extends FluidToFluidRecipeManager {

        public static final EvaporatingRecipeManager INSTANCE = new EvaporatingRecipeManager();

        private EvaporatingRecipeManager() {
        }

        @Override
        public IRecipeType<FluidToFluidRecipe> getRecipeType() {
            return MekanismRecipeType.EVAPORATING;
        }
    }
}