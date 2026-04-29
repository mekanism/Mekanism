package mekanism.tools.client;

import mekanism.client.model.BaseModelProvider;
import mekanism.common.Mekanism;
import mekanism.common.registration.INamedEntry;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.item.ItemMekanismPaxel;
import mekanism.tools.common.item.ItemMekanismShield;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;

public class ToolsItemModelProvider extends BaseModelProvider {

    public ToolsItemModelProvider(PackOutput output, ResourceManager clientResources) {
        super(output, MekanismTools.MODID, clientResources);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
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
            Identifier texture;
            if (isVanilla(holder, name)) {
                texture = itemTexture(holder);
            } else {
                int index = name.lastIndexOf('_');
                texture = modLocation("item/" + name.substring(0, index) + '/' + name.substring(index + 1));
            }
            armorOrHandheld(holder, texture);
        }
    }

    protected ItemModelBuilder generated(Holder<Item> item, Identifier texture) {
        return withExistingParent(getPath(item), "item/generated").texture("layer0", texture);
    }

    protected void armorOrHandheld(Holder<Item> holder, Identifier texture) {
        if (holder.value() instanceof ArmorItem armorItem) {
            //todo net.minecraft.client.data.models.ItemModelGenerators.generateTrimmableItem ?
            ItemModelBuilder builder = generated(holder, texture);
            for (ItemModelGenerators.TrimMaterialData trimModelData : ItemModelGenerators.TRIM_MATERIAL_MODELS) {
                String trimId = trimModelData.name(armorItem.getMaterial());
                ItemModelBuilder override = withExistingParent(builder.getLocation().withSuffix("_" + trimId + "_trim").getPath(), "item/generated")
                      .texture("layer0", texture)
                      .texture("layer1", Identifier.withDefaultNamespace("trims/items/" + armorItem.getType().getName() + "_trim_" + trimId));
                builder.override()
                      .predicate(ItemModelGenerators.TRIM_TYPE_PREDICATE_ID, trimModelData.itemModelIndex())
                      .model(override);
            }
            return builder;
        }
        return withExistingParent(getPath(holder), "item/handheld").texture("layer0", texture);
    }

    private boolean isVanilla(Holder<Item> item, String name) {
        if (item.value() instanceof ItemMekanismPaxel) {
            return name.startsWith("netherite") || name.startsWith("diamond") || name.startsWith("gold") || name.startsWith("iron") ||
                   name.startsWith("stone") || name.startsWith("wood");
        }
        return false;
    }

    private void addShieldModel(INamedEntry shield, Identifier particle) {
        ItemModelBuilder blockingModel = getBuilder(shield.getName() + "_blocking")
              .parent(new ExistingModelFile(mcLoc(folder + "/shield_blocking"), existingFileHelper))
              .texture("particle", particle);
        getBuilder(shield.getName())
              .parent(new ExistingModelFile(mcLoc(folder + "/shield"), existingFileHelper))
              .texture("particle", particle)
              .override()
              .predicate(modLocation("blocking"), 1)
              .model(blockingModel)
              .end();
    }
}