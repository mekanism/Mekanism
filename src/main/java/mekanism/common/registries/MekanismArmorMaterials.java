package mekanism.common.registries;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public class MekanismArmorMaterials {

    private MekanismArmorMaterials() {
    }

    public static final MekanismDeferredRegister<ArmorMaterial> ARMOR_MATERIALS = new MekanismDeferredRegister<>(Registries.ARMOR_MATERIAL, Mekanism.MODID);

    public static final MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> HAZMAT = ARMOR_MATERIALS.register("hazmat", () -> new ArmorMaterial(
          Collections.emptyMap(), 0, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY,
          List.of(new ArmorMaterial.Layer(Mekanism.rl("hazmat"))), 0, 0
    ));
    public static final MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> SCUBA_GEAR = registerBaseSpecial("scuba_gear");
    public static final MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> FREE_RUNNERS = registerBaseSpecial("free_runners");
    public static final MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> ARMORED_FREE_RUNNERS = ARMOR_MATERIALS.register("armored_free_runners", () -> new ArmorMaterial(
          Map.of(ArmorItem.Type.BOOTS, MekanismConfig.gear.armoredFreeRunnerArmor.getOrDefault()),
          0, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, Collections.emptyList(),
          MekanismConfig.gear.armoredFreeRunnerToughness.getOrDefault(),
          MekanismConfig.gear.armoredFreeRunnerKnockbackResistance.getOrDefault()
    ));
    public static final MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> JETPACK = registerBaseSpecial("jetpack");
    public static final MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> ARMORED_JETPACK = ARMOR_MATERIALS.register("armored_jetpack", () -> new ArmorMaterial(
          Map.of(ArmorItem.Type.CHESTPLATE, MekanismConfig.gear.armoredJetpackArmor.getOrDefault()),
          0, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, Collections.emptyList(),
          MekanismConfig.gear.armoredJetpackToughness.getOrDefault(),
          MekanismConfig.gear.armoredJetpackKnockbackResistance.getOrDefault()
    ));
    // This is unused for the most part; toughness / damage reduction is handled manually, though it can fall back to netherite values
    //TODO - 1.20.5: Figure out how to get the configs working again
    public static final MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> MEKASUIT = ARMOR_MATERIALS.register("mekasuit", () -> new ArmorMaterial(
          Map.of(
                ArmorItem.Type.BOOTS, MekanismConfig.gear.mekaSuitBootsArmor.getOrDefault(),
                ArmorItem.Type.LEGGINGS, MekanismConfig.gear.mekaSuitPantsArmor.getOrDefault(),
                ArmorItem.Type.CHESTPLATE, MekanismConfig.gear.mekaSuitBodyArmorArmor.getOrDefault(),
                ArmorItem.Type.HELMET, MekanismConfig.gear.mekaSuitHelmetArmor.getOrDefault()
          ),
          0, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, Collections.emptyList(),
          MekanismConfig.gear.mekaSuitToughness.getOrDefault(),
          MekanismConfig.gear.mekaSuitKnockbackResistance.getOrDefault()
    ));

    private static MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> registerBaseSpecial(String name) {
        return ARMOR_MATERIALS.register(name, () -> new ArmorMaterial(Collections.emptyMap(), 0, SoundEvents.ARMOR_EQUIP_GENERIC,
              () -> Ingredient.EMPTY, Collections.emptyList(), 0, 0));
    }
}