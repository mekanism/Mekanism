package mekanism.common.block.interfaces;

import mekanism.api.tier.ITier;

public interface ITieredBlock<TIER extends ITier> {

    TIER getTier();
}