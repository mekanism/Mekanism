package mekanism.common.lib.transmitter;

import net.minecraft.util.text.ITextComponent;

public interface INetworkDataHandler {

    ITextComponent getNeededInfo();

    ITextComponent getStoredInfo();

    ITextComponent getFlowInfo();
}