package mekanism.generators.common.network;

import mekanism.common.lib.Version;
import mekanism.common.network.BasePacketHandler;
import mekanism.generators.common.network.to_server.PacketGeneratorsTileButtonPress;
import mekanism.generators.common.network.to_server.PacketGeneratorsGuiInteract;
import net.neoforged.bus.api.IEventBus;

public class GeneratorsPacketHandler extends BasePacketHandler {

    public GeneratorsPacketHandler(IEventBus modEventBus, String modid, Version version) {
        super(modEventBus, modid, version);
    }

    @Override
    protected void registerClientToServer(PacketRegistrar registrar) {
        registrar.play(PacketGeneratorsTileButtonPress.ID, PacketGeneratorsTileButtonPress::new);
        registrar.play(PacketGeneratorsGuiInteract.ID, PacketGeneratorsGuiInteract::new);
    }

    @Override
    protected void registerServerToClient(PacketRegistrar registrar) {
    }
}