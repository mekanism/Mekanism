package mekanism.tools.common;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.tools.item.ItemMekanismArmor;
import mekanism.tools.item.ItemMekanismAxe;
import mekanism.tools.item.ItemMekanismHoe;
import mekanism.tools.item.ItemMekanismPaxel;
import mekanism.tools.item.ItemMekanismPickaxe;
import mekanism.tools.item.ItemMekanismShovel;
import mekanism.tools.item.ItemMekanismSword;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

public enum ToolsItem {
    WOOD_PAXEL("WoodPaxel", new ItemMekanismPaxel(ToolMaterial.WOOD)),
    STONE_PAXEL("StonePaxel", new ItemMekanismPaxel(ToolMaterial.STONE)),
    IRON_PAXEL("IronPaxel", new ItemMekanismPaxel(ToolMaterial.IRON)),
    DIAMOND_PAXEL("DiamondPaxel", new ItemMekanismPaxel(ToolMaterial.DIAMOND)),
    GOLD_PAXEL("GoldPaxel", new ItemMekanismPaxel(ToolMaterial.GOLD)),

    GLOWSTONE_PICKAXE("GlowstonePickaxe", new ItemMekanismPickaxe(Materials.GLOWSTONE)),
    GLOWSTONE_AXE("GlowstoneAxe", new ItemMekanismAxe(Materials.GLOWSTONE)),
    GLOWSTONE_SHOVEL("GlowstoneShovel", new ItemMekanismShovel(Materials.GLOWSTONE)),
    GLOWSTONE_HOE("GlowstoneHoe", new ItemMekanismHoe(Materials.GLOWSTONE)),
    GLOWSTONE_SWORD("GlowstoneSword", new ItemMekanismSword(Materials.GLOWSTONE)),
    GLOWSTONE_PAXEL("GlowstonePaxel", new ItemMekanismPaxel(Materials.GLOWSTONE)),
    GLOWSTONE_HELMET("GlowstoneHelmet", new ItemMekanismArmor(Materials.GLOWSTONE, 0, EntityEquipmentSlot.HEAD)),
    GLOWSTONE_CHESTPLATE("GlowstoneChestplate", new ItemMekanismArmor(Materials.GLOWSTONE, 1, EntityEquipmentSlot.CHEST)),
    GLOWSTONE_LEGGINGS("GlowstoneLeggings", new ItemMekanismArmor(Materials.GLOWSTONE, 2, EntityEquipmentSlot.LEGS)),
    GLOWSTONE_BOOTS("GlowstoneBoots", new ItemMekanismArmor(Materials.GLOWSTONE, 3, EntityEquipmentSlot.FEET)),

    BRONZE_PICKAXE("BronzePickaxe", new ItemMekanismPickaxe(Materials.BRONZE)),
    BRONZE_AXE("BronzeAxe", new ItemMekanismAxe(Materials.BRONZE)),
    BRONZE_SHOVEL("BronzeShovel", new ItemMekanismShovel(Materials.BRONZE)),
    BRONZE_HOE("BronzeHoe", new ItemMekanismHoe(Materials.BRONZE)),
    BRONZE_SWORD("BronzeSword", new ItemMekanismSword(Materials.BRONZE)),
    BRONZE_PAXEL("BronzePaxel", new ItemMekanismPaxel(Materials.BRONZE)),
    BRONZE_HELMET("BronzeHelmet", new ItemMekanismArmor(Materials.BRONZE, 0, EntityEquipmentSlot.HEAD)),
    BRONZE_CHESTPLATE("BronzeChestplate", new ItemMekanismArmor(Materials.BRONZE, 1, EntityEquipmentSlot.CHEST)),
    BRONZE_LEGGINGS("BronzeLeggings", new ItemMekanismArmor(Materials.BRONZE, 2, EntityEquipmentSlot.LEGS)),
    BRONZE_BOOTS("BronzeBoots", new ItemMekanismArmor(Materials.BRONZE, 3, EntityEquipmentSlot.FEET)),

    OSMIUM_PICKAXE("OsmiumPickaxe", new ItemMekanismPickaxe(Materials.OSMIUM)),
    OSMIUM_AXE("OsmiumAxe", new ItemMekanismAxe(Materials.OSMIUM)),
    OSMIUM_SHOVEL("OsmiumShovel", new ItemMekanismShovel(Materials.OSMIUM)),
    OSMIUM_HOE("OsmiumHoe", new ItemMekanismHoe(Materials.OSMIUM)),
    OSMIUM_SWORD("OsmiumSword", new ItemMekanismSword(Materials.OSMIUM)),
    OSMIUM_PAXEL("OsmiumPaxel", new ItemMekanismPaxel(Materials.OSMIUM)),
    OSMIUM_HELMET("OsmiumHelmet", new ItemMekanismArmor(Materials.OSMIUM, 0, EntityEquipmentSlot.HEAD)),
    OSMIUM_CHESTPLATE("OsmiumChestplate", new ItemMekanismArmor(Materials.OSMIUM, 1, EntityEquipmentSlot.CHEST)),
    OSMIUM_LEGGINGS("OsmiumLeggings", new ItemMekanismArmor(Materials.OSMIUM, 2, EntityEquipmentSlot.LEGS)),
    OSMIUM_BOOTS("OsmiumBoots", new ItemMekanismArmor(Materials.OSMIUM, 3, EntityEquipmentSlot.FEET)),

