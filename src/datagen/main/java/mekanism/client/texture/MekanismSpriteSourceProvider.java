package mekanism.client.texture;

import java.util.concurrent.CompletableFuture;
import mekanism.client.RobitSpriteUploader;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.AtlasIds;
import net.minecraft.data.PackOutput;

public class MekanismSpriteSourceProvider extends BaseSpriteSourceProvider {

    public MekanismSpriteSourceProvider(PackOutput output, CompletableFuture<Provider> lookupProvider) {
        super(output, Mekanism.MODID, lookupProvider);
    }

    @Override
    protected void gather() {
        SourceList blockAtlas = atlas(AtlasIds.BLOCKS);
        SourceList itemAtlas = atlas(AtlasIds.ITEMS);

        //MekaSuit
        addFiles(itemAtlas,
              Mekanism.rl("entity/armor/blank"),
              Mekanism.rl("entity/armor/mekasuit_player"),
              Mekanism.rl("entity/armor/mekasuit_armor_body"),
              Mekanism.rl("entity/armor/mekasuit_armor_helmet"),
              Mekanism.rl("entity/armor/mekasuit_armor_exoskeleton"),
              Mekanism.rl("entity/armor/mekasuit_gravitational_modulator"),
              Mekanism.rl("entity/armor/mekasuit_elytra"),
              Mekanism.rl("entity/armor/mekasuit_armor_modules"),
              Mekanism.rl("entity/armor/mekatool")
        );

        blockAtlas.addSource(new DirectoryLister("mek_liquid", "mek_liquid/"));
        blockAtlas.addSource(new DirectoryLister("mek_chemical", "mek_chemical/"));

        SourceList robitAtlas = atlas(RobitSpriteUploader.ATLAS_ID);
        addDirectory(robitAtlas, "entity/robit", "");
    }
}