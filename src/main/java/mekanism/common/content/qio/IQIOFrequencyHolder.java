package mekanism.common.content.qio;

import java.util.List;
import mekanism.api.inventory.qio.IQIOComponent;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.tile.interfaces.ITileWrapper;
import org.jspecify.annotations.Nullable;

public interface IQIOFrequencyHolder extends IFrequencyHandler, ITileWrapper, IQIOComponent {

    @Nullable
    @Override
    default QIOFrequency getQIOFrequency() {
        return getFrequency(FrequencyTypes.QIO);
    }

    default List<QIOFrequency> getPublicFrequencies() {
        return getPublicCache(FrequencyTypes.QIO);
    }

    default List<QIOFrequency> getPrivateFrequencies() {
        return getPrivateCache(FrequencyTypes.QIO);
    }
}
