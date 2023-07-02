package mekanism.client.texture;

import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismFluids;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;

public class MekanismSpriteSourceProvider extends BaseSpriteSourceProvider {

    public MekanismSpriteSourceProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, Mekanism.MODID, fileHelper);
    }

    @Override
    protected void addSources() {
        SourceList atlas = atlas(BLOCKS_ATLAS);
        addFiles(atlas, Mekanism.rl("liquid/energy"));
        addFiles(atlas, Mekanism.rl("liquid/heat"));
        addFiles(atlas, Mekanism.rl("icon/redstone_control_pulse"));

        //MekaSuit
        addFiles(atlas,
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

        //TODO - 1.20: Add javadocs stating that chemical resources now need to be added via sources???
        // is this even accurate? See if fluids need it as well. Chemicals potentially should use their own directory as well
        // just to simplify things? Or maybe we add manually and then also add a specific directory to make it easier for others
        addChemicalSprites(atlas);
        addFluids(atlas, MekanismFluids.FLUIDS);

        //TODO - 1.20: Mention in RobitSkin docs about adding to atlas if using a different path
        SourceList robitAtlas = atlas(Mekanism.rl("entity/robit"));
        addDirectory(robitAtlas, "entity/robit", "");
    }
}