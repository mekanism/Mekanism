package mekanism.common.lib.transmitter;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface INetworkDataHandler {

    @Nullable
    default Component getNeededInfo() {
        return null;
    }

    @Nullable
    default Component getStoredInfo() {
        return null;
    }

    @Nullable
    default Component getFlowInfo() {
        return null;
    }

    @Nullable
    default Object getNetworkReaderCapacity() {
        return null;
    }
}