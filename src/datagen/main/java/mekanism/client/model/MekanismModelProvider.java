package mekanism.client.model;

import com.google.common.collect.Table;
import java.util.Map;
import java.util.Optional;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockPersonalBarrel;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.IResource;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.resource.ore.OreType;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MekanismModelProvider extends BaseModelProvider {

    /// Like regular cube, but with the faces having tint index 0
    public static final ModelTemplate COLORED_CUBE = new ModelTemplate(Optional.of(Mekanism.rl("block/colored_cube")), Optional.empty(), TextureSlot.ALL);

    public MekanismModelProvider(PackOutput packOutput, ResourceManager clientResources) {
        super(packOutput, Mekanism.MODID, clientResources);
    }

    protected void resourceItem(Holder<Item> item, String type, int tint, ItemModelGenerators itemModels) {
        Material base = new Material(modLocation("item/" + type));
        Material overlay = new Material(modLocation("item/" + type + "_overlay"));
        ModelTemplate template = ModelTemplates.FLAT_ITEM;
        Item item1 = item.value();
        TextureMapping textures = TextureMapping.layer0(base);
        if (textureExists(overlay)) {
            template = ModelTemplates.TWO_LAYERED_ITEM;
            textures.put(TextureSlot.LAYER1, overlay);
        }

        Identifier generatedModel = template.create(ModelLocationUtils.getModelLocation(item1), textures, itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item1, ItemModelUtils.tintedModel(generatedModel, new Constant(tint)));
    }

    private void addOreBlock(BlockModelGenerators blockModels, Holder<Block> oreBlock, String path) {
        Identifier modelPath = modLocation(path);
        Identifier texPath = ModelLocationUtils.getModelLocation(oreBlock.value());
        TextureMapping textureMapping = TextureMapping.cube(new Material(texPath));
        simpleCustomModel(blockModels, oreBlock.value(), modelPath, ModelTemplates.CUBE_ALL, textureMapping);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        //Items
        registerBuckets(MekanismFluids.FLUIDS, itemModels);
        registerModules(MekanismItems.ITEMS, itemModels);
        for (Table.Cell<ResourceType, PrimaryResource, ItemRegistryObject<Item>> item : MekanismItems.PROCESSED_RESOURCES.cellSet()) {
            ItemRegistryObject<Item> itemValue = item.getValue();
            Identifier texture = itemTexture(itemValue);
            //NB: as at time of creation, all our base materials have an override
            if (textureExists(texture)) {
                itemModels.generateFlatItem(itemValue.asItem(), ModelTemplates.FLAT_ITEM);
            } else {
                //If the texture does not exist fallback to the default texture
                resourceItem(itemValue, item.getRowKey().getRegistryPrefix(), item.getColumnKey().getTint(), itemModels);
            }
        }

        //Blocks
        registerFluidBlockStates(blockModels, MekanismFluids.FLUIDS);

        for (Map.Entry<IResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            String registrySuffix = entry.getKey().getRegistrySuffix();
            Block block = entry.getValue().value();
            Item asItem = entry.getValue().asItem();
            Material texture = modTexture("block/block_" + registrySuffix);
            Identifier targetModelPath = modLocation("block/storage/" + registrySuffix);
            TextureMapping textureMapping;
            ModelTemplate modelTemplate;
            if (textureExists(texture)) {
                //If we have an override we can just use a basic cube that has no color tints in it
                textureMapping = TextureMapping.cube(texture);
                modelTemplate = ModelTemplates.CUBE_ALL;
            } else {
                //If the texture does not exist fallback to the default texture and use a colorable base model
                textureMapping = TextureMapping.cube(modTexture("block/resource_block"));
                modelTemplate = COLORED_CUBE;
                int tint;
                if (entry.getKey() instanceof PrimaryResource primaryResource) {
                    tint = primaryResource.getTint();
                }/* else {
                    throw new UnsupportedOperationException("No tint available?!");
                }*/
                //TODO - 26.1: is this needed??? blockModels.registerSimpleTintedItemModel(block, targetModelPath, new Constant(tint));
            }
            simpleCustomModel(blockModels, block, targetModelPath, modelTemplate, textureMapping);
        }
        for (Map.Entry<OreType, OreBlockType> entry : MekanismBlocks.ORES.entrySet()) {
            String registrySuffix = entry.getKey().getResource().getRegistrySuffix();
            OreBlockType oreBlockType = entry.getValue();
            addOreBlock(blockModels, oreBlockType.stone(), "block/ore/" + registrySuffix);
            addOreBlock(blockModels, oreBlockType.deepslate(), "block/deepslate_ore/" + registrySuffix);
        }

        BlockPersonalBarrel barrelBlock = MekanismBlocks.PERSONAL_BARREL.value();
        Identifier barrelModel = ModelTemplates.CUBE_BOTTOM_TOP.create(
              barrelBlock,
              new TextureMapping()
                    .put(TextureSlot.SIDE, modTexture("block/personal_barrel/side"))
                    .put(TextureSlot.TOP, modTexture("block/personal_barrel/top"))
                    .put(TextureSlot.BOTTOM, modTexture("block/personal_barrel/bottom")),
              blockModels.modelOutput
        );
        Identifier openBarrel = new ModelTemplate(Optional.of(barrelModel), Optional.of("_open"), TextureSlot.TOP)
              .create(
                    barrelBlock,
                    new TextureMapping()
                          .put(TextureSlot.TOP, modTexture("block/personal_barrel/top_open")),
                    blockModels.modelOutput
              );

        blockModels.blockStateOutput.accept(
              MultiVariantGenerator.dispatch(
                          barrelBlock,
                          BlockModelGenerators.plainVariant(barrelModel)
                    )
                    .with(BlockModelGenerators.ROTATION_FACING)
                    .with(
                          PropertyDispatch.modify(BlockStateProperties.OPEN)
                                .select(false, BlockModelGenerators.NOP)
                                .select(true, VariantMutator.MODEL.withValue(openBarrel))
                    )
        );

        Block stabilizerBlock = MekanismBlocks.DIMENSIONAL_STABILIZER.value();
        Identifier stabilizerModel = ModelTemplates.CUBE_BOTTOM_TOP.create(
              stabilizerBlock,
              new TextureMapping()
                    .put(TextureSlot.SIDE, modTexture("block/dimensional_stabilizer/side"))
                    .put(TextureSlot.TOP, modTexture("block/dimensional_stabilizer/top"))
                    .put(TextureSlot.BOTTOM, modTexture("block/dimensional_stabilizer/bottom")),
              blockModels.modelOutput
        );
        Identifier activeStabilizer = new ModelTemplate(Optional.of(stabilizerModel), Optional.of("_active"), TextureSlot.TOP, TextureSlot.SIDE)
              .create(
                    stabilizerBlock,
                    new TextureMapping()
                          .put(TextureSlot.TOP, modTexture("block/personal_barrel/top_active"))
                          .put(TextureSlot.SIDE, modTexture("block/dimensional_stabilizer/side_active")),
                    blockModels.modelOutput
              );

        blockModels.blockStateOutput.accept(
              MultiVariantGenerator.dispatch(
                          stabilizerBlock,
                          BlockModelGenerators.plainVariant(stabilizerModel)
                    )
                    .with(
                          PropertyDispatch.modify(AttributeStateActive.activeProperty)
                                .select(false, BlockModelGenerators.NOP)
                                .select(true, VariantMutator.MODEL.withValue(activeStabilizer))
                    )
        );
    }

}
