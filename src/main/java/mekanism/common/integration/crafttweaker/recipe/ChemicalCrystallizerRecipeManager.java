package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_CRYSTALLIZING)
public class ChemicalCrystallizerRecipeManager implements IRecipeManager {

    public static final ChemicalCrystallizerRecipeManager INSTANCE = new ChemicalCrystallizerRecipeManager();

    private ChemicalCrystallizerRecipeManager() {
    }

    @Override
    public IRecipeType<ChemicalCrystallizerRecipe> getRecipeType() {
        return MekanismRecipeType.CRYSTALLIZING;
    }
}