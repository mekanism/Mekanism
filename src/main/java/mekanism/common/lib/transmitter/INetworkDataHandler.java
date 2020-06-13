package mekanism.common.lib.transmitter;

import javax.annotation.Nullable;
import net.minecraft.util.text.ITextComponent;

public interface INetworkDataHandler {

    @Nullable
    default ITextComponent getNeededInfo() {
        return null;
    }

    @Nullable
    default ITextComponent getStoredInfo() {
        return null;
    }

    @Nullable
    default ITextComponent getFlowInfo() {
        return null;
    }

    @Nullable
    default Object getNetworkReaderCapacity() {
        return null;
    }
}