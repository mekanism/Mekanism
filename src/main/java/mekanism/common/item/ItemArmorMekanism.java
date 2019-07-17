package mekanism.common.item;

import java.util.Locale;
import mekanism.common.Mekanism;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;

public class ItemArmorMekanism extends ItemArmor implements IItemMekanism {

    public ItemArmorMekanism(ArmorMaterial material, EntityEquipmentSlot slot, String name) {
        this(material, 0, slot, name);
    }

    public ItemArmorMekanism(ArmorMaterial material, int renderIndex, EntityEquipmentSlot slot, String name) {
        super(material, renderIndex, slot);
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        name = name.toLowerCase(Locale.ROOT);
        setCreativeTab(Mekanism.tabMekanism);
        setTranslationKey(Mekanism.MODID + "." + name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, name));
    }
}