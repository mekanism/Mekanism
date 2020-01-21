package mekanism.additions.common.config;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.config.IMekanismConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.registries.ForgeRegistries;

public class AdditionsCommonConfig implements IMekanismConfig {

    private final ForgeConfigSpec configSpec;

    public final BooleanValue spawnBabySkeletons;
    public final IntValue babySkeletonWeight;
    public final IntValue babySkeletonMinSize;
    public final ConfigValue<Integer> babySkeletonMaxSize;
    public final ConfigValue<List<? extends String>> babySkeletonBlackList;

    AdditionsCommonConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Mekanism Additions Common Config. This config is not sync'd between server and client.").push("additions-common");
        builder.comment("Config options regarding baby skeletons.").push("baby-skeletons");
        spawnBabySkeletons = builder.comment("Enable the spawning of baby skeletons. Think baby zombies but skeletons.").worldRestart().define("shouldSpawn", true);
        babySkeletonWeight = builder.comment("The weight that a baby skeleton spawns.").worldRestart().defineInRange("weight", 40, 1, 100);
        babySkeletonMinSize = builder.comment("The minimum group size of how many baby skeletons can spawn.").worldRestart().defineInRange("minSize", 1, 1, Integer.MAX_VALUE - 1);
        babySkeletonMaxSize = builder.comment("The maximum group size of how many baby skeletons can spawn.").worldRestart()
              .define("maxSize", 3, value -> {
                  if (value instanceof Integer) {
                      int val = (int) value;
                      int minSize = babySkeletonMinSize.get();
                      return minSize <= val && val - minSize <= Integer.MAX_VALUE - 1;
                  }
                  return false;
              });
        babySkeletonBlackList = builder.comment("The list of biome ids that baby skeletons will not spawn in even if normal skeletons can spawn in.").worldRestart()
              .defineList("biomeBlackList", new ArrayList<>(), o -> {
                  if (o instanceof String) {
                      String string = ((String) o).toLowerCase();
                      if (ResourceLocation.isResouceNameValid(string)) {
                          ResourceLocation biome = new ResourceLocation(string);
                          return ForgeRegistries.BIOMES.containsKey(biome);
                      }
                  }
                  return false;
              });
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
}