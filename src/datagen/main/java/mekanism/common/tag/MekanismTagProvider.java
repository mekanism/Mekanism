package mekanism.common.tag;

import com.google.common.collect.Table.Cell;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.Chemical;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.IModuleItem;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registration.impl.SlurryRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGameEvents;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.resource.IResource;
import mekanism.common.resource.MiscResource;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

public class MekanismTagProvider extends BaseTagProvider {

    public MekanismTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Mekanism.MODID, existingFileHelper);
    }

    @Override
    protected Collection<? extends DeferredHolder<Block, ?>> getAllBlocks() {
        return MekanismBlocks.BLOCKS.getPrimaryEntries();
    }

    @Override
    protected void registerTags(HolderLookup.Provider registries) {
        addProcessedResources();
        addBeaconTags();
        addBoxBlacklist();
        addTools();
        addArmor();
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
        addBiomes();
        addDamageTypes();
        addFluids();
        addGameEvents();
        addChemicalTags();
        addPellets();
        addColorableItems();
        getBlockBuilder(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE).add(Tags.Blocks.ORES, BlockTags.LOGS);
        getBlockBuilder(MekanismTags.Blocks.INCORRECT_FOR_DISASSEMBLER);
        getBlockBuilder(MekanismTags.Blocks.INCORRECT_FOR_MEKA_TOOL);
        addBlocksToTag(BlockTags.GUARDED_BY_PIGLINS, MekanismBlocks.REFINED_GLOWSTONE_BLOCK, MekanismBlocks.PERSONAL_BARREL, MekanismBlocks.PERSONAL_CHEST);
        addBlocksToTag(BlockTags.HOGLIN_REPELLENTS, MekanismBlocks.TELEPORTER, MekanismBlocks.QUANTUM_ENTANGLOPORTER);
        getItemBuilder(ItemTags.PIGLIN_LOVED).addHolders(
              MekanismBlocks.REFINED_GLOWSTONE_BLOCK.getItemHolder(),
              MekanismItems.REFINED_GLOWSTONE_INGOT,
              MekanismItems.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.GOLD)
        ).add(
              MekanismTags.Items.ENRICHED_GOLD,
              MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.SHARD, PrimaryResource.GOLD),
              MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.CRYSTAL, PrimaryResource.GOLD),
              MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DIRTY_DUST, PrimaryResource.GOLD),
              MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.CLUMP, PrimaryResource.GOLD)
        );
        addItemsToTag(MekanismTags.Items.MEKASUIT_HUD_RENDERER, MekanismItems.MEKASUIT_HELMET);
        getItemBuilder(MekanismTags.Items.STONE_CRAFTING_MATERIALS).add(ItemTags.STONE_CRAFTING_MATERIALS, Tags.Items.COBBLESTONES_NORMAL);
        getItemBuilder(MekanismTags.Items.MUFFLING_CENTER).add(
              Tags.Items.BRICKS,
              Tags.Items.INGOTS_IRON,
              Tags.Items.INGOTS_GOLD,
              Tags.Items.INGOTS_COPPER,
              MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM),
              MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN),
              MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD),
              MekanismTags.Items.GEMS_FLUORITE
        );
        addEntities();
        getBlockBuilder(MekanismTags.Blocks.MINER_BLACKLIST);
        addHarvestRequirements();
        addBlocksToTag(BlockTags.IMPERMEABLE, MekanismBlocks.STRUCTURAL_GLASS);
        //Note: Axolotls live in a brackish water (mix between fresh and salt), so it is reasonable there may be salt nearby
        addBlocksToTag(BlockTags.AXOLOTLS_SPAWNABLE_ON, MekanismBlocks.SALT_BLOCK);
        addItemsToTag(ItemTags.CLUSTER_MAX_HARVESTABLES, MekanismItems.ATOMIC_DISASSEMBLER, MekanismItems.MEKA_TOOL);
        addItemsToTag(ItemTags.FREEZE_IMMUNE_WEARABLES, MekanismItems.MEKASUIT_HELMET, MekanismItems.MEKASUIT_BODYARMOR, MekanismItems.MEKASUIT_PANTS, MekanismItems.MEKASUIT_BOOTS);
        addBlocksToTag(BlockTags.SCULK_REPLACEABLE, MekanismBlocks.SALT_BLOCK);
        getMobEffectBuilder(MekanismAPITags.MobEffects.SPEED_UP_BLACKLIST);

        getBlockBuilder(MekanismTags.Blocks.FARMING_OVERRIDE).add(
              Blocks.PINK_PETALS
        );
        addBlocksToTag(BlockTags.CAMEL_SAND_STEP_SOUND_BLOCKS, MekanismBlocks.SALT_BLOCK);

        addToTags(Tags.Items.HIDDEN_FROM_RECIPE_VIEWERS, Tags.Blocks.HIDDEN_FROM_RECIPE_VIEWERS, MekanismBlocks.BOUNDING_BLOCK);

        addBlocksToTag(BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON,
              MekanismBlocks.STRUCTURAL_GLASS,

              MekanismBlocks.BOILER_CASING,
              MekanismBlocks.BOILER_VALVE,
              MekanismBlocks.PRESSURE_DISPERSER,
              MekanismBlocks.SUPERHEATING_ELEMENT,

              MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER,
              MekanismBlocks.THERMAL_EVAPORATION_BLOCK,
              MekanismBlocks.THERMAL_EVAPORATION_VALVE,

              MekanismBlocks.INDUCTION_CASING,
              MekanismBlocks.INDUCTION_PORT,
              MekanismBlocks.BASIC_INDUCTION_CELL,
              MekanismBlocks.BASIC_INDUCTION_PROVIDER,
              MekanismBlocks.ADVANCED_INDUCTION_CELL,
              MekanismBlocks.ADVANCED_INDUCTION_PROVIDER,
              MekanismBlocks.ELITE_INDUCTION_CELL,
              MekanismBlocks.ELITE_INDUCTION_PROVIDER,
              MekanismBlocks.ULTIMATE_INDUCTION_CELL,
              MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER,

              MekanismBlocks.SPS_CASING,
              MekanismBlocks.SPS_PORT,
              MekanismBlocks.SUPERCHARGED_COIL,

              MekanismBlocks.DYNAMIC_TANK,
              MekanismBlocks.DYNAMIC_VALVE
        );

        addBlocksToTag(FRAMEABLE, MekanismBlocks.STRUCTURAL_GLASS);
        addBlocksToTag(FB_BE_WHITELIST, MekanismBlocks.STRUCTURAL_GLASS);

        getItemBuilder(MekanismAPITags.Items.MEKA_UNITS).add(MekanismItems.ITEMS.getEntries().stream().filter(item -> item.get() instanceof IModuleItem).toList());
    }

    private void addEntities() {
        addEntitiesToTag(EntityTypeTags.IMPACT_PROJECTILES, MekanismEntityTypes.FLAME);
        addEntitiesToTag(PVI_COMPAT, MekanismEntityTypes.ROBIT);

        addEntitiesToTag(MekanismAPITags.Entities.RADIATION_IMMUNE, MekanismEntityTypes.ROBIT);
        getEntityTypeBuilder(MekanismAPITags.Entities.MEK_RADIATION_IMMUNE).add(MekanismAPITags.Entities.RADIATION_IMMUNE).add(
              EntityType.IRON_GOLEM,
              EntityType.SNOW_GOLEM
        );

        addEntitiesToTag(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS, MekanismEntityTypes.ROBIT);
        addEntitiesToTag(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES, MekanismEntityTypes.ROBIT);
        addEntitiesToTag(EntityTypeTags.CAN_BREATHE_UNDER_WATER, MekanismEntityTypes.ROBIT);
        addEntitiesToTag(EntityTypeTags.IGNORES_POISON_AND_REGEN, MekanismEntityTypes.ROBIT);
        addEntitiesToTag(EntityTypeTags.IMMUNE_TO_INFESTED, MekanismEntityTypes.ROBIT);
        addEntitiesToTag(EntityTypeTags.IMMUNE_TO_OOZING, MekanismEntityTypes.ROBIT);
        addEntitiesToTag(EntityTypeTags.FALL_DAMAGE_IMMUNE, MekanismEntityTypes.ROBIT);
        //Robit's don't need to breathe
        addEntitiesToTag(EntityTypeTags.CAN_BREATHE_UNDER_WATER, MekanismEntityTypes.ROBIT);
        //Robit's are not scary, they are friends!
        addEntitiesToTag(EntityTypeTags.NOT_SCARY_FOR_PUFFERFISH, MekanismEntityTypes.ROBIT);
        addEntitiesToTag(EntityTypeTags.ILLAGER_FRIENDS, MekanismEntityTypes.ROBIT);
        addEntitiesToTag(EntityTypeTags.WITHER_FRIENDS, MekanismEntityTypes.ROBIT);
    }

    private void addProcessedResources() {
        for (Cell<ResourceType, PrimaryResource, ? extends Holder<Item>> item : MekanismItems.PROCESSED_RESOURCES.cellSet()) {
            TagKey<Item> tag = MekanismTags.Items.PROCESSED_RESOURCES.get(item.getRowKey(), item.getColumnKey());
            addItemsToTag(tag, item.getValue());
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

    private void addBeaconTags() {
        //Beacon bases
        addBlocksToTag(BlockTags.BEACON_BASE_BLOCKS,
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
        addItemsToTag(ItemTags.BEACON_PAYMENT_ITEMS,
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
        addBlocksToTag(Tags.Blocks.RELOCATION_NOT_SUPPORTED,
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
              MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE,
              //Don't allow other transmitters that have a buffer either due to dupe bugs
              //TODO: Maybe some better way of doing this can be thought of? But there isn't a great way to make it so transmitters push their contents
              // into remaining network when removed except for when they are removed by a mod that saved their contents first
              // In theory one solution might be to save the contents of the network on the network level but that would introduce other issues
              MekanismBlocks.BASIC_MECHANICAL_PIPE,
              MekanismBlocks.ADVANCED_MECHANICAL_PIPE,
              MekanismBlocks.ELITE_MECHANICAL_PIPE,
              MekanismBlocks.ULTIMATE_MECHANICAL_PIPE,
              MekanismBlocks.BASIC_UNIVERSAL_CABLE,
              MekanismBlocks.ADVANCED_UNIVERSAL_CABLE,
              MekanismBlocks.ELITE_UNIVERSAL_CABLE,
              MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE
        );
        getBlockBuilder(MekanismTags.Blocks.CARDBOARD_BLACKLIST)
              .add(Tags.Blocks.RELOCATION_NOT_SUPPORTED, BlockTags.BEDS, BlockTags.DOORS)
              .add(Blocks.TRIAL_SPAWNER, Blocks.VAULT);
    }

    private void addTools() {
        addWrenches();
        addItemsToTag(ItemTags.BREAKS_DECORATED_POTS, MekanismItems.ATOMIC_DISASSEMBLER, MekanismItems.MEKA_TOOL);
        addItemsToTag(Tags.Items.MINING_TOOL_TOOLS, MekanismItems.ATOMIC_DISASSEMBLER, MekanismItems.MEKA_TOOL);
        addItemsToTag(Tags.Items.TOOLS_BOW, MekanismItems.ELECTRIC_BOW);
        addItemsToTag(Tags.Items.RANGED_WEAPON_TOOLS, MekanismItems.ELECTRIC_BOW);
        addItemsToTag(ItemTags.BOW_ENCHANTABLE, MekanismItems.ELECTRIC_BOW);
        addItemsToTag(ItemTags.DURABILITY_ENCHANTABLE, MekanismItems.HDPE_REINFORCED_ELYTRA);
        addItemsToTag(ItemTags.EQUIPPABLE_ENCHANTABLE, MekanismItems.HDPE_REINFORCED_ELYTRA);
    }

    private void addWrenches() {
        //Note: We don't add wrenches to the vanilla tools tag as that is for a different style of tool and used for things like breaking pots
        addItemsToTag(Tags.Items.TOOLS_WRENCH, MekanismItems.CONFIGURATOR);
        getItemBuilder(MekanismTags.Items.CONFIGURATORS).add(Tags.Items.TOOLS_WRENCH);
    }

    private void addArmor() {
        addItemsToTag(ItemTags.HEAD_ARMOR, MekanismItems.HAZMAT_MASK, MekanismItems.MEKASUIT_HELMET);
        addItemsToTag(ItemTags.CHEST_ARMOR, MekanismItems.HAZMAT_GOWN, MekanismItems.MEKASUIT_BODYARMOR);
        addItemsToTag(ItemTags.LEG_ARMOR, MekanismItems.HAZMAT_PANTS, MekanismItems.MEKASUIT_PANTS);
        addItemsToTag(ItemTags.FOOT_ARMOR, MekanismItems.HAZMAT_BOOTS, MekanismItems.MEKASUIT_BOOTS);
        getItemBuilder(ItemTags.TRIMMABLE_ARMOR).remove(
              MekanismItems.HAZMAT_MASK,
              MekanismItems.HAZMAT_GOWN,
              MekanismItems.HAZMAT_PANTS,
              MekanismItems.HAZMAT_BOOTS,
              MekanismItems.MEKASUIT_HELMET,
              MekanismItems.MEKASUIT_BODYARMOR,
              MekanismItems.MEKASUIT_PANTS,
              MekanismItems.MEKASUIT_BOOTS
        );
        ItemRegistryObject<?>[] providers = {
              MekanismItems.MEKASUIT_HELMET,
              MekanismItems.MEKASUIT_BODYARMOR,
              MekanismItems.MEKASUIT_PANTS,
              MekanismItems.MEKASUIT_BOOTS
        };
        getItemBuilder(ItemTags.DURABILITY_ENCHANTABLE).remove(providers);
        getItemBuilder(ItemTags.EQUIPPABLE_ENCHANTABLE).remove(providers);
        getItemBuilder(ItemTags.HEAD_ARMOR_ENCHANTABLE).remove(MekanismItems.MEKASUIT_HELMET);
        getItemBuilder(ItemTags.CHEST_ARMOR_ENCHANTABLE).remove(MekanismItems.MEKASUIT_BODYARMOR);
        getItemBuilder(ItemTags.LEG_ARMOR_ENCHANTABLE).remove(MekanismItems.MEKASUIT_PANTS);
        getItemBuilder(ItemTags.FOOT_ARMOR_ENCHANTABLE).remove(MekanismItems.MEKASUIT_BOOTS);
    }

    private void addRods() {
        addItemsToTag(MekanismTags.Items.RODS_PLASTIC, MekanismItems.HDPE_STICK);
        getItemBuilder(Tags.Items.RODS).add(MekanismTags.Items.RODS_PLASTIC);
    }

    private void addFuels() {
        addItemsToTag(MekanismTags.Items.FUELS_BIO, MekanismItems.BIO_FUEL);
        addItemsToTag(MekanismTags.Items.FUELS_BLOCK_BIO, MekanismBlocks.BIO_FUEL_BLOCK.getItemHolder());
        getItemBuilder(MekanismTags.Items.FUELS).add(MekanismTags.Items.FUELS_BIO, MekanismTags.Items.FUELS_BLOCK_BIO);
    }

    private void addAlloys() {
        //Alloy Tags that go in the forge domain
        addItemsToTag(MekanismTags.Items.ALLOYS_ADVANCED, MekanismItems.INFUSED_ALLOY);
        addItemsToTag(MekanismTags.Items.ALLOYS_ELITE, MekanismItems.REINFORCED_ALLOY);
        addItemsToTag(MekanismTags.Items.ALLOYS_ULTIMATE, MekanismItems.ATOMIC_ALLOY);
        getItemBuilder(MekanismTags.Items.COMMON_ALLOYS).add(MekanismTags.Items.ALLOYS_ADVANCED, MekanismTags.Items.ALLOYS_ELITE, MekanismTags.Items.ALLOYS_ULTIMATE);
        //Alloy tags that go in our domain
        getItemBuilder(MekanismTags.Items.ALLOYS_BASIC).add(Items.REDSTONE);
        getItemBuilder(MekanismTags.Items.ALLOYS_INFUSED).add(MekanismTags.Items.ALLOYS_ADVANCED);
        getItemBuilder(MekanismTags.Items.ALLOYS_REINFORCED).add(MekanismTags.Items.ALLOYS_ELITE);
        getItemBuilder(MekanismTags.Items.ALLOYS_ATOMIC).add(MekanismTags.Items.ALLOYS_ULTIMATE);
        getItemBuilder(MekanismTags.Items.ALLOYS).add(MekanismTags.Items.ALLOYS_BASIC, MekanismTags.Items.ALLOYS_INFUSED, MekanismTags.Items.ALLOYS_REINFORCED,
              MekanismTags.Items.ALLOYS_ATOMIC);
    }

    private void addCircuits() {
        addItemsToTag(MekanismTags.Items.CIRCUITS_BASIC, MekanismItems.BASIC_CONTROL_CIRCUIT);
        addItemsToTag(MekanismTags.Items.CIRCUITS_ADVANCED, MekanismItems.ADVANCED_CONTROL_CIRCUIT);
        addItemsToTag(MekanismTags.Items.CIRCUITS_ELITE, MekanismItems.ELITE_CONTROL_CIRCUIT);
        addItemsToTag(MekanismTags.Items.CIRCUITS_ULTIMATE, MekanismItems.ULTIMATE_CONTROL_CIRCUIT);
        getItemBuilder(MekanismTags.Items.CIRCUITS).add(MekanismTags.Items.CIRCUITS_BASIC, MekanismTags.Items.CIRCUITS_ADVANCED, MekanismTags.Items.CIRCUITS_ELITE,
              MekanismTags.Items.CIRCUITS_ULTIMATE);
    }

    private void addEndermanBlacklist() {
        addBlocksToTag(Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST,
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
        addItemsToTag(MekanismTags.Items.ENRICHED_CARBON, MekanismItems.ENRICHED_CARBON);
        addItemsToTag(MekanismTags.Items.ENRICHED_DIAMOND, MekanismItems.ENRICHED_DIAMOND);
        addItemsToTag(MekanismTags.Items.ENRICHED_OBSIDIAN, MekanismItems.ENRICHED_OBSIDIAN);
        addItemsToTag(MekanismTags.Items.ENRICHED_REDSTONE, MekanismItems.ENRICHED_REDSTONE);
        addItemsToTag(MekanismTags.Items.ENRICHED_GOLD, MekanismItems.ENRICHED_GOLD);
        addItemsToTag(MekanismTags.Items.ENRICHED_TIN, MekanismItems.ENRICHED_TIN);
        getItemBuilder(MekanismTags.Items.ENRICHED).add(MekanismTags.Items.ENRICHED_CARBON, MekanismTags.Items.ENRICHED_DIAMOND, MekanismTags.Items.ENRICHED_OBSIDIAN,
              MekanismTags.Items.ENRICHED_REDSTONE, MekanismTags.Items.ENRICHED_GOLD, MekanismTags.Items.ENRICHED_TIN);
    }

    private void addStorage() {
        addBlocksToTag(MekanismTags.Blocks.BARRELS_PERSONAL, MekanismBlocks.PERSONAL_BARREL);
        getBlockBuilder(Tags.Blocks.BARRELS).add(MekanismTags.Blocks.BARRELS_PERSONAL);
        addBlocksToTag(MekanismTags.Blocks.CHESTS_ELECTRIC, MekanismBlocks.PERSONAL_CHEST);
        addBlocksToTag(MekanismTags.Blocks.CHESTS_PERSONAL, MekanismBlocks.PERSONAL_CHEST);
        getBlockBuilder(Tags.Blocks.CHESTS).add(MekanismTags.Blocks.CHESTS_ELECTRIC, MekanismTags.Blocks.CHESTS_PERSONAL);
        addItemsToTag(MekanismTags.Items.PERSONAL_STORAGE, MekanismBlocks.PERSONAL_BARREL.getItemHolder(), MekanismBlocks.PERSONAL_CHEST.getItemHolder());
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
            addBlocksToTag(BlockTags.OVERWORLD_CARVER_REPLACEABLES, oreBlockType.stone(), oreBlockType.deepslate());
            addBlocksToTag(BlockTags.SNAPS_GOAT_HORN, oreBlockType.stone(), oreBlockType.deepslate());
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
        addItemsToTag(MekanismTags.Items.INGOTS_BRONZE, MekanismItems.BRONZE_INGOT);
        addItemsToTag(MekanismTags.Items.INGOTS_REFINED_GLOWSTONE, MekanismItems.REFINED_GLOWSTONE_INGOT);
        addItemsToTag(MekanismTags.Items.INGOTS_REFINED_OBSIDIAN, MekanismItems.REFINED_OBSIDIAN_INGOT);
        addItemsToTag(MekanismTags.Items.INGOTS_STEEL, MekanismItems.STEEL_INGOT);
        getItemBuilder(Tags.Items.INGOTS).add(MekanismTags.Items.INGOTS_BRONZE,
              MekanismTags.Items.INGOTS_REFINED_GLOWSTONE, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN, MekanismTags.Items.INGOTS_STEEL);
    }

    private void addNuggets() {
        addItemsToTag(MekanismTags.Items.NUGGETS_BRONZE, MekanismItems.BRONZE_NUGGET);
        addItemsToTag(MekanismTags.Items.NUGGETS_REFINED_GLOWSTONE, MekanismItems.REFINED_GLOWSTONE_NUGGET);
        addItemsToTag(MekanismTags.Items.NUGGETS_REFINED_OBSIDIAN, MekanismItems.REFINED_OBSIDIAN_NUGGET);
        addItemsToTag(MekanismTags.Items.NUGGETS_STEEL, MekanismItems.STEEL_NUGGET);
        getItemBuilder(Tags.Items.NUGGETS).add(MekanismTags.Items.NUGGETS_BRONZE,
              MekanismTags.Items.NUGGETS_REFINED_GLOWSTONE, MekanismTags.Items.NUGGETS_REFINED_OBSIDIAN, MekanismTags.Items.NUGGETS_STEEL);
    }

    private void addDusts() {
        addItemsToTag(MekanismTags.Items.DUSTS_BRONZE, MekanismItems.BRONZE_DUST);
        addItemsToTag(MekanismTags.Items.DUSTS_CHARCOAL, MekanismItems.CHARCOAL_DUST);
        addItemsToTag(MekanismTags.Items.DUSTS_COAL, MekanismItems.COAL_DUST);
        addItemsToTag(MekanismTags.Items.DUSTS_DIAMOND, MekanismItems.DIAMOND_DUST);
        addItemsToTag(MekanismTags.Items.DUSTS_EMERALD, MekanismItems.EMERALD_DUST);
        addItemsToTag(MekanismTags.Items.DUSTS_NETHERITE, MekanismItems.NETHERITE_DUST);
        addItemsToTag(MekanismTags.Items.DUSTS_LAPIS, MekanismItems.LAPIS_LAZULI_DUST);
        addItemsToTag(MekanismTags.Items.DUSTS_LITHIUM, MekanismItems.LITHIUM_DUST);
        addItemsToTag(MekanismTags.Items.DUSTS_OBSIDIAN, MekanismItems.OBSIDIAN_DUST);
        addItemsToTag(MekanismTags.Items.DUSTS_QUARTZ, MekanismItems.QUARTZ_DUST);
        addItemsToTag(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN, MekanismItems.REFINED_OBSIDIAN_DUST);
        addItemsToTag(MekanismTags.Items.DUSTS_SALT, MekanismItems.SALT);
        addItemsToTag(MekanismTags.Items.DUSTS_STEEL, MekanismItems.STEEL_DUST);
        addItemsToTag(MekanismTags.Items.DUSTS_SULFUR, MekanismItems.SULFUR_DUST);
        addItemsToTag(MekanismTags.Items.DUSTS_WOOD, MekanismItems.SAWDUST);
        addItemsToTag(MekanismTags.Items.DUSTS_FLUORITE, MekanismItems.FLUORITE_DUST);
        getItemBuilder(Tags.Items.DUSTS).add(MekanismTags.Items.DUSTS_BRONZE, MekanismTags.Items.DUSTS_CHARCOAL, MekanismTags.Items.DUSTS_COAL,
              MekanismTags.Items.DUSTS_DIAMOND, MekanismTags.Items.DUSTS_EMERALD, MekanismTags.Items.DUSTS_NETHERITE, MekanismTags.Items.DUSTS_LAPIS,
              MekanismTags.Items.DUSTS_LITHIUM, MekanismTags.Items.DUSTS_OBSIDIAN, MekanismTags.Items.DUSTS_QUARTZ, MekanismTags.Items.DUSTS_REFINED_OBSIDIAN,
              MekanismTags.Items.DUSTS_SALT, MekanismTags.Items.DUSTS_STEEL, MekanismTags.Items.DUSTS_SULFUR, MekanismTags.Items.DUSTS_WOOD,
              MekanismTags.Items.DUSTS_FLUORITE);
    }

    private void addGems() {
        addItemsToTag(MekanismTags.Items.GEMS_FLUORITE, MekanismItems.FLUORITE_GEM);
        getItemBuilder(Tags.Items.GEMS).add(MekanismTags.Items.GEMS_FLUORITE);
    }

    private void addPellets() {
        addItemsToTag(MekanismTags.Items.PELLETS_ANTIMATTER, MekanismItems.ANTIMATTER_PELLET);
        addItemsToTag(MekanismTags.Items.PELLETS_PLUTONIUM, MekanismItems.PLUTONIUM_PELLET);
        addItemsToTag(MekanismTags.Items.PELLETS_POLONIUM, MekanismItems.POLONIUM_PELLET);
    }

    private void addColorableItems() {
        getItemBuilder(MekanismTags.Items.COLORABLE_WOOL).add(Items.WHITE_WOOL, Items.ORANGE_WOOL, Items.MAGENTA_WOOL, Items.LIGHT_BLUE_WOOL, Items.YELLOW_WOOL,
              Items.LIME_WOOL, Items.PINK_WOOL, Items.GRAY_WOOL, Items.LIGHT_GRAY_WOOL, Items.CYAN_WOOL, Items.PURPLE_WOOL, Items.BLUE_WOOL, Items.BROWN_WOOL,
              Items.GREEN_WOOL, Items.RED_WOOL, Items.BLACK_WOOL);
        getItemBuilder(MekanismTags.Items.COLORABLE_CARPETS).add(Items.WHITE_CARPET, Items.ORANGE_CARPET, Items.MAGENTA_CARPET, Items.LIGHT_BLUE_CARPET, Items.YELLOW_CARPET,
              Items.LIME_CARPET, Items.PINK_CARPET, Items.GRAY_CARPET, Items.LIGHT_GRAY_CARPET, Items.CYAN_CARPET, Items.PURPLE_CARPET, Items.BLUE_CARPET,
              Items.BROWN_CARPET, Items.GREEN_CARPET, Items.RED_CARPET, Items.BLACK_CARPET);
        getItemBuilder(MekanismTags.Items.COLORABLE_BEDS).add(Items.WHITE_BED, Items.ORANGE_BED, Items.MAGENTA_BED, Items.LIGHT_BLUE_BED, Items.YELLOW_BED,
              Items.LIME_BED, Items.PINK_BED, Items.GRAY_BED, Items.LIGHT_GRAY_BED, Items.CYAN_BED, Items.PURPLE_BED, Items.BLUE_BED, Items.BROWN_BED,
              Items.GREEN_BED, Items.RED_BED, Items.BLACK_BED);
        getItemBuilder(MekanismTags.Items.COLORABLE_GLASS).add(Items.GLASS, Items.WHITE_STAINED_GLASS, Items.ORANGE_STAINED_GLASS, Items.MAGENTA_STAINED_GLASS,
              Items.LIGHT_BLUE_STAINED_GLASS, Items.YELLOW_STAINED_GLASS, Items.LIME_STAINED_GLASS, Items.PINK_STAINED_GLASS, Items.GRAY_STAINED_GLASS,
              Items.LIGHT_GRAY_STAINED_GLASS, Items.CYAN_STAINED_GLASS, Items.PURPLE_STAINED_GLASS, Items.BLUE_STAINED_GLASS, Items.BROWN_STAINED_GLASS,
              Items.GREEN_STAINED_GLASS, Items.RED_STAINED_GLASS, Items.BLACK_STAINED_GLASS);
        getItemBuilder(MekanismTags.Items.COLORABLE_GLASS_PANES).add(Items.GLASS_PANE, Items.WHITE_STAINED_GLASS_PANE, Items.ORANGE_STAINED_GLASS_PANE,
              Items.MAGENTA_STAINED_GLASS_PANE, Items.LIGHT_BLUE_STAINED_GLASS_PANE, Items.YELLOW_STAINED_GLASS_PANE, Items.LIME_STAINED_GLASS_PANE,
              Items.PINK_STAINED_GLASS_PANE, Items.GRAY_STAINED_GLASS_PANE, Items.LIGHT_GRAY_STAINED_GLASS_PANE, Items.CYAN_STAINED_GLASS_PANE,
              Items.PURPLE_STAINED_GLASS_PANE, Items.BLUE_STAINED_GLASS_PANE, Items.BROWN_STAINED_GLASS_PANE, Items.GREEN_STAINED_GLASS_PANE,
              Items.RED_STAINED_GLASS_PANE, Items.BLACK_STAINED_GLASS_PANE);
        getItemBuilder(MekanismTags.Items.COLORABLE_TERRACOTTA).add(Items.TERRACOTTA, Items.WHITE_TERRACOTTA, Items.ORANGE_TERRACOTTA, Items.MAGENTA_TERRACOTTA,
              Items.LIGHT_BLUE_TERRACOTTA, Items.YELLOW_TERRACOTTA, Items.LIME_TERRACOTTA, Items.PINK_TERRACOTTA, Items.GRAY_TERRACOTTA,
              Items.LIGHT_GRAY_TERRACOTTA, Items.CYAN_TERRACOTTA, Items.PURPLE_TERRACOTTA, Items.BLUE_TERRACOTTA, Items.BROWN_TERRACOTTA,
              Items.GREEN_TERRACOTTA, Items.RED_TERRACOTTA, Items.BLACK_TERRACOTTA);
        getItemBuilder(MekanismTags.Items.COLORABLE_CANDLE).add(Items.CANDLE, Items.WHITE_CANDLE, Items.ORANGE_CANDLE, Items.MAGENTA_CANDLE, Items.LIGHT_BLUE_CANDLE,
              Items.YELLOW_CANDLE, Items.LIME_CANDLE, Items.PINK_CANDLE, Items.GRAY_CANDLE, Items.LIGHT_GRAY_CANDLE, Items.CYAN_CANDLE, Items.PURPLE_CANDLE,
              Items.BLUE_CANDLE, Items.BROWN_CANDLE, Items.GREEN_CANDLE, Items.RED_CANDLE, Items.BLACK_CANDLE);
        getItemBuilder(MekanismTags.Items.COLORABLE_CONCRETE).add(Items.WHITE_CONCRETE, Items.ORANGE_CONCRETE, Items.MAGENTA_CONCRETE, Items.LIGHT_BLUE_CONCRETE,
              Items.YELLOW_CONCRETE, Items.LIME_CONCRETE, Items.PINK_CONCRETE, Items.GRAY_CONCRETE, Items.LIGHT_GRAY_CONCRETE, Items.CYAN_CONCRETE,
              Items.PURPLE_CONCRETE, Items.BLUE_CONCRETE, Items.BROWN_CONCRETE, Items.GREEN_CONCRETE, Items.RED_CONCRETE, Items.BLACK_CONCRETE);
        getItemBuilder(MekanismTags.Items.COLORABLE_CONCRETE_POWDER).add(Items.WHITE_CONCRETE_POWDER, Items.ORANGE_CONCRETE_POWDER, Items.MAGENTA_CONCRETE_POWDER,
              Items.LIGHT_BLUE_CONCRETE_POWDER, Items.YELLOW_CONCRETE_POWDER, Items.LIME_CONCRETE_POWDER, Items.PINK_CONCRETE_POWDER, Items.GRAY_CONCRETE_POWDER,
              Items.LIGHT_GRAY_CONCRETE_POWDER, Items.CYAN_CONCRETE_POWDER, Items.PURPLE_CONCRETE_POWDER, Items.BLUE_CONCRETE_POWDER, Items.BROWN_CONCRETE_POWDER,
              Items.GREEN_CONCRETE_POWDER, Items.RED_CONCRETE_POWDER, Items.BLACK_CONCRETE_POWDER);
        getItemBuilder(MekanismTags.Items.COLORABLE_BANNERS).addTyped(color -> BannerBlock.byColor(color).asItem(), DyeColor.values());
    }

    private void addBiomes() {
        getBiomeBuilder(MekanismTags.Biomes.SPAWN_ORES).add(BiomeTags.IS_OVERWORLD, Tags.Biomes.IS_OVERWORLD);
    }

    private void addDamageTypes() {
        addToTag(Tags.DamageTypes.IS_ENVIRONMENT, MekanismDamageTypes.RADIATION);
        addToTag(DamageTypeTags.BYPASSES_ARMOR, MekanismDamageTypes.RADIATION);
        addToTag(DamageTypeTags.BYPASSES_RESISTANCE, MekanismDamageTypes.RADIATION);
        addToTag(DamageTypeTags.BYPASSES_SHIELD, MekanismDamageTypes.RADIATION);
        addToTag(DamageTypeTags.BYPASSES_WOLF_ARMOR, MekanismDamageTypes.RADIATION);
        addToTag(DamageTypeTags.BYPASSES_COOLDOWN, MekanismDamageTypes.LASER);
        addToTag(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS, MekanismDamageTypes.LASER);
        addToTag(DamageTypeTags.PANIC_CAUSES, MekanismDamageTypes.LASER);
        addToTag(DamageTypeTags.NO_KNOCKBACK, MekanismDamageTypes.LASER, MekanismDamageTypes.RADIATION);
        addToTag(DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES, MekanismDamageTypes.RADIATION);
        getDamageTypeBuilder(MekanismAPITags.DamageTypes.IS_PREVENTABLE_MAGIC).add(DamageTypes.MAGIC, DamageTypes.INDIRECT_MAGIC);

        addToTag(MekanismAPITags.DamageTypes.MEKASUIT_ALWAYS_SUPPORTED, DamageTypes.FALLING_ANVIL, DamageTypes.CACTUS, DamageTypes.CRAMMING,
              DamageTypes.DRAGON_BREATH, DamageTypes.DRY_OUT, DamageTypes.FALL, DamageTypes.FALLING_BLOCK, DamageTypes.FLY_INTO_WALL, DamageTypes.GENERIC,
              DamageTypes.HOT_FLOOR, DamageTypes.IN_FIRE, DamageTypes.IN_WALL, DamageTypes.LAVA, DamageTypes.LIGHTNING_BOLT, DamageTypes.ON_FIRE,
              DamageTypes.SWEET_BERRY_BUSH, DamageTypes.WITHER, DamageTypes.FREEZE, DamageTypes.FALLING_STALACTITE, DamageTypes.STALAGMITE, DamageTypes.SONIC_BOOM);
    }

    private void addFluids() {
        addToGenericFluidTags(MekanismFluids.FLUIDS);
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
        addToTag(MekanismTags.Fluids.NUTRITIONAL_PASTE, MekanismFluids.NUTRITIONAL_PASTE);
        addToTag(MekanismTags.Fluids.URANIUM_OXIDE, MekanismFluids.URANIUM_OXIDE);
        addToTag(MekanismTags.Fluids.URANIUM_HEXAFLUORIDE, MekanismFluids.URANIUM_HEXAFLUORIDE);
        addToTag(Tags.Fluids.GASEOUS, MekanismFluids.STEAM);
    }

    private void addGameEvents() {
        addToTag(GameEventTags.VIBRATIONS, MekanismGameEvents.SEISMIC_VIBRATION, MekanismGameEvents.JETPACK_BURN, MekanismGameEvents.GRAVITY_MODULATE,
              MekanismGameEvents.GRAVITY_MODULATE_BOOSTED);
        addToTag(GameEventTags.WARDEN_CAN_LISTEN, MekanismGameEvents.SEISMIC_VIBRATION, MekanismGameEvents.JETPACK_BURN, MekanismGameEvents.GRAVITY_MODULATE,
              MekanismGameEvents.GRAVITY_MODULATE_BOOSTED);
    }

    private void addChemicalTags() {
        addChemicalsToTag(MekanismTags.Chemicals.WATER_VAPOR, MekanismChemicals.WATER_VAPOR, MekanismChemicals.STEAM);
        addChemicalsToTag(MekanismAPITags.Chemicals.WASTE_BARREL_DECAY_BLACKLIST, MekanismChemicals.PLUTONIUM, MekanismChemicals.POLONIUM);

        // add dynamic slurry tags
        getChemicalBuilder(MekanismAPITags.Chemicals.DIRTY).addHolders(MekanismChemicals.PROCESSED_RESOURCES.values());
        IntrinsicMekanismTagBuilder<Chemical> cleanTagBuilder = getChemicalBuilder(MekanismAPITags.Chemicals.CLEAN);
        for (SlurryRegistryObject<?, ?> slurryRO : MekanismChemicals.PROCESSED_RESOURCES.values()) {
            cleanTagBuilder.addHolders(slurryRO.getCleanSlurry());
        }

        addChemicalsToTag(MekanismAPITags.Chemicals.GASEOUS,
              MekanismChemicals.WATER_VAPOR,
              MekanismChemicals.STEAM,
              MekanismChemicals.BRINE,
              MekanismChemicals.HYDROGEN,
              MekanismChemicals.OXYGEN,
              MekanismChemicals.SULFUR_DIOXIDE,
              MekanismChemicals.SULFUR_TRIOXIDE,
              MekanismChemicals.HYDROGEN_CHLORIDE,
              MekanismChemicals.ETHENE
        );

        addChemicalsToTag(MekanismAPITags.Chemicals.CARBON, MekanismChemicals.CARBON);
        addChemicalsToTag(MekanismAPITags.Chemicals.REDSTONE, MekanismChemicals.REDSTONE);
        addChemicalsToTag(MekanismAPITags.Chemicals.DIAMOND, MekanismChemicals.DIAMOND);
        addChemicalsToTag(MekanismAPITags.Chemicals.REFINED_OBSIDIAN, MekanismChemicals.REFINED_OBSIDIAN);
        addChemicalsToTag(MekanismAPITags.Chemicals.GOLD, MekanismChemicals.GOLD);
        addChemicalsToTag(MekanismAPITags.Chemicals.TIN, MekanismChemicals.TIN);
        addChemicalsToTag(MekanismAPITags.Chemicals.FUNGI, MekanismChemicals.FUNGI);
        addChemicalsToTag(MekanismAPITags.Chemicals.BIO, MekanismChemicals.BIO);
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
        IntrinsicMekanismTagBuilder<Block> needsStoneToolBuilder = getBlockBuilder(BlockTags.NEEDS_STONE_TOOL);
        IntrinsicMekanismTagBuilder<Block> tagBuilder = getBlockBuilder(BlockTags.MINEABLE_WITH_PICKAXE);
        for (OreBlockType ore : MekanismBlocks.ORES.values()) {
            Holder<Block> stone = ore.stone();
            tagBuilder.addHolders(stone);
            hasHarvestData(stone);
            needsStoneToolBuilder.addHolders(stone);
            Holder<Block> deepslate = ore.deepslate();
            tagBuilder.addHolders(deepslate);
            hasHarvestData(deepslate);
            needsStoneToolBuilder.addHolders(deepslate);
        }
        addToHarvestTag(BlockTags.MINEABLE_WITH_SHOVEL, MekanismBlocks.SALT_BLOCK);
        addBlocksToTag(BlockTags.NEEDS_STONE_TOOL,
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
        addBlocksToTag(BlockTags.NEEDS_DIAMOND_TOOL, MekanismBlocks.REFINED_OBSIDIAN_BLOCK);
    }
}
