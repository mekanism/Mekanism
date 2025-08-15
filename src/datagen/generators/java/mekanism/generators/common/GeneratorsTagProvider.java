package mekanism.generators.common;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPITags;
import mekanism.common.content.gear.IModuleItem;
import mekanism.common.tag.BaseTagProvider;
import mekanism.common.tags.MekanismTags;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsChemicals;
import mekanism.generators.common.registries.GeneratorsDamageTypes;
import mekanism.generators.common.registries.GeneratorsDataComponents;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

public class GeneratorsTagProvider extends BaseTagProvider {

    public GeneratorsTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MekanismGenerators.MODID, existingFileHelper);
    }

    @Override
    protected Collection<? extends DeferredHolder<Block, ?>> getAllBlocks() {
        return GeneratorsBlocks.BLOCKS.getPrimaryEntries();
    }

    @Override
    protected void registerTags(HolderLookup.Provider registries) {
        addBoxBlacklist();
        addEndermanBlacklist();
        addDataComponents();
        addFluids();
        addGases();
        addDamageTypes();
        addHarvestRequirements();
        getBuilder(BlockTags.IMPERMEABLE).add(GeneratorsBlocks.REACTOR_GLASS);

        getBuilder(BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON).add(
              GeneratorsBlocks.REACTOR_GLASS,

              GeneratorsBlocks.FISSION_REACTOR_CASING,
              GeneratorsBlocks.FISSION_REACTOR_PORT,
              GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER,
              GeneratorsBlocks.FISSION_FUEL_ASSEMBLY,
              GeneratorsBlocks.CONTROL_ROD_ASSEMBLY,

              GeneratorsBlocks.TURBINE_CASING,
              GeneratorsBlocks.TURBINE_VENT,
              GeneratorsBlocks.TURBINE_VALVE,
              GeneratorsBlocks.TURBINE_ROTOR,
              GeneratorsBlocks.SATURATING_CONDENSER,
              GeneratorsBlocks.ELECTROMAGNETIC_COIL,
              GeneratorsBlocks.ROTATIONAL_COMPLEX,

              GeneratorsBlocks.FUSION_REACTOR_CONTROLLER,
              GeneratorsBlocks.FUSION_REACTOR_FRAME,
              GeneratorsBlocks.FUSION_REACTOR_PORT,
              GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER,
              GeneratorsBlocks.LASER_FOCUS_MATRIX);

        getBuilder(FRAMEABLE).add(GeneratorsBlocks.REACTOR_GLASS, GeneratorsBlocks.LASER_FOCUS_MATRIX);
        getBuilder(FB_BE_WHITELIST).add(GeneratorsBlocks.REACTOR_GLASS, GeneratorsBlocks.LASER_FOCUS_MATRIX);

        getBuilder(MekanismAPITags.Items.MEKA_UNITS).add(GeneratorsItems.ITEMS.getEntries().stream()
              .filter(item -> item.get() instanceof IModuleItem)
              .toList());
    }

    private void addBoxBlacklist() {
        getBuilder(Tags.Blocks.RELOCATION_NOT_SUPPORTED).add(
              GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR,
              GeneratorsBlocks.WIND_GENERATOR
        );
    }

    private void addEndermanBlacklist() {
        getBuilder(Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST).add(
              GeneratorsBlocks.TURBINE_CASING,
              GeneratorsBlocks.TURBINE_VALVE,
              GeneratorsBlocks.TURBINE_VENT,
              GeneratorsBlocks.ELECTROMAGNETIC_COIL,
              GeneratorsBlocks.ROTATIONAL_COMPLEX,
              GeneratorsBlocks.SATURATING_CONDENSER,
              GeneratorsBlocks.TURBINE_ROTOR,
              GeneratorsBlocks.FISSION_REACTOR_CASING,
              GeneratorsBlocks.FISSION_REACTOR_PORT,
              GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER,
              GeneratorsBlocks.FISSION_FUEL_ASSEMBLY,
              GeneratorsBlocks.CONTROL_ROD_ASSEMBLY,
              GeneratorsBlocks.FUSION_REACTOR_CONTROLLER,
              GeneratorsBlocks.FUSION_REACTOR_PORT,
              GeneratorsBlocks.FUSION_REACTOR_FRAME,
              GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER,
              GeneratorsBlocks.LASER_FOCUS_MATRIX,
              GeneratorsBlocks.REACTOR_GLASS
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
        getBuilder(GeneratorTags.Chemicals.DEUTERIUM).add(GeneratorsChemicals.DEUTERIUM);
        getBuilder(GeneratorTags.Chemicals.TRITIUM).add(GeneratorsChemicals.TRITIUM);
        getBuilder(GeneratorTags.Chemicals.FUSION_FUEL).add(GeneratorsChemicals.FUSION_FUEL);

        getBuilder(MekanismAPITags.Chemicals.GASEOUS).add(
              GeneratorsChemicals.DEUTERIUM,
              GeneratorsChemicals.TRITIUM,
              GeneratorsChemicals.FUSION_FUEL
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

    private void addHarvestRequirements() {
        addToHarvestTag(BlockTags.MINEABLE_WITH_PICKAXE,
              GeneratorsBlocks.HEAT_GENERATOR,
              GeneratorsBlocks.SOLAR_GENERATOR, GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR,
              GeneratorsBlocks.GAS_BURNING_GENERATOR,
              GeneratorsBlocks.BIO_GENERATOR,
              GeneratorsBlocks.WIND_GENERATOR,
              GeneratorsBlocks.TURBINE_ROTOR, GeneratorsBlocks.ROTATIONAL_COMPLEX, GeneratorsBlocks.ELECTROMAGNETIC_COIL, GeneratorsBlocks.TURBINE_CASING,
              GeneratorsBlocks.TURBINE_VALVE, GeneratorsBlocks.TURBINE_VENT, GeneratorsBlocks.SATURATING_CONDENSER,
              GeneratorsBlocks.REACTOR_GLASS, GeneratorsBlocks.LASER_FOCUS_MATRIX,
              GeneratorsBlocks.FISSION_REACTOR_CASING, GeneratorsBlocks.FISSION_REACTOR_PORT, GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER,
              GeneratorsBlocks.FISSION_FUEL_ASSEMBLY, GeneratorsBlocks.CONTROL_ROD_ASSEMBLY,
              GeneratorsBlocks.FUSION_REACTOR_CONTROLLER, GeneratorsBlocks.FUSION_REACTOR_FRAME, GeneratorsBlocks.FUSION_REACTOR_PORT,
              GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER
        );
    }
}