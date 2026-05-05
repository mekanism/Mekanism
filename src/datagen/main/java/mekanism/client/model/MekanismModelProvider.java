package mekanism.client.model;

import com.google.common.collect.Table;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mekanism.api.tier.BaseTier;
import mekanism.client.model.itemtint.ColorComponent;
import mekanism.client.model.props.ClientRadiationScale;
import mekanism.client.model.props.ConfigCardEncoded;
import mekanism.client.model.props.CraftingFormulaStatus;
import mekanism.client.render.item.block.RenderFluidTankItem;
import mekanism.client.render.item.gear.RenderFreeRunners;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockPersonalBarrel;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.lib.radiation.RadiationScale;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.IResource;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.tier.FactoryTier;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MekanismModelProvider extends BaseModelProvider {

    /// Like regular cube, but with the faces having tint index 0
    public static final ModelTemplate COLORED_CUBE = new ModelTemplate(Optional.of(Mekanism.rl("block/colored_cube")), Optional.empty(), TextureSlot.ALL);
    /// Used to skip a tint index
    private static final Constant IGNORE_LAYER = new Constant(-1);

    public MekanismModelProvider(PackOutput packOutput, ResourceManager clientResources) {
        super(packOutput, Mekanism.MODID, clientResources);
        markManualBlocks();
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

        Identifier generatedModel = template.create(defaultModelLoc(item1), textures, itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item1, ItemModelUtils.tintedModel(generatedModel, new Constant(tint)));
    }

    private void addOreBlock(BlockModelGenerators blockModels, Holder<Block> oreBlock, String path) {
        Identifier modelPath = modLocation(path);
        Block block = oreBlock.value();
        Identifier texPath = defaultModelLoc(block);
        TextureMapping textureMapping = TextureMapping.cube(new Material(texPath));
        simpleCustomModel(blockModels, block, modelPath, ModelTemplates.CUBE_ALL, textureMapping, ItemModelUtils.plainModel(modelPath));
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

        itemModels.itemModelOutput.accept(
              MekanismItems.CONFIGURATION_CARD.asItem(),
              ItemModelUtils.conditional(
                    ConfigCardEncoded.INSTANCE,
                    ItemModelUtils.plainModel(modLocation("item/configuration_card_encoded")),
                    ItemModelUtils.plainModel(modLocation("item/configuration_card"))
              )
        );

        {
            Item configurator = MekanismItems.CONFIGURATOR.value();
            ItemModel.Unbaked baseConfigurator = ItemModelUtils.plainModel(modLocation("item/configurator"));
            itemModels.itemModelOutput.accept(
                  configurator,
                  ItemModelUtils.select(
                        new ComponentContents<>(MekanismDataComponents.CONFIGURATOR_MODE.get()),
                        baseConfigurator,
                        ItemModelUtils.when(ItemConfigurator.ConfiguratorMode.EMPTY, ItemModelUtils.plainModel(modLocation("item/configurator_empty"))),
                        ItemModelUtils.when(ItemConfigurator.ConfiguratorMode.ROTATE, ItemModelUtils.plainModel(modLocation("item/configurator_rotate"))),
                        ItemModelUtils.when(ItemConfigurator.ConfiguratorMode.WRENCH, ItemModelUtils.plainModel(modLocation("item/configurator_wrench")))
                  )
            );
        }

        {
            Item craftingFormula = MekanismItems.CRAFTING_FORMULA.value();
            ItemModel.Unbaked baseFormula = ItemModelUtils.plainModel(modLocation("item/crafting_formula"));
            itemModels.itemModelOutput.accept(
                  craftingFormula,
                  ItemModelUtils.select(
                        new CraftingFormulaStatus(),
                        baseFormula,
                        ItemModelUtils.when(CraftingFormulaStatus.CraftingCardStatus.INVALID, ItemModelUtils.plainModel(modLocation("item/crafting_formula_invalid"))),
                        ItemModelUtils.when(CraftingFormulaStatus.CraftingCardStatus.ENCODED, ItemModelUtils.plainModel(modLocation("item/crafting_formula_encoded")))
                  )
            );
        }

        itemModels.generateBow(MekanismItems.ELECTRIC_BOW.asItem());

        simpleISTER(itemModels, MekanismItems.FREE_RUNNERS, new RenderFreeRunners.Unbaked(false));
        simpleISTER(itemModels, MekanismItems.ARMORED_FREE_RUNNERS, new RenderFreeRunners.Unbaked(true));

        //todo - 26.1: we could possibly merge the base item models to one now? Assuming the perspective translations are the same
        simpleISTER(itemModels, MekanismBlocks.ADVANCED_FLUID_TANK.getItemHolder(), new RenderFluidTankItem.Unbaked(BaseTier.ADVANCED));
        simpleISTER(itemModels, MekanismBlocks.BASIC_FLUID_TANK.getItemHolder(), new RenderFluidTankItem.Unbaked(BaseTier.BASIC));
        simpleISTER(itemModels, MekanismBlocks.ELITE_FLUID_TANK.getItemHolder(), new RenderFluidTankItem.Unbaked(BaseTier.ELITE));
        simpleISTER(itemModels, MekanismBlocks.ULTIMATE_FLUID_TANK.getItemHolder(), new RenderFluidTankItem.Unbaked(BaseTier.ULTIMATE));
        simpleISTER(itemModels, MekanismBlocks.CREATIVE_FLUID_TANK.getItemHolder(), new RenderFluidTankItem.Unbaked(BaseTier.CREATIVE));

        {
            Item geigerCounter = MekanismItems.GEIGER_COUNTER.value();
            Identifier geigerLoc = modLocation("item/geiger_counter");
            ItemModel.Unbaked baseGeiger = ItemModelUtils.plainModel(geigerLoc);
            itemModels.itemModelOutput.accept(
                  geigerCounter,
                  ItemModelUtils.select(
                        ClientRadiationScale.INSTANCE,
                        baseGeiger,
                        Arrays.stream(RadiationScale.values())
                              .map(scale -> ItemModelUtils.when(scale, ItemModelUtils.plainModel(geigerLoc.withSuffix("_" + scale.ordinal()))))
                              .toList()
                  )
            );
        }

        itemModels.generateElytra(MekanismItems.HDPE_REINFORCED_ELYTRA.asItem());

        itemModels.declareCustomModelItem(MekanismItems.JETPACK.asItem());//todo renderer
        itemModels.declareCustomModelItem(MekanismItems.ARMORED_JETPACK.asItem());//todo renderer

        {
            ItemModel.Unbaked modelToRegister = ItemModelUtils.select(
                  new DisplayContext(),
                  ItemModelUtils.plainModel(modLocation("item/meka_tool_default")),
                  ItemModelUtils.when(List.of(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, ItemDisplayContext.THIRD_PERSON_LEFT_HAND), ItemModelUtils.plainModel(modLocation("item/meka_tool_left")))
            );
            itemModels.itemModelOutput.accept(MekanismItems.MEKA_TOOL.asItem(), modelToRegister);
        }

        itemModels.declareCustomModelItem(MekanismItems.ROBIT.asItem());//todo renderer?

        registerManualItemModels(itemModels);

        for (ItemLike holder : List.of(MekanismItems.PORTABLE_QIO_DASHBOARD, MekanismBlocks.QIO_DRIVE_ARRAY, MekanismBlocks.QIO_DASHBOARD,
              MekanismBlocks.QIO_IMPORTER, MekanismBlocks.QIO_EXPORTER, MekanismBlocks.QIO_REDSTONE_ADAPTER)) {
            Identifier modelLocation = switch (holder) {
                case BlockRegistryObject<?, ?> block -> existingModel(block.value());
                case ItemRegistryObject<?> item -> existingModel(item.value());
                default -> throw new IllegalArgumentException("unknown type");
            };
            itemModels.itemModelOutput.accept(holder.asItem(), ItemModelUtils.tintedModel(modelLocation, IGNORE_LAYER, ColorComponent.INSTANCE));
        }

        //Blocks
        registerFluidBlockStates(blockModels, MekanismFluids.FLUIDS);
        blockModels.itemModelOutput.accept(
              MekanismBlocks.CARDBOARD_BOX.asItem(),
              ItemModelUtils.conditional(
                    ItemModelUtils.hasComponent(MekanismDataComponents.BLOCK_DATA.get()),
                    ItemModelUtils.plainModel(modLocation("block/cardboard_box_storage")),
                    ItemModelUtils.plainModel(modLocation("block/cardboard_box"))
              )
        );

        for (Map.Entry<IResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            String registrySuffix = entry.getKey().getRegistrySuffix();
            Block block = entry.getValue().value();
            Material texture = modTexture("block/block_" + registrySuffix);
            Identifier targetModelPath = modLocation("block/storage/" + registrySuffix);
            TextureMapping textureMapping;
            ModelTemplate modelTemplate;
            ItemModel.Unbaked itemModel;
            if (textureExists(texture)) {
                //If we have an override we can just use a basic cube that has no color tints in it
                textureMapping = TextureMapping.cube(texture);
                modelTemplate = ModelTemplates.CUBE_ALL;
                itemModel = ItemModelUtils.plainModel(targetModelPath);
            } else {
                //If the texture does not exist fallback to the default texture and use a colorable base model
                textureMapping = TextureMapping.cube(modTexture("block/resource_block"));
                modelTemplate = COLORED_CUBE;
                int tint;
                if (entry.getKey() instanceof PrimaryResource primaryResource) {
                    tint = primaryResource.getTint();
                } else {
                    throw new UnsupportedOperationException("Need tint wired in");
                }
                itemModel = ItemModelUtils.tintedModel(targetModelPath, new Constant(tint));
            }
            simpleCustomModel(blockModels, block, targetModelPath, modelTemplate, textureMapping, itemModel);
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
                    .with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING)
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
                          .put(TextureSlot.TOP, modTexture("block/dimensional_stabilizer/top_active"))
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

        for (FactoryType factoryType : FactoryType.values()) {
            for (FactoryTier tier : FactoryTier.values()) {
                plainBlockItemModel(
                      blockModels,
                      MekanismBlocks.getFactory(tier, factoryType),
                      "block/factory/%s/%s".formatted(factoryType.getRegistryNameComponent(), tier.getBaseTier().getLowerName())
                );
            }
        }

        plainBlockItemModel(blockModels, MekanismBlocks.ADVANCED_BIN, "block/bin/advanced");
        plainBlockItemModel(blockModels, MekanismBlocks.BASIC_BIN, "block/bin/basic");
        plainBlockItemModel(blockModels, MekanismBlocks.ELITE_BIN, "block/bin/elite");
        plainBlockItemModel(blockModels, MekanismBlocks.ULTIMATE_BIN, "block/bin/ultimate");
        plainBlockItemModel(blockModels, MekanismBlocks.CREATIVE_BIN, "block/bin/creative");

        plainBlockItemModel(blockModels, MekanismBlocks.ADVANCED_CHEMICAL_TANK, "block/chemical_tank/advanced");
        plainBlockItemModel(blockModels, MekanismBlocks.BASIC_CHEMICAL_TANK, "block/chemical_tank/basic");
        plainBlockItemModel(blockModels, MekanismBlocks.ELITE_CHEMICAL_TANK, "block/chemical_tank/elite");
        plainBlockItemModel(blockModels, MekanismBlocks.ULTIMATE_CHEMICAL_TANK, "block/chemical_tank/ultimate");
        plainBlockItemModel(blockModels, MekanismBlocks.CREATIVE_CHEMICAL_TANK, "block/chemical_tank/creative");

        plainBlockItemModel(blockModels, MekanismBlocks.ADVANCED_INDUCTION_CELL, "block/induction/cell/advanced");
        plainBlockItemModel(blockModels, MekanismBlocks.BASIC_INDUCTION_CELL, "block/induction/cell/basic");
        plainBlockItemModel(blockModels, MekanismBlocks.ELITE_INDUCTION_CELL, "block/induction/cell/elite");
        plainBlockItemModel(blockModels, MekanismBlocks.ULTIMATE_INDUCTION_CELL, "block/induction/cell/ultimate");

        plainBlockItemModel(blockModels, MekanismBlocks.ADVANCED_INDUCTION_PROVIDER, "block/induction/provider/advanced");
        plainBlockItemModel(blockModels, MekanismBlocks.BASIC_INDUCTION_PROVIDER, "block/induction/provider/basic");
        plainBlockItemModel(blockModels, MekanismBlocks.ELITE_INDUCTION_PROVIDER, "block/induction/provider/elite");
        plainBlockItemModel(blockModels, MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER, "block/induction/provider/ultimate");
        plainBlockItemModel(blockModels, MekanismBlocks.INDUCTION_CASING, "block/induction/casing");
        plainBlockItemModel(blockModels, MekanismBlocks.INDUCTION_PORT, "block/induction/port");

        plainBlockItemModel(blockModels, MekanismBlocks.BRONZE_BLOCK, "block/storage/bronze");
        plainBlockItemModel(blockModels, MekanismBlocks.REFINED_OBSIDIAN_BLOCK, "block/storage/refined_obsidian");
        plainBlockItemModel(blockModels, MekanismBlocks.CHARCOAL_BLOCK, "block/storage/charcoal");
        plainBlockItemModel(blockModels, MekanismBlocks.REFINED_GLOWSTONE_BLOCK, "block/storage/refined_glowstone");
        plainBlockItemModel(blockModels, MekanismBlocks.STEEL_BLOCK, "block/storage/steel");
        plainBlockItemModel(blockModels, MekanismBlocks.FLUORITE_BLOCK, "block/storage/fluorite");
        plainBlockItemModel(blockModels, MekanismBlocks.SALT_BLOCK, "block/storage/salt");
        plainBlockItemModel(blockModels, MekanismBlocks.BIO_FUEL_BLOCK, "block/storage/bio_fuel");

        plainBlockItemModel(blockModels, MekanismBlocks.THERMAL_EVAPORATION_BLOCK, "block/thermal_evaporation/block");
        plainBlockItemModel(blockModels, MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, "block/thermal_evaporation/controller");
        plainBlockItemModel(blockModels, MekanismBlocks.THERMAL_EVAPORATION_VALVE, "block/thermal_evaporation/valve");

        plainBlockItemModel(blockModels, MekanismBlocks.BOILER_VALVE, "block/boiler_valve_input");
    }

    private void plainBlockItemModel(BlockModelGenerators blockModels, BlockRegistryObject<?, ?> registryObject, String name) {
        blockModels.itemModelOutput.accept(
              registryObject.asItem(),
              ItemModelUtils.plainModel(modLocation(name))
        );
    }

    private void registerManualItemModels(ItemModelGenerators itemModels) {
        itemModels.declareCustomModelItem(MekanismItems.ADVANCED_CONTROL_CIRCUIT.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ADVANCED_TIER_INSTALLER.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ATOMIC_ALLOY.asItem());
        itemModels.declareCustomModelItem(MekanismItems.INFUSED_ALLOY.asItem());
        itemModels.declareCustomModelItem(MekanismItems.REINFORCED_ALLOY.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ATOMIC_DISASSEMBLER.asItem());
        itemModels.declareCustomModelItem(MekanismItems.BASIC_CONTROL_CIRCUIT.asItem());
        itemModels.declareCustomModelItem(MekanismItems.BASIC_TIER_INSTALLER.asItem());
        itemModels.declareCustomModelItem(MekanismItems.BIO_FUEL.asItem());
        itemModels.declareCustomModelItem(MekanismItems.CANTEEN.asItem());
        itemModels.declareCustomModelItem(MekanismItems.DICTIONARY.asItem());
        itemModels.declareCustomModelItem(MekanismItems.DIRTY_NETHERITE_SCRAP.asItem());
        itemModels.declareCustomModelItem(MekanismItems.DOSIMETER.asItem());
        itemModels.declareCustomModelItem(MekanismItems.DYE_BASE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ELECTROLYTIC_CORE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ELITE_CONTROL_CIRCUIT.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ELITE_TIER_INSTALLER.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ENERGY_TABLET.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ENRICHED_CARBON.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ENRICHED_DIAMOND.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ENRICHED_GOLD.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ENRICHED_IRON.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ENRICHED_REDSTONE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ENRICHED_OBSIDIAN.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ENRICHED_TIN.asItem());
        itemModels.declareCustomModelItem(MekanismItems.FLAMETHROWER.asItem());
        itemModels.declareCustomModelItem(MekanismItems.FLUORITE_GEM.asItem());
        itemModels.declareCustomModelItem(MekanismItems.GAUGE_DROPPER.asItem());
        itemModels.declareCustomModelItem(MekanismItems.HAZMAT_BOOTS.asItem());
        itemModels.declareCustomModelItem(MekanismItems.HAZMAT_GOWN.asItem());
        itemModels.declareCustomModelItem(MekanismItems.HAZMAT_MASK.asItem());
        itemModels.declareCustomModelItem(MekanismItems.HAZMAT_PANTS.asItem());
        itemModels.declareCustomModelItem(MekanismItems.HDPE_PELLET.asItem());
        itemModels.declareCustomModelItem(MekanismItems.HDPE_ROD.asItem());
        itemModels.declareCustomModelItem(MekanismItems.HDPE_SHEET.asItem());
        itemModels.declareCustomModelItem(MekanismItems.HDPE_STICK.asItem());
        itemModels.declareCustomModelItem(MekanismItems.MEKASUIT_BODYARMOR.asItem());
        itemModels.declareCustomModelItem(MekanismItems.MEKASUIT_BOOTS.asItem());
        itemModels.declareCustomModelItem(MekanismItems.MEKASUIT_HELMET.asItem());
        itemModels.declareCustomModelItem(MekanismItems.MEKASUIT_PANTS.asItem());
        itemModels.declareCustomModelItem(MekanismItems.MODULE_BASE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.NETWORK_READER.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ANTIMATTER_PELLET.asItem());
        itemModels.declareCustomModelItem(MekanismItems.PLUTONIUM_PELLET.asItem());
        itemModels.declareCustomModelItem(MekanismItems.POLONIUM_PELLET.asItem());
        itemModels.declareCustomModelItem(MekanismItems.PORTABLE_TELEPORTER.asItem());
        itemModels.declareCustomModelItem(MekanismItems.BASE_QIO_DRIVE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.HYPER_DENSE_QIO_DRIVE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.TIME_DILATING_QIO_DRIVE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.SUPERMASSIVE_QIO_DRIVE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.REPROCESSED_FISSILE_FRAGMENT.asItem());
        itemModels.declareCustomModelItem(MekanismItems.SALT.asItem());
        itemModels.declareCustomModelItem(MekanismItems.SAWDUST.asItem());
        itemModels.declareCustomModelItem(MekanismItems.SCUBA_MASK.asItem());
        itemModels.declareCustomModelItem(MekanismItems.SCUBA_TANK.asItem());
        itemModels.declareCustomModelItem(MekanismItems.SEISMIC_READER.asItem());
        itemModels.declareCustomModelItem(MekanismItems.SUBSTRATE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.TELEPORTATION_CORE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ULTIMATE_CONTROL_CIRCUIT.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ULTIMATE_TIER_INSTALLER.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ANCHOR_UPGRADE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.CHEMICAL_UPGRADE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.ENERGY_UPGRADE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.FILTER_UPGRADE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.MUFFLING_UPGRADE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.SPEED_UPGRADE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.STONE_GENERATOR_UPGRADE.asItem());
        itemModels.declareCustomModelItem(MekanismItems.YELLOW_CAKE_URANIUM.asItem());
        itemModels.declareCustomModelItem(MekanismItems.BRONZE_DUST.asItem());
        itemModels.declareCustomModelItem(MekanismItems.LAPIS_LAZULI_DUST.asItem());
        itemModels.declareCustomModelItem(MekanismItems.COAL_DUST.asItem());
        itemModels.declareCustomModelItem(MekanismItems.CHARCOAL_DUST.asItem());
        itemModels.declareCustomModelItem(MekanismItems.QUARTZ_DUST.asItem());
        itemModels.declareCustomModelItem(MekanismItems.EMERALD_DUST.asItem());
        itemModels.declareCustomModelItem(MekanismItems.DIAMOND_DUST.asItem());
        itemModels.declareCustomModelItem(MekanismItems.NETHERITE_DUST.asItem());
        itemModels.declareCustomModelItem(MekanismItems.STEEL_DUST.asItem());
        itemModels.declareCustomModelItem(MekanismItems.SULFUR_DUST.asItem());
        itemModels.declareCustomModelItem(MekanismItems.LITHIUM_DUST.asItem());
        itemModels.declareCustomModelItem(MekanismItems.REFINED_OBSIDIAN_DUST.asItem());
        itemModels.declareCustomModelItem(MekanismItems.OBSIDIAN_DUST.asItem());
        itemModels.declareCustomModelItem(MekanismItems.FLUORITE_DUST.asItem());
        itemModels.declareCustomModelItem(MekanismItems.BRONZE_INGOT.asItem());
        itemModels.declareCustomModelItem(MekanismItems.REFINED_OBSIDIAN_INGOT.asItem());
        itemModels.declareCustomModelItem(MekanismItems.REFINED_GLOWSTONE_INGOT.asItem());
        itemModels.declareCustomModelItem(MekanismItems.STEEL_INGOT.asItem());
        itemModels.declareCustomModelItem(MekanismItems.REFINED_OBSIDIAN_NUGGET.asItem());
        itemModels.declareCustomModelItem(MekanismItems.BRONZE_NUGGET.asItem());
        itemModels.declareCustomModelItem(MekanismItems.REFINED_GLOWSTONE_NUGGET.asItem());
        itemModels.declareCustomModelItem(MekanismItems.STEEL_NUGGET.asItem());
    }

    private void markManualBlocks() {
        markManualBlockState(MekanismBlocks.ADVANCED_BIN);
        //markManualBlockState(MekanismBlocks.ADVANCED_BOUNDING_BLOCK); todo: is this an old file?
        markManualBlockState(MekanismBlocks.ADVANCED_CHEMICAL_TANK);
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ADVANCED, FactoryType.COMBINING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ADVANCED, FactoryType.COMPRESSING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ADVANCED, FactoryType.CRUSHING));
        markManualBlockState(MekanismBlocks.ADVANCED_ENERGY_CUBE);
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ADVANCED, FactoryType.ENRICHING));
        markManualBlockState(MekanismBlocks.ADVANCED_FLUID_TANK);
        markManualBlockState(MekanismBlocks.ADVANCED_INDUCTION_CELL);
        markManualBlockState(MekanismBlocks.ADVANCED_INDUCTION_PROVIDER);
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ADVANCED, FactoryType.INFUSING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ADVANCED, FactoryType.INJECTING));
        markManualBlockState(MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER);
        markManualBlockState(MekanismBlocks.ADVANCED_MECHANICAL_PIPE);
        markManualBlockState(MekanismBlocks.ADVANCED_PRESSURIZED_TUBE);
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ADVANCED, FactoryType.PURIFYING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ADVANCED, FactoryType.SAWING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ADVANCED, FactoryType.SMELTING));
        markManualBlockState(MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR);
        markManualBlockState(MekanismBlocks.ADVANCED_UNIVERSAL_CABLE);
        markManualBlockState(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER);
        markManualBlockState(MekanismBlocks.BASIC_BIN);
        markManualBlockState(MekanismBlocks.BASIC_CHEMICAL_TANK);
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.BASIC, FactoryType.COMBINING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.BASIC, FactoryType.COMPRESSING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.BASIC, FactoryType.CRUSHING));
        markManualBlockState(MekanismBlocks.BASIC_ENERGY_CUBE);
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.BASIC, FactoryType.ENRICHING));
        markManualBlockState(MekanismBlocks.BASIC_FLUID_TANK);
        markManualBlockState(MekanismBlocks.BASIC_INDUCTION_CELL);
        markManualBlockState(MekanismBlocks.BASIC_INDUCTION_PROVIDER);
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.BASIC, FactoryType.INFUSING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.BASIC, FactoryType.INJECTING));
        markManualBlockState(MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER);
        markManualBlockState(MekanismBlocks.BASIC_MECHANICAL_PIPE);
        markManualBlockState(MekanismBlocks.BASIC_PRESSURIZED_TUBE);
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.BASIC, FactoryType.PURIFYING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.BASIC, FactoryType.SAWING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.BASIC, FactoryType.SMELTING));
        markManualBlockState(MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR);
        markManualBlockState(MekanismBlocks.BASIC_UNIVERSAL_CABLE);
        markManualBlockState(MekanismBlocks.BIO_FUEL_BLOCK);
        markManualBlockState(MekanismBlocks.BRONZE_BLOCK);
        markManualBlockState(MekanismBlocks.CHARCOAL_BLOCK);
        markManualBlockState(MekanismBlocks.FLUORITE_BLOCK);
        markManualBlockState(MekanismBlocks.REFINED_GLOWSTONE_BLOCK);
        markManualBlockState(MekanismBlocks.REFINED_OBSIDIAN_BLOCK);
        markManualBlockState(MekanismBlocks.SALT_BLOCK);
        markManualBlockState(MekanismBlocks.STEEL_BLOCK);
        markManualBlockState(MekanismBlocks.BOILER_CASING);
        markManualBlockState(MekanismBlocks.BOILER_VALVE);
        markManualBlockState(MekanismBlocks.BOUNDING_BLOCK);
        markManualBlockState(MekanismBlocks.CARDBOARD_BOX);
        markManualBlockState(MekanismBlocks.CHARGEPAD);
        markManualBlockState(MekanismBlocks.CHEMICAL_CRYSTALLIZER);
        markManualBlockState(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER);
        markManualBlockState(MekanismBlocks.CHEMICAL_INFUSER);
        markManualBlockState(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER);
        markManualBlockState(MekanismBlocks.CHEMICAL_OXIDIZER);
        markManualBlockState(MekanismBlocks.CHEMICAL_WASHER);
        markManualBlockState(MekanismBlocks.COMBINER);
        markManualBlockState(MekanismBlocks.CREATIVE_BIN);
        markManualBlockState(MekanismBlocks.CREATIVE_CHEMICAL_TANK);
        markManualBlockState(MekanismBlocks.CREATIVE_ENERGY_CUBE);
        markManualBlockState(MekanismBlocks.CREATIVE_FLUID_TANK);
        markManualBlockState(MekanismBlocks.CRUSHER);
        markManualBlockState(MekanismBlocks.DIGITAL_MINER);
        markManualBlockState(MekanismBlocks.DIVERSION_TRANSPORTER);
        markManualBlockState(MekanismBlocks.DYNAMIC_TANK);
        markManualBlockState(MekanismBlocks.DYNAMIC_VALVE);
        markManualBlockState(MekanismBlocks.ELECTRIC_PUMP);
        markManualBlockState(MekanismBlocks.ELECTROLYTIC_SEPARATOR);
        markManualBlockState(MekanismBlocks.ELITE_BIN);
        markManualBlockState(MekanismBlocks.ELITE_CHEMICAL_TANK);
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ELITE, FactoryType.COMBINING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ELITE, FactoryType.COMPRESSING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ELITE, FactoryType.CRUSHING));
        markManualBlockState(MekanismBlocks.ELITE_ENERGY_CUBE);
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ELITE, FactoryType.ENRICHING));
        markManualBlockState(MekanismBlocks.ELITE_FLUID_TANK);
        markManualBlockState(MekanismBlocks.ELITE_INDUCTION_CELL);
        markManualBlockState(MekanismBlocks.ELITE_INDUCTION_PROVIDER);
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ELITE, FactoryType.INFUSING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ELITE, FactoryType.INJECTING));
        markManualBlockState(MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER);
        markManualBlockState(MekanismBlocks.ELITE_MECHANICAL_PIPE);
        markManualBlockState(MekanismBlocks.ELITE_PRESSURIZED_TUBE);
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ELITE, FactoryType.PURIFYING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ELITE, FactoryType.SAWING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ELITE, FactoryType.SMELTING));
        markManualBlockState(MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR);
        markManualBlockState(MekanismBlocks.ELITE_UNIVERSAL_CABLE);
        markManualBlockState(MekanismBlocks.ENERGIZED_SMELTER);
        markManualBlockState(MekanismBlocks.ENRICHMENT_CHAMBER);
        markManualBlockState(MekanismBlocks.FLUIDIC_PLENISHER);
        markManualBlockState(MekanismBlocks.FORMULAIC_ASSEMBLICATOR);
        markManualBlockState(MekanismBlocks.FUELWOOD_HEATER);
        markManualBlockState(MekanismBlocks.INDUCTION_CASING);
        markManualBlockState(MekanismBlocks.INDUCTION_PORT);
        markManualBlockState(MekanismBlocks.INDUSTRIAL_ALARM);
        markManualBlockState(MekanismBlocks.ISOTOPIC_CENTRIFUGE);
        markManualBlockState(MekanismBlocks.LASER);
        markManualBlockState(MekanismBlocks.LASER_AMPLIFIER);
        markManualBlockState(MekanismBlocks.LASER_TRACTOR_BEAM);
        markManualBlockState(MekanismBlocks.LOGISTICAL_SORTER);
        markManualBlockState(MekanismBlocks.METALLURGIC_INFUSER);
        markManualBlockState(MekanismBlocks.MODIFICATION_STATION);
        markManualBlockState(MekanismBlocks.NUTRITIONAL_LIQUIFIER);
        markManualBlockState(MekanismBlocks.OREDICTIONIFICATOR);
        markManualBlockState(MekanismBlocks.OSMIUM_COMPRESSOR);
        markManualBlockState(MekanismBlocks.PAINTING_MACHINE);
        markManualBlockState(MekanismBlocks.PERSONAL_CHEST);
        markManualBlockState(MekanismBlocks.PIGMENT_EXTRACTOR);
        markManualBlockState(MekanismBlocks.PIGMENT_MIXER);
        markManualBlockState(MekanismBlocks.PRECISION_SAWMILL);
        markManualBlockState(MekanismBlocks.PRESSURE_DISPERSER);
        markManualBlockState(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER);
        markManualBlockState(MekanismBlocks.PURIFICATION_CHAMBER);
        markManualBlockState(MekanismBlocks.QIO_DASHBOARD);
        markManualBlockState(MekanismBlocks.QIO_DRIVE_ARRAY);
        markManualBlockState(MekanismBlocks.QIO_EXPORTER);
        markManualBlockState(MekanismBlocks.QIO_IMPORTER);
        markManualBlockState(MekanismBlocks.QIO_REDSTONE_ADAPTER);
        markManualBlockState(MekanismBlocks.QUANTUM_ENTANGLOPORTER);
        markManualBlockState(MekanismBlocks.RADIOACTIVE_WASTE_BARREL);
        markManualBlockState(MekanismBlocks.RESISTIVE_HEATER);
        markManualBlockState(MekanismBlocks.RESTRICTIVE_TRANSPORTER);
        markManualBlockState(MekanismBlocks.ROTARY_CONDENSENTRATOR);
        markManualBlockState(MekanismBlocks.SECURITY_DESK);
        markManualBlockState(MekanismBlocks.SEISMIC_VIBRATOR);
        markManualBlockState(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR);
        markManualBlockState(MekanismBlocks.SPS_CASING);
        markManualBlockState(MekanismBlocks.SPS_PORT);
        markManualBlockState(MekanismBlocks.STEEL_CASING);
        markManualBlockState(MekanismBlocks.STRUCTURAL_GLASS);
        markManualBlockState(MekanismBlocks.SUPERCHARGED_COIL);
        markManualBlockState(MekanismBlocks.SUPERHEATING_ELEMENT);
        markManualBlockState(MekanismBlocks.TELEPORTER);
        markManualBlockState(MekanismBlocks.TELEPORTER_FRAME);
        markManualBlockState(MekanismBlocks.THERMAL_EVAPORATION_BLOCK);
        markManualBlockState(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER);
        markManualBlockState(MekanismBlocks.THERMAL_EVAPORATION_VALVE);
        markManualBlockState(MekanismBlocks.ULTIMATE_BIN);
        markManualBlockState(MekanismBlocks.ULTIMATE_CHEMICAL_TANK);
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ULTIMATE, FactoryType.COMBINING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ULTIMATE, FactoryType.COMPRESSING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ULTIMATE, FactoryType.CRUSHING));
        markManualBlockState(MekanismBlocks.ULTIMATE_ENERGY_CUBE);
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ULTIMATE, FactoryType.ENRICHING));
        markManualBlockState(MekanismBlocks.ULTIMATE_FLUID_TANK);
        markManualBlockState(MekanismBlocks.ULTIMATE_INDUCTION_CELL);
        markManualBlockState(MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER);
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ULTIMATE, FactoryType.INFUSING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ULTIMATE, FactoryType.INJECTING));
        markManualBlockState(MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER);
        markManualBlockState(MekanismBlocks.ULTIMATE_MECHANICAL_PIPE);
        markManualBlockState(MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE);
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ULTIMATE, FactoryType.PURIFYING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ULTIMATE, FactoryType.SAWING));
        markManualBlockState(MekanismBlocks.getFactory(FactoryTier.ULTIMATE, FactoryType.SMELTING));
        markManualBlockState(MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR);
        markManualBlockState(MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE);
    }

}
