package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_DISSOLUTION)
public class ChemicalDissolutionRecipeManager implements IRecipeManager {

    public static final ChemicalDissolutionRecipeManager INSTANCE = new ChemicalDissolutionRecipeManager();

    private ChemicalDissolutionRecipeManager() {
    }

    @Override
    public IRecipeType<ChemicalDissolutionRecipe> getRecipeType() {
        return MekanismRecipeType.DISSOLUTION;
    }
}