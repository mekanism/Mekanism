package mekanism.tools.common.registration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import java.util.function.Consumer;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;

public record ArmorCollection(ItemRegistryObject<Item> helmet, ItemRegistryObject<Item> chestplate,
                              ItemRegistryObject<Item> leggings, ItemRegistryObject<Item> boots) {

    public static ArmorCollection create(ItemDeferredRegister registry, MaterialCreator material) {
        return new ArmorCollection(
              registerArmor(registry, material, ArmorType.HELMET),
              registerArmor(registry, material, ArmorType.CHESTPLATE),
              registerArmor(registry, material, ArmorType.LEGGINGS),
              registerArmor(registry, material, ArmorType.BOOTS)
        );
    }

    private static ItemRegistryObject<Item> registerArmor(ItemDeferredRegister registry, MaterialCreator material, ArmorType armorType) {
        return registry.registerSimple(material.getRegistryPrefix() + "_" + armorType.getName(), properties -> applyArmorProps(properties, material, armorType));
    }

    /// Copied and adapted from [Item.Properties#humanoidArmor]
    private static Properties applyArmorProps(Properties properties, MaterialCreator material, ArmorType armorType) {
        //TODO - 26.2: Can we call humanoidArmor and just create a per armor type ArmorMaterial that we pass?
        return ToolsItems.setCommonProperties(properties, material)
              .durability(material.getDurabilityForType(armorType))
              .attributes(material.createAttributes(armorType))
              .enchantable(material.getArmorEnchantmentValue())
              .component(
                    DataComponents.EQUIPPABLE, Equippable.builder(armorType.getSlot())
                          .setEquipSound(material.equipSound())
                          .setAsset(material.equipmentAsset())
                          .build()
              )
              .repairable(material.getRepairItems());
    }

    public List<ItemRegistryObject<Item>> asList() {
        Builder<ItemRegistryObject<Item>> builder = ImmutableList.builderWithExpectedSize(4);
        forEach(builder::add);
        return builder.build();
    }

    public void forEach(Consumer<ItemRegistryObject<Item>> consumer) {
        consumer.accept(this.helmet);
        consumer.accept(this.chestplate);
        consumer.accept(this.leggings);
        consumer.accept(this.boots);
    }
}