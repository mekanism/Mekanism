package mekanism.generators.client;

import java.util.concurrent.CompletableFuture;
import mekanism.client.texture.BaseSpriteSourceProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsFluids;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.AtlasIds;
import net.minecraft.data.PackOutput;

public class GeneratorsSpriteSourceProvider extends BaseSpriteSourceProvider {

    public GeneratorsSpriteSourceProvider(PackOutput output, CompletableFuture<Provider> lookupProvider) {
        super(output, MekanismGenerators.MODID, lookupProvider);
    }

    @Override
    protected void gather() {
        SourceList atlas = atlas(AtlasIds.BLOCKS);
        addChemicalSprites(atlas);
        addFluids(atlas, GeneratorsFluids.FLUIDS);
    }
}