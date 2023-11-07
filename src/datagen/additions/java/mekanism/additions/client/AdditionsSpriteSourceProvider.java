package mekanism.additions.client;

import java.util.concurrent.CompletableFuture;
import mekanism.additions.common.MekanismAdditions;
import mekanism.client.texture.BaseSpriteSourceProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class AdditionsSpriteSourceProvider extends BaseSpriteSourceProvider {

    public AdditionsSpriteSourceProvider(PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<Provider> lookupProvider) {
        super(output, MekanismAdditions.MODID, fileHelper, lookupProvider);
    }

    @Override
    protected void gather() {
        SourceList atlas = atlas(BLOCKS_ATLAS);
        addFiles(atlas, MekanismAdditions.rl("entity/balloon"), MekanismAdditions.rl("entity/balloon_string"));
    }
}