package mekanism.api.block;

import javax.annotation.Nonnull;

public interface IHasFactoryType {

    @Nonnull
    FactoryType getFactoryType();
}