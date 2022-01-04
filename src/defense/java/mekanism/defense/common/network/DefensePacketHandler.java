package mekanism.defense.common.network;

import mekanism.common.network.BasePacketHandler;
import mekanism.defense.common.MekanismDefense;
import net.minecraftforge.network.simple.SimpleChannel;

public class DefensePacketHandler extends BasePacketHandler {

    private final SimpleChannel netHandler = createChannel(MekanismDefense.rl(MekanismDefense.MODID), MekanismDefense.instance.versionNumber);

    @Override
    protected SimpleChannel getChannel() {
        return netHandler;
    }

    @Override
    public void initialize() {
        //Client to server messages
        //Server to client messages
    }
}