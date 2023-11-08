package mekanism.tools.client;

import java.util.concurrent.CompletableFuture;
import mekanism.client.texture.BaseSpriteSourceProvider;
import mekanism.tools.common.MekanismTools;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ToolsSpriteSourceProvider extends BaseSpriteSourceProvider {

    public ToolsSpriteSourceProvider(PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<Provider> lookupProvider) {
        super(output, MekanismTools.MODID, fileHelper, lookupProvider);
    }

    @Override
    protected void gather() {
        SourceList atlas = atlas(SHIELD_PATTERNS_ATLAS);
        for (ShieldTextures textures : ShieldTextures.values()) {
            addFiles(atlas, textures.getBase().texture());
        }
    }
}