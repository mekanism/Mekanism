package mekanism.tools.common.item;

import mekanism.tools.common.material.MaterialCreator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;

public class ItemMekanismShovel extends ShovelItem implements IsMekanismTool {

    public ItemMekanismShovel(MaterialCreator material, Item.Properties properties) {
        super(material.toToolMaterial(), material.getShovelDamage(), material.getShovelAtkSpeed(), properties);
    }
}