package mekanism.additions.client.integration.emi;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.client.integration.emi.BaseEmiDefaults;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

@NothingNullByDefault
public class AdditionsEmiDefaults extends BaseEmiDefaults {

    public AdditionsEmiDefaults(PackOutput output, ExistingFileHelper existingFileHelper, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, existingFileHelper, registries, MekanismAdditions.MODID);
    }

    @Override
    protected void addDefaults(HolderLookup.Provider lookupProvider) {
        addRecipe(AdditionsItems.WALKIE_TALKIE);
        addRecipe(AdditionsBlocks.OBSIDIAN_TNT);
        addRecipes("balloon/", AdditionsItems.BALLOONS, true);
        addRecipes("glow_panel/", AdditionsBlocks.GLOW_PANELS, true);
        addPlasticBlocks();
    }

    private void addRecipes(String basePath, Map<EnumColor, ?> map, boolean requiresDye) {
        for (EnumColor color : map.keySet()) {
            if (!requiresDye || color.getDyeColor() != null) {
                addRecipe(basePath + color.getRegistryPrefix());
            }
        }
    }

    private void addPlasticBlocks() {
        String basePath = "plastic/";
        addRecipes(basePath + "fence/", AdditionsBlocks.PLASTIC_FENCES, false);
        addRecipes(basePath + "fence_gate/", AdditionsBlocks.PLASTIC_FENCE_GATES, false);
        addPlasticSlabs(basePath);
        addPlasticStairs(basePath);
        addRecipes(basePath + "block/", AdditionsBlocks.PLASTIC_BLOCKS, true);
        addRecipes(basePath + "glow/", AdditionsBlocks.PLASTIC_GLOW_BLOCKS, false);
        addRecipes(basePath + "reinforced/", AdditionsBlocks.REINFORCED_PLASTIC_BLOCKS, false);
        addRecipes(basePath + "road/", AdditionsBlocks.PLASTIC_ROADS, false);
        addRecipes(basePath + "slick/enriching/", AdditionsBlocks.SLICK_PLASTIC_BLOCKS, false);
        addRecipes(basePath + "transparent/", AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS, true);
    }

    private void addPlasticSlabs(String basePath) {
        basePath += "slab/";
        addRecipes(basePath, AdditionsBlocks.PLASTIC_SLABS, false);
        addRecipes(basePath + "transparent/", AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS, false);
        addRecipes(basePath + "glow/", AdditionsBlocks.PLASTIC_GLOW_SLABS, false);
    }

    private void addPlasticStairs(String basePath) {
        basePath += "stairs/";
        addRecipes(basePath, AdditionsBlocks.PLASTIC_STAIRS, false);
        addRecipes(basePath + "transparent/", AdditionsBlocks.TRANSPARENT_PLASTIC_STAIRS, false);
        addRecipes(basePath + "glow/", AdditionsBlocks.PLASTIC_GLOW_STAIRS, false);
    }
}