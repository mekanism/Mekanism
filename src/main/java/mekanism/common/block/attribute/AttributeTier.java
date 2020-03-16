package mekanism.common.block.attribute;

import mekanism.api.tier.ITier;

public class AttributeTier<TIER extends ITier> implements Attribute {

    private TIER tier;

    public AttributeTier(TIER tier) {
        this.tier = tier;
    }

    public TIER getTier() {
        return tier;
    }
}
