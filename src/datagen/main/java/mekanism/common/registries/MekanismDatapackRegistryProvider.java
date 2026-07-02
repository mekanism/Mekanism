package mekanism.common.registries;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.BasicChemical;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.CleanDirtySlurryId;
import mekanism.api.robit.AdvancementBasedRobitSkin;
import mekanism.api.robit.BasicRobitSkin;
import mekanism.api.robit.RobitSkin;
import mekanism.api.text.EnumColorCollection;
import mekanism.common.ChemicalConstants;
import mekanism.common.Mekanism;
import mekanism.common.chemical.EnumColorPigment;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreVeinConfig;
import mekanism.common.entity.RobitPrideSkinData;
import mekanism.common.registration.impl.MekanismDamageType;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.resource.ore.OreType.OreVeinType;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.EnumUtils;
import mekanism.common.world.ConfigurableConstantInt;
import mekanism.common.world.ConfigurableUniformInt;
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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MekanismDatapackRegistryProvider extends BaseDatapackRegistryProvider {

    public MekanismDatapackRegistryProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, BUILDER, Mekanism.MODID);
    }

    private static final Map<OreType, List<TargetBlockState>> ORE_STONE_TARGETS = new EnumMap<>(OreType.class);
    private static final RuleTest STONE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
    private static final RuleTest DEEPSLATE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

    private static ConfiguredFeature<ResizableOreFeatureConfig, ResizableOreFeature> configureOreFeature(OreVeinType oreVeinType,
          Supplier<? extends ResizableOreFeature> featureRO) {
        OreVeinConfig oreVeinConfig = MekanismConfig.world.getVeinConfig(oreVeinType);
        List<TargetBlockState> targetStates = ORE_STONE_TARGETS.computeIfAbsent(oreVeinType.type(), oreType -> {
            OreBlockType oreBlockType = MekanismBlocks.ORES.get(oreType);
            return List.of(
                  OreConfiguration.target(STONE_ORE_REPLACEABLES, oreBlockType.stone().defaultState()),
                  OreConfiguration.target(DEEPSLATE_ORE_REPLACEABLES, oreBlockType.deepslate().defaultState())
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
                      Identifier name = Mekanism.rl(oreVeinType.name());
                      context.register(configuredFeature(name), configureOreFeature(oreVeinType, MekanismFeatures.ORE));
                      context.register(configuredFeature(name.withSuffix("_retrogen")), configureOreFeature(oreVeinType, MekanismFeatures.ORE_RETROGEN));
                  }
              }
              context.register(configuredFeature(Mekanism.rl("salt")), new ConfiguredFeature<>(MekanismFeatures.DISK.get(), new ResizableDiskConfig(
                    RuleBasedStateProvider.simple(MekanismBlocks.SALT_BLOCK.value()),
                    BlockPredicate.matchesBlocks(Blocks.DIRT, Blocks.CLAY),
                    ConfigurableUniformInt.SALT
              )));
          })
          .add(Registries.PLACED_FEATURE, context -> {
              for (OreType type : EnumUtils.ORE_TYPES) {
                  int features = type.getBaseConfigs().size();
                  for (int vein = 0; vein < features; vein++) {
                      OreVeinType oreVeinType = new OreVeinType(type, vein);
                      OreVeinConfig oreVeinConfig = MekanismConfig.world.getVeinConfig(oreVeinType);
                      Identifier name = Mekanism.rl(oreVeinType.name());
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
                    BlockPredicateFilter.forPredicate(BlockPredicate.matchesFluids(Fluids.WATER)),
                    BiomeFilter.biome()
              ));
          })
          .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
              HolderSet.Named<Biome> isOverworldTag = context.lookup(Registries.BIOME).getOrThrow(MekanismTags.Biomes.SPAWN_ORES);
              HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
              for (OreType type : EnumUtils.ORE_TYPES) {
                  int features = type.getBaseConfigs().size();
                  List<Reference<PlacedFeature>> placedVeins = new ArrayList<>(features);
                  for (int vein = 0; vein < features; vein++) {
                      OreVeinType oreVeinType = new OreVeinType(type, vein);
                      Identifier name = Mekanism.rl(oreVeinType.name());
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
              for (MekanismDamageType damageType : MekanismDamageTypes.DAMAGE_TYPES.damageTypes()) {
                  context.register(damageType.key(), damageType.toVanilla());
              }
          })
          .add(MekanismRegistries.Keys.ROBIT_SKINS, context -> {
              context.register(MekanismRobitSkins.BASE, makeRobitSkin(MekanismRobitSkins.BASE_SKIN_TEXTURE, 2));
              context.register(MekanismRobitSkins.ALLAY, new AdvancementBasedRobitSkin(
                    List.of(
                          Mekanism.rl("allay"),
                          Mekanism.rl("allay2")
                    ),
                    Mekanism.rl("robit/robit_allay"),
                    Identifier.withDefaultNamespace("husbandry/allay_deliver_item_to_player")
              ));
              for (Map.Entry<RobitPrideSkinData, ResourceKey<RobitSkin>> entry : MekanismRobitSkins.PRIDE_SKINS.entrySet()) {
                  ResourceKey<RobitSkin> key = entry.getValue();
                  context.register(key, makeRobitSkin(key.identifier(), entry.getKey().getColor().length));
              }
          })
          .add(MekanismRegistries.Keys.CHEMICAL, context -> {
              context.register(ChemicalIds.EMPTY, BasicChemical.builder().build());
              //Infuse Types
              context.register(ChemicalIds.BIO, BasicChemical.builder(Mekanism.rl("mek_chemical/infuse_type/bio")).tint(0xFF5A4630).build());
              context.register(ChemicalIds.FUNGI, BasicChemical.builder(Mekanism.rl("mek_chemical/infuse_type/fungi")).tint(0xFF74656A).lightLevel(1).build());
              context.register(ChemicalIds.TIN, BasicChemical.infuseType().tint(0xFFCCCCD9).build());
              context.register(ChemicalIds.GOLD, BasicChemical.infuseType().tint(0xFFF2CD67).build());
              context.register(ChemicalIds.REFINED_OBSIDIAN, BasicChemical.infuseType().tint(0xFF7C00ED).build());
              context.register(ChemicalIds.DIAMOND, BasicChemical.infuseType().tint(0xFF6CEDD8).lightLevel(4).build());
              context.register(ChemicalIds.REDSTONE, BasicChemical.infuseType().tint(0xFFB30505).lightLevel(9).build());
              context.register(ChemicalIds.CARBON, BasicChemical.infuseType().tint(0xFF2C2C2C).build());
              //Chemicals
              for (ChemicalConstants constant : ChemicalConstants.values()) {
                  registerConstant(context, constant);
              }
              Chemical steam = BasicChemical.builder(Mekanism.rl("mek_liquid/steam")).build();
              context.register(ChemicalIds.STEAM, steam);
              context.register(ChemicalIds.WATER_VAPOR, steam);
              context.register(ChemicalIds.BRINE, BasicChemical.builder().tint(0xFFFEEF9C).build());

              context.register(ChemicalIds.OSMIUM, BasicChemical.builder().tint(0xFF52BDCA).build());
              context.register(ChemicalIds.FISSILE_FUEL, BasicChemical.builder().tint(0xFF2E332F).build());
              context.register(ChemicalIds.NUCLEAR_WASTE, BasicChemical.builder().tint(0xFF4F412A).build());
              context.register(ChemicalIds.SPENT_NUCLEAR_WASTE, BasicChemical.builder().tint(0xFF262015).build());
              context.register(ChemicalIds.PLUTONIUM, BasicChemical.builder().tint(0xFF1F919C).lightLevel(2).build());
              context.register(ChemicalIds.POLONIUM, BasicChemical.builder().tint(0xFF1B9E7B).lightLevel(2).build());
              context.register(ChemicalIds.ANTIMATTER, BasicChemical.builder().tint(0xFFA464B3).lightLevel(11).build());
              //Pigments
              EnumColorCollection.zipApply(ChemicalIds.SIMPLE_PIGMENTS, EnumColorCollection.VALUES, (pigment, color) -> context.register(pigment, new EnumColorPigment(color)));
              //Slurries
              for (Map.Entry<PrimaryResource, CleanDirtySlurryId> entry : MekanismChemicals.PROCESSED_RESOURCES.entrySet()) {
                  int tint = entry.getKey().getTint();
                  CleanDirtySlurryId slurryId = entry.getValue();
                  context.register(slurryId.clean(), BasicChemical.cleanSlurry().tint(tint).build());
                  context.register(slurryId.dirty(), BasicChemical.dirtySlurry().tint(tint).build());
              }
          })
          ;

    private static RobitSkin makeRobitSkin(Identifier name, int variants) {
        List<Identifier> textures = new ArrayList<>(variants);
        for (int variant = 0; variant < variants; variant++) {
            if (variant == 0) {
                textures.add(name);
            } else {
                textures.add(name.withSuffix(Integer.toString(variant + 1)));
            }
        }
        return new BasicRobitSkin(textures);
    }
}