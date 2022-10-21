package mekanism.generators.common;

import java.util.List;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tag.BaseTagProvider;
import mekanism.common.tag.MekanismTagProvider;
import mekanism.common.tags.MekanismTags;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsGases;
import mekanism.generators.common.registries.GeneratorsTileEntityTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class GeneratorsTagProvider extends BaseTagProvider {

    public GeneratorsTagProvider(DataGenerator gen, @Nullable ExistingFileHelper existingFileHelper) {
        super(gen, MekanismGenerators.MODID, existingFileHelper);
    }

    @Override
    protected List<IBlockProvider> getAllBlocks() {
        return GeneratorsBlocks.BLOCKS.getAllBlocks();
    }

    @Override
    protected void registerTags() {
        addBoxBlacklist();
        addEndermanBlacklist();
        addFluids();
        addGases();
        addHarvestRequirements();
        addToTag(BlockTags.IMPERMEABLE, GeneratorsBlocks.REACTOR_GLASS);
    }

    private void addBoxBlacklist() {
        addToTag(MekanismTags.Blocks.RELOCATION_NOT_SUPPORTED,
              GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR,
              GeneratorsBlocks.WIND_GENERATOR
        );
        TileEntityTypeRegistryObject<?>[] tilesToBlacklist = {
              GeneratorsTileEntityTypes.ADVANCED_SOLAR_GENERATOR,
              GeneratorsTileEntityTypes.WIND_GENERATOR
        };
        addToTag(MekanismTags.TileEntityTypes.IMMOVABLE, tilesToBlacklist);
        addToTag(MekanismTags.TileEntityTypes.RELOCATION_NOT_SUPPORTED, tilesToBlacklist);
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
        addToTag(GeneratorTags.Fluids.BIOETHANOL, GeneratorsFluids.BIOETHANOL);
        addToTag(GeneratorTags.Fluids.DEUTERIUM, GeneratorsFluids.DEUTERIUM);
        addToTag(GeneratorTags.Fluids.FUSION_FUEL, GeneratorsFluids.FUSION_FUEL);
        addToTag(GeneratorTags.Fluids.TRITIUM, GeneratorsFluids.TRITIUM);
        //Prevent all our fluids from being duped by create
        for (FluidRegistryObject<?, ?, ?, ?, ?> fluid : GeneratorsFluids.FLUIDS.getAllFluids()) {
            addToTag(MekanismTagProvider.CREATE_NO_INFINITE_FLUID, fluid);
        }
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