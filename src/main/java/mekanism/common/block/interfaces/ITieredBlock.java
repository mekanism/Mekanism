package mekanism.common.block.interfaces;

import mekanism.common.tier.ITier;

public interface ITieredBlock<TIER extends ITier> {

    TIER getTier();
}