    OBSIDIAN_PICKAXE("ObsidianPickaxe", new ItemMekanismPickaxe(Materials.OBSIDIAN)),
    OBSIDIAN_AXE("ObsidianAxe", new ItemMekanismAxe(Materials.OBSIDIAN)),
    OBSIDIAN_SHOVEL("ObsidianShovel", new ItemMekanismShovel(Materials.OBSIDIAN)),
    OBSIDIAN_HOE("ObsidianHoe", new ItemMekanismHoe(Materials.OBSIDIAN)),
    OBSIDIAN_SWORD("ObsidianSword", new ItemMekanismSword(Materials.OBSIDIAN)),
    OBSIDIAN_PAXEL("ObsidianPaxel", new ItemMekanismPaxel(Materials.OBSIDIAN)),
    OBSIDIAN_HELMET("ObsidianHelmet", new ItemMekanismArmor(Materials.OBSIDIAN, 0, EntityEquipmentSlot.HEAD)),
    OBSIDIAN_CHESTPLATE("ObsidianChestplate", new ItemMekanismArmor(Materials.OBSIDIAN, 1, EntityEquipmentSlot.CHEST)),
    OBSIDIAN_LEGGINGS("ObsidianLeggings", new ItemMekanismArmor(Materials.OBSIDIAN, 2, EntityEquipmentSlot.LEGS)),
    OBSIDIAN_BOOTS("ObsidianBoots", new ItemMekanismArmor(Materials.OBSIDIAN, 3, EntityEquipmentSlot.FEET)),

    LAPIS_LAZULI_PICKAXE("LapisLazuliPickaxe", new ItemMekanismPickaxe(Materials.LAZULI)),
    LAPIS_LAZULI_AXE("LapisLazuliAxe", new ItemMekanismAxe(Materials.LAZULI)),
    LAPIS_LAZULI_SHOVEL("LapisLazuliShovel", new ItemMekanismShovel(Materials.LAZULI)),
    LAPIS_LAZULI_HOE("LapisLazuliHoe", new ItemMekanismHoe(Materials.LAZULI)),
    LAPIS_LAZULI_SWORD("LapisLazuliSword", new ItemMekanismSword(Materials.LAZULI)),
    LAPIS_LAZULI_PAXEL("LapisLazuliPaxel", new ItemMekanismPaxel(Materials.LAZULI)),
    LAPIS_LAZULI_HELMET("LapisLazuliHelmet", new ItemMekanismArmor(Materials.LAZULI, 0, EntityEquipmentSlot.HEAD)),
    LAPIS_LAZULI_CHESTPLATE("LapisLazuliChestplate", new ItemMekanismArmor(Materials.LAZULI, 1, EntityEquipmentSlot.CHEST)),
    LAPIS_LAZULI_LEGGINGS("LapisLazuliLeggings", new ItemMekanismArmor(Materials.LAZULI, 2, EntityEquipmentSlot.LEGS)),
    LAPIS_LAZULI_BOOTS("LapisLazuliBoots", new ItemMekanismArmor(Materials.LAZULI, 3, EntityEquipmentSlot.FEET)),

    STEEL_PICKAXE("SteelPickaxe", new ItemMekanismPickaxe(Materials.STEEL)),
    STEEL_AXE("SteelAxe", new ItemMekanismAxe(Materials.STEEL)),
    STEEL_SHOVEL("SteelShovel", new ItemMekanismShovel(Materials.STEEL)),
    STEEL_HOE("SteelHoe", new ItemMekanismHoe(Materials.STEEL)),
    STEEL_SWORD("SteelSword", new ItemMekanismSword(Materials.STEEL)),
    STEEL_PAXEL("SteelPaxel", new ItemMekanismPaxel(Materials.STEEL)),
    STEEL_HELMET("SteelHelmet", new ItemMekanismArmor(Materials.STEEL, 0, EntityEquipmentSlot.HEAD)),
    STEEL_CHESTPLATE("SteelChestplate", new ItemMekanismArmor(Materials.STEEL, 1, EntityEquipmentSlot.CHEST)),
    STEEL_LEGGINGS("SteelLeggings", new ItemMekanismArmor(Materials.STEEL, 2, EntityEquipmentSlot.LEGS)),
    STEEL_BOOTS("SteelBoots", new ItemMekanismArmor(Materials.STEEL, 3, EntityEquipmentSlot.FEET));

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

    <ITEM extends Item & IHasRepairType> ToolsItem(@Nonnull String name, @Nonnull ITEM item) {
        //TODO: Make name be part of item instead of added on this extra layer.
        // Also make them have underscores rather than "fake" capitalization that is just to make it easier to read in the enum
        // This note is for 1.14 when we are going to be mass changing ids anyways to flatten things
        this.item = item.setTranslationKey(name).setRegistryName(new ResourceLocation(MekanismTools.MODID, name)).setCreativeTab(Mekanism.tabMekanism);
    }

    @Nonnull
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
    public ItemStack getItemStack() {
        return getItemStack(1);
    }

    @Nonnull
    public ItemStack getItemStack(int size) {
        return new ItemStack(getItem(), size);
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
            registry.register(toolsItem.getItem());
        }
    }

    public static void remapItems() {
        for (ToolsItem toolsItem : values()) {
            if(!toolsItem.getItemStack().isEmpty()) {
                ResourceLocation registryName = toolsItem.getItem().getRegistryName();
                toolsItem.updateItem(ForgeRegistries.ITEMS.getValue(registryName));
            }
        }
    }
}