package mekanism.common.advancements;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.common.Mekanism;
import mekanism.common.advancements.triggers.AlloyUpgradeTrigger;
import mekanism.common.advancements.triggers.BlockLaserTrigger;
import mekanism.common.advancements.triggers.ChangeRobitSkinTrigger;
import mekanism.common.advancements.triggers.ConfigurationCardTrigger;
import mekanism.common.advancements.triggers.MekanismDamageTrigger;
import mekanism.common.advancements.triggers.SPSExperimentTrigger;
import mekanism.common.advancements.triggers.UnboxCardboardBoxTrigger;
import mekanism.common.advancements.triggers.UseGaugeDropperTrigger;
import mekanism.common.advancements.triggers.UseTierInstallerTrigger;
import mekanism.common.advancements.triggers.ViewVibrationsTrigger;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.entity.RobitPrideSkinData;
import mekanism.common.item.block.machine.ItemBlockFactory;
import mekanism.common.item.predicate.FullCanteenItemPredicate;
import mekanism.common.item.predicate.MaxedModuleContainerItemPredicate;
import mekanism.common.item.predicate.MekanismItemPredicates;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tier.FactoryTier;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.SummonedEntityTrigger;
import net.minecraft.advancements.critereon.UsingItemTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class MekanismAdvancementProvider extends BaseAdvancementProvider {

    public MekanismAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper existingFileHelper) {
        super(output, provider, existingFileHelper, Mekanism.MODID);
    }

    //TODO - 1.19: xp rewards for any of these?
    @Override
    protected void registerAdvancements(@NotNull Consumer<AdvancementHolder> consumer) {
        advancement(MekanismAdvancements.ROOT)
              .display(MekanismItems.ATOMIC_DISASSEMBLER, Mekanism.rl("textures/block/block_osmium.png"), AdvancementType.GOAL, false, false, false)
              .addCriterion("automatic", MekanismCriteriaTriggers.LOGGED_IN.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty())))
              .save(consumer);
        advancement(MekanismAdvancements.MATERIALS)
              .display(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM), AdvancementType.TASK, false)
              .orCriteria("material", MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM),
                    MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN),
                    MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD),
                    MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.URANIUM),
                    MekanismItems.FLUORITE_GEM
              ).save(consumer);

        advancement(MekanismAdvancements.CLEANING_GAUGES)
              .display(MekanismItems.GAUGE_DROPPER, AdvancementType.GOAL, true)
              .addCriterion("use_dropper", UseGaugeDropperTrigger.TriggerInstance.any())
              .save(consumer);

        advancement(MekanismAdvancements.METALLURGIC_INFUSER)
              .displayAndCriterion(MekanismBlocks.METALLURGIC_INFUSER, AdvancementType.TASK, true)
              .save(consumer);
        advancement(MekanismAdvancements.STEEL_INGOT)
              .displayAndCriterion(MekanismItems.STEEL_INGOT, AdvancementType.TASK, true)
              .save(consumer);
        advancement(MekanismAdvancements.STEEL_CASING)
              .displayAndCriterion(MekanismBlocks.STEEL_CASING, AdvancementType.TASK, true)
              .save(consumer);

        advancement(MekanismAdvancements.INFUSED_ALLOY)
              .displayAndCriterion(MekanismItems.INFUSED_ALLOY, AdvancementType.TASK, true)
              .save(consumer);
        advancement(MekanismAdvancements.REINFORCED_ALLOY)
              .displayAndCriterion(MekanismItems.REINFORCED_ALLOY, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.ATOMIC_ALLOY)
              .displayAndCriterion(MekanismItems.ATOMIC_ALLOY, AdvancementType.GOAL, false)
              .save(consumer);

        advancement(MekanismAdvancements.BASIC_CONTROL_CIRCUIT)
              .displayAndCriterion(MekanismItems.BASIC_CONTROL_CIRCUIT, AdvancementType.TASK, true)
              .save(consumer);
        advancement(MekanismAdvancements.ADVANCED_CONTROL_CIRCUIT)
              .displayAndCriterion(MekanismItems.ADVANCED_CONTROL_CIRCUIT, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.ELITE_CONTROL_CIRCUIT)
              .displayAndCriterion(MekanismItems.ELITE_CONTROL_CIRCUIT, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.ULTIMATE_CONTROL_CIRCUIT)
              .displayAndCriterion(MekanismItems.ULTIMATE_CONTROL_CIRCUIT, AdvancementType.GOAL, false)
              .save(consumer);

        advancement(MekanismAdvancements.ALLOY_UPGRADING)
              .display(MekanismItems.INFUSED_ALLOY, AdvancementType.GOAL, false)
              .addCriterion("upgrade", AlloyUpgradeTrigger.TriggerInstance.upgraded())
              .save(consumer);
        advancement(MekanismAdvancements.LASER)
              .displayAndCriterion(MekanismBlocks.LASER, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.LASER_DEATH)
              .display(Items.SKELETON_SKULL, null, AdvancementType.TASK, true, true, true)
              .addCriterion("death", MekanismDamageTrigger.TriggerInstance.killed(MekanismDamageTypes.LASER))
              .save(consumer);
        advancement(MekanismAdvancements.STOPPING_LASERS)
              .display(Items.SHIELD, AdvancementType.TASK, true)
              .addCriterion("block", BlockLaserTrigger.TriggerInstance.block())
              .save(consumer);
        advancement(MekanismAdvancements.AUTO_COLLECTION)
              .displayAndCriterion(MekanismBlocks.LASER_TRACTOR_BEAM, AdvancementType.TASK, false)
              .save(consumer);

        advancement(MekanismAdvancements.ALARM)
              .displayAndCriterion(MekanismBlocks.INDUSTRIAL_ALARM, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.INSTALLER)
              .display(MekanismItems.BASIC_TIER_INSTALLER, AdvancementType.GOAL, false)
              .orCriteria("installer", MekanismItems.BASIC_TIER_INSTALLER,
                    MekanismItems.ADVANCED_TIER_INSTALLER,
                    MekanismItems.ELITE_TIER_INSTALLER,
                    MekanismItems.ULTIMATE_TIER_INSTALLER
              ).save(consumer);
        advancement(MekanismAdvancements.FACTORY)
              .display(MekanismBlocks.getFactory(FactoryTier.BASIC, FactoryType.SMELTING), AdvancementType.GOAL, true)
              .orCriteria("factory", getItems(MekanismBlocks.BLOCKS.getSecondaryEntries(), item -> item instanceof ItemBlockFactory))
              .orCriteria("tier_installer", UseTierInstallerTrigger.TriggerInstance.any())
              .save(consumer);
        advancement(MekanismAdvancements.CONFIGURATION_COPYING)
              .display(MekanismItems.CONFIGURATION_CARD, AdvancementType.TASK, false)
              .andCriteria(
                    new RecipeCriterion("copy", ConfigurationCardTrigger.TriggerInstance.copyTrigger()),
                    new RecipeCriterion("paste", ConfigurationCardTrigger.TriggerInstance.pasteTrigger())
              ).save(consumer);

        advancement(MekanismAdvancements.RUNNING_FREE)
              .displayAndCriterion(MekanismItems.FREE_RUNNERS, AdvancementType.TASK, true)
              .save(consumer);
        advancement(MekanismAdvancements.PLAYING_WITH_FIRE)
              .displayAndCriterion(MekanismItems.FLAMETHROWER, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.MACHINE_SECURITY)
              .displayAndCriterion(MekanismBlocks.SECURITY_DESK, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.SOLAR_NEUTRON_ACTIVATOR)
              .displayAndCriterion(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.STABILIZING_CHUNKS)
              .displayAndCriterion(MekanismBlocks.DIMENSIONAL_STABILIZER, AdvancementType.CHALLENGE, true)
              .addCriterion(MekanismItems.ANCHOR_UPGRADE)
              .save(consumer);

        advancement(MekanismAdvancements.PERSONAL_STORAGE)
              .display(MekanismBlocks.PERSONAL_CHEST, AdvancementType.TASK, false)
              .addCriterion("storage", hasItems(MekanismTags.Items.PERSONAL_STORAGE))
              .save(consumer);
        advancement(MekanismAdvancements.SIMPLE_MASS_STORAGE)
              .displayAndCriterion(MekanismBlocks.BASIC_BIN, AdvancementType.TASK, false)
              .save(consumer);

        advancement(MekanismAdvancements.CONFIGURATOR)
              .displayAndCriterion(MekanismItems.CONFIGURATOR, AdvancementType.TASK, true)
              .save(consumer);
        advancement(MekanismAdvancements.NETWORK_READER)
              .displayAndCriterion(MekanismItems.NETWORK_READER, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.FLUID_TANK)
              .displayAndCriterion(MekanismBlocks.BASIC_FLUID_TANK, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.CHEMICAL_TANK)
              .displayAndCriterion(MekanismBlocks.BASIC_CHEMICAL_TANK, AdvancementType.TASK, false)
              .save(consumer);

        advancement(MekanismAdvancements.BREATHING_ASSISTANCE)
              .display(MekanismItems.SCUBA_MASK, AdvancementType.GOAL, true)
              .addCriterion("scuba_gear", hasAllItems(
                    MekanismItems.SCUBA_MASK,
                    MekanismItems.SCUBA_TANK
              )).save(consumer);
        advancement(MekanismAdvancements.HYDROGEN_POWERED_FLIGHT)
              .displayAndCriterion(MekanismItems.JETPACK, AdvancementType.TASK, true)
              .save(consumer);

        advancement(MekanismAdvancements.WASTE_REMOVAL)
              .displayAndCriterion(MekanismBlocks.RADIOACTIVE_WASTE_BARREL, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.ENVIRONMENTAL_RADIATION)
              .display(MekanismItems.GEIGER_COUNTER, AdvancementType.TASK, false)
              .addCriterion("use_geiger_counter", CriteriaTriggers.USING_ITEM.createCriterion(new UsingItemTrigger.TriggerInstance(Optional.empty(), Optional.of(predicate(MekanismItems.GEIGER_COUNTER)))))
              .save(consumer);
        advancement(MekanismAdvancements.PERSONAL_RADIATION)
              .display(MekanismItems.DOSIMETER, AdvancementType.TASK, false)
              .addCriterion("use_dosimeter", CriteriaTriggers.USING_ITEM.createCriterion(new UsingItemTrigger.TriggerInstance(Optional.empty(), Optional.of(predicate(MekanismItems.DOSIMETER)))))
              .save(consumer);
        advancement(MekanismAdvancements.RADIATION_PREVENTION)
              .display(MekanismItems.HAZMAT_GOWN, AdvancementType.TASK, true)
              .addCriterion("full_set", hasAllItems(
                    MekanismItems.HAZMAT_MASK,
                    MekanismItems.HAZMAT_GOWN,
                    MekanismItems.HAZMAT_PANTS,
                    MekanismItems.HAZMAT_BOOTS
              )).save(consumer);
        advancement(MekanismAdvancements.RADIATION_POISONING)
              .display(MekanismBlocks.RADIOACTIVE_WASTE_BARREL, AdvancementType.TASK, true)
              .addCriterion("poisoned", MekanismDamageTrigger.TriggerInstance.damaged(MekanismDamageTypes.RADIATION))
              .save(consumer);
        advancement(MekanismAdvancements.RADIATION_POISONING_DEATH)
              .display(Items.PLAYER_HEAD, null, AdvancementType.TASK, true, true, true)
              .addCriterion("death", MekanismDamageTrigger.TriggerInstance.killed(MekanismDamageTypes.RADIATION))
              .save(consumer);

        advancement(MekanismAdvancements.PLUTONIUM)
              .displayAndCriterion(MekanismItems.PLUTONIUM_PELLET, AdvancementType.TASK, true)
              .save(consumer);
        //TODO: If we end up adding a criteria for creating a multiblock switch the criteria for this to using that
        advancement(MekanismAdvancements.SPS)
              .display(MekanismBlocks.SPS_CASING, AdvancementType.TASK, false)
              .andCriteria(MekanismBlocks.SPS_CASING,
                    MekanismBlocks.SPS_PORT
              ).save(consumer);
        advancement(MekanismAdvancements.ANTIMATTER)
              .displayAndCriterion(MekanismItems.ANTIMATTER_PELLET, AdvancementType.TASK, true)
              .save(consumer);
        advancement(MekanismAdvancements.NUCLEOSYNTHESIZER)
              .displayAndCriterion(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, AdvancementType.CHALLENGE, true)
              .save(consumer);
        addExperiments(consumer);

        advancement(MekanismAdvancements.POLONIUM)
              .displayAndCriterion(MekanismItems.POLONIUM_PELLET, AdvancementType.TASK, true)
              .save(consumer);

        advancement(MekanismAdvancements.TELEPORTATION_CORE)
              .displayAndCriterion(MekanismItems.TELEPORTATION_CORE, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.QUANTUM_ENTANGLOPORTER)
              .displayAndCriterion(MekanismBlocks.QUANTUM_ENTANGLOPORTER, AdvancementType.TASK, true)
              .save(consumer);
        advancement(MekanismAdvancements.TELEPORTER)
              .displayAndCriterion(MekanismBlocks.TELEPORTER, AdvancementType.TASK, true)
              .addCriterion("teleport", MekanismCriteriaTriggers.TELEPORT.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty())))
              .save(consumer);
        advancement(MekanismAdvancements.PORTABLE_TELEPORTER)
              .displayAndCriterion(MekanismItems.PORTABLE_TELEPORTER, AdvancementType.TASK, true)
              .save(consumer);

        advancement(MekanismAdvancements.QIO_DRIVE_ARRAY)
              .displayAndCriterion(MekanismBlocks.QIO_DRIVE_ARRAY, AdvancementType.TASK, true)
              .save(consumer);
        advancement(MekanismAdvancements.QIO_EXPORTER)
              .displayAndCriterion(MekanismBlocks.QIO_EXPORTER, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.QIO_IMPORTER)
              .displayAndCriterion(MekanismBlocks.QIO_IMPORTER, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.QIO_REDSTONE_ADAPTER)
              .displayAndCriterion(MekanismBlocks.QIO_REDSTONE_ADAPTER, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.QIO_DASHBOARD)
              .displayAndCriterion(MekanismBlocks.QIO_DASHBOARD, AdvancementType.TASK, true)
              .save(consumer);
        advancement(MekanismAdvancements.PORTABLE_QIO_DASHBOARD)
              .displayAndCriterion(MekanismItems.PORTABLE_QIO_DASHBOARD, AdvancementType.GOAL, true)
              .save(consumer);
        advancement(MekanismAdvancements.BASIC_QIO_DRIVE)
              .displayAndCriterion(MekanismItems.BASE_QIO_DRIVE, AdvancementType.TASK, true)
              .save(consumer);
        advancement(MekanismAdvancements.ADVANCED_QIO_DRIVE)
              .displayAndCriterion(MekanismItems.HYPER_DENSE_QIO_DRIVE, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.ELITE_QIO_DRIVE)
              .displayAndCriterion(MekanismItems.TIME_DILATING_QIO_DRIVE, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.ULTIMATE_QIO_DRIVE)
              .displayAndCriterion(MekanismItems.SUPERMASSIVE_QIO_DRIVE, AdvancementType.CHALLENGE, true)
              .save(consumer);

        advancement(MekanismAdvancements.ROBIT)
              .display(MekanismItems.ROBIT, AdvancementType.GOAL, true)
              .addCriterion("summon", SummonedEntityTrigger.TriggerInstance.summonedEntity(EntityPredicate.Builder.entity().of(MekanismEntityTypes.ROBIT.value())))
              .save(consumer);
        ItemStack skinnedRobit = MekanismItems.ROBIT.asStack();
        skinnedRobit.set(MekanismDataComponents.ROBIT_SKIN, MekanismRobitSkins.PRIDE_SKINS.get(RobitPrideSkinData.TRANS));
        advancement(MekanismAdvancements.ROBIT_AESTHETICS)
              .display(skinnedRobit, null, AdvancementType.TASK, true, false, true)
              .addCriterion("change_skin", ChangeRobitSkinTrigger.TriggerInstance.toAny())
              .save(consumer);
        advancement(MekanismAdvancements.DIGITAL_MINER)
              .displayAndCriterion(MekanismBlocks.DIGITAL_MINER, AdvancementType.GOAL, true)
              .save(consumer);
        advancement(MekanismAdvancements.DICTIONARY)
              .displayAndCriterion(MekanismItems.DICTIONARY, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.STONE_GENERATOR)
              .displayAndCriterion(MekanismItems.STONE_GENERATOR_UPGRADE, AdvancementType.TASK, true)
              .save(consumer);

        advancement(MekanismAdvancements.DISASSEMBLER)
              .displayAndCriterion(MekanismItems.ATOMIC_DISASSEMBLER, AdvancementType.TASK, true)
              .save(consumer);
        advancement(MekanismAdvancements.MEKASUIT)
              .display(MekanismItems.MEKASUIT_BODYARMOR, AdvancementType.GOAL, true)
              .addCriterion("full_set", hasAllItems(
                    MekanismItems.MEKASUIT_HELMET,
                    MekanismItems.MEKASUIT_BODYARMOR,
                    MekanismItems.MEKASUIT_PANTS,
                    MekanismItems.MEKASUIT_BOOTS,
                    MekanismItems.MEKA_TOOL
              )).save(consumer);
        advancement(MekanismAdvancements.MODIFICATION_STATION)
              .displayAndCriterion(MekanismBlocks.MODIFICATION_STATION, AdvancementType.TASK, true)
              .save(consumer);
        //Require having all of them maxed at once
        advancement(MekanismAdvancements.UPGRADED_MEKASUIT)
              .display(MekanismItems.MEKASUIT_BODYARMOR, null, AdvancementType.CHALLENGE, true, true, true)
              .addCriterion("maxed_gear", hasItems(Stream.of(
                                MekanismItems.MEKASUIT_HELMET,
                                MekanismItems.MEKASUIT_BODYARMOR,
                                MekanismItems.MEKASUIT_PANTS,
                                MekanismItems.MEKASUIT_BOOTS,
                                MekanismItems.MEKA_TOOL
                          ).map(item -> ItemPredicate.Builder.item().withSubPredicate(MekanismItemPredicates.MAXED_MODULE_CONTAINER_ITEM.value(),
                                new MaxedModuleContainerItemPredicate(item)).build())
                          .toArray(ItemPredicate[]::new)
              )).save(consumer);

        advancement(MekanismAdvancements.FLUID_TRANSPORT)
              .displayAndCriterion(MekanismBlocks.BASIC_MECHANICAL_PIPE, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.CHEMICAL_TRANSPORT)
              .displayAndCriterion(MekanismBlocks.BASIC_PRESSURIZED_TUBE, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.ENERGY_TRANSPORT)
              .displayAndCriterion(MekanismBlocks.BASIC_UNIVERSAL_CABLE, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.HEAT_TRANSPORT)
              .displayAndCriterion(MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.ITEM_TRANSPORT)
              .displayAndCriterion(MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.RESTRICTIVE_ITEM_TRANSPORT)
              .displayAndCriterion(MekanismBlocks.RESTRICTIVE_TRANSPORTER, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.DIVERSION_ITEM_TRANSPORT)
              .displayAndCriterion(MekanismBlocks.DIVERSION_TRANSPORTER, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.SORTER)
              .displayAndCriterion(MekanismBlocks.LOGISTICAL_SORTER, AdvancementType.GOAL, true)
              .save(consumer);

        advancement(MekanismAdvancements.ENERGY_CUBE)
              .displayAndCriterion(MekanismBlocks.BASIC_ENERGY_CUBE, AdvancementType.TASK, false)
              .save(consumer);

        advancement(MekanismAdvancements.AUTOMATED_CRAFTING)
              .displayAndCriterion(MekanismBlocks.FORMULAIC_ASSEMBLICATOR, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.SEISMIC_VIBRATIONS)
              .displayAndCriterion(MekanismBlocks.SEISMIC_VIBRATOR, AdvancementType.TASK, false)
              .addCriterion(MekanismItems.SEISMIC_READER)
              .addCriterion("view_vibrations", ViewVibrationsTrigger.TriggerInstance.view())
              .save(consumer);
        advancement(MekanismAdvancements.PAINTING_MACHINE)
              .displayAndCriterion(MekanismBlocks.PAINTING_MACHINE, AdvancementType.TASK, false)
              .save(consumer);

        advancement(MekanismAdvancements.ENRICHER)
              .displayAndCriterion(MekanismBlocks.ENRICHMENT_CHAMBER, AdvancementType.TASK, true)
              .save(consumer);
        advancement(MekanismAdvancements.INFUSING_EFFICIENCY)
              .display(MekanismItems.ENRICHED_REDSTONE, AdvancementType.TASK, true)
              .addCriterion("enriched_material", hasItems(MekanismTags.Items.ENRICHED))
              .save(consumer);
        advancement(MekanismAdvancements.YELLOW_CAKE)
              .displayAndCriterion(MekanismItems.YELLOW_CAKE_URANIUM, AdvancementType.GOAL, false)
              .save(consumer);

        advancement(MekanismAdvancements.PURIFICATION_CHAMBER)
              .displayAndCriterion(MekanismBlocks.PURIFICATION_CHAMBER, AdvancementType.GOAL, false)
              .save(consumer);
        advancement(MekanismAdvancements.INJECTION_CHAMBER)
              .displayAndCriterion(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER, AdvancementType.GOAL, false)
              .save(consumer);
        advancement(MekanismAdvancements.CHEMICAL_CRYSTALLIZER)
              .displayAndCriterion(MekanismBlocks.CHEMICAL_CRYSTALLIZER, AdvancementType.CHALLENGE, true)
              .andCriteria(MekanismBlocks.CHEMICAL_WASHER, MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER)
              .save(consumer);

        advancement(MekanismAdvancements.SAWMILL)
              .displayAndCriterion(MekanismBlocks.PRECISION_SAWMILL, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.MOVING_BLOCKS)
              .displayAndCriterion(MekanismBlocks.CARDBOARD_BOX, AdvancementType.TASK, true)
              .addCriterion("unbox", UnboxCardboardBoxTrigger.TriggerInstance.unbox())
              .save(consumer);

        advancement(MekanismAdvancements.PUMP)
              .displayAndCriterion(MekanismBlocks.ELECTRIC_PUMP, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.PLENISHER)
              .displayAndCriterion(MekanismBlocks.FLUIDIC_PLENISHER, AdvancementType.TASK, false)
              .save(consumer);

        advancement(MekanismAdvancements.LIQUIFIER)
              .displayAndCriterion(MekanismBlocks.NUTRITIONAL_LIQUIFIER, AdvancementType.TASK, false)
              .save(consumer);
        advancement(MekanismAdvancements.FULL_CANTEEN)
              .display(MekanismItems.CANTEEN, null, AdvancementType.GOAL, true, true, true)
              .addCriterion("full_canteen", hasItems(ItemPredicate.Builder.item().withSubPredicate(MekanismItemPredicates.FULL_CANTEEN.value(), FullCanteenItemPredicate.INSTANCE).build()))
              .save(consumer);
    }

    private void addExperiments(@NotNull Consumer<AdvancementHolder> consumer) {
        advancement(MekanismAdvancements.SPS_EXPERIMENT_CREEPER)
              .display(Items.CREEPER_HEAD, null, AdvancementType.CHALLENGE, true, true, true)
              .addCriterion("experiment", SPSExperimentTrigger.TriggerInstance.create(MekanismTags.Entities.CREEPERS))
              .save(consumer);
        advancement(MekanismAdvancements.SPS_EXPERIMENT_MOOSHROOM)
              .display(Items.MOOSHROOM_SPAWN_EGG, null, AdvancementType.CHALLENGE, true, true, true)
              .addCriterion("experiment", SPSExperimentTrigger.TriggerInstance.create(EntityType.MOOSHROOM))
              .save(consumer);
        advancement(MekanismAdvancements.SPS_EXPERIMENT_PIG)
              .display(Items.PIG_SPAWN_EGG, null, AdvancementType.CHALLENGE, true, true, true)
              .addCriterion("experiment", SPSExperimentTrigger.TriggerInstance.create(EntityType.PIG))
              .save(consumer);
        advancement(MekanismAdvancements.SPS_EXPERIMENT_VILLAGER)
              .display(Items.VILLAGER_SPAWN_EGG, null, AdvancementType.CHALLENGE, true, true, true)
              .addCriterion("experiment", SPSExperimentTrigger.TriggerInstance.create(EntityType.VILLAGER))
              .save(consumer);
    }
}