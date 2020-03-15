package mekanism.common.base;

import javax.annotation.Nonnull;
import mekanism.common.content.blocktype.FactoryType;

public interface IHasFactoryType {

    @Nonnull
    FactoryType getFactoryType();
}