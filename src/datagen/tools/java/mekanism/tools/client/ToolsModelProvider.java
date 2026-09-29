package mekanism.tools.client;

import java.util.Optional;
import mekanism.client.model.BaseModelProvider;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismTrimMaterials;
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
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.neoforged.neoforge.client.model.item.TrimmedArmorModel.PaletteTransform;
import org.jspecify.annotations.Nullable;

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

            material.armor.forEachHumanoid(armorItem -> {
                Material itemTexture = getTexture(armorItem);
                Identifier modelLocation = armorItem.getId().withPrefix("item/");
                ModelTemplates.FLAT_ITEM.create(modelLocation, TextureMapping.layer0(itemTexture), itemModels.modelOutput);
            });
            PaletteTransformData transform = getTransform(material);
            itemModels.generateDynamicTrimmableArmorSet(material.armor.helmet().asItem(), material.armor.chestplate().asItem(), material.armor.leggings().asItem(),
                  material.armor.boots().asItem(), transform == null ? null : transform.transform());
            generateFlatItem(itemModels, material.armor.horse());
            generateFlatItem(itemModels, material.armor.nautilus());
        }
        ToolsItems.vanillaPaxels().forEach(paxel -> handheld(itemModels, paxel, new Material(itemTexture(paxel))));
    }

    @Nullable
    public static PaletteTransformData getTransform(MaterialType material) {
        return switch (material) {
            case BRONZE -> transformMek(MekanismTrimMaterials.BRONZE);
            case LAPIS_LAZULI -> new PaletteTransformData(TrimMaterials.LAPIS, TrimMaterials.Palette.LAPIS.id(),
                  MekanismTools.rl("trim/" + MekanismTools.MODID + "_lapis_lighter"));
            case REFINED_GLOWSTONE -> transformMek(MekanismTrimMaterials.REFINED_GLOWSTONE);
            case REFINED_OBSIDIAN -> transformMek(MekanismTrimMaterials.REFINED_OBSIDIAN);
            case OSMIUM, STEEL -> null;
        };
    }

    private static PaletteTransformData transformMek(ResourceKey<TrimMaterial> material) {
        Identifier id = material.identifier();
        Identifier target = id.withPrefix("trim/" + id.getNamespace() + "_");
        return new PaletteTransformData(material, target, target.withSuffix("_lighter"));
    }

    public record PaletteTransformData(ResourceKey<TrimMaterial> material, Identifier target, Identifier replacement) {

        public PaletteTransform transform() {
            return new PaletteTransform(target, replacement);
        }
    }

    private Material getTexture(ItemRegistryObject<?> mekItem) {
        String name = mekItem.getName();
        int index = name.lastIndexOf('_');
        String last = name.substring(index + 1);
        if (last.equals("armor")) {//TODO: Do this in a less special cased way
            index = name.lastIndexOf('_', index - 1);
            last = name.substring(index + 1);
        }
        return new Material(modLocation("item/" + name.substring(0, index) + '/' + last));
    }

    private void generateFlatItem(ItemModelGenerators itemModels, ItemRegistryObject<?> holder) {
        Item item = holder.value();
        Identifier flatItemModel = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(getTexture(holder)), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(flatItemModel));
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