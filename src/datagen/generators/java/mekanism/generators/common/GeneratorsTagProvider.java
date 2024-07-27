package mekanism.generators.common;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import mekanism.common.tag.BaseTagProvider;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsGases;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class GeneratorsTagProvider extends BaseTagProvider {

    public GeneratorsTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MekanismGenerators.MODID, existingFileHelper);
    }

    @Override
    protected Collection<? extends Holder<Block>> getAllBlocks() {
        return GeneratorsBlocks.BLOCKS.getPrimaryEntries();
    }

    @Override
    protected void registerTags(HolderLookup.Provider registries) {
        addBoxBlacklist();
        addEndermanBlacklist();
        addFluids();
        addGases();
        addHarvestRequirements();
        addToTag(BlockTags.IMPERMEABLE, GeneratorsBlocks.REACTOR_GLASS);

        addToTag(BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON,
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
    }

    private void addBoxBlacklist() {
        addToTag(Tags.Blocks.RELOCATION_NOT_SUPPORTED,
              GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR,
              GeneratorsBlocks.WIND_GENERATOR
        );
    }

    private void addEndermanBlacklist() {
        addToTag(Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST,
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

    private void addFluids() {
        addToGenericFluidTags(GeneratorsFluids.FLUIDS);
        addToTag(GeneratorTags.Fluids.BIOETHANOL, GeneratorsFluids.BIOETHANOL);
        addToTag(GeneratorTags.Fluids.DEUTERIUM, GeneratorsFluids.DEUTERIUM);
        addToTag(GeneratorTags.Fluids.FUSION_FUEL, GeneratorsFluids.FUSION_FUEL);
        addToTag(GeneratorTags.Fluids.TRITIUM, GeneratorsFluids.TRITIUM);
    }

    private void addGases() {
        addToTag(GeneratorTags.Gases.DEUTERIUM, GeneratorsGases.DEUTERIUM);
        addToTag(GeneratorTags.Gases.TRITIUM, GeneratorsGases.TRITIUM);
        addToTag(GeneratorTags.Gases.FUSION_FUEL, GeneratorsGases.FUSION_FUEL);
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