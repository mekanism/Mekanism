package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_SEPARATING)
public class ElectrolysisRecipeManager implements IRecipeManager {

    public static final ElectrolysisRecipeManager INSTANCE = new ElectrolysisRecipeManager();

    private ElectrolysisRecipeManager() {
    }

    @Override
    public IRecipeType<ElectrolysisRecipe> getRecipeType() {
        return MekanismRecipeType.SEPARATING;
    }
}