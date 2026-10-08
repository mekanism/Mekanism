package mekanism.common.registries;

import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.BasicChemical;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.CleanDirtySlurryId;
import mekanism.api.robit.AdvancementBasedRobitSkin;
import mekanism.api.robit.BasicRobitSkin;
import mekanism.api.robit.RobitSkin;
import mekanism.api.text.EnumColor;
import mekanism.api.text.EnumColorCollection;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
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
import mekanism.common.util.DatapackRegistryUtils;
import mekanism.common.world.ConfigurableConstantInt;
import mekanism.common.world.ConfigurableUniformInt;
import mekanism.common.world.DisableableFeaturePlacement;
import mekanism.common.world.ResizableDiskReplaceFeature;
import mekanism.common.world.ResizableOreFeature;
import mekanism.common.world.height.ConfigurableHeightProvider;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.HeightMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MekanismDatapackRegistries {

    private MekanismDatapackRegistries() {
    }

    public static void register(GatherDataRegistryEntriesEvent event) {
        event.add(Registries.FEATURE, context -> {
                  RuleTest stoneOreReplaceables = RuleTest.either(new TagMatchTest(BlockTags.HEIGHT_SPECIFIC_ORE_REPLACEABLES), HeightMatchTest.min(0),
                        new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES));
                  RuleTest deepslateOreReplaceables = RuleTest.either(new TagMatchTest(BlockTags.HEIGHT_SPECIFIC_ORE_REPLACEABLES), HeightMatchTest.max(8),
                        new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES));
                  for (OreType type : OreType.VALUES) {
                      int features = type.getBaseConfigs().size();
                      for (int vein = 0; vein < features; vein++) {
                          OreVeinType oreVeinType = new OreVeinType(type, vein);
                          Identifier name = Mekanism.rl(oreVeinType.name());
                          context.register(feature(name), configureOreFeature(stoneOreReplaceables, deepslateOreReplaceables, oreVeinType, false));
                          context.register(feature(name.withSuffix("_retrogen")), configureOreFeature(stoneOreReplaceables, deepslateOreReplaceables, oreVeinType, true));
                      }
                  }
                  context.register(feature(Mekanism.rl("salt")), new ResizableDiskReplaceFeature(
                        BlockStateProvider.holderOf(MekanismBlocks.SALT_BLOCK.value()),
                        BlockPredicate.matchesBlocks(Blocks.DIRT, Blocks.CLAY),
                        ConfigurableUniformInt.SALT
                  ));
              })
              .add(Registries.PLACED_FEATURE, context -> {
                  for (OreType type : OreType.VALUES) {
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
                  for (OreType type : OreType.VALUES) {
                      int features = type.getBaseConfigs().size();
                      List<Reference<PlacedFeature>> placedVeins = new ArrayList<>(features);
                      for (int vein = 0; vein < features; vein++) {
                          OreVeinType oreVeinType = new OreVeinType(type, vein);
                          Identifier name = Mekanism.rl(oreVeinType.name());
                          placedVeins.add(placedFeatures.getOrThrow(placedFeature(name)));
                      }
                      context.register(DatapackRegistryUtils.biomeModifier(Mekanism.rl(type.getSerializedName())), new AddFeaturesBiomeModifier(isOverworldTag, HolderSet.direct(placedVeins),
                            GenerationStep.Decoration.UNDERGROUND_ORES));
                  }
                  Reference<PlacedFeature> placedSalt = placedFeatures.getOrThrow(placedFeature(Mekanism.rl("salt")));
                  context.register(DatapackRegistryUtils.biomeModifier(Mekanism.rl("salt")), new AddFeaturesBiomeModifier(isOverworldTag, HolderSet.direct(placedSalt),
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
                  context.register(ChemicalIds.BIO, BasicChemical.builder(Mekanism.rl("mek_chemical/infuse_type/bio")).colorRepresentation(0xFF5A4630).build());
                  context.register(ChemicalIds.FUNGI, BasicChemical.builder(Mekanism.rl("mek_chemical/infuse_type/fungi")).colorRepresentation(0xFF74656A).lightLevel(1).build());
                  context.register(ChemicalIds.TIN, BasicChemical.infuseType().tint(0xFFCCCCD9).build());
                  context.register(ChemicalIds.GOLD, BasicChemical.infuseType().tint(0xFFF2CD67).build());
                  context.register(ChemicalIds.REFINED_OBSIDIAN, BasicChemical.infuseType().tint(0xFF7C00ED).build());
                  context.register(ChemicalIds.DIAMOND, BasicChemical.infuseType().tint(0xFF6CEDD8).lightLevel(4).build());
                  context.register(ChemicalIds.REDSTONE, BasicChemical.infuseType().tint(0xFFB30505).lightLevel(9).build());
                  context.register(ChemicalIds.CARBON, BasicChemical.infuseType().tint(0xFF2C2C2C).build());
                  //Chemicals
                  for (ChemicalConstants constant : ChemicalConstants.values()) {
                      DatapackRegistryUtils.registerConstant(context, constant);
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
              .add(MekanismRegistries.Keys.UPGRADES, context -> {
                  context.register(UpgradeIds.ANCHOR, Upgrade.create(UpgradeIds.ANCHOR, EnumColor.DARK_GREEN));
                  context.register(UpgradeIds.CHEMICAL, Upgrade.create(UpgradeIds.CHEMICAL, EnumColor.YELLOW, 8));
                  context.register(UpgradeIds.ENERGY, Upgrade.create(UpgradeIds.ENERGY, EnumColor.BRIGHT_GREEN, 8));
                  context.register(UpgradeIds.FILTER, Upgrade.create(UpgradeIds.FILTER, EnumColor.DARK_AQUA));
                  context.register(UpgradeIds.MUFFLING, Upgrade.create(UpgradeIds.MUFFLING, EnumColor.INDIGO));
                  context.register(UpgradeIds.SPEED, Upgrade.create(UpgradeIds.SPEED, EnumColor.RED, 8));
                  context.register(UpgradeIds.STONE_GENERATOR, Upgrade.create(UpgradeIds.STONE_GENERATOR, EnumColor.ORANGE));
              })
              .add(Registries.TRIM_MATERIAL, context -> {
                  DatapackRegistryUtils.registerTrimMaterial(context, MekanismTrimMaterials.ANTIMATTER, 0xD479E5);
                  DatapackRegistryUtils.registerTrimMaterial(context, MekanismTrimMaterials.BRONZE, 0xC38443);
                  DatapackRegistryUtils.registerTrimMaterial(context, MekanismTrimMaterials.FLUORITE, 0xD9F4E9);
                  DatapackRegistryUtils.registerTrimMaterial(context, MekanismTrimMaterials.LEAD, 0x768B89);
                  DatapackRegistryUtils.registerTrimMaterial(context, MekanismTrimMaterials.OSMIUM, 0x8FA6B6);
                  DatapackRegistryUtils.registerTrimMaterial(context, MekanismTrimMaterials.PLUTONIUM, 0x3C8A97);
                  DatapackRegistryUtils.registerTrimMaterial(context, MekanismTrimMaterials.POLONIUM, 0x2A7361);
                  DatapackRegistryUtils.registerTrimMaterial(context, MekanismTrimMaterials.REFINED_GLOWSTONE, 0xE9C567);
                  DatapackRegistryUtils.registerTrimMaterial(context, MekanismTrimMaterials.REFINED_OBSIDIAN, 0x46395A);
                  DatapackRegistryUtils.registerTrimMaterial(context, MekanismTrimMaterials.STEEL, 0x868686);
                  DatapackRegistryUtils.registerTrimMaterial(context, MekanismTrimMaterials.TIN, 0xAA7AEAD);
                  DatapackRegistryUtils.registerTrimMaterial(context, MekanismTrimMaterials.URANIUM, 0x76B36A);
              })
        .add(Registries.CONTEXT_INT_PROVIDER, context -> {
                  HolderGetter<LootItemCondition> predicates = context.lookup(Registries.PREDICATE);

                  Holder.Reference<ContextIntProvider> normalBurnTime = context.register(ContextIntProviders.COOKING_NORMAL_BURN_TIME_REDUCTION_FACTOR, new ConstantValue(1));
                  Holder.Reference<ContextIntProvider> fastBurnTime = context.register(ContextIntProviders.COOKING_FAST_BURN_TIME_REDUCTION_FACTOR, new ConstantValue(2));
                  context.register(MekanismContextIntProviders.COOKING_TIME_CHARCOAL_BLOCK, DatapackRegistryUtils.cooking(predicates, normalBurnTime, fastBurnTime, 16_000));
                  int bioFuelBurnTime = 5 * SharedConstants.TICKS_PER_SECOND;
                  context.register(MekanismContextIntProviders.COOKING_TIME_BIO_FUEL, DatapackRegistryUtils.cooking(predicates, normalBurnTime, fastBurnTime, bioFuelBurnTime));
                  //Note: Similar to how vanilla handles coal -> coal block burn times, we multiply by 10 instead of by 9
                  // so that you get a little bit more bang for your buck
                  context.register(MekanismContextIntProviders.COOKING_TIME_BIO_FUEL_BLOCK, DatapackRegistryUtils.cooking(predicates, normalBurnTime, fastBurnTime, 10 * bioFuelBurnTime));
              })
        ;
    }

    private static void registerPlacedFeature(BootstrapContext<PlacedFeature> context, Identifier name, Boolean2ObjectFunction<List<PlacementModifier>> placementModifiers) {
        registerPlacedFeature(context, name, name, placementModifiers);
    }

    private static void registerPlacedFeature(BootstrapContext<PlacedFeature> context, Identifier name, Identifier retrogenName,
          Boolean2ObjectFunction<List<PlacementModifier>> placementModifiers) {
        HolderGetter<Feature> configuredFeatures = context.lookup(Registries.FEATURE);

        Reference<Feature> configuredFeature = configuredFeatures.getOrThrow(feature(name));
        context.register(placedFeature(name), new PlacedFeature(configuredFeature, placementModifiers.get(false)));

        Reference<Feature> retrogenConfiguredFeature = configuredFeatures.getOrThrow(feature(retrogenName));
        context.register(placedFeature(name.withSuffix("_retrogen")), new PlacedFeature(retrogenConfiguredFeature, placementModifiers.get(true)));
    }

    private static ResourceKey<Feature> feature(Identifier name) {
        return ResourceKey.create(Registries.FEATURE, name);
    }

    private static ResourceKey<PlacedFeature> placedFeature(Identifier name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, name);
    }

    private static ResizableOreFeature configureOreFeature(RuleTest stoneOreReplaceables, RuleTest deepslateOreReplaceables, OreVeinType oreVeinType, boolean retrogen) {
        OreVeinConfig oreVeinConfig = MekanismConfig.world.getVeinConfig(oreVeinType);
        OreBlockType oreBlockType = MekanismBlocks.ORES.get(oreVeinType.type());
        return new ResizableOreFeature(List.of(
              BlockReplacement.replace(stoneOreReplaceables, oreBlockType.stone().defaultState()),
              BlockReplacement.replace(deepslateOreReplaceables, oreBlockType.deepslate().defaultState())
        ), oreVeinType, oreVeinConfig.maxVeinSize(), oreVeinConfig.discardChanceOnAirExposure(), retrogen);
    }

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