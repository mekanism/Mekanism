package mekanism.common.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ItemSalt extends ItemMekanism {

    public ItemSalt() {
        super("salt");
    }

    @Override
    public void registerOreDict() {
        OreDictionary.registerOre("itemSalt", new ItemStack(this));
        OreDictionary.registerOre("dustSalt", new ItemStack(this));
        OreDictionary.registerOre("foodSalt", new ItemStack(this));
    }
}