package mekanism.common.config;

import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedEnumValue;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class CommonConfig extends BaseMekanismConfig {

    private final ForgeConfigSpec configSpec;

    public final CachedEnumValue<EnergyUnit> energyUnit;
    public final CachedEnumValue<TemperatureUnit> tempUnit;
    public final CachedBooleanValue enableDecayTimers;

    CommonConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Mekanism Common Config. This config is not synced between server and client.").push("common");
        energyUnit = CachedEnumValue.wrap(this, builder.comment("Displayed energy type in Mekanism GUIs and network reader readings.")
              .defineEnum("energyType", EnergyUnit.FORGE_ENERGY));
        tempUnit = CachedEnumValue.wrap(this, builder.comment("Displayed temperature unit in Mekanism GUIs and network reader readings.")
              .defineEnum("temperatureUnit", TemperatureUnit.KELVIN));
        enableDecayTimers = CachedBooleanValue.wrap(this, builder.comment("Show time to decay radiation when readings are above safe levels. Set to false on the client side to disable MekaSuit Geiger and Dosimeter Unit timers. Set to false on the server side to disable handheld Geiger Counter and Dosimeter timers.")
                .define("enableDecayTimers", true));
        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "common";
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