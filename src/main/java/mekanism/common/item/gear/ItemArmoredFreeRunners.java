package mekanism.common.item.gear;

import mekanism.common.registries.MekanismArmorMaterials;
import net.minecraft.world.item.Item;

public class ItemArmoredFreeRunners extends ItemFreeRunners {

    public ItemArmoredFreeRunners(Item.Properties properties) {
        super(MekanismArmorMaterials.ARMORED_FREE_RUNNERS, properties);
    }
}
