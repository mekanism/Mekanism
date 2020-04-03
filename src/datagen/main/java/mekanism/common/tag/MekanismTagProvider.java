package mekanism.common.tag;

import mekanism.api.chemical.gas.Gas;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.SlurryRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismInfuseTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;

public class MekanismTagProvider extends BaseTagProvider {

    public MekanismTagProvider(DataGenerator gen) {
        super(gen, Mekanism.MODID);
    }

    @Override
    protected void registerTags() {
        addBoxBlacklist();
        addToTag(MekanismTags.Items.WRENCHES, MekanismItems.CONFIGURATOR);
        addToTag(MekanismTags.Items.BATTERIES, MekanismItems.ENERGY_TABLET);
        addToTag(MekanismTags.Items.YELLOW_CAKE_URANIUM, MekanismItems.YELLOW_CAKE_URANIUM);
        addRods();
        addFuels();
        addAlloys();
        addCircuits();
        addEnriched();
        addChests();
        addOres();
        addStorageBlocks();
        addIngots();
        addNuggets();
        addDusts();
        addDirtyDusts();
        addClumps();
        addShards();
        addCrystals();
        addGems();
        addFluids();
        addSlurryTags(MekanismGases.IRON_SLURRY, MekanismGases.GOLD_SLURRY, MekanismGases.OSMIUM_SLURRY, MekanismGases.COPPER_SLURRY, MekanismGases.TIN_SLURRY);
        addInfuseTags();
        addPellets();
        getBlockBuilder(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE).add(Tags.Blocks.ORES, BlockTags.LOGS);
    }

