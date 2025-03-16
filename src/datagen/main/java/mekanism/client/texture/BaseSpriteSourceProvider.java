package mekanism.client.texture;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidDeferredRegister.MekanismFluidType;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;
import net.neoforged.neoforge.fluids.FluidType;

public abstract class BaseSpriteSourceProvider extends SpriteSourceProvider {

    private final Set<ResourceLocation> trackedSingles = new HashSet<>();

    protected BaseSpriteSourceProvider(PackOutput output, String modid, ExistingFileHelper fileHelper, CompletableFuture<Provider> lookupProvider) {
        super(output, lookupProvider, modid, fileHelper);
    }

    protected void addFiles(SourceList atlas, List<ResourceLocation> resourceLocations) {
        addFiles(atlas, resourceLocations.stream().sorted(ResourceLocation::compareNamespaced).toArray(ResourceLocation[]::new));
    }

    protected void addFiles(SourceList atlas, ResourceLocation... resourceLocations) {
        for (ResourceLocation rl : resourceLocations) {
            //Only add this source if we haven't already added it as a direct single file source
            if (trackedSingles.add(rl)) {
                atlas.addSource(new SingleFile(rl, Optional.empty()));
            }
        }
    }

    //TODO - 1.20: Re-evaluate doing this
    protected void addChemicalSprites(SourceList atlas) {
        List<ResourceLocation> icons = new ArrayList<>();
        for (Map.Entry<ResourceKey<Chemical>, Chemical> entry : MekanismAPI.CHEMICAL_REGISTRY.entrySet()) {
            if (entry.getKey().location().getNamespace().equals(modid)) {
                icons.add(entry.getValue().getIcon());
            }
        }
        addFiles(atlas, icons);
    }

    protected void addFluids(SourceList atlas, FluidDeferredRegister register) {
        List<ResourceLocation> icons = new ArrayList<>();
        for (Holder<FluidType> holder : register.getFluidTypeEntries()) {
            //Note: This should always be the case
            if (holder.value() instanceof MekanismFluidType fluidType) {
                icons.add(fluidType.stillTexture);
                icons.add(fluidType.flowingTexture);
                icons.add(fluidType.overlayTexture);
            }
        }
        addFiles(atlas, icons);
    }

    protected void addDirectory(SourceList atlas, String directory, String spritePrefix) {
        atlas.addSource(new DirectoryLister(directory, spritePrefix));
    }
}