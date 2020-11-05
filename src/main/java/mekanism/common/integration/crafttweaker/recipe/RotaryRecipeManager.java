package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ROTARY)
public class RotaryRecipeManager implements IRecipeManager {

    public static final RotaryRecipeManager INSTANCE = new RotaryRecipeManager();

    private RotaryRecipeManager() {
    }

    @Override
    public IRecipeType<RotaryRecipe> getRecipeType() {
        return MekanismRecipeType.ROTARY;
    }
}