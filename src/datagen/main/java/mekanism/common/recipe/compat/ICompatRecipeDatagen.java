package mekanism.common.recipe.compat;

import java.util.Map;
import java.util.function.BiFunction;
import mekanism.api.MekanismAPI;
import mekanism.api.ModBasedService;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.crafting.Recipe;

public interface ICompatRecipeDatagen extends ModBasedService, BiFunction<BootstrapContext<Recipe<?>>, BootstrapContext<Advancement>, CompatRecipeProvider> {

    Map<String, ICompatRecipeDatagen> INSTANCES = MekanismAPI.getModBasedServices(ICompatRecipeDatagen.class);
}