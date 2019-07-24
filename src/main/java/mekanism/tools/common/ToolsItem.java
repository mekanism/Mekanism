package mekanism.tools.common;

import java.util.Arrays;
import java.util.List;
import mekanism.common.Mekanism;
import mekanism.tools.item.ItemMekanismArmor;
import mekanism.tools.item.ItemMekanismAxe;
import mekanism.tools.item.ItemMekanismHoe;
import mekanism.tools.item.ItemMekanismPaxel;
import mekanism.tools.item.ItemMekanismPickaxe;
import mekanism.tools.item.ItemMekanismShovel;
import mekanism.tools.item.ItemMekanismSword;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public enum ToolsItem {
    WOOD_PAXEL("WoodPaxel", new ItemMekanismPaxel(ToolMaterial.WOOD, (ItemPickaxe) Items.WOODEN_PICKAXE, (ItemSpade) Items.WOODEN_SHOVEL, (ItemAxe) Items.WOODEN_AXE)),
    STONE_PAXEL("StonePaxel", new ItemMekanismPaxel(ToolMaterial.STONE, (ItemPickaxe) Items.STONE_PICKAXE, (ItemSpade) Items.STONE_SHOVEL, (ItemAxe) Items.STONE_AXE)),
    IRON_PAXEL("IronPaxel", new ItemMekanismPaxel(ToolMaterial.IRON, (ItemPickaxe) Items.IRON_PICKAXE, (ItemSpade) Items.IRON_SHOVEL, (ItemAxe) Items.IRON_AXE)),
    DIAMOND_PAXEL("DiamondPaxel", new ItemMekanismPaxel(ToolMaterial.DIAMOND, (ItemPickaxe) Items.DIAMOND_PICKAXE, (ItemSpade) Items.DIAMOND_SHOVEL, (ItemAxe) Items.DIAMOND_AXE)),
    GOLD_PAXEL("GoldPaxel", new ItemMekanismPaxel(ToolMaterial.GOLD, (ItemPickaxe) Items.GOLDEN_PICKAXE, (ItemSpade) Items.GOLDEN_SHOVEL, (ItemAxe) Items.GOLDEN_AXE)),

    GLOWSTONE_PICKAXE("GlowstonePickaxe", new ItemMekanismPickaxe(Materials.GLOWSTONE)),
    GLOWSTONE_AXE("GlowstoneAxe", new ItemMekanismAxe(Materials.GLOWSTONE)),
    GLOWSTONE_SHOVEL("GlowstoneShovel", new ItemMekanismShovel(Materials.GLOWSTONE)),
    GLOWSTONE_HOE("GlowstoneHoe", new ItemMekanismHoe(Materials.GLOWSTONE)),
    GLOWSTONE_SWORD("GlowstoneSword", new ItemMekanismSword(Materials.GLOWSTONE)),
    GLOWSTONE_PAXEL("GlowstonePaxel", Materials.GLOWSTONE, GLOWSTONE_PICKAXE, GLOWSTONE_SHOVEL, GLOWSTONE_AXE),
    GLOWSTONE_HELMET("GlowstoneHelmet", new ItemMekanismArmor(Materials.GLOWSTONE, 0, EntityEquipmentSlot.HEAD)),
    GLOWSTONE_CHESTPLATE("GlowstoneChestplate", new ItemMekanismArmor(Materials.GLOWSTONE, 1, EntityEquipmentSlot.CHEST)),
    GLOWSTONE_LEGGINGS("GlowstoneLeggings", new ItemMekanismArmor(Materials.GLOWSTONE, 2, EntityEquipmentSlot.LEGS)),
    GLOWSTONE_BOOTS("GlowstoneBoots", new ItemMekanismArmor(Materials.GLOWSTONE, 3, EntityEquipmentSlot.FEET)),

    BRONZE_PICKAXE("BronzePickaxe", new ItemMekanismPickaxe(Materials.BRONZE)),
    BRONZE_AXE("BronzeAxe", new ItemMekanismAxe(Materials.BRONZE)),
    BRONZE_SHOVEL("BronzeShovel", new ItemMekanismShovel(Materials.BRONZE)),
    BRONZE_HOE("BronzeHoe", new ItemMekanismHoe(Materials.BRONZE)),
    BRONZE_SWORD("BronzeSword", new ItemMekanismSword(Materials.BRONZE)),
    BRONZE_PAXEL("BronzePaxel", Materials.BRONZE, BRONZE_PICKAXE, BRONZE_SHOVEL, BRONZE_AXE),
    BRONZE_HELMET("BronzeHelmet", new ItemMekanismArmor(Materials.BRONZE, 0, EntityEquipmentSlot.HEAD)),
    BRONZE_CHESTPLATE("BronzeChestplate", new ItemMekanismArmor(Materials.BRONZE, 1, EntityEquipmentSlot.CHEST)),
    BRONZE_LEGGINGS("BronzeLeggings", new ItemMekanismArmor(Materials.BRONZE, 2, EntityEquipmentSlot.LEGS)),
    BRONZE_BOOTS("BronzeBoots", new ItemMekanismArmor(Materials.BRONZE, 3, EntityEquipmentSlot.FEET)),

    OSMIUM_PICKAXE("OsmiumPickaxe", new ItemMekanismPickaxe(Materials.OSMIUM)),
    OSMIUM_AXE("OsmiumAxe", new ItemMekanismAxe(Materials.OSMIUM)),
    OSMIUM_SHOVEL("OsmiumShovel", new ItemMekanismShovel(Materials.OSMIUM)),
    OSMIUM_HOE("OsmiumHoe", new ItemMekanismHoe(Materials.OSMIUM)),
    OSMIUM_SWORD("OsmiumSword", new ItemMekanismSword(Materials.OSMIUM)),
    OSMIUM_PAXEL("OsmiumPaxel", Materials.OSMIUM, OSMIUM_PICKAXE, OSMIUM_SHOVEL, OSMIUM_AXE),
    OSMIUM_HELMET("OsmiumHelmet", new ItemMekanismArmor(Materials.OSMIUM, 0, EntityEquipmentSlot.HEAD)),
    OSMIUM_CHESTPLATE("OsmiumChestplate", new ItemMekanismArmor(Materials.OSMIUM, 1, EntityEquipmentSlot.CHEST)),
    OSMIUM_LEGGINGS("OsmiumLeggings", new ItemMekanismArmor(Materials.OSMIUM, 2, EntityEquipmentSlot.LEGS)),
    OSMIUM_BOOTS("OsmiumBoots", new ItemMekanismArmor(Materials.OSMIUM, 3, EntityEquipmentSlot.FEET)),

    OBSIDIAN_PICKAXE("ObsidianPickaxe", new ItemMekanismPickaxe(Materials.OBSIDIAN)),
    OBSIDIAN_AXE("ObsidianAxe", new ItemMekanismAxe(Materials.OBSIDIAN)),
    OBSIDIAN_SHOVEL("ObsidianShovel", new ItemMekanismShovel(Materials.OBSIDIAN)),
    OBSIDIAN_HOE("ObsidianHoe", new ItemMekanismHoe(Materials.OBSIDIAN)),
    OBSIDIAN_SWORD("ObsidianSword", new ItemMekanismSword(Materials.OBSIDIAN)),
    OBSIDIAN_PAXEL("ObsidianPaxel", Materials.OBSIDIAN, OBSIDIAN_PICKAXE, OBSIDIAN_SHOVEL, OBSIDIAN_AXE),
    OBSIDIAN_HELMET("ObsidianHelmet", new ItemMekanismArmor(Materials.OBSIDIAN, 0, EntityEquipmentSlot.HEAD)),
    OBSIDIAN_CHESTPLATE("ObsidianChestplate", new ItemMekanismArmor(Materials.OBSIDIAN, 1, EntityEquipmentSlot.CHEST)),
    OBSIDIAN_LEGGINGS("ObsidianLeggings", new ItemMekanismArmor(Materials.OBSIDIAN, 2, EntityEquipmentSlot.LEGS)),
    OBSIDIAN_BOOTS("ObsidianBoots", new ItemMekanismArmor(Materials.OBSIDIAN, 3, EntityEquipmentSlot.FEET)),

    LAPIS_LAZULI_PICKAXE("LapisLazuliPickaxe", new ItemMekanismPickaxe(Materials.LAZULI)),
    LAPIS_LAZULI_AXE("LapisLazuliAxe", new ItemMekanismAxe(Materials.LAZULI)),
    LAPIS_LAZULI_SHOVEL("LapisLazuliShovel", new ItemMekanismShovel(Materials.LAZULI)),
    LAPIS_LAZULI_HOE("LapisLazuliHoe", new ItemMekanismHoe(Materials.LAZULI)),
    LAPIS_LAZULI_SWORD("LapisLazuliSword", new ItemMekanismSword(Materials.LAZULI)),
    LAPIS_LAZULI_PAXEL("LapisLazuliPaxel", Materials.LAZULI, LAPIS_LAZULI_PICKAXE, LAPIS_LAZULI_SHOVEL, LAPIS_LAZULI_AXE),
    LAPIS_LAZULI_HELMET("LapisLazuliHelmet", new ItemMekanismArmor(Materials.LAZULI, 0, EntityEquipmentSlot.HEAD)),
    LAPIS_LAZULI_CHESTPLATE("LapisLazuliChestplate", new ItemMekanismArmor(Materials.LAZULI, 1, EntityEquipmentSlot.CHEST)),
    LAPIS_LAZULI_LEGGINGS("LapisLazuliLeggings", new ItemMekanismArmor(Materials.LAZULI, 2, EntityEquipmentSlot.LEGS)),
    LAPIS_LAZULI_BOOTS("LapisLazuliBoots", new ItemMekanismArmor(Materials.LAZULI, 3, EntityEquipmentSlot.FEET)),

    STEEL_PICKAXE("SteelPickaxe", new ItemMekanismPickaxe(Materials.STEEL)),
    STEEL_AXE("SteelAxe", new ItemMekanismAxe(Materials.STEEL)),
    STEEL_SHOVEL("SteelShovel", new ItemMekanismShovel(Materials.STEEL)),
    STEEL_HOE("SteelHoe", new ItemMekanismHoe(Materials.STEEL)),
    STEEL_SWORD("SteelSword", new ItemMekanismSword(Materials.STEEL)),
    STEEL_PAXEL("SteelPaxel", Materials.STEEL, STEEL_PICKAXE, STEEL_SHOVEL, STEEL_AXE),
    STEEL_HELMET("SteelHelmet", new ItemMekanismArmor(Materials.STEEL, 0, EntityEquipmentSlot.HEAD)),
    STEEL_CHESTPLATE("SteelChestplate", new ItemMekanismArmor(Materials.STEEL, 1, EntityEquipmentSlot.CHEST)),
    STEEL_LEGGINGS("SteelLeggings", new ItemMekanismArmor(Materials.STEEL, 2, EntityEquipmentSlot.LEGS)),
    STEEL_BOOTS("SteelBoots", new ItemMekanismArmor(Materials.STEEL, 3, EntityEquipmentSlot.FEET));

    public static final List<ToolsItem> bronzeSet = Arrays.asList(BRONZE_PICKAXE, BRONZE_AXE, BRONZE_SHOVEL, BRONZE_HOE, BRONZE_SWORD, BRONZE_PAXEL, BRONZE_HELMET,
          BRONZE_CHESTPLATE, BRONZE_LEGGINGS, BRONZE_BOOTS);
    public static final List<ToolsItem> osmiumSet = Arrays.asList(OSMIUM_PICKAXE, OSMIUM_AXE, OSMIUM_SHOVEL, OSMIUM_HOE, OSMIUM_SWORD, OSMIUM_PAXEL, OSMIUM_HELMET,
          OSMIUM_CHESTPLATE, OSMIUM_LEGGINGS, OSMIUM_BOOTS);
    public static final List<ToolsItem> obsidianSet = Arrays.asList(OBSIDIAN_PICKAXE, OBSIDIAN_AXE, OBSIDIAN_SHOVEL, OBSIDIAN_HOE, OBSIDIAN_SWORD, OBSIDIAN_PAXEL,
          OBSIDIAN_HELMET, OBSIDIAN_CHESTPLATE, OBSIDIAN_LEGGINGS, OBSIDIAN_BOOTS);
    public static final List<ToolsItem> glowstoneSet = Arrays.asList(GLOWSTONE_PICKAXE, GLOWSTONE_AXE, GLOWSTONE_SHOVEL, GLOWSTONE_HOE, GLOWSTONE_SWORD, GLOWSTONE_PAXEL,
          GLOWSTONE_HELMET, GLOWSTONE_CHESTPLATE, GLOWSTONE_LEGGINGS, GLOWSTONE_BOOTS);
    public static final List<ToolsItem> steelSet = Arrays.asList(STEEL_PICKAXE, STEEL_AXE, STEEL_SHOVEL, STEEL_HOE, STEEL_SWORD, STEEL_PAXEL, STEEL_HELMET,
          STEEL_CHESTPLATE, STEEL_LEGGINGS, STEEL_BOOTS);

    private final Item item;

    ToolsItem(String name, Materials paxelMaterial, ToolsItem pickaxe, ToolsItem shovel, ToolsItem axe) {
        //Paxel helper
        this(name, new ItemMekanismPaxel(paxelMaterial, (ItemPickaxe) pickaxe.getItem(), (ItemSpade) shovel.getItem(), (ItemAxe) axe.getItem()));
    }

    ToolsItem(String name, Item item) {
        //TODO: Make name be part of item instead of added on this extra layer.
        // Also make them have underscores rather than "fake" capitalization that is just to make it easier to read in the enum
        this.item = item.setTranslationKey(name).setRegistryName(new ResourceLocation(MekanismTools.MODID, name)).setCreativeTab(Mekanism.tabMekanism);
    }

    public Item getItem() {
        return item;
    }

    public ItemStack getItemStack() {
        return getItemStack(1);
    }

    public ItemStack getItemStack(int size) {
        return new ItemStack(getItem(), size);
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        for (ToolsItem toolsItem : values()) {
            registry.register(toolsItem.getItem());
        }
    }
}