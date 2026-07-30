package mekanism.tools.common.item;

import mekanism.tools.common.material.MaterialCreator;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;

//TODO - 26.2: Remove specific subclasses if possible
public class ItemMekanismAxe extends AxeItem implements IsMekanismTool {

    public ItemMekanismAxe(MaterialCreator material, Item.Properties properties) {
        //super(material, properties.attributes(createAttributes(material, material.getAxeDamage(), material.getAxeAtkSpeed())));
        super(material.toToolMaterial(), material.getAxeDamage(), material.getAxeAtkSpeed(), properties);
    }
}