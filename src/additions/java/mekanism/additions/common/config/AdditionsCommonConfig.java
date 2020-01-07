package mekanism.additions.common.config;

import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.fml.config.ModConfig.Type;

public class AdditionsCommonConfig implements IMekanismConfig {

    private final ForgeConfigSpec configSpec;

    public final BooleanValue spawnBabySkeletons;

    AdditionsCommonConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Mekanism Additions Common Config. This config is not sync'd between server and client.").push("additions-common");
        spawnBabySkeletons = builder.comment("Enable the spawning of baby skeletons. Think baby zombies but skeletons.").define("spawnBabySkeletons", true);
        builder.pop();
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