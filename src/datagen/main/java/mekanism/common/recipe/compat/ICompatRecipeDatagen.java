package mekanism.common.recipe.compat;

import java.util.Map;
import mekanism.api.MekanismAPI;
import mekanism.api.ModBasedService;
import net.minecraft.data.worldgen.BootstrapContextAccess;

public interface ICompatRecipeDatagen extends ModBasedService {

    Map<String, ICompatRecipeDatagen> INSTANCES = MekanismAPI.getModBasedServices(ICompatRecipeDatagen.class);

    CompatRecipeProvider recipeProvider(BootstrapContextAccess contextAccess);
}