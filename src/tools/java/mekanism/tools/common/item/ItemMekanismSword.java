package mekanism.tools.common.item;

import mekanism.tools.common.material.MaterialCreator;
import net.minecraft.world.item.Item;

public class ItemMekanismSword extends Item {

    public ItemMekanismSword(MaterialCreator material, Item.Properties properties) {
        super(properties.sword(material.toToolMaterial(), material.getSwordDamage(), material.getSwordAtkSpeed()));
    }
}