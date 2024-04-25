package mekanism.generators.common.network;

import mekanism.common.lib.Version;
import mekanism.common.network.BasePacketHandler;
import mekanism.generators.common.network.to_server.PacketGeneratorsGuiInteract;
import mekanism.generators.common.network.to_server.PacketGeneratorsTileButtonPress;
import net.neoforged.bus.api.IEventBus;

public class GeneratorsPacketHandler extends BasePacketHandler {

    public GeneratorsPacketHandler(IEventBus modEventBus, Version version) {
        super(modEventBus, version);
    }

    @Override
    protected void registerClientToServer(PacketRegistrar registrar) {
        registrar.play(PacketGeneratorsTileButtonPress.TYPE, PacketGeneratorsTileButtonPress.STREAM_CODEC);
        registrar.play(PacketGeneratorsGuiInteract.TYPE, PacketGeneratorsGuiInteract.STREAM_CODEC);
    }

    @Override
    protected void registerServerToClient(PacketRegistrar registrar) {
    }
}