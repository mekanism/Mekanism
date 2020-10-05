package mekanism.tools.common.registries;

import java.util.Locale;
import java.util.function.BiFunction;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.tools.client.render.item.ToolsISTERProvider;
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
import mekanism.tools.common.material.BaseMekanismMaterial;
import mekanism.tools.common.material.MaterialCreator;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTier;

public class ToolsItems {

    private ToolsItems() {
    }

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekanismTools.MODID);

    public static final ItemRegistryObject<ItemMekanismPaxel> WOOD_PAXEL = registerPaxel(ItemTier.WOOD);
    public static final ItemRegistryObject<ItemMekanismPaxel> STONE_PAXEL = registerPaxel(ItemTier.STONE);
    public static final ItemRegistryObject<ItemMekanismPaxel> IRON_PAXEL = registerPaxel(ItemTier.IRON);
    public static final ItemRegistryObject<ItemMekanismPaxel> DIAMOND_PAXEL = registerPaxel(ItemTier.DIAMOND);
    public static final ItemRegistryObject<ItemMekanismPaxel> GOLD_PAXEL = registerPaxel(ItemTier.GOLD);
    public static final ItemRegistryObject<ItemMekanismPaxel> NETHERITE_PAXEL = registerPaxel(ItemTier.NETHERITE);

    public static final ItemRegistryObject<ItemMekanismPickaxe> BRONZE_PICKAXE = registerPickaxe(MekanismToolsConfig.tools.bronze);
    public static final ItemRegistryObject<ItemMekanismAxe> BRONZE_AXE = registerAxe(MekanismToolsConfig.tools.bronze);
    public static final ItemRegistryObject<ItemMekanismShovel> BRONZE_SHOVEL = registerShovel(MekanismToolsConfig.tools.bronze);
    public static final ItemRegistryObject<ItemMekanismHoe> BRONZE_HOE = registerHoe(MekanismToolsConfig.tools.bronze);
    public static final ItemRegistryObject<ItemMekanismSword> BRONZE_SWORD = registerSword(MekanismToolsConfig.tools.bronze);
    public static final ItemRegistryObject<ItemMekanismPaxel> BRONZE_PAXEL = registerPaxel(MekanismToolsConfig.tools.bronze);
    public static final ItemRegistryObject<ItemMekanismArmor> BRONZE_HELMET = registerArmor(MekanismToolsConfig.tools.bronze, EquipmentSlotType.HEAD);
    public static final ItemRegistryObject<ItemMekanismArmor> BRONZE_CHESTPLATE = registerArmor(MekanismToolsConfig.tools.bronze, EquipmentSlotType.CHEST);
    public static final ItemRegistryObject<ItemMekanismArmor> BRONZE_LEGGINGS = registerArmor(MekanismToolsConfig.tools.bronze, EquipmentSlotType.LEGS);
    public static final ItemRegistryObject<ItemMekanismArmor> BRONZE_BOOTS = registerArmor(MekanismToolsConfig.tools.bronze, EquipmentSlotType.FEET);
    public static final ItemRegistryObject<ItemMekanismShield> BRONZE_SHIELD = registerShield(MekanismToolsConfig.tools.bronze);

    public static final ItemRegistryObject<ItemMekanismPickaxe> LAPIS_LAZULI_PICKAXE = registerPickaxe(MekanismToolsConfig.tools.lapisLazuli);
    public static final ItemRegistryObject<ItemMekanismAxe> LAPIS_LAZULI_AXE = registerAxe(MekanismToolsConfig.tools.lapisLazuli);
    public static final ItemRegistryObject<ItemMekanismShovel> LAPIS_LAZULI_SHOVEL = registerShovel(MekanismToolsConfig.tools.lapisLazuli);
    public static final ItemRegistryObject<ItemMekanismHoe> LAPIS_LAZULI_HOE = registerHoe(MekanismToolsConfig.tools.lapisLazuli);
    public static final ItemRegistryObject<ItemMekanismSword> LAPIS_LAZULI_SWORD = registerSword(MekanismToolsConfig.tools.lapisLazuli);
    public static final ItemRegistryObject<ItemMekanismPaxel> LAPIS_LAZULI_PAXEL = registerPaxel(MekanismToolsConfig.tools.lapisLazuli);
    public static final ItemRegistryObject<ItemMekanismArmor> LAPIS_LAZULI_HELMET = registerArmor(MekanismToolsConfig.tools.lapisLazuli, EquipmentSlotType.HEAD);
    public static final ItemRegistryObject<ItemMekanismArmor> LAPIS_LAZULI_CHESTPLATE = registerArmor(MekanismToolsConfig.tools.lapisLazuli, EquipmentSlotType.CHEST);
    public static final ItemRegistryObject<ItemMekanismArmor> LAPIS_LAZULI_LEGGINGS = registerArmor(MekanismToolsConfig.tools.lapisLazuli, EquipmentSlotType.LEGS);
    public static final ItemRegistryObject<ItemMekanismArmor> LAPIS_LAZULI_BOOTS = registerArmor(MekanismToolsConfig.tools.lapisLazuli, EquipmentSlotType.FEET);
    public static final ItemRegistryObject<ItemMekanismShield> LAPIS_LAZULI_SHIELD = registerShield(MekanismToolsConfig.tools.lapisLazuli);

    public static final ItemRegistryObject<ItemMekanismPickaxe> OSMIUM_PICKAXE = registerPickaxe(MekanismToolsConfig.tools.osmium);
    public static final ItemRegistryObject<ItemMekanismAxe> OSMIUM_AXE = registerAxe(MekanismToolsConfig.tools.osmium);
    public static final ItemRegistryObject<ItemMekanismShovel> OSMIUM_SHOVEL = registerShovel(MekanismToolsConfig.tools.osmium);
    public static final ItemRegistryObject<ItemMekanismHoe> OSMIUM_HOE = registerHoe(MekanismToolsConfig.tools.osmium);
    public static final ItemRegistryObject<ItemMekanismSword> OSMIUM_SWORD = registerSword(MekanismToolsConfig.tools.osmium);
    public static final ItemRegistryObject<ItemMekanismPaxel> OSMIUM_PAXEL = registerPaxel(MekanismToolsConfig.tools.osmium);
    public static final ItemRegistryObject<ItemMekanismArmor> OSMIUM_HELMET = registerArmor(MekanismToolsConfig.tools.osmium, EquipmentSlotType.HEAD);
    public static final ItemRegistryObject<ItemMekanismArmor> OSMIUM_CHESTPLATE = registerArmor(MekanismToolsConfig.tools.osmium, EquipmentSlotType.CHEST);
    public static final ItemRegistryObject<ItemMekanismArmor> OSMIUM_LEGGINGS = registerArmor(MekanismToolsConfig.tools.osmium, EquipmentSlotType.LEGS);
    public static final ItemRegistryObject<ItemMekanismArmor> OSMIUM_BOOTS = registerArmor(MekanismToolsConfig.tools.osmium, EquipmentSlotType.FEET);
    public static final ItemRegistryObject<ItemMekanismShield> OSMIUM_SHIELD = registerShield(MekanismToolsConfig.tools.osmium);

    public static final ItemRegistryObject<ItemMekanismPickaxe> REFINED_GLOWSTONE_PICKAXE = registerPickaxe(MekanismToolsConfig.tools.refinedGlowstone);
    public static final ItemRegistryObject<ItemMekanismAxe> REFINED_GLOWSTONE_AXE = registerAxe(MekanismToolsConfig.tools.refinedGlowstone);
    public static final ItemRegistryObject<ItemMekanismShovel> REFINED_GLOWSTONE_SHOVEL = registerShovel(MekanismToolsConfig.tools.refinedGlowstone);
    public static final ItemRegistryObject<ItemMekanismHoe> REFINED_GLOWSTONE_HOE = registerHoe(MekanismToolsConfig.tools.refinedGlowstone);
    public static final ItemRegistryObject<ItemMekanismSword> REFINED_GLOWSTONE_SWORD = registerSword(MekanismToolsConfig.tools.refinedGlowstone);
    public static final ItemRegistryObject<ItemMekanismPaxel> REFINED_GLOWSTONE_PAXEL = registerPaxel(MekanismToolsConfig.tools.refinedGlowstone);
    public static final ItemRegistryObject<ItemMekanismArmor> REFINED_GLOWSTONE_HELMET = registerArmor(MekanismToolsConfig.tools.refinedGlowstone, EquipmentSlotType.HEAD, true);
    public static final ItemRegistryObject<ItemMekanismArmor> REFINED_GLOWSTONE_CHESTPLATE = registerArmor(MekanismToolsConfig.tools.refinedGlowstone, EquipmentSlotType.CHEST, true);
    public static final ItemRegistryObject<ItemMekanismArmor> REFINED_GLOWSTONE_LEGGINGS = registerArmor(MekanismToolsConfig.tools.refinedGlowstone, EquipmentSlotType.LEGS, true);
    public static final ItemRegistryObject<ItemMekanismArmor> REFINED_GLOWSTONE_BOOTS = registerArmor(MekanismToolsConfig.tools.refinedGlowstone, EquipmentSlotType.FEET, true);
    public static final ItemRegistryObject<ItemMekanismShield> REFINED_GLOWSTONE_SHIELD = registerShield(MekanismToolsConfig.tools.refinedGlowstone);

    public static final ItemRegistryObject<ItemMekanismPickaxe> REFINED_OBSIDIAN_PICKAXE = registerPickaxe(MekanismToolsConfig.tools.refinedObsidian);
    public static final ItemRegistryObject<ItemMekanismAxe> REFINED_OBSIDIAN_AXE = registerAxe(MekanismToolsConfig.tools.refinedObsidian);
    public static final ItemRegistryObject<ItemMekanismShovel> REFINED_OBSIDIAN_SHOVEL = registerShovel(MekanismToolsConfig.tools.refinedObsidian);
    public static final ItemRegistryObject<ItemMekanismHoe> REFINED_OBSIDIAN_HOE = registerHoe(MekanismToolsConfig.tools.refinedObsidian);
    public static final ItemRegistryObject<ItemMekanismSword> REFINED_OBSIDIAN_SWORD = registerSword(MekanismToolsConfig.tools.refinedObsidian);
    public static final ItemRegistryObject<ItemMekanismPaxel> REFINED_OBSIDIAN_PAXEL = registerPaxel(MekanismToolsConfig.tools.refinedObsidian);
    public static final ItemRegistryObject<ItemMekanismArmor> REFINED_OBSIDIAN_HELMET = registerArmor(MekanismToolsConfig.tools.refinedObsidian, EquipmentSlotType.HEAD);
    public static final ItemRegistryObject<ItemMekanismArmor> REFINED_OBSIDIAN_CHESTPLATE = registerArmor(MekanismToolsConfig.tools.refinedObsidian, EquipmentSlotType.CHEST);
    public static final ItemRegistryObject<ItemMekanismArmor> REFINED_OBSIDIAN_LEGGINGS = registerArmor(MekanismToolsConfig.tools.refinedObsidian, EquipmentSlotType.LEGS);
    public static final ItemRegistryObject<ItemMekanismArmor> REFINED_OBSIDIAN_BOOTS = registerArmor(MekanismToolsConfig.tools.refinedObsidian, EquipmentSlotType.FEET);
    public static final ItemRegistryObject<ItemMekanismShield> REFINED_OBSIDIAN_SHIELD = registerShield(MekanismToolsConfig.tools.refinedObsidian);

    public static final ItemRegistryObject<ItemMekanismPickaxe> STEEL_PICKAXE = registerPickaxe(MekanismToolsConfig.tools.steel);
    public static final ItemRegistryObject<ItemMekanismAxe> STEEL_AXE = registerAxe(MekanismToolsConfig.tools.steel);
    public static final ItemRegistryObject<ItemMekanismShovel> STEEL_SHOVEL = registerShovel(MekanismToolsConfig.tools.steel);
    public static final ItemRegistryObject<ItemMekanismHoe> STEEL_HOE = registerHoe(MekanismToolsConfig.tools.steel);
    public static final ItemRegistryObject<ItemMekanismSword> STEEL_SWORD = registerSword(MekanismToolsConfig.tools.steel);
    public static final ItemRegistryObject<ItemMekanismPaxel> STEEL_PAXEL = registerPaxel(MekanismToolsConfig.tools.steel);
    public static final ItemRegistryObject<ItemMekanismArmor> STEEL_HELMET = registerArmor(MekanismToolsConfig.tools.steel, EquipmentSlotType.HEAD);
    public static final ItemRegistryObject<ItemMekanismArmor> STEEL_CHESTPLATE = registerArmor(MekanismToolsConfig.tools.steel, EquipmentSlotType.CHEST);
    public static final ItemRegistryObject<ItemMekanismArmor> STEEL_LEGGINGS = registerArmor(MekanismToolsConfig.tools.steel, EquipmentSlotType.LEGS);
    public static final ItemRegistryObject<ItemMekanismArmor> STEEL_BOOTS = registerArmor(MekanismToolsConfig.tools.steel, EquipmentSlotType.FEET);
    public static final ItemRegistryObject<ItemMekanismShield> STEEL_SHIELD = registerShield(MekanismToolsConfig.tools.steel);

    private static ItemRegistryObject<ItemMekanismShield> registerShield(MaterialCreator material) {
        return register((mat, properties) -> new ItemMekanismShield(mat, properties.setISTER(ToolsISTERProvider::shield)), "_shield", material);
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

    private static ItemRegistryObject<ItemMekanismPaxel> registerPaxel(ItemTier material) {
        return ITEMS.register(material.name().toLowerCase(Locale.ROOT) + "_paxel", () -> new ItemMekanismPaxel(material));
    }

    private static ItemRegistryObject<ItemMekanismArmor> registerArmor(MaterialCreator material, EquipmentSlotType slot) {
        return registerArmor(material, slot, false);
    }

    private static ItemRegistryObject<ItemMekanismArmor> registerArmor(MaterialCreator material, EquipmentSlotType slot, boolean makesPiglinsNeutral) {
        String suffix;
        if (slot == EquipmentSlotType.HEAD) {
            suffix = "_helmet";
        } else if (slot == EquipmentSlotType.CHEST) {
            suffix = "_chestplate";
        } else if (slot == EquipmentSlotType.LEGS) {
            suffix = "_leggings";
        } else {//EquipmentSlotType.FEET
            suffix = "_boots";
        }
        return ITEMS.register(material.getRegistryPrefix() + suffix, () -> new ItemMekanismArmor(material, slot, getBaseProperties(material), makesPiglinsNeutral));
    }

    private static <ITEM extends Item> ItemRegistryObject<ITEM> register(BiFunction<MaterialCreator, Item.Properties, ITEM> itemCreator, String suffix,
          MaterialCreator material) {
        return ITEMS.register(material.getRegistryPrefix() + suffix, () -> itemCreator.apply(material, getBaseProperties(material)));
    }

    private static Item.Properties getBaseProperties(BaseMekanismMaterial material) {
        Item.Properties properties = ItemDeferredRegister.getMekBaseProperties();
        if (!material.burnsInFire()) {
            properties = properties.isImmuneToFire();
        }
        return properties;
    }
}