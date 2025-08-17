package mekanism.tools.common.registries;

import java.util.function.BiFunction;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemDeferredRegister.StrictProperties;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.config.MekanismToolsConfig;
import mekanism.tools.common.item.ItemMekanismArmor;
import mekanism.tools.common.item.ItemMekanismAxe;
import mekanism.tools.common.item.ItemMekanismHoe;
import mekanism.tools.common.item.ItemMekanismPaxel;
import mekanism.tools.common.item.ItemMekanismPickaxe;
import mekanism.tools.common.item.ItemMekanismShield;
import mekanism.tools.common.item.ItemMekanismShovel;
import mekanism.tools.common.item.ItemMekanismSword;
import mekanism.tools.common.item.ItemRefinedGlowstoneArmor;
import mekanism.tools.common.material.BaseMekanismMaterial;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.material.VanillaPaxelMaterialCreator;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;

public class ToolsItems {

    private ToolsItems() {
    }

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekanismTools.MODID);

    public static final ItemRegistryObject<ItemMekanismPaxel> WOOD_PAXEL = registerPaxel(MekanismToolsConfig.materials.wood);
    public static final ItemRegistryObject<ItemMekanismPaxel> STONE_PAXEL = registerPaxel(MekanismToolsConfig.materials.stone);
    public static final ItemRegistryObject<ItemMekanismPaxel> IRON_PAXEL = registerPaxel(MekanismToolsConfig.materials.iron);
    public static final ItemRegistryObject<ItemMekanismPaxel> DIAMOND_PAXEL = registerPaxel(MekanismToolsConfig.materials.diamond);
    public static final ItemRegistryObject<ItemMekanismPaxel> GOLD_PAXEL = registerPaxel(MekanismToolsConfig.materials.gold);
    public static final ItemRegistryObject<ItemMekanismPaxel> NETHERITE_PAXEL = registerPaxel(MekanismToolsConfig.materials.netherite);

    public static final ItemRegistryObject<ItemMekanismPickaxe> BRONZE_PICKAXE = registerPickaxe(MekanismToolsConfig.materials.bronze);
    public static final ItemRegistryObject<ItemMekanismAxe> BRONZE_AXE = registerAxe(MekanismToolsConfig.materials.bronze);
    public static final ItemRegistryObject<ItemMekanismShovel> BRONZE_SHOVEL = registerShovel(MekanismToolsConfig.materials.bronze);
    public static final ItemRegistryObject<ItemMekanismHoe> BRONZE_HOE = registerHoe(MekanismToolsConfig.materials.bronze);
    public static final ItemRegistryObject<ItemMekanismSword> BRONZE_SWORD = registerSword(MekanismToolsConfig.materials.bronze);
    public static final ItemRegistryObject<ItemMekanismPaxel> BRONZE_PAXEL = registerPaxel(MekanismToolsConfig.materials.bronze);
    public static final ItemRegistryObject<ItemMekanismArmor> BRONZE_HELMET = registerArmor(ToolsArmorMaterials.BRONZE, MekanismToolsConfig.materials.bronze, ArmorItem.Type.HELMET);
    public static final ItemRegistryObject<ItemMekanismArmor> BRONZE_CHESTPLATE = registerArmor(ToolsArmorMaterials.BRONZE, MekanismToolsConfig.materials.bronze, ArmorItem.Type.CHESTPLATE);
    public static final ItemRegistryObject<ItemMekanismArmor> BRONZE_LEGGINGS = registerArmor(ToolsArmorMaterials.BRONZE, MekanismToolsConfig.materials.bronze, ArmorItem.Type.LEGGINGS);
    public static final ItemRegistryObject<ItemMekanismArmor> BRONZE_BOOTS = registerArmor(ToolsArmorMaterials.BRONZE, MekanismToolsConfig.materials.bronze, ArmorItem.Type.BOOTS);
    public static final ItemRegistryObject<ItemMekanismShield> BRONZE_SHIELD = registerShield(MekanismToolsConfig.materials.bronze);

    public static final ItemRegistryObject<ItemMekanismPickaxe> LAPIS_LAZULI_PICKAXE = registerPickaxe(MekanismToolsConfig.materials.lapisLazuli);
    public static final ItemRegistryObject<ItemMekanismAxe> LAPIS_LAZULI_AXE = registerAxe(MekanismToolsConfig.materials.lapisLazuli);
    public static final ItemRegistryObject<ItemMekanismShovel> LAPIS_LAZULI_SHOVEL = registerShovel(MekanismToolsConfig.materials.lapisLazuli);
    public static final ItemRegistryObject<ItemMekanismHoe> LAPIS_LAZULI_HOE = registerHoe(MekanismToolsConfig.materials.lapisLazuli);
    public static final ItemRegistryObject<ItemMekanismSword> LAPIS_LAZULI_SWORD = registerSword(MekanismToolsConfig.materials.lapisLazuli);
    public static final ItemRegistryObject<ItemMekanismPaxel> LAPIS_LAZULI_PAXEL = registerPaxel(MekanismToolsConfig.materials.lapisLazuli);
    public static final ItemRegistryObject<ItemMekanismArmor> LAPIS_LAZULI_HELMET = registerArmor(ToolsArmorMaterials.LAPIS_LAZULI, MekanismToolsConfig.materials.lapisLazuli, ArmorItem.Type.HELMET);
    public static final ItemRegistryObject<ItemMekanismArmor> LAPIS_LAZULI_CHESTPLATE = registerArmor(ToolsArmorMaterials.LAPIS_LAZULI, MekanismToolsConfig.materials.lapisLazuli, ArmorItem.Type.CHESTPLATE);
    public static final ItemRegistryObject<ItemMekanismArmor> LAPIS_LAZULI_LEGGINGS = registerArmor(ToolsArmorMaterials.LAPIS_LAZULI, MekanismToolsConfig.materials.lapisLazuli, ArmorItem.Type.LEGGINGS);
    public static final ItemRegistryObject<ItemMekanismArmor> LAPIS_LAZULI_BOOTS = registerArmor(ToolsArmorMaterials.LAPIS_LAZULI, MekanismToolsConfig.materials.lapisLazuli, ArmorItem.Type.BOOTS);
    public static final ItemRegistryObject<ItemMekanismShield> LAPIS_LAZULI_SHIELD = registerShield(MekanismToolsConfig.materials.lapisLazuli);

    public static final ItemRegistryObject<ItemMekanismPickaxe> OSMIUM_PICKAXE = registerPickaxe(MekanismToolsConfig.materials.osmium);
    public static final ItemRegistryObject<ItemMekanismAxe> OSMIUM_AXE = registerAxe(MekanismToolsConfig.materials.osmium);
    public static final ItemRegistryObject<ItemMekanismShovel> OSMIUM_SHOVEL = registerShovel(MekanismToolsConfig.materials.osmium);
    public static final ItemRegistryObject<ItemMekanismHoe> OSMIUM_HOE = registerHoe(MekanismToolsConfig.materials.osmium);
    public static final ItemRegistryObject<ItemMekanismSword> OSMIUM_SWORD = registerSword(MekanismToolsConfig.materials.osmium);
    public static final ItemRegistryObject<ItemMekanismPaxel> OSMIUM_PAXEL = registerPaxel(MekanismToolsConfig.materials.osmium);
    public static final ItemRegistryObject<ItemMekanismArmor> OSMIUM_HELMET = registerArmor(ToolsArmorMaterials.OSMIUM, MekanismToolsConfig.materials.osmium, ArmorItem.Type.HELMET);
    public static final ItemRegistryObject<ItemMekanismArmor> OSMIUM_CHESTPLATE = registerArmor(ToolsArmorMaterials.OSMIUM, MekanismToolsConfig.materials.osmium, ArmorItem.Type.CHESTPLATE);
    public static final ItemRegistryObject<ItemMekanismArmor> OSMIUM_LEGGINGS = registerArmor(ToolsArmorMaterials.OSMIUM, MekanismToolsConfig.materials.osmium, ArmorItem.Type.LEGGINGS);
    public static final ItemRegistryObject<ItemMekanismArmor> OSMIUM_BOOTS = registerArmor(ToolsArmorMaterials.OSMIUM, MekanismToolsConfig.materials.osmium, ArmorItem.Type.BOOTS);
    public static final ItemRegistryObject<ItemMekanismShield> OSMIUM_SHIELD = registerShield(MekanismToolsConfig.materials.osmium);

    public static final ItemRegistryObject<ItemMekanismPickaxe> REFINED_GLOWSTONE_PICKAXE = registerPickaxe(MekanismToolsConfig.materials.refinedGlowstone);
    public static final ItemRegistryObject<ItemMekanismAxe> REFINED_GLOWSTONE_AXE = registerAxe(MekanismToolsConfig.materials.refinedGlowstone);
    public static final ItemRegistryObject<ItemMekanismShovel> REFINED_GLOWSTONE_SHOVEL = registerShovel(MekanismToolsConfig.materials.refinedGlowstone);
    public static final ItemRegistryObject<ItemMekanismHoe> REFINED_GLOWSTONE_HOE = registerHoe(MekanismToolsConfig.materials.refinedGlowstone);
    public static final ItemRegistryObject<ItemMekanismSword> REFINED_GLOWSTONE_SWORD = registerSword(MekanismToolsConfig.materials.refinedGlowstone);
    public static final ItemRegistryObject<ItemMekanismPaxel> REFINED_GLOWSTONE_PAXEL = registerPaxel(MekanismToolsConfig.materials.refinedGlowstone);
    public static final ItemRegistryObject<ItemMekanismArmor> REFINED_GLOWSTONE_HELMET = registerArmor(ToolsArmorMaterials.REFINED_GLOWSTONE, MekanismToolsConfig.materials.refinedGlowstone, ArmorItem.Type.HELMET, ItemRefinedGlowstoneArmor::new);
    public static final ItemRegistryObject<ItemMekanismArmor> REFINED_GLOWSTONE_CHESTPLATE = registerArmor(ToolsArmorMaterials.REFINED_GLOWSTONE, MekanismToolsConfig.materials.refinedGlowstone, ArmorItem.Type.CHESTPLATE, ItemRefinedGlowstoneArmor::new);
    public static final ItemRegistryObject<ItemMekanismArmor> REFINED_GLOWSTONE_LEGGINGS = registerArmor(ToolsArmorMaterials.REFINED_GLOWSTONE, MekanismToolsConfig.materials.refinedGlowstone, ArmorItem.Type.LEGGINGS, ItemRefinedGlowstoneArmor::new);
    public static final ItemRegistryObject<ItemMekanismArmor> REFINED_GLOWSTONE_BOOTS = registerArmor(ToolsArmorMaterials.REFINED_GLOWSTONE, MekanismToolsConfig.materials.refinedGlowstone, ArmorItem.Type.BOOTS, ItemRefinedGlowstoneArmor::new);
    public static final ItemRegistryObject<ItemMekanismShield> REFINED_GLOWSTONE_SHIELD = registerShield(MekanismToolsConfig.materials.refinedGlowstone);

    public static final ItemRegistryObject<ItemMekanismPickaxe> REFINED_OBSIDIAN_PICKAXE = registerPickaxe(MekanismToolsConfig.materials.refinedObsidian);
    public static final ItemRegistryObject<ItemMekanismAxe> REFINED_OBSIDIAN_AXE = registerAxe(MekanismToolsConfig.materials.refinedObsidian);
    public static final ItemRegistryObject<ItemMekanismShovel> REFINED_OBSIDIAN_SHOVEL = registerShovel(MekanismToolsConfig.materials.refinedObsidian);
    public static final ItemRegistryObject<ItemMekanismHoe> REFINED_OBSIDIAN_HOE = registerHoe(MekanismToolsConfig.materials.refinedObsidian);
    public static final ItemRegistryObject<ItemMekanismSword> REFINED_OBSIDIAN_SWORD = registerSword(MekanismToolsConfig.materials.refinedObsidian);
    public static final ItemRegistryObject<ItemMekanismPaxel> REFINED_OBSIDIAN_PAXEL = registerPaxel(MekanismToolsConfig.materials.refinedObsidian);
    public static final ItemRegistryObject<ItemMekanismArmor> REFINED_OBSIDIAN_HELMET = registerArmor(ToolsArmorMaterials.REFINED_OBSIDIAN, MekanismToolsConfig.materials.refinedObsidian, ArmorItem.Type.HELMET);
    public static final ItemRegistryObject<ItemMekanismArmor> REFINED_OBSIDIAN_CHESTPLATE = registerArmor(ToolsArmorMaterials.REFINED_OBSIDIAN, MekanismToolsConfig.materials.refinedObsidian, ArmorItem.Type.CHESTPLATE);
    public static final ItemRegistryObject<ItemMekanismArmor> REFINED_OBSIDIAN_LEGGINGS = registerArmor(ToolsArmorMaterials.REFINED_OBSIDIAN, MekanismToolsConfig.materials.refinedObsidian, ArmorItem.Type.LEGGINGS);
    public static final ItemRegistryObject<ItemMekanismArmor> REFINED_OBSIDIAN_BOOTS = registerArmor(ToolsArmorMaterials.REFINED_OBSIDIAN, MekanismToolsConfig.materials.refinedObsidian, ArmorItem.Type.BOOTS);
    public static final ItemRegistryObject<ItemMekanismShield> REFINED_OBSIDIAN_SHIELD = registerShield(MekanismToolsConfig.materials.refinedObsidian);

    public static final ItemRegistryObject<ItemMekanismPickaxe> STEEL_PICKAXE = registerPickaxe(MekanismToolsConfig.materials.steel);
    public static final ItemRegistryObject<ItemMekanismAxe> STEEL_AXE = registerAxe(MekanismToolsConfig.materials.steel);
    public static final ItemRegistryObject<ItemMekanismShovel> STEEL_SHOVEL = registerShovel(MekanismToolsConfig.materials.steel);
    public static final ItemRegistryObject<ItemMekanismHoe> STEEL_HOE = registerHoe(MekanismToolsConfig.materials.steel);
    public static final ItemRegistryObject<ItemMekanismSword> STEEL_SWORD = registerSword(MekanismToolsConfig.materials.steel);
    public static final ItemRegistryObject<ItemMekanismPaxel> STEEL_PAXEL = registerPaxel(MekanismToolsConfig.materials.steel);
    public static final ItemRegistryObject<ItemMekanismArmor> STEEL_HELMET = registerArmor(ToolsArmorMaterials.STEEL, MekanismToolsConfig.materials.steel, ArmorItem.Type.HELMET);
    public static final ItemRegistryObject<ItemMekanismArmor> STEEL_CHESTPLATE = registerArmor(ToolsArmorMaterials.STEEL, MekanismToolsConfig.materials.steel, ArmorItem.Type.CHESTPLATE);
    public static final ItemRegistryObject<ItemMekanismArmor> STEEL_LEGGINGS = registerArmor(ToolsArmorMaterials.STEEL, MekanismToolsConfig.materials.steel, ArmorItem.Type.LEGGINGS);
    public static final ItemRegistryObject<ItemMekanismArmor> STEEL_BOOTS = registerArmor(ToolsArmorMaterials.STEEL, MekanismToolsConfig.materials.steel, ArmorItem.Type.BOOTS);
    public static final ItemRegistryObject<ItemMekanismShield> STEEL_SHIELD = registerShield(MekanismToolsConfig.materials.steel);

    private static ItemRegistryObject<ItemMekanismShield> registerShield(MaterialCreator material) {
        return register(ItemMekanismShield::new, "_shield", material);
    }

    private static ItemRegistryObject<ItemMekanismPickaxe> registerPickaxe(MaterialCreator material) {
        return register(ItemMekanismPickaxe::new, "_pickaxe", material);
    }

    private static ItemRegistryObject<ItemMekanismAxe> registerAxe(MaterialCreator material) {
        return register(ItemMekanismAxe::new, "_axe", material);
    }

    private static ItemRegistryObject<ItemMekanismShovel> registerShovel(MaterialCreator material) {
        return register(ItemMekanismShovel::new, "_shovel", material);
    }

    private static ItemRegistryObject<ItemMekanismHoe> registerHoe(MaterialCreator material) {
        return register(ItemMekanismHoe::new, "_hoe", material);
    }

    private static ItemRegistryObject<ItemMekanismSword> registerSword(MaterialCreator material) {
        return register(ItemMekanismSword::new, "_sword", material);
    }

    private static ItemRegistryObject<ItemMekanismPaxel> registerPaxel(MaterialCreator material) {
        return register(ItemMekanismPaxel::new, "_paxel", material);
    }

    private static ItemRegistryObject<ItemMekanismPaxel> registerPaxel(VanillaPaxelMaterialCreator material) {
        return ITEMS.register(material.getRegistryPrefix() + "_paxel", () -> {
            Item.Properties properties = new StrictProperties();
            if (material.getVanillaTier() == Tiers.NETHERITE) {
                properties.fireResistant();
            }
            return new ItemMekanismPaxel(material, properties);
        });
    }

    private static ItemRegistryObject<ItemMekanismArmor> registerArmor(Holder<ArmorMaterial> armorMaterial, MaterialCreator material, ArmorItem.Type armorType) {
        return registerArmor(armorMaterial, material, armorType, ItemMekanismArmor::new);
    }

    private static ItemRegistryObject<ItemMekanismArmor> registerArmor(Holder<ArmorMaterial> armorMaterial, MaterialCreator material, ArmorItem.Type armorType, ArmorCreator armorCreator) {
        return ITEMS.register(material.getRegistryPrefix() + "_" + armorType.getName(), () -> armorCreator.create(armorMaterial, armorType, getBaseProperties(material)
              .durability(material.getDurabilityForType(armorType))));
    }

    private static <ITEM extends Item> ItemRegistryObject<ITEM> register(BiFunction<MaterialCreator, Item.Properties, ITEM> itemCreator, String suffix,
          MaterialCreator material) {
        return ITEMS.register(material.getRegistryPrefix() + suffix, () -> itemCreator.apply(material, getBaseProperties(material)));
    }

    private static Item.Properties getBaseProperties(BaseMekanismMaterial material) {
        Item.Properties properties = new StrictProperties();
        if (!material.burnsInFire()) {
            properties = properties.fireResistant();
        }
        return properties;
    }

    @FunctionalInterface
    private interface ArmorCreator {

        ItemMekanismArmor create(Holder<ArmorMaterial> material, ArmorItem.Type armorType, Item.Properties properties);
    }
}