package mekanism.common.item;

import mekanism.common.tier.BaseTier;
import net.minecraft.item.Item;

public class ItemMekanismTiered extends ItemMekanism {

    private final BaseTier tier;

    public ItemMekanismTiered(BaseTier tier, String name) {
        this(tier, name, new Item.Properties());
    }

    public ItemMekanismTiered(BaseTier tier, String name, Item.Properties properties) {
        super(tier.getSimpleName() + "_" + name, properties);
        this.tier = tier;
    }

    public BaseTier getTier() {
        return tier;
    }
}