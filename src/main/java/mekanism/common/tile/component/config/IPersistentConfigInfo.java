package mekanism.common.tile.component.config;

import mekanism.api.RelativeSide;
import org.jetbrains.annotations.NotNull;

public interface IPersistentConfigInfo {

    @NotNull
    DataType getDataType(@NotNull RelativeSide side);

    boolean isEjecting();
}