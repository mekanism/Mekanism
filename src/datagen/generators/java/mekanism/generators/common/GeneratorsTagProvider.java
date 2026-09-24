package mekanism.generators.common;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.ChemicalIds;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tag.BaseTagProvider;
import mekanism.common.tags.MekanismTags;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsDamageTypes;
import mekanism.generators.common.registries.GeneratorsDataComponents;
import mekanism.generators.common.registries.GeneratorsFluids;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

public class GeneratorsTagProvider extends BaseTagProvider {

    public GeneratorsTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, MekanismGenerators.MODID);
    }

    @Override
    protected Collection<? extends DeferredHolder<Block, ?>> getAllBlocks() {
        return GeneratorsBlocks.BLOCKS.getPrimaryEntries();
    }

    @Override
    protected void registerTags(HolderLookup.Provider registries) {
        addDataComponents();
        addFluids();
        addGases();
        addDamageTypes();
        addMultiblocks();
        addHarvestRequirements();
        getBuilder(BlockTags.IMPERMEABLE).add(GeneratorsBlocks.REACTOR_GLASS);
        getBuilder(BlockTags.DANGEROUS_FOR_TELEPORTATION).addAsBlocks(GeneratorTags.BlockItems.STRUCTURES_REACTORS);

        getBuilder(FRAMEABLE).add(GeneratorsBlocks.REACTOR_GLASS, GeneratorsBlocks.LASER_FOCUS_MATRIX);
        getBuilder(FB_BE_WHITELIST).add(GeneratorsBlocks.REACTOR_GLASS, GeneratorsBlocks.LASER_FOCUS_MATRIX);

        getBuilder(BlockTags.BLOCKS_MOTION_NO_LEAVES).add(
              GeneratorsBlocks.HEAT_GENERATOR,
              GeneratorsBlocks.SOLAR_GENERATOR, GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR,
              GeneratorsBlocks.GAS_BURNING_GENERATOR,
              GeneratorsBlocks.BIO_GENERATOR,
              GeneratorsBlocks.WIND_GENERATOR
        );
    }

    private void addDataComponents() {
        getBuilder(MekanismTags.DataComponents.CLEARABLE_CONFIG).add(
              GeneratorsDataComponents.FISSION_LOGIC_TYPE,
              GeneratorsDataComponents.FUSION_LOGIC_TYPE,
              GeneratorsDataComponents.ACTIVE_COOLED
        );
    }

    private void addFluids() {
        addToGenericFluidTags(GeneratorsFluids.FLUIDS);
        addToTag(GeneratorTags.Fluids.BIOETHANOL, GeneratorsFluids.BIOETHANOL);
        addToTag(GeneratorTags.Fluids.DEUTERIUM, GeneratorsFluids.DEUTERIUM);
        addToTag(GeneratorTags.Fluids.FUSION_FUEL, GeneratorsFluids.FUSION_FUEL);
        addToTag(GeneratorTags.Fluids.TRITIUM, GeneratorsFluids.TRITIUM);
    }

    private void addGases() {
        getBuilder(GeneratorTags.Chemicals.DEUTERIUM).add(ChemicalIds.DEUTERIUM);
        getBuilder(GeneratorTags.Chemicals.TRITIUM).add(ChemicalIds.TRITIUM);
        getBuilder(GeneratorTags.Chemicals.FUSION_FUEL).add(ChemicalIds.FUSION_FUEL);

        getBuilder(MekanismAPITags.Chemicals.GASEOUS).add(
              ChemicalIds.DEUTERIUM,
              ChemicalIds.TRITIUM,
              ChemicalIds.FUSION_FUEL
        );
    }

    private void addDamageTypes() {
        ResourceKey<DamageType> fusion = GeneratorsDamageTypes.FUSION.key();
        getBuilder(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS).add(fusion);
        getBuilder(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS).add(fusion);
        getBuilder(DamageTypeTags.BYPASSES_ARMOR).add(fusion);
        getBuilder(DamageTypeTags.BYPASSES_COOLDOWN).add(fusion);
        getBuilder(DamageTypeTags.BYPASSES_EFFECTS).add(fusion);
        getBuilder(DamageTypeTags.BYPASSES_ENCHANTMENTS).add(fusion);
        getBuilder(DamageTypeTags.BYPASSES_RESISTANCE).add(fusion);
        getBuilder(DamageTypeTags.BYPASSES_SHIELD).add(fusion);
        getBuilder(DamageTypeTags.BYPASSES_WOLF_ARMOR).add(fusion);
        getBuilder(DamageTypeTags.NO_KNOCKBACK).add(fusion);
        getBuilder(DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES).add(fusion);
    }

    private void addMultiblocks() {
        addToTags(MekanismTags.BlockItems.STRUCTURES_COMMON, GeneratorsBlocks.REACTOR_GLASS);

        addToTagsAndMarkKnown(GeneratorTags.BlockItems.STRUCTURES_TURBINE, MekanismBlocks.PRESSURE_DISPERSER,
              GeneratorsBlocks.TURBINE_ROTOR, GeneratorsBlocks.ROTATIONAL_COMPLEX, GeneratorsBlocks.ELECTROMAGNETIC_COIL, GeneratorsBlocks.TURBINE_CASING,
              GeneratorsBlocks.TURBINE_VALVE, GeneratorsBlocks.TURBINE_VENT, GeneratorsBlocks.SATURATING_CONDENSER
        );
        addToTags(GeneratorTags.BlockItems.STRUCTURES_TURBINE, MekanismTags.BlockItems.STRUCTURES_COMMON);
        addToTagsAndMarkKnown(GeneratorTags.BlockItems.STRUCTURES_FISSION, GeneratorsBlocks.REACTOR_GLASS,
              GeneratorsBlocks.FISSION_REACTOR_CASING, GeneratorsBlocks.FISSION_REACTOR_PORT, GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER,
              GeneratorsBlocks.FISSION_FUEL_ASSEMBLY, GeneratorsBlocks.CONTROL_ROD_ASSEMBLY
        );
        addToTagsAndMarkKnown(GeneratorTags.BlockItems.STRUCTURES_FUSION, GeneratorsBlocks.REACTOR_GLASS,
              GeneratorsBlocks.FUSION_REACTOR_CONTROLLER, GeneratorsBlocks.FUSION_REACTOR_FRAME, GeneratorsBlocks.FUSION_REACTOR_PORT,
              GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER, GeneratorsBlocks.LASER_FOCUS_MATRIX
        );

        addToTags(GeneratorTags.BlockItems.STRUCTURES_REACTORS, GeneratorTags.BlockItems.STRUCTURES_FISSION, GeneratorTags.BlockItems.STRUCTURES_FUSION);
        addToTags(MekanismTags.BlockItems.STRUCTURES, GeneratorTags.BlockItems.STRUCTURES_TURBINE, GeneratorTags.BlockItems.STRUCTURES_REACTORS);
    }

    private void addHarvestRequirements() {
        addToHarvestTag(BlockTags.MINEABLE_WITH_PICKAXE,
              GeneratorsBlocks.HEAT_GENERATOR,
              GeneratorsBlocks.SOLAR_GENERATOR, GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR,
              GeneratorsBlocks.GAS_BURNING_GENERATOR,
              GeneratorsBlocks.BIO_GENERATOR,
              GeneratorsBlocks.WIND_GENERATOR
        );
    }
}