package mekanism.tools.common.item;

import mekanism.tools.common.material.MaterialCreator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;

public class ItemMekanismArmor extends Item {

    private final ArmorType armorType;

    private final ResourceKey<EquipmentAsset> equipmentAssetId;

    public ItemMekanismArmor(MaterialCreator material, ArmorType armorType, Item.Properties properties) {
        super(applyArmorProps(properties, material, armorType));
        this.armorType = armorType;
        this.equipmentAssetId = material.equipmentAsset();
    }

    /// Copied and adapted from [Item.Properties#humanoidArmor]
    private static Properties applyArmorProps(Properties properties, MaterialCreator material, ArmorType armorType) {
        return properties.durability(material.getDurabilityForType(armorType))
              .attributes(material.createAttributes(armorType))
              .enchantable(material.getEnchantmentValue())
              .component(
                    DataComponents.EQUIPPABLE, Equippable.builder(armorType.getSlot())
                          .setEquipSound(material.equipSound())
                          .setAsset(material.equipmentAsset())
                          .build()
              )
              .repairable(material.getRepairItems());
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    public ResourceKey<EquipmentAsset> getEquipmentAssetId() {
        return equipmentAssetId;
    }
}