package mekanism.common.block.interfaces;

import javax.annotation.Nonnull;
import mekanism.common.base.FactoryType;

public interface IHasFactoryType {

    @Nonnull
    FactoryType getFactoryType();
}