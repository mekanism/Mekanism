package mekanism.common.tag;

import com.google.common.collect.Table.Cell;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.CleanDirtySlurryId;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismDataComponents;
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
import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityTypeIds;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MekanismTagProvider extends BaseTagProvider {

    public MekanismTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Mekanism.MODID);
    }

    @Override
    protected Collection<? extends DeferredHolder<Block, ?>> getAllBlocks() {
        return MekanismBlocks.BLOCKS.getPrimaryEntries();
    }

    @Override
    protected void registerTags(HolderLookup.Provider registries) {
        //TODO - 26.3: Make it so that cats sit/lay on cardboard boxes
        addProcessedResources();
        addBeaconTags();
        addBoxBlacklist();
        addGear();
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
        addDataComponents();
        addFluids();
        addGameEvents();
        addChemicalTags();
        addPellets();
        addUpgrades();
        addColorableItems();
        getBuilder(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE).add(Tags.Blocks.ORES, BlockTags.LOGS);
        getBuilder(MekanismTags.Blocks.INCORRECT_FOR_DISASSEMBLER);
        getBuilder(MekanismTags.Blocks.INCORRECT_FOR_MEKA_TOOL);
        getBuilder(BlockTags.GUARDED_BY_PIGLINS).add(MekanismBlocks.REFINED_GLOWSTONE_BLOCK, MekanismBlocks.PERSONAL_BARREL, MekanismBlocks.PERSONAL_CHEST);
        getBuilder(BlockTags.HOGLIN_REPELLENTS).add(MekanismBlocks.TELEPORTER, MekanismBlocks.QUANTUM_ENTANGLOPORTER);
        getBuilder(ItemTags.PIGLIN_LOVED).add(
              MekanismBlocks.REFINED_GLOWSTONE_BLOCK.getItemHolder(),
              MekanismItems.REFINED_GLOWSTONE_INGOT,
              MekanismItems.getProcessedResource(ResourceType.DUST, PrimaryResource.GOLD)
        ).add(
              MekanismTags.Items.ENRICHED_GOLD,
              MekanismTags.Items.getProcessedResource(ResourceType.SHARD, PrimaryResource.GOLD),
              MekanismTags.Items.getProcessedResource(ResourceType.CRYSTAL, PrimaryResource.GOLD),
              MekanismTags.Items.getProcessedResource(ResourceType.DIRTY_DUST, PrimaryResource.GOLD),
              MekanismTags.Items.getProcessedResource(ResourceType.CLUMP, PrimaryResource.GOLD)
        );
        getBuilder(MekanismAPITags.Items.MEKASUIT_HUD_RENDERER).add(MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR_HELMET);
        getBuilder(MekanismTags.Items.STONE_CRAFTING_MATERIALS).add(ItemTags.STONE_CRAFTING_MATERIALS, Tags.Items.COBBLESTONES_NORMAL);
        getBuilder(MekanismTags.Items.MUFFLING_CENTER).add(
              Tags.Items.BRICKS,
              Tags.Items.INGOTS_IRON,
              Tags.Items.INGOTS_GOLD,
              Tags.Items.INGOTS_COPPER,
              MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM),
              MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.TIN),
              MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.LEAD),
              MekanismTags.Items.GEMS_FLUORITE
        );
        addEntities();
        getBuilder(MekanismTags.Blocks.MINER_BLACKLIST);
        addHarvestRequirements();
        getBuilder(BlockTags.IMPERMEABLE).add(MekanismBlocks.STRUCTURAL_GLASS);
        //Note: Axolotls live in a brackish water (mix between fresh and salt), so it is reasonable there may be salt nearby
        getBuilder(BlockTags.AXOLOTLS_SPAWNABLE_ON).add(MekanismBlocks.SALT_BLOCK);
        getBuilder(ItemTags.CLUSTER_MAX_HARVESTABLES).add(MekanismItems.ATOMIC_DISASSEMBLER, MekanismItems.MEKA_TOOL);
        getBuilder(ItemTags.FREEZE_IMMUNE_WEARABLES).add(
              MekanismItems.MEKASUIT_HELMET,
              MekanismItems.MEKASUIT_BODYARMOR,
              MekanismItems.MEKASUIT_PANTS,
              MekanismItems.MEKASUIT_BOOTS
        );
        getBuilder(BlockTags.SCULK_REPLACEABLE).add(MekanismBlocks.SALT_BLOCK);
        getBuilder(MekanismAPITags.MobEffects.SPEED_UP_BLACKLIST);

        getBuilder(MekanismTags.Blocks.FARMING_OVERRIDE).add(BlockItemIds.PINK_PETALS.block());
        getBuilder(BlockTags.CAMEL_SAND_STEP_SOUND_BLOCKS).add(MekanismBlocks.SALT_BLOCK);

        getBuilder(Tags.Blocks.HIDDEN_FROM_RECIPE_VIEWERS).add(MekanismBlocks.BOUNDING_BLOCK);

        getBuilder(BlockTags.CANNOT_SUPPORT_SNOW_LAYER).add(
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

        getBuilder(FRAMEABLE).add(MekanismBlocks.STRUCTURAL_GLASS);
        getBuilder(FB_BE_WHITELIST).add(MekanismBlocks.STRUCTURAL_GLASS);
        getBuilder(PE_VEIN_SHOVEL).add(MekanismBlocks.SALT_BLOCK);
    }

    private void addEntities() {
        getBuilder(EntityTypeTags.IMPACT_PROJECTILES).add(MekanismEntityTypes.FLAME);
        getBuilder(PVI_COMPAT).add(MekanismEntityTypes.ROBIT);

        getBuilder(MekanismTags.Entities.CREEPERS)
              .add(EntityTypeIds.CREEPER);

        getBuilder(MekanismAPITags.Entities.RADIATION_IMMUNE).add(MekanismEntityTypes.ROBIT);
        getBuilder(MekanismAPITags.Entities.MEK_RADIATION_IMMUNE)
              .add(MekanismAPITags.Entities.RADIATION_IMMUNE)
              .add(EntityTypeIds.IRON_GOLEM, EntityTypeIds.SNOW_GOLEM);

        getBuilder(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS).add(MekanismEntityTypes.ROBIT);
        getBuilder(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES).add(MekanismEntityTypes.ROBIT);
        getBuilder(EntityTypeTags.CAN_BREATHE_UNDER_WATER).add(MekanismEntityTypes.ROBIT);
        getBuilder(EntityTypeTags.IGNORES_POISON_AND_REGEN).add(MekanismEntityTypes.ROBIT);
        getBuilder(EntityTypeTags.IMMUNE_TO_INFESTED).add(MekanismEntityTypes.ROBIT);
        getBuilder(EntityTypeTags.IMMUNE_TO_OOZING).add(MekanismEntityTypes.ROBIT);
        getBuilder(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(MekanismEntityTypes.ROBIT);
        //Robit's don't need to breathe
        getBuilder(EntityTypeTags.CAN_BREATHE_UNDER_WATER).add(MekanismEntityTypes.ROBIT);
        //Robit's are not scary, they are friends!
        getBuilder(EntityTypeTags.NOT_SCARY_FOR_PUFFERFISH).add(MekanismEntityTypes.ROBIT);
        getBuilder(EntityTypeTags.ILLAGER_FRIENDS).add(MekanismEntityTypes.ROBIT);
        getBuilder(EntityTypeTags.WITHER_FRIENDS).add(MekanismEntityTypes.ROBIT);

        getBuilder(MekanismTags.Entities.VALID_SPS_EXPERIMENT).add(
              EntityTypeIds.MOOSHROOM,//Changes color
              EntityTypeIds.PIG,//Turns into a zombified piglin
              EntityTypeIds.VILLAGER//Turns into witch
        ).add(
              MekanismTags.Entities.CREEPERS//Becomes charged
        );
    }

    private void addProcessedResources() {
        for (Cell<ResourceType, PrimaryResource, ? extends Holder<Item>> item : MekanismItems.PROCESSED_RESOURCES.cellSet()) {
            TagKey<Item> tag = MekanismTags.Items.getProcessedResource(item.getRowKey(), item.getColumnKey());
            getBuilder(tag).add(item.getValue());
            getBuilder(switch (item.getRowKey()) {
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
        getBuilder(BlockTags.BEACON_BASE_BLOCKS).add(
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
        getBuilder(ItemTags.BEACON_PAYMENT_ITEMS).add(
              MekanismItems.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM),
              MekanismItems.getProcessedResource(ResourceType.INGOT, PrimaryResource.TIN),
              MekanismItems.getProcessedResource(ResourceType.INGOT, PrimaryResource.LEAD),
              MekanismItems.getProcessedResource(ResourceType.INGOT, PrimaryResource.URANIUM),
              MekanismItems.BRONZE_INGOT,
              MekanismItems.REFINED_OBSIDIAN_INGOT,
              MekanismItems.REFINED_GLOWSTONE_INGOT,
              MekanismItems.STEEL_INGOT
        );
    }

    private void addBoxBlacklist() {
        getBuilder(Tags.Blocks.RELOCATION_NOT_SUPPORTED).add(
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
        getBuilder(MekanismTags.Blocks.CARDBOARD_BLACKLIST)
              .add(Tags.Blocks.RELOCATION_NOT_SUPPORTED, BlockTags.BEDS, BlockTags.DOORS)
              .add(BlockItemIds.TRIAL_SPAWNER.block(), BlockItemIds.VAULT.block());
    }

    private void addGear() {
        addTools();
        addArmor();
        getBuilder(MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR_HELMET).add(MekanismItems.MEKASUIT_HELMET);
        getBuilder(MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR_CHESTPLATE).add(MekanismItems.MEKASUIT_BODYARMOR);
        getBuilder(MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR_LEGGINGS).add(MekanismItems.MEKASUIT_PANTS);
        getBuilder(MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR_BOOTS).add(MekanismItems.MEKASUIT_BOOTS);
        getBuilder(MekanismAPITags.Items.MODULE_CONTAINERS_HELD_MEKA_TOOL).add(MekanismItems.MEKA_TOOL);
        getBuilder(MekanismAPITags.Items.MODULE_CONTAINERS_HELD).add(
              MekanismAPITags.Items.MODULE_CONTAINERS_HELD_MEKA_TOOL
        );
        getBuilder(MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR).add(
              MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR_HELMET,
              MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR_CHESTPLATE,
              MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR_LEGGINGS,
              MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR_BOOTS
        );
        getBuilder(MekanismAPITags.Items.MODULE_CONTAINERS_COLORABLE).add(
              MekanismItems.MEKASUIT_HELMET,
              MekanismItems.MEKASUIT_BODYARMOR,
              MekanismItems.MEKASUIT_PANTS,
              MekanismItems.MEKASUIT_BOOTS
        );
        getBuilder(MekanismAPITags.Items.MODULE_CONTAINERS).add(
              MekanismAPITags.Items.MODULE_CONTAINERS_COLORABLE,
              MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR,
              MekanismAPITags.Items.MODULE_CONTAINERS_HELD
        );
    }

    private void addTools() {
        addWrenches();
        getBuilder(ItemTags.BREAKS_DECORATED_POTS).add(MekanismItems.ATOMIC_DISASSEMBLER, MekanismItems.MEKA_TOOL);
        getBuilder(Tags.Items.MINING_TOOL_TOOLS).add(MekanismItems.ATOMIC_DISASSEMBLER, MekanismItems.MEKA_TOOL);
        getBuilder(Tags.Items.TOOLS_BOW).add(MekanismItems.ELECTRIC_BOW);
        getBuilder(Tags.Items.RANGED_WEAPON_TOOLS).add(MekanismItems.ELECTRIC_BOW);
        getBuilder(ItemTags.BOW_ENCHANTABLE).add(MekanismItems.ELECTRIC_BOW);
        getBuilder(ItemTags.DURABILITY_ENCHANTABLE).add(MekanismItems.HDPE_REINFORCED_ELYTRA);
        getBuilder(ItemTags.EQUIPPABLE_ENCHANTABLE).add(MekanismItems.HDPE_REINFORCED_ELYTRA);
    }

    private void addWrenches() {
        //Note: We don't add wrenches to the vanilla tools tag as that is for a different style of tool and used for things like breaking pots
        getBuilder(Tags.Items.TOOLS_WRENCH).add(MekanismItems.CONFIGURATOR);
        getBuilder(MekanismTags.Items.CONFIGURATORS).add(Tags.Items.TOOLS_WRENCH);
    }

    private void addArmor() {
        getBuilder(ItemTags.HEAD_ARMOR).add(MekanismItems.HAZMAT_MASK, MekanismItems.MEKASUIT_HELMET);
        getBuilder(ItemTags.CHEST_ARMOR).add(MekanismItems.HAZMAT_GOWN, MekanismItems.MEKASUIT_BODYARMOR);
        getBuilder(ItemTags.LEG_ARMOR).add(MekanismItems.HAZMAT_PANTS, MekanismItems.MEKASUIT_PANTS);
        getBuilder(ItemTags.FOOT_ARMOR).add(MekanismItems.HAZMAT_BOOTS, MekanismItems.MEKASUIT_BOOTS);
        getBuilder(ItemTags.TRIMMABLE_ARMOR).remove(
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
        getBuilder(ItemTags.DURABILITY_ENCHANTABLE).remove(providers);
        getBuilder(ItemTags.EQUIPPABLE_ENCHANTABLE).remove(providers);
        getBuilder(ItemTags.HEAD_ARMOR_ENCHANTABLE).remove(MekanismItems.MEKASUIT_HELMET);
        getBuilder(ItemTags.CHEST_ARMOR_ENCHANTABLE).remove(MekanismItems.MEKASUIT_BODYARMOR);
        getBuilder(ItemTags.LEG_ARMOR_ENCHANTABLE).remove(MekanismItems.MEKASUIT_PANTS);
        getBuilder(ItemTags.FOOT_ARMOR_ENCHANTABLE).remove(MekanismItems.MEKASUIT_BOOTS);
    }

    private void addRods() {
        getBuilder(MekanismTags.Items.RODS_PLASTIC).add(MekanismItems.HDPE_STICK);
        getBuilder(Tags.Items.RODS).add(MekanismTags.Items.RODS_PLASTIC);
    }

    private void addFuels() {
        getBuilder(MekanismTags.Items.FUELS_BIO).add(MekanismItems.BIO_FUEL);
        getBuilder(MekanismTags.Items.FUELS_BLOCK_BIO).add(MekanismBlocks.BIO_FUEL_BLOCK.getItemHolder());
        getBuilder(MekanismTags.Items.FUELS).add(MekanismTags.Items.FUELS_BIO, MekanismTags.Items.FUELS_BLOCK_BIO);
    }

    private void addAlloys() {
        //Alloy Tags that go in the forge domain
        getBuilder(MekanismTags.Items.ALLOYS_ADVANCED).add(MekanismItems.INFUSED_ALLOY);
        getBuilder(MekanismTags.Items.ALLOYS_ELITE).add(MekanismItems.REINFORCED_ALLOY);
        getBuilder(MekanismTags.Items.ALLOYS_ULTIMATE).add(MekanismItems.ATOMIC_ALLOY);
        getBuilder(MekanismTags.Items.COMMON_ALLOYS).add(MekanismTags.Items.ALLOYS_ADVANCED, MekanismTags.Items.ALLOYS_ELITE, MekanismTags.Items.ALLOYS_ULTIMATE);
        //Alloy tags that go in our domain
        getBuilder(MekanismTags.Items.ALLOYS_BASIC).add(BlockItemIds.REDSTONE_DUST.item());
        getBuilder(MekanismTags.Items.ALLOYS_INFUSED).add(MekanismTags.Items.ALLOYS_ADVANCED);
        getBuilder(MekanismTags.Items.ALLOYS_REINFORCED).add(MekanismTags.Items.ALLOYS_ELITE);
        getBuilder(MekanismTags.Items.ALLOYS_ATOMIC).add(MekanismTags.Items.ALLOYS_ULTIMATE);
        getBuilder(MekanismTags.Items.ALLOYS).add(MekanismTags.Items.ALLOYS_BASIC, MekanismTags.Items.ALLOYS_INFUSED, MekanismTags.Items.ALLOYS_REINFORCED,
              MekanismTags.Items.ALLOYS_ATOMIC);
    }

    private void addCircuits() {
        getBuilder(MekanismTags.Items.CIRCUITS_BASIC).add(MekanismItems.BASIC_CONTROL_CIRCUIT);
        getBuilder(MekanismTags.Items.CIRCUITS_ADVANCED).add(MekanismItems.ADVANCED_CONTROL_CIRCUIT);
        getBuilder(MekanismTags.Items.CIRCUITS_ELITE).add(MekanismItems.ELITE_CONTROL_CIRCUIT);
        getBuilder(MekanismTags.Items.CIRCUITS_ULTIMATE).add(MekanismItems.ULTIMATE_CONTROL_CIRCUIT);
        getBuilder(MekanismTags.Items.CIRCUITS).add(MekanismTags.Items.CIRCUITS_BASIC, MekanismTags.Items.CIRCUITS_ADVANCED, MekanismTags.Items.CIRCUITS_ELITE,
              MekanismTags.Items.CIRCUITS_ULTIMATE);
    }

    private void addEndermanBlacklist() {
        getBuilder(Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST).add(
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
        getBuilder(MekanismTags.Items.ENRICHED_CARBON).add(MekanismItems.ENRICHED_CARBON);
        getBuilder(MekanismTags.Items.ENRICHED_DIAMOND).add(MekanismItems.ENRICHED_DIAMOND);
        getBuilder(MekanismTags.Items.ENRICHED_OBSIDIAN).add(MekanismItems.ENRICHED_OBSIDIAN);
        getBuilder(MekanismTags.Items.ENRICHED_REDSTONE).add(MekanismItems.ENRICHED_REDSTONE);
        getBuilder(MekanismTags.Items.ENRICHED_GOLD).add(MekanismItems.ENRICHED_GOLD);
        getBuilder(MekanismTags.Items.ENRICHED_TIN).add(MekanismItems.ENRICHED_TIN);
        getBuilder(MekanismTags.Items.ENRICHED).add(MekanismTags.Items.ENRICHED_CARBON, MekanismTags.Items.ENRICHED_DIAMOND, MekanismTags.Items.ENRICHED_OBSIDIAN,
              MekanismTags.Items.ENRICHED_REDSTONE, MekanismTags.Items.ENRICHED_GOLD, MekanismTags.Items.ENRICHED_TIN);
    }

    private void addStorage() {
        getBuilder(MekanismTags.Blocks.BARRELS_PERSONAL).add(MekanismBlocks.PERSONAL_BARREL);
        getBuilder(Tags.Blocks.BARRELS).add(MekanismTags.Blocks.BARRELS_PERSONAL);
        getBuilder(MekanismTags.Blocks.CHESTS_ELECTRIC).add(MekanismBlocks.PERSONAL_CHEST);
        getBuilder(MekanismTags.Blocks.CHESTS_PERSONAL).add(MekanismBlocks.PERSONAL_CHEST);
        getBuilder(Tags.Blocks.CHESTS).add(MekanismTags.Blocks.CHESTS_ELECTRIC, MekanismTags.Blocks.CHESTS_PERSONAL);
        getBuilder(MekanismTags.BlockItems.PERSONAL_STORAGE.item()).add(MekanismBlocks.PERSONAL_BARREL.getItemHolder(), MekanismBlocks.PERSONAL_CHEST.getItemHolder());
        getBuilder(MekanismTags.BlockItems.PERSONAL_STORAGE.block()).add(MekanismTags.Blocks.BARRELS_PERSONAL, MekanismTags.Blocks.CHESTS_PERSONAL);
    }

    private void addOres() {
        for (Map.Entry<OreType, OreBlockType> entry : MekanismBlocks.ORES.entrySet()) {
            OreType type = entry.getKey();
            OreBlockType oreBlockType = entry.getValue();
            BlockItemTagId tag = MekanismTags.BlockItems.ORES.get(type);
            addToTags(tag, oreBlockType.stone(), oreBlockType.deepslate());
            getBuilder(Tags.Items.ORES).add(tag.item());
            getBuilder(Tags.Blocks.ORES).add(tag.block());
            if (type.getResource() == MiscResource.FLUORITE) {
                addToTags(Tags.Items.ORE_RATES_DENSE, Tags.Blocks.ORE_RATES_DENSE, oreBlockType.stone(), oreBlockType.deepslate());
            } else {
                addToTags(Tags.Items.ORE_RATES_SINGULAR, Tags.Blocks.ORE_RATES_SINGULAR, oreBlockType.stone(), oreBlockType.deepslate());
            }
            addToTags(Tags.Items.ORES_IN_GROUND_DEEPSLATE, Tags.Blocks.ORES_IN_GROUND_DEEPSLATE, oreBlockType.deepslate());
            addToTags(Tags.Items.ORES_IN_GROUND_STONE, Tags.Blocks.ORES_IN_GROUND_STONE, oreBlockType.stone());
            getBuilder(BlockTags.OVERWORLD_CARVER_REPLACEABLES).add(oreBlockType.stone(), oreBlockType.deepslate());
            getBuilder(BlockTags.SNAPS_GOAT_HORN).add(oreBlockType.stone(), oreBlockType.deepslate());
        }
    }

    private void addStorageBlocks() {
        addToTags(MekanismTags.BlockItems.STORAGE_BLOCKS_BRONZE, MekanismBlocks.BRONZE_BLOCK);
        addToTags(MekanismTags.BlockItems.STORAGE_BLOCKS_CHARCOAL, MekanismBlocks.CHARCOAL_BLOCK);
        addToTags(MekanismTags.BlockItems.STORAGE_BLOCKS_REFINED_GLOWSTONE, MekanismBlocks.REFINED_GLOWSTONE_BLOCK);
        addToTags(MekanismTags.BlockItems.STORAGE_BLOCKS_REFINED_OBSIDIAN, MekanismBlocks.REFINED_OBSIDIAN_BLOCK);
        addToTags(MekanismTags.BlockItems.STORAGE_BLOCKS_STEEL, MekanismBlocks.STEEL_BLOCK);
        addToTags(MekanismTags.BlockItems.STORAGE_BLOCKS_FLUORITE, MekanismBlocks.FLUORITE_BLOCK);
        addToTags(Tags.Items.STORAGE_BLOCKS, Tags.Blocks.STORAGE_BLOCKS,
              MekanismTags.BlockItems.STORAGE_BLOCKS_BRONZE, MekanismTags.BlockItems.STORAGE_BLOCKS_CHARCOAL, MekanismTags.BlockItems.STORAGE_BLOCKS_REFINED_GLOWSTONE,
              MekanismTags.BlockItems.STORAGE_BLOCKS_REFINED_OBSIDIAN, MekanismTags.BlockItems.STORAGE_BLOCKS_STEEL, MekanismTags.BlockItems.STORAGE_BLOCKS_FLUORITE
        );
        // Dynamic storage blocks
        for (Map.Entry<IResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            BlockItemTagId tag = MekanismTags.BlockItems.PROCESSED_RESOURCE_BLOCKS.get(entry.getKey());
            addToTags(tag, entry.getValue());
            getBuilder(Tags.Items.STORAGE_BLOCKS).add(tag.item());
            getBuilder(Tags.Blocks.STORAGE_BLOCKS).add(tag.block());
        }
    }

    private void addIngots() {
        getBuilder(MekanismTags.Items.INGOTS_BRONZE).add(MekanismItems.BRONZE_INGOT);
        getBuilder(MekanismTags.Items.INGOTS_REFINED_GLOWSTONE).add(MekanismItems.REFINED_GLOWSTONE_INGOT);
        getBuilder(MekanismTags.Items.INGOTS_REFINED_OBSIDIAN).add(MekanismItems.REFINED_OBSIDIAN_INGOT);
        getBuilder(MekanismTags.Items.INGOTS_STEEL).add(MekanismItems.STEEL_INGOT);
        getBuilder(Tags.Items.INGOTS).add(MekanismTags.Items.INGOTS_BRONZE,
              MekanismTags.Items.INGOTS_REFINED_GLOWSTONE, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN, MekanismTags.Items.INGOTS_STEEL);
    }

    private void addNuggets() {
        getBuilder(MekanismTags.Items.NUGGETS_BRONZE).add(MekanismItems.BRONZE_NUGGET);
        getBuilder(MekanismTags.Items.NUGGETS_REFINED_GLOWSTONE).add(MekanismItems.REFINED_GLOWSTONE_NUGGET);
        getBuilder(MekanismTags.Items.NUGGETS_REFINED_OBSIDIAN).add(MekanismItems.REFINED_OBSIDIAN_NUGGET);
        getBuilder(MekanismTags.Items.NUGGETS_STEEL).add(MekanismItems.STEEL_NUGGET);
        getBuilder(Tags.Items.NUGGETS).add(MekanismTags.Items.NUGGETS_BRONZE,
              MekanismTags.Items.NUGGETS_REFINED_GLOWSTONE, MekanismTags.Items.NUGGETS_REFINED_OBSIDIAN, MekanismTags.Items.NUGGETS_STEEL);
    }

    private void addDusts() {
        getBuilder(MekanismTags.Items.DUSTS_BRONZE).add(MekanismItems.BRONZE_DUST);
        getBuilder(MekanismTags.Items.DUSTS_CHARCOAL).add(MekanismItems.CHARCOAL_DUST);
        getBuilder(MekanismTags.Items.DUSTS_COAL).add(MekanismItems.COAL_DUST);
        getBuilder(MekanismTags.Items.DUSTS_DIAMOND).add(MekanismItems.DIAMOND_DUST);
        getBuilder(MekanismTags.Items.DUSTS_EMERALD).add(MekanismItems.EMERALD_DUST);
        getBuilder(MekanismTags.Items.DUSTS_NETHERITE).add(MekanismItems.NETHERITE_DUST);
        getBuilder(MekanismTags.Items.DUSTS_LAPIS).add(MekanismItems.LAPIS_LAZULI_DUST);
        getBuilder(MekanismTags.Items.DUSTS_LITHIUM).add(MekanismItems.LITHIUM_DUST);
        getBuilder(MekanismTags.Items.DUSTS_OBSIDIAN).add(MekanismItems.OBSIDIAN_DUST);
        getBuilder(MekanismTags.Items.DUSTS_QUARTZ).add(MekanismItems.QUARTZ_DUST);
        getBuilder(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN).add(MekanismItems.REFINED_OBSIDIAN_DUST);
        getBuilder(MekanismTags.Items.DUSTS_SALT).add(MekanismItems.SALT);
        getBuilder(MekanismTags.Items.DUSTS_STEEL).add(MekanismItems.STEEL_DUST);
        getBuilder(MekanismTags.Items.DUSTS_SULFUR).add(MekanismItems.SULFUR_DUST);
        getBuilder(MekanismTags.Items.DUSTS_WOOD).add(MekanismItems.SAWDUST);
        getBuilder(MekanismTags.Items.DUSTS_FLUORITE).add(MekanismItems.FLUORITE_DUST);
        getBuilder(Tags.Items.DUSTS).add(MekanismTags.Items.DUSTS_BRONZE, MekanismTags.Items.DUSTS_CHARCOAL, MekanismTags.Items.DUSTS_COAL,
              MekanismTags.Items.DUSTS_DIAMOND, MekanismTags.Items.DUSTS_EMERALD, MekanismTags.Items.DUSTS_NETHERITE, MekanismTags.Items.DUSTS_LAPIS,
              MekanismTags.Items.DUSTS_LITHIUM, MekanismTags.Items.DUSTS_OBSIDIAN, MekanismTags.Items.DUSTS_QUARTZ, MekanismTags.Items.DUSTS_REFINED_OBSIDIAN,
              MekanismTags.Items.DUSTS_SALT, MekanismTags.Items.DUSTS_STEEL, MekanismTags.Items.DUSTS_SULFUR, MekanismTags.Items.DUSTS_WOOD,
              MekanismTags.Items.DUSTS_FLUORITE);
    }

    private void addGems() {
        getBuilder(MekanismTags.Items.GEMS_FLUORITE).add(MekanismItems.FLUORITE_GEM);
        getBuilder(Tags.Items.GEMS).add(MekanismTags.Items.GEMS_FLUORITE);
    }

    private void addPellets() {
        getBuilder(MekanismTags.Items.PELLETS_ANTIMATTER).add(MekanismItems.ANTIMATTER_PELLET);
        getBuilder(MekanismTags.Items.PELLETS_PLUTONIUM).add(MekanismItems.PLUTONIUM_PELLET);
        getBuilder(MekanismTags.Items.PELLETS_POLONIUM).add(MekanismItems.POLONIUM_PELLET);
    }

    private void addUpgrades() {
        getBuilder(MekanismTags.Upgrades.SIMPLE_MACHINE_UPGRADES).add(UpgradeIds.ENERGY, UpgradeIds.SPEED);
        getBuilder(MekanismTags.Upgrades.DEFAULT_MACHINE_UPGRADES)
              .add(MekanismTags.Upgrades.SIMPLE_MACHINE_UPGRADES)
              .add(UpgradeIds.MUFFLING);
        getBuilder(MekanismTags.Upgrades.DEFAULT_ADVANCED_MACHINE_UPGRADES)
              .add(MekanismTags.Upgrades.DEFAULT_MACHINE_UPGRADES)
              .add(UpgradeIds.CHEMICAL);
        getBuilder(MekanismTags.Upgrades.ANCHOR_ONLY).add(UpgradeIds.ANCHOR);
        getBuilder(MekanismTags.Upgrades.MUFFLING_ONLY).add(UpgradeIds.MUFFLING);

        getBuilder(MekanismTags.Upgrades.DIMENSIONAL_STABILIZER)
              .add(UpgradeIds.ENERGY);
        getBuilder(MekanismTags.Upgrades.QIO_FILTER_HANDLER)
              .add(UpgradeIds.SPEED);
        getBuilder(MekanismTags.Upgrades.ELECTRIC_PUMP)
              .add(MekanismTags.Upgrades.SIMPLE_MACHINE_UPGRADES)
              .add(UpgradeIds.FILTER);
        getBuilder(MekanismTags.Upgrades.DIGITAL_MINER)
              .add(MekanismTags.Upgrades.SIMPLE_MACHINE_UPGRADES)
              .add(UpgradeIds.ANCHOR, UpgradeIds.STONE_GENERATOR);
    }

    private void addColorableItems() {
        getBuilder(MekanismTags.Items.COLORABLE_WOOL).add(BlockItemIds.WOOL, BlockItemId::item);
        getBuilder(MekanismTags.Items.COLORABLE_CARPETS).add(BlockItemIds.CARPET, BlockItemId::item);
        getBuilder(MekanismTags.Items.COLORABLE_BEDS).add(BlockItemIds.BED, BlockItemId::item);
        getBuilder(MekanismTags.Items.COLORABLE_GLASS).add(BlockItemIds.STAINED_GLASS, BlockItemId::item);
        getBuilder(MekanismTags.Items.COLORABLE_GLASS_PANES).add(BlockItemIds.STAINED_GLASS_PANE, BlockItemId::item);
        getBuilder(MekanismTags.Items.COLORABLE_TERRACOTTA).add(BlockItemIds.DYED_TERRACOTTA, BlockItemId::item);
        getBuilder(MekanismTags.Items.COLORABLE_CANDLE).add(BlockItemIds.DYED_CANDLE, BlockItemId::item);
        getBuilder(MekanismTags.Items.COLORABLE_CONCRETE).add(BlockItemIds.CONCRETE, BlockItemId::item);
        getBuilder(MekanismTags.Items.COLORABLE_CONCRETE_POWDER).add(BlockItemIds.CONCRETE_POWDER, BlockItemId::item);
        getBuilder(MekanismTags.Items.COLORABLE_BANNERS).add(BlockItemIds.BANNER, BlockItemId::item);
    }

    private void addBiomes() {
        getBuilder(MekanismTags.Biomes.SPAWN_ORES).add(BiomeTags.IS_OVERWORLD, Tags.Biomes.IS_OVERWORLD);
    }

    private void addDamageTypes() {
        ResourceKey<DamageType> flamethrower = MekanismDamageTypes.FLAMETHROWER.key();
        ResourceKey<DamageType> laser = MekanismDamageTypes.LASER.key();
        ResourceKey<DamageType> radiation = MekanismDamageTypes.RADIATION.key();
        ResourceKey<DamageType> sps = MekanismDamageTypes.SPS.key();
        getBuilder(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS).add(laser, radiation, sps);
        getBuilder(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS).add(flamethrower, laser, sps);
        getBuilder(DamageTypeTags.BYPASSES_ARMOR).add(radiation, sps);
        getBuilder(DamageTypeTags.BYPASSES_COOLDOWN).add(laser, sps);
        getBuilder(DamageTypeTags.BYPASSES_EFFECTS).add(radiation, sps);
        getBuilder(DamageTypeTags.BYPASSES_ENCHANTMENTS).add(radiation, sps);
        getBuilder(DamageTypeTags.BYPASSES_RESISTANCE).add(radiation, sps);
        getBuilder(DamageTypeTags.BYPASSES_SHIELD).add(radiation, sps);
        getBuilder(DamageTypeTags.BYPASSES_WOLF_ARMOR).add(radiation, sps);
        getBuilder(Tags.DamageTypes.IS_ENVIRONMENT).add(radiation);
        getBuilder(DamageTypeTags.IS_FIRE).add(flamethrower);
        getBuilder(DamageTypeTags.IS_LIGHTNING).add(sps);
        getBuilder(MekanismAPITags.DamageTypes.IS_PREVENTABLE_MAGIC).add(DamageTypes.MAGIC, DamageTypes.INDIRECT_MAGIC);
        getBuilder(DamageTypeTags.IS_PROJECTILE).add(flamethrower);
        getBuilder(DamageTypeTags.NO_KNOCKBACK).add(flamethrower, laser, radiation, sps);
        getBuilder(DamageTypeTags.PANIC_CAUSES).add(flamethrower, laser);
        getBuilder(DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES).add(radiation, sps);

        getBuilder(MekanismAPITags.DamageTypes.MEKASUIT_ALWAYS_SUPPORTED).add(DamageTypes.FALLING_ANVIL, DamageTypes.CACTUS, DamageTypes.CRAMMING,
              DamageTypes.DRAGON_BREATH, DamageTypes.DRY_OUT, DamageTypes.FALL, DamageTypes.FALLING_BLOCK, DamageTypes.FLY_INTO_WALL, DamageTypes.GENERIC,
              DamageTypes.HOT_FLOOR, DamageTypes.IN_FIRE, DamageTypes.IN_WALL, DamageTypes.LAVA, DamageTypes.LIGHTNING_BOLT, DamageTypes.ON_FIRE,
              DamageTypes.SWEET_BERRY_BUSH, DamageTypes.WITHER, DamageTypes.FREEZE, DamageTypes.FALLING_STALACTITE, DamageTypes.STALAGMITE, DamageTypes.SONIC_BOOM);
    }

    private void addDataComponents() {
        //TODO: Evaluate supporting some of these in some sort of generic way in RecipeUpgradeType?
        getBuilder(MekanismTags.DataComponents.CLEARABLE_CONFIG).add(
              MekanismDataComponents.DISASSEMBLER_MODE,
              MekanismDataComponents.CONFIGURATOR_MODE,
              MekanismDataComponents.FLAMETHROWER_MODE,
              MekanismDataComponents.FREE_RUNNER_MODE,
              MekanismDataComponents.JETPACK_MODE,
              MekanismDataComponents.EDIT_MODE,
              MekanismDataComponents.DUMP_MODE,
              MekanismDataComponents.SECONDARY_DUMP_MODE,
              MekanismDataComponents.REDSTONE_CONTROL,
              MekanismDataComponents.REDSTONE_OUTPUT,
              MekanismDataComponents.SCUBA_TANK_MODE,
              MekanismDataComponents.ELECTRIC_BOW_MODE,
              MekanismDataComponents.BUCKET_MODE,
              MekanismDataComponents.ROTARY_MODE,
              MekanismDataComponents.AUTO,
              MekanismDataComponents.SORTING,
              MekanismDataComponents.EJECT,
              MekanismDataComponents.PULL,
              MekanismDataComponents.ROUND_ROBIN,
              MekanismDataComponents.SINGLE_ITEM,
              MekanismDataComponents.FUZZY,
              MekanismDataComponents.SILK_TOUCH,
              MekanismDataComponents.INVERSE,
              MekanismDataComponents.INVERSE_REQUIRES_REPLACE,
              MekanismDataComponents.FROM_RECIPE,
              MekanismDataComponents.INSERT_INTO_FREQUENCY,
              MekanismDataComponents.RADIUS,
              MekanismDataComponents.MIN_Y,
              MekanismDataComponents.MAX_Y,
              MekanismDataComponents.REPLACE_STACK,
              MekanismDataComponents.DELAY,
              MekanismDataComponents.MIN_THRESHOLD,
              MekanismDataComponents.MAX_THRESHOLD,
              //This is the amount of energy per tick a resistive heater is set to
              MekanismDataComponents.ENERGY_USAGE,
              MekanismDataComponents.LONG_AMOUNT,
              MekanismDataComponents.ITEM_TARGET,
              MekanismDataComponents.STABILIZER_CHUNKS,
              MekanismDataComponents.ROBIT_NAME,
              //Note: We intentionally don't clear the DEFAULT_MANUALLY_SELECTED for robits, as we want to make sure that people have a way to make them stack if needed
              MekanismDataComponents.ROBIT_SKIN,
              MekanismDataComponents.FORMULA_HOLDER,
              MekanismDataComponents.CONFIGURATION_DATA,
              MekanismDataComponents.COLOR,
              MekanismDataComponents.EJECTOR,
              MekanismDataComponents.SIDE_CONFIG,
              MekanismDataComponents.FILTER_AWARE,
              MekanismDataComponents.TELEPORTER_FREQUENCY,
              MekanismDataComponents.INVENTORY_FREQUENCY,
              MekanismDataComponents.QIO_FREQUENCY,
              //Note: We clear attached heat even though it isn't exactly a config, just because people may want their heat generators and the like to be able to stack
              // and the heat value is based on the temperature it was in the world (that it is no longer in)
              MekanismDataComponents.ATTACHED_HEAT
        );
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
        getBuilder(GameEventTags.VIBRATIONS).add(
              MekanismGameEvents.SEISMIC_VIBRATION,
              MekanismGameEvents.JETPACK_BURN,
              MekanismGameEvents.GRAVITY_MODULATE,
              MekanismGameEvents.GRAVITY_MODULATE_BOOSTED
        );
        getBuilder(GameEventTags.WARDEN_CAN_LISTEN).add(
              MekanismGameEvents.SEISMIC_VIBRATION,
              MekanismGameEvents.JETPACK_BURN,
              MekanismGameEvents.GRAVITY_MODULATE,
              MekanismGameEvents.GRAVITY_MODULATE_BOOSTED
        );
    }

    private void addChemicalTags() {
        getBuilder(MekanismTags.Chemicals.WATER_VAPOR).add(ChemicalIds.WATER_VAPOR, ChemicalIds.STEAM);
        getBuilder(MekanismAPITags.Chemicals.WASTE_BARREL_DECAY_BLACKLIST).add(ChemicalIds.PLUTONIUM, ChemicalIds.POLONIUM);

        // add dynamic slurry tags
        MekanismTagBuilder<Chemical> dirtyTagBuilder = getBuilder(MekanismAPITags.Chemicals.DIRTY);
        MekanismTagBuilder<Chemical> cleanTagBuilder = getBuilder(MekanismAPITags.Chemicals.CLEAN);
        for (CleanDirtySlurryId slurry : MekanismChemicals.PROCESSED_RESOURCES.values()) {
            dirtyTagBuilder.add(slurry.dirty());
            cleanTagBuilder.add(slurry.clean());
        }

        getBuilder(MekanismAPITags.Chemicals.GASEOUS).add(
              ChemicalIds.WATER_VAPOR,
              ChemicalIds.STEAM,
              ChemicalIds.BRINE,
              ChemicalIds.HYDROGEN,
              ChemicalIds.OXYGEN,
              ChemicalIds.SULFUR_DIOXIDE,
              ChemicalIds.SULFUR_TRIOXIDE,
              ChemicalIds.HYDROGEN_CHLORIDE,
              ChemicalIds.ETHENE,
              ChemicalIds.SUPERHEATED_SODIUM
        );

        getBuilder(MekanismAPITags.Chemicals.CARBON).add(ChemicalIds.CARBON);
        getBuilder(MekanismAPITags.Chemicals.REDSTONE).add(ChemicalIds.REDSTONE);
        getBuilder(MekanismAPITags.Chemicals.DIAMOND).add(ChemicalIds.DIAMOND);
        getBuilder(MekanismAPITags.Chemicals.REFINED_OBSIDIAN).add(ChemicalIds.REFINED_OBSIDIAN);
        getBuilder(MekanismAPITags.Chemicals.GOLD).add(ChemicalIds.GOLD);
        getBuilder(MekanismAPITags.Chemicals.TIN).add(ChemicalIds.TIN);
        getBuilder(MekanismAPITags.Chemicals.FUNGI).add(ChemicalIds.FUNGI);
        getBuilder(MekanismAPITags.Chemicals.BIO).add(ChemicalIds.BIO);
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
              MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.values()
        );
        MekanismTagBuilder<Block> needsStoneToolBuilder = getBuilder(BlockTags.NEEDS_STONE_TOOL);
        for (OreBlockType ore : MekanismBlocks.ORES.values()) {
            Holder<Block> stone = ore.stone();
            Holder<Block> deepslate = ore.deepslate();
            addToHarvestTag(BlockTags.MINEABLE_WITH_PICKAXE, stone, deepslate);
            needsStoneToolBuilder.add(stone, deepslate);
        }
        addToHarvestTag(BlockTags.MINEABLE_WITH_SHOVEL, MekanismBlocks.SALT_BLOCK);
        getBuilder(BlockTags.NEEDS_STONE_TOOL).add(
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
        getBuilder(BlockTags.NEEDS_DIAMOND_TOOL).add(MekanismBlocks.REFINED_OBSIDIAN_BLOCK);
    }
}
