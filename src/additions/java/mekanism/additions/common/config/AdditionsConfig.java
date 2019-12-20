package mekanism.additions.common.config;

import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.config.ModConfig.Type;

public class AdditionsConfig implements IMekanismConfig {

    private final ForgeConfigSpec configSpec;

    public final BooleanValue spawnBabySkeletons;
    public final IntValue obsidianTNTDelay;
    public final ConfigValue<Integer> obsidianTNTBlastRadius;
    public final BooleanValue voiceServerEnabled;
    public final IntValue VOICE_PORT;

    AdditionsConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Mekanism Additions Config");

        spawnBabySkeletons = builder.comment("Enable the spawning of baby skeletons. Think baby zombies but skeletons.").define("spawnBabySkeletons", true);
        obsidianTNTDelay = builder.comment("Fuse time for Obsidian TNT.").defineInRange("obsidianTNTDelay", 100, 0, Integer.MAX_VALUE);
        obsidianTNTBlastRadius = builder.comment("Radius of the explosion of Obsidian TNT.").define("obsidianTNTBlastRadius", 12);

        voiceServerEnabled = builder.comment("Enables the voice server for Walkie Talkies.").define("voiceServerEnabled", false);
        VOICE_PORT = builder.comment("TCP port for the Voice server to listen on.").defineInRange("VoicePort", 36123, 1, 65535);

        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "additions";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }
}