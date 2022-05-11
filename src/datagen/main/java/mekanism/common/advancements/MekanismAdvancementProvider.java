package mekanism.common.advancements;

import mekanism.common.Mekanism;
import net.minecraft.advancements.Advancement;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.ResourceType;
import mekanism.common.resource.PrimaryResource;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.TickTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

public class MekanismAdvancementProvider extends baseAdvancementProvider{
    public MekanismAdvancementProvider(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, helper, Mekanism.MODID);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {
        Advancement root = Advancement.Builder.advancement()
                .display(MekanismBlocks.STEEL_BLOCK, super.title("root"), super.description("root"), new ResourceLocation(Mekanism.MODID, "textures/block/block_osmium.png"), FrameType.GOAL, false, false, false)
                .addCriterion("tick", new TickTrigger.TriggerInstance(EntityPredicate.Composite.ANY))
                .save(consumer, super.advancementLocation("root"));
        Advancement materials = Advancement.Builder.advancement()
                .parent(root)
                .display(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM), super.title("materials"),super.description("materials"), null, FrameType.TASK, true, true, false)
                .requirements(RequirementsStrategy.OR)
                .addCriterion("osmium", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)))
                .addCriterion("tin", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN)))
                .addCriterion("lead", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD)))
                .addCriterion("uranium", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.URANIUM)))
                .addCriterion("fluorite", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.FLUORITE_GEM.get()))
                .save(consumer, super.advancementLocation("materials"));
        Advancement fluidtank = Advancement.Builder.advancement()
                .parent(root)
                .display(MekanismBlocks.BASIC_FLUID_TANK,super.title("fluidtank"),super.description("fluidtank"), null, FrameType.TASK, true, true, false)
                .addCriterion("fluidtank", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.BASIC_FLUID_TANK.asItem()))
                .save(consumer, super.advancementLocation("fluidtank"));
        Advancement chemicaltank = Advancement.Builder.advancement()
                .parent(root)
                .display(MekanismBlocks.BASIC_CHEMICAL_TANK,super.title("chemicaltank"),super.description("chemicaltank"), null, FrameType.TASK, true, true, false)
                .addCriterion("chemicaltank", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.BASIC_CHEMICAL_TANK.asItem()))
                .save(consumer, super.advancementLocation("chemicaltank"));
        Advancement fullcanteen = Advancement.Builder.advancement()
                .parent(materials)
                .display(MekanismItems.CANTEEN,super.title("fullcanteen"),super.description("fullcanteen"), null, FrameType.TASK, true, true, true)
                .addCriterion("fullcanteen", InventoryChangeTrigger.TriggerInstance.hasItems(super.fullCanteen()))
                .save(consumer, super.advancementLocation("fullcanteen"));
        generateMetallurgy(consumer, materials);
        generateDisassembly(consumer, materials);
    }

    protected void generateMetallurgy(Consumer<Advancement> consumer, Advancement materials) {
        Advancement metallurgicinfuser = Advancement.Builder.advancement()
                .parent(materials)
                .display(MekanismBlocks.METALLURGIC_INFUSER,super.title("metallurgicinfuser"),super.description("metallurgicinfuser"), null, FrameType.TASK, true, true, false)
                .addCriterion("metallurgicinfuser", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.METALLURGIC_INFUSER.asItem()))
                .save(consumer, super.advancementLocation("metallurgicinfuser"));
        Advancement steelingot = Advancement.Builder.advancement()
                .parent(metallurgicinfuser)
                .display(MekanismItems.STEEL_INGOT,super.title("steelingot"),super.description("steelingot"), null, FrameType.TASK, true, true, false)
                .addCriterion("steelingot", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.STEEL_INGOT.get()))
                .save(consumer, super.advancementLocation("steelingot"));
        Advancement steelcasing = Advancement.Builder.advancement()
                .parent(metallurgicinfuser)
                .display(MekanismBlocks.STEEL_CASING,super.title("steelcasing"),super.description("steelcasing"), null, FrameType.TASK, true, true, false)
                .addCriterion("steelcasing", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.STEEL_CASING.asItem()))
                .save(consumer, super.advancementLocation("steelcasing"));
        generateAlloys(consumer, metallurgicinfuser);
        generateControls(consumer, metallurgicinfuser);
    }

    protected void generateAlloys(Consumer<Advancement> consumer, Advancement metallurgicinfuser) {
        Advancement infused_alloy = Advancement.Builder.advancement()
                .parent(metallurgicinfuser)
                .display(MekanismItems.INFUSED_ALLOY,super.title("infused_alloy"),super.description("infused_alloy"), null, FrameType.TASK, true, true, false)
                .addCriterion("infused_alloy", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.INFUSED_ALLOY.get()))
                .save(consumer, super.advancementLocation("infused_alloy"));
        Advancement reinforced_alloy = Advancement.Builder.advancement()
                .parent(infused_alloy)
                .display(MekanismItems.REINFORCED_ALLOY,super.title("reinforced_alloy"),super.description("reinforced_alloy"), null, FrameType.TASK, true, true, false)
                .addCriterion("reinforced_alloy", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.REINFORCED_ALLOY.get()))
                .save(consumer, super.advancementLocation("reinforced_alloy"));
        Advancement atomic_alloy = Advancement.Builder.advancement()
                .parent(reinforced_alloy)
                .display(MekanismItems.ATOMIC_ALLOY,super.title("atomic_alloy"),super.description("atomic_alloy"), null, FrameType.GOAL, true, true, false)
                .addCriterion("atomic_alloy", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.ATOMIC_ALLOY.get()))
                .save(consumer, super.advancementLocation("atomic_alloy"));
        generateSPS(consumer, atomic_alloy);
        generateQIO(consumer, atomic_alloy);
        generateTeleports(consumer, atomic_alloy);
    }

    protected void generateSPS(Consumer<Advancement> consumer, Advancement atomic_alloy) {
        Advancement plutonium = Advancement.Builder.advancement()
                .parent(atomic_alloy)
                .display(MekanismItems.PLUTONIUM_PELLET,super.title("plutonium"),super.description("plutonium"), null, FrameType.TASK, true, true, false)
                .addCriterion("plutonium", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.PLUTONIUM_PELLET.get()))
                .save(consumer, super.advancementLocation("plutonium"));
        Advancement sps = Advancement.Builder.advancement()
                .parent(plutonium)
                .display(MekanismBlocks.SPS_CASING,super.title("sps"),super.description("sps"), null, FrameType.TASK, true, true, false)
                .requirements(RequirementsStrategy.OR)
                .addCriterion("sps_casing", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.SPS_CASING.asItem()))
                .addCriterion("sps_port", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.SPS_PORT.asItem()))
                .save(consumer, super.advancementLocation("sps"));
        Advancement antimatter = Advancement.Builder.advancement()
                .parent(sps)
                .display(MekanismItems.ANTIMATTER_PELLET,super.title("antimatter"), super.obfuscatedDescription("antimatter"), null, FrameType.TASK, true, true, false)
                .addCriterion("antimatter", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.ANTIMATTER_PELLET.get()))
                .save(consumer, super.advancementLocation("antimatter"));
        Advancement nucleosynthesizer = Advancement.Builder.advancement()
                .parent(antimatter)
                .display(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER,super.title("nucleosynthesizer"), super.obfuscatedDescription("nucleosynthesizer"), null, FrameType.CHALLENGE, true, true, false)
                .addCriterion("nucleosynthesizer", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER.asItem()))
                .save(consumer, super.advancementLocation("nucleosynthesizer"));
    }

    protected void generateQIO(Consumer<Advancement> consumer, Advancement atomic_alloy) {
        Advancement polonium = Advancement.Builder.advancement()
                .parent(atomic_alloy)
                .display(MekanismItems.POLONIUM_PELLET,super.title("polonium"),super.description("polonium"), null, FrameType.TASK, true, true, false)
                .addCriterion("polonium", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.POLONIUM_PELLET.get()))
                .save(consumer, super.advancementLocation("polonium"));
        Advancement qiodrivearray = Advancement.Builder.advancement()
                .parent(polonium)
                .display(MekanismBlocks.QIO_DRIVE_ARRAY,super.title("qiodrivearray"),super.description("qiodrivearray"), null, FrameType.TASK, true, true, false)
                .addCriterion("qiodrivearray", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.QIO_DRIVE_ARRAY.asItem()))
                .save(consumer, super.advancementLocation("qiodrivearray"));
        generateQIODrives(consumer, qiodrivearray);
        generateQIODashboards(consumer, qiodrivearray);
    }

    protected void generateQIODrives(Consumer<Advancement> consumer, Advancement qiodrivearray) {
        Advancement basicqio = Advancement.Builder.advancement()
                .parent(qiodrivearray)
                .display(MekanismItems.BASE_QIO_DRIVE,super.title("basicqiodrive"),super.description("basicqiodrive"), null, FrameType.TASK, true, true, false)
                .addCriterion("basicqiodrive", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.BASE_QIO_DRIVE.get()))
                .save(consumer, super.advancementLocation("basicqiodrive"));
        Advancement advancedqio = Advancement.Builder.advancement()
                .parent(basicqio)
                .display(MekanismItems.HYPER_DENSE_QIO_DRIVE,super.title("advancedqiodrive"),super.description("advancedqiodrive"), null, FrameType.TASK, true, true, false)
                .addCriterion("advancedqiodrive", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.HYPER_DENSE_QIO_DRIVE.get()))
                .save(consumer, super.advancementLocation("advancedqiodrive"));
        Advancement eliteqio = Advancement.Builder.advancement()
                .parent(advancedqio)
                .display(MekanismItems.TIME_DILATING_QIO_DRIVE,super.title("eliteqiodrive"),super.description("eliteqiodrive"), null, FrameType.TASK, true, true, false)
                .addCriterion("eliteqiodrive", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.TIME_DILATING_QIO_DRIVE.get()))
                .save(consumer, super.advancementLocation("eliteqiodrive"));
        Advancement ultimateqio = Advancement.Builder.advancement()
                .parent(eliteqio)
                .display(MekanismItems.SUPERMASSIVE_QIO_DRIVE,super.title("ultimateqiodrive"), super.obfuscatedDescription("ultimateqiodrive"), null, FrameType.CHALLENGE, true, true, false)
                .addCriterion("ultimateqiodrive", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.SUPERMASSIVE_QIO_DRIVE.get()))
                .save(consumer, super.advancementLocation("ultimateqiodrive"));
    }

    protected void generateQIODashboards(Consumer<Advancement> consumer, Advancement qiodrivearray) {
        Advancement dashboard = Advancement.Builder.advancement()
                .parent(qiodrivearray)
                .display(MekanismBlocks.QIO_DASHBOARD,super.title("qiodashboard"),super.description("qiodashboard"), null, FrameType.TASK, true, true, false)
                .addCriterion("qiodashboard", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.QIO_DASHBOARD.asItem()))
                .save(consumer, super.advancementLocation("qiodashboard"));
        Advancement portabledashboard = Advancement.Builder.advancement()
                .parent(dashboard)
                .display(MekanismItems.PORTABLE_QIO_DASHBOARD,super.title("portableqiodashboard"),super.description("portableqiodashboard"), null, FrameType.GOAL, true, true, false)
                .addCriterion("portableqiodashboard", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.PORTABLE_QIO_DASHBOARD.get()))
                .save(consumer, super.advancementLocation("portableqiodashboard"));
    }

    protected void generateTeleports(Consumer<Advancement> consumer, Advancement atomic_alloy) {
        Advancement teleportcore = Advancement.Builder.advancement()
                .parent(atomic_alloy)
                .display(MekanismItems.TELEPORTATION_CORE,super.title("teleportcore"),super.description("teleportcore"), null, FrameType.TASK, true, true, false)
                .addCriterion("teleportcore", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.TELEPORTATION_CORE.get()))
                .save(consumer, super.advancementLocation("teleportcore"));
        Advancement teleporter = Advancement.Builder.advancement()
                .parent(teleportcore)
                .display(MekanismBlocks.TELEPORTER,super.title("teleporter"),super.description("teleporter"), null, FrameType.TASK, true, true, false)
                .addCriterion("teleporter", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.TELEPORTER.asItem()))
                .save(consumer, super.advancementLocation("teleporter"));
        Advancement portableteleporter = Advancement.Builder.advancement()
                .parent(teleporter)
                .display(MekanismItems.PORTABLE_TELEPORTER,super.title("portableteleporter"),super.description("portableteleporter"), null, FrameType.TASK, true, true, false)
                .addCriterion("portableteleporter", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.PORTABLE_TELEPORTER.get()))
                .save(consumer, super.advancementLocation("portableteleporter"));
        Advancement quantumentangloporter = Advancement.Builder.advancement()
                .parent(teleportcore)
                .display(MekanismBlocks.QUANTUM_ENTANGLOPORTER,super.title("quantumentangloporter"),super.description("quantumentangloporter"), null, FrameType.TASK, true, true, false)
                .addCriterion("quantumentangloporter", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.QUANTUM_ENTANGLOPORTER.asItem()))
                .save(consumer, super.advancementLocation("quantumentangloporter"));
    }

    protected void generateControls(Consumer<Advancement> consumer, Advancement metallurgicinfuser) {
        Advancement basic_control = Advancement.Builder.advancement()
                .parent(metallurgicinfuser)
                .display(MekanismItems.BASIC_CONTROL_CIRCUIT, super.title("basic_control"), super.description("basic_control"), null, FrameType.TASK, true, true, false)
                .addCriterion("basic_control", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.BASIC_CONTROL_CIRCUIT.get()))
                .save(consumer, super.advancementLocation("control_basic"));
        Advancement advanced_control = Advancement.Builder.advancement()
                .parent(basic_control)
                .display(MekanismItems.ADVANCED_CONTROL_CIRCUIT,super.title("advanced_control"),super.description("advanced_control"), null, FrameType.TASK, true, true, false)
                .addCriterion("advanced_control", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.ADVANCED_CONTROL_CIRCUIT.get()))
                .save(consumer, super.advancementLocation("advanced_control"));
        Advancement elite_control = Advancement.Builder.advancement()
                .parent(advanced_control)
                .display(MekanismItems.ELITE_CONTROL_CIRCUIT,super.title("elite_control"),super.description("elite_control"), null, FrameType.TASK, true, true, false)
                .addCriterion("elite_control", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.ELITE_CONTROL_CIRCUIT.get()))
                .save(consumer, super.advancementLocation("elite_control"));
        Advancement ultimate_control = Advancement.Builder.advancement()
                .parent(elite_control)
                .display(MekanismItems.ULTIMATE_CONTROL_CIRCUIT,super.title("ultimate_control"),super.description("elite_control"), null, FrameType.GOAL, true, true, false)
                .addCriterion("elite_control", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.ULTIMATE_CONTROL_CIRCUIT.get()))
                .save(consumer, super.advancementLocation("ultimate_control"));
        Advancement robit = Advancement.Builder.advancement()
                .parent(ultimate_control)
                .display(MekanismItems.ROBIT,super.title("robit"),super.description("robit"), null, FrameType.GOAL, true, true, false)
                .addCriterion("elite_control", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.ROBIT.get()))
                .save(consumer, super.advancementLocation("robit"));
        Advancement digitalminer = Advancement.Builder.advancement()
                .parent(robit)
                .display(MekanismBlocks.DIGITAL_MINER.asItem(),super.title("robit"),super.description("robit"), null, FrameType.GOAL, true, true, false)
                .addCriterion("digitalminer", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.DIGITAL_MINER.asItem()))
                .save(consumer, super.advancementLocation("digitalminer"));
    }

    protected void generateDisassembly(Consumer<Advancement> consumer, Advancement materials) {
        Advancement disassembler = Advancement.Builder.advancement()
                .parent(materials)
                .display(MekanismItems.ATOMIC_DISASSEMBLER,super.title("disassembler"),super.description("disassembler"), null, FrameType.TASK, true, true, false)
                .addCriterion("disassembler", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.ATOMIC_DISASSEMBLER.get()))
                .save(consumer, super.advancementLocation("disassembler"));
        Advancement mekasuit = Advancement.Builder.advancement()
                .parent(materials)
                .display(MekanismItems.MEKASUIT_BODYARMOR,super.title("mekasuit"),super.description("mekasuit"), null, FrameType.GOAL, true, true, false)
                .requirements(RequirementsStrategy.AND)
                .addCriterion("helmet", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.MEKASUIT_HELMET.get()))
                .addCriterion("bodyarmor", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.MEKASUIT_BODYARMOR.get()))
                .addCriterion("pants", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.MEKASUIT_PANTS.get()))
                .addCriterion("boots", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.MEKASUIT_BOOTS.get()))
                .addCriterion("tool", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.MEKA_TOOL.get()))
                .save(consumer, super.advancementLocation("mekasuit"));
        Advancement upgradedmekasuit = Advancement.Builder.advancement()
                .parent(mekasuit)
                .display(MekanismItems.MEKASUIT_BODYARMOR,super.title("upgradedmekasuit"),super.description("upgradedmekasuit"), null, FrameType.CHALLENGE, true, true, true)
                .requirements(RequirementsStrategy.AND)
                .addCriterion("helmet", InventoryChangeTrigger.TriggerInstance.hasItems(super.maxedGear(MekanismItems.MEKASUIT_HELMET)))
                .addCriterion("bodyarmor", InventoryChangeTrigger.TriggerInstance.hasItems(super.maxedGear(MekanismItems.MEKASUIT_BODYARMOR)))
                .addCriterion("pants", InventoryChangeTrigger.TriggerInstance.hasItems(super.maxedGear(MekanismItems.MEKASUIT_PANTS)))
                .addCriterion("boots", InventoryChangeTrigger.TriggerInstance.hasItems(super.maxedGear(MekanismItems.MEKASUIT_BOOTS)))
                .addCriterion("tool", InventoryChangeTrigger.TriggerInstance.hasItems(super.maxedGear(MekanismItems.MEKA_TOOL)))
                .save(consumer, super.advancementLocation("upgradedmekasuit"));

    }
}
