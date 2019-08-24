package mekanism.additions.common.config;

import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.config.ModConfig.Type;

public class AdditionsConfig implements IMekanismConfig {

    private final ForgeConfigSpec configSpec;

    public final BooleanValue spawnBabySkeletons;
    public final ConfigValue<Integer> obsidianTNTDelay;
    public final ConfigValue<Integer> obsidianTNTBlastRadius;

    AdditionsConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Mekanism Additions Config");

        //TODO: Use this config
        spawnBabySkeletons = builder.comment("Enable the spawning of baby skeletons. Think baby zombies but skeletons.").define("spawnBabySkeletons", true);
        obsidianTNTDelay = builder.comment("Fuse time for Obsidian TNT.").define("obsidianTNTDelay", 100);
        obsidianTNTBlastRadius = builder.comment("Radius of the explosion of Obsidian TNT.").define("obsidianTNTBlastRadius", 12);

        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "mekanism-additions.toml";
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