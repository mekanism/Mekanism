package mekanism.client.integration.emi;

import com.mojang.serialization.Codec;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.tier.BaseTier;
import mekanism.common.DataGenSerializationConstants;
import mekanism.common.Mekanism;
import mekanism.common.registration.INamedEntry;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.PathProvider;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;

@NothingNullByDefault
public abstract class BaseEmiDefaults implements DataProvider {

    private static final Codec<List<Identifier>> CODEC = ExtraCodecs.nonEmptyList(Identifier.CODEC.listOf())
          .fieldOf(DataGenSerializationConstants.ADDED)
          .codec();

    private final CompletableFuture<HolderLookup.Provider> registries;
    private final Set<Identifier> recipes = new HashSet<>();
    private final ResourceManager serverResources;
    private final PathProvider pathProvider;
    private final String modid;

    protected BaseEmiDefaults(PackOutput output, ResourceManager serverResources, CompletableFuture<HolderLookup.Provider> registries, String modid) {
        this.pathProvider = output.createPathProvider(Target.RESOURCE_PACK, "recipe/defaults");
        this.serverResources = serverResources;
        this.registries = registries;
        this.modid = modid;
    }

    @Override
    public String getName() {
        return "EMI Default Recipe Provider: " + modid;
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput cachedOutput) {
        return this.registries.thenCompose(lookupProvider -> {
            addDefaults(lookupProvider);
            //Sort to make the output more stable
            List<Identifier> sortedRecipes = new ArrayList<>(recipes);
            sortedRecipes.sort(Identifier::compareNamespaced);
            Path path = pathProvider.json(Mekanism.hooks.emi.rl(modid));
            return DataProvider.saveStable(cachedOutput, lookupProvider, CODEC, sortedRecipes, path);
        });
    }

    protected abstract void addDefaults(HolderLookup.Provider lookupProvider);

    protected void addTieredRecipes(String basePath) {
        for (BaseTier tier : EnumUtils.TIERS) {
            if (tier != BaseTier.CREATIVE) {
                addRecipe(basePath + tier.getLowerName());
            }
        }
    }

    protected void addRecipe(DeferredHolder<?, ?> output) {
        addRecipe(output.getId());
    }

    protected void addRotaryRecipe(INamedEntry gas) {
        //Allow showing all gas -> fluid rotary recipes by default, in case someone needs a fluid variant that then it consistently gets them to the gas
        // But we don't bother with the decondensentrating ones
        addUncheckedRecipe(RegistryUtils.synthetic(Identifier.fromNamespaceAndPath(modid, "rotary/" + gas.getName()), "condensentrating"));
    }

    protected void addRecipe(String recipePath) {
        addRecipe(Identifier.fromNamespaceAndPath(modid, recipePath));
    }

    protected void addRecipe(Identifier recipe) {
        if (recipeExists(recipe)) {
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

    public boolean recipeExists(Identifier location) {
        return serverResources.getResource(location.withPrefix("recipes/").withSuffix(".json")).isPresent();
    }
}