package mekanism.chemistry.common.network;

import mekanism.chemistry.common.MekanismChemistry;
import mekanism.common.network.BasePacketHandler;
import net.minecraftforge.network.simple.SimpleChannel;

public class ChemistryPacketHandler extends BasePacketHandler {

    private final SimpleChannel netHandler = createChannel(MekanismChemistry.rl(MekanismChemistry.MODID), MekanismChemistry.instance.versionNumber);


    @Override
    protected SimpleChannel getChannel() {
        return netHandler;
    }

    @Override
    public void initialize() {
        //Client to server messages
        //registerClientToServer(PacketChemistryGuiButtonPress.class, PacketChemistryGuiButtonPress::decode);
        //registerClientToServer(PacketChemistryGuiInteract.class, PacketChemistryGuiInteract::decode);
        //Server to client messages
    }
}
