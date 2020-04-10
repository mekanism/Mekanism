package mekanism.generators.common.network;

import mekanism.common.network.BasePacketHandler;
import mekanism.generators.common.MekanismGenerators;
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
        registerClientToServer(PacketGeneratorsGuiButtonPress.class, PacketGeneratorsGuiButtonPress::encode, PacketGeneratorsGuiButtonPress::decode, PacketGeneratorsGuiButtonPress::handle);
        registerClientToServer(PacketGeneratorsGuiInteract.class, PacketGeneratorsGuiInteract::encode, PacketGeneratorsGuiInteract::decode, PacketGeneratorsGuiInteract::handle);
        //Server to client messages
    }
}