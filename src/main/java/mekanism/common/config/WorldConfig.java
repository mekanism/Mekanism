package mekanism.common.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import mekanism.api.functions.FloatSupplier;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.resource.ore.BaseOreConfig;
import mekanism.common.resource.ore.OreType;
import mekanism.common.resource.ore.OreType.OreVeinType;
import mekanism.common.util.EnumUtils;
import mekanism.common.world.height.ConfigurableHeightRange;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class WorldConfig extends BaseMekanismConfig {

    private final ForgeConfigSpec configSpec;
    public final CachedBooleanValue enableRegeneration;
    public final CachedIntValue userGenVersion;

    private final Map<OreType, OreConfig> ores = new EnumMap<>(OreType.class);
    public final SaltConfig salt;

    WorldConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("World generation settings for Mekanism. This config is synced from server to client").push("world_generation");
        enableRegeneration = CachedBooleanValue.wrap(this, builder.comment("Allows chunks to retrogen Mekanism ore blocks.")
              .define("enableRegeneration", false));
        userGenVersion = CachedIntValue.wrap(this, builder.comment("Change this value to cause Mekanism to regen its ore in all loaded chunks.")
              .defineInRange("userWorldGenVersion", 0, 0, Integer.MAX_VALUE));
        for (OreType ore : EnumUtils.ORE_TYPES) {
            ores.put(ore, new OreConfig(this, builder, ore));
        }
        salt = new SaltConfig(this, builder, 2, 2, 3, 1);
        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "world";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }

    @Override
    public boolean addToContainer() {
        return false;
    }

    public OreVeinConfig getVeinConfig(OreVeinType oreVeinType) {
        return ores.get(oreVeinType.type()).veinConfigs.get(oreVeinType.index());
    }

    public record OreVeinConfig(BooleanSupplier shouldGenerate, IntSupplier perChunk, IntSupplier maxVeinSize, FloatSupplier discardChanceOnAirExposure,
                                ConfigurableHeightRange range) {
    }

    private static class OreConfig {

        private final CachedBooleanValue shouldGenerate;
        private final List<OreVeinConfig> veinConfigs;

        private OreConfig(IMekanismConfig config, ForgeConfigSpec.Builder builder, OreType oreType) {
            String ore = oreType.getResource().getRegistrySuffix();
            builder.comment("Generation Settings for " + ore + " ore.").push(ore);
            this.shouldGenerate = CachedBooleanValue.wrap(config, builder.comment("Determines if " + ore + " ore should be added to world generation.")
                  .define("shouldGenerate", true));
            Builder<OreVeinConfig> veinBuilder = ImmutableList.builder();
            for (BaseOreConfig baseConfig : oreType.getBaseConfigs()) {
                String veinType = baseConfig.name() + " " + ore + " vein";
                builder.comment(veinType + " Generation Settings.").push(baseConfig.name());
                CachedBooleanValue shouldVeinTypeGenerate = CachedBooleanValue.wrap(config, builder.comment("Determines if " + veinType + "s should be added to world generation. Note: Requires generating " + ore + " ore to be enabled.")
                      .define("shouldGenerate", true));
                veinBuilder.add(new OreVeinConfig(
                      () -> this.shouldGenerate.get() && shouldVeinTypeGenerate.get(),
                      CachedIntValue.wrap(config, builder.comment("Chance that " + veinType + "s generates in a chunk.")
                            .defineInRange("perChunk", baseConfig.perChunk(), 1, 256)),
                      CachedIntValue.wrap(config, builder.comment("Maximum number of blocks in a " + veinType + ".")
                            .defineInRange("maxVeinSize", baseConfig.maxVeinSize(), 1, 64)),
                      CachedFloatValue.wrap(config, builder.comment("Chance that blocks that are directly exposed to air in a " + veinType + " are not placed.")
                            .defineInRange("discardChanceOnAirExposure", baseConfig.discardChanceOnAirExposure(), 0, 1)),
                      ConfigurableHeightRange.create(config, builder, veinType, baseConfig)
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

        private SaltConfig(IMekanismConfig config, ForgeConfigSpec.Builder builder, int perChunk, int baseRadius, int spread, int ySize) {
            builder.comment("Generation Settings for salt.").push("salt");
            this.shouldGenerate = CachedBooleanValue.wrap(config, builder.comment("Determines if salt should be added to world generation.")
                  .define("shouldGenerate", true));
            //The max for perChunk and vein size are the values of the max number of blocks in a chunk.
            this.perChunk = CachedIntValue.wrap(config, builder.comment("Chance that salt generates in a chunk.")
                  .defineInRange("perChunk", perChunk, 1, (DimensionType.MAX_Y - DimensionType.MIN_Y) / 2));
            this.minRadius = CachedIntValue.wrap(config, builder.comment("Base radius of a vein of salt.")
                  .defineInRange("minRadius", baseRadius, 1, 4));
            this.maxRadius = CachedIntValue.wrap(config, builder.comment("Extended variability (spread) for the radius in a vein of salt.")
                  .define("maxRadius", spread, o -> {
                      if (o instanceof Integer value && value >= 1 && value <= 4) {
                          return value >= this.minRadius.get();
                      }
                      return false;
                  }));
            this.halfHeight = CachedIntValue.wrap(config, builder.comment("Number of blocks to extend up and down when placing a vein of salt.")
                  .defineInRange("halfHeight", ySize, 0, (DimensionType.MAX_Y - DimensionType.MIN_Y - 1) / 2));
            builder.pop();
        }
    }
}