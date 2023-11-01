package mekanism.client.texture;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidDeferredRegister.MekanismFluidType;
import mekanism.common.registration.impl.FluidRegistryObject;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;
import net.neoforged.neoforge.registries.IForgeRegistry;

public abstract class BaseSpriteSourceProvider extends SpriteSourceProvider {

    private final Set<ResourceLocation> trackedSingles = new HashSet<>();

    protected BaseSpriteSourceProvider(PackOutput output, String modid, ExistingFileHelper fileHelper) {
        super(output, fileHelper, modid);
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
        addChemicalSprites(atlas, MekanismAPI.gasRegistry());
        addChemicalSprites(atlas, MekanismAPI.infuseTypeRegistry());
        addChemicalSprites(atlas, MekanismAPI.pigmentRegistry());
        addChemicalSprites(atlas, MekanismAPI.slurryRegistry());
    }

    private <CHEMICAL extends Chemical<CHEMICAL>> void addChemicalSprites(SourceList atlas, IForgeRegistry<CHEMICAL> chemicalRegistry) {
        for (Chemical<?> chemical : chemicalRegistry.getValues()) {
            //TODO - 1.20: Evaluate this
            if (chemical.getRegistryName().getNamespace().equals(modid)) {
                addFiles(atlas, chemical.getIcon());
            }
        }
    }

    protected void addFluids(SourceList atlas, FluidDeferredRegister register) {
        for (FluidRegistryObject<? extends MekanismFluidType, ?, ?, ?, ?> fluidRO : register.getAllFluids()) {
            MekanismFluidType fluidType = fluidRO.getFluidType();
            addFiles(atlas, fluidType.stillTexture, fluidType.flowingTexture, fluidType.overlayTexture);
        }
    }

    protected void addDirectory(SourceList atlas, String directory, String spritePrefix) {
        atlas.addSource(new DirectoryLister(directory, spritePrefix));
    }
}