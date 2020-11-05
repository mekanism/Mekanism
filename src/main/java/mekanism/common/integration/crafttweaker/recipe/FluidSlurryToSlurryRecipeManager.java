package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_FLUID_SLURRY_TO_SLURRY)
public abstract class FluidSlurryToSlurryRecipeManager implements IRecipeManager {

    protected FluidSlurryToSlurryRecipeManager() {
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_WASHING)
    public static class ChemicalWasherRecipeManager extends FluidSlurryToSlurryRecipeManager {

        public static final ChemicalWasherRecipeManager INSTANCE = new ChemicalWasherRecipeManager();

        private ChemicalWasherRecipeManager() {
        }

        @Override
        public IRecipeType<FluidSlurryToSlurryRecipe> getRecipeType() {
            return MekanismRecipeType.WASHING;
        }
    }
}