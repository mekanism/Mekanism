package mekanism.additions.client.integration.emi;

import java.util.concurrent.CompletableFuture;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.text.EnumColor;
import mekanism.client.integration.emi.BaseEmiDefaults;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.resources.ResourceManager;

public class AdditionsEmiDefaults extends BaseEmiDefaults {

    public AdditionsEmiDefaults(PackOutput output, ResourceManager serverResources, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, serverResources, registries, MekanismAdditions.MODID);
    }

    @Override
    protected void addDefaults(HolderLookup.Provider lookupProvider) {
        addRecipe(AdditionsItems.WALKIE_TALKIE);
        addRecipe(AdditionsBlocks.OBSIDIAN_TNT);
        addColoredRecipes("balloon/", true);
        addColoredRecipes("glow_panel/", true);
        addPlasticBlocks();
    }

    private void addColoredRecipes(String basePath, boolean requiresDye) {
        for (EnumColor color : EnumUtils.COLORS) {
            if (!requiresDye || color.getDyeColor() != null) {
                addRecipe(basePath + color.getRegistryPrefix());
            }
        }
    }

    private void addPlasticBlocks() {
        String basePath = "plastic/";
        addColoredRecipes(basePath + "fence/", false);
        addColoredRecipes(basePath + "fence_gate/", false);
        addPlasticSlabs(basePath);
        addPlasticStairs(basePath);
        addColoredRecipes(basePath + "block/", true);
        addColoredRecipes(basePath + "glow/", false);
        addColoredRecipes(basePath + "reinforced/", false);
        addColoredRecipes(basePath + "road/", false);
        addColoredRecipes(basePath + "slick/enriching/", false);
        addColoredRecipes(basePath + "transparent/", true);
    }

    private void addPlasticSlabs(String basePath) {
        basePath += "slab/";
        addColoredRecipes(basePath, false);
        addColoredRecipes(basePath + "transparent/", false);
        addColoredRecipes(basePath + "glow/", false);
    }

    private void addPlasticStairs(String basePath) {
        basePath += "stairs/";
        addColoredRecipes(basePath, false);
        addColoredRecipes(basePath + "transparent/", false);
        addColoredRecipes(basePath + "glow/", false);
    }
}