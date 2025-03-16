package mekanism.tools.client;

import mekanism.client.model.BaseItemModelProvider;
import mekanism.common.Mekanism;
import mekanism.common.registration.INamedEntry;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.item.ItemMekanismPaxel;
import mekanism.tools.common.item.ItemMekanismShield;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile.ExistingModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ToolsItemModelProvider extends BaseItemModelProvider {

    public ToolsItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MekanismTools.MODID, existingFileHelper);
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
        for (Holder<Item> holder : ToolsItems.ITEMS.getEntries()) {
            if (holder.value() instanceof ItemMekanismShield) {
                //Skip shields, we manually handle them above
                continue;
            }
            String name = getPath(holder);
            ResourceLocation texture;
            if (isVanilla(holder, name)) {
                texture = itemTexture(holder);
            } else {
                int index = name.lastIndexOf('_');
                texture = modLoc("item/" + name.substring(0, index) + '/' + name.substring(index + 1));
            }
            armorOrHandheld(holder, texture);
        }
    }

    private boolean isVanilla(Holder<Item> item, String name) {
        if (item.value() instanceof ItemMekanismPaxel) {
            return name.startsWith("netherite") || name.startsWith("diamond") || name.startsWith("gold") || name.startsWith("iron") ||
                   name.startsWith("stone") || name.startsWith("wood");
        }
        return false;
    }

    private void addShieldModel(INamedEntry shield, ResourceLocation particle) {
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