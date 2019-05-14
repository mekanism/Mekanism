package mekanism.common.tier;

import javax.annotation.Nullable;

public interface ITier<ENUM> {

    boolean hasNext();

    @Nullable
    ENUM next();

    BaseTier getBaseTier();
}