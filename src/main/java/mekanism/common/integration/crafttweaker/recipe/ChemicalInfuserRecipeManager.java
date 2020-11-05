package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_CHEMICAL_INFUSING)
public class ChemicalInfuserRecipeManager implements IRecipeManager {

    public static final ChemicalInfuserRecipeManager INSTANCE = new ChemicalInfuserRecipeManager();

    private ChemicalInfuserRecipeManager() {
    }

    @Override
    public IRecipeType<ChemicalInfuserRecipe> getRecipeType() {
        return MekanismRecipeType.CHEMICAL_INFUSING;
    }
}