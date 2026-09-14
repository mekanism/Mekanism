package mekanism.tools.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mekanism.client.model.BaseModelProvider;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.tools.client.render.item.RenderMekanismShieldItem;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.material.MaterialType;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.ClientItem;
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
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public class ToolsModelProvider extends BaseModelProvider {

    public ToolsModelProvider(PackOutput output, ResourceManager clientResources) {
        super(output, MekanismTools.MODID, clientResources);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        //Shields
        addShieldModel(itemModels, MaterialType.BRONZE, Mekanism.rl("block/block_bronze"));
        addShieldModel(itemModels, MaterialType.LAPIS_LAZULI, mcLocation("block/lapis_block"));
        addShieldModel(itemModels, MaterialType.OSMIUM, Mekanism.rl("block/block_osmium"));
        addShieldModel(itemModels, MaterialType.REFINED_GLOWSTONE, Mekanism.rl("block/block_refined_glowstone"));
        addShieldModel(itemModels, MaterialType.REFINED_OBSIDIAN, Mekanism.rl("block/block_refined_obsidian"));
        addShieldModel(itemModels, MaterialType.STEEL, Mekanism.rl("block/block_steel"));
        //Armor items are generated textures, all other tools module items are handheld
        for (MaterialType material : MaterialType.VALUES) {
            //Skip shields, we manually handle them above
            handheld(itemModels, material.tools.axe());
            handheld(itemModels, material.tools.hoe());
            handheld(itemModels, material.tools.paxel());
            handheld(itemModels, material.tools.pickaxe());
            handheld(itemModels, material.tools.shovel());
            handheld(itemModels, material.tools.sword());
            spear(itemModels, material.tools.spear());

            ResourceKey<EquipmentAsset> armorAssetId = material.material.equipmentAsset();
            generateTrimmableItem(itemModels, material.armor.helmet(), armorAssetId, ItemModelGenerators.TRIM_PREFIX_HELMET);
            generateTrimmableItem(itemModels, material.armor.chestplate(), armorAssetId, ItemModelGenerators.TRIM_PREFIX_CHESTPLATE);
            generateTrimmableItem(itemModels, material.armor.leggings(), armorAssetId, ItemModelGenerators.TRIM_PREFIX_LEGGINGS);
            generateTrimmableItem(itemModels, material.armor.boots(), armorAssetId, ItemModelGenerators.TRIM_PREFIX_BOOTS);

        }
        ToolsItems.vanillaPaxels().forEach(paxel -> handheld(itemModels, paxel, new Material(itemTexture(paxel))));
    }

    private Material getTexture(ItemRegistryObject<?> mekItem) {
        String name = mekItem.getName();
        int index = name.lastIndexOf('_');
        return new Material(modLocation("item/" + name.substring(0, index) + '/' + name.substring(index + 1)));
    }

    private void handheld(ItemModelGenerators itemModels, ItemRegistryObject<?> holder) {
        handheld(itemModels, holder, getTexture(holder));
    }

    private void handheld(ItemModelGenerators itemModels, Holder<Item> holder, Material texture) {
        Item item = holder.value();
        Identifier itemModel = ModelTemplates.FLAT_HANDHELD_ITEM.create(
              ModelLocationUtils.getModelLocation(item),
              TextureMapping.layer0(texture),
              itemModels.modelOutput
        );
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(itemModel));
    }

    /// Inlined and adapted from [net.minecraft.client.data.models.ItemModelGenerators#generateSpear], to take in custom base item texture
    private void spear(ItemModelGenerators itemModels, ItemRegistryObject<?> spear) {
        Material itemTexture = getTexture(spear);
        Item item = spear.value();
        Identifier modelLocation = ModelLocationUtils.getModelLocation(item);
        ItemModel.Unbaked flatModel = ItemModelUtils.plainModel(ModelTemplates.FLAT_ITEM.create(modelLocation, TextureMapping.layer0(itemTexture), itemModels.modelOutput));
        ItemModel.Unbaked inHandModel = ItemModelUtils.plainModel(
              ModelTemplates.SPEAR_IN_HAND.create(item, TextureMapping.layer0(new Material(itemTexture.sprite().withSuffix("_in_hand"))), itemModels.modelOutput)
        );
        itemModels.itemModelOutput.accept(item, ItemModelGenerators.createFlatModelDispatch(flatModel, inHandModel), new ClientItem.Properties(true, false, 1.95F));
    }

    /// Inlined and adapted from [net.minecraft.client.data.models.ItemModelGenerators#generateTrimmableItem], to take in custom base item texture
    private void generateTrimmableItem(ItemModelGenerators itemModels, ItemRegistryObject<?> armorItem, ResourceKey<EquipmentAsset> equipmentAssetId, Identifier slotTrimPrefix) {
        Material itemTexture = getTexture(armorItem);
        Identifier modelLocation = armorItem.getId().withPrefix("item/");
        List<SelectItemModel.SwitchCase<ResourceKey<TrimMaterial>>> cases = new ArrayList<>(ItemModelGenerators.TRIM_MATERIAL_MODELS.size());

        for (ItemModelGenerators.TrimMaterialData material : ItemModelGenerators.TRIM_MATERIAL_MODELS) {
            Identifier trimModelLocation = modelLocation.withSuffix("_" + material.assets().base().suffix() + "_trim");
            Material trimOverlayTexture = new Material(slotTrimPrefix.withSuffix("_" + material.assets().assetId(equipmentAssetId).suffix()));
            itemModels.generateLayeredItem(trimModelLocation, itemTexture, trimOverlayTexture);
            ItemModel.Unbaked trimModel = ItemModelUtils.plainModel(trimModelLocation);

            cases.add(ItemModelUtils.when(material.materialKey(), trimModel));
        }

        ModelTemplates.FLAT_ITEM.create(modelLocation, TextureMapping.layer0(itemTexture), itemModels.modelOutput);
        ItemModel.Unbaked untrimmedModel = ItemModelUtils.plainModel(modelLocation);

        itemModels.itemModelOutput.accept(armorItem.value(), ItemModelUtils.select(new TrimMaterialProperty(), untrimmedModel, cases));
    }

    private static final ModelTemplate SHIELD = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("item/shield")), Optional.empty(), TextureSlot.PARTICLE);
    private static final ModelTemplate SHIELD_BLOCKING = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("item/shield_blocking")), Optional.of("_blocking"), TextureSlot.PARTICLE);

    /// Inlined and adapted from [ItemModelGenerators#generateShield(Item)] to use our renderer and vanilla's base model
    private void addShieldModel(ItemModelGenerators itemModels, MaterialType material, Identifier particle) {
        Item item = material.tools.shield().value();
        TextureMapping textureMapping = TextureMapping.particle(new Material(particle));
        RenderMekanismShieldItem.UnbakedShield unbaked = new RenderMekanismShieldItem.UnbakedShield(MekanismTools.rl(material.getSerializedName()));
        ItemModel.Unbaked normal = ItemModelUtils.specialModel(SHIELD.create(item, textureMapping, itemModels.modelOutput), unbaked);
        ItemModel.Unbaked blocking = ItemModelUtils.specialModel(SHIELD_BLOCKING.create(item, textureMapping, itemModels.modelOutput), unbaked);
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