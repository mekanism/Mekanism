package mekanism.common.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ItemBioFuel extends ItemMekanism {

    public ItemBioFuel() {
        super("bio_fuel");
    }

    @Override
    public void registerOreDict() {
        OreDictionary.registerOre("itemBioFuel", new ItemStack(this));
    }
}