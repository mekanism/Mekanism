package mekanism.common.advancements;

import javax.annotation.Nonnull;
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

public class MekanismAdvancementProvider extends BaseAdvancementProvider {

    public MekanismAdvancementProvider(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, helper, Mekanism.MODID);
    }

    @Override
    protected void registerAdvancements(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper fileHelper) {
        Advancement root = Advancement.Builder.advancement()
              .display(MekanismBlocks.STEEL_BLOCK, title("root"), description("root"), new ResourceLocation(Mekanism.MODID, "textures/block/block_osmium.png"), FrameType.GOAL, false, false, false)
              .addCriterion("tick", new TickTrigger.TriggerInstance(EntityPredicate.Composite.ANY))
              .save(consumer, advancementLocation("root"));
        Advancement materials = Advancement.Builder.advancement()
              .parent(root)
              .display(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM), title("materials"), description("materials"), null, FrameType.TASK, true, true, false)
              .requirements(RequirementsStrategy.OR)
              .addCriterion("osmium", hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)))
              .addCriterion("tin", hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN)))
              .addCriterion("lead", hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD)))
              .addCriterion("uranium", hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.URANIUM)))
              .addCriterion("fluorite", hasItems(MekanismItems.FLUORITE_GEM))
              .save(consumer, advancementLocation("materials"));
        Advancement fluidtank = Advancement.Builder.advancement()
              .parent(root)
              .display(MekanismBlocks.BASIC_FLUID_TANK, title("fluidtank"), description("fluidtank"), null, FrameType.TASK, true, true, false)
              .addCriterion("fluidtank", hasItems(MekanismBlocks.BASIC_FLUID_TANK.asItem()))
              .save(consumer, advancementLocation("fluidtank"));
        Advancement chemicaltank = Advancement.Builder.advancement()
              .parent(root)
              .display(MekanismBlocks.BASIC_CHEMICAL_TANK, title("chemicaltank"), description("chemicaltank"), null, FrameType.TASK, true, true, false)
              .addCriterion("chemicaltank", hasItems(MekanismBlocks.BASIC_CHEMICAL_TANK.asItem()))
              .save(consumer, advancementLocation("chemicaltank"));
        Advancement fullcanteen = Advancement.Builder.advancement()
              .parent(materials)
              .display(MekanismItems.CANTEEN, title("fullcanteen"), description("fullcanteen"), null, FrameType.TASK, true, true, true)
              .addCriterion("fullcanteen", InventoryChangeTrigger.TriggerInstance.hasItems(fullCanteen()))
              .save(consumer, advancementLocation("fullcanteen"));
        generateMetallurgy(consumer, materials);
        generateDisassembly(consumer, materials);
    }

    protected void generateMetallurgy(Consumer<Advancement> consumer, Advancement materials) {
        Advancement metallurgicinfuser = Advancement.Builder.advancement()
              .parent(materials)
              .display(MekanismBlocks.METALLURGIC_INFUSER, title("metallurgicinfuser"), description("metallurgicinfuser"), null, FrameType.TASK, true, true, false)
              .addCriterion("metallurgicinfuser", hasItems(MekanismBlocks.METALLURGIC_INFUSER.asItem()))
              .save(consumer, advancementLocation("metallurgicinfuser"));
        Advancement steelingot = Advancement.Builder.advancement()
              .parent(metallurgicinfuser)
              .display(MekanismItems.STEEL_INGOT, title("steelingot"), description("steelingot"), null, FrameType.TASK, true, true, false)
              .addCriterion("steelingot", hasItems(MekanismItems.STEEL_INGOT))
              .save(consumer, advancementLocation("steelingot"));
        Advancement steelcasing = Advancement.Builder.advancement()
              .parent(metallurgicinfuser)
              .display(MekanismBlocks.STEEL_CASING, title("steelcasing"), description("steelcasing"), null, FrameType.TASK, true, true, false)
              .addCriterion("steelcasing", hasItems(MekanismBlocks.STEEL_CASING.asItem()))
              .save(consumer, advancementLocation("steelcasing"));
        generateAlloys(consumer, metallurgicinfuser);
        generateControls(consumer, metallurgicinfuser);
    }

    protected void generateAlloys(Consumer<Advancement> consumer, Advancement metallurgicinfuser) {
        Advancement infused_alloy = Advancement.Builder.advancement()
              .parent(metallurgicinfuser)
              .display(MekanismItems.INFUSED_ALLOY, title("infused_alloy"), description("infused_alloy"), null, FrameType.TASK, true, true, false)
              .addCriterion("infused_alloy", hasItems(MekanismItems.INFUSED_ALLOY))
              .save(consumer, advancementLocation("infused_alloy"));
        Advancement reinforced_alloy = Advancement.Builder.advancement()
              .parent(infused_alloy)
              .display(MekanismItems.REINFORCED_ALLOY, title("reinforced_alloy"), description("reinforced_alloy"), null, FrameType.TASK, true, true, false)
              .addCriterion("reinforced_alloy", hasItems(MekanismItems.REINFORCED_ALLOY))
              .save(consumer, advancementLocation("reinforced_alloy"));
        Advancement atomic_alloy = Advancement.Builder.advancement()
              .parent(reinforced_alloy)
              .display(MekanismItems.ATOMIC_ALLOY, title("atomic_alloy"), description("atomic_alloy"), null, FrameType.GOAL, true, true, false)
              .addCriterion("atomic_alloy", hasItems(MekanismItems.ATOMIC_ALLOY))
              .save(consumer, advancementLocation("atomic_alloy"));
        generateSPS(consumer, atomic_alloy);
        generateQIO(consumer, atomic_alloy);
        generateTeleports(consumer, atomic_alloy);
    }

    protected void generateSPS(Consumer<Advancement> consumer, Advancement atomic_alloy) {
        Advancement plutonium = Advancement.Builder.advancement()
              .parent(atomic_alloy)
              .display(MekanismItems.PLUTONIUM_PELLET, title("plutonium"), description("plutonium"), null, FrameType.TASK, true, true, false)
              .addCriterion("plutonium", hasItems(MekanismItems.PLUTONIUM_PELLET))
              .save(consumer, advancementLocation("plutonium"));
        Advancement sps = Advancement.Builder.advancement()
              .parent(plutonium)
              .display(MekanismBlocks.SPS_CASING, title("sps"), description("sps"), null, FrameType.TASK, true, true, false)
              .requirements(RequirementsStrategy.OR)
              .addCriterion("sps_casing", hasItems(MekanismBlocks.SPS_CASING.asItem()))
              .addCriterion("sps_port", hasItems(MekanismBlocks.SPS_PORT.asItem()))
              .save(consumer, advancementLocation("sps"));
        Advancement antimatter = Advancement.Builder.advancement()
              .parent(sps)
              .display(MekanismItems.ANTIMATTER_PELLET, title("antimatter"), obfuscatedDescription("antimatter"), null, FrameType.TASK, true, true, false)
              .addCriterion("antimatter", hasItems(MekanismItems.ANTIMATTER_PELLET))
              .save(consumer, advancementLocation("antimatter"));
        Advancement nucleosynthesizer = Advancement.Builder.advancement()
              .parent(antimatter)
              .display(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, title("nucleosynthesizer"), obfuscatedDescription("nucleosynthesizer"), null, FrameType.CHALLENGE, true, true, false)
              .addCriterion("nucleosynthesizer", hasItems(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER.asItem()))
              .save(consumer, advancementLocation("nucleosynthesizer"));
    }

    protected void generateQIO(Consumer<Advancement> consumer, Advancement atomic_alloy) {
        Advancement polonium = Advancement.Builder.advancement()
              .parent(atomic_alloy)
              .display(MekanismItems.POLONIUM_PELLET, title("polonium"), description("polonium"), null, FrameType.TASK, true, true, false)
              .addCriterion("polonium", hasItems(MekanismItems.POLONIUM_PELLET))
              .save(consumer, advancementLocation("polonium"));
        Advancement qiodrivearray = Advancement.Builder.advancement()
              .parent(polonium)
              .display(MekanismBlocks.QIO_DRIVE_ARRAY, title("qiodrivearray"), description("qiodrivearray"), null, FrameType.TASK, true, true, false)
              .addCriterion("qiodrivearray", hasItems(MekanismBlocks.QIO_DRIVE_ARRAY.asItem()))
              .save(consumer, advancementLocation("qiodrivearray"));
        generateQIODrives(consumer, qiodrivearray);
        generateQIODashboards(consumer, qiodrivearray);
    }

    protected void generateQIODrives(Consumer<Advancement> consumer, Advancement qiodrivearray) {
        Advancement basicqio = Advancement.Builder.advancement()
              .parent(qiodrivearray)
              .display(MekanismItems.BASE_QIO_DRIVE, title("basicqiodrive"), description("basicqiodrive"), null, FrameType.TASK, true, true, false)
              .addCriterion("basicqiodrive", hasItems(MekanismItems.BASE_QIO_DRIVE))
              .save(consumer, advancementLocation("basicqiodrive"));
        Advancement advancedqio = Advancement.Builder.advancement()
              .parent(basicqio)
              .display(MekanismItems.HYPER_DENSE_QIO_DRIVE, title("advancedqiodrive"), description("advancedqiodrive"), null, FrameType.TASK, true, true, false)
              .addCriterion("advancedqiodrive", hasItems(MekanismItems.HYPER_DENSE_QIO_DRIVE))
              .save(consumer, advancementLocation("advancedqiodrive"));
        Advancement eliteqio = Advancement.Builder.advancement()
              .parent(advancedqio)
              .display(MekanismItems.TIME_DILATING_QIO_DRIVE, title("eliteqiodrive"), description("eliteqiodrive"), null, FrameType.TASK, true, true, false)
              .addCriterion("eliteqiodrive", hasItems(MekanismItems.TIME_DILATING_QIO_DRIVE))
              .save(consumer, advancementLocation("eliteqiodrive"));
        Advancement ultimateqio = Advancement.Builder.advancement()
              .parent(eliteqio)
              .display(MekanismItems.SUPERMASSIVE_QIO_DRIVE, title("ultimateqiodrive"), obfuscatedDescription("ultimateqiodrive"), null, FrameType.CHALLENGE, true, true, false)
              .addCriterion("ultimateqiodrive", hasItems(MekanismItems.SUPERMASSIVE_QIO_DRIVE))
              .save(consumer, advancementLocation("ultimateqiodrive"));
    }

    protected void generateQIODashboards(Consumer<Advancement> consumer, Advancement qiodrivearray) {
        Advancement dashboard = Advancement.Builder.advancement()
              .parent(qiodrivearray)
              .display(MekanismBlocks.QIO_DASHBOARD, title("qiodashboard"), description("qiodashboard"), null, FrameType.TASK, true, true, false)
              .addCriterion("qiodashboard", hasItems(MekanismBlocks.QIO_DASHBOARD.asItem()))
              .save(consumer, advancementLocation("qiodashboard"));
        Advancement portabledashboard = Advancement.Builder.advancement()
              .parent(dashboard)
              .display(MekanismItems.PORTABLE_QIO_DASHBOARD, title("portableqiodashboard"), description("portableqiodashboard"), null, FrameType.GOAL, true, true, false)
              .addCriterion("portableqiodashboard", hasItems(MekanismItems.PORTABLE_QIO_DASHBOARD))
              .save(consumer, advancementLocation("portableqiodashboard"));
    }

    protected void generateTeleports(Consumer<Advancement> consumer, Advancement atomic_alloy) {
        Advancement teleportcore = Advancement.Builder.advancement()
              .parent(atomic_alloy)
              .display(MekanismItems.TELEPORTATION_CORE, title("teleportcore"), description("teleportcore"), null, FrameType.TASK, true, true, false)
              .addCriterion("teleportcore", hasItems(MekanismItems.TELEPORTATION_CORE))
              .save(consumer, advancementLocation("teleportcore"));
        Advancement teleporter = Advancement.Builder.advancement()
              .parent(teleportcore)
              .display(MekanismBlocks.TELEPORTER, title("teleporter"), description("teleporter"), null, FrameType.TASK, true, true, false)
              .addCriterion("teleporter", hasItems(MekanismBlocks.TELEPORTER.asItem()))
              .save(consumer, advancementLocation("teleporter"));
        Advancement portableteleporter = Advancement.Builder.advancement()
              .parent(teleporter)
              .display(MekanismItems.PORTABLE_TELEPORTER, title("portableteleporter"), description("portableteleporter"), null, FrameType.TASK, true, true, false)
              .addCriterion("portableteleporter", hasItems(MekanismItems.PORTABLE_TELEPORTER))
              .save(consumer, advancementLocation("portableteleporter"));
        Advancement quantumentangloporter = Advancement.Builder.advancement()
              .parent(teleportcore)
              .display(MekanismBlocks.QUANTUM_ENTANGLOPORTER, title("quantumentangloporter"), description("quantumentangloporter"), null, FrameType.TASK, true, true, false)
              .addCriterion("quantumentangloporter", hasItems(MekanismBlocks.QUANTUM_ENTANGLOPORTER.asItem()))
              .save(consumer, advancementLocation("quantumentangloporter"));
    }

    protected void generateControls(Consumer<Advancement> consumer, Advancement metallurgicinfuser) {
        Advancement basic_control = Advancement.Builder.advancement()
              .parent(metallurgicinfuser)
              .display(MekanismItems.BASIC_CONTROL_CIRCUIT, title("basic_control"), description("basic_control"), null, FrameType.TASK, true, true, false)
              .addCriterion("basic_control", hasItems(MekanismItems.BASIC_CONTROL_CIRCUIT))
              .save(consumer, advancementLocation("control_basic"));
        Advancement advanced_control = Advancement.Builder.advancement()
              .parent(basic_control)
              .display(MekanismItems.ADVANCED_CONTROL_CIRCUIT, title("advanced_control"), description("advanced_control"), null, FrameType.TASK, true, true, false)
              .addCriterion("advanced_control", hasItems(MekanismItems.ADVANCED_CONTROL_CIRCUIT))
              .save(consumer, advancementLocation("advanced_control"));
        Advancement elite_control = Advancement.Builder.advancement()
              .parent(advanced_control)
              .display(MekanismItems.ELITE_CONTROL_CIRCUIT, title("elite_control"), description("elite_control"), null, FrameType.TASK, true, true, false)
              .addCriterion("elite_control", hasItems(MekanismItems.ELITE_CONTROL_CIRCUIT))
              .save(consumer, advancementLocation("elite_control"));
        Advancement ultimate_control = Advancement.Builder.advancement()
              .parent(elite_control)
              .display(MekanismItems.ULTIMATE_CONTROL_CIRCUIT, title("ultimate_control"), description("elite_control"), null, FrameType.GOAL, true, true, false)
              .addCriterion("elite_control", hasItems(MekanismItems.ULTIMATE_CONTROL_CIRCUIT))
              .save(consumer, advancementLocation("ultimate_control"));
        Advancement robit = Advancement.Builder.advancement()
              .parent(ultimate_control)
              .display(MekanismItems.ROBIT, title("robit"), description("robit"), null, FrameType.GOAL, true, true, false)
              .addCriterion("elite_control", hasItems(MekanismItems.ROBIT))
              .save(consumer, advancementLocation("robit"));
        Advancement digitalminer = Advancement.Builder.advancement()
              .parent(robit)
              .display(MekanismBlocks.DIGITAL_MINER.asItem(), title("robit"), description("robit"), null, FrameType.GOAL, true, true, false)
              .addCriterion("digitalminer", hasItems(MekanismBlocks.DIGITAL_MINER.asItem()))
              .save(consumer, advancementLocation("digitalminer"));
    }

    protected void generateDisassembly(Consumer<Advancement> consumer, Advancement materials) {
        Advancement disassembler = Advancement.Builder.advancement()
              .parent(materials)
              .display(MekanismItems.ATOMIC_DISASSEMBLER, title("disassembler"), description("disassembler"), null, FrameType.TASK, true, true, false)
              .addCriterion("disassembler", hasItems(MekanismItems.ATOMIC_DISASSEMBLER))
              .save(consumer, advancementLocation("disassembler"));
        Advancement mekasuit = Advancement.Builder.advancement()
              .parent(materials)
              .display(MekanismItems.MEKASUIT_BODYARMOR, title("mekasuit"), description("mekasuit"), null, FrameType.GOAL, true, true, false)
              .requirements(RequirementsStrategy.AND)
              .addCriterion("helmet", hasItems(MekanismItems.MEKASUIT_HELMET))
              .addCriterion("bodyarmor", hasItems(MekanismItems.MEKASUIT_BODYARMOR))
              .addCriterion("pants", hasItems(MekanismItems.MEKASUIT_PANTS))
              .addCriterion("boots", hasItems(MekanismItems.MEKASUIT_BOOTS))
              .addCriterion("tool", hasItems(MekanismItems.MEKA_TOOL))
              .save(consumer, advancementLocation("mekasuit"));
        Advancement upgradedmekasuit = Advancement.Builder.advancement()
              .parent(mekasuit)
              .display(MekanismItems.MEKASUIT_BODYARMOR, title("upgradedmekasuit"), description("upgradedmekasuit"), null, FrameType.CHALLENGE, true, true, true)
              .requirements(RequirementsStrategy.AND)
              .addCriterion("helmet", hasMaxed(MekanismItems.MEKASUIT_HELMET))
              .addCriterion("bodyarmor", hasMaxed(MekanismItems.MEKASUIT_BODYARMOR))
              .addCriterion("pants", hasMaxed(MekanismItems.MEKASUIT_PANTS))
              .addCriterion("boots", hasMaxed(MekanismItems.MEKASUIT_BOOTS))
              .addCriterion("tool", hasMaxed(MekanismItems.MEKA_TOOL))
              .save(consumer, advancementLocation("upgradedmekasuit"));

    }
}
