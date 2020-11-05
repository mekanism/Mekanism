package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_COMBINING)
public class CombinerRecipeManager implements IRecipeManager {

    public static final CombinerRecipeManager INSTANCE = new CombinerRecipeManager();

    private CombinerRecipeManager() {
    }

    @Override
    public IRecipeType<CombinerRecipe> getRecipeType() {
        return MekanismRecipeType.COMBINING;
    }
}