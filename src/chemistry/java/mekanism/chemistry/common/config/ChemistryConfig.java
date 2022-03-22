package mekanism.chemistry.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedDoubleValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class ChemistryConfig extends BaseMekanismConfig {

    private static final String DISTILLER_CATEGORY = "fractionating_distiller";

    //Fractionating Distiller
    public final CachedDoubleValue distillerHeatDissipation;
    public final CachedDoubleValue distillerHeatCapacity;

    private final ForgeConfigSpec configSpec;

    ChemistryConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Mekanism Chemistry Config. This config is synced between server and client.").push("chemistry");

        builder.comment("Fractionating Distiller Settings").push(DISTILLER_CATEGORY);
        distillerHeatDissipation = CachedDoubleValue.wrap(this, builder.comment("Fractionating Distiller heat loss per tick.")
              .define("heatDissipation", 0.02));
        distillerHeatCapacity = CachedDoubleValue.wrap(this, builder.comment("Heat capacity of Fractionating Distiller (increases amount of energy needed to increase temperature).")
              .define("heatCapacity", 100D));
        builder.pop();

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "chemistry";
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
