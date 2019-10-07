package mekanism.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.config.ModConfig.Type;

public class ClientConfig implements IMekanismConfig {

    private final ForgeConfigSpec configSpec;

    public final BooleanValue enablePlayerSounds;
    public final BooleanValue enableMachineSounds;
    public final BooleanValue holidays;
    public final FloatValue baseSoundVolume;
    public final BooleanValue machineEffects;
    public final BooleanValue enableAmbientLighting;
    public final IntValue ambientLightingLevel;
    public final BooleanValue opaqueTransmitters;
    public final BooleanValue allowConfiguratorModeScroll;
    public final BooleanValue enableMultiblockFormationParticles;
    public final BooleanValue alignHUDLeft;

    ClientConfig() {
        //TODO: Should this stuff be moved from constructor to an init method defined in IMekanismConfig
        // Only really matters if they can't stay final
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Client Config");

        enablePlayerSounds = builder.comment("Play sounds for Jetpack/Gas Mask/Flamethrower (all players).").define("enablePlayerSounds", true);
        enableMachineSounds = builder.comment("If enabled machines play their sounds while running.").define("enableMachineSounds", true);
        holidays = builder.comment("Christmas/New Years greetings in chat.").define("holidays", true);
        baseSoundVolume = FloatValue.of(builder.comment("Adjust Mekanism sounds' base volume. < 1 is softer, higher is louder.")
              .defineInRange("baseSoundVolume", 1, 0, Float.MAX_VALUE));
        machineEffects = builder.comment("Show particles when machines active.").define("machineEffects", true);
        enableAmbientLighting = builder.comment("Should active machines produce block light.").define("enableAmbientLighting", true);
        ambientLightingLevel = builder.comment("How much light to produce if ambient lighting is enabled.").defineInRange("ambientLightingLevel", 15, 1, 15);
        opaqueTransmitters = builder.comment("If true, don't render Cables/Pipes/Tubes as transparent and don't render their contents.").define("opaqueTransmitters", false);
        allowConfiguratorModeScroll = builder.comment("Allow sneak+scroll to change Configurator modes.").define("allowConfiguratorModeScroll", true);
        enableMultiblockFormationParticles = builder.comment("Set to false to prevent particle spam when loading multiblocks (notification message will display instead).")
              .define("enableMultiblockFormationParticles", true);
        alignHUDLeft = builder.comment("Align HUD with left (if true) or right (if false)").define("alignHUDLeft", true);

        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "client.toml";
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