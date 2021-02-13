package mekanism.generators.common.registries;

import mekanism.api.math.FloatingLong;
import mekanism.common.block.attribute.AttributeCustomSelectionBox;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.block.attribute.Attributes.AttributeMobSpawn;
import mekanism.common.block.attribute.Attributes.AttributeRedstoneEmitter;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.blocktype.BlockTypeTile.BlockTileBuilder;
import mekanism.common.lib.math.Pos3D;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.block.attribute.AttributeStateFissionPortMode;
import mekanism.generators.common.content.blocktype.BlockShapes;
import mekanism.generators.common.content.blocktype.Generator;
import mekanism.generators.common.content.blocktype.Generator.GeneratorBuilder;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntityReactorGlass;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import mekanism.generators.common.tile.fission.TileEntityControlRodAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionFuelAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter.RedstoneStatus;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorPort;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorBlock;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorPort;
import mekanism.generators.common.tile.fusion.TileEntityLaserFocusMatrix;
import mekanism.generators.common.tile.turbine.TileEntityElectromagneticCoil;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntitySaturatingCondenser;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import mekanism.generators.common.tile.turbine.TileEntityTurbineValve;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;
import net.minecraft.particles.ParticleTypes;

public class GeneratorsBlockTypes {

    private GeneratorsBlockTypes() {
    }

    //TODO: Do this in a cleaner way
    private static final FloatingLong STORAGE = FloatingLong.createConst(160_000);
    private static final FloatingLong STORAGE2 = FloatingLong.createConst(200_000);
    private static final FloatingLong SOLAR_STORAGE = FloatingLong.createConst(96_000);

