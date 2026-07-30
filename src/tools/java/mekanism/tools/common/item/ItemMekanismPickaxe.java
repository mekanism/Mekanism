package mekanism.tools.common.item;

import mekanism.tools.common.material.MaterialCreator;
import net.minecraft.world.item.Item;

public class ItemMekanismPickaxe extends Item implements IsMekanismTool {

    public ItemMekanismPickaxe(MaterialCreator material, Item.Properties properties) {
        super(properties.pickaxe(material.toToolMaterial(), material.getPickaxeDamage(), material.getPickaxeAtkSpeed()));
    }
}