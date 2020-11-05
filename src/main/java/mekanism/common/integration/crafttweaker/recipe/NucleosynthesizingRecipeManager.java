package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_NUCLEOSYNTHESIZING)
public class NucleosynthesizingRecipeManager implements IRecipeManager {

    public static final NucleosynthesizingRecipeManager INSTANCE = new NucleosynthesizingRecipeManager();

    private NucleosynthesizingRecipeManager() {
    }

    @Override
    public IRecipeType<NucleosynthesizingRecipe> getRecipeType() {
        return MekanismRecipeType.NUCLEOSYNTHESIZING;
    }
}