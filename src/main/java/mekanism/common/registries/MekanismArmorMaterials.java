package mekanism.common.registries;

import java.util.Collections;
import java.util.Map;
import mekanism.common.config.MekanismConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;

/// NB: these objects CANNOT be used with [Item.Properties#humanoidArmor] as they will fail some checks due to vanilla assumptions
///
/// Use [#apply(Properties, ArmorMaterial, ArmorType)] instead
public class MekanismArmorMaterials {

    private MekanismArmorMaterials() {
    }

    private static final Map<ArmorType, Integer> NONE = Collections.emptyMap();

    //TODO - 26.2: List.of(new ArmorMaterial.Layer(rl))
    public static final ArmorMaterial HAZMAT = create(MekanismEquipmentAssets.HAZMAT, NONE, 0, 0, 0);

    //Same enchantment value as iron and turtle
    public static final ArmorMaterial SCUBA_MASK = create(MekanismEquipmentAssets.SCUBA_MASK, NONE, 9, 0, 0);
    public static final ArmorMaterial SCUBA_GEAR = create(MekanismEquipmentAssets.SCUBA_GEAR, NONE, 0, 0, 0);

    public static final ArmorMaterial FREE_RUNNERS = create(MekanismEquipmentAssets.FREE_RUNNERS, NONE, 0, 0, 0);
    public static final ArmorMaterial ARMORED_FREE_RUNNERS = create(
          MekanismEquipmentAssets.ARMORED_FREE_RUNNERS,
          Map.of(ArmorType.BOOTS, MekanismConfig.startup.armoredFreeRunnerArmor.get()),
          0,
          MekanismConfig.startup.armoredFreeRunnerToughness.get(),
          MekanismConfig.startup.armoredFreeRunnerKnockbackResistance.get()
    );

    public static final ArmorMaterial JETPACK = create(MekanismEquipmentAssets.JETPACK, NONE, 0, 0, 0);
    public static final ArmorMaterial ARMORED_JETPACK = create(
          MekanismEquipmentAssets.ARMORED_JETPACK,
          Map.of(ArmorType.CHESTPLATE, MekanismConfig.startup.armoredJetpackArmor.get()),
          0,
          MekanismConfig.startup.armoredJetpackToughness.get(),
          MekanismConfig.startup.armoredJetpackKnockbackResistance.get()
    );

    // This is unused for the most part; toughness / damage reduction is handled manually, though it can fall back to netherite values
    public static final ArmorMaterial MEKASUIT = create(
          MekanismEquipmentAssets.MEKASUIT,
          Map.of(
                ArmorType.BOOTS, MekanismConfig.startup.mekaSuitBootsArmor.get(),
                ArmorType.LEGGINGS, MekanismConfig.startup.mekaSuitPantsArmor.get(),
                ArmorType.CHESTPLATE, MekanismConfig.startup.mekaSuitBodyArmorArmor.get(),
                ArmorType.HELMET, MekanismConfig.startup.mekaSuitHelmetArmor.get()
          ),
          0,
          MekanismConfig.startup.mekaSuitToughness.get(),
          MekanismConfig.startup.mekaSuitKnockbackResistance.get()
    );

    public static Item.Properties apply(Item.Properties properties, ArmorMaterial armorMaterial, ArmorType armorType) {
        properties.attributes(armorMaterial.createAttributes(armorType))
              .component(
                    DataComponents.EQUIPPABLE,
                    Equippable.builder(armorType.getSlot())
                          .setEquipSound(armorMaterial.equipSound())
                          .setAsset(armorMaterial.assetId())
                          .build()
              );
        if (armorMaterial.enchantmentValue() > 0) {
            properties.enchantable(armorMaterial.enchantmentValue());
        }
        if (armorMaterial.durability() != 0) { //Used by Mek Tools
            properties.durability(armorType.getDurability(armorMaterial.durability()));
        }
        //noinspection ConstantValue - yes it is possible for our usages
        if (armorMaterial.repairIngredient() != null) {
            properties.repairable(armorMaterial.repairIngredient());
        }
        return properties;
    }

    private static ArmorMaterial create(
          ResourceKey<EquipmentAsset> key,
          Map<ArmorType, Integer> defense,
          int enchantmentValue,
          float toughness,
          float knockbackResistance
    ) {
        return new ArmorMaterial(/*unused*/0, defense, enchantmentValue, SoundEvents.ARMOR_EQUIP_GENERIC, toughness, knockbackResistance, null, key);
    }
}