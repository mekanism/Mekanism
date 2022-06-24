package mekanism.common.lib.multiblock;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

//TODO: Eventually debate making this extend IMultiblockBase but for now this is mostly just a marker interface
public interface IInternalMultiblock {

    @Nullable
    UUID getMultiblockUUID();

    /**
     * Only valid on the server
     */
    @Nullable
    MultiblockData getMultiblock();

    void setMultiblock(@Nullable MultiblockData multiblock);

    default boolean hasFormedMultiblock() {
        return getMultiblockUUID() != null;
    }
}