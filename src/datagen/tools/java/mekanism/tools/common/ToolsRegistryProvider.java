package mekanism.tools.common;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.registries.BaseRegistryProvider;
import mekanism.tools.common.recipe.ToolsRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

public class ToolsRegistryProvider extends BaseRegistryProvider {

    public static DatapackBuiltinEntriesProvider forWorldLayer(PackOutput output, CompletableFuture<HolderLookup.Provider> worldRegistries) {
        return forWorldLayer(output, worldRegistries, MekanismTools.MODID, new RegistrySetBuilder()

        );
    }

    public static DatapackBuiltinEntriesProvider forReloadableLayer(PackOutput output, CompletableFuture<HolderLookup.Provider> worldRegistries,
          CompletableFuture<HolderLookup.Provider> reloadableRegistries) {
        return forReloadableLayer(output, worldRegistries, reloadableRegistries, MekanismTools.MODID, new RegistrySetBuilder()
              .add(Registries.ADVANCEMENT, new AdvancementProvider(List.of(ToolsAdvancementProvider::new)))
              .add(BaseRecipeProvider.registerRecipes(ToolsRecipeProvider::new))
        );
    }
}