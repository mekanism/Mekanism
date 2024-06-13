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
          Map.of(ArmorItem.Type.BOOTS, MekanismConfig.startup.armoredFreeRunnerArmor.get()),
          0, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, Collections.emptyList(),
          MekanismConfig.startup.armoredFreeRunnerToughness.get(),
          MekanismConfig.startup.armoredFreeRunnerKnockbackResistance.get()
    ));
    public static final MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> JETPACK = registerBaseSpecial("jetpack");
    public static final MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> ARMORED_JETPACK = ARMOR_MATERIALS.register("armored_jetpack", () -> new ArmorMaterial(
          Map.of(ArmorItem.Type.CHESTPLATE, MekanismConfig.startup.armoredJetpackArmor.get()),
          0, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, Collections.emptyList(),
          MekanismConfig.startup.armoredJetpackToughness.get(),
          MekanismConfig.startup.armoredJetpackKnockbackResistance.get()
    ));
    // This is unused for the most part; toughness / damage reduction is handled manually, though it can fall back to netherite values
    public static final MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> MEKASUIT = ARMOR_MATERIALS.register("mekasuit", () -> new ArmorMaterial(
          Map.of(
                ArmorItem.Type.BOOTS, MekanismConfig.startup.mekaSuitBootsArmor.get(),
                ArmorItem.Type.LEGGINGS, MekanismConfig.startup.mekaSuitPantsArmor.get(),
                ArmorItem.Type.CHESTPLATE, MekanismConfig.startup.mekaSuitBodyArmorArmor.get(),
                ArmorItem.Type.HELMET, MekanismConfig.startup.mekaSuitHelmetArmor.get()
          ),
          0, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, Collections.emptyList(),
          MekanismConfig.startup.mekaSuitToughness.get(),
          MekanismConfig.startup.mekaSuitKnockbackResistance.get()
    ));

    private static MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> registerBaseSpecial(String name) {
        return ARMOR_MATERIALS.register(name, () -> new ArmorMaterial(Collections.emptyMap(), 0, SoundEvents.ARMOR_EQUIP_GENERIC,
              () -> Ingredient.EMPTY, Collections.emptyList(), 0, 0));
    }
}