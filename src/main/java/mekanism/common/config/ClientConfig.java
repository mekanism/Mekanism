package mekanism.common.config;

import io.netty.buffer.ByteBuf;

/**
 * Created by Thiakil on 15/03/2019.
 */
public class ClientConfig extends BaseConfig {

    public final BooleanOption enablePlayerSounds = new BooleanOption(this, "client", "EnablePlayerSounds", true,
          "Play sounds for Jetpack/Gas Mask/Flamethrower (all players)");

    public final BooleanOption enableMachineSounds = new BooleanOption(this, "client", "EnableMachineSounds", true,
          "Machine make sound?");

    public final BooleanOption holidays = new BooleanOption(this, "client", "Holidays", true,
          "Christmas/New Years greetings in chat");

    public final DoubleOption baseSoundVolume = new DoubleOption(this, "client", "SoundVolume", 1D,
          "Adjust Mekanism sounds' base volume. < 1 is softer, higher is louder");

    public final BooleanOption machineEffects = new BooleanOption(this, "client", "MachineEffects", true,
          "Show particles when machines active");

    public final BooleanOption replaceSoundsWhenResuming = new BooleanOption(this, "client",
          "ReplaceSoundsWhenResuming", true,
          "If true, will reduce lagging between player sounds. Setting to false will reduce GC load");

    public final BooleanOption enableAmbientLighting = new BooleanOption(this, "client", "EnableAmbientLighting", false,
          "Should active machines produce block light. Causes chunk redraws!");

    public final IntOption ambientLightingLevel = new IntOption(this, "client", "AmbientLightingLevel", 15,
          "How much light to produce if ambient lighting is enabled", 1, 15);

    public final BooleanOption opaqueTransmitters = new BooleanOption(this, "client", "OpaqueTransmitterRender", false,
          "If true, don't render Cables/Pipes/Tubes as transparent and don't render their contents");

    public final BooleanOption allowConfiguratorModeScroll = new BooleanOption(this, "client", "ConfiguratorModeScroll",
          true, "Allow sneak+scroll to change Configurator modes");

    public final BooleanOption doMultiblockSparkle = new BooleanOption(this, "client", "MultiblockSparkle", true,
          "Spawn redstone particles on successful multiblock forming");

    public final IntOption multiblockSparkleIntensity = new IntOption(this, "client", "MultiblockSparkleIntensity", 1,
          "How many particles rounds to spawn for EACH block in the multiblock frame. Each particle will have a slightly randomised offset. A round is 6 particles. Old default is 6");

    //todo remove??
    public boolean oldTransmitterRender = false;

    @Override
    public void write(ByteBuf config) {
        throw new UnsupportedOperationException("Client config shouldn't be synced");
    }

    @Override
    public void read(ByteBuf config) {
        throw new UnsupportedOperationException("Client config shouldn't be synced");
    }
}
