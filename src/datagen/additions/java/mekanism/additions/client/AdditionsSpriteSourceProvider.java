package mekanism.additions.client;

import mekanism.additions.common.MekanismAdditions;
import mekanism.client.texture.BaseSpriteSourceProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class AdditionsSpriteSourceProvider extends BaseSpriteSourceProvider {

    public AdditionsSpriteSourceProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, MekanismAdditions.MODID, fileHelper);
    }

    @Override
    protected void addSources() {
        SourceList atlas = atlas(BLOCKS_ATLAS);
        addFiles(atlas, MekanismAdditions.rl("entity/balloon"), MekanismAdditions.rl("entity/balloon_string"));
    }
}