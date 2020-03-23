package mekanism.additions.common.config;

import java.util.ArrayList;
import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedResourceLocationListValue;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.registries.ForgeRegistries;

public class AdditionsCommonConfig extends BaseMekanismConfig {

    private final ForgeConfigSpec configSpec;

    public final SpawnConfig babyCreeper;
    public final SpawnConfig babyEnderman;
    public final SpawnConfig babySkeleton;
    public final SpawnConfig babyStray;
    public final SpawnConfig babyWitherSkeleton;

    AdditionsCommonConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Mekanism Additions Common Config. This config is not sync'd between server and client.").push("additions-common");
        builder.comment("Config options regarding spawning of entities.").push("spawning");
        babyCreeper = new SpawnConfig(this, builder, "baby creepers");
        babyEnderman = new SpawnConfig(this, builder, "baby endermen");
        babySkeleton = new SpawnConfig(this, builder, "baby skeletons");
        babyStray = new SpawnConfig(this, builder, "baby strays");
        babyWitherSkeleton = new SpawnConfig(this, builder, "baby wither skeletons");
        builder.pop(2);
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "additions-common";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.COMMON;
    }

    public static class SpawnConfig {

        public final CachedBooleanValue shouldSpawn;
        public final CachedDoubleValue weightPercentage;
        public final CachedDoubleValue minSizePercentage;
        public final CachedDoubleValue maxSizePercentage;
        public final CachedResourceLocationListValue biomeBlackList;

        private SpawnConfig(IMekanismConfig config, ForgeConfigSpec.Builder builder, String name) {
            builder.comment("Config options regarding " + name + ".").push(name.replaceAll(" ", "-"));
            this.shouldSpawn = CachedBooleanValue.wrap(config, builder.comment("Enable the spawning of " + name + ". Think baby zombies.")
                  .worldRestart()
                  .define("shouldSpawn", true));
            this.weightPercentage = CachedDoubleValue.wrap(config, builder.comment("The multiplier for weight of " + name + " spawns, compared to the adult mob.")
                  .worldRestart()
                  .defineInRange("weightPercentage", 0.5, 0, 100));
            this.minSizePercentage = CachedDoubleValue.wrap(config, builder.comment("The multiplier for minimum group size of " + name + " spawns, compared to the adult mob.")
                  .worldRestart()
                  .defineInRange("minSizePercentage", 0.5, 0, 100));
            this.maxSizePercentage = CachedDoubleValue.wrap(config, builder.comment("The multiplier for maximum group size of " + name + " spawns, compared to the adult mob.")
                  .worldRestart()
                  .defineInRange("maxSizePercentage", 0.5, 0, 100));
            this.biomeBlackList = CachedResourceLocationListValue.wrap(config, builder.comment("The list of biome ids that " + name + " will not spawn in even if the normal mob variant can spawn.")
                  .worldRestart()
                  .defineList("biomeBlackList", new ArrayList<>(), o -> {
                      if (o instanceof String) {
                          ResourceLocation rl = ResourceLocation.tryCreate(((String) o).toLowerCase());
                          if (rl != null) {
                              return ForgeRegistries.BIOMES.containsKey(rl);
                          }
                      }
                      return false;
                  }));
            builder.pop();
        }
    }
}