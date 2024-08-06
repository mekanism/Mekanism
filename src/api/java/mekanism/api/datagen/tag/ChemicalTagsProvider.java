package mekanism.api.datagen.tag;

import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

/**
 * Helper classes for implementing tag providers for various chemical types.
 */
public abstract class ChemicalTagsProvider extends IntrinsicHolderTagsProvider<Chemical> {

    protected ChemicalTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider,
          String modid, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, MekanismAPI.CHEMICAL_REGISTRY_NAME, lookupProvider, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()),
              chemical -> ResourceKey.create(MekanismAPI.CHEMICAL_REGISTRY_NAME, chemical.getRegistryName()), modid, existingFileHelper);
    }
}