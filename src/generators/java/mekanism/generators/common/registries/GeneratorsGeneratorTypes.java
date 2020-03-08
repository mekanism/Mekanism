package mekanism.generators.common.registries;

import mekanism.api.Pos3D;
import mekanism.common.config.MekanismConfig;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.content.blocktype.BlockShapes;
import mekanism.generators.common.content.blocktype.Generator;
import mekanism.generators.common.content.blocktype.Generator.GeneratorBuilder;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.particles.ParticleTypes;

public class GeneratorsGeneratorTypes {

    // Heat Generator
    public static final Generator<TileEntityHeatGenerator> HEAT_GENERATOR = GeneratorBuilder
          .createGenerator(GeneratorsTileEntityTypes.HEAT_GENERATOR, GeneratorsContainerTypes.HEAT_GENERATOR, GeneratorsLang.DESCRIPTION_HEAT_GENERATOR)
          .withConfig(() -> 160000)
          .withCustomShape(BlockShapes.HEAT_GENERATOR)
          .withSound(GeneratorsSounds.HEAT_GENERATOR)
          .addParticleFX(ParticleTypes.SMOKE, (rand) -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, -0.52))
          .addParticleFX(ParticleTypes.FLAME, (rand) -> new Pos3D(rand.nextFloat() * 0.6F - 0.3F, rand.nextFloat() * 6.0F / 16.0F, -0.52))
          .build();
    // Bio Generator
    public static final Generator<TileEntityBioGenerator> BIO_GENERATOR = GeneratorBuilder
          .createGenerator(GeneratorsTileEntityTypes.BIO_GENERATOR, GeneratorsContainerTypes.BIO_GENERATOR, GeneratorsLang.DESCRIPTION_BIO_GENERATOR)
          .withConfig(() -> 160000)
          .withCustomShape(BlockShapes.BIO_GENERATOR)
          .withSound(GeneratorsSounds.BIO_GENERATOR)
          .addParticleFX(ParticleTypes.SMOKE, (rand) -> new Pos3D(0, 0.3, -0.25))
          .build();
    // Solar Generator
    public static final Generator<TileEntitySolarGenerator> SOLAR_GENERATOR = GeneratorBuilder
          .createGenerator(GeneratorsTileEntityTypes.SOLAR_GENERATOR, GeneratorsContainerTypes.SOLAR_GENERATOR, GeneratorsLang.DESCRIPTION_SOLAR_GENERATOR)
          .withConfig(() -> 96000)
          .withCustomShape(BlockShapes.SOLAR_GENERATOR)
          .withSound(GeneratorsSounds.SOLAR_GENERATOR)
          .build();
    // Wind Generator
    public static final Generator<TileEntityWindGenerator> WIND_GENERATOR = GeneratorBuilder
          .createGenerator(GeneratorsTileEntityTypes.WIND_GENERATOR, GeneratorsContainerTypes.WIND_GENERATOR, GeneratorsLang.DESCRIPTION_WIND_GENERATOR)
          .withConfig(() -> 200_000)
          .withCustomShape(BlockShapes.WIND_GENERATOR)
          .withSound(GeneratorsSounds.WIND_GENERATOR)
          .build();
    // Gas Burning Generator
    public static final Generator<TileEntityGasGenerator> GAS_BURNING_GENERATOR = GeneratorBuilder
          .createGenerator(GeneratorsTileEntityTypes.GAS_BURNING_GENERATOR, GeneratorsContainerTypes.GAS_BURNING_GENERATOR, GeneratorsLang.DESCRIPTION_GAS_BURNING_GENERATOR)
          .withConfig(() -> 1000 * MekanismConfig.general.FROM_H2.get())
          .withCustomShape(BlockShapes.GAS_BURNING_GENERATOR)
          .withSound(GeneratorsSounds.GAS_BURNING_GENERATOR)
          .build();
    // Advanced Solar Generator
    public static final Generator<TileEntityAdvancedSolarGenerator> ADVANCED_SOLAR_GENERATOR = GeneratorBuilder
          .createGenerator(GeneratorsTileEntityTypes.ADVANCED_SOLAR_GENERATOR, GeneratorsContainerTypes.ADVANCED_SOLAR_GENERATOR, GeneratorsLang.DESCRIPTION_ADVANCED_SOLAR_GENERATOR)
          .withConfig(() -> 200_000)
          .withCustomShape(BlockShapes.ADVANCED_SOLAR_GENERATOR)
          .withSound(GeneratorsSounds.SOLAR_GENERATOR)
          .build();
}
