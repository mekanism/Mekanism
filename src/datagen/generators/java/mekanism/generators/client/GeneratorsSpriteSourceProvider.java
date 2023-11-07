package mekanism.generators.client;

import java.util.concurrent.CompletableFuture;
import mekanism.client.texture.BaseSpriteSourceProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsFluids;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class GeneratorsSpriteSourceProvider extends BaseSpriteSourceProvider {

    public GeneratorsSpriteSourceProvider(PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<Provider> lookupProvider) {
        super(output, MekanismGenerators.MODID, fileHelper, lookupProvider);
    }

    @Override
    protected void gather() {
        SourceList atlas = atlas(BLOCKS_ATLAS);
        addChemicalSprites(atlas);
        addFluids(atlas, GeneratorsFluids.FLUIDS);
    }
}