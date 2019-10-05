package mekanism.common.item;

import java.util.Locale;
import mekanism.common.Mekanism;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class ItemMekanism extends Item {

    public ItemMekanism(String name) {
        this(Mekanism.MODID, name);
    }

    public ItemMekanism(String name, Item.Properties properties) {
        this(Mekanism.MODID, name, properties);
    }

    public ItemMekanism(String modid, String name) {
        //TODO: Check defaults
        this(modid, name, new Item.Properties());
    }

    public ItemMekanism(String modid, String name, Item.Properties properties) {
        super(properties.group(Mekanism.tabMekanism));
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        setRegistryName(new ResourceLocation(modid, name.toLowerCase(Locale.ROOT)));
    }
}