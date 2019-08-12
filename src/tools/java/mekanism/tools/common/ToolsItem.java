package mekanism.tools.common;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.base.IItemProvider;
import mekanism.common.item.IItemMekanism;
import mekanism.tools.item.ItemMekanismArmor;
import mekanism.tools.item.ItemMekanismAxe;
import mekanism.tools.item.ItemMekanismHoe;
import mekanism.tools.item.ItemMekanismPaxel;
import mekanism.tools.item.ItemMekanismPickaxe;
import mekanism.tools.item.ItemMekanismShovel;
import mekanism.tools.item.ItemMekanismSword;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

public enum ToolsItem implements IItemProvider {
    WOOD_PAXEL(new ItemMekanismPaxel(ToolMaterial.WOOD)),
    STONE_PAXEL(new ItemMekanismPaxel(ToolMaterial.STONE)),
    IRON_PAXEL(new ItemMekanismPaxel(ToolMaterial.IRON)),
    DIAMOND_PAXEL(new ItemMekanismPaxel(ToolMaterial.DIAMOND)),
    GOLD_PAXEL(new ItemMekanismPaxel(ToolMaterial.GOLD)),

    GLOWSTONE_PICKAXE(new ItemMekanismPickaxe(Materials.GLOWSTONE)),
    GLOWSTONE_AXE(new ItemMekanismAxe(Materials.GLOWSTONE)),
    GLOWSTONE_SHOVEL(new ItemMekanismShovel(Materials.GLOWSTONE)),
    GLOWSTONE_HOE(new ItemMekanismHoe(Materials.GLOWSTONE)),
    GLOWSTONE_SWORD(new ItemMekanismSword(Materials.GLOWSTONE)),
    GLOWSTONE_PAXEL(new ItemMekanismPaxel(Materials.GLOWSTONE)),
    GLOWSTONE_HELMET(new ItemMekanismArmor(Materials.GLOWSTONE, 0, EquipmentSlotType.HEAD)),
    GLOWSTONE_CHESTPLATE(new ItemMekanismArmor(Materials.GLOWSTONE, 1, EquipmentSlotType.CHEST)),
    GLOWSTONE_LEGGINGS(new ItemMekanismArmor(Materials.GLOWSTONE, 2, EquipmentSlotType.LEGS)),
    GLOWSTONE_BOOTS(new ItemMekanismArmor(Materials.GLOWSTONE, 3, EquipmentSlotType.FEET)),

    BRONZE_PICKAXE(new ItemMekanismPickaxe(Materials.BRONZE)),
    BRONZE_AXE(new ItemMekanismAxe(Materials.BRONZE)),
    BRONZE_SHOVEL(new ItemMekanismShovel(Materials.BRONZE)),
    BRONZE_HOE(new ItemMekanismHoe(Materials.BRONZE)),
    BRONZE_SWORD(new ItemMekanismSword(Materials.BRONZE)),
    BRONZE_PAXEL(new ItemMekanismPaxel(Materials.BRONZE)),
    BRONZE_HELMET(new ItemMekanismArmor(Materials.BRONZE, 0, EquipmentSlotType.HEAD)),
    BRONZE_CHESTPLATE(new ItemMekanismArmor(Materials.BRONZE, 1, EquipmentSlotType.CHEST)),
    BRONZE_LEGGINGS(new ItemMekanismArmor(Materials.BRONZE, 2, EquipmentSlotType.LEGS)),
    BRONZE_BOOTS(new ItemMekanismArmor(Materials.BRONZE, 3, EquipmentSlotType.FEET)),

