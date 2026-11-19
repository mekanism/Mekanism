package mekanism.common.lib.transmitter;

import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public interface INetworkDataHandler {

    @Nullable
    default Component getNeededInfo(boolean hasShiftDown) {
        return null;
    }

    @Nullable
    default Component getStoredInfo(boolean hasShiftDown) {
        return null;
    }

    @Nullable
    default Component getFlowInfo(boolean hasShiftDown) {
        return null;
    }

    @Nullable
    default Number getNetworkReaderCapacity() {
        return null;
    }
}