package mekanism.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public interface IMekanismConfig {

    String getFileName();

    ForgeConfigSpec getConfigSpec();

    ModConfig.Type getConfigType();
}