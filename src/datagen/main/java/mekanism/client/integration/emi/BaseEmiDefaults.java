package mekanism.client.integration.emi;

import com.mojang.serialization.Codec;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import mekanism.api.chemical.Chemical;
import mekanism.api.tier.BaseTier;
import mekanism.common.Mekanism;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.PathProvider;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;

public abstract class BaseEmiDefaults implements DataProvider {

    private static final Codec<List<Identifier>> CODEC = ExtraCodecs.nonEmptyList(Identifier.CODEC.listOf()).fieldOf("added").codec();

    private final CompletableFuture<HolderLookup.Provider> reloadableLookupProvider;
    private final Set<Identifier> recipes = new HashSet<>();
    private final PathProvider pathProvider;
    private final String modid;

    protected BaseEmiDefaults(PackOutput output, CompletableFuture<HolderLookup.Provider> reloadableLookupProvider, String modid) {
        this.pathProvider = output.createPathProvider(Target.RESOURCE_PACK, "recipe/defaults");
        this.reloadableLookupProvider = reloadableLookupProvider;
        this.modid = modid;
    }

    @Override
    public String getName() {
        return "EMI Default Recipe Provider: " + modid;
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput cachedOutput) {
        return this.reloadableLookupProvider.thenCompose(lookupProvider -> {
            addDefaults(lookupProvider);
            //Sort to make the output more stable
            List<Identifier> sortedRecipes = new ArrayList<>(recipes);
            sortedRecipes.sort(Identifier::compareNamespaced);
            Path path = pathProvider.json(Mekanism.hooks.emi.rl(modid));
            return DataProvider.saveStable(cachedOutput, lookupProvider, CODEC, sortedRecipes, path);
        });
    }

    protected abstract void addDefaults(HolderLookup.Provider reloadableLookupProvider);

    protected void addTieredRecipes(HolderLookup.Provider reloadableLookupProvider, String basePath) {
        for (BaseTier tier : BaseTier.VALUES) {
            if (tier != BaseTier.CREATIVE) {
                addRecipe(reloadableLookupProvider, basePath + tier.getLowerName());
            }
        }
    }

    protected void addRecipe(HolderLookup.Provider reloadableLookupProvider, DeferredHolder<?, ?> output) {
        addRecipe(reloadableLookupProvider, output.getId());
    }

    protected void addRotaryRecipe(ResourceKey<Chemical> chemical) {
        //Allow showing all gas -> fluid rotary recipes by default, in case someone needs a fluid variant that then it consistently gets them to the gas
        // But we don't bother with the decondensentrating ones
        addUncheckedRecipe(RegistryUtils.synthetic(Identifier.fromNamespaceAndPath(modid, "rotary/" + chemical.identifier().getPath()), "condensentrating"));
    }

    protected void addRecipe(HolderLookup.Provider reloadableLookupProvider, String recipePath) {
        addRecipe(reloadableLookupProvider, Identifier.fromNamespaceAndPath(modid, recipePath));
    }

    protected void addRecipe(HolderLookup.Provider reloadableLookupProvider, Identifier recipe) {
        if (recipeExists(reloadableLookupProvider, recipe)) {
            addUncheckedRecipe(recipe);
        } else {
            throw new IllegalArgumentException("Recipe '" + recipe + "' does not exist.");
        }
    }

    protected void addUncheckedRecipe(Identifier recipe) {
        if (!recipes.add(recipe)) {
            throw new IllegalArgumentException("Recipe '" + recipe + "' was added multiple times.");
        }
    }

    private boolean recipeExists(HolderLookup.Provider reloadableLookupProvider, Identifier location) {
        return reloadableLookupProvider.get(ResourceKey.create(Registries.RECIPE, location)).isPresent();
    }
}