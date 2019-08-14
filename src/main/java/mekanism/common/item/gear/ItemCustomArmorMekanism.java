package mekanism.common.item.gear;

import java.util.Locale;
import mekanism.common.Mekanism;
import mekanism.common.item.IItemMekanism;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class ItemCustomArmorMekanism extends ArmorItem implements IItemMekanism {

    public ItemCustomArmorMekanism(IArmorMaterial material, EquipmentSlotType slot, String name) {
        this(material, slot, name, new Item.Properties());
    }

    //Note: The items are set as non repairable
    public ItemCustomArmorMekanism(IArmorMaterial material, EquipmentSlotType slot, String name, Item.Properties properties) {
        super(material, slot, properties.group(Mekanism.tabMekanism).setNoRepair());
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        name = name.toLowerCase(Locale.ROOT);
        setRegistryName(new ResourceLocation(Mekanism.MODID, name));
    }
}