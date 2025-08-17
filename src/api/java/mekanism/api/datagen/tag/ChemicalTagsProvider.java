package mekanism.api.datagen.tag;

import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.TagsProvider;

/**
 * Helper class for implementing tag providers for chemicals.
 */
public abstract class ChemicalTagsProvider extends IntrinsicHolderTagsProvider<Chemical> {

    protected ChemicalTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, String modid) {
        super(packOutput, MekanismAPI.CHEMICAL_REGISTRY_NAME, lookupProvider, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()),
              chemical -> MekanismAPI.CHEMICAL_REGISTRY.getResourceKey(chemical).orElseThrow(), modid);
    }
}