    OSMIUM_PICKAXE(new ItemMekanismPickaxe(Materials.OSMIUM)),
    OSMIUM_AXE(new ItemMekanismAxe(Materials.OSMIUM)),
    OSMIUM_SHOVEL(new ItemMekanismShovel(Materials.OSMIUM)),
    OSMIUM_HOE(new ItemMekanismHoe(Materials.OSMIUM)),
    OSMIUM_SWORD(new ItemMekanismSword(Materials.OSMIUM)),
    OSMIUM_PAXEL(new ItemMekanismPaxel(Materials.OSMIUM)),
    OSMIUM_HELMET(new ItemMekanismArmor(Materials.OSMIUM, 0, EquipmentSlotType.HEAD)),
    OSMIUM_CHESTPLATE(new ItemMekanismArmor(Materials.OSMIUM, 1, EquipmentSlotType.CHEST)),
    OSMIUM_LEGGINGS(new ItemMekanismArmor(Materials.OSMIUM, 2, EquipmentSlotType.LEGS)),
    OSMIUM_BOOTS(new ItemMekanismArmor(Materials.OSMIUM, 3, EquipmentSlotType.FEET)),

    OBSIDIAN_PICKAXE(new ItemMekanismPickaxe(Materials.OBSIDIAN)),
    OBSIDIAN_AXE(new ItemMekanismAxe(Materials.OBSIDIAN)),
    OBSIDIAN_SHOVEL(new ItemMekanismShovel(Materials.OBSIDIAN)),
    OBSIDIAN_HOE(new ItemMekanismHoe(Materials.OBSIDIAN)),
    OBSIDIAN_SWORD(new ItemMekanismSword(Materials.OBSIDIAN)),
    OBSIDIAN_PAXEL(new ItemMekanismPaxel(Materials.OBSIDIAN)),
    OBSIDIAN_HELMET(new ItemMekanismArmor(Materials.OBSIDIAN, 0, EquipmentSlotType.HEAD)),
    OBSIDIAN_CHESTPLATE(new ItemMekanismArmor(Materials.OBSIDIAN, 1, EquipmentSlotType.CHEST)),
    OBSIDIAN_LEGGINGS(new ItemMekanismArmor(Materials.OBSIDIAN, 2, EquipmentSlotType.LEGS)),
    OBSIDIAN_BOOTS(new ItemMekanismArmor(Materials.OBSIDIAN, 3, EquipmentSlotType.FEET)),

    LAPIS_LAZULI_PICKAXE(new ItemMekanismPickaxe(Materials.LAPIS_LAZULI)),
    LAPIS_LAZULI_AXE(new ItemMekanismAxe(Materials.LAPIS_LAZULI)),
    LAPIS_LAZULI_SHOVEL(new ItemMekanismShovel(Materials.LAPIS_LAZULI)),
    LAPIS_LAZULI_HOE(new ItemMekanismHoe(Materials.LAPIS_LAZULI)),
    LAPIS_LAZULI_SWORD(new ItemMekanismSword(Materials.LAPIS_LAZULI)),
    LAPIS_LAZULI_PAXEL(new ItemMekanismPaxel(Materials.LAPIS_LAZULI)),
    LAPIS_LAZULI_HELMET(new ItemMekanismArmor(Materials.LAPIS_LAZULI, 0, EquipmentSlotType.HEAD)),
    LAPIS_LAZULI_CHESTPLATE(new ItemMekanismArmor(Materials.LAPIS_LAZULI, 1, EquipmentSlotType.CHEST)),
    LAPIS_LAZULI_LEGGINGS(new ItemMekanismArmor(Materials.LAPIS_LAZULI, 2, EquipmentSlotType.LEGS)),
    LAPIS_LAZULI_BOOTS(new ItemMekanismArmor(Materials.LAPIS_LAZULI, 3, EquipmentSlotType.FEET)),

