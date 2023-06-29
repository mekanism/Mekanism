package mekanism.tools.client;

import mekanism.client.texture.BaseSpriteSourceProvider;
import mekanism.tools.common.MekanismTools;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ToolsSpriteSourceProvider extends BaseSpriteSourceProvider {

    public ToolsSpriteSourceProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, MekanismTools.MODID, fileHelper);
    }

    @Override
    protected void addSources() {
        SourceList atlas = atlas(SHIELD_PATTERNS_ATLAS);
        for (ShieldTextures textures : ShieldTextures.values()) {
            addFiles(atlas, textures.getBase().texture());
        }
    }
}