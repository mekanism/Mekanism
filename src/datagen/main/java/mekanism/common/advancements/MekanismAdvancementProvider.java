package mekanism.common.advancements;

import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.advancements.triggers.ChangeRobitSkinTrigger;
import mekanism.common.entity.RobitPrideSkinData;
import mekanism.common.item.predicate.FullCanteenItemPredicate;
import mekanism.common.item.predicate.MaxedModuleContainerItemPredicate;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.data.ExistingFileHelper;

public class MekanismAdvancementProvider extends BaseAdvancementProvider {

    public MekanismAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper, Mekanism.MODID);
    }

    //TODO - 1.19: xp rewards for any of these?

    @Override
    protected void registerAdvancements(@Nonnull Consumer<Advancement> consumer) {
        //TODO - 1.19: Reorganize these to follow their order better
        advancement(MekanismAdvancements.ROOT)
              .display(MekanismItems.ATOMIC_DISASSEMBLER, Mekanism.rl("textures/block/block_osmium.png"), FrameType.GOAL, false, false, false)
              .addCriterion("automatic", new PlayerTrigger.TriggerInstance(MekanismCriteriaTriggers.LOGGED_IN.getId(), EntityPredicate.Composite.ANY))
              .save(consumer);
        advancement(MekanismAdvancements.MATERIALS)
              .display(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM), FrameType.TASK)
              .orCriteria(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM),
                    MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN),
                    MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD),
                    MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.URANIUM),
                    MekanismItems.FLUORITE_GEM
              ).save(consumer);

        advancement(MekanismAdvancements.METALLURGIC_INFUSER)
              .displayAndCriterion(MekanismBlocks.METALLURGIC_INFUSER, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.STEEL_INGOT)
              .displayAndCriterion(MekanismItems.STEEL_INGOT, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.STEEL_CASING)
              .displayAndCriterion(MekanismBlocks.STEEL_CASING, FrameType.TASK)
              .save(consumer);

        advancement(MekanismAdvancements.INFUSED_ALLOY)
              .displayAndCriterion(MekanismItems.INFUSED_ALLOY, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.REINFORCED_ALLOY)
              .displayAndCriterion(MekanismItems.REINFORCED_ALLOY, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.ATOMIC_ALLOY)
              .displayAndCriterion(MekanismItems.ATOMIC_ALLOY, FrameType.GOAL)
              .save(consumer);

        advancement(MekanismAdvancements.FLUID_TANK)
              .displayAndCriterion(MekanismBlocks.BASIC_FLUID_TANK, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.CHEMICAL_TANK)
              .displayAndCriterion(MekanismBlocks.BASIC_CHEMICAL_TANK, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.WASTE_REMOVAL)
              .displayAndCriterion(MekanismBlocks.RADIOACTIVE_WASTE_BARREL, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.FULL_CANTEEN)
              .display(MekanismItems.CANTEEN, null, FrameType.TASK, true, true, true)
              .addCriterion("full_canteen", InventoryChangeTrigger.TriggerInstance.hasItems(FullCanteenItemPredicate.INSTANCE))
              .save(consumer);

        generateSPS(consumer);
        generateQIO(consumer);
        generateTeleports(consumer);
        generateControls(consumer);
        generateDisassembly(consumer);
    }

    private void generateSPS(@Nonnull Consumer<Advancement> consumer) {
        advancement(MekanismAdvancements.PLUTONIUM)
              .displayAndCriterion(MekanismItems.PLUTONIUM_PELLET, FrameType.TASK)
              .save(consumer);
        //TODO: If we end up adding a criteria for creating a multiblock switch the criteria for this to using that
        advancement(MekanismAdvancements.SPS)
              .display(MekanismBlocks.SPS_CASING, FrameType.TASK)
              .andCriteria(MekanismBlocks.SPS_CASING,
                    MekanismBlocks.SPS_PORT
              ).save(consumer);
        advancement(MekanismAdvancements.ANTIMATTER)
              .displayAndCriterion(MekanismItems.ANTIMATTER_PELLET, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.NUCLEOSYNTHESIZER)
              .displayAndCriterion(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, FrameType.CHALLENGE)
              .save(consumer);
    }

    private void generateQIO(@Nonnull Consumer<Advancement> consumer) {
        advancement(MekanismAdvancements.POLONIUM)
              .displayAndCriterion(MekanismItems.POLONIUM_PELLET, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.QIO_DRIVE_ARRAY)
              .displayAndCriterion(MekanismBlocks.QIO_DRIVE_ARRAY, FrameType.TASK)
              .save(consumer);
        generateQIODrives(consumer);
        generateQIODashboards(consumer);
    }

    private void generateQIODrives(@Nonnull Consumer<Advancement> consumer) {
        advancement(MekanismAdvancements.BASIC_QIO_DRIVE)
              .displayAndCriterion(MekanismItems.BASE_QIO_DRIVE, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.ADVANCED_QIO_DRIVE)
              .displayAndCriterion(MekanismItems.HYPER_DENSE_QIO_DRIVE, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.ELITE_QIO_DRIVE)
              .displayAndCriterion(MekanismItems.TIME_DILATING_QIO_DRIVE, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.ULTIMATE_QIO_DRIVE)
              .displayAndCriterion(MekanismItems.SUPERMASSIVE_QIO_DRIVE, FrameType.CHALLENGE)
              .save(consumer);
    }

    private void generateQIODashboards(@Nonnull Consumer<Advancement> consumer) {
        advancement(MekanismAdvancements.QIO_DASHBOARD)
              .displayAndCriterion(MekanismBlocks.QIO_DASHBOARD, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.PORTABLE_QIO_DASHBOARD)
              .displayAndCriterion(MekanismItems.PORTABLE_QIO_DASHBOARD, FrameType.GOAL)
              .save(consumer);
    }

    private void generateTeleports(@Nonnull Consumer<Advancement> consumer) {
        advancement(MekanismAdvancements.TELEPORTATION_CORE)
              .displayAndCriterion(MekanismItems.TELEPORTATION_CORE, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.TELEPORTER)
              .display(MekanismBlocks.TELEPORTER, FrameType.TASK)
              .addCriterion(MekanismBlocks.TELEPORTER)
              .addCriterion("teleport", new PlayerTrigger.TriggerInstance(MekanismCriteriaTriggers.TELEPORT.getId(), EntityPredicate.Composite.ANY))
              .save(consumer);
        advancement(MekanismAdvancements.PORTABLE_TELEPORTER)
              .displayAndCriterion(MekanismItems.PORTABLE_TELEPORTER, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.QUANTUM_ENTANGLOPORTER)
              .displayAndCriterion(MekanismBlocks.QUANTUM_ENTANGLOPORTER, FrameType.TASK)
              .save(consumer);
    }

    private void generateControls(@Nonnull Consumer<Advancement> consumer) {
        advancement(MekanismAdvancements.BASIC_CONTROL_CIRCUIT)
              .displayAndCriterion(MekanismItems.BASIC_CONTROL_CIRCUIT, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.ADVANCED_CONTROL_CIRCUIT)
              .displayAndCriterion(MekanismItems.ADVANCED_CONTROL_CIRCUIT, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.ELITE_CONTROL_CIRCUIT)
              .displayAndCriterion(MekanismItems.ELITE_CONTROL_CIRCUIT, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.ULTIMATE_CONTROL_CIRCUIT)
              .displayAndCriterion(MekanismItems.ULTIMATE_CONTROL_CIRCUIT, FrameType.GOAL)
              .save(consumer);
        advancement(MekanismAdvancements.ROBIT)
              .displayAndCriterion(MekanismItems.ROBIT, FrameType.GOAL)
              .save(consumer);
        ItemStack skinnedRobit = MekanismItems.ROBIT.getItemStack();
        MekanismItems.ROBIT.get().setSkin(skinnedRobit, MekanismRobitSkins.PRIDE_SKINS.get(RobitPrideSkinData.TRANS).getSkin());
        advancement(MekanismAdvancements.ROBIT_AESTHETICS)
              .display(skinnedRobit, null, FrameType.TASK, true, true, true)
              .addCriterion("change_skin", ChangeRobitSkinTrigger.TriggerInstance.toAny())
              .save(consumer);
        advancement(MekanismAdvancements.DIGITAL_MINER)
              .displayAndCriterion(MekanismBlocks.DIGITAL_MINER, FrameType.GOAL)
              .save(consumer);
        advancement(MekanismAdvancements.DICTIONARY)
              .displayAndCriterion(MekanismItems.DICTIONARY, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.STONE_GENERATOR)
              .displayAndCriterion(MekanismItems.STONE_GENERATOR_UPGRADE, FrameType.TASK)
              .save(consumer);
    }

    private void generateDisassembly(@Nonnull Consumer<Advancement> consumer) {
        advancement(MekanismAdvancements.DISASSEMBLER)
              .displayAndCriterion(MekanismItems.ATOMIC_DISASSEMBLER, FrameType.TASK)
              .save(consumer);
        advancement(MekanismAdvancements.MEKASUIT)
              .display(MekanismItems.MEKASUIT_BODYARMOR, FrameType.GOAL)
              .addCriterion("full_set", InventoryChangeTrigger.TriggerInstance.hasItems(
                    MekanismItems.MEKASUIT_HELMET,
                    MekanismItems.MEKASUIT_BODYARMOR,
                    MekanismItems.MEKASUIT_PANTS,
                    MekanismItems.MEKASUIT_BOOTS,
                    MekanismItems.MEKA_TOOL
              )).save(consumer);
        //Require having all of them maxed at once
        advancement(MekanismAdvancements.UPGRADED_MEKASUIT)
              .display(MekanismItems.MEKASUIT_BODYARMOR, null, FrameType.CHALLENGE, true, true, true)
              .addCriterion("maxed_gear", InventoryChangeTrigger.TriggerInstance.hasItems(Stream.of(
                                MekanismItems.MEKASUIT_HELMET,
                                MekanismItems.MEKASUIT_BODYARMOR,
                                MekanismItems.MEKASUIT_PANTS,
                                MekanismItems.MEKASUIT_BOOTS,
                                MekanismItems.MEKA_TOOL
                          ).map(item -> new MaxedModuleContainerItemPredicate<>(item.asItem()))
                          .toArray(ItemPredicate[]::new)
              )).save(consumer);
    }
}