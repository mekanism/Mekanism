package mekanism.tools.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class ToolsClientConfig extends BaseMekanismConfig {

    private final ForgeConfigSpec configSpec;

    public final CachedBooleanValue displayDurabilityTooltips;

    public ToolsClientConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
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
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.CLIENT;
    }
}