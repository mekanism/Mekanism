package mekanism.common.config;

import java.util.HashMap;
import java.util.Map;
import mekanism.common.MekanismBlock;
import mekanism.common.base.IBlockProvider;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.fml.config.ModConfig.Type;

//TODO
public class GeneralConfig implements IMekanismConfig {

    private static final String ENABLED_CATEGORY = "enabled_machines";

    private final ForgeConfigSpec configSpec;

    public final Map<IBlockProvider, BooleanValue> enabledMachines = new HashMap<>();

    GeneralConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("General Config");

        builder.comment("Enabled Machines").push(ENABLED_CATEGORY);
        for (IBlockProvider blockProvider : MekanismBlock.values()) {
            if (blockProvider.canBeDisabled()) {
                BooleanValue enabled = builder.comment("Allow " + blockProvider.getName() + " to be used/crafted. Requires game restart to fully take effect.")
                      .define(blockProvider.getName(), blockProvider.isEnabled());
                //TODO: Should this be passed up to the blocks to keep track of them more directly
                enabledMachines.put(blockProvider, enabled);
                //TODO: Actually make the different blocks in MekanismBlock that can be disabled, implement the correct interface
            }
        }
        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "mekanism-general.toml";
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