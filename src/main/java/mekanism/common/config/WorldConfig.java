package mekanism.common.config;

import com.google.common.collect.ImmutableList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import mekanism.api.functions.FloatSupplier;
import mekanism.common.config.MekanismConfigTranslations.OreConfigTranslations;
import mekanism.common.config.MekanismConfigTranslations.OreVeinConfigTranslations;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.resource.ore.BaseOreConfig;
import mekanism.common.resource.ore.OreType;
import mekanism.common.resource.ore.OreType.OreVeinType;
import mekanism.common.util.EnumUtils;
import mekanism.common.world.height.ConfigurableHeightRange;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class WorldConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;
    public final CachedBooleanValue enableRegeneration;
    public final CachedIntValue userGenVersion;

    private final Map<OreType, OreConfig> ores = new EnumMap<>(OreType.class);
    public final SaltConfig salt;

    WorldConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        enableRegeneration = CachedBooleanValue.wrap(this, MekanismConfigTranslations.WORLD_RETROGEN.applyToBuilder(builder)
              .define("enableRegeneration", false));
        userGenVersion = CachedIntValue.wrap(this, MekanismConfigTranslations.WORLD_WORLD_VERSION.applyToBuilder(builder)
              .defineInRange("userWorldGenVersion", 0, 0, Integer.MAX_VALUE));
        for (OreType ore : EnumUtils.ORE_TYPES) {
            ores.put(ore, new OreConfig(this, builder, ore));
        }
        salt = new SaltConfig(this, builder, 2, 2, 3, 1);

        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "world";
    }

    @Override
    public String getTranslation() {
        return "World Config";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }

    public OreVeinConfig getVeinConfig(OreVeinType oreVeinType) {
        return ores.get(oreVeinType.type()).veinConfigs.get(oreVeinType.index());
    }

    public record OreVeinConfig(BooleanSupplier shouldGenerate, CachedIntValue perChunk, IntSupplier maxVeinSize, FloatSupplier discardChanceOnAirExposure,
                                ConfigurableHeightRange range) {
    }

    private static class OreConfig {

        private final CachedBooleanValue shouldGenerate;
        private final List<OreVeinConfig> veinConfigs;

        private OreConfig(IMekanismConfig config, ModConfigSpec.Builder builder, OreType oreType) {
            String ore = oreType.getResource().getRegistrySuffix();
            OreConfigTranslations translations = OreConfigTranslations.create(ore);
            translations.topLevel().applyToBuilder(builder).push(ore);
            this.shouldGenerate = CachedBooleanValue.wrap(config, translations.shouldGenerate().applyToBuilder(builder).define("shouldGenerate", true));

            ImmutableList.Builder<OreVeinConfig> veinBuilder = ImmutableList.builder();
            for (BaseOreConfig baseConfig : oreType.getBaseConfigs()) {
                OreVeinConfigTranslations veinTranslations = OreVeinConfigTranslations.create(ore, baseConfig.name());
                veinTranslations.topLevel().applyToBuilder(builder).push(baseConfig.name());
                CachedBooleanValue shouldVeinTypeGenerate = CachedBooleanValue.wrap(config, veinTranslations.shouldGenerate().applyToBuilder(builder)
                      .define("shouldGenerate", true));
                veinBuilder.add(new OreVeinConfig(
                      () -> this.shouldGenerate.get() && shouldVeinTypeGenerate.get(),
                      CachedIntValue.wrap(config, veinTranslations.perChunk().applyToBuilder(builder)
                            .defineInRange("perChunk", baseConfig.perChunk(), 1, 256)),
                      CachedIntValue.wrap(config, veinTranslations.maxVeinSize().applyToBuilder(builder)
                            .defineInRange("maxVeinSize", baseConfig.maxVeinSize(), 1, 64)),
                      CachedFloatValue.wrap(config, veinTranslations.discardChanceOnAirExposure().applyToBuilder(builder)
                            .defineInRange("discardChanceOnAirExposure", baseConfig.discardChanceOnAirExposure(), 0, 1)),
                      ConfigurableHeightRange.create(config, builder, veinTranslations, baseConfig)
                ));
                builder.pop();
            }
            veinConfigs = veinBuilder.build();
            builder.pop();
        }
    }

    //TODO: If need be make this more generic
    public static class SaltConfig {

        public final CachedBooleanValue shouldGenerate;
        public final CachedIntValue perChunk;
        public final CachedIntValue minRadius;
        public final CachedIntValue maxRadius;
        public final CachedIntValue halfHeight;

        private SaltConfig(IMekanismConfig config, ModConfigSpec.Builder builder, int perChunk, int baseRadius, int spread, int ySize) {
            MekanismConfigTranslations.WORLD_SALT.applyToBuilder(builder).push("salt");
            this.shouldGenerate = CachedBooleanValue.wrap(config, MekanismConfigTranslations.WORLD_SALT_SHOULD_GENERATE.applyToBuilder(builder)
                  .define("shouldGenerate", true));
            //The max for perChunk and vein size are the values of the max number of blocks in a chunk.
            this.perChunk = CachedIntValue.wrap(config, MekanismConfigTranslations.WORLD_SALT_PER_CHUNK.applyToBuilder(builder)
                  .defineInRange("perChunk", perChunk, 1, 256));
            this.minRadius = CachedIntValue.wrap(config, MekanismConfigTranslations.WORLD_SALT_RADIUS_MIN.applyToBuilder(builder)
                  .defineInRange("minRadius", baseRadius, 1, 4));
            this.maxRadius = CachedIntValue.wrap(config, MekanismConfigTranslations.WORLD_SALT_RADIUS_MAX.applyToBuilder(builder).define("maxRadius", spread, o -> {
                if (o instanceof Integer value && value >= 1 && value <= 4) {
                    return value >= this.minRadius.getOrDefault();
                }
                return false;
            }));
            this.halfHeight = CachedIntValue.wrap(config, MekanismConfigTranslations.WORLD_SALT_HALF_HEIGHT.applyToBuilder(builder)
                  .defineInRange("halfHeight", ySize, 0, (DimensionType.MAX_Y - DimensionType.MIN_Y - 1) / 2));
            builder.pop();
        }
    }
}