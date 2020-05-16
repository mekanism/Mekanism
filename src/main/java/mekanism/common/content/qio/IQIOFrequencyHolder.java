package mekanism.common.content.qio;

import java.util.List;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.lib.security.ISecurityTile;
import net.minecraft.util.math.BlockPos;

public interface IQIOFrequencyHolder extends ISecurityTile, IFrequencyHandler {

    default QIOFrequency getQIOFrequency() {
        return getFrequency(FrequencyType.QIO);
    }

    default List<QIOFrequency> getPublicFrequencies() {
        return getPublicCache(FrequencyType.QIO);
    }

    default List<QIOFrequency> getPrivateFrequencies() {
        return getPrivateCache(FrequencyType.QIO);
    }

    BlockPos getPos();
}
