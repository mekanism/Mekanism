package mekanism.generators.client;

import mekanism.client.texture.BaseSpriteSourceProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsFluids;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class GeneratorsSpriteSourceProvider extends BaseSpriteSourceProvider {

    public GeneratorsSpriteSourceProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, MekanismGenerators.MODID, fileHelper);
    }

    @Override
    protected void addSources() {
        SourceList atlas = atlas(BLOCKS_ATLAS);
        addChemicalSprites(atlas);
        addFluids(atlas, GeneratorsFluids.FLUIDS);
    }
}