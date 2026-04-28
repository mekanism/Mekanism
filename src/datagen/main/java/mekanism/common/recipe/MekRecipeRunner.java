package mekanism.common.recipe;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

public class MekRecipeRunner extends RecipeProvider.Runner {

    private final BiFunction<Provider, RecipeOutput, RecipeProvider> factory;
    private final String modId;

    public MekRecipeRunner(PackOutput packOutput, CompletableFuture<Provider> registries, BiFunction<Provider, RecipeOutput, RecipeProvider> factory, String modId) {
        super(packOutput, registries);
        this.factory = factory;
        this.modId = modId;
    }

    @Override
    protected RecipeProvider createRecipeProvider(Provider registries, RecipeOutput output) {
        return factory.apply(registries, output);
    }

    @Override
    public String getName() {
        return "Recipes: " + modId;
    }
}
