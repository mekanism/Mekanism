package mekanism.common.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mekanism.common.Mekanism;
import net.minecraft.advancements.Advancement;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.ResourceType;
import mekanism.common.resource.PrimaryResource;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.TickTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Set;
import java.util.function.Consumer;

public class MekanismAdvancementProvider extends baseAdvancementProvider{
    public MekanismAdvancementProvider(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, helper, Mekanism.MODID);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {
        Advancement root = Advancement.Builder.advancement()
                .display(MekanismBlocks.STEEL_BLOCK, title("root"), description("root"), new ResourceLocation(Mekanism.MODID, "textures/block/block_osmium.png"), FrameType.GOAL, false, false, false)
                .addCriterion("tick", new TickTrigger.TriggerInstance(EntityPredicate.Composite.ANY))
                .save(consumer, advancementLocation("root"));
        Advancement materials = Advancement.Builder.advancement()
                .parent(root)
                .display(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM), title("materials"), description("materials"), null, FrameType.TASK, true, true, false)
                .requirements(RequirementsStrategy.OR)
                .addCriterion("osmium", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)))
                .addCriterion("tin", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN)))
                .addCriterion("lead", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD)))
                .addCriterion("uranium", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.URANIUM)))
                .addCriterion("fluorite", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.FLUORITE_GEM.get()))
                .save(consumer, advancementLocation("materials"));
        Advancement fluidtank = Advancement.Builder.advancement()
                .parent(root)
                .display(MekanismBlocks.BASIC_FLUID_TANK, title("fluidtank"), description("fluidtank"), null, FrameType.TASK, true, true, false)
                .addCriterion("fluidtank", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.BASIC_FLUID_TANK.asItem()))
                .save(consumer, advancementLocation("fluidtank"));
        Advancement chemicaltank = Advancement.Builder.advancement()
                .parent(root)
                .display(MekanismBlocks.BASIC_CHEMICAL_TANK, title("chemicaltank"), description("chemicaltank"), null, FrameType.TASK, true, true, false)
                .addCriterion("chemicaltank", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.BASIC_CHEMICAL_TANK.asItem()))
                .save(consumer, advancementLocation("chemicaltank"));
        JsonElement canteenJson = JsonParser.parseString("{\"mekData\": {\"FluidTanks\": [{\"Tank\": 0b, \"stored\": {\"FluidName\": \"mekanism:nutritional_paste\", \"Amount\": 64000}}]}}");
        ItemPredicate canteen = new ItemPredicate(null, Set.of(MekanismItems.CANTEEN.get()), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, null, NbtPredicate.fromJson(canteenJson));
        Advancement fullcanteen = Advancement.Builder.advancement()
                .parent(materials)
                .display(MekanismItems.CANTEEN, title("fullcanteen"), description("fullcanteen"), null, FrameType.TASK, true, true, true)
                .addCriterion("fullcanteen", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.CANTEEN.get()))
                .save(consumer, advancementLocation("fullcanteen"));
        generateMetallurgy(consumer, materials);
        generateDisassembly(consumer, materials);
    }

    protected void generateMetallurgy(Consumer<Advancement> consumer, Advancement materials) {
        Advancement metallurgicinfuser = Advancement.Builder.advancement()
                .parent(materials)
                .display(MekanismBlocks.METALLURGIC_INFUSER, title("metallurgicinfuser"), description("metallurgicinfuser"), null, FrameType.TASK, true, true, false)
                .addCriterion("metallurgicinfuser", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.METALLURGIC_INFUSER.asItem()))
                .save(consumer, advancementLocation("metallurgicinfuser"));
        Advancement steelingot = Advancement.Builder.advancement()
                .parent(metallurgicinfuser)
                .display(MekanismItems.STEEL_INGOT, title("steelingot"), description("steelingot"), null, FrameType.TASK, true, true, false)
                .addCriterion("steelingot", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.STEEL_INGOT.get()))
                .save(consumer, advancementLocation("steelingot"));
        Advancement steelcasing = Advancement.Builder.advancement()
                .parent(metallurgicinfuser)
                .display(MekanismBlocks.STEEL_CASING, title("steelcasing"), description("steelcasing"), null, FrameType.TASK, true, true, false)
                .addCriterion("steelcasing", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.STEEL_CASING.asItem()))
                .save(consumer, advancementLocation("steelcasing"));
        generateAlloys(consumer, metallurgicinfuser);
        generateControls(consumer, metallurgicinfuser);
    }

    protected void generateAlloys(Consumer<Advancement> consumer, Advancement metallurgicinfuser) {
        Advancement infused_alloy = Advancement.Builder.advancement()
                .parent(metallurgicinfuser)
                .display(MekanismItems.INFUSED_ALLOY, title("infused_alloy"), description("infused_alloy"), null, FrameType.TASK, true, true, false)
                .addCriterion("infused_alloy", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.INFUSED_ALLOY.get()))
                .save(consumer, advancementLocation("infused_alloy"));
        Advancement reinforced_alloy = Advancement.Builder.advancement()
                .parent(infused_alloy)
                .display(MekanismItems.REINFORCED_ALLOY, title("reinforced_alloy"), description("reinforced_alloy"), null, FrameType.TASK, true, true, false)
                .addCriterion("reinforced_alloy", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.REINFORCED_ALLOY.get()))
                .save(consumer, advancementLocation("reinforced_alloy"));
        Advancement atomic_alloy = Advancement.Builder.advancement()
                .parent(reinforced_alloy)
                .display(MekanismItems.ATOMIC_ALLOY, title("atomic_alloy"), description("atomic_alloy"), null, FrameType.GOAL, true, true, false)
                .addCriterion("atomic_alloy", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.ATOMIC_ALLOY.get()))
                .save(consumer, advancementLocation("atomic_alloy"));
        generateSPS(consumer, atomic_alloy);
        generateQIO(consumer, atomic_alloy);
        generateTeleports(consumer, atomic_alloy);
    }

    protected void generateSPS(Consumer<Advancement> consumer, Advancement atomic_alloy) {
        Advancement plutonium = Advancement.Builder.advancement()
                .parent(atomic_alloy)
                .display(MekanismItems.PLUTONIUM_PELLET, title("plutonium"), description("plutonium"), null, FrameType.TASK, true, true, false)
                .addCriterion("plutonium", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.PLUTONIUM_PELLET.get()))
                .save(consumer, advancementLocation("plutonium"));
        Advancement sps = Advancement.Builder.advancement()
                .parent(plutonium)
                .display(MekanismBlocks.SPS_CASING, title("sps"), description("sps"), null, FrameType.TASK, true, true, false)
                .requirements(RequirementsStrategy.OR)
                .addCriterion("sps_casing", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.SPS_CASING.asItem()))
                .addCriterion("sps_port", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.SPS_PORT.asItem()))
                .save(consumer, advancementLocation("sps"));
        Advancement antimatter = Advancement.Builder.advancement()
                .parent(sps)
                .display(MekanismItems.ANTIMATTER_PELLET, title("antimatter"), obfuscatedDescription("antimatter"), null, FrameType.TASK, true, true, false)
                .addCriterion("antimatter", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.ANTIMATTER_PELLET.get()))
                .save(consumer, advancementLocation("antimatter"));
        Advancement nucleosynthesizer = Advancement.Builder.advancement()
                .parent(antimatter)
                .display(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, title("nucleosynthesizer"), obfuscatedDescription("nucleosynthesizer"), null, FrameType.CHALLENGE, true, true, false)
                .addCriterion("nucleosynthesizer", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER.asItem()))
                .save(consumer, advancementLocation("nucleosynthesizer"));
    }

    protected void generateQIO(Consumer<Advancement> consumer, Advancement atomic_alloy) {
        Advancement polonium = Advancement.Builder.advancement()
                .parent(atomic_alloy)
                .display(MekanismItems.POLONIUM_PELLET, title("polonium"), description("polonium"), null, FrameType.TASK, true, true, false)
                .addCriterion("polonium", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.POLONIUM_PELLET.get()))
                .save(consumer, advancementLocation("polonium"));
        Advancement qiodrivearray = Advancement.Builder.advancement()
                .parent(polonium)
                .display(MekanismBlocks.QIO_DRIVE_ARRAY, title("qiodrivearray"), description("qiodrivearray"), null, FrameType.TASK, true, true, false)
                .addCriterion("qiodrivearray", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.QIO_DRIVE_ARRAY.asItem()))
                .save(consumer, advancementLocation("qiodrivearray"));
        generateQIODrives(consumer, qiodrivearray);
        generateQIODashboards(consumer, qiodrivearray);
    }

    protected void generateQIODrives(Consumer<Advancement> consumer, Advancement qiodrivearray) {
        Advancement basicqio = Advancement.Builder.advancement()
                .parent(qiodrivearray)
                .display(MekanismItems.BASE_QIO_DRIVE, title("basicqiodrive"), description("basicqiodrive"), null, FrameType.TASK, true, true, false)
                .addCriterion("basicqiodrive", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.BASE_QIO_DRIVE.get()))
                .save(consumer, advancementLocation("basicqiodrive"));
        Advancement advancedqio = Advancement.Builder.advancement()
                .parent(basicqio)
                .display(MekanismItems.HYPER_DENSE_QIO_DRIVE, title("advancedqiodrive"), description("advancedqiodrive"), null, FrameType.TASK, true, true, false)
                .addCriterion("advancedqiodrive", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.HYPER_DENSE_QIO_DRIVE.get()))
                .save(consumer, advancementLocation("advancedqiodrive"));
        Advancement eliteqio = Advancement.Builder.advancement()
                .parent(advancedqio)
                .display(MekanismItems.TIME_DILATING_QIO_DRIVE, title("eliteqiodrive"), description("eliteqiodrive"), null, FrameType.TASK, true, true, false)
                .addCriterion("eliteqiodrive", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.TIME_DILATING_QIO_DRIVE.get()))
                .save(consumer, advancementLocation("eliteqiodrive"));
        Advancement ultimateqio = Advancement.Builder.advancement()
                .parent(eliteqio)
                .display(MekanismItems.SUPERMASSIVE_QIO_DRIVE, title("ultimateqiodrive"), obfuscatedDescription("ultimateqiodrive"), null, FrameType.CHALLENGE, true, true, false)
                .addCriterion("ultimateqiodrive", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.SUPERMASSIVE_QIO_DRIVE.get()))
                .save(consumer, advancementLocation("ultimateqiodrive"));
    }

    protected void generateQIODashboards(Consumer<Advancement> consumer, Advancement qiodrivearray) {
        Advancement dashboard = Advancement.Builder.advancement()
                .parent(qiodrivearray)
                .display(MekanismBlocks.QIO_DASHBOARD, title("qiodashboard"), description("qiodashboard"), null, FrameType.TASK, true, true, false)
                .addCriterion("qiodashboard", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.QIO_DASHBOARD.asItem()))
                .save(consumer, advancementLocation("qiodashboard"));
        Advancement portabledashboard = Advancement.Builder.advancement()
                .parent(dashboard)
                .display(MekanismItems.PORTABLE_QIO_DASHBOARD, title("portableqiodashboard"), description("portableqiodashboard"), null, FrameType.GOAL, true, true, false)
                .addCriterion("portableqiodashboard", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.PORTABLE_QIO_DASHBOARD.get()))
                .save(consumer, advancementLocation("portableqiodashboard"));
    }

    protected void generateTeleports(Consumer<Advancement> consumer, Advancement atomic_alloy) {
        Advancement teleportcore = Advancement.Builder.advancement()
                .parent(atomic_alloy)
                .display(MekanismItems.TELEPORTATION_CORE, title("teleportcore"), description("teleportcore"), null, FrameType.TASK, true, true, false)
                .addCriterion("teleportcore", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.TELEPORTATION_CORE.get()))
                .save(consumer, advancementLocation("teleportcore"));
        Advancement teleporter = Advancement.Builder.advancement()
                .parent(teleportcore)
                .display(MekanismBlocks.TELEPORTER, title("teleporter"), description("teleporter"), null, FrameType.TASK, true, true, false)
                .addCriterion("teleporter", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.TELEPORTER.asItem()))
                .save(consumer, advancementLocation("teleporter"));
        Advancement portableteleporter = Advancement.Builder.advancement()
                .parent(teleporter)
                .display(MekanismItems.PORTABLE_TELEPORTER, title("portableteleporter"), description("portableteleporter"), null, FrameType.TASK, true, true, false)
                .addCriterion("portableteleporter", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.PORTABLE_TELEPORTER.get()))
                .save(consumer, advancementLocation("portableteleporter"));
        Advancement quantumentangloporter = Advancement.Builder.advancement()
                .parent(teleportcore)
                .display(MekanismBlocks.QUANTUM_ENTANGLOPORTER, title("quantumentangloporter"), description("quantumentangloporter"), null, FrameType.TASK, true, true, false)
                .addCriterion("quantumentangloporter", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.QUANTUM_ENTANGLOPORTER.asItem()))
                .save(consumer, advancementLocation("quantumentangloporter"));
    }

    protected void generateControls(Consumer<Advancement> consumer, Advancement metallurgicinfuser) {
        Advancement basic_control = Advancement.Builder.advancement()
                .parent(metallurgicinfuser)
                .display(MekanismItems.BASIC_CONTROL_CIRCUIT, title("basic_control"), description("basic_control"), null, FrameType.TASK, true, true, false)
                .addCriterion("basic_control", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.BASIC_CONTROL_CIRCUIT.get()))
                .save(consumer, advancementLocation("control_basic"));
        Advancement advanced_control = Advancement.Builder.advancement()
                .parent(basic_control)
                .display(MekanismItems.ADVANCED_CONTROL_CIRCUIT, title("advanced_control"), description("advanced_control"), null, FrameType.TASK, true, true, false)
                .addCriterion("advanced_control", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.ADVANCED_CONTROL_CIRCUIT.get()))
                .save(consumer, advancementLocation("advanced_control"));
        Advancement elite_control = Advancement.Builder.advancement()
                .parent(advanced_control)
                .display(MekanismItems.ELITE_CONTROL_CIRCUIT, title("elite_control"), description("elite_control"), null, FrameType.TASK, true, true, false)
                .addCriterion("elite_control", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.ELITE_CONTROL_CIRCUIT.get()))
                .save(consumer, advancementLocation("elite_control"));
        Advancement ultimate_control = Advancement.Builder.advancement()
                .parent(elite_control)
                .display(MekanismItems.ULTIMATE_CONTROL_CIRCUIT, title("ultimate_control"), description("elite_control"), null, FrameType.GOAL, true, true, false)
                .addCriterion("elite_control", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.ULTIMATE_CONTROL_CIRCUIT.get()))
                .save(consumer, advancementLocation("ultimate_control"));
        Advancement robit = Advancement.Builder.advancement()
                .parent(ultimate_control)
                .display(MekanismItems.ROBIT, title("robit"), description("robit"), null, FrameType.GOAL, true, true, false)
                .addCriterion("elite_control", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.ROBIT.get()))
                .save(consumer, advancementLocation("robit"));
        Advancement digitalminer = Advancement.Builder.advancement()
                .parent(robit)
                .display(MekanismBlocks.DIGITAL_MINER.asItem(), title("robit"), description("robit"), null, FrameType.GOAL, true, true, false)
                .addCriterion("digitalminer", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.DIGITAL_MINER.asItem()))
                .save(consumer, advancementLocation("digitalminer"));
    }

    protected void generateDisassembly(Consumer<Advancement> consumer, Advancement materials) {
        Advancement disassembler = Advancement.Builder.advancement()
                .parent(materials)
                .display(MekanismItems.ATOMIC_DISASSEMBLER, title("disassembler"), description("disassembler"), null, FrameType.TASK, true, true, false)
                .addCriterion("disassembler", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.ATOMIC_DISASSEMBLER.get()))
                .save(consumer, advancementLocation("disassembler"));
        Advancement mekasuit = Advancement.Builder.advancement()
                .parent(materials)
                .display(MekanismItems.MEKASUIT_BODYARMOR, title("mekasuit"), description("mekasuit"), null, FrameType.GOAL, true, true, false)
                .requirements(RequirementsStrategy.AND)
                .addCriterion("helmet", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.MEKASUIT_HELMET.get()))
                .addCriterion("bodyarmor", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.MEKASUIT_BODYARMOR.get()))
                .addCriterion("pants", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.MEKASUIT_PANTS.get()))
                .addCriterion("boots", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.MEKASUIT_BOOTS.get()))
                .addCriterion("tool", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.MEKA_TOOL.get()))
                .save(consumer, advancementLocation("mekasuit"));
        JsonElement helmetJson = JsonParser.parseString("{mekData: {modules: {\"mekanism:radiation_shielding_unit\": {}, \"mekanism:inhalation_purification_unit\": {amount: 1}, \"mekanism:laser_dissipation_unit\": {}, \"mekanism:electrolytic_breathing_unit\": {amount: 4}, \"mekanism:nutritional_injection_unit\": {}, \"mekanism:energy_unit\": {amount: 8}, \"mekanismgenerators:solar_recharging_unit\": {amount: 8}}}}");
        ItemPredicate helmet = new ItemPredicate(null, Set.of(MekanismItems.MEKASUIT_HELMET.get()), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, null, NbtPredicate.fromJson(helmetJson));
        JsonElement bodyarmorJson = JsonParser.parseString("{mekData: {modules: {\"mekanism:radiation_shielding_unit\": {}, \"mekanism:jetpack_unit\": {amount:1}, \"mekanism:laser_dissipation_unit\": {}, \"mekanism:dosimeter_unit\": {}, \"mekanism:geiger_unit\": {}, \"mekanism:charge_distribution_unit\": {}, \"mekanism:elytra_unit\": {}, \"mekanism:gravitational_modulating_unit\": {amount: 1}, \"mekanism:energy_unit\": {amount: 8}}}}");
        ItemPredicate bodyarmor = new ItemPredicate(null, Set.of(MekanismItems.MEKASUIT_BODYARMOR.get()), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, null, NbtPredicate.fromJson(bodyarmorJson));
        JsonElement pantsJson = JsonParser.parseString("{mekData: {modules: {\"mekanismgenerators:geothermal_generator_unit\": {amount: 8}, \"mekanism:radiation_shielding_unit\": {}, \"mekanism:locomotive_boosting_unit\": {amount: 4}, \"mekanism:laser_dissipation_unit\": {}, \"mekanism:energy_unit\": {amount: 8}}}}");
        ItemPredicate pants = new ItemPredicate(null, Set.of(MekanismItems.MEKASUIT_PANTS.get()), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, null, NbtPredicate.fromJson(pantsJson));
        JsonElement bootsJson = JsonParser.parseString("{mekData: {modules: {\"mekanism:radiation_shielding_unit\": {}, \"mekanism:frost_walker_unit\": {amount: 2}, \"mekanism:laser_dissipation_unit\": {}, \"mekanism:hydraulic_propulsion_unit\": {amount: 4}, \"mekanism:energy_unit\": {amount: 8}, \"mekanism:magnetic_attraction_unit\": {amount: 4}}}}");
        ItemPredicate boots = new ItemPredicate(null, Set.of(MekanismItems.MEKASUIT_BOOTS.get()), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, null, NbtPredicate.fromJson(bootsJson));
        JsonElement toolJson = JsonParser.parseString("{mekData: {modules: {\"mekanism:silk_touch_unit\": {}, \"mekanism:attack_amplification_unit\": {amount: 4}, \"mekanism:farming_unit\": {amount: 4}, \"mekanism:teleportation_unit\": {}, \"mekanism:excavation_escalation_unit\": {amount: 4}, \"mekanism:vein_mining_unit\": {amount: 4}, \"mekanism:shearing_unit\": {amount: 1}, \"mekanism:energy_unit\": {amount: 8}}}}");
        ItemPredicate tool = new ItemPredicate(null, Set.of(MekanismItems.MEKA_TOOL.get()), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, null, NbtPredicate.fromJson(toolJson));
        Advancement upgradedmekasuit = Advancement.Builder.advancement()
                .parent(mekasuit)
                .display(MekanismItems.MEKASUIT_BODYARMOR, title("upgradedmekasuit"), description("upgradedmekasuit"), null, FrameType.CHALLENGE, true, true, true)
                .requirements(RequirementsStrategy.AND)
                .addCriterion("helmet", InventoryChangeTrigger.TriggerInstance.hasItems(helmet))
                .addCriterion("bodyarmor", InventoryChangeTrigger.TriggerInstance.hasItems(bodyarmor))
                .addCriterion("pants", InventoryChangeTrigger.TriggerInstance.hasItems(pants))
                .addCriterion("boots", InventoryChangeTrigger.TriggerInstance.hasItems(boots))
                .addCriterion("tool", InventoryChangeTrigger.TriggerInstance.hasItems(tool))
                .save(consumer, advancementLocation("upgradedmekasuit"));
    }
}
