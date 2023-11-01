package mekanism.tools.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.NeoForgeConfigSpec;

public class ToolsClientConfig extends BaseMekanismConfig {

    private final NeoForgeConfigSpec configSpec;

    public final CachedBooleanValue displayDurabilityTooltips;

    public ToolsClientConfig() {
        NeoForgeConfigSpec.Builder builder = new NeoForgeConfigSpec.Builder();
        builder.comment("Mekanism Tools Client Config. This config only exists on the client").push("tools-client");
        this.displayDurabilityTooltips = CachedBooleanValue.wrap(this, builder.comment("Enable durability tooltips for Mekanism Tools gear.")
              .define("displayDurabilityTooltips", true));
        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "tools-client";
    }

    @Override
    public NeoForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.CLIENT;
    }
}