    // Heat Generator
    public static final Generator<TileEntityHeatGenerator> HEAT_GENERATOR = GeneratorBuilder
          .createGenerator(() -> GeneratorsTileEntityTypes.HEAT_GENERATOR, GeneratorsLang.DESCRIPTION_HEAT_GENERATOR)
          .withGui(() -> GeneratorsContainerTypes.HEAT_GENERATOR)
          .withEnergyConfig(() -> STORAGE)
          .withCustomShape(BlockShapes.HEAT_GENERATOR)
          .withSound(GeneratorsSounds.HEAT_GENERATOR)
          .with(new AttributeParticleFX()
                .add(ParticleTypes.SMOKE, rand -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, -0.52))
                .add(ParticleTypes.FLAME, rand -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, -0.52)))
          .build();
    // Bio Generator
    public static final Generator<TileEntityBioGenerator> BIO_GENERATOR = GeneratorBuilder
          .createGenerator(() -> GeneratorsTileEntityTypes.BIO_GENERATOR, GeneratorsLang.DESCRIPTION_BIO_GENERATOR)
          .withGui(() -> GeneratorsContainerTypes.BIO_GENERATOR)
          .withEnergyConfig(() -> STORAGE)
          .withCustomShape(BlockShapes.BIO_GENERATOR)
          .withSound(GeneratorsSounds.BIO_GENERATOR)
          .with(new AttributeParticleFX()
                .add(ParticleTypes.SMOKE, rand -> new Pos3D(0, 0.3, -0.25)))
          .build();
    // Solar Generator
    public static final Generator<TileEntitySolarGenerator> SOLAR_GENERATOR = GeneratorBuilder
          .createGenerator(() -> GeneratorsTileEntityTypes.SOLAR_GENERATOR, GeneratorsLang.DESCRIPTION_SOLAR_GENERATOR)
          .withGui(() -> GeneratorsContainerTypes.SOLAR_GENERATOR)
          .withEnergyConfig(() -> SOLAR_STORAGE)
          .withCustomShape(BlockShapes.SOLAR_GENERATOR)
          .withSound(GeneratorsSounds.SOLAR_GENERATOR)
          .build();
    // Wind Generator
    public static final Generator<TileEntityWindGenerator> WIND_GENERATOR = GeneratorBuilder
          .createGenerator(() -> GeneratorsTileEntityTypes.WIND_GENERATOR, GeneratorsLang.DESCRIPTION_WIND_GENERATOR)
          .withGui(() -> GeneratorsContainerTypes.WIND_GENERATOR)
          .withEnergyConfig(() -> STORAGE2)
          .withCustomShape(BlockShapes.WIND_GENERATOR)
          .with(AttributeCustomSelectionBox.JAVA)
          .withSound(GeneratorsSounds.WIND_GENERATOR)
          .build();
    // Gas Burning Generator
    public static final Generator<TileEntityGasGenerator> GAS_BURNING_GENERATOR = GeneratorBuilder
          .createGenerator(() -> GeneratorsTileEntityTypes.GAS_BURNING_GENERATOR, GeneratorsLang.DESCRIPTION_GAS_BURNING_GENERATOR)
          .withGui(() -> GeneratorsContainerTypes.GAS_BURNING_GENERATOR)
          .withEnergyConfig(() -> MekanismConfig.general.FROM_H2.get().multiply(1_000))
          .withCustomShape(BlockShapes.GAS_BURNING_GENERATOR)
          .with(AttributeCustomSelectionBox.JSON)
          .withSound(GeneratorsSounds.GAS_BURNING_GENERATOR)
          .build();
    // Advanced Solar Generator
    public static final Generator<TileEntityAdvancedSolarGenerator> ADVANCED_SOLAR_GENERATOR = GeneratorBuilder
          .createGenerator(() -> GeneratorsTileEntityTypes.ADVANCED_SOLAR_GENERATOR, GeneratorsLang.DESCRIPTION_ADVANCED_SOLAR_GENERATOR)
          .withGui(() -> GeneratorsContainerTypes.ADVANCED_SOLAR_GENERATOR)
          .withEnergyConfig(() -> STORAGE2)
          .withCustomShape(BlockShapes.ADVANCED_SOLAR_GENERATOR)
          .withSound(GeneratorsSounds.SOLAR_GENERATOR)
          .build();

    // Turbine Casing
    public static final BlockTypeTile<TileEntityTurbineCasing> TURBINE_CASING = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.TURBINE_CASING, GeneratorsLang.DESCRIPTION_TURBINE_CASING)
          .withGui(() -> GeneratorsContainerTypes.INDUSTRIAL_TURBINE)
          .with(Attributes.MULTIBLOCK, AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Turbine Valve
    public static final BlockTypeTile<TileEntityTurbineValve> TURBINE_VALVE = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.TURBINE_VALVE, GeneratorsLang.DESCRIPTION_TURBINE_VALVE)
          .withGui(() -> GeneratorsContainerTypes.INDUSTRIAL_TURBINE)
          .with(Attributes.COMPARATOR, Attributes.MULTIBLOCK, AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Turbine Vent
    public static final BlockTypeTile<TileEntityTurbineVent> TURBINE_VENT = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.TURBINE_VENT, GeneratorsLang.DESCRIPTION_TURBINE_VENT)
          .withGui(() -> GeneratorsContainerTypes.INDUSTRIAL_TURBINE)
          .with(Attributes.MULTIBLOCK, AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Electromagnetic Coil
    public static final BlockTypeTile<TileEntityElectromagneticCoil> ELECTROMAGNETIC_COIL = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.ELECTROMAGNETIC_COIL, GeneratorsLang.DESCRIPTION_ELECTROMAGNETIC_COIL)
          .with(Attributes.MULTIBLOCK, AttributeMobSpawn.NEVER)
          .build();
    // Rotational Complex
    public static final BlockTypeTile<TileEntityRotationalComplex> ROTATIONAL_COMPLEX = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.ROTATIONAL_COMPLEX, GeneratorsLang.DESCRIPTION_ROTATIONAL_COMPLEX)
          .with(Attributes.MULTIBLOCK, AttributeMobSpawn.NEVER)
          .build();
    // Saturating Condenser
    public static final BlockTypeTile<TileEntitySaturatingCondenser> SATURATING_CONDENSER = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.SATURATING_CONDENSER, GeneratorsLang.DESCRIPTION_SATURATING_CONDENSER)
          .with(Attributes.MULTIBLOCK, AttributeMobSpawn.NEVER)
          .build();
    // Turbine Rotor
    public static final BlockTypeTile<TileEntityTurbineRotor> TURBINE_ROTOR = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.TURBINE_ROTOR, GeneratorsLang.DESCRIPTION_TURBINE_ROTOR)
          .with(Attributes.MULTIBLOCK, AttributeMobSpawn.NEVER)
          .build();

    // Fission Reactor Casing
    public static final BlockTypeTile<TileEntityFissionReactorCasing> FISSION_REACTOR_CASING = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.FISSION_REACTOR_CASING, GeneratorsLang.DESCRIPTION_FISSION_REACTOR_CASING)
          .withGui(() -> GeneratorsContainerTypes.FISSION_REACTOR)
          .withEmptyContainer(GeneratorsContainerTypes.FISSION_REACTOR)
          .withSound(GeneratorsSounds.FISSION_REACTOR)
          .with(Attributes.MULTIBLOCK, AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Fission Reactor Port
    public static final BlockTypeTile<TileEntityFissionReactorPort> FISSION_REACTOR_PORT = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.FISSION_REACTOR_PORT, GeneratorsLang.DESCRIPTION_FISSION_REACTOR_PORT)
          .with(new AttributeStateFissionPortMode(), Attributes.MULTIBLOCK, AttributeMobSpawn.WHEN_NOT_FORMED)
          .withGui(() -> GeneratorsContainerTypes.FISSION_REACTOR)
          .withEmptyContainer(GeneratorsContainerTypes.FISSION_REACTOR)
          .withSound(GeneratorsSounds.FISSION_REACTOR)
          .build();
    // Fission Reactor Logic Adapter
    public static final BlockTypeTile<TileEntityFissionReactorLogicAdapter> FISSION_REACTOR_LOGIC_ADAPTER = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.FISSION_REACTOR_LOGIC_ADAPTER, GeneratorsLang.DESCRIPTION_FISSION_REACTOR_LOGIC_ADAPTER)
          .with(new AttributeRedstoneEmitter<>(tile -> tile.getStatus() == RedstoneStatus.OUTPUTTING ? 15 : 0))
          .with(Attributes.REDSTONE, Attributes.MULTIBLOCK, AttributeMobSpawn.WHEN_NOT_FORMED)
          .withGui(() -> GeneratorsContainerTypes.FISSION_REACTOR_LOGIC_ADAPTER)
          .withEmptyContainer(GeneratorsContainerTypes.FISSION_REACTOR_LOGIC_ADAPTER)
          .withSound(GeneratorsSounds.FISSION_REACTOR)
          .build();
    // Fission Fuel Assembly
    public static final BlockTypeTile<TileEntityFissionFuelAssembly> FISSION_FUEL_ASSEMBLY = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.FISSION_FUEL_ASSEMBLY, GeneratorsLang.DESCRIPTION_FISSION_FUEL_ASSEMBLY)
          .with(Attributes.MULTIBLOCK, AttributeMobSpawn.NEVER)
          .withCustomShape(BlockShapes.FUEL_ASSEMBLY)
          .build();
    // Control Rod Assembly
    public static final BlockTypeTile<TileEntityControlRodAssembly> CONTROL_ROD_ASSEMBLY = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.CONTROL_ROD_ASSEMBLY, GeneratorsLang.DESCRIPTION_CONTROL_ROD_ASSEMBLY)
          .with(Attributes.MULTIBLOCK, AttributeMobSpawn.NEVER)
          .withCustomShape(BlockShapes.CONTROL_ROD_ASSEMBLY)
          .build();

    // Fusion Reactor Controller
    public static final BlockTypeTile<TileEntityFusionReactorController> FUSION_REACTOR_CONTROLLER = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.FUSION_REACTOR_CONTROLLER, GeneratorsLang.DESCRIPTION_FUSION_REACTOR_CONTROLLER)
          .withGui(() -> GeneratorsContainerTypes.FUSION_REACTOR_CONTROLLER)
          .withSound(GeneratorsSounds.FUSION_REACTOR)
          .with(Attributes.ACTIVE, Attributes.INVENTORY, Attributes.MULTIBLOCK, AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Fusion Reactor Port
    public static final BlockTypeTile<TileEntityFusionReactorPort> FUSION_REACTOR_PORT = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.FUSION_REACTOR_PORT, GeneratorsLang.DESCRIPTION_FUSION_REACTOR_PORT)
          .with(Attributes.ACTIVE, Attributes.MULTIBLOCK, AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Fusion Reactor Frame
    public static final BlockTypeTile<TileEntityFusionReactorBlock> FUSION_REACTOR_FRAME = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.FUSION_REACTOR_FRAME, GeneratorsLang.DESCRIPTION_FUSION_REACTOR_FRAME)
          .withEnergyConfig(null, null)
          .with(Attributes.MULTIBLOCK, AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Fusion Reactor Logic Adapter
    public static final BlockTypeTile<TileEntityFusionReactorLogicAdapter> FUSION_REACTOR_LOGIC_ADAPTER = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.FUSION_REACTOR_LOGIC_ADAPTER, GeneratorsLang.DESCRIPTION_FUSION_REACTOR_LOGIC_ADAPTER)
          .withGui(() -> GeneratorsContainerTypes.FUSION_REACTOR_LOGIC_ADAPTER)
          .with(new AttributeRedstoneEmitter<>(tile -> tile.checkMode() ? 15 : 0))
          .with(Attributes.MULTIBLOCK, AttributeMobSpawn.WHEN_NOT_FORMED)
          .withEmptyContainer(GeneratorsContainerTypes.FUSION_REACTOR_LOGIC_ADAPTER)
          .build();
    // Laser Focus Matrix
    public static final BlockTypeTile<TileEntityLaserFocusMatrix> LASER_FOCUS_MATRIX = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.LASER_FOCUS_MATRIX, GeneratorsLang.DESCRIPTION_LASER_FOCUS_MATRIX)
          .with(Attributes.MULTIBLOCK, AttributeMobSpawn.NEVER)
          .build();
    // Reactor Glass
    public static final BlockTypeTile<TileEntityReactorGlass> REACTOR_GLASS = BlockTileBuilder
          .createBlock(() -> GeneratorsTileEntityTypes.REACTOR_GLASS, GeneratorsLang.DESCRIPTION_REACTOR_GLASS)
          .with(Attributes.MULTIBLOCK, AttributeMobSpawn.NEVER)
          .build();
}