    private void addBoxBlacklist() {
        addToTag(MekanismTags.Blocks.CARDBOARD_BLACKLIST,
              MekanismBlocks.CARDBOARD_BOX,
              MekanismBlocks.BOUNDING_BLOCK,
              MekanismBlocks.ADVANCED_BOUNDING_BLOCK,
              MekanismBlocks.SECURITY_DESK,
              MekanismBlocks.DIGITAL_MINER,
              MekanismBlocks.SEISMIC_VIBRATOR,
              MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR
        );
        getBlockBuilder(MekanismTags.Blocks.CARDBOARD_BLACKLIST).add(
              Blocks.WHITE_BED,
              Blocks.ORANGE_BED,
              Blocks.MAGENTA_BED,
              Blocks.LIGHT_BLUE_BED,
              Blocks.YELLOW_BED,
              Blocks.LIME_BED,
              Blocks.PINK_BED,
              Blocks.GRAY_BED,
              Blocks.LIGHT_GRAY_BED,
              Blocks.CYAN_BED,
              Blocks.PURPLE_BED,
              Blocks.BLUE_BED,
              Blocks.BROWN_BED,
              Blocks.GREEN_BED,
              Blocks.RED_BED,
              Blocks.BLACK_BED,
              Blocks.OAK_DOOR,
              Blocks.SPRUCE_DOOR,
              Blocks.BIRCH_DOOR,
              Blocks.JUNGLE_DOOR,
              Blocks.ACACIA_DOOR,
              Blocks.DARK_OAK_DOOR,
              Blocks.IRON_DOOR
        );
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
        //Alloys Tags that go in the forge domain
        addToTag(MekanismTags.Items.ALLOYS_ADVANCED, MekanismItems.INFUSED_ALLOY);
        addToTag(MekanismTags.Items.ALLOYS_ELITE, MekanismItems.REINFORCED_ALLOY);
        addToTag(MekanismTags.Items.ALLOYS_ULTIMATE, MekanismItems.ATOMIC_ALLOY);
        getItemBuilder(MekanismTags.Items.FORGE_ALLOYS).add(MekanismTags.Items.ALLOYS_ADVANCED, MekanismTags.Items.ALLOYS_ELITE, MekanismTags.Items.ALLOYS_ULTIMATE);
        //Alloy tags that go in our domain
        getItemBuilder(MekanismTags.Items.ALLOYS_BASIC).add(Items.REDSTONE);
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

    private void addEnriched() {
        addToTag(MekanismTags.Items.ENRICHED_CARBON, MekanismItems.ENRICHED_CARBON);
        addToTag(MekanismTags.Items.ENRICHED_DIAMOND, MekanismItems.ENRICHED_DIAMOND);
        addToTag(MekanismTags.Items.ENRICHED_OBSIDIAN, MekanismItems.ENRICHED_OBSIDIAN);
        addToTag(MekanismTags.Items.ENRICHED_REDSTONE, MekanismItems.ENRICHED_REDSTONE);
        addToTag(MekanismTags.Items.ENRICHED_TIN, MekanismItems.ENRICHED_TIN);
        getItemBuilder(MekanismTags.Items.ENRICHED).add(MekanismTags.Items.ENRICHED_CARBON, MekanismTags.Items.ENRICHED_DIAMOND, MekanismTags.Items.ENRICHED_OBSIDIAN,
              MekanismTags.Items.ENRICHED_REDSTONE, MekanismTags.Items.ENRICHED_TIN);
    }

    private void addChests() {
        addToTags(MekanismTags.Items.CHESTS_ELECTRIC, MekanismTags.Blocks.CHESTS_ELECTRIC, MekanismBlocks.PERSONAL_CHEST);
        addToTags(MekanismTags.Items.CHESTS_PERSONAL, MekanismTags.Blocks.CHESTS_PERSONAL, MekanismBlocks.PERSONAL_CHEST);
        getItemBuilder(Tags.Items.CHESTS).add(MekanismTags.Items.CHESTS_ELECTRIC, MekanismTags.Items.CHESTS_PERSONAL);
        getBlockBuilder(Tags.Blocks.CHESTS).add(MekanismTags.Blocks.CHESTS_ELECTRIC, MekanismTags.Blocks.CHESTS_PERSONAL);
    }

    private void addOres() {
        addToTags(MekanismTags.Items.ORES_COPPER, MekanismTags.Blocks.ORES_COPPER, MekanismBlocks.COPPER_ORE);
        addToTags(MekanismTags.Items.ORES_OSMIUM, MekanismTags.Blocks.ORES_OSMIUM, MekanismBlocks.OSMIUM_ORE);
        addToTags(MekanismTags.Items.ORES_TIN, MekanismTags.Blocks.ORES_TIN, MekanismBlocks.TIN_ORE);
        addToTags(MekanismTags.Items.ORES_FLUORITE, MekanismTags.Blocks.ORES_FLUORITE, MekanismBlocks.FLUORITE_ORE);
        addToTags(MekanismTags.Items.ORES_URANIUM, MekanismTags.Blocks.ORES_URANIUM, MekanismBlocks.URANIUM_ORE);
        getItemBuilder(Tags.Items.ORES).add(MekanismTags.Items.ORES_COPPER, MekanismTags.Items.ORES_OSMIUM, MekanismTags.Items.ORES_TIN, MekanismTags.Items.ORES_FLUORITE, MekanismTags.Items.ORES_URANIUM);
        getBlockBuilder(Tags.Blocks.ORES).add(MekanismTags.Blocks.ORES_COPPER, MekanismTags.Blocks.ORES_OSMIUM, MekanismTags.Blocks.ORES_TIN, MekanismTags.Blocks.ORES_FLUORITE, MekanismTags.Blocks.ORES_URANIUM);
    }

    private void addStorageBlocks() {
        addToTags(MekanismTags.Items.STORAGE_BLOCKS_BRONZE, MekanismTags.Blocks.STORAGE_BLOCKS_BRONZE, MekanismBlocks.BRONZE_BLOCK);
        addToTags(MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL, MekanismTags.Blocks.STORAGE_BLOCKS_CHARCOAL, MekanismBlocks.CHARCOAL_BLOCK);
        addToTags(MekanismTags.Items.STORAGE_BLOCKS_COPPER, MekanismTags.Blocks.STORAGE_BLOCKS_COPPER, MekanismBlocks.COPPER_BLOCK);
        addToTags(MekanismTags.Items.STORAGE_BLOCKS_OSMIUM, MekanismTags.Blocks.STORAGE_BLOCKS_OSMIUM, MekanismBlocks.OSMIUM_BLOCK);
        addToTags(MekanismTags.Items.STORAGE_BLOCKS_REFINED_GLOWSTONE, MekanismTags.Blocks.STORAGE_BLOCKS_REFINED_GLOWSTONE, MekanismBlocks.REFINED_GLOWSTONE_BLOCK);
        addToTags(MekanismTags.Items.STORAGE_BLOCKS_REFINED_OBSIDIAN, MekanismTags.Blocks.STORAGE_BLOCKS_REFINED_OBSIDIAN, MekanismBlocks.REFINED_OBSIDIAN_BLOCK);
        addToTags(MekanismTags.Items.STORAGE_BLOCKS_STEEL, MekanismTags.Blocks.STORAGE_BLOCKS_STEEL, MekanismBlocks.STEEL_BLOCK);
        addToTags(MekanismTags.Items.STORAGE_BLOCKS_TIN, MekanismTags.Blocks.STORAGE_BLOCKS_TIN, MekanismBlocks.TIN_BLOCK);
        getItemBuilder(Tags.Items.STORAGE_BLOCKS).add(MekanismTags.Items.STORAGE_BLOCKS_BRONZE, MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL,
              MekanismTags.Items.STORAGE_BLOCKS_COPPER, MekanismTags.Items.STORAGE_BLOCKS_OSMIUM, MekanismTags.Items.STORAGE_BLOCKS_REFINED_GLOWSTONE,
              MekanismTags.Items.STORAGE_BLOCKS_REFINED_OBSIDIAN, MekanismTags.Items.STORAGE_BLOCKS_STEEL, MekanismTags.Items.STORAGE_BLOCKS_TIN);
        getBlockBuilder(Tags.Blocks.STORAGE_BLOCKS).add(MekanismTags.Blocks.STORAGE_BLOCKS_BRONZE, MekanismTags.Blocks.STORAGE_BLOCKS_CHARCOAL,
              MekanismTags.Blocks.STORAGE_BLOCKS_COPPER, MekanismTags.Blocks.STORAGE_BLOCKS_OSMIUM, MekanismTags.Blocks.STORAGE_BLOCKS_REFINED_GLOWSTONE,
              MekanismTags.Blocks.STORAGE_BLOCKS_REFINED_OBSIDIAN, MekanismTags.Blocks.STORAGE_BLOCKS_STEEL, MekanismTags.Blocks.STORAGE_BLOCKS_TIN);
    }

    private void addIngots() {
        addToTag(MekanismTags.Items.INGOTS_BRONZE, MekanismItems.BRONZE_INGOT);
        addToTag(MekanismTags.Items.INGOTS_COPPER, MekanismItems.COPPER_INGOT);
        addToTag(MekanismTags.Items.INGOTS_OSMIUM, MekanismItems.OSMIUM_INGOT);
        addToTag(MekanismTags.Items.INGOTS_REFINED_GLOWSTONE, MekanismItems.REFINED_GLOWSTONE_INGOT);
        addToTag(MekanismTags.Items.INGOTS_REFINED_OBSIDIAN, MekanismItems.REFINED_OBSIDIAN_INGOT);
        addToTag(MekanismTags.Items.INGOTS_STEEL, MekanismItems.STEEL_INGOT);
        addToTag(MekanismTags.Items.INGOTS_TIN, MekanismItems.TIN_INGOT);
        getItemBuilder(Tags.Items.INGOTS).add(MekanismTags.Items.INGOTS_BRONZE, MekanismTags.Items.INGOTS_COPPER, MekanismTags.Items.INGOTS_OSMIUM,
              MekanismTags.Items.INGOTS_REFINED_GLOWSTONE, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN, MekanismTags.Items.INGOTS_STEEL, MekanismTags.Items.INGOTS_TIN);
    }

    private void addNuggets() {
        addToTag(MekanismTags.Items.NUGGETS_BRONZE, MekanismItems.BRONZE_NUGGET);
        addToTag(MekanismTags.Items.NUGGETS_COPPER, MekanismItems.COPPER_NUGGET);
        addToTag(MekanismTags.Items.NUGGETS_OSMIUM, MekanismItems.OSMIUM_NUGGET);
        addToTag(MekanismTags.Items.NUGGETS_REFINED_GLOWSTONE, MekanismItems.REFINED_GLOWSTONE_NUGGET);
        addToTag(MekanismTags.Items.NUGGETS_REFINED_OBSIDIAN, MekanismItems.REFINED_OBSIDIAN_NUGGET);
        addToTag(MekanismTags.Items.NUGGETS_STEEL, MekanismItems.STEEL_NUGGET);
        addToTag(MekanismTags.Items.NUGGETS_TIN, MekanismItems.TIN_NUGGET);
        getItemBuilder(Tags.Items.NUGGETS).add(MekanismTags.Items.NUGGETS_BRONZE, MekanismTags.Items.NUGGETS_COPPER, MekanismTags.Items.NUGGETS_OSMIUM,
              MekanismTags.Items.NUGGETS_REFINED_GLOWSTONE, MekanismTags.Items.NUGGETS_REFINED_OBSIDIAN, MekanismTags.Items.NUGGETS_STEEL, MekanismTags.Items.NUGGETS_TIN);
    }

    private void addDusts() {
        addToTag(MekanismTags.Items.DUSTS_BRONZE, MekanismItems.BRONZE_DUST);
        addToTag(MekanismTags.Items.DUSTS_CHARCOAL, MekanismItems.CHARCOAL_DUST);
        addToTag(MekanismTags.Items.DUSTS_COAL, MekanismItems.COAL_DUST);
        addToTag(MekanismTags.Items.DUSTS_COPPER, MekanismItems.COPPER_DUST);
        addToTag(MekanismTags.Items.DUSTS_DIAMOND, MekanismItems.DIAMOND_DUST);
        addToTag(MekanismTags.Items.DUSTS_EMERALD, MekanismItems.EMERALD_DUST);
        addToTag(MekanismTags.Items.DUSTS_GOLD, MekanismItems.GOLD_DUST);
        addToTag(MekanismTags.Items.DUSTS_IRON, MekanismItems.IRON_DUST);
        addToTag(MekanismTags.Items.DUSTS_LAPIS_LAZULI, MekanismItems.LAPIS_LAZULI_DUST);
        addToTag(MekanismTags.Items.DUSTS_LITHIUM, MekanismItems.LITHIUM_DUST);
        addToTag(MekanismTags.Items.DUSTS_OBSIDIAN, MekanismItems.OBSIDIAN_DUST);
        addToTag(MekanismTags.Items.DUSTS_OSMIUM, MekanismItems.OSMIUM_DUST);
        addToTag(MekanismTags.Items.DUSTS_QUARTZ, MekanismItems.QUARTZ_DUST);
        addToTag(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN, MekanismItems.REFINED_OBSIDIAN_DUST);
        addToTag(MekanismTags.Items.DUSTS_SALT, MekanismItems.SALT);
        addToTag(MekanismTags.Items.DUSTS_STEEL, MekanismItems.STEEL_DUST);
        addToTag(MekanismTags.Items.DUSTS_SULFUR, MekanismItems.SULFUR_DUST);
        addToTag(MekanismTags.Items.DUSTS_TIN, MekanismItems.TIN_DUST);
        addToTag(MekanismTags.Items.DUSTS_WOOD, MekanismItems.SAWDUST);
        addToTag(MekanismTags.Items.DUSTS_FLUORITE, MekanismItems.FLUORITE_DUST);
        getItemBuilder(Tags.Items.DUSTS).add(MekanismTags.Items.DUSTS_BRONZE, MekanismTags.Items.DUSTS_CHARCOAL, MekanismTags.Items.DUSTS_COAL,
              MekanismTags.Items.DUSTS_COPPER, MekanismTags.Items.DUSTS_DIAMOND, MekanismTags.Items.DUSTS_EMERALD, MekanismTags.Items.DUSTS_GOLD,
              MekanismTags.Items.DUSTS_IRON, MekanismTags.Items.DUSTS_LAPIS_LAZULI, MekanismTags.Items.DUSTS_LITHIUM, MekanismTags.Items.DUSTS_OBSIDIAN,
              MekanismTags.Items.DUSTS_OSMIUM, MekanismTags.Items.DUSTS_QUARTZ, MekanismTags.Items.DUSTS_REFINED_OBSIDIAN, MekanismTags.Items.DUSTS_SALT,
              MekanismTags.Items.DUSTS_STEEL, MekanismTags.Items.DUSTS_SULFUR, MekanismTags.Items.DUSTS_TIN, MekanismTags.Items.DUSTS_WOOD,
              MekanismTags.Items.DUSTS_FLUORITE);

        getItemBuilder(MekanismTags.Items.SALT).add(MekanismTags.Items.DUSTS_SALT);
        getItemBuilder(MekanismTags.Items.SAWDUST).add(MekanismTags.Items.DUSTS_WOOD);
    }

    private void addDirtyDusts() {
        addToTag(MekanismTags.Items.DIRTY_DUSTS_COPPER, MekanismItems.DIRTY_COPPER_DUST);
        addToTag(MekanismTags.Items.DIRTY_DUSTS_GOLD, MekanismItems.DIRTY_GOLD_DUST);
        addToTag(MekanismTags.Items.DIRTY_DUSTS_IRON, MekanismItems.DIRTY_IRON_DUST);
        addToTag(MekanismTags.Items.DIRTY_DUSTS_OSMIUM, MekanismItems.DIRTY_OSMIUM_DUST);
        addToTag(MekanismTags.Items.DIRTY_DUSTS_TIN, MekanismItems.DIRTY_TIN_DUST);
        getItemBuilder(MekanismTags.Items.DIRTY_DUSTS).add(MekanismTags.Items.DIRTY_DUSTS_COPPER, MekanismTags.Items.DIRTY_DUSTS_GOLD, MekanismTags.Items.DIRTY_DUSTS_IRON,
              MekanismTags.Items.DIRTY_DUSTS_OSMIUM, MekanismTags.Items.DIRTY_DUSTS_TIN);
    }

    private void addClumps() {
        addToTag(MekanismTags.Items.CLUMPS_COPPER, MekanismItems.COPPER_CLUMP);
        addToTag(MekanismTags.Items.CLUMPS_GOLD, MekanismItems.GOLD_CLUMP);
        addToTag(MekanismTags.Items.CLUMPS_IRON, MekanismItems.IRON_CLUMP);
        addToTag(MekanismTags.Items.CLUMPS_OSMIUM, MekanismItems.OSMIUM_CLUMP);
        addToTag(MekanismTags.Items.CLUMPS_TIN, MekanismItems.TIN_CLUMP);
        getItemBuilder(MekanismTags.Items.CLUMPS).add(MekanismTags.Items.CLUMPS_COPPER, MekanismTags.Items.CLUMPS_GOLD, MekanismTags.Items.CLUMPS_IRON,
              MekanismTags.Items.CLUMPS_OSMIUM, MekanismTags.Items.CLUMPS_TIN);
    }

    private void addShards() {
        addToTag(MekanismTags.Items.SHARDS_COPPER, MekanismItems.COPPER_SHARD);
        addToTag(MekanismTags.Items.SHARDS_GOLD, MekanismItems.GOLD_SHARD);
        addToTag(MekanismTags.Items.SHARDS_IRON, MekanismItems.IRON_SHARD);
        addToTag(MekanismTags.Items.SHARDS_OSMIUM, MekanismItems.OSMIUM_SHARD);
        addToTag(MekanismTags.Items.SHARDS_TIN, MekanismItems.TIN_SHARD);
        getItemBuilder(MekanismTags.Items.SHARDS).add(MekanismTags.Items.SHARDS_COPPER, MekanismTags.Items.SHARDS_GOLD, MekanismTags.Items.SHARDS_IRON,
              MekanismTags.Items.SHARDS_OSMIUM, MekanismTags.Items.SHARDS_TIN);
    }

    private void addCrystals() {
        addToTag(MekanismTags.Items.CRYSTALS_COPPER, MekanismItems.COPPER_CRYSTAL);
        addToTag(MekanismTags.Items.CRYSTALS_GOLD, MekanismItems.GOLD_CRYSTAL);
        addToTag(MekanismTags.Items.CRYSTALS_IRON, MekanismItems.IRON_CRYSTAL);
        addToTag(MekanismTags.Items.CRYSTALS_OSMIUM, MekanismItems.OSMIUM_CRYSTAL);
        addToTag(MekanismTags.Items.CRYSTALS_TIN, MekanismItems.TIN_CRYSTAL);
        getItemBuilder(MekanismTags.Items.CRYSTALS).add(MekanismTags.Items.CRYSTALS_COPPER, MekanismTags.Items.CRYSTALS_GOLD, MekanismTags.Items.CRYSTALS_IRON,
              MekanismTags.Items.CRYSTALS_OSMIUM, MekanismTags.Items.CRYSTALS_TIN);
    }

    private void addGems() {
        addToTag(MekanismTags.Items.GEMS_FLUORITE, MekanismItems.FLUORITE_GEM);
    }

    private void addPellets() {
        addToTag(MekanismTags.Items.PELLETS_FISSILE_FUEL, MekanismItems.FISSILE_FUEL_PELLET);
        addToTag(MekanismTags.Items.PELLETS_ANTIMATTER, MekanismItems.ANTIMATTER_PELLET);
        addToTag(MekanismTags.Items.PELLETS_PLUTONIUM, MekanismItems.PLUTONIUM_PELLET);
        addToTag(MekanismTags.Items.PELLETS_POLONIUM, MekanismItems.POLONIUM_PELLET);
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
        addToTag(MekanismTags.Fluids.STEAM, MekanismFluids.STEAM);
        addToTag(MekanismTags.Fluids.SULFUR_DIOXIDE, MekanismFluids.SULFUR_DIOXIDE);
        addToTag(MekanismTags.Fluids.SULFUR_TRIOXIDE, MekanismFluids.SULFUR_TRIOXIDE);
        addToTag(MekanismTags.Fluids.SULFURIC_ACID, MekanismFluids.SULFURIC_ACID);
        addToTag(MekanismTags.Fluids.HYDROFLUORIC_ACID, MekanismFluids.HYDROFLUORIC_ACID);
    }

    private void addSlurryTags(SlurryRegistryObject<?, ?>... slurryRegistryObjects) {
        Tag.Builder<Gas> dirtyTagBuilder = getGasBuilder(MekanismTags.Gases.DIRTY_SLURRY);
        Tag.Builder<Gas> cleanTagBuilder = getGasBuilder(MekanismTags.Gases.CLEAN_SLURRY);
        for (SlurryRegistryObject<?, ?> slurryRO : slurryRegistryObjects) {
            dirtyTagBuilder.add(slurryRO.getDirtySlurry());
            cleanTagBuilder.add(slurryRO.getCleanSlurry());
        }
    }

    private void addInfuseTags() {
        addToTag(MekanismTags.InfuseTypes.CARBON, MekanismInfuseTypes.CARBON);
        addToTag(MekanismTags.InfuseTypes.REDSTONE, MekanismInfuseTypes.REDSTONE);
        addToTag(MekanismTags.InfuseTypes.DIAMOND, MekanismInfuseTypes.DIAMOND);
        addToTag(MekanismTags.InfuseTypes.REFINED_OBSIDIAN, MekanismInfuseTypes.REFINED_OBSIDIAN);
        addToTag(MekanismTags.InfuseTypes.TIN, MekanismInfuseTypes.TIN);
        addToTag(MekanismTags.InfuseTypes.FUNGI, MekanismInfuseTypes.FUNGI);
        addToTag(MekanismTags.InfuseTypes.BIO, MekanismInfuseTypes.BIO);
    }
}