package mekanism.common.config;

import java.util.EnumMap;
import java.util.Map;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.resource.OreType;
import mekanism.common.util.EnumUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class WorldConfig extends BaseMekanismConfig {

    private final ForgeConfigSpec configSpec;
    public final CachedBooleanValue enableRegeneration;
    public final CachedIntValue userGenVersion;

    public final Map<OreType, OreConfig> ores = new EnumMap<>(OreType.class);
    public final SaltConfig salt;

    WorldConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("World generation settings for Mekanism. This config is synced from server to client").push("world_generation");
        enableRegeneration = CachedBooleanValue.wrap(this, builder.comment("Allows chunks to retrogen Mekanism ore blocks.")
              .define("enableRegeneration", false));
        userGenVersion = CachedIntValue.wrap(this, builder.comment("Change this value to cause Mekanism to regen its ore in all loaded chunks.")
              .defineInRange("userWorldGenVersion", 0, 0, Integer.MAX_VALUE));
        for (OreType ore : EnumUtils.ORE_TYPES) {
            ores.put(ore, new OreConfig(this, builder, ore.getResource().getRegistrySuffix(), true, ore.getPerChunk(), ore.getMaxVeinSize(),
                  ore.getBottomOffset(), ore.getTopOffset(), ore.getMaxHeight()));
        }
        salt = new SaltConfig(this, builder, true, 2, 2, 3, 1);
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

    public static class OreConfig {

        public final CachedBooleanValue shouldGenerate;
        public final CachedIntValue perChunk;
        public final CachedIntValue maxVeinSize;
        public final CachedIntValue bottomOffset;
        public final CachedIntValue topOffset;
        public final CachedIntValue maxHeight;

        private OreConfig(IMekanismConfig config, ForgeConfigSpec.Builder builder, String ore, boolean shouldGenerate, int perChunk, int maxVeinSize, int bottomOffset,
              int topOffset, int maxHeight) {
            builder.comment("Generation Settings for " + ore + " ore.").push(ore);
            this.shouldGenerate = CachedBooleanValue.wrap(config, builder.comment("Determines if " + ore + " ore should be added to world generation.")
                  .define("shouldGenerate", shouldGenerate));
            //The max for perChunk and vein size are the values of the max number of blocks in a chunk.
            //TODO: Improve upon it at some point so that the max vein size then gets determined by per chunk as well
            this.perChunk = CachedIntValue.wrap(config, builder.comment("Chance that " + ore + " generates in a chunk.")
                  .defineInRange("perChunk", perChunk, 1, 128));
            this.maxVeinSize = CachedIntValue.wrap(config, builder.comment("Maximum number of blocks in a vein of " + ore + ".")
                  .defineInRange("maxVeinSize", maxVeinSize, 1, 512));
            //TODO: See if we can use world.getHeight() somehow
            this.maxHeight = CachedIntValue.wrap(config, builder.comment("Maximum height (exclusive) that veins of " + ore + " can spawn. Height is calculated by: random.nextInt(maxHeight - topOffset) + bottomOffset")
                  .defineInRange("maxHeight", maxHeight, 1, 256));
            this.topOffset = CachedIntValue.wrap(config, builder.comment("Top offset for calculating height that veins of " + ore + " can spawn. Height is calculated by: random.nextInt(maxHeight - topOffset) + bottomOffset")
                  .define("topOffset", topOffset, value -> {
                      if (value instanceof Integer) {
                          int val = (int) value;
                          return val >= 0 && val < this.maxHeight.get();
                      }
                      return false;
                  }));
            this.bottomOffset = CachedIntValue.wrap(config, builder.comment("Bottom offset for calculating height that veins of " + ore + " can spawn. Height is calculated by: random.nextInt(maxHeight - topOffset) + bottomOffset")
                  .define("bottomOffset", bottomOffset, value -> {
                      if (value instanceof Integer) {
                          int val = (int) value;
                          return val >= 0 && val <= 256 - this.maxHeight.get() + this.topOffset.get();
                      }
                      return false;
                  }));
            builder.pop();
        }
    }

    //TODO: If need be make this more generic
    public static class SaltConfig {

        public final CachedBooleanValue shouldGenerate;
        public final CachedIntValue perChunk;
        public final CachedIntValue baseRadius;
        public final CachedIntValue spread;
        public final CachedIntValue ySize;

        private SaltConfig(IMekanismConfig config, ForgeConfigSpec.Builder builder, boolean shouldGenerate, int perChunk, int baseRadius, int spread, int ySize) {
            builder.comment("Generation Settings for salt.").push("salt");
            this.shouldGenerate = CachedBooleanValue.wrap(config, builder.comment("Determines if salt should be added to world generation.")
                  .define("shouldGenerate", shouldGenerate));
            //The max for perChunk and vein size are the values of the max number of blocks in a chunk.
            this.perChunk = CachedIntValue.wrap(config, builder.comment("Chance that salt generates in a chunk.")
                  .defineInRange("perChunk", perChunk, 1, 128));
            this.baseRadius = CachedIntValue.wrap(config, builder.comment("Base radius of a vein of salt.")
                  .defineInRange("baseRadius", baseRadius, 1, 4));
            this.spread = CachedIntValue.wrap(config, builder.comment("Extended variability (spread) for the radius in a vein of salt.")
                  .defineInRange("spread", spread, 1, 4));
            //TODO: Improve the max value of ySize
            this.ySize = CachedIntValue.wrap(config, builder.comment("Number of blocks to extend up and down when placing a vein of salt.")
                  .defineInRange("ySize", ySize, 0, 127));
            builder.pop();
        }
    }
}