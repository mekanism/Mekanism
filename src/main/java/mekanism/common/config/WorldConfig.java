package mekanism.common.config;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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

    public final Map<OreType, OreConfig> ores = new Object2ObjectOpenHashMap<>();
    public final SaltConfig salt;

    WorldConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("World generation settings for Mekanism. This config is not synced from server to client").push("world_generation");
        enableRegeneration = CachedBooleanValue.wrap(this, builder.comment("Allows chunks to retrogen Mekanism ore blocks.")
              .worldRestart()
              .define("enableRegeneration", false));
        userGenVersion = CachedIntValue.wrap(this, builder.comment("Change this value to cause Mekanism to regen its ore in all loaded chunks.")
              .defineInRange("userWorldGenVersion", 0, 0, Integer.MAX_VALUE));
        for (OreType ore : EnumUtils.ORE_TYPES) {
            ores.put(ore, new OreConfig(this, builder, ore.getResource().getRegistrySuffix(), true, ore.getPerChunk(), ore.getMaxVeinSize(), ore.getMaxHeight()));
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
        return Type.COMMON;
    }

    @Override
    public boolean addToContainer() {
        return false;
    }

    public static class OreConfig {

        public final CachedBooleanValue shouldGenerate;
        public final CachedIntValue perChunk;
        public final CachedIntValue maxVeinSize;
        public final CachedIntValue maxHeight;

        private OreConfig(IMekanismConfig config, ForgeConfigSpec.Builder builder, String ore, boolean shouldGenerate, int perChunk, int maxVeinSize, int maxHeight) {
            builder.comment("Generation Settings for " + ore + " ore.").push(ore);
            this.shouldGenerate = CachedBooleanValue.wrap(config, builder.comment("Determines if " + ore + " ore should be added to world generation.")
                  .define("shouldGenerate", shouldGenerate));
            //The max for perChunk and vein size are the values of the max number of blocks in a chunk.
            //TODO: Improve upon it at some point so that the max vein size then gets determined by per chunk as well
            this.perChunk = CachedIntValue.wrap(config, builder.comment("Chance that " + ore + " generates in a chunk.")
                  .defineInRange("perChunk", perChunk, 1, 512));
            this.maxVeinSize = CachedIntValue.wrap(config, builder.comment("Maximum number of blocks in a vein of " + ore + ".")
                  .defineInRange("maxVeinSize", maxVeinSize, 1, 512));
            //TODO: See if we can use world.getHeight() somehow
            this.maxHeight = CachedIntValue.wrap(config, builder.comment("Maximum height (exclusive) that veins of " + ore + " can spawn.")
                  .defineInRange("maxHeight", maxHeight, 1, 256));
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
            //TODO: Improve upon it at some point so that the max vein size then gets determined by per chunk as well
            this.perChunk = CachedIntValue.wrap(config, builder.comment("Chance that salt generates in a chunk.")
                  .defineInRange("perChunk", perChunk, 1, 65536));
            this.baseRadius = CachedIntValue.wrap(config, builder.comment("Base radius of a vein of salt.")
                  .defineInRange("baseRadius", baseRadius, 1, 65536));
            this.spread = CachedIntValue.wrap(config, builder.comment("Extended variability (spread) for the radius in a vein of salt.")
                  .defineInRange("spread", spread, 1, 65536));
            //TODO: Improve the max value of ySize
            this.ySize = CachedIntValue.wrap(config, builder.comment("Number of blocks to extend up and down when placing a vein of salt.")
                  .defineInRange("ySize", ySize, 0, 127));
            builder.pop();
        }
    }
}