package mekanism.tools.client;

import mekanism.api.providers.IItemProvider;
import mekanism.client.model.BaseItemModelProvider;
import mekanism.common.Mekanism;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.item.ItemMekanismPaxel;
import mekanism.tools.common.item.ItemMekanismShield;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile.ExistingModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ToolsItemModelProvider extends BaseItemModelProvider {

    public ToolsItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MekanismTools.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //Shields
        addShieldModel(ToolsItems.BRONZE_SHIELD, Mekanism.rl("block/block_bronze"));
        addShieldModel(ToolsItems.LAPIS_LAZULI_SHIELD, mcLoc("block/lapis_block"));
        addShieldModel(ToolsItems.OSMIUM_SHIELD, Mekanism.rl("block/block_osmium"));
        addShieldModel(ToolsItems.REFINED_GLOWSTONE_SHIELD, Mekanism.rl("block/block_refined_glowstone"));
        addShieldModel(ToolsItems.REFINED_OBSIDIAN_SHIELD, Mekanism.rl("block/block_refined_obsidian"));
        addShieldModel(ToolsItems.STEEL_SHIELD, Mekanism.rl("block/block_steel"));
        //Armor items are generated textures, all other tools module items are handheld
        for (IItemProvider itemProvider : ToolsItems.ITEMS.getAllItems()) {
            Item item = itemProvider.getItem();
            if (item instanceof ItemMekanismShield) {
                //Skip shields, we manually handle them above
                continue;
            }
            ResourceLocation texture;
            if (isVanilla(itemProvider)) {
                texture = itemTexture(itemProvider);
            } else {
                String name = itemProvider.getName();
                int index = name.lastIndexOf('_');
                texture = modLoc("item/" + name.substring(0, index) + '/' + name.substring(index + 1));
            }
            if (item instanceof ArmorItem) {
                generated(itemProvider, texture);
            } else {
                handheld(itemProvider, texture);
            }
        }
    }

    private boolean isVanilla(IItemProvider itemProvider) {
        if (itemProvider.getItem() instanceof ItemMekanismPaxel) {
            String name = itemProvider.getName();
            return name.startsWith("netherite") || name.startsWith("diamond") || name.startsWith("gold") || name.startsWith("iron") ||
                   name.startsWith("stone") || name.startsWith("wood");
        }
        return false;
    }

    private void addShieldModel(IItemProvider shield, ResourceLocation particle) {
        ItemModelBuilder blockingModel = getBuilder(shield.getName() + "_blocking")
              .parent(new ExistingModelFile(mcLoc(folder + "/shield_blocking"), existingFileHelper))
              .texture("particle", particle);
        getBuilder(shield.getName())
              .parent(new ExistingModelFile(mcLoc(folder + "/shield"), existingFileHelper))
              .texture("particle", particle)
              .override()
              .predicate(modLoc("blocking"), 1)
              .model(blockingModel)
              .end();
    }
}