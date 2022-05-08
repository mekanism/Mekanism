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
import net.minecraft.data.DataGenerator;
import net.minecraft.advancements.critereon.TickTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import javax.json.JsonObject;
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
                .addCriterion("fluidtank", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.BASIC_FLUID_TANK))
                .save(consumer, advancementLocation("fluidtank"));
        Advancement chemicaltank = Advancement.Builder.advancement()
                .parent(root)
                .display(MekanismBlocks.BASIC_CHEMICAL_TANK, title("chemicaltank"), description("chemicaltank"), null, FrameType.TASK, true, true, false)
                .addCriterion("chemicaltank", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.BASIC_CHEMICAL_TANK))
                .save(consumer, advancementLocation("chemicaltank"));
        Advancement metallurgicinfuser = Advancement.Builder.advancement()
                .parent(materials)
                .display(MekanismBlocks.METALLURGIC_INFUSER, title("metallurgicinfuser"), description("metallurgicinfuser"), null, FrameType.TASK, true, true, false)
                .addCriterion("metallurgicinfuser", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismBlocks.METALLURGIC_INFUSER.asItem()))
                .save(consumer, advancementLocation("metallurgicinfuser"));
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
        Advancement polonium = Advancement.Builder.advancement()
                .parent(atomic_alloy)
                .display(MekanismItems.POLONIUM_PELLET, title("polonium"), description("polonium"), null, FrameType.TASK, true, true, false)
                .addCriterion("polonium", InventoryChangeTrigger.TriggerInstance.hasItems(MekanismItems.POLONIUM_PELLET.get()))
                .save(consumer, advancementLocation("polonium"));
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
}
