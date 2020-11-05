package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_REACTION)
public class PressurizedReactionRecipeManager implements IRecipeManager {

    public static final PressurizedReactionRecipeManager INSTANCE = new PressurizedReactionRecipeManager();

    private PressurizedReactionRecipeManager() {
    }

    @Override
    public IRecipeType<PressurizedReactionRecipe> getRecipeType() {
        return MekanismRecipeType.REACTION;
    }
}