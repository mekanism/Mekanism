package mekanism.common.advancements;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.item.predicate.FullCanteenItemPredicate;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class MekanismAdvancementProvider extends BaseAdvancementProvider {

    public MekanismAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper, Mekanism.MODID);
    }

    @Override
    protected void registerAdvancements(@Nonnull Consumer<Advancement> consumer) {
        //TODO - 1.19: For performance reasons maybe we want to replace the tick trigger with a custom trigger that effectively is on join world?
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ROOT)
              .display(MekanismBlocks.STEEL_BLOCK, Mekanism.rl("textures/block/block_osmium.png"), FrameType.GOAL, false, false, false)
              .addCriterion("tick", new PlayerTrigger.TriggerInstance(CriteriaTriggers.TICK.getId(), EntityPredicate.Composite.ANY))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.MATERIALS)
              .display(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM), FrameType.TASK)
              .orRequirements()
              .addCriterion("osmium", hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)))
              .addCriterion("tin", hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN)))
              .addCriterion("lead", hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD)))
              .addCriterion("uranium", hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.URANIUM)))
              .addCriterion("fluorite", hasItems(MekanismItems.FLUORITE_GEM))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.FLUID_TANK)
              .display(MekanismBlocks.BASIC_FLUID_TANK, FrameType.TASK)
              .addCriterion("fluid_tank", hasItems(MekanismBlocks.BASIC_FLUID_TANK))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.CHEMICAL_TANK)
              .display(MekanismBlocks.BASIC_CHEMICAL_TANK, FrameType.TASK)
              .addCriterion("chemical_tank", hasItems(MekanismBlocks.BASIC_CHEMICAL_TANK))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.FULL_CANTEEN)
              .display(MekanismItems.CANTEEN, null, FrameType.TASK, true, true, true)
              .addCriterion("full_canteen", InventoryChangeTrigger.TriggerInstance.hasItems(FullCanteenItemPredicate.INSTANCE))
              .save(consumer, fileHelper);
        generateMetallurgy(consumer);
        generateDisassembly(consumer);
    }

    private void generateMetallurgy(@Nonnull Consumer<Advancement> consumer) {
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.METALLURGIC_INFUSER)
              .display(MekanismBlocks.METALLURGIC_INFUSER, FrameType.TASK)
              .addCriterion("metallurgic_infuser", hasItems(MekanismBlocks.METALLURGIC_INFUSER))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.STEEL_INGOT)
              .display(MekanismItems.STEEL_INGOT, FrameType.TASK)
              .addCriterion("steel_ingot", hasItems(MekanismItems.STEEL_INGOT))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.STEEL_CASING)
              .display(MekanismBlocks.STEEL_CASING, FrameType.TASK)
              .addCriterion("steel_casing", hasItems(MekanismBlocks.STEEL_CASING))
              .save(consumer, fileHelper);
        generateAlloys(consumer);
        generateControls(consumer);
    }

    private void generateAlloys(@Nonnull Consumer<Advancement> consumer) {
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.INFUSED_ALLOY)
              .display(MekanismItems.INFUSED_ALLOY, FrameType.TASK)
              .addCriterion("infused_alloy", hasItems(MekanismItems.INFUSED_ALLOY))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.REINFORCED_ALLOY)
              .display(MekanismItems.REINFORCED_ALLOY, FrameType.TASK)
              .addCriterion("reinforced_alloy", hasItems(MekanismItems.REINFORCED_ALLOY))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ATOMIC_ALLOY)
              .display(MekanismItems.ATOMIC_ALLOY, FrameType.GOAL)
              .addCriterion("atomic_alloy", hasItems(MekanismItems.ATOMIC_ALLOY))
              .save(consumer, fileHelper);
        generateSPS(consumer);
        generateQIO(consumer);
        generateTeleports(consumer);
    }

    private void generateSPS(@Nonnull Consumer<Advancement> consumer) {
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.PLUTONIUM)
              .display(MekanismItems.PLUTONIUM_PELLET, FrameType.TASK)
              .addCriterion("plutonium", hasItems(MekanismItems.PLUTONIUM_PELLET))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.SPS)
              .display(MekanismBlocks.SPS_CASING, FrameType.TASK)
              .orRequirements()
              .addCriterion("sps_casing", hasItems(MekanismBlocks.SPS_CASING))
              .addCriterion("sps_port", hasItems(MekanismBlocks.SPS_PORT))
              .save(consumer, fileHelper);
        //TODO: Was obfuscated for description. Do we want it like that or to have the actual description?
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ANTIMATTER)
              .display(MekanismItems.ANTIMATTER_PELLET, FrameType.TASK)
              .addCriterion("antimatter", hasItems(MekanismItems.ANTIMATTER_PELLET))
              .save(consumer, fileHelper);
        //TODO: Was obfuscated for description. Do we want it like that or to have the actual description?
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.NUCLEOSYNTHESIZER)
              .display(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, FrameType.CHALLENGE)
              .addCriterion("nucleosynthesizer", hasItems(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER))
              .save(consumer, fileHelper);
    }

    private void generateQIO(@Nonnull Consumer<Advancement> consumer) {
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.POLONIUM)
              .display(MekanismItems.POLONIUM_PELLET, FrameType.TASK)
              .addCriterion("polonium", hasItems(MekanismItems.POLONIUM_PELLET))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.QIO_DRIVE_ARRAY)
              .display(MekanismBlocks.QIO_DRIVE_ARRAY, FrameType.TASK)
              .addCriterion("qio_drive_array", hasItems(MekanismBlocks.QIO_DRIVE_ARRAY))
              .save(consumer, fileHelper);
        generateQIODrives(consumer);
        generateQIODashboards(consumer);
    }

    private void generateQIODrives(@Nonnull Consumer<Advancement> consumer) {
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.BASIC_QIO_DRIVE)
              .display(MekanismItems.BASE_QIO_DRIVE, FrameType.TASK)
              .addCriterion("basic_qio_drive", hasItems(MekanismItems.BASE_QIO_DRIVE))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ADVANCED_QIO_DRIVE)
              .display(MekanismItems.HYPER_DENSE_QIO_DRIVE, FrameType.TASK)
              .addCriterion("advanced_qio_drive", hasItems(MekanismItems.HYPER_DENSE_QIO_DRIVE))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ELITE_QIO_DRIVE)
              .display(MekanismItems.TIME_DILATING_QIO_DRIVE, FrameType.TASK)
              .addCriterion("elite_qio_drive", hasItems(MekanismItems.TIME_DILATING_QIO_DRIVE))
              .save(consumer, fileHelper);
        //TODO: Was obfuscated for description. Do we want it like that or to have the actual description?
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ULTIMATE_QIO_DRIVE)
              .display(MekanismItems.SUPERMASSIVE_QIO_DRIVE, FrameType.CHALLENGE)
              .addCriterion("ultimate_qio_drive", hasItems(MekanismItems.SUPERMASSIVE_QIO_DRIVE))
              .save(consumer, fileHelper);
    }

    private void generateQIODashboards(@Nonnull Consumer<Advancement> consumer) {
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.QIO_DASHBOARD)
              .display(MekanismBlocks.QIO_DASHBOARD, FrameType.TASK)
              .addCriterion("qio_dashboard", hasItems(MekanismBlocks.QIO_DASHBOARD))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.PORTABLE_QIO_DASHBOARD)
              .display(MekanismItems.PORTABLE_QIO_DASHBOARD, FrameType.GOAL)
              .addCriterion("portable_qio_dashboard", hasItems(MekanismItems.PORTABLE_QIO_DASHBOARD))
              .save(consumer, fileHelper);
    }

    private void generateTeleports(@Nonnull Consumer<Advancement> consumer) {
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.TELEPORTATION_CORE)
              .display(MekanismItems.TELEPORTATION_CORE, FrameType.TASK)
              .addCriterion("teleportation_core", hasItems(MekanismItems.TELEPORTATION_CORE))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.TELEPORTER)
              .display(MekanismBlocks.TELEPORTER, FrameType.TASK)
              .addCriterion("teleporter", hasItems(MekanismBlocks.TELEPORTER))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.PORTABLE_TELEPORTER)
              .display(MekanismItems.PORTABLE_TELEPORTER, FrameType.TASK)
              .addCriterion("portable_teleporter", hasItems(MekanismItems.PORTABLE_TELEPORTER))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.QUANTUM_ENTANGLOPORTER)
              .display(MekanismBlocks.QUANTUM_ENTANGLOPORTER, FrameType.TASK)
              .addCriterion("quantum_entangloporter", hasItems(MekanismBlocks.QUANTUM_ENTANGLOPORTER))
              .save(consumer, fileHelper);
    }

    private void generateControls(@Nonnull Consumer<Advancement> consumer) {
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.BASIC_CONTROL)
              .display(MekanismItems.BASIC_CONTROL_CIRCUIT, FrameType.TASK)
              .addCriterion("basic_control", hasItems(MekanismItems.BASIC_CONTROL_CIRCUIT))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ADVANCED_CONTROL)
              .display(MekanismItems.ADVANCED_CONTROL_CIRCUIT, FrameType.TASK)
              .addCriterion("advanced_control", hasItems(MekanismItems.ADVANCED_CONTROL_CIRCUIT))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ELITE_CONTROL)
              .display(MekanismItems.ELITE_CONTROL_CIRCUIT, FrameType.TASK)
              .addCriterion("elite_control", hasItems(MekanismItems.ELITE_CONTROL_CIRCUIT))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ULTIMATE_CONTROL)
              .display(MekanismItems.ULTIMATE_CONTROL_CIRCUIT, FrameType.GOAL)
              .addCriterion("ultimate_control", hasItems(MekanismItems.ULTIMATE_CONTROL_CIRCUIT))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ROBIT)
              .display(MekanismItems.ROBIT, FrameType.GOAL)
              .addCriterion("robit", hasItems(MekanismItems.ROBIT))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.DIGITAL_MINER)
              .display(MekanismBlocks.DIGITAL_MINER, FrameType.GOAL)
              .addCriterion("digital_miner", hasItems(MekanismBlocks.DIGITAL_MINER))
              .save(consumer, fileHelper);
    }

    private void generateDisassembly(@Nonnull Consumer<Advancement> consumer) {
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.DISASSEMBLER)
              .display(MekanismItems.ATOMIC_DISASSEMBLER, FrameType.TASK)
              .addCriterion("disassembler", hasItems(MekanismItems.ATOMIC_DISASSEMBLER))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.MEKASUIT)
              .display(MekanismItems.MEKASUIT_BODYARMOR, FrameType.GOAL)
              .addCriterion("helmet", hasItems(MekanismItems.MEKASUIT_HELMET))
              .addCriterion("bodyarmor", hasItems(MekanismItems.MEKASUIT_BODYARMOR))
              .addCriterion("pants", hasItems(MekanismItems.MEKASUIT_PANTS))
              .addCriterion("boots", hasItems(MekanismItems.MEKASUIT_BOOTS))
              .addCriterion("tool", hasItems(MekanismItems.MEKA_TOOL))
              .save(consumer, fileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.UPGRADED_MEKASUIT)
              .display(MekanismItems.MEKASUIT_BODYARMOR, null, FrameType.CHALLENGE, true, true, true)
              .addCriterion("helmet", hasMaxed(MekanismItems.MEKASUIT_HELMET))
              .addCriterion("bodyarmor", hasMaxed(MekanismItems.MEKASUIT_BODYARMOR))
              .addCriterion("pants", hasMaxed(MekanismItems.MEKASUIT_PANTS))
              .addCriterion("boots", hasMaxed(MekanismItems.MEKASUIT_BOOTS))
              .addCriterion("tool", hasMaxed(MekanismItems.MEKA_TOOL))
              .save(consumer, fileHelper);
    }
}