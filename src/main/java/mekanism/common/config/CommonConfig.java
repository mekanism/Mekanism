package mekanism.common.config;

import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedEnumValue;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class CommonConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final CachedEnumValue<EnergyUnit> energyUnit;
    public final CachedEnumValue<TemperatureUnit> tempUnit;
    public final CachedBooleanValue enableDecayTimers;
    public final CachedBooleanValue copyBlockData;
    public final CachedBooleanValue holidays;

    CommonConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Mekanism Common Config. This config is not synced between server and client.").push("common");
        energyUnit = CachedEnumValue.wrap(this, builder.comment("Displayed energy type in Mekanism GUIs and network reader readings.")
              .defineEnum("energyType", EnergyUnit.FORGE_ENERGY));
        tempUnit = CachedEnumValue.wrap(this, builder.comment("Displayed temperature unit in Mekanism GUIs and network reader readings.")
              .defineEnum("temperatureUnit", TemperatureUnit.KELVIN));
        enableDecayTimers = CachedBooleanValue.wrap(this, builder.comment("Show time to decay radiation when readings are above safe levels. Set to false on the client side to disable MekaSuit Geiger and Dosimeter Unit timers. Set to false on the server side to disable handheld Geiger Counter and Dosimeter timers.")
              .define("enableDecayTimers", true));
        copyBlockData = CachedBooleanValue.wrap(this, builder.comment("Determines whether machine configuration data is copied when using middle click. If this is set to false no data will be copied and the default instance of the stack will be returned.")
              .define("copyBlockData", true));
        holidays = CachedBooleanValue.wrap(this, builder.comment("Should holiday greetings and easter eggs play for holidays (ex: Christmas and New Years) on the client. And should robit skins be randomized on the server.")
              .define("holidays", true));
        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "common";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.COMMON;
    }
}