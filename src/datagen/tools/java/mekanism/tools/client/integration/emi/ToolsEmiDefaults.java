package mekanism.tools.client.integration.emi;

import java.util.concurrent.CompletableFuture;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.integration.emi.BaseEmiDefaults;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

@NothingNullByDefault
public class ToolsEmiDefaults extends BaseEmiDefaults {

    public ToolsEmiDefaults(PackOutput output, ExistingFileHelper existingFileHelper, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, existingFileHelper, registries, MekanismTools.MODID);
    }

    @Override
    protected void addDefaults(HolderLookup.Provider lookupProvider) {
        addDefaults("bronze");
        addDefaults("lapis_lazuli");
        addDefaults("osmium");
        addDefaults("refined_glowstone");
        addDefaults("refined_obsidian");
        addDefaults("steel");
        addVanillaPaxelDefaults();
    }

    private void addDefaults(String name) {
        //Emi adds default for all vanilla tools and armor crafting recipes (not smelting)
        String baseArmorPath = name + "/armor/";
        addRecipe(baseArmorPath + "helmet");
        addRecipe(baseArmorPath + "chestplate");
        addRecipe(baseArmorPath + "leggings");
        addRecipe(baseArmorPath + "boots");
        addRecipe(name + "/shield");
        String baseToolsPath = name + "/tools/";
        addRecipe(baseToolsPath + "sword");
        addRecipe(baseToolsPath + "pickaxe");
        addRecipe(baseToolsPath + "axe");
        addRecipe(baseToolsPath + "shovel");
        addRecipe(baseToolsPath + "hoe");
        addRecipe(baseToolsPath + "paxel");
    }

    private void addVanillaPaxelDefaults() {
        addRecipe(ToolsItems.WOOD_PAXEL);
        addRecipe(ToolsItems.STONE_PAXEL);
        addRecipe(ToolsItems.IRON_PAXEL);
        addRecipe(ToolsItems.GOLD_PAXEL);
        addRecipe(ToolsItems.DIAMOND_PAXEL);
    }
}