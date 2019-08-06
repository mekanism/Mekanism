package mekanism.common.item.gear;

import java.util.Locale;
import mekanism.common.Mekanism;
import mekanism.common.item.IItemMekanism;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;

public class ItemArmorMekanism extends ItemArmor implements IItemMekanism {

    public ItemArmorMekanism(ArmorMaterial material, EquipmentSlotType slot, String name) {
        this(material, 0, slot, name);
    }

    public ItemArmorMekanism(ArmorMaterial material, int renderIndex, EquipmentSlotType slot, String name) {
        super(material, renderIndex, slot);
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        name = name.toLowerCase(Locale.ROOT);
        setCreativeTab(Mekanism.tabMekanism);
        setTranslationKey(Mekanism.MODID + "." + name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, name));
    }
}