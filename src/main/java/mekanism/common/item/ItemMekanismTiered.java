package mekanism.common.item;

import mekanism.common.tier.BaseTier;

public class ItemMekanismTiered extends ItemMekanism {

    private final BaseTier tier;

    public ItemMekanismTiered(BaseTier tier, String name) {
        super(tier.getSimpleName() + "_" + name);
        this.tier = tier;
    }

    public BaseTier getTier() {
        return tier;
    }
}