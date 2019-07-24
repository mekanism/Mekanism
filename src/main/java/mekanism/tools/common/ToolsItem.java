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

    GLOWSTONE_PICKAXE("GlowstonePickaxe", new ItemMekanismPickaxe(MekanismTools.toolGLOWSTONE)),
    GLOWSTONE_AXE("GlowstoneAxe", new ItemMekanismAxe(MekanismTools.toolGLOWSTONE)),
    GLOWSTONE_SHOVEL("GlowstoneShovel", new ItemMekanismShovel(MekanismTools.toolGLOWSTONE)),
    GLOWSTONE_HOE("GlowstoneHoe", new ItemMekanismHoe(MekanismTools.toolGLOWSTONE)),
    GLOWSTONE_SWORD("GlowstoneSword", new ItemMekanismSword(MekanismTools.toolGLOWSTONE)),
    GLOWSTONE_PAXEL("GlowstonePaxel", MekanismTools.toolGLOWSTONE2, GLOWSTONE_PICKAXE, GLOWSTONE_SHOVEL, GLOWSTONE_AXE),
    GLOWSTONE_HELMET("GlowstoneHelmet", new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, 0, EntityEquipmentSlot.HEAD)),
    GLOWSTONE_CHESTPLATE("GlowstoneChestplate", new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, 1, EntityEquipmentSlot.CHEST)),
    GLOWSTONE_LEGGINGS("GlowstoneLeggings", new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, 2, EntityEquipmentSlot.LEGS)),
    GLOWSTONE_BOOTS("GlowstoneBoots", new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, 3, EntityEquipmentSlot.FEET)),

    BRONZE_PICKAXE("BronzePickaxe", new ItemMekanismPickaxe(MekanismTools.toolBRONZE)),
    BRONZE_AXE("BronzeAxe", new ItemMekanismAxe(MekanismTools.toolBRONZE)),
    BRONZE_SHOVEL("BronzeShovel", new ItemMekanismShovel(MekanismTools.toolBRONZE)),
    BRONZE_HOE("BronzeHoe", new ItemMekanismHoe(MekanismTools.toolBRONZE)),
    BRONZE_SWORD("BronzeSword", new ItemMekanismSword(MekanismTools.toolBRONZE)),
    BRONZE_PAXEL("BronzePaxel", MekanismTools.toolBRONZE2, BRONZE_PICKAXE, BRONZE_SHOVEL, BRONZE_AXE),
    BRONZE_HELMET("BronzeHelmet", new ItemMekanismArmor(MekanismTools.armorBRONZE, 0, EntityEquipmentSlot.HEAD)),
    BRONZE_CHESTPLATE("BronzeChestplate", new ItemMekanismArmor(MekanismTools.armorBRONZE, 1, EntityEquipmentSlot.CHEST)),
    BRONZE_LEGGINGS("BronzeLeggings", new ItemMekanismArmor(MekanismTools.armorBRONZE, 2, EntityEquipmentSlot.LEGS)),
    BRONZE_BOOTS("BronzeBoots", new ItemMekanismArmor(MekanismTools.armorBRONZE, 3, EntityEquipmentSlot.FEET)),

    OSMIUM_PICKAXE("OsmiumPickaxe", new ItemMekanismPickaxe(MekanismTools.toolOSMIUM)),
    OSMIUM_AXE("OsmiumAxe", new ItemMekanismAxe(MekanismTools.toolOSMIUM)),
    OSMIUM_SHOVEL("OsmiumShovel", new ItemMekanismShovel(MekanismTools.toolOSMIUM)),
    OSMIUM_HOE("OsmiumHoe", new ItemMekanismHoe(MekanismTools.toolOSMIUM)),
    OSMIUM_SWORD("OsmiumSword", new ItemMekanismSword(MekanismTools.toolOSMIUM)),
    OSMIUM_PAXEL("OsmiumPaxel", MekanismTools.toolOSMIUM2, OSMIUM_PICKAXE, OSMIUM_SHOVEL, OSMIUM_AXE),
    OSMIUM_HELMET("OsmiumHelmet", new ItemMekanismArmor(MekanismTools.armorOSMIUM, 0, EntityEquipmentSlot.HEAD)),
    OSMIUM_CHESTPLATE("OsmiumChestplate", new ItemMekanismArmor(MekanismTools.armorOSMIUM, 1, EntityEquipmentSlot.CHEST)),
    OSMIUM_LEGGINGS("OsmiumLeggings", new ItemMekanismArmor(MekanismTools.armorOSMIUM, 2, EntityEquipmentSlot.LEGS)),
    OSMIUM_BOOTS("OsmiumBoots", new ItemMekanismArmor(MekanismTools.armorOSMIUM, 3, EntityEquipmentSlot.FEET)),

    OBSIDIAN_PICKAXE("ObsidianPickaxe", new ItemMekanismPickaxe(MekanismTools.toolOBSIDIAN)),
    OBSIDIAN_AXE("ObsidianAxe", new ItemMekanismAxe(MekanismTools.toolOBSIDIAN)),
    OBSIDIAN_SHOVEL("ObsidianShovel", new ItemMekanismShovel(MekanismTools.toolOBSIDIAN)),
    OBSIDIAN_HOE("ObsidianHoe", new ItemMekanismHoe(MekanismTools.toolOBSIDIAN)),
    OBSIDIAN_SWORD("ObsidianSword", new ItemMekanismSword(MekanismTools.toolOBSIDIAN)),
    OBSIDIAN_PAXEL("ObsidianPaxel", MekanismTools.toolOBSIDIAN2, OBSIDIAN_PICKAXE, OBSIDIAN_SHOVEL, OBSIDIAN_AXE),
    OBSIDIAN_HELMET("ObsidianHelmet", new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, 0, EntityEquipmentSlot.HEAD)),
    OBSIDIAN_CHESTPLATE("ObsidianChestplate", new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, 1, EntityEquipmentSlot.CHEST)),
    OBSIDIAN_LEGGINGS("ObsidianLeggings", new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, 2, EntityEquipmentSlot.LEGS)),
    OBSIDIAN_BOOTS("ObsidianBoots", new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, 3, EntityEquipmentSlot.FEET)),

    LAPIS_LAZULI_PICKAXE("LapisLazuliPickaxe", new ItemMekanismPickaxe(MekanismTools.toolLAZULI)),
    LAPIS_LAZULI_AXE("LapisLazuliAxe", new ItemMekanismAxe(MekanismTools.toolLAZULI)),
    LAPIS_LAZULI_SHOVEL("LapisLazuliShovel", new ItemMekanismShovel(MekanismTools.toolLAZULI)),
    LAPIS_LAZULI_HOE("LapisLazuliHoe", new ItemMekanismHoe(MekanismTools.toolLAZULI)),
    LAPIS_LAZULI_SWORD("LapisLazuliSword", new ItemMekanismSword(MekanismTools.toolLAZULI)),
    LAPIS_LAZULI_PAXEL("LapisLazuliPaxel", MekanismTools.toolLAZULI2, LAPIS_LAZULI_PICKAXE, LAPIS_LAZULI_SHOVEL, LAPIS_LAZULI_AXE),
    LAPIS_LAZULI_HELMET("LapisLazuliHelmet", new ItemMekanismArmor(MekanismTools.armorLAZULI, 0, EntityEquipmentSlot.HEAD)),
    LAPIS_LAZULI_CHESTPLATE("LapisLazuliChestplate", new ItemMekanismArmor(MekanismTools.armorLAZULI, 1, EntityEquipmentSlot.CHEST)),
    LAPIS_LAZULI_LEGGINGS("LapisLazuliLeggings", new ItemMekanismArmor(MekanismTools.armorLAZULI, 2, EntityEquipmentSlot.LEGS)),
    LAPIS_LAZULI_BOOTS("LapisLazuliBoots", new ItemMekanismArmor(MekanismTools.armorLAZULI, 3, EntityEquipmentSlot.FEET)),

    STEEL_PICKAXE("SteelPickaxe", new ItemMekanismPickaxe(MekanismTools.toolSTEEL)),
    STEEL_AXE("SteelAxe", new ItemMekanismAxe(MekanismTools.toolSTEEL)),
    STEEL_SHOVEL("SteelShovel", new ItemMekanismShovel(MekanismTools.toolSTEEL)),
    STEEL_HOE("SteelHoe", new ItemMekanismHoe(MekanismTools.toolSTEEL)),
    STEEL_SWORD("SteelSword", new ItemMekanismSword(MekanismTools.toolSTEEL)),
    STEEL_PAXEL("SteelPaxel", MekanismTools.toolSTEEL2, STEEL_PICKAXE, STEEL_SHOVEL, STEEL_AXE),
    STEEL_HELMET("SteelHelmet", new ItemMekanismArmor(MekanismTools.armorSTEEL, 0, EntityEquipmentSlot.HEAD)),
    STEEL_CHESTPLATE("SteelChestplate", new ItemMekanismArmor(MekanismTools.armorSTEEL, 1, EntityEquipmentSlot.CHEST)),
    STEEL_LEGGINGS("SteelLeggings", new ItemMekanismArmor(MekanismTools.armorSTEEL, 2, EntityEquipmentSlot.LEGS)),
    STEEL_BOOTS("SteelBoots", new ItemMekanismArmor(MekanismTools.armorSTEEL, 3, EntityEquipmentSlot.FEET));

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

    ToolsItem(String name, ToolMaterial paxelMaterial, ToolsItem pickaxe, ToolsItem shovel, ToolsItem axe) {
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