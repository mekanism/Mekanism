package mekanism.common.registries;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismArmorMaterials {

    private MekanismArmorMaterials() {
    }

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Mekanism.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HAZMAT = ARMOR_MATERIALS.register("hazmat", rl -> new ArmorMaterial(
          Collections.emptyMap(), 0, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY,
          List.of(new ArmorMaterial.Layer(rl)), 0, 0
    ));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SCUBA_MASK = registerEnchantableSpecial("scuba_mask");
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SCUBA_GEAR = registerBaseSpecial("scuba_gear");
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> FREE_RUNNERS = registerBaseSpecial("free_runners");
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARMORED_FREE_RUNNERS = ARMOR_MATERIALS.register("armored_free_runners", () -> new ArmorMaterial(
          Map.of(ArmorItem.Type.BOOTS, MekanismConfig.startup.armoredFreeRunnerArmor.get()),
          0, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, Collections.emptyList(),
          MekanismConfig.startup.armoredFreeRunnerToughness.get(),
          MekanismConfig.startup.armoredFreeRunnerKnockbackResistance.get()
    ));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> JETPACK = registerBaseSpecial("jetpack");
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARMORED_JETPACK = ARMOR_MATERIALS.register("armored_jetpack", () -> new ArmorMaterial(
          Map.of(ArmorItem.Type.CHESTPLATE, MekanismConfig.startup.armoredJetpackArmor.get()),
          0, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, Collections.emptyList(),
          MekanismConfig.startup.armoredJetpackToughness.get(),
          MekanismConfig.startup.armoredJetpackKnockbackResistance.get()
    ));
    // This is unused for the most part; toughness / damage reduction is handled manually, though it can fall back to netherite values
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MEKASUIT = ARMOR_MATERIALS.register("mekasuit", () -> new ArmorMaterial(
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

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> registerBaseSpecial(String name) {
        return registerBaseSpecial(name, 0);
    }

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> registerEnchantableSpecial(String name) {
        //Same enchantment value as iron and turtle
        return registerBaseSpecial(name, 9);
    }

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> registerBaseSpecial(String name, int enchantmentValue) {
        return ARMOR_MATERIALS.register(name, () -> new ArmorMaterial(Collections.emptyMap(), enchantmentValue, SoundEvents.ARMOR_EQUIP_GENERIC,
              () -> Ingredient.EMPTY, Collections.emptyList(), 0, 0));
    }
}