package mekanism.tools.client.integration.emi;

import java.util.concurrent.CompletableFuture;
import mekanism.client.integration.emi.BaseEmiDefaults;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

public class ToolsEmiDefaults extends BaseEmiDefaults {

    public ToolsEmiDefaults(PackOutput output, CompletableFuture<HolderLookup.Provider> reloadableLookupProvider) {
        super(output, reloadableLookupProvider, MekanismTools.MODID);
    }

    @Override
    protected void addDefaults(HolderLookup.Provider reloadableLookupProvider) {
        addDefaults(reloadableLookupProvider, "bronze");
        addDefaults(reloadableLookupProvider, "lapis_lazuli");
        addDefaults(reloadableLookupProvider, "osmium");
        addDefaults(reloadableLookupProvider, "refined_glowstone");
        addDefaults(reloadableLookupProvider, "refined_obsidian");
        addDefaults(reloadableLookupProvider, "steel");
        ToolsItems.vanillaPaxels().forEach(paxel -> {
            if (paxel != ToolsItems.NETHERITE_PAXEL) {
                addRecipe(reloadableLookupProvider, paxel);
            }
        });
    }

    private void addDefaults(HolderLookup.Provider reloadableLookupProvider, String name) {
        //Emi adds default for all vanilla tools and armor crafting recipes (not smelting)
        String baseArmorPath = name + "/armor/";
        addRecipe(reloadableLookupProvider, baseArmorPath + "helmet");
        addRecipe(reloadableLookupProvider, baseArmorPath + "chestplate");
        addRecipe(reloadableLookupProvider, baseArmorPath + "leggings");
        addRecipe(reloadableLookupProvider, baseArmorPath + "boots");
        addRecipe(reloadableLookupProvider, name + "/shield");
        String baseToolsPath = name + "/tools/";
        addRecipe(reloadableLookupProvider, baseToolsPath + "sword");
        addRecipe(reloadableLookupProvider, baseToolsPath + "pickaxe");
        addRecipe(reloadableLookupProvider, baseToolsPath + "axe");
        addRecipe(reloadableLookupProvider, baseToolsPath + "shovel");
        addRecipe(reloadableLookupProvider, baseToolsPath + "hoe");
        addRecipe(reloadableLookupProvider, baseToolsPath + "paxel");
    }
}