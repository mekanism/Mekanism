package mekanism.common.registries;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreVeinConfig;
import mekanism.common.registration.impl.FeatureDeferredRegister;
import mekanism.common.registration.impl.FeatureRegistryObject;
import mekanism.common.registration.impl.SetupFeatureDeferredRegister;
import mekanism.common.registration.impl.SetupFeatureDeferredRegister.MekFeature;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.resource.ore.OreType.OreVeinType;
import mekanism.common.util.EnumUtils;
import mekanism.common.world.ConfigurableConstantInt;
import mekanism.common.world.DisableableFeaturePlacement;
import mekanism.common.world.OreRetrogenFeature;
import mekanism.common.world.ResizableDiskConfig;
import mekanism.common.world.ResizableDiskReplaceFeature;
import mekanism.common.world.ResizableOreFeature;
import mekanism.common.world.ResizableOreFeatureConfig;
import mekanism.common.world.height.ConfigurableHeightProvider;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;

public class MekanismFeatures {

    private MekanismFeatures() {
    }

    public static final FeatureDeferredRegister FEATURES = new FeatureDeferredRegister(Mekanism.MODID);
    public static final SetupFeatureDeferredRegister SETUP_FEATURES = new SetupFeatureDeferredRegister(Mekanism.MODID);

    public static final FeatureRegistryObject<ResizableDiskConfig, ResizableDiskReplaceFeature> DISK = FEATURES.register("disk", () -> new ResizableDiskReplaceFeature(ResizableDiskConfig.CODEC));
    public static final FeatureRegistryObject<ResizableOreFeatureConfig, ResizableOreFeature> ORE = FEATURES.register("ore", ResizableOreFeature::new);
    public static final FeatureRegistryObject<ResizableOreFeatureConfig, OreRetrogenFeature> ORE_RETROGEN = FEATURES.register("ore_retrogen", OreRetrogenFeature::new);

    private static final Map<OreType, List<TargetBlockState>> ORE_STONE_TARGETS = new EnumMap<>(OreType.class);
    public static final Map<OreType, MekFeature<ResizableOreFeatureConfig, ResizableOreFeature>[]> ORES = new EnumMap<>(OreType.class);

    static {
        for (OreType type : EnumUtils.ORE_TYPES) {
            int features = type.getBaseConfigs().size();
            //noinspection unchecked
            MekFeature<ResizableOreFeatureConfig, ResizableOreFeature>[] oreFeatures = new MekFeature[features];
            for (int vein = 0; vein < features; vein++) {
                OreVeinType oreVeinType = new OreVeinType(type, vein);
                oreFeatures[vein] = SETUP_FEATURES.register(oreVeinType.name(),
                      () -> configureOreFeature(oreVeinType, MekanismFeatures.ORE),
                      () -> configureOreFeature(oreVeinType, MekanismFeatures.ORE_RETROGEN),
                      retrogen -> {
                          OreVeinConfig oreVeinConfig = MekanismConfig.world.getVeinConfig(oreVeinType);
                          return List.of(
                                new DisableableFeaturePlacement(oreVeinType, oreVeinConfig.shouldGenerate(), retrogen),
                                CountPlacement.of(new ConfigurableConstantInt(oreVeinType, oreVeinConfig.perChunk())),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.of(ConfigurableHeightProvider.of(oreVeinType, oreVeinConfig)),
                                BiomeFilter.biome()
                          );
                      }
                );
            }
            ORES.put(type, oreFeatures);
        }
    }

    public static final MekFeature<ResizableDiskConfig, ResizableDiskReplaceFeature> SALT = SETUP_FEATURES.register("salt",
          () -> new ConfiguredFeature<>(DISK.get(), new ResizableDiskConfig(MekanismBlocks.SALT_BLOCK.getBlock().defaultBlockState(), MekanismConfig.world.salt)),
          retrogen -> List.of(
                new DisableableFeaturePlacement(null, MekanismConfig.world.salt.shouldGenerate, retrogen),
                CountPlacement.of(new ConfigurableConstantInt(null, MekanismConfig.world.salt.perChunk)),
                InSquarePlacement.spread(),
                retrogen ? PlacementUtils.HEIGHTMAP_OCEAN_FLOOR : PlacementUtils.HEIGHTMAP_TOP_SOLID,
                BiomeFilter.biome()
          )
    );

    private static ConfiguredFeature<ResizableOreFeatureConfig, ResizableOreFeature> configureOreFeature(OreVeinType oreVeinType,
          FeatureRegistryObject<ResizableOreFeatureConfig, ? extends ResizableOreFeature> featureRO) {
        OreVeinConfig oreVeinConfig = MekanismConfig.world.getVeinConfig(oreVeinType);
        List<TargetBlockState> targetStates = ORE_STONE_TARGETS.computeIfAbsent(oreVeinType.type(), oreType -> {
            OreBlockType oreBlockType = MekanismBlocks.ORES.get(oreType);
            return List.of(
                  OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, oreBlockType.stoneBlock().defaultBlockState()),
                  OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, oreBlockType.deepslateBlock().defaultBlockState())
            );
        });
        return new ConfiguredFeature<>(featureRO.get(), new ResizableOreFeatureConfig(targetStates, oreVeinType, oreVeinConfig.maxVeinSize(),
              oreVeinConfig.discardChanceOnAirExposure()));
    }
}