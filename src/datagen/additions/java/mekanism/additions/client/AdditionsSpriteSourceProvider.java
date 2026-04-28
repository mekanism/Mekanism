package mekanism.additions.client;

import java.util.concurrent.CompletableFuture;
import mekanism.additions.common.MekanismAdditions;
import mekanism.client.texture.BaseSpriteSourceProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.AtlasIds;
import net.minecraft.data.PackOutput;

public class AdditionsSpriteSourceProvider extends BaseSpriteSourceProvider {

    public AdditionsSpriteSourceProvider(PackOutput output, CompletableFuture<Provider> lookupProvider) {
        super(output, MekanismAdditions.MODID, lookupProvider);
    }

    @Override
    protected void gather() {
        SourceList atlas = atlas(AtlasIds.BLOCKS);
        addFiles(atlas, MekanismAdditions.rl("entity/balloon"), MekanismAdditions.rl("entity/balloon_string"));
    }
}