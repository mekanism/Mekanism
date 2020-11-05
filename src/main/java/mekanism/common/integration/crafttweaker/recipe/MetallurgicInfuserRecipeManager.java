package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_METALLURGIC_INFUSING)
public class MetallurgicInfuserRecipeManager implements IRecipeManager {

    public static final MetallurgicInfuserRecipeManager INSTANCE = new MetallurgicInfuserRecipeManager();

    private MetallurgicInfuserRecipeManager() {
    }

    @Override
    public IRecipeType<MetallurgicInfuserRecipe> getRecipeType() {
        return MekanismRecipeType.METALLURGIC_INFUSING;
    }
}