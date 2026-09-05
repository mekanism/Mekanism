package mekanism.tools.client;

import java.util.concurrent.CompletableFuture;
import mekanism.client.texture.BaseSpriteSourceProvider;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.material.MaterialType;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.AtlasIds;
import net.minecraft.data.PackOutput;

public class ToolsSpriteSourceProvider extends BaseSpriteSourceProvider {

    public ToolsSpriteSourceProvider(PackOutput output, CompletableFuture<Provider> lookupProvider) {
        super(output, MekanismTools.MODID, lookupProvider);
    }

    @Override
    protected void gather() {
        SourceList atlas = atlas(AtlasIds.SHIELD_PATTERNS);
        for (MaterialType material : MaterialType.VALUES) {
            addFiles(atlas, MekanismTools.rl(material.getSerializedName()).withPrefix("entity/shield/"));
        }
    }
}