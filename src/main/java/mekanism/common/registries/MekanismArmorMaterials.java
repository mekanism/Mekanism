package mekanism.common.registries;

import java.util.Map;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedFloatValue;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class MekanismArmorMaterials {

    private MekanismArmorMaterials() {
    }

    private static final ArmorMaterial MEKASUIT = create(
          Map.of(
                ArmorType.BOOTS, MekanismConfig.startup.mekaSuitBootsArmor.get(),
                ArmorType.LEGGINGS, MekanismConfig.startup.mekaSuitPantsArmor.get(),
                ArmorType.CHESTPLATE, MekanismConfig.startup.mekaSuitBodyArmorArmor.get(),
                ArmorType.HELMET, MekanismConfig.startup.mekaSuitHelmetArmor.get()
          ),
          MekanismConfig.startup.mekaSuitToughness,
          MekanismConfig.startup.mekaSuitKnockbackResistance
    );

    @SuppressWarnings("DataFlowIssue")
    private static ArmorMaterial create(Map<ArmorType, Integer> defense, CachedFloatValue toughness, CachedFloatValue knockbackResistance) {
        return new ArmorMaterial(/*unused*/0, defense, /*unused*/0, /*unused*/SoundEvents.ARMOR_EQUIP_GENERIC, toughness.get(), knockbackResistance.get(), /*unused*/null, /*unused*/null);
    }

    public static ItemAttributeModifiers armoredFreeRunners() {
        return create(
              Map.of(ArmorType.BOOTS, MekanismConfig.startup.armoredFreeRunnerArmor.get()),
              MekanismConfig.startup.armoredFreeRunnerToughness,
              MekanismConfig.startup.armoredFreeRunnerKnockbackResistance
        ).createAttributes(ArmorType.BOOTS);
    }

    public static ItemAttributeModifiers armoredJetpack() {
        return create(
              Map.of(ArmorType.CHESTPLATE, MekanismConfig.startup.armoredJetpackArmor.get()),
              MekanismConfig.startup.armoredJetpackToughness,
              MekanismConfig.startup.armoredJetpackKnockbackResistance
        ).createAttributes(ArmorType.CHESTPLATE);
    }

    public static ItemAttributeModifiers mekaSuit(ArmorType type) {
        return MEKASUIT.createAttributes(type);
    }
}