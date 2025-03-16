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
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.common.DataGenSerializationConstants;
import mekanism.common.Mekanism;
import mekanism.common.registration.INamedEntry;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.PathProvider;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

@NothingNullByDefault
public abstract class BaseEmiDefaults implements DataProvider {

    private static final Codec<List<ResourceLocation>> CODEC = ExtraCodecs.nonEmptyList(ResourceLocation.CODEC.listOf())
          .fieldOf(DataGenSerializationConstants.ADDED)
          .codec();

    private final CompletableFuture<HolderLookup.Provider> registries;
    private final Set<ResourceLocation> recipes = new HashSet<>();
    private final ExistingFileHelper existingFileHelper;
    private final PathProvider pathProvider;
    private final String modid;

    protected BaseEmiDefaults(PackOutput output, ExistingFileHelper existingFileHelper, CompletableFuture<HolderLookup.Provider> registries, String modid) {
        this.pathProvider = output.createPathProvider(Target.RESOURCE_PACK, "recipe/defaults");
        this.existingFileHelper = existingFileHelper;
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
            List<ResourceLocation> sortedRecipes = new ArrayList<>(recipes);
            sortedRecipes.sort(ResourceLocation::compareNamespaced);
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
        addUncheckedRecipe(RecipeViewerUtils.synthetic(ResourceLocation.fromNamespaceAndPath(modid, "rotary/" + gas.getName()), "condensentrating"));
    }

    protected void addRecipe(String recipePath) {
        addRecipe(ResourceLocation.fromNamespaceAndPath(modid, recipePath));
    }

    protected void addRecipe(ResourceLocation recipe) {
        if (recipeExists(recipe)) {
            addUncheckedRecipe(recipe);
        } else {
            throw new IllegalArgumentException("Recipe '" + recipe + "' does not exist.");
        }
    }

    protected void addUncheckedRecipe(ResourceLocation recipe) {
        if (!recipes.add(recipe)) {
            throw new IllegalArgumentException("Recipe '" + recipe + "' was added multiple times.");
        }
    }

    public boolean recipeExists(ResourceLocation location) {
        return existingFileHelper.exists(location, PackType.SERVER_DATA, ".json", "recipes");
    }
}