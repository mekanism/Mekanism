package mekanism.common.tile.component.config;

import mekanism.api.RelativeSide;
import org.jetbrains.annotations.NotNull;

public interface IPersistentConfigInfo {

    @NotNull
    DataType getDataType(@NotNull RelativeSide side);

    boolean setDataType(@NotNull DataType dataType, @NotNull RelativeSide side);

    default void setDataType(@NotNull DataType dataType, @NotNull RelativeSide... sides) {
        for (RelativeSide side : sides) {
            setDataType(dataType, side);
        }
    }

    boolean isEjecting();

    void setEjecting(boolean ejecting);
}