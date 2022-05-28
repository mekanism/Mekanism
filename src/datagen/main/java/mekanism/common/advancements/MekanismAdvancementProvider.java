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
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.TickTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class MekanismAdvancementProvider extends BaseAdvancementProvider {

    public MekanismAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper, Mekanism.MODID);
    }

    @Override
    protected void registerAdvancements(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper existingFileHelper) {
        Advancement root = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ROOT)
              .display(MekanismBlocks.STEEL_BLOCK, Mekanism.rl("textures/block/block_osmium.png"), FrameType.GOAL, false, false, false)
              .addCriterion("tick", new TickTrigger.TriggerInstance(EntityPredicate.Composite.ANY))
              .save(consumer, existingFileHelper);
        Advancement materials = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.MATERIALS)
              .parent(root)
              .display(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM), FrameType.TASK)
              .orRequirements()
              .addCriterion("osmium", hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)))
              .addCriterion("tin", hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN)))
              .addCriterion("lead", hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD)))
              .addCriterion("uranium", hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.URANIUM)))
              .addCriterion("fluorite", hasItems(MekanismItems.FLUORITE_GEM))
              .save(consumer, existingFileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.FLUID_TANK)
              .parent(root)
              .display(MekanismBlocks.BASIC_FLUID_TANK, FrameType.TASK)
              .addCriterion("fluid_tank", hasItems(MekanismBlocks.BASIC_FLUID_TANK))
              .save(consumer, existingFileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.CHEMICAL_TANK)
              .parent(root)
              .display(MekanismBlocks.BASIC_CHEMICAL_TANK, FrameType.TASK)
              .addCriterion("chemical_tank", hasItems(MekanismBlocks.BASIC_CHEMICAL_TANK))
              .save(consumer, existingFileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.FULL_CANTEEN)
              .parent(materials)
              .display(MekanismItems.CANTEEN, null, FrameType.TASK, true, true, true)
              .addCriterion("full_canteen", InventoryChangeTrigger.TriggerInstance.hasItems(FullCanteenItemPredicate.INSTANCE))
              .save(consumer, existingFileHelper);
        generateMetallurgy(consumer, existingFileHelper, materials);
        generateDisassembly(consumer, existingFileHelper, materials);
    }

    private void generateMetallurgy(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper existingFileHelper, Advancement materials) {
        Advancement infuser = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.METALLURGIC_INFUSER)
              .parent(materials)
              .display(MekanismBlocks.METALLURGIC_INFUSER, FrameType.TASK)
              .addCriterion("metallurgic_infuser", hasItems(MekanismBlocks.METALLURGIC_INFUSER))
              .save(consumer, existingFileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.STEEL_INGOT)
              .parent(infuser)
              .display(MekanismItems.STEEL_INGOT, FrameType.TASK)
              .addCriterion("steel_ingot", hasItems(MekanismItems.STEEL_INGOT))
              .save(consumer, existingFileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.STEEL_CASING)
              .parent(infuser)
              .display(MekanismBlocks.STEEL_CASING, FrameType.TASK)
              .addCriterion("steel_casing", hasItems(MekanismBlocks.STEEL_CASING))
              .save(consumer, existingFileHelper);
        generateAlloys(consumer, existingFileHelper, infuser);
        generateControls(consumer, existingFileHelper, infuser);
    }

    private void generateAlloys(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper existingFileHelper, Advancement infuser) {
        Advancement infusedAlloy = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.INFUSED_ALLOY)
              .parent(infuser)
              .display(MekanismItems.INFUSED_ALLOY, FrameType.TASK)
              .addCriterion("infused_alloy", hasItems(MekanismItems.INFUSED_ALLOY))
              .save(consumer, existingFileHelper);
        Advancement reinforcedAlloy = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.REINFORCED_ALLOY)
              .parent(infusedAlloy)
              .display(MekanismItems.REINFORCED_ALLOY, FrameType.TASK)
              .addCriterion("reinforced_alloy", hasItems(MekanismItems.REINFORCED_ALLOY))
              .save(consumer, existingFileHelper);
        Advancement atomicAlloy = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ATOMIC_ALLOY)
              .parent(reinforcedAlloy)
              .display(MekanismItems.ATOMIC_ALLOY, FrameType.GOAL)
              .addCriterion("atomic_alloy", hasItems(MekanismItems.ATOMIC_ALLOY))
              .save(consumer, existingFileHelper);
        generateSPS(consumer, existingFileHelper, atomicAlloy);
        generateQIO(consumer, existingFileHelper, atomicAlloy);
        generateTeleports(consumer, existingFileHelper, atomicAlloy);
    }

    private void generateSPS(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper existingFileHelper, Advancement atomicAlloy) {
        Advancement plutonium = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.PLUTONIUM)
              .parent(atomicAlloy)
              .display(MekanismItems.PLUTONIUM_PELLET, FrameType.TASK)
              .addCriterion("plutonium", hasItems(MekanismItems.PLUTONIUM_PELLET))
              .save(consumer, existingFileHelper);
        Advancement sps = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.SPS)
              .parent(plutonium)
              .display(MekanismBlocks.SPS_CASING, FrameType.TASK)
              .orRequirements()
              .addCriterion("sps_casing", hasItems(MekanismBlocks.SPS_CASING))
              .addCriterion("sps_port", hasItems(MekanismBlocks.SPS_PORT))
              .save(consumer, existingFileHelper);
        //TODO: Was obfuscated for description. Do we want it like that or to have the actual description?
        Advancement antimatter = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ANTIMATTER)
              .parent(sps)
              .display(MekanismItems.ANTIMATTER_PELLET, FrameType.TASK)
              .addCriterion("antimatter", hasItems(MekanismItems.ANTIMATTER_PELLET))
              .save(consumer, existingFileHelper);
        //TODO: Was obfuscated for description. Do we want it like that or to have the actual description?
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.NUCLEOSYNTHESIZER)
              .parent(antimatter)
              .display(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, FrameType.CHALLENGE)
              .addCriterion("nucleosynthesizer", hasItems(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER))
              .save(consumer, existingFileHelper);
    }

    private void generateQIO(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper existingFileHelper, Advancement atomicAlloy) {
        Advancement polonium = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.POLONIUM)
              .parent(atomicAlloy)
              .display(MekanismItems.POLONIUM_PELLET, FrameType.TASK)
              .addCriterion("polonium", hasItems(MekanismItems.POLONIUM_PELLET))
              .save(consumer, existingFileHelper);
        Advancement driveArray = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.QIO_DRIVE_ARRAY)
              .parent(polonium)
              .display(MekanismBlocks.QIO_DRIVE_ARRAY, FrameType.TASK)
              .addCriterion("qio_drive_array", hasItems(MekanismBlocks.QIO_DRIVE_ARRAY))
              .save(consumer, existingFileHelper);
        generateQIODrives(consumer, existingFileHelper, driveArray);
        generateQIODashboards(consumer, existingFileHelper, driveArray);
    }

    private void generateQIODrives(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper existingFileHelper, Advancement driveArray) {
        Advancement basicQIODrive = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.BASIC_QIO_DRIVE)
              .parent(driveArray)
              .display(MekanismItems.BASE_QIO_DRIVE, FrameType.TASK)
              .addCriterion("basic_qio_drive", hasItems(MekanismItems.BASE_QIO_DRIVE))
              .save(consumer, existingFileHelper);
        Advancement advancedQIODrive = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ADVANCED_QIO_DRIVE)
              .parent(basicQIODrive)
              .display(MekanismItems.HYPER_DENSE_QIO_DRIVE, FrameType.TASK)
              .addCriterion("advanced_qio_drive", hasItems(MekanismItems.HYPER_DENSE_QIO_DRIVE))
              .save(consumer, existingFileHelper);
        Advancement eliteQIODrive = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ELITE_QIO_DRIVE)
              .parent(advancedQIODrive)
              .display(MekanismItems.TIME_DILATING_QIO_DRIVE, FrameType.TASK)
              .addCriterion("elite_qio_drive", hasItems(MekanismItems.TIME_DILATING_QIO_DRIVE))
              .save(consumer, existingFileHelper);
        //TODO: Was obfuscated for description. Do we want it like that or to have the actual description?
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ULTIMATE_QIO_DRIVE)
              .parent(eliteQIODrive)
              .display(MekanismItems.SUPERMASSIVE_QIO_DRIVE, FrameType.CHALLENGE)
              .addCriterion("ultimate_qio_drive", hasItems(MekanismItems.SUPERMASSIVE_QIO_DRIVE))
              .save(consumer, existingFileHelper);
    }

    private void generateQIODashboards(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper existingFileHelper, Advancement qiodrivearray) {
        Advancement dashboard = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.QIO_DASHBOARD)
              .parent(qiodrivearray)
              .display(MekanismBlocks.QIO_DASHBOARD, FrameType.TASK)
              .addCriterion("qio_dashboard", hasItems(MekanismBlocks.QIO_DASHBOARD))
              .save(consumer, existingFileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.PORTABLE_QIO_DASHBOARD)
              .parent(dashboard)
              .display(MekanismItems.PORTABLE_QIO_DASHBOARD, FrameType.GOAL)
              .addCriterion("portable_qio_dashboard", hasItems(MekanismItems.PORTABLE_QIO_DASHBOARD))
              .save(consumer, existingFileHelper);
    }

    private void generateTeleports(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper existingFileHelper, Advancement atomicAlloy) {
        Advancement teleportationCore = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.TELEPORTATION_CORE)
              .parent(atomicAlloy)
              .display(MekanismItems.TELEPORTATION_CORE, FrameType.TASK)
              .addCriterion("teleportation_core", hasItems(MekanismItems.TELEPORTATION_CORE))
              .save(consumer, existingFileHelper);
        Advancement teleporter = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.TELEPORTER)
              .parent(teleportationCore)
              .display(MekanismBlocks.TELEPORTER, FrameType.TASK)
              .addCriterion("teleporter", hasItems(MekanismBlocks.TELEPORTER))
              .save(consumer, existingFileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.PORTABLE_TELEPORTER)
              .parent(teleporter)
              .display(MekanismItems.PORTABLE_TELEPORTER, FrameType.TASK)
              .addCriterion("portable_teleporter", hasItems(MekanismItems.PORTABLE_TELEPORTER))
              .save(consumer, existingFileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.QUANTUM_ENTANGLOPORTER)
              .parent(teleportationCore)
              .display(MekanismBlocks.QUANTUM_ENTANGLOPORTER, FrameType.TASK)
              .addCriterion("quantum_entangloporter", hasItems(MekanismBlocks.QUANTUM_ENTANGLOPORTER))
              .save(consumer, existingFileHelper);
    }

    private void generateControls(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper existingFileHelper, Advancement infuser) {
        Advancement basicControlCircuit = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.BASIC_CONTROL)
              .parent(infuser)
              .display(MekanismItems.BASIC_CONTROL_CIRCUIT, FrameType.TASK)
              .addCriterion("basic_control", hasItems(MekanismItems.BASIC_CONTROL_CIRCUIT))
              .save(consumer, existingFileHelper);
        Advancement advancedControlCircuit = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ADVANCED_CONTROL)
              .parent(basicControlCircuit)
              .display(MekanismItems.ADVANCED_CONTROL_CIRCUIT, FrameType.TASK)
              .addCriterion("advanced_control", hasItems(MekanismItems.ADVANCED_CONTROL_CIRCUIT))
              .save(consumer, existingFileHelper);
        Advancement eliteControlCircuit = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ELITE_CONTROL)
              .parent(advancedControlCircuit)
              .display(MekanismItems.ELITE_CONTROL_CIRCUIT, FrameType.TASK)
              .addCriterion("elite_control", hasItems(MekanismItems.ELITE_CONTROL_CIRCUIT))
              .save(consumer, existingFileHelper);
        Advancement ultimateControlCircuit = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ULTIMATE_CONTROL)
              .parent(eliteControlCircuit)
              .display(MekanismItems.ULTIMATE_CONTROL_CIRCUIT, FrameType.GOAL)
              .addCriterion("ultimate_control", hasItems(MekanismItems.ULTIMATE_CONTROL_CIRCUIT))
              .save(consumer, existingFileHelper);
        Advancement robit = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.ROBIT)
              .parent(ultimateControlCircuit)
              .display(MekanismItems.ROBIT, FrameType.GOAL)
              .addCriterion("robit", hasItems(MekanismItems.ROBIT))
              .save(consumer, existingFileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.DIGITAL_MINER)
              .parent(robit)
              .display(MekanismBlocks.DIGITAL_MINER, FrameType.GOAL)
              .addCriterion("digital_miner", hasItems(MekanismBlocks.DIGITAL_MINER))
              .save(consumer, existingFileHelper);
    }

    private void generateDisassembly(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper existingFileHelper, Advancement materials) {
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.DISASSEMBLER)
              .parent(materials)
              .display(MekanismItems.ATOMIC_DISASSEMBLER, FrameType.TASK)
              .addCriterion("disassembler", hasItems(MekanismItems.ATOMIC_DISASSEMBLER))
              .save(consumer, existingFileHelper);
        Advancement mekasuit = ExtendedAdvancementBuilder.advancement(MekanismAdvancements.MEKASUIT)
              .parent(materials)
              .display(MekanismItems.MEKASUIT_BODYARMOR, FrameType.GOAL)
              .addCriterion("helmet", hasItems(MekanismItems.MEKASUIT_HELMET))
              .addCriterion("bodyarmor", hasItems(MekanismItems.MEKASUIT_BODYARMOR))
              .addCriterion("pants", hasItems(MekanismItems.MEKASUIT_PANTS))
              .addCriterion("boots", hasItems(MekanismItems.MEKASUIT_BOOTS))
              .addCriterion("tool", hasItems(MekanismItems.MEKA_TOOL))
              .save(consumer, existingFileHelper);
        ExtendedAdvancementBuilder.advancement(MekanismAdvancements.UPGRADED_MEKASUIT)
              .parent(mekasuit)
              .display(MekanismItems.MEKASUIT_BODYARMOR, null, FrameType.CHALLENGE, true, true, true)
              .addCriterion("helmet", hasMaxed(MekanismItems.MEKASUIT_HELMET))
              .addCriterion("bodyarmor", hasMaxed(MekanismItems.MEKASUIT_BODYARMOR))
              .addCriterion("pants", hasMaxed(MekanismItems.MEKASUIT_PANTS))
              .addCriterion("boots", hasMaxed(MekanismItems.MEKASUIT_BOOTS))
              .addCriterion("tool", hasMaxed(MekanismItems.MEKA_TOOL))
              .save(consumer, existingFileHelper);
    }
}