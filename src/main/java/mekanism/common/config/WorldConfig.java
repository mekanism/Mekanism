package mekanism.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.config.ModConfig.Type;

public class WorldConfig implements IMekanismConfig {

    private final ForgeConfigSpec configSpec;
    public final BooleanValue enableRegeneration;
    public final IntValue userGenVersion;
    public final OreConfig copper;
    public final OreConfig osmium;
    public final OreConfig tin;
    public final SaltConfig salt;

    WorldConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("World generation settings for Mekanism. This config is not synced from server to client").push("world_generation");
        enableRegeneration = builder.comment("Allows chunks to retrogen Mekanism ore blocks.").worldRestart().define("enableRegeneration", false);
        userGenVersion = builder.comment("Change this value to cause Mekanism to regen its ore in all loaded chunks.").defineInRange("userWorldGenVersion", 0, 0, Integer.MAX_VALUE);
        copper = new OreConfig(builder, "copper", true, 16, 8, 0, 0, 60);
        osmium = new OreConfig(builder, "osmium", true, 12, 8, 0, 0, 60);
        tin = new OreConfig(builder, "tin", true, 14, 8, 0, 0, 60);
        salt = new SaltConfig(builder, true, 2, 6, 1);
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

        public final BooleanValue shouldGenerate;
        public final IntValue perChunk;
        public final IntValue maxVeinSize;
        public final ConfigValue<Integer> bottomOffset;
        public final ConfigValue<Integer> topOffset;
        public final IntValue maxHeight;

        private OreConfig(Builder builder, String ore, boolean shouldGenerate, int perChunk, int maxVeinSize, int bottomOffset, int topOffset, int maxHeight) {
            builder.comment("Generation Settings for " + ore + " ore.").push(ore);
            this.shouldGenerate = builder.comment("Determines if " + ore + " ore should be added to world generation.").define("shouldGenerate", shouldGenerate);
            //The max for perChunk and vein size are the values of the max number of blocks in a chunk.
            //TODO: Improve upon it at some point so that the max vein size then gets determined by per chunk as well
            this.perChunk = builder.comment("Chance that " + ore + " generates in a chunk.").defineInRange("perChunk", perChunk, 1, 65536);
            this.maxVeinSize = builder.comment("Maximum number of blocks in a vein of " + ore + ".").defineInRange("maxVeinSize", maxVeinSize, 1, 65536);
            this.maxHeight = builder.comment("Base maximum height (exclusive) that veins of " + ore + " can spawn. Height is calculated by: random.nextInt(maxHeight - topOffset) + bottomOffset").defineInRange("maxHeight", maxHeight, 1, 256);
            this.topOffset = builder.comment("Top offset for calculating height that veins of " + ore + " can spawn. Height is calculated by: random.nextInt(maxHeight - topOffset) + bottomOffset")
                  .define("topOffset", topOffset, value -> {
                      if (value instanceof Integer) {
                          int val = (int) value;
                          return val >= 0 && val < this.maxHeight.get();
                      }
                      return false;
                  });
            this.bottomOffset = builder.comment("Bottom offset for calculating height that veins of " + ore + " can spawn. Height is calculated by: random.nextInt(maxHeight - topOffset) + bottomOffset")
                  .define("bottomOffset", bottomOffset, value -> {
                      if (value instanceof Integer) {
                          int val = (int) value;
                          return val >= 0 && val <= 256 - this.maxHeight.get() + this.topOffset.get();
                      }
                      return false;
                  });
            builder.pop();
        }
    }

    //TODO: If need be make this more generic
    public static class SaltConfig {

        public final BooleanValue shouldGenerate;
        public final IntValue perChunk;
        public final IntValue maxVeinSize;
        public final IntValue ySize;

        private SaltConfig(Builder builder, boolean shouldGenerate, int perChunk, int maxVeinSize, int ySize) {
            builder.comment("Generation Settings for salt.").push("salt");
            this.shouldGenerate = builder.comment("Determines if salt should be added to world generation.").define("shouldGenerate", shouldGenerate);
            //The max for perChunk and vein size are the values of the max number of blocks in a chunk.
            //TODO: Improve upon it at some point so that the max vein size then gets determined by per chunk as well
            this.perChunk = builder.comment("Chance that salt generates in a chunk.").defineInRange("perChunk", perChunk, 1, 65536);
            this.maxVeinSize = builder.comment("Maximum number of blocks in a vein of salt.").defineInRange("maxVeinSize", maxVeinSize, 1, 65536);
            //TODO: Improve the max value of ySize
            this.ySize = builder.comment("Number of blocks to extend up and down when placing a vein of salt.").defineInRange("ySize", ySize, 0, 127);
            builder.pop();
        }
    }
}