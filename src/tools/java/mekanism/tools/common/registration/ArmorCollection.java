package mekanism.tools.common.registration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import java.util.function.Consumer;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

public record ArmorCollection(ItemRegistryObject<Item> helmet, ItemRegistryObject<Item> chestplate, ItemRegistryObject<Item> leggings, ItemRegistryObject<Item> boots) {

    public static ArmorCollection create(ItemDeferredRegister registry, MaterialCreator material) {
        return new ArmorCollection(
              registerArmor(registry, material, ArmorType.HELMET),
              registerArmor(registry, material, ArmorType.CHESTPLATE),
              registerArmor(registry, material, ArmorType.LEGGINGS),
              registerArmor(registry, material, ArmorType.BOOTS)
        );
    }

    private static ItemRegistryObject<Item> registerArmor(ItemDeferredRegister registry, MaterialCreator material, ArmorType armorType) {
        return registry.registerSimple(material.registryPrefix() + "_" + armorType.getName(), properties -> ToolsItems.setCommonProperties(properties, material)
              .humanoidArmor(material.toArmorMaterial(armorType), armorType)
              //Durability must go after humanoidArmor to set it to the correct value, rather than one that scales it based on the armor type
              .durability(material.durability(armorType))
        );
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