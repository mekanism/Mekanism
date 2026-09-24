package mekanism.additions.client.integration.emi;

import java.util.concurrent.CompletableFuture;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.text.EnumColor;
import mekanism.client.integration.emi.BaseEmiDefaults;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

public class AdditionsEmiDefaults extends BaseEmiDefaults {

    public AdditionsEmiDefaults(PackOutput output, CompletableFuture<HolderLookup.Provider> reloadableLookupProvider) {
        super(output, reloadableLookupProvider, MekanismAdditions.MODID);
    }

    @Override
    protected void addDefaults(HolderLookup.Provider reloadableLookupProvider) {
        addRecipe(reloadableLookupProvider, AdditionsItems.WALKIE_TALKIE);
        addRecipe(reloadableLookupProvider, AdditionsBlocks.OBSIDIAN_TNT);
        addColoredRecipes(reloadableLookupProvider, "balloon/", true);
        addColoredRecipes(reloadableLookupProvider, "glow_panel/", true);
        addPlasticBlocks(reloadableLookupProvider);
    }

    private void addColoredRecipes(HolderLookup.Provider reloadableLookupProvider, String basePath, boolean requiresDye) {
        for (EnumColor color : EnumColor.VALUES) {
            if (!requiresDye || color.getDyeColor() != null) {
                addRecipe(reloadableLookupProvider, basePath + color.getRegistryPrefix());
            }
        }
    }

    private void addPlasticBlocks(HolderLookup.Provider reloadableLookupProvider) {
        String basePath = "plastic/";
        addColoredRecipes(reloadableLookupProvider, basePath + "fence/", false);
        addColoredRecipes(reloadableLookupProvider, basePath + "fence_gate/", false);
        addPlasticSlabs(reloadableLookupProvider, basePath);
        addPlasticStairs(reloadableLookupProvider, basePath);
        addColoredRecipes(reloadableLookupProvider, basePath + "block/", true);
        addColoredRecipes(reloadableLookupProvider, basePath + "glow/", false);
        addColoredRecipes(reloadableLookupProvider, basePath + "reinforced/", false);
        addColoredRecipes(reloadableLookupProvider, basePath + "road/", false);
        addColoredRecipes(reloadableLookupProvider, basePath + "slick/enriching/", false);
        addColoredRecipes(reloadableLookupProvider, basePath + "transparent/", true);
    }

    private void addPlasticSlabs(HolderLookup.Provider reloadableLookupProvider, String basePath) {
        basePath += "slab/";
        addColoredRecipes(reloadableLookupProvider, basePath, false);
        addColoredRecipes(reloadableLookupProvider, basePath + "transparent/", false);
        addColoredRecipes(reloadableLookupProvider, basePath + "glow/", false);
    }

    private void addPlasticStairs(HolderLookup.Provider reloadableLookupProvider, String basePath) {
        basePath += "stairs/";
        addColoredRecipes(reloadableLookupProvider, basePath, false);
        addColoredRecipes(reloadableLookupProvider, basePath + "transparent/", false);
        addColoredRecipes(reloadableLookupProvider, basePath + "glow/", false);
    }
}