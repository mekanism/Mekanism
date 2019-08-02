package mekanism.common.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ItemSawdust extends ItemMekanism {

    public ItemSawdust() {
        super("sawdust");
    }

    @Override
    public void registerOreDict() {
        OreDictionary.registerOre("pulpWood", new ItemStack(this));
        OreDictionary.registerOre("dustWood", new ItemStack(this));
    }
}