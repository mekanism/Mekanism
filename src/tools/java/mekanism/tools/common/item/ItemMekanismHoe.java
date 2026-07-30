package mekanism.tools.common.item;

import mekanism.tools.common.material.MaterialCreator;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;

public class ItemMekanismHoe extends HoeItem implements IsMekanismTool {

    public ItemMekanismHoe(MaterialCreator material, Item.Properties properties) {
        super(material.toToolMaterial(), material.getHoeDamage(), material.getHoeAtkSpeed(), properties);
    }
}