    STEEL_PICKAXE(new ItemMekanismPickaxe(Materials.STEEL)),
    STEEL_AXE(new ItemMekanismAxe(Materials.STEEL)),
    STEEL_SHOVEL(new ItemMekanismShovel(Materials.STEEL)),
    STEEL_HOE(new ItemMekanismHoe(Materials.STEEL)),
    STEEL_SWORD(new ItemMekanismSword(Materials.STEEL)),
    STEEL_PAXEL(new ItemMekanismPaxel(Materials.STEEL)),
    STEEL_HELMET(new ItemMekanismArmor(Materials.STEEL, 0, EquipmentSlotType.HEAD)),
    STEEL_CHESTPLATE(new ItemMekanismArmor(Materials.STEEL, 1, EquipmentSlotType.CHEST)),
    STEEL_LEGGINGS(new ItemMekanismArmor(Materials.STEEL, 2, EquipmentSlotType.LEGS)),
    STEEL_BOOTS(new ItemMekanismArmor(Materials.STEEL, 3, EquipmentSlotType.FEET));

    public static final List<ToolsItem> BRONZE_SET = Arrays.asList(BRONZE_PICKAXE, BRONZE_AXE, BRONZE_SHOVEL, BRONZE_HOE, BRONZE_SWORD, BRONZE_PAXEL, BRONZE_HELMET,
          BRONZE_CHESTPLATE, BRONZE_LEGGINGS, BRONZE_BOOTS);
    public static final List<ToolsItem> OSMIUM_SET = Arrays.asList(OSMIUM_PICKAXE, OSMIUM_AXE, OSMIUM_SHOVEL, OSMIUM_HOE, OSMIUM_SWORD, OSMIUM_PAXEL, OSMIUM_HELMET,
          OSMIUM_CHESTPLATE, OSMIUM_LEGGINGS, OSMIUM_BOOTS);
    public static final List<ToolsItem> OBSIDIAN_SET = Arrays.asList(OBSIDIAN_PICKAXE, OBSIDIAN_AXE, OBSIDIAN_SHOVEL, OBSIDIAN_HOE, OBSIDIAN_SWORD, OBSIDIAN_PAXEL,
          OBSIDIAN_HELMET, OBSIDIAN_CHESTPLATE, OBSIDIAN_LEGGINGS, OBSIDIAN_BOOTS);
    public static final List<ToolsItem> GLOWSTONE_SET = Arrays.asList(GLOWSTONE_PICKAXE, GLOWSTONE_AXE, GLOWSTONE_SHOVEL, GLOWSTONE_HOE, GLOWSTONE_SWORD, GLOWSTONE_PAXEL,
          GLOWSTONE_HELMET, GLOWSTONE_CHESTPLATE, GLOWSTONE_LEGGINGS, GLOWSTONE_BOOTS);
    public static final List<ToolsItem> STEEL_SET = Arrays.asList(STEEL_PICKAXE, STEEL_AXE, STEEL_SHOVEL, STEEL_HOE, STEEL_SWORD, STEEL_PAXEL, STEEL_HELMET,
          STEEL_CHESTPLATE, STEEL_LEGGINGS, STEEL_BOOTS);
    //Unused but for consistency if something needs it
    public static final List<ToolsItem> LAPIS_LAZULI_SET = Arrays.asList(LAPIS_LAZULI_PICKAXE, LAPIS_LAZULI_AXE, LAPIS_LAZULI_SHOVEL, LAPIS_LAZULI_HOE, LAPIS_LAZULI_SWORD,
          LAPIS_LAZULI_PAXEL, LAPIS_LAZULI_HELMET, LAPIS_LAZULI_CHESTPLATE, LAPIS_LAZULI_LEGGINGS, LAPIS_LAZULI_BOOTS);

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
    public ItemStack getRepairStack() {
        //All cases currently implement IHasRepairType but just in case we decide to add some eventually that doesn't
        return item instanceof IHasRepairType ? ((IHasRepairType) item).getRepairStack() : ItemStack.EMPTY;
    }

    @Nonnull
    public ItemStack getItemStackAnyDamage() {
        return getItemStackAnyDamage(1);
    }

    @Nonnull
    public ItemStack getItemStackAnyDamage(int size) {
        return new ItemStack(getItem(), size, OreDictionary.WILDCARD_VALUE);
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        for (ToolsItem toolsItem : values()) {
            Item item = toolsItem.getItem();
            item.setCreativeTab(Mekanism.tabMekanism);
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