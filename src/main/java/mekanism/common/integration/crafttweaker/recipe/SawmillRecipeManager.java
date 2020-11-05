package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_SAWING)
public class SawmillRecipeManager implements IRecipeManager {

    public static final SawmillRecipeManager INSTANCE = new SawmillRecipeManager();

    private SawmillRecipeManager() {
    }

    @Override
    public IRecipeType<SawmillRecipe> getRecipeType() {
        return MekanismRecipeType.SAWING;
    }
}