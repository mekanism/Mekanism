package mekanism.generators.common.network;

import mekanism.common.network.BasePacketHandler;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.network.to_server.PacketGeneratorsGuiButtonPress;
import mekanism.generators.common.network.to_server.PacketGeneratorsGuiInteract;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class GeneratorsPacketHandler extends BasePacketHandler {

    private static final SimpleChannel netHandler = createChannel(MekanismGenerators.rl(MekanismGenerators.MODID));

    @Override
    protected SimpleChannel getChannel() {
        return netHandler;
    }

    @Override
    public void initialize() {
        //Client to server messages
        registerClientToServer(PacketGeneratorsGuiButtonPress.class, PacketGeneratorsGuiButtonPress::decode);
        registerClientToServer(PacketGeneratorsGuiInteract.class, PacketGeneratorsGuiInteract::decode);
        //Server to client messages
    }
}