package mekanism.additions.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedIntValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class AdditionsConfig extends BaseMekanismConfig {

    private final ForgeConfigSpec configSpec;

    public final CachedIntValue obsidianTNTDelay;
    public final CachedIntValue obsidianTNTBlastRadius;
    public final CachedBooleanValue voiceServerEnabled;
    public final CachedIntValue voicePort;

    AdditionsConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Mekanism Additions Config. This config is synced between server and client.").push("additions");

        obsidianTNTDelay = CachedIntValue.wrap(this, builder.comment("Fuse time for Obsidian TNT.")
              .defineInRange("obsidianTNTDelay", 100, 0, Integer.MAX_VALUE));
        obsidianTNTBlastRadius = CachedIntValue.wrap(this, builder.comment("Radius of the explosion of Obsidian TNT.")
              .define("obsidianTNTBlastRadius", 12));

        voiceServerEnabled = CachedBooleanValue.wrap(this, builder.comment("Enables the voice server for Walkie Talkies.")
              .define("voiceServerEnabled", false));
        voicePort = CachedIntValue.wrap(this, builder.comment("TCP port for the Voice server to listen on.")
              .defineInRange("VoicePort", 36_123, 1, 65_535));
        builder.pop();
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