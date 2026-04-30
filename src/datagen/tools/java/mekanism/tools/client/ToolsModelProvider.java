package mekanism.tools.client;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.model.BaseModelProvider;
import mekanism.common.Mekanism;
import mekanism.tools.client.render.item.RenderMekanismShieldItem;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.item.ItemMekanismArmor;
import mekanism.tools.common.item.ItemMekanismPaxel;
import mekanism.tools.common.item.ItemMekanismShield;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.TrimMaterialProperty;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ToolsModelProvider extends BaseModelProvider {

    public ToolsModelProvider(PackOutput output, ResourceManager clientResources) {
        super(output, MekanismTools.MODID, clientResources);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        //Shields
        addShieldModel(itemModels, ToolsItems.BRONZE_SHIELD, Mekanism.rl("block/block_bronze"));
        addShieldModel(itemModels, ToolsItems.LAPIS_LAZULI_SHIELD, mcLocation("block/lapis_block"));
        addShieldModel(itemModels, ToolsItems.OSMIUM_SHIELD, Mekanism.rl("block/block_osmium"));
        addShieldModel(itemModels, ToolsItems.REFINED_GLOWSTONE_SHIELD, Mekanism.rl("block/block_refined_glowstone"));
        addShieldModel(itemModels, ToolsItems.REFINED_OBSIDIAN_SHIELD, Mekanism.rl("block/block_refined_obsidian"));
        addShieldModel(itemModels, ToolsItems.STEEL_SHIELD, Mekanism.rl("block/block_steel"));
        //Armor items are generated textures, all other tools module items are handheld
        for (Holder<Item> holder : ToolsItems.ITEMS.getEntries()) {
            if (holder.value() instanceof ItemMekanismShield) {
                //Skip shields, we manually handle them above
                continue;
            }
            String name = getPath(holder);
            Identifier textureLoc;
            if (isVanilla(holder, name)) {
                textureLoc = itemTexture(holder);
            } else {
                int index = name.lastIndexOf('_');
                textureLoc = modLocation("item/" + name.substring(0, index) + '/' + name.substring(index + 1));
            }
            Material texture = new Material(textureLoc);
            armorOrHandheld(itemModels, holder, texture);
        }
    }

    private static Identifier typeToTrimSlot(ArmorType armorType) {
        return switch (armorType) {
            case HELMET -> ItemModelGenerators.TRIM_PREFIX_HELMET;
            case CHESTPLATE -> ItemModelGenerators.TRIM_PREFIX_CHESTPLATE;
            case LEGGINGS -> ItemModelGenerators.TRIM_PREFIX_LEGGINGS;
            case BOOTS -> ItemModelGenerators.TRIM_PREFIX_BOOTS;
            case BODY -> throw new UnsupportedOperationException();
        };
    }

    protected void armorOrHandheld(ItemModelGenerators itemModels, Holder<Item> holder, Material texture) {
        if (holder.value() instanceof ItemMekanismArmor armorItem) {
            ResourceKey<EquipmentAsset> equipmentAssetId = armorItem.getEquipmentAssetId();
            generateTrimmableItem(itemModels, armorItem, equipmentAssetId, texture);
        } else {
            Item item = holder.value();
            Identifier itemModel = ModelTemplates.FLAT_HANDHELD_ITEM.create(
                  ModelLocationUtils.getModelLocation(item),
                  TextureMapping.layer0(texture),
                  itemModels.modelOutput
            );
            itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(itemModel));
        }
    }

    /// Inlined and adapted from [net.minecraft.client.data.models.ItemModelGenerators#generateTrimmableItem], to take in custom base item texture
    private static void generateTrimmableItem(ItemModelGenerators itemModels, ItemMekanismArmor armorItem, ResourceKey<EquipmentAsset> equipmentAssetId, Material itemTexture) {
        Identifier slotTrimPrefix = typeToTrimSlot(armorItem.getArmorType());
        Identifier modelLocation = ModelLocationUtils.getModelLocation(armorItem);
        List<SelectItemModel.SwitchCase<ResourceKey<TrimMaterial>>> cases = new ArrayList<>(ItemModelGenerators.TRIM_MATERIAL_MODELS.size());

        for (ItemModelGenerators.TrimMaterialData material : ItemModelGenerators.TRIM_MATERIAL_MODELS) {
            Identifier trimModelLocation = modelLocation.withSuffix("_" + material.assets().base().suffix() + "_trim");
            Material trimOverlayTexture = new Material(slotTrimPrefix.withSuffix("_" + material.assets().assetId(equipmentAssetId).suffix()));
            ItemModel.Unbaked trimModel;
            itemModels.generateLayeredItem(trimModelLocation, itemTexture, trimOverlayTexture);
            trimModel = ItemModelUtils.plainModel(trimModelLocation);

            cases.add(ItemModelUtils.when(material.materialKey(), trimModel));
        }

        ItemModel.Unbaked untrimmedModel;
        ModelTemplates.FLAT_ITEM.create(modelLocation, TextureMapping.layer0(itemTexture), itemModels.modelOutput);
        untrimmedModel = ItemModelUtils.plainModel(modelLocation);

        itemModels.itemModelOutput.accept(armorItem, ItemModelUtils.select(new TrimMaterialProperty(), untrimmedModel, cases));
    }

    private boolean isVanilla(Holder<Item> item, String name) {
        if (item.value() instanceof ItemMekanismPaxel) {
            return name.startsWith("netherite") || name.startsWith("diamond") || name.startsWith("gold") || name.startsWith("iron") ||
                   name.startsWith("stone") || name.startsWith("wood");
        }
        return false;
    }

    /// Inlined and adapted from [ItemModelGenerators#generateShield(Item)] to use our renderer and vanilla's base model
    private void addShieldModel(ItemModelGenerators itemModels, Holder<Item> shield, Identifier particle) {
        Item item = shield.value();
        ItemModel.Unbaked normal = ItemModelUtils.specialModel(mcLocation("item/shield"), new RenderMekanismShieldItem.UnbakedShield());
        ItemModel.Unbaked blocking = ItemModelUtils.specialModel(mcLocation("item/shield_blocking"), new RenderMekanismShieldItem.UnbakedShield());
        itemModels.itemModelOutput.accept(
              item,
              ItemModelUtils.conditional(
                    ShieldSpecialRenderer.DEFAULT_TRANSFORMATION,
                    ItemModelUtils.isUsingItem(),
                    blocking,
                    normal
              )
        );
    }
}