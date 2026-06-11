package mekanism.common.tile.component.config;

import mekanism.api.RelativeSide;

public interface IPersistentConfigInfo {

    DataType getDataType(RelativeSide side);

    boolean isEjecting();
}