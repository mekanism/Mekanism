package mekanism.common.tag;

import com.google.common.collect.Table.Cell;
import java.util.List;
import java.util.Map;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registration.impl.SlurryRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGameEvents;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismInfuseTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismSlurries;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.resource.IResource;
import mekanism.common.resource.MiscResource;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tags.TagUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class MekanismTagProvider extends BaseTagProvider {

    public static final TagKey<EntityType<?>> PVI_COMPAT = TagUtils.createKey(ForgeRegistries.ENTITY_TYPES, new ResourceLocation("per-viam-invenire", "replace_vanilla_navigator"));
    public static final TagKey<Fluid> CREATE_NO_INFINITE_FLUID = FluidTags.create(new ResourceLocation("create", "no_infinite_draining"));

    public MekanismTagProvider(DataGenerator gen, @Nullable ExistingFileHelper existingFileHelper) {
        super(gen, Mekanism.MODID, existingFileHelper);
    }

    @Override
    protected List<IBlockProvider> getAllBlocks() {
        return MekanismBlocks.BLOCKS.getAllBlocks();
    }

    @Override
    protected void registerTags() {
        addProcessedResources();
        addBeaconTags();
        addBoxBlacklist();
        addTools();
        addArmor();
        addToTag(MekanismTags.Items.BATTERIES, MekanismItems.ENERGY_TABLET);
        addToTag(MekanismTags.Items.YELLOW_CAKE_URANIUM, MekanismItems.YELLOW_CAKE_URANIUM);
        addRods();
        addFuels();
        addAlloys();
        addCircuits();
        addEndermanBlacklist();
        addEnriched();
        addStorage();
        addOres();
        addStorageBlocks();
        addIngots();
        addNuggets();
        addDusts();
        addGems();
        addFluids();
        addGameEvents();
        addGasTags();
        addSlurryTags();
        addInfuseTags();
        addPellets();
        addColorableItems();
        getBlockBuilder(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE).add(Tags.Blocks.ORES, BlockTags.LOGS);
        addToTag(BlockTags.GUARDED_BY_PIGLINS, MekanismBlocks.REFINED_GLOWSTONE_BLOCK, MekanismBlocks.PERSONAL_BARREL, MekanismBlocks.PERSONAL_CHEST);
        addToTag(BlockTags.HOGLIN_REPELLENTS, MekanismBlocks.TELEPORTER, MekanismBlocks.QUANTUM_ENTANGLOPORTER);
        getItemBuilder(ItemTags.PIGLIN_LOVED).add(
              MekanismBlocks.REFINED_GLOWSTONE_BLOCK.asItem(),
              MekanismItems.REFINED_GLOWSTONE_INGOT.asItem(),
              MekanismItems.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.GOLD).asItem()
        ).add(
              MekanismTags.Items.ENRICHED_GOLD,
              MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.SHARD, PrimaryResource.GOLD),
              MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.CRYSTAL, PrimaryResource.GOLD),
              MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DIRTY_DUST, PrimaryResource.GOLD),
              MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.CLUMP, PrimaryResource.GOLD)
        );
        addEntities();
        getBlockBuilder(MekanismTags.Blocks.MINER_BLACKLIST);
        addHarvestRequirements();
        addToTag(BlockTags.IMPERMEABLE, MekanismBlocks.STRUCTURAL_GLASS);
        //Note: Axolotls live in a brackish water (mix between fresh and salt), so it is reasonable there may be salt nearby
        addToTag(BlockTags.AXOLOTLS_SPAWNABLE_ON, MekanismBlocks.SALT_BLOCK);
        addToTag(ItemTags.CLUSTER_MAX_HARVESTABLES, MekanismItems.ATOMIC_DISASSEMBLER, MekanismItems.MEKA_TOOL);
        addToTag(ItemTags.FREEZE_IMMUNE_WEARABLES, MekanismItems.MEKASUIT_HELMET, MekanismItems.MEKASUIT_BODYARMOR, MekanismItems.MEKASUIT_PANTS, MekanismItems.MEKASUIT_BOOTS);
        addToTag(BlockTags.SCULK_REPLACEABLE, MekanismBlocks.SALT_BLOCK);
    }

    private void addEntities() {
        addToTag(EntityTypeTags.IMPACT_PROJECTILES, MekanismEntityTypes.FLAME);
        addToTag(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS, MekanismEntityTypes.ROBIT);
        addToTag(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES, MekanismEntityTypes.ROBIT);
        addToTag(PVI_COMPAT, MekanismEntityTypes.ROBIT);
    }

    private void addProcessedResources() {
        for (Cell<ResourceType, PrimaryResource, ItemRegistryObject<Item>> item : MekanismItems.PROCESSED_RESOURCES.cellSet()) {
            TagKey<Item> tag = addToTag(item.getRowKey(), item.getColumnKey(), item.getValue());
            getItemBuilder(switch (item.getRowKey()) {
                case SHARD -> MekanismTags.Items.SHARDS;
                case CRYSTAL -> MekanismTags.Items.CRYSTALS;
                case DUST -> Tags.Items.DUSTS;
                case DIRTY_DUST -> MekanismTags.Items.DIRTY_DUSTS;
                case CLUMP -> MekanismTags.Items.CLUMPS;
                case INGOT -> Tags.Items.INGOTS;
                case RAW -> Tags.Items.RAW_MATERIALS;
                case NUGGET -> Tags.Items.NUGGETS;
                default -> throw new IllegalStateException("Unexpected resource type for primary resource.");
            }).add(tag);
        }
    }

    private TagKey<Item> addToTag(ResourceType type, PrimaryResource resource, IItemProvider... items) {
        TagKey<Item> tag = MekanismTags.Items.PROCESSED_RESOURCES.get(type, resource);
        addToTag(tag, items);
        return tag;
    }

    private void addBeaconTags() {
        //Beacon bases
        addToTag(BlockTags.BEACON_BASE_BLOCKS,
              MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(PrimaryResource.OSMIUM),
              MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(PrimaryResource.TIN),
              MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(PrimaryResource.LEAD),
              MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(PrimaryResource.URANIUM),
              MekanismBlocks.BRONZE_BLOCK,
              MekanismBlocks.REFINED_OBSIDIAN_BLOCK,
              MekanismBlocks.REFINED_GLOWSTONE_BLOCK,
              MekanismBlocks.STEEL_BLOCK
        );
        //Beacon payment items
        addToTag(ItemTags.BEACON_PAYMENT_ITEMS,
              MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM),
              MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN),
              MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD),
              MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.URANIUM),
              MekanismItems.BRONZE_INGOT,
              MekanismItems.REFINED_OBSIDIAN_INGOT,
              MekanismItems.REFINED_GLOWSTONE_INGOT,
              MekanismItems.STEEL_INGOT
        );
    }

    private void addBoxBlacklist() {
        addToTag(MekanismTags.Blocks.RELOCATION_NOT_SUPPORTED,
              MekanismBlocks.CARDBOARD_BOX,
              MekanismBlocks.BOUNDING_BLOCK,
              MekanismBlocks.SECURITY_DESK,
              MekanismBlocks.DIGITAL_MINER,
              MekanismBlocks.SEISMIC_VIBRATOR,
              MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR,
              MekanismBlocks.MODIFICATION_STATION,
              MekanismBlocks.ISOTOPIC_CENTRIFUGE,
              MekanismBlocks.PIGMENT_MIXER,
              //Don't allow blocks that may have a radioactive substance in them to be picked up as it
              // will effectively dupe the radiation and also leak out into the atmosphere which is not
              // what people want, and means that it is likely someone miss-clicked.
              MekanismBlocks.RADIOACTIVE_WASTE_BARREL,
              MekanismBlocks.PRESSURIZED_REACTION_CHAMBER,
              MekanismBlocks.BASIC_PRESSURIZED_TUBE,
              MekanismBlocks.ADVANCED_PRESSURIZED_TUBE,
              MekanismBlocks.ELITE_PRESSURIZED_TUBE,
              MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE
        );
        getBlockBuilder(MekanismTags.Blocks.CARDBOARD_BLACKLIST)
              .add(MekanismTags.Blocks.RELOCATION_NOT_SUPPORTED, BlockTags.BEDS, BlockTags.DOORS);
        TileEntityTypeRegistryObject<?>[] tilesToBlacklist = {
              MekanismTileEntityTypes.CARDBOARD_BOX,
              MekanismTileEntityTypes.BOUNDING_BLOCK,
              MekanismTileEntityTypes.SECURITY_DESK,
              MekanismTileEntityTypes.DIGITAL_MINER,
              MekanismTileEntityTypes.SEISMIC_VIBRATOR,
              MekanismTileEntityTypes.SOLAR_NEUTRON_ACTIVATOR,
              MekanismTileEntityTypes.MODIFICATION_STATION,
              MekanismTileEntityTypes.ISOTOPIC_CENTRIFUGE,
              MekanismTileEntityTypes.PIGMENT_MIXER,
              //Don't allow blocks that may have a radioactive substance in them to be picked up as it
              // will effectively dupe the radiation and also leak out into the atmosphere which is not
              // what people want, and means that it is likely someone miss-clicked.
              MekanismTileEntityTypes.RADIOACTIVE_WASTE_BARREL,
              MekanismTileEntityTypes.PRESSURIZED_REACTION_CHAMBER,
              MekanismTileEntityTypes.BASIC_PRESSURIZED_TUBE,
              MekanismTileEntityTypes.ADVANCED_PRESSURIZED_TUBE,
              MekanismTileEntityTypes.ELITE_PRESSURIZED_TUBE,
              MekanismTileEntityTypes.ULTIMATE_PRESSURIZED_TUBE
        };
        addToTag(MekanismTags.TileEntityTypes.IMMOVABLE, tilesToBlacklist);
        addToTag(MekanismTags.TileEntityTypes.RELOCATION_NOT_SUPPORTED, tilesToBlacklist);
        getTileEntityTypeBuilder(MekanismTags.TileEntityTypes.CARDBOARD_BLACKLIST)
              .add(MekanismTags.TileEntityTypes.IMMOVABLE, MekanismTags.TileEntityTypes.RELOCATION_NOT_SUPPORTED);
    }

    private void addTools() {
        addWrenches();
        addToTag(Tags.Items.TOOLS_BOWS, MekanismItems.ELECTRIC_BOW);
    }

    private void addWrenches() {
        addToTag(MekanismTags.Items.WRENCHES, MekanismItems.CONFIGURATOR);
        getItemBuilder(Tags.Items.TOOLS).add(MekanismTags.Items.TOOLS_WRENCH);
        addToTag(MekanismTags.Items.TOOLS_WRENCH, MekanismItems.CONFIGURATOR);
        getItemBuilder(MekanismTags.Items.CONFIGURATORS).add(MekanismTags.Items.WRENCHES, MekanismTags.Items.TOOLS_WRENCH);
    }

    private void addArmor() {
        getItemBuilder(Tags.Items.ARMORS_HELMETS).add(MekanismTags.Items.ARMORS_HELMETS_HAZMAT);
        getItemBuilder(Tags.Items.ARMORS_CHESTPLATES).add(MekanismTags.Items.ARMORS_CHESTPLATES_HAZMAT);
        getItemBuilder(Tags.Items.ARMORS_LEGGINGS).add(MekanismTags.Items.ARMORS_LEGGINGS_HAZMAT);
        getItemBuilder(Tags.Items.ARMORS_BOOTS).add(MekanismTags.Items.ARMORS_BOOTS_HAZMAT);

        addToTag(MekanismTags.Items.ARMORS_HELMETS_HAZMAT, MekanismItems.HAZMAT_MASK);
        addToTag(MekanismTags.Items.ARMORS_CHESTPLATES_HAZMAT, MekanismItems.HAZMAT_GOWN);
        addToTag(MekanismTags.Items.ARMORS_LEGGINGS_HAZMAT, MekanismItems.HAZMAT_PANTS);
        addToTag(MekanismTags.Items.ARMORS_BOOTS_HAZMAT, MekanismItems.HAZMAT_BOOTS);
    }

    private void addRods() {
        addToTag(MekanismTags.Items.RODS_PLASTIC, MekanismItems.HDPE_STICK);
        getItemBuilder(Tags.Items.RODS).add(MekanismTags.Items.RODS_PLASTIC);
    }

    private void addFuels() {
        addToTag(MekanismTags.Items.FUELS_BIO, MekanismItems.BIO_FUEL);
        getItemBuilder(MekanismTags.Items.FUELS).add(MekanismTags.Items.FUELS_BIO);
    }

    private void addAlloys() {
        //Alloy Tags that go in the forge domain
        addToTag(MekanismTags.Items.ALLOYS_ADVANCED, MekanismItems.INFUSED_ALLOY);
        addToTag(MekanismTags.Items.ALLOYS_ELITE, MekanismItems.REINFORCED_ALLOY);
        addToTag(MekanismTags.Items.ALLOYS_ULTIMATE, MekanismItems.ATOMIC_ALLOY);
        getItemBuilder(MekanismTags.Items.FORGE_ALLOYS).add(MekanismTags.Items.ALLOYS_ADVANCED, MekanismTags.Items.ALLOYS_ELITE, MekanismTags.Items.ALLOYS_ULTIMATE);
        //Alloy tags that go in our domain
        addToTag(MekanismTags.Items.ALLOYS_BASIC, Items.REDSTONE);
        getItemBuilder(MekanismTags.Items.ALLOYS_INFUSED).add(MekanismTags.Items.ALLOYS_ADVANCED);
        getItemBuilder(MekanismTags.Items.ALLOYS_REINFORCED).add(MekanismTags.Items.ALLOYS_ELITE);
        getItemBuilder(MekanismTags.Items.ALLOYS_ATOMIC).add(MekanismTags.Items.ALLOYS_ULTIMATE);
        getItemBuilder(MekanismTags.Items.ALLOYS).add(MekanismTags.Items.ALLOYS_BASIC, MekanismTags.Items.ALLOYS_INFUSED, MekanismTags.Items.ALLOYS_REINFORCED,
              MekanismTags.Items.ALLOYS_ATOMIC);
    }

    private void addCircuits() {
        addToTag(MekanismTags.Items.CIRCUITS_BASIC, MekanismItems.BASIC_CONTROL_CIRCUIT);
        addToTag(MekanismTags.Items.CIRCUITS_ADVANCED, MekanismItems.ADVANCED_CONTROL_CIRCUIT);
        addToTag(MekanismTags.Items.CIRCUITS_ELITE, MekanismItems.ELITE_CONTROL_CIRCUIT);
        addToTag(MekanismTags.Items.CIRCUITS_ULTIMATE, MekanismItems.ULTIMATE_CONTROL_CIRCUIT);
        getItemBuilder(MekanismTags.Items.CIRCUITS).add(MekanismTags.Items.CIRCUITS_BASIC, MekanismTags.Items.CIRCUITS_ADVANCED, MekanismTags.Items.CIRCUITS_ELITE,
              MekanismTags.Items.CIRCUITS_ULTIMATE);
    }

    private void addEndermanBlacklist() {
        addToTag(Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST,
              MekanismBlocks.DYNAMIC_TANK,
              MekanismBlocks.DYNAMIC_VALVE,
              MekanismBlocks.BOILER_CASING,
              MekanismBlocks.BOILER_VALVE,
              MekanismBlocks.PRESSURE_DISPERSER,
              MekanismBlocks.SUPERHEATING_ELEMENT,
              MekanismBlocks.INDUCTION_CASING,
              MekanismBlocks.INDUCTION_PORT,
              MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER,
              MekanismBlocks.THERMAL_EVAPORATION_VALVE,
              MekanismBlocks.THERMAL_EVAPORATION_BLOCK,
              MekanismBlocks.STRUCTURAL_GLASS,
              MekanismBlocks.SPS_CASING,
              MekanismBlocks.SPS_PORT,
              MekanismBlocks.SUPERCHARGED_COIL
        );
    }

    private void addEnriched() {
        addToTag(MekanismTags.Items.ENRICHED_CARBON, MekanismItems.ENRICHED_CARBON);
        addToTag(MekanismTags.Items.ENRICHED_DIAMOND, MekanismItems.ENRICHED_DIAMOND);
        addToTag(MekanismTags.Items.ENRICHED_OBSIDIAN, MekanismItems.ENRICHED_OBSIDIAN);
        addToTag(MekanismTags.Items.ENRICHED_REDSTONE, MekanismItems.ENRICHED_REDSTONE);
        addToTag(MekanismTags.Items.ENRICHED_GOLD, MekanismItems.ENRICHED_GOLD);
        addToTag(MekanismTags.Items.ENRICHED_TIN, MekanismItems.ENRICHED_TIN);
        getItemBuilder(MekanismTags.Items.ENRICHED).add(MekanismTags.Items.ENRICHED_CARBON, MekanismTags.Items.ENRICHED_DIAMOND, MekanismTags.Items.ENRICHED_OBSIDIAN,
              MekanismTags.Items.ENRICHED_REDSTONE, MekanismTags.Items.ENRICHED_GOLD, MekanismTags.Items.ENRICHED_TIN);
    }

    private void addStorage() {
        addToTag(MekanismTags.Blocks.BARRELS_PERSONAL, MekanismBlocks.PERSONAL_BARREL);
        getBlockBuilder(Tags.Blocks.BARRELS).add(MekanismTags.Blocks.BARRELS_PERSONAL);
        addToTag(MekanismTags.Blocks.CHESTS_ELECTRIC, MekanismBlocks.PERSONAL_CHEST);
        addToTag(MekanismTags.Blocks.CHESTS_PERSONAL, MekanismBlocks.PERSONAL_CHEST);
        getBlockBuilder(Tags.Blocks.CHESTS).add(MekanismTags.Blocks.CHESTS_ELECTRIC, MekanismTags.Blocks.CHESTS_PERSONAL);
        addToTag(MekanismTags.Items.PERSONAL_STORAGE, MekanismBlocks.PERSONAL_BARREL, MekanismBlocks.PERSONAL_CHEST);
        getBlockBuilder(MekanismTags.Blocks.PERSONAL_STORAGE).add(MekanismTags.Blocks.BARRELS_PERSONAL, MekanismTags.Blocks.CHESTS_PERSONAL);
    }

    private void addOres() {
        for (Map.Entry<OreType, OreBlockType> entry : MekanismBlocks.ORES.entrySet()) {
            OreType type = entry.getKey();
            OreBlockType oreBlockType = entry.getValue();
            TagKey<Item> itemTag = MekanismTags.Items.ORES.get(type);
            TagKey<Block> blockTag = MekanismTags.Blocks.ORES.get(type);
            addToTags(itemTag, blockTag, oreBlockType.stone(), oreBlockType.deepslate());
            getItemBuilder(Tags.Items.ORES).add(itemTag);
            getBlockBuilder(Tags.Blocks.ORES).add(blockTag);
            if (type.getResource() == MiscResource.FLUORITE) {
                addToTags(Tags.Items.ORE_RATES_DENSE, Tags.Blocks.ORE_RATES_DENSE, oreBlockType.stone(), oreBlockType.deepslate());
            } else {
                addToTags(Tags.Items.ORE_RATES_SINGULAR, Tags.Blocks.ORE_RATES_SINGULAR, oreBlockType.stone(), oreBlockType.deepslate());
            }
            addToTags(Tags.Items.ORES_IN_GROUND_DEEPSLATE, Tags.Blocks.ORES_IN_GROUND_DEEPSLATE, oreBlockType.deepslate());
            addToTags(Tags.Items.ORES_IN_GROUND_STONE, Tags.Blocks.ORES_IN_GROUND_STONE, oreBlockType.stone());
            addToTag(BlockTags.OVERWORLD_CARVER_REPLACEABLES, oreBlockType.stone(), oreBlockType.deepslate());
            addToTag(BlockTags.SNAPS_GOAT_HORN, oreBlockType.stone(), oreBlockType.deepslate());
        }
    }

    private void addStorageBlocks() {
        addToTags(MekanismTags.Items.STORAGE_BLOCKS_BRONZE, MekanismTags.Blocks.STORAGE_BLOCKS_BRONZE, MekanismBlocks.BRONZE_BLOCK);
        addToTags(MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL, MekanismTags.Blocks.STORAGE_BLOCKS_CHARCOAL, MekanismBlocks.CHARCOAL_BLOCK);
        addToTags(MekanismTags.Items.STORAGE_BLOCKS_REFINED_GLOWSTONE, MekanismTags.Blocks.STORAGE_BLOCKS_REFINED_GLOWSTONE, MekanismBlocks.REFINED_GLOWSTONE_BLOCK);
        addToTags(MekanismTags.Items.STORAGE_BLOCKS_REFINED_OBSIDIAN, MekanismTags.Blocks.STORAGE_BLOCKS_REFINED_OBSIDIAN, MekanismBlocks.REFINED_OBSIDIAN_BLOCK);
        addToTags(MekanismTags.Items.STORAGE_BLOCKS_STEEL, MekanismTags.Blocks.STORAGE_BLOCKS_STEEL, MekanismBlocks.STEEL_BLOCK);
        addToTags(MekanismTags.Items.STORAGE_BLOCKS_FLUORITE, MekanismTags.Blocks.STORAGE_BLOCKS_FLUORITE, MekanismBlocks.FLUORITE_BLOCK);
        getItemBuilder(Tags.Items.STORAGE_BLOCKS).add(MekanismTags.Items.STORAGE_BLOCKS_BRONZE, MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL,
              MekanismTags.Items.STORAGE_BLOCKS_REFINED_GLOWSTONE, MekanismTags.Items.STORAGE_BLOCKS_REFINED_OBSIDIAN, MekanismTags.Items.STORAGE_BLOCKS_STEEL,
              MekanismTags.Items.STORAGE_BLOCKS_FLUORITE);
        getBlockBuilder(Tags.Blocks.STORAGE_BLOCKS).add(MekanismTags.Blocks.STORAGE_BLOCKS_BRONZE, MekanismTags.Blocks.STORAGE_BLOCKS_CHARCOAL,
              MekanismTags.Blocks.STORAGE_BLOCKS_REFINED_GLOWSTONE, MekanismTags.Blocks.STORAGE_BLOCKS_REFINED_OBSIDIAN, MekanismTags.Blocks.STORAGE_BLOCKS_STEEL,
              MekanismTags.Blocks.STORAGE_BLOCKS_FLUORITE);
        // Dynamic storage blocks
        for (Map.Entry<IResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            TagKey<Item> itemTag = MekanismTags.Items.PROCESSED_RESOURCE_BLOCKS.get(entry.getKey());
            TagKey<Block> blockTag = MekanismTags.Blocks.RESOURCE_STORAGE_BLOCKS.get(entry.getKey());
            addToTags(itemTag, blockTag, entry.getValue());
            getItemBuilder(Tags.Items.STORAGE_BLOCKS).add(itemTag);
            getBlockBuilder(Tags.Blocks.STORAGE_BLOCKS).add(blockTag);
        }
    }

    private void addIngots() {
        addToTag(MekanismTags.Items.INGOTS_BRONZE, MekanismItems.BRONZE_INGOT);
        addToTag(MekanismTags.Items.INGOTS_REFINED_GLOWSTONE, MekanismItems.REFINED_GLOWSTONE_INGOT);
        addToTag(MekanismTags.Items.INGOTS_REFINED_OBSIDIAN, MekanismItems.REFINED_OBSIDIAN_INGOT);
        addToTag(MekanismTags.Items.INGOTS_STEEL, MekanismItems.STEEL_INGOT);
        getItemBuilder(Tags.Items.INGOTS).add(MekanismTags.Items.INGOTS_BRONZE,
              MekanismTags.Items.INGOTS_REFINED_GLOWSTONE, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN, MekanismTags.Items.INGOTS_STEEL);
    }

    private void addNuggets() {
        addToTag(MekanismTags.Items.NUGGETS_BRONZE, MekanismItems.BRONZE_NUGGET);
        addToTag(MekanismTags.Items.NUGGETS_REFINED_GLOWSTONE, MekanismItems.REFINED_GLOWSTONE_NUGGET);
        addToTag(MekanismTags.Items.NUGGETS_REFINED_OBSIDIAN, MekanismItems.REFINED_OBSIDIAN_NUGGET);
        addToTag(MekanismTags.Items.NUGGETS_STEEL, MekanismItems.STEEL_NUGGET);
        getItemBuilder(Tags.Items.NUGGETS).add(MekanismTags.Items.NUGGETS_BRONZE,
              MekanismTags.Items.NUGGETS_REFINED_GLOWSTONE, MekanismTags.Items.NUGGETS_REFINED_OBSIDIAN, MekanismTags.Items.NUGGETS_STEEL);
    }

    private void addDusts() {
        addToTag(MekanismTags.Items.DUSTS_BRONZE, MekanismItems.BRONZE_DUST);
        addToTag(MekanismTags.Items.DUSTS_CHARCOAL, MekanismItems.CHARCOAL_DUST);
        addToTag(MekanismTags.Items.DUSTS_COAL, MekanismItems.COAL_DUST);
        addToTag(MekanismTags.Items.DUSTS_DIAMOND, MekanismItems.DIAMOND_DUST);
        addToTag(MekanismTags.Items.DUSTS_EMERALD, MekanismItems.EMERALD_DUST);
        addToTag(MekanismTags.Items.DUSTS_NETHERITE, MekanismItems.NETHERITE_DUST);
        addToTag(MekanismTags.Items.DUSTS_LAPIS, MekanismItems.LAPIS_LAZULI_DUST);
        addToTag(MekanismTags.Items.DUSTS_LITHIUM, MekanismItems.LITHIUM_DUST);
        addToTag(MekanismTags.Items.DUSTS_OBSIDIAN, MekanismItems.OBSIDIAN_DUST);
        addToTag(MekanismTags.Items.DUSTS_QUARTZ, MekanismItems.QUARTZ_DUST);
        addToTag(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN, MekanismItems.REFINED_OBSIDIAN_DUST);
        addToTag(MekanismTags.Items.DUSTS_SALT, MekanismItems.SALT);
        addToTag(MekanismTags.Items.DUSTS_STEEL, MekanismItems.STEEL_DUST);
        addToTag(MekanismTags.Items.DUSTS_SULFUR, MekanismItems.SULFUR_DUST);
        addToTag(MekanismTags.Items.DUSTS_WOOD, MekanismItems.SAWDUST);
        addToTag(MekanismTags.Items.DUSTS_FLUORITE, MekanismItems.FLUORITE_DUST);
        getItemBuilder(Tags.Items.DUSTS).add(MekanismTags.Items.DUSTS_BRONZE, MekanismTags.Items.DUSTS_CHARCOAL, MekanismTags.Items.DUSTS_COAL,
              MekanismTags.Items.DUSTS_DIAMOND, MekanismTags.Items.DUSTS_EMERALD, MekanismTags.Items.DUSTS_NETHERITE, MekanismTags.Items.DUSTS_LAPIS,
              MekanismTags.Items.DUSTS_LITHIUM, MekanismTags.Items.DUSTS_OBSIDIAN, MekanismTags.Items.DUSTS_QUARTZ, MekanismTags.Items.DUSTS_REFINED_OBSIDIAN,
              MekanismTags.Items.DUSTS_SALT, MekanismTags.Items.DUSTS_STEEL, MekanismTags.Items.DUSTS_SULFUR, MekanismTags.Items.DUSTS_WOOD,
              MekanismTags.Items.DUSTS_FLUORITE);

        addToTag(Tags.Items.DYES_YELLOW, MekanismItems.SULFUR_DUST);

        getItemBuilder(MekanismTags.Items.SALT).add(MekanismTags.Items.DUSTS_SALT);
        getItemBuilder(MekanismTags.Items.SAWDUST).add(MekanismTags.Items.DUSTS_WOOD);
    }

    private void addGems() {
        addToTag(MekanismTags.Items.GEMS_FLUORITE, MekanismItems.FLUORITE_GEM);
        getItemBuilder(Tags.Items.GEMS).add(MekanismTags.Items.GEMS_FLUORITE);
    }

    private void addPellets() {
        addToTag(MekanismTags.Items.PELLETS_ANTIMATTER, MekanismItems.ANTIMATTER_PELLET);
        addToTag(MekanismTags.Items.PELLETS_PLUTONIUM, MekanismItems.PLUTONIUM_PELLET);
        addToTag(MekanismTags.Items.PELLETS_POLONIUM, MekanismItems.POLONIUM_PELLET);
    }

    private void addColorableItems() {
        addToTag(MekanismTags.Items.COLORABLE_WOOL, Blocks.WHITE_WOOL, Blocks.ORANGE_WOOL, Blocks.MAGENTA_WOOL, Blocks.LIGHT_BLUE_WOOL, Blocks.YELLOW_WOOL,
              Blocks.LIME_WOOL, Blocks.PINK_WOOL, Blocks.GRAY_WOOL, Blocks.LIGHT_GRAY_WOOL, Blocks.CYAN_WOOL, Blocks.PURPLE_WOOL, Blocks.BLUE_WOOL, Blocks.BROWN_WOOL,
              Blocks.GREEN_WOOL, Blocks.RED_WOOL, Blocks.BLACK_WOOL);
        addToTag(MekanismTags.Items.COLORABLE_CARPETS, Blocks.WHITE_CARPET, Blocks.ORANGE_CARPET, Blocks.MAGENTA_CARPET, Blocks.LIGHT_BLUE_CARPET, Blocks.YELLOW_CARPET,
              Blocks.LIME_CARPET, Blocks.PINK_CARPET, Blocks.GRAY_CARPET, Blocks.LIGHT_GRAY_CARPET, Blocks.CYAN_CARPET, Blocks.PURPLE_CARPET, Blocks.BLUE_CARPET,
              Blocks.BROWN_CARPET, Blocks.GREEN_CARPET, Blocks.RED_CARPET, Blocks.BLACK_CARPET);
        addToTag(MekanismTags.Items.COLORABLE_BEDS, Blocks.WHITE_BED, Blocks.ORANGE_BED, Blocks.MAGENTA_BED, Blocks.LIGHT_BLUE_BED, Blocks.YELLOW_BED,
              Blocks.LIME_BED, Blocks.PINK_BED, Blocks.GRAY_BED, Blocks.LIGHT_GRAY_BED, Blocks.CYAN_BED, Blocks.PURPLE_BED, Blocks.BLUE_BED, Blocks.BROWN_BED,
              Blocks.GREEN_BED, Blocks.RED_BED, Blocks.BLACK_BED);
        addToTag(MekanismTags.Items.COLORABLE_GLASS, Blocks.GLASS, Blocks.WHITE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS,
              Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS, Blocks.LIME_STAINED_GLASS, Blocks.PINK_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS,
              Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS,
              Blocks.GREEN_STAINED_GLASS, Blocks.RED_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS);
        addToTag(MekanismTags.Items.COLORABLE_GLASS_PANES, Blocks.GLASS_PANE, Blocks.WHITE_STAINED_GLASS_PANE, Blocks.ORANGE_STAINED_GLASS_PANE,
              Blocks.MAGENTA_STAINED_GLASS_PANE, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.LIME_STAINED_GLASS_PANE,
              Blocks.PINK_STAINED_GLASS_PANE, Blocks.GRAY_STAINED_GLASS_PANE, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Blocks.CYAN_STAINED_GLASS_PANE,
              Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.BLUE_STAINED_GLASS_PANE, Blocks.BROWN_STAINED_GLASS_PANE, Blocks.GREEN_STAINED_GLASS_PANE,
              Blocks.RED_STAINED_GLASS_PANE, Blocks.BLACK_STAINED_GLASS_PANE);
        addToTag(MekanismTags.Items.COLORABLE_TERRACOTTA, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA,
              Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA,
              Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA,
              Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA);
        addToTag(MekanismTags.Items.COLORABLE_CANDLE, Blocks.CANDLE, Blocks.WHITE_CANDLE, Blocks.ORANGE_CANDLE, Blocks.MAGENTA_CANDLE, Blocks.LIGHT_BLUE_CANDLE,
              Blocks.YELLOW_CANDLE, Blocks.LIME_CANDLE, Blocks.PINK_CANDLE, Blocks.GRAY_CANDLE, Blocks.LIGHT_GRAY_CANDLE, Blocks.CYAN_CANDLE, Blocks.PURPLE_CANDLE,
              Blocks.BLUE_CANDLE, Blocks.BROWN_CANDLE, Blocks.GREEN_CANDLE, Blocks.RED_CANDLE, Blocks.BLACK_CANDLE);
        addToTag(MekanismTags.Items.COLORABLE_CONCRETE, Blocks.WHITE_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.MAGENTA_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE,
              Blocks.YELLOW_CONCRETE, Blocks.LIME_CONCRETE, Blocks.PINK_CONCRETE, Blocks.GRAY_CONCRETE, Blocks.LIGHT_GRAY_CONCRETE, Blocks.CYAN_CONCRETE,
              Blocks.PURPLE_CONCRETE, Blocks.BLUE_CONCRETE, Blocks.BROWN_CONCRETE, Blocks.GREEN_CONCRETE, Blocks.RED_CONCRETE, Blocks.BLACK_CONCRETE);
        addToTag(MekanismTags.Items.COLORABLE_CONCRETE_POWDER, Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER,
              Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER, Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER,
              Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER, Blocks.BROWN_CONCRETE_POWDER,
              Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER, Blocks.BLACK_CONCRETE_POWDER);
        getItemBuilder(MekanismTags.Items.COLORABLE_BANNERS).addTyped(color -> BannerBlock.byColor(color).asItem(), DyeColor.values());
    }

    private void addFluids() {
        addToTag(MekanismTags.Fluids.BRINE, MekanismFluids.BRINE);
        addToTag(MekanismTags.Fluids.CHLORINE, MekanismFluids.CHLORINE);
        addToTag(MekanismTags.Fluids.ETHENE, MekanismFluids.ETHENE);
        addToTag(MekanismTags.Fluids.HEAVY_WATER, MekanismFluids.HEAVY_WATER);
        addToTag(MekanismTags.Fluids.HYDROGEN, MekanismFluids.HYDROGEN);
        addToTag(MekanismTags.Fluids.HYDROGEN_CHLORIDE, MekanismFluids.HYDROGEN_CHLORIDE);
        addToTag(MekanismTags.Fluids.LITHIUM, MekanismFluids.LITHIUM);
        addToTag(MekanismTags.Fluids.OXYGEN, MekanismFluids.OXYGEN);
        addToTag(MekanismTags.Fluids.SODIUM, MekanismFluids.SODIUM);
        addToTag(MekanismTags.Fluids.SUPERHEATED_SODIUM, MekanismFluids.SUPERHEATED_SODIUM);
        addToTag(MekanismTags.Fluids.STEAM, MekanismFluids.STEAM);
        addToTag(MekanismTags.Fluids.SULFUR_DIOXIDE, MekanismFluids.SULFUR_DIOXIDE);
        addToTag(MekanismTags.Fluids.SULFUR_TRIOXIDE, MekanismFluids.SULFUR_TRIOXIDE);
        addToTag(MekanismTags.Fluids.SULFURIC_ACID, MekanismFluids.SULFURIC_ACID);
        addToTag(MekanismTags.Fluids.HYDROFLUORIC_ACID, MekanismFluids.HYDROFLUORIC_ACID);
        addToTag(MekanismTags.Fluids.URANIUM_OXIDE, MekanismFluids.URANIUM_OXIDE);
        addToTag(MekanismTags.Fluids.URANIUM_HEXAFLUORIDE, MekanismFluids.URANIUM_HEXAFLUORIDE);
        addToTag(Tags.Fluids.GASEOUS, MekanismFluids.STEAM);
        //Prevent all our fluids from being duped by create
        for (FluidRegistryObject<?, ?, ?, ?, ?> fluid : MekanismFluids.FLUIDS.getAllFluids()) {
            addToTag(CREATE_NO_INFINITE_FLUID, fluid);
        }
    }

    private void addGameEvents() {
        addToTag(GameEventTags.VIBRATIONS, MekanismGameEvents.SEISMIC_VIBRATION, MekanismGameEvents.JETPACK_BURN, MekanismGameEvents.GRAVITY_MODULATE,
              MekanismGameEvents.GRAVITY_MODULATE_BOOSTED);
        addToTag(GameEventTags.WARDEN_CAN_LISTEN, MekanismGameEvents.SEISMIC_VIBRATION, MekanismGameEvents.JETPACK_BURN, MekanismGameEvents.GRAVITY_MODULATE,
              MekanismGameEvents.GRAVITY_MODULATE_BOOSTED);
    }

    private void addGasTags() {
        addToTag(MekanismTags.Gases.WATER_VAPOR, MekanismGases.WATER_VAPOR, MekanismGases.STEAM);
        addToTag(MekanismTags.Gases.WASTE_BARREL_DECAY_BLACKLIST, MekanismGases.PLUTONIUM, MekanismGases.POLONIUM);
    }

    private void addSlurryTags(SlurryRegistryObject<?, ?>... slurryRegistryObjects) {
        ForgeRegistryTagBuilder<Slurry> dirtyTagBuilder = getSlurryBuilder(MekanismTags.Slurries.DIRTY);
        ForgeRegistryTagBuilder<Slurry> cleanTagBuilder = getSlurryBuilder(MekanismTags.Slurries.CLEAN);
        for (SlurryRegistryObject<?, ?> slurryRO : slurryRegistryObjects) {
            dirtyTagBuilder.add(slurryRO.getDirtySlurry());
            cleanTagBuilder.add(slurryRO.getCleanSlurry());
        }
        // add dynamic slurry tags
        for (SlurryRegistryObject<?, ?> slurryRO : MekanismSlurries.PROCESSED_RESOURCES.values()) {
            dirtyTagBuilder.add(slurryRO.getDirtySlurry());
            cleanTagBuilder.add(slurryRO.getCleanSlurry());
        }
    }

    private void addInfuseTags() {
        addToTag(MekanismTags.InfuseTypes.CARBON, MekanismInfuseTypes.CARBON);
        addToTag(MekanismTags.InfuseTypes.REDSTONE, MekanismInfuseTypes.REDSTONE);
        addToTag(MekanismTags.InfuseTypes.DIAMOND, MekanismInfuseTypes.DIAMOND);
        addToTag(MekanismTags.InfuseTypes.REFINED_OBSIDIAN, MekanismInfuseTypes.REFINED_OBSIDIAN);
        addToTag(MekanismTags.InfuseTypes.GOLD, MekanismInfuseTypes.GOLD);
        addToTag(MekanismTags.InfuseTypes.TIN, MekanismInfuseTypes.TIN);
        addToTag(MekanismTags.InfuseTypes.FUNGI, MekanismInfuseTypes.FUNGI);
        addToTag(MekanismTags.InfuseTypes.BIO, MekanismInfuseTypes.BIO);
    }

    private void addHarvestRequirements() {
        addToHarvestTag(BlockTags.MINEABLE_WITH_PICKAXE,
              MekanismBlocks.BOUNDING_BLOCK,
              MekanismBlocks.BASIC_ENERGY_CUBE, MekanismBlocks.ADVANCED_ENERGY_CUBE, MekanismBlocks.ELITE_ENERGY_CUBE, MekanismBlocks.ULTIMATE_ENERGY_CUBE,
              MekanismBlocks.CREATIVE_ENERGY_CUBE,
              MekanismBlocks.BASIC_FLUID_TANK, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismBlocks.ELITE_FLUID_TANK, MekanismBlocks.ULTIMATE_FLUID_TANK,
              MekanismBlocks.CREATIVE_FLUID_TANK,
              MekanismBlocks.BASIC_CHEMICAL_TANK, MekanismBlocks.ADVANCED_CHEMICAL_TANK, MekanismBlocks.ELITE_CHEMICAL_TANK, MekanismBlocks.ULTIMATE_CHEMICAL_TANK,
              MekanismBlocks.CREATIVE_CHEMICAL_TANK,
              MekanismBlocks.BASIC_BIN, MekanismBlocks.ADVANCED_BIN, MekanismBlocks.ELITE_BIN, MekanismBlocks.ULTIMATE_BIN, MekanismBlocks.CREATIVE_BIN,
              MekanismBlocks.BRONZE_BLOCK, MekanismBlocks.REFINED_OBSIDIAN_BLOCK, MekanismBlocks.CHARCOAL_BLOCK, MekanismBlocks.REFINED_GLOWSTONE_BLOCK,
              MekanismBlocks.STEEL_BLOCK, MekanismBlocks.FLUORITE_BLOCK,
              MekanismBlocks.TELEPORTER, MekanismBlocks.TELEPORTER_FRAME,
              MekanismBlocks.STEEL_CASING,
              MekanismBlocks.STRUCTURAL_GLASS,
              MekanismBlocks.DYNAMIC_TANK, MekanismBlocks.DYNAMIC_VALVE,
              MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, MekanismBlocks.THERMAL_EVAPORATION_VALVE, MekanismBlocks.THERMAL_EVAPORATION_BLOCK,
              MekanismBlocks.INDUCTION_CASING,
              MekanismBlocks.INDUCTION_PORT,
              MekanismBlocks.BASIC_INDUCTION_CELL, MekanismBlocks.ADVANCED_INDUCTION_CELL, MekanismBlocks.ELITE_INDUCTION_CELL, MekanismBlocks.ULTIMATE_INDUCTION_CELL,
              MekanismBlocks.BASIC_INDUCTION_PROVIDER, MekanismBlocks.ADVANCED_INDUCTION_PROVIDER, MekanismBlocks.ELITE_INDUCTION_PROVIDER,
              MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER,
              MekanismBlocks.SUPERHEATING_ELEMENT, MekanismBlocks.PRESSURE_DISPERSER, MekanismBlocks.BOILER_CASING, MekanismBlocks.BOILER_VALVE,
              MekanismBlocks.SECURITY_DESK,
              MekanismBlocks.RADIOACTIVE_WASTE_BARREL,
              MekanismBlocks.ENRICHMENT_CHAMBER,
              MekanismBlocks.OSMIUM_COMPRESSOR,
              MekanismBlocks.COMBINER,
              MekanismBlocks.CRUSHER,
              MekanismBlocks.DIGITAL_MINER,
              MekanismBlocks.METALLURGIC_INFUSER,
              MekanismBlocks.PURIFICATION_CHAMBER,
              MekanismBlocks.ENERGIZED_SMELTER,
              MekanismBlocks.ELECTRIC_PUMP, MekanismBlocks.FLUIDIC_PLENISHER,
              MekanismBlocks.PERSONAL_BARREL, MekanismBlocks.PERSONAL_CHEST,
              MekanismBlocks.CHARGEPAD,
              MekanismBlocks.LOGISTICAL_SORTER,
              MekanismBlocks.ROTARY_CONDENSENTRATOR,
              MekanismBlocks.CHEMICAL_OXIDIZER,
              MekanismBlocks.CHEMICAL_INFUSER,
              MekanismBlocks.CHEMICAL_INJECTION_CHAMBER,
              MekanismBlocks.ELECTROLYTIC_SEPARATOR,
              MekanismBlocks.PRECISION_SAWMILL,
              MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER,
              MekanismBlocks.CHEMICAL_WASHER,
              MekanismBlocks.CHEMICAL_CRYSTALLIZER,
              MekanismBlocks.SEISMIC_VIBRATOR,
              MekanismBlocks.PRESSURIZED_REACTION_CHAMBER,
              MekanismBlocks.ISOTOPIC_CENTRIFUGE,
              MekanismBlocks.NUTRITIONAL_LIQUIFIER,
              MekanismBlocks.LASER, MekanismBlocks.LASER_AMPLIFIER, MekanismBlocks.LASER_TRACTOR_BEAM,
              MekanismBlocks.QUANTUM_ENTANGLOPORTER,
              MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR,
              MekanismBlocks.OREDICTIONIFICATOR,
              MekanismBlocks.FUELWOOD_HEATER, MekanismBlocks.RESISTIVE_HEATER,
              MekanismBlocks.FORMULAIC_ASSEMBLICATOR,
              MekanismBlocks.MODIFICATION_STATION,
              MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER,
              MekanismBlocks.PIGMENT_EXTRACTOR, MekanismBlocks.PIGMENT_MIXER, MekanismBlocks.PAINTING_MACHINE,
              MekanismBlocks.SPS_CASING, MekanismBlocks.SPS_PORT, MekanismBlocks.SUPERCHARGED_COIL,
              MekanismBlocks.DIMENSIONAL_STABILIZER,
              MekanismBlocks.QIO_DRIVE_ARRAY, MekanismBlocks.QIO_DASHBOARD, MekanismBlocks.QIO_IMPORTER, MekanismBlocks.QIO_EXPORTER, MekanismBlocks.QIO_REDSTONE_ADAPTER
        );
        addToHarvestTag(BlockTags.MINEABLE_WITH_PICKAXE, MekanismBlocks.getFactoryBlocks());
        addToHarvestTag(BlockTags.MINEABLE_WITH_PICKAXE,
              MekanismBlocks.PROCESSED_RESOURCE_BLOCKS
        );
        ForgeRegistryTagBuilder<Block> needsStoneToolBuilder = getBlockBuilder(BlockTags.NEEDS_STONE_TOOL);
        ForgeRegistryTagBuilder<Block> tagBuilder = getBlockBuilder(BlockTags.MINEABLE_WITH_PICKAXE);
        for (OreBlockType ore : MekanismBlocks.ORES.values()) {
            Block stone = ore.stoneBlock();
            tagBuilder.add(stone);
            hasHarvestData(stone);
            needsStoneToolBuilder.add(stone);
            Block deepslate = ore.deepslateBlock();
            tagBuilder.add(deepslate);
            hasHarvestData(deepslate);
            needsStoneToolBuilder.add(deepslate);
        }
        addToHarvestTag(BlockTags.MINEABLE_WITH_SHOVEL, MekanismBlocks.SALT_BLOCK);
        addToTag(BlockTags.NEEDS_STONE_TOOL,
              MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(PrimaryResource.OSMIUM),
              MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(BlockResourceInfo.RAW_OSMIUM),
              MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(PrimaryResource.TIN),
              MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(BlockResourceInfo.RAW_TIN),
              MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(PrimaryResource.LEAD),
              MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(BlockResourceInfo.RAW_LEAD),
              MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(PrimaryResource.URANIUM),
              MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(BlockResourceInfo.RAW_URANIUM),
              MekanismBlocks.FLUORITE_BLOCK,
              MekanismBlocks.BRONZE_BLOCK,
              MekanismBlocks.STEEL_BLOCK,
              MekanismBlocks.REFINED_GLOWSTONE_BLOCK
        );
        addToTag(BlockTags.NEEDS_DIAMOND_TOOL, MekanismBlocks.REFINED_OBSIDIAN_BLOCK);
    }
}