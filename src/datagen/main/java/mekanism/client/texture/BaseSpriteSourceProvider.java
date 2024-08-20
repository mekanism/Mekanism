package mekanism.client.texture;

import java.util.HashSet;
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
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;
import net.neoforged.neoforge.fluids.FluidType;

public abstract class BaseSpriteSourceProvider extends SpriteSourceProvider {

    private final Set<ResourceLocation> trackedSingles = new HashSet<>();

    protected BaseSpriteSourceProvider(PackOutput output, String modid, ExistingFileHelper fileHelper, CompletableFuture<Provider> lookupProvider) {
        super(output, lookupProvider, modid, fileHelper);
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
        for (Chemical chemical : MekanismAPI.CHEMICAL_REGISTRY) {
            if (chemical.getRegistryName().getNamespace().equals(modid)) {
                addFiles(atlas, chemical.getIcon());
            }
        }
    }

    protected void addFluids(SourceList atlas, FluidDeferredRegister register) {
        for (Holder<FluidType> holder : register.getFluidTypeEntries()) {
            //Note: This should always be the case
            if (holder.value() instanceof MekanismFluidType fluidType) {
                addFiles(atlas, fluidType.stillTexture, fluidType.flowingTexture, fluidType.overlayTexture);
            }
        }
    }

    protected void addDirectory(SourceList atlas, String directory, String spritePrefix) {
        atlas.addSource(new DirectoryLister(directory, spritePrefix));
    }
}