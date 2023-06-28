package mekanism.common.registries;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreVeinConfig;
import mekanism.common.registration.impl.FeatureRegistryObject;
import mekanism.common.registries.MekanismDamageTypes.MekanismDamageType;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.resource.ore.OreType.OreVeinType;
import mekanism.common.util.EnumUtils;
import mekanism.common.world.ConfigurableConstantInt;
import mekanism.common.world.DisableableFeaturePlacement;
import mekanism.common.world.ResizableDiskConfig;
import mekanism.common.world.ResizableOreFeature;
import mekanism.common.world.ResizableOreFeatureConfig;
import mekanism.common.world.height.ConfigurableHeightProvider;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.common.world.ForgeBiomeModifiers.AddFeaturesBiomeModifier;
import net.minecraftforge.registries.ForgeRegistries;

public class MekanismDatapackRegistryProvider extends BaseDatapackRegistryProvider {

    public MekanismDatapackRegistryProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, BUILDER, Mekanism.MODID);
    }

    private static final Map<OreType, List<TargetBlockState>> ORE_STONE_TARGETS = new EnumMap<>(OreType.class);
    private static final RuleTest STONE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
    private static final RuleTest DEEPSLATE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

    private static ConfiguredFeature<ResizableOreFeatureConfig, ResizableOreFeature> configureOreFeature(OreVeinType oreVeinType,
          FeatureRegistryObject<ResizableOreFeatureConfig, ? extends ResizableOreFeature> featureRO) {
        OreVeinConfig oreVeinConfig = MekanismConfig.world.getVeinConfig(oreVeinType);
        List<TargetBlockState> targetStates = ORE_STONE_TARGETS.computeIfAbsent(oreVeinType.type(), oreType -> {
            OreBlockType oreBlockType = MekanismBlocks.ORES.get(oreType);
            return List.of(
                  OreConfiguration.target(STONE_ORE_REPLACEABLES, oreBlockType.stoneBlock().defaultBlockState()),
                  OreConfiguration.target(DEEPSLATE_ORE_REPLACEABLES, oreBlockType.deepslateBlock().defaultBlockState())
            );
        });
        return new ConfiguredFeature<>(featureRO.get(), new ResizableOreFeatureConfig(targetStates, oreVeinType, oreVeinConfig.maxVeinSize(),
              oreVeinConfig.discardChanceOnAirExposure()));
    }

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
          .add(Registries.CONFIGURED_FEATURE, context -> {
              for (OreType type : EnumUtils.ORE_TYPES) {
                  int features = type.getBaseConfigs().size();
                  for (int vein = 0; vein < features; vein++) {
                      OreVeinType oreVeinType = new OreVeinType(type, vein);
                      ResourceLocation name = Mekanism.rl(oreVeinType.name());
                      context.register(configuredFeature(name), configureOreFeature(oreVeinType, MekanismFeatures.ORE));
                      context.register(configuredFeature(name.withSuffix("_retrogen")), configureOreFeature(oreVeinType, MekanismFeatures.ORE_RETROGEN));
                  }
              }
              context.register(configuredFeature(Mekanism.rl("salt")), new ConfiguredFeature<>(MekanismFeatures.DISK.get(),
                    new ResizableDiskConfig(MekanismBlocks.SALT_BLOCK.getBlock().defaultBlockState(), MekanismConfig.world.salt)));
          })
          .add(Registries.PLACED_FEATURE, context -> {
              for (OreType type : EnumUtils.ORE_TYPES) {
                  int features = type.getBaseConfigs().size();
                  for (int vein = 0; vein < features; vein++) {
                      OreVeinType oreVeinType = new OreVeinType(type, vein);
                      OreVeinConfig oreVeinConfig = MekanismConfig.world.getVeinConfig(oreVeinType);
                      ResourceLocation name = Mekanism.rl(oreVeinType.name());
                      registerPlacedFeature(context, name, name.withSuffix("_retrogen"), retrogen -> List.of(
                            new DisableableFeaturePlacement(oreVeinType, oreVeinConfig.shouldGenerate(), retrogen),
                            CountPlacement.of(new ConfigurableConstantInt(oreVeinType, oreVeinConfig.perChunk())),
                            InSquarePlacement.spread(),
                            HeightRangePlacement.of(ConfigurableHeightProvider.of(oreVeinType, oreVeinConfig)),
                            BiomeFilter.biome()
                      ));
                  }
              }
              registerPlacedFeature(context, Mekanism.rl("salt"), retrogen -> List.of(
                    new DisableableFeaturePlacement(null, MekanismConfig.world.salt.shouldGenerate, retrogen),
                    CountPlacement.of(new ConfigurableConstantInt(null, MekanismConfig.world.salt.perChunk)),
                    InSquarePlacement.spread(),
                    retrogen ? PlacementUtils.HEIGHTMAP_OCEAN_FLOOR : PlacementUtils.HEIGHTMAP_TOP_SOLID,
                    BiomeFilter.biome()
              ));
          })
          .add(ForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
              //TODO - 1.20: Replace the overworld tag check with a mekanism biome tag that just defaults to containing the overworld
              // that way it will be easier for people to add mining dims to have our ores in them
              HolderSet.Named<Biome> isOverworldTag = context.lookup(Registries.BIOME).getOrThrow(BiomeTags.IS_OVERWORLD);
              HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
              for (OreType type : EnumUtils.ORE_TYPES) {
                  int features = type.getBaseConfigs().size();
                  List<Reference<PlacedFeature>> placedVeins = new ArrayList<>(features);
                  for (int vein = 0; vein < features; vein++) {
                      OreVeinType oreVeinType = new OreVeinType(type, vein);
                      ResourceLocation name = Mekanism.rl(oreVeinType.name());
                      placedVeins.add(placedFeatures.getOrThrow(placedFeature(name)));
                  }
                  context.register(biomeModifier(Mekanism.rl(type.getSerializedName())), new AddFeaturesBiomeModifier(isOverworldTag, HolderSet.direct(placedVeins),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
              }
              Reference<PlacedFeature> placedSalt = placedFeatures.getOrThrow(placedFeature(Mekanism.rl("salt")));
              context.register(biomeModifier(Mekanism.rl("salt")), new AddFeaturesBiomeModifier(isOverworldTag, HolderSet.direct(placedSalt),
                    GenerationStep.Decoration.UNDERGROUND_ORES));
          })
          .add(Registries.DAMAGE_TYPE, context -> {
              for (MekanismDamageType damageType : MekanismDamageTypes.DAMAGE_TYPES.values()) {
                  context.register(damageType.key(), new DamageType(damageType.getMsgId(), damageType.exhaustion()));
              }
          })
          ;
}