package mekanism.tools.common;

import javax.annotation.Nonnull;
import mekanism.api.IItemProvider;
import mekanism.common.item.IItemMekanism;
import mekanism.tools.common.config.MekanismToolsConfig;
import mekanism.tools.common.item.ItemMekanismArmor;
import mekanism.tools.common.item.ItemMekanismAxe;
import mekanism.tools.common.item.ItemMekanismHoe;
import mekanism.tools.common.item.ItemMekanismPaxel;
import mekanism.tools.common.item.ItemMekanismPickaxe;
import mekanism.tools.common.item.ItemMekanismShovel;
import mekanism.tools.common.item.ItemMekanismSword;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTier;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public enum ToolsItem implements IItemProvider {
    WOOD_PAXEL(new ItemMekanismPaxel(ItemTier.WOOD)),
    STONE_PAXEL(new ItemMekanismPaxel(ItemTier.STONE)),
    IRON_PAXEL(new ItemMekanismPaxel(ItemTier.IRON)),
    DIAMOND_PAXEL(new ItemMekanismPaxel(ItemTier.DIAMOND)),
    GOLD_PAXEL(new ItemMekanismPaxel(ItemTier.GOLD)),

    BRONZE_PICKAXE(new ItemMekanismPickaxe(MekanismToolsConfig.tools.bronze)),
    BRONZE_AXE(new ItemMekanismAxe(MekanismToolsConfig.tools.bronze)),
    BRONZE_SHOVEL(new ItemMekanismShovel(MekanismToolsConfig.tools.bronze)),
    BRONZE_HOE(new ItemMekanismHoe(MekanismToolsConfig.tools.bronze)),
    BRONZE_SWORD(new ItemMekanismSword(MekanismToolsConfig.tools.bronze)),
    BRONZE_PAXEL(new ItemMekanismPaxel(MekanismToolsConfig.tools.bronze)),
    BRONZE_HELMET(new ItemMekanismArmor(MekanismToolsConfig.tools.bronze, EquipmentSlotType.HEAD)),
    BRONZE_CHESTPLATE(new ItemMekanismArmor(MekanismToolsConfig.tools.bronze, EquipmentSlotType.CHEST)),
    BRONZE_LEGGINGS(new ItemMekanismArmor(MekanismToolsConfig.tools.bronze, EquipmentSlotType.LEGS)),
    BRONZE_BOOTS(new ItemMekanismArmor(MekanismToolsConfig.tools.bronze, EquipmentSlotType.FEET)),

    LAPIS_LAZULI_PICKAXE(new ItemMekanismPickaxe(MekanismToolsConfig.tools.lapisLazuli)),
    LAPIS_LAZULI_AXE(new ItemMekanismAxe(MekanismToolsConfig.tools.lapisLazuli)),
    LAPIS_LAZULI_SHOVEL(new ItemMekanismShovel(MekanismToolsConfig.tools.lapisLazuli)),
    LAPIS_LAZULI_HOE(new ItemMekanismHoe(MekanismToolsConfig.tools.lapisLazuli)),
    LAPIS_LAZULI_SWORD(new ItemMekanismSword(MekanismToolsConfig.tools.lapisLazuli)),
    LAPIS_LAZULI_PAXEL(new ItemMekanismPaxel(MekanismToolsConfig.tools.lapisLazuli)),
    LAPIS_LAZULI_HELMET(new ItemMekanismArmor(MekanismToolsConfig.tools.lapisLazuli, EquipmentSlotType.HEAD)),
    LAPIS_LAZULI_CHESTPLATE(new ItemMekanismArmor(MekanismToolsConfig.tools.lapisLazuli, EquipmentSlotType.CHEST)),
    LAPIS_LAZULI_LEGGINGS(new ItemMekanismArmor(MekanismToolsConfig.tools.lapisLazuli, EquipmentSlotType.LEGS)),
    LAPIS_LAZULI_BOOTS(new ItemMekanismArmor(MekanismToolsConfig.tools.lapisLazuli, EquipmentSlotType.FEET)),

    OSMIUM_PICKAXE(new ItemMekanismPickaxe(MekanismToolsConfig.tools.osmium)),
    OSMIUM_AXE(new ItemMekanismAxe(MekanismToolsConfig.tools.osmium)),
    OSMIUM_SHOVEL(new ItemMekanismShovel(MekanismToolsConfig.tools.osmium)),
    OSMIUM_HOE(new ItemMekanismHoe(MekanismToolsConfig.tools.osmium)),
    OSMIUM_SWORD(new ItemMekanismSword(MekanismToolsConfig.tools.osmium)),
    OSMIUM_PAXEL(new ItemMekanismPaxel(MekanismToolsConfig.tools.osmium)),
    OSMIUM_HELMET(new ItemMekanismArmor(MekanismToolsConfig.tools.osmium, EquipmentSlotType.HEAD)),
    OSMIUM_CHESTPLATE(new ItemMekanismArmor(MekanismToolsConfig.tools.osmium, EquipmentSlotType.CHEST)),
    OSMIUM_LEGGINGS(new ItemMekanismArmor(MekanismToolsConfig.tools.osmium, EquipmentSlotType.LEGS)),
    OSMIUM_BOOTS(new ItemMekanismArmor(MekanismToolsConfig.tools.osmium, EquipmentSlotType.FEET)),

    REFINED_GLOWSTONE_PICKAXE(new ItemMekanismPickaxe(MekanismToolsConfig.tools.refinedGlowstone)),
    REFINED_GLOWSTONE_AXE(new ItemMekanismAxe(MekanismToolsConfig.tools.refinedGlowstone)),
    REFINED_GLOWSTONE_SHOVEL(new ItemMekanismShovel(MekanismToolsConfig.tools.refinedGlowstone)),
    REFINED_GLOWSTONE_HOE(new ItemMekanismHoe(MekanismToolsConfig.tools.refinedGlowstone)),
    REFINED_GLOWSTONE_SWORD(new ItemMekanismSword(MekanismToolsConfig.tools.refinedGlowstone)),
    REFINED_GLOWSTONE_PAXEL(new ItemMekanismPaxel(MekanismToolsConfig.tools.refinedGlowstone)),
    REFINED_GLOWSTONE_HELMET(new ItemMekanismArmor(MekanismToolsConfig.tools.refinedGlowstone, EquipmentSlotType.HEAD)),
    REFINED_GLOWSTONE_CHESTPLATE(new ItemMekanismArmor(MekanismToolsConfig.tools.refinedGlowstone, EquipmentSlotType.CHEST)),
    REFINED_GLOWSTONE_LEGGINGS(new ItemMekanismArmor(MekanismToolsConfig.tools.refinedGlowstone, EquipmentSlotType.LEGS)),
    REFINED_GLOWSTONE_BOOTS(new ItemMekanismArmor(MekanismToolsConfig.tools.refinedGlowstone, EquipmentSlotType.FEET)),

    REFINED_OBSIDIAN_PICKAXE(new ItemMekanismPickaxe(MekanismToolsConfig.tools.refinedObsidian)),
    REFINED_OBSIDIAN_AXE(new ItemMekanismAxe(MekanismToolsConfig.tools.refinedObsidian)),
    REFINED_OBSIDIAN_SHOVEL(new ItemMekanismShovel(MekanismToolsConfig.tools.refinedObsidian)),
    REFINED_OBSIDIAN_HOE(new ItemMekanismHoe(MekanismToolsConfig.tools.refinedObsidian)),
    REFINED_OBSIDIAN_SWORD(new ItemMekanismSword(MekanismToolsConfig.tools.refinedObsidian)),
    REFINED_OBSIDIAN_PAXEL(new ItemMekanismPaxel(MekanismToolsConfig.tools.refinedObsidian)),
    REFINED_OBSIDIAN_HELMET(new ItemMekanismArmor(MekanismToolsConfig.tools.refinedObsidian, EquipmentSlotType.HEAD)),
    REFINED_OBSIDIAN_CHESTPLATE(new ItemMekanismArmor(MekanismToolsConfig.tools.refinedObsidian, EquipmentSlotType.CHEST)),
    REFINED_OBSIDIAN_LEGGINGS(new ItemMekanismArmor(MekanismToolsConfig.tools.refinedObsidian, EquipmentSlotType.LEGS)),
    REFINED_OBSIDIAN_BOOTS(new ItemMekanismArmor(MekanismToolsConfig.tools.refinedObsidian, EquipmentSlotType.FEET)),

    STEEL_PICKAXE(new ItemMekanismPickaxe(MekanismToolsConfig.tools.steel)),
    STEEL_AXE(new ItemMekanismAxe(MekanismToolsConfig.tools.steel)),
    STEEL_SHOVEL(new ItemMekanismShovel(MekanismToolsConfig.tools.steel)),
    STEEL_HOE(new ItemMekanismHoe(MekanismToolsConfig.tools.steel)),
    STEEL_SWORD(new ItemMekanismSword(MekanismToolsConfig.tools.steel)),
    STEEL_PAXEL(new ItemMekanismPaxel(MekanismToolsConfig.tools.steel)),
    STEEL_HELMET(new ItemMekanismArmor(MekanismToolsConfig.tools.steel, EquipmentSlotType.HEAD)),
    STEEL_CHESTPLATE(new ItemMekanismArmor(MekanismToolsConfig.tools.steel, EquipmentSlotType.CHEST)),
    STEEL_LEGGINGS(new ItemMekanismArmor(MekanismToolsConfig.tools.steel, EquipmentSlotType.LEGS)),
    STEEL_BOOTS(new ItemMekanismArmor(MekanismToolsConfig.tools.steel, EquipmentSlotType.FEET));

    @Nonnull
    private Item item;

    <ITEM extends Item & IHasRepairType> ToolsItem(@Nonnull ITEM item) {
        this.item = item;
    }

    @Nonnull
    @Override
    public Item getItem() {
        return item;
    }

    private void updateItem(Item item) {
        this.item = item;
    }

    @Nonnull
    public Ingredient getRepairMaterial() {
        //All cases currently implement IHasRepairType but just in case we decide to add some eventually that doesn't
        return item instanceof IHasRepairType ? ((IHasRepairType) item).getRepairMaterial() : Ingredient.EMPTY;
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        for (ToolsItem toolsItem : values()) {
            Item item = toolsItem.getItem();
            registry.register(item);
            if (item instanceof IItemMekanism) {
                ((IItemMekanism) item).registerOreDict();
            }
        }
    }

    public static void remapItems() {
        //TODO: Add this to other modules
        for (ToolsItem toolsItem : values()) {
            ResourceLocation registryName = toolsItem.getItem().getRegistryName();
            toolsItem.updateItem(ForgeRegistries.ITEMS.getValue(registryName));
        }
    }
}