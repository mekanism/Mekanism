package mekanism.tools.common;

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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

@ObjectHolder(MekanismTools.MODID)
public class ToolsItems {

    //Vanilla Material Paxels
    public static Item WoodPaxel;
    public static Item StonePaxel;
    public static Item IronPaxel;
    public static Item DiamondPaxel;
    public static Item GoldPaxel;

    //Glowstone Items
    public static Item GlowstonePaxel;
    public static Item GlowstonePickaxe;
    public static Item GlowstoneAxe;
    public static Item GlowstoneShovel;
    public static Item GlowstoneHoe;
    public static Item GlowstoneSword;
    public static Item GlowstoneHelmet;
    public static Item GlowstoneChestplate;
    public static Item GlowstoneLeggings;
    public static Item GlowstoneBoots;

    //Bronze Items
    public static Item BronzePaxel;
    public static Item BronzePickaxe;
    public static Item BronzeAxe;
    public static Item BronzeShovel;
    public static Item BronzeHoe;
    public static Item BronzeSword;
    public static Item BronzeHelmet;
    public static Item BronzeChestplate;
    public static Item BronzeLeggings;
    public static Item BronzeBoots;

    //Osmium Items
    public static Item OsmiumPaxel;
    public static Item OsmiumPickaxe;
    public static Item OsmiumAxe;
    public static Item OsmiumShovel;
    public static Item OsmiumHoe;
    public static Item OsmiumSword;
    public static Item OsmiumHelmet;
    public static Item OsmiumChestplate;
    public static Item OsmiumLeggings;
    public static Item OsmiumBoots;

    //Obsidian Items
    public static Item ObsidianPaxel;
    public static Item ObsidianPickaxe;
    public static Item ObsidianAxe;
    public static Item ObsidianShovel;
    public static Item ObsidianHoe;
    public static Item ObsidianSword;
    public static Item ObsidianHelmet;
    public static Item ObsidianChestplate;
    public static Item ObsidianLeggings;
    public static Item ObsidianBoots;

    //Lazuli Items
    public static Item LazuliPaxel;
    public static Item LazuliPickaxe;
    public static Item LazuliAxe;
    public static Item LazuliShovel;
    public static Item LazuliHoe;
    public static Item LazuliSword;
    public static Item LazuliHelmet;
    public static Item LazuliChestplate;
    public static Item LazuliLeggings;
    public static Item LazuliBoots;

    //Steel Items
    public static Item SteelPaxel;
    public static Item SteelPickaxe;
    public static Item SteelAxe;
    public static Item SteelShovel;
    public static Item SteelHoe;
    public static Item SteelSword;
    public static Item SteelHelmet;
    public static Item SteelChestplate;
    public static Item SteelLeggings;
    public static Item SteelBoots;

    public static void initializeItems() {
        WoodPaxel = init(new ItemMekanismPaxel(ToolMaterial.WOOD), "WoodPaxel");
        StonePaxel = init(new ItemMekanismPaxel(ToolMaterial.STONE), "StonePaxel");
        IronPaxel = init(new ItemMekanismPaxel(ToolMaterial.IRON), "IronPaxel");
        DiamondPaxel = init(new ItemMekanismPaxel(ToolMaterial.DIAMOND), "DiamondPaxel");
        GoldPaxel = init(new ItemMekanismPaxel(ToolMaterial.GOLD), "GoldPaxel");
        GlowstonePaxel = init(new ItemMekanismPaxel(MekanismTools.toolGLOWSTONE2), "GlowstonePaxel");
        GlowstonePickaxe = init(new ItemMekanismPickaxe(MekanismTools.toolGLOWSTONE), "GlowstonePickaxe");
        GlowstoneAxe = init(new ItemMekanismAxe(MekanismTools.toolGLOWSTONE), "GlowstoneAxe");
        GlowstoneShovel = init(new ItemMekanismShovel(MekanismTools.toolGLOWSTONE), "GlowstoneShovel");
        GlowstoneHoe = init(new ItemMekanismHoe(MekanismTools.toolGLOWSTONE), "GlowstoneHoe");
        GlowstoneSword = init(new ItemMekanismSword(MekanismTools.toolGLOWSTONE), "GlowstoneSword");
        GlowstoneHelmet = init(new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, 0, EntityEquipmentSlot.HEAD),
              "GlowstoneHelmet");
        GlowstoneChestplate = init(new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, 1, EntityEquipmentSlot.CHEST),
              "GlowstoneChestplate");
        GlowstoneLeggings = init(new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, 2, EntityEquipmentSlot.LEGS),
              "GlowstoneLeggings");
        GlowstoneBoots = init(new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, 3, EntityEquipmentSlot.FEET),
              "GlowstoneBoots");
        BronzePaxel = init(new ItemMekanismPaxel(MekanismTools.toolBRONZE2), "BronzePaxel");
        BronzePickaxe = init(new ItemMekanismPickaxe(MekanismTools.toolBRONZE), "BronzePickaxe");
        BronzeAxe = init(new ItemMekanismAxe(MekanismTools.toolBRONZE), "BronzeAxe");
        BronzeShovel = init(new ItemMekanismShovel(MekanismTools.toolBRONZE), "BronzeShovel");
        BronzeHoe = init(new ItemMekanismHoe(MekanismTools.toolBRONZE), "BronzeHoe");
        BronzeSword = init(new ItemMekanismSword(MekanismTools.toolBRONZE), "BronzeSword");
        BronzeHelmet = init(new ItemMekanismArmor(MekanismTools.armorBRONZE, 0, EntityEquipmentSlot.HEAD),
              "BronzeHelmet");
        BronzeChestplate = init(new ItemMekanismArmor(MekanismTools.armorBRONZE, 1, EntityEquipmentSlot.CHEST),
              "BronzeChestplate");
        BronzeLeggings = init(new ItemMekanismArmor(MekanismTools.armorBRONZE, 2, EntityEquipmentSlot.LEGS),
              "BronzeLeggings");
        BronzeBoots = init(new ItemMekanismArmor(MekanismTools.armorBRONZE, 3, EntityEquipmentSlot.FEET),
              "BronzeBoots");
        OsmiumPaxel = init(new ItemMekanismPaxel(MekanismTools.toolOSMIUM2), "OsmiumPaxel");
        OsmiumPickaxe = init(new ItemMekanismPickaxe(MekanismTools.toolOSMIUM), "OsmiumPickaxe");
        OsmiumAxe = init(new ItemMekanismAxe(MekanismTools.toolOSMIUM), "OsmiumAxe");
        OsmiumShovel = init(new ItemMekanismShovel(MekanismTools.toolOSMIUM), "OsmiumShovel");
        OsmiumHoe = init(new ItemMekanismHoe(MekanismTools.toolOSMIUM), "OsmiumHoe");
        OsmiumSword = init(new ItemMekanismSword(MekanismTools.toolOSMIUM), "OsmiumSword");
        OsmiumHelmet = init(new ItemMekanismArmor(MekanismTools.armorOSMIUM, 0, EntityEquipmentSlot.HEAD),
              "OsmiumHelmet");
        OsmiumChestplate = init(new ItemMekanismArmor(MekanismTools.armorOSMIUM, 1, EntityEquipmentSlot.CHEST),
              "OsmiumChestplate");
        OsmiumLeggings = init(new ItemMekanismArmor(MekanismTools.armorOSMIUM, 2, EntityEquipmentSlot.LEGS),
              "OsmiumLeggings");
        OsmiumBoots = init(new ItemMekanismArmor(MekanismTools.armorOSMIUM, 3, EntityEquipmentSlot.FEET),
              "OsmiumBoots");
        ObsidianPaxel = init(new ItemMekanismPaxel(MekanismTools.toolOBSIDIAN2), "ObsidianPaxel");
        ObsidianPickaxe = init(new ItemMekanismPickaxe(MekanismTools.toolOBSIDIAN), "ObsidianPickaxe");
        ObsidianAxe = init(new ItemMekanismAxe(MekanismTools.toolOBSIDIAN), "ObsidianAxe");
        ObsidianShovel = init(new ItemMekanismShovel(MekanismTools.toolOBSIDIAN), "ObsidianShovel");
        ObsidianHoe = init(new ItemMekanismHoe(MekanismTools.toolOBSIDIAN), "ObsidianHoe");
        ObsidianSword = init(new ItemMekanismSword(MekanismTools.toolOBSIDIAN), "ObsidianSword");
        ObsidianHelmet = init(new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, 0, EntityEquipmentSlot.HEAD),
              "ObsidianHelmet");
        ObsidianChestplate = init(new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, 1, EntityEquipmentSlot.CHEST),
              "ObsidianChestplate");
        ObsidianLeggings = init(new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, 2, EntityEquipmentSlot.LEGS),
              "ObsidianLeggings");
        ObsidianBoots = init(new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, 3, EntityEquipmentSlot.FEET),
              "ObsidianBoots");
        LazuliPaxel = init(new ItemMekanismPaxel(MekanismTools.toolLAZULI2), "LapisLazuliPaxel");
        LazuliPickaxe = init(new ItemMekanismPickaxe(MekanismTools.toolLAZULI), "LapisLazuliPickaxe");
        LazuliAxe = init(new ItemMekanismAxe(MekanismTools.toolLAZULI), "LapisLazuliAxe");
        LazuliShovel = init(new ItemMekanismShovel(MekanismTools.toolLAZULI), "LapisLazuliShovel");
        LazuliHoe = init(new ItemMekanismHoe(MekanismTools.toolLAZULI), "LapisLazuliHoe");
        LazuliSword = init(new ItemMekanismSword(MekanismTools.toolLAZULI), "LapisLazuliSword");
        LazuliHelmet = init(new ItemMekanismArmor(MekanismTools.armorLAZULI, 0, EntityEquipmentSlot.HEAD),
              "LapisLazuliHelmet");
        LazuliChestplate = init(new ItemMekanismArmor(MekanismTools.armorLAZULI, 1, EntityEquipmentSlot.CHEST),
              "LapisLazuliChestplate");
        LazuliLeggings = init(new ItemMekanismArmor(MekanismTools.armorLAZULI, 2, EntityEquipmentSlot.LEGS),
              "LapisLazuliLeggings");
        LazuliBoots = init(new ItemMekanismArmor(MekanismTools.armorLAZULI, 3, EntityEquipmentSlot.FEET),
              "LapisLazuliBoots");
        SteelPaxel = init(new ItemMekanismPaxel(MekanismTools.toolSTEEL2), "SteelPaxel");
        SteelPickaxe = init(new ItemMekanismPickaxe(MekanismTools.toolSTEEL), "SteelPickaxe");
        SteelAxe = init(new ItemMekanismAxe(MekanismTools.toolSTEEL), "SteelAxe");
        SteelShovel = init(new ItemMekanismShovel(MekanismTools.toolSTEEL), "SteelShovel");
        SteelHoe = init(new ItemMekanismHoe(MekanismTools.toolSTEEL), "SteelHoe");
        SteelSword = init(new ItemMekanismSword(MekanismTools.toolSTEEL), "SteelSword");
        SteelHelmet = init(new ItemMekanismArmor(MekanismTools.armorSTEEL, 0, EntityEquipmentSlot.HEAD), "SteelHelmet");
        SteelChestplate = init(new ItemMekanismArmor(MekanismTools.armorSTEEL, 1, EntityEquipmentSlot.CHEST),
              "SteelChestplate");
        SteelLeggings = init(new ItemMekanismArmor(MekanismTools.armorSTEEL, 2, EntityEquipmentSlot.LEGS),
              "SteelLeggings");
        SteelBoots = init(new ItemMekanismArmor(MekanismTools.armorSTEEL, 3, EntityEquipmentSlot.FEET), "SteelBoots");
    }

    public static void setHarvestLevels() {
        setPaxelHarvest(BronzePaxel, MekanismTools.toolBRONZE2);
        BronzePickaxe.setHarvestLevel("pickaxe", MekanismTools.toolBRONZE.getHarvestLevel());
        BronzeAxe.setHarvestLevel("axe", MekanismTools.toolBRONZE.getHarvestLevel());
        BronzeShovel.setHarvestLevel("shovel", MekanismTools.toolBRONZE.getHarvestLevel());

        setPaxelHarvest(OsmiumPaxel, MekanismTools.toolOSMIUM2);
        OsmiumPickaxe.setHarvestLevel("pickaxe", MekanismTools.toolOSMIUM.getHarvestLevel());
        OsmiumAxe.setHarvestLevel("axe", MekanismTools.toolOSMIUM.getHarvestLevel());
        OsmiumShovel.setHarvestLevel("shovel", MekanismTools.toolOSMIUM.getHarvestLevel());

        setPaxelHarvest(ObsidianPaxel, MekanismTools.toolOBSIDIAN2);
        ObsidianPickaxe.setHarvestLevel("pickaxe", MekanismTools.toolOBSIDIAN.getHarvestLevel());
        ObsidianAxe.setHarvestLevel("axe", MekanismTools.toolOBSIDIAN.getHarvestLevel());
        ObsidianShovel.setHarvestLevel("shovel", MekanismTools.toolOBSIDIAN.getHarvestLevel());

        setPaxelHarvest(LazuliPaxel, MekanismTools.toolLAZULI2);
        LazuliPickaxe.setHarvestLevel("pickaxe", MekanismTools.toolLAZULI.getHarvestLevel());
        LazuliAxe.setHarvestLevel("axe", MekanismTools.toolLAZULI.getHarvestLevel());
        LazuliShovel.setHarvestLevel("shovel", MekanismTools.toolLAZULI.getHarvestLevel());

        setPaxelHarvest(GlowstonePaxel, MekanismTools.toolGLOWSTONE2);
        GlowstonePickaxe.setHarvestLevel("pickaxe", MekanismTools.toolGLOWSTONE.getHarvestLevel());
        GlowstoneAxe.setHarvestLevel("axe", MekanismTools.toolGLOWSTONE.getHarvestLevel());
        GlowstoneShovel.setHarvestLevel("shovel", MekanismTools.toolGLOWSTONE.getHarvestLevel());

        setPaxelHarvest(SteelPaxel, MekanismTools.toolSTEEL2);
        SteelPickaxe.setHarvestLevel("pickaxe", MekanismTools.toolSTEEL.getHarvestLevel());
        SteelAxe.setHarvestLevel("axe", MekanismTools.toolSTEEL.getHarvestLevel());
        SteelShovel.setHarvestLevel("shovel", MekanismTools.toolSTEEL.getHarvestLevel());

        setPaxelHarvest(WoodPaxel, ToolMaterial.WOOD);
        setPaxelHarvest(StonePaxel, ToolMaterial.STONE);
        setPaxelHarvest(IronPaxel, ToolMaterial.IRON);
        setPaxelHarvest(DiamondPaxel, ToolMaterial.DIAMOND);
        setPaxelHarvest(GoldPaxel, ToolMaterial.GOLD);
    }

    private static void setPaxelHarvest(Item item, ToolMaterial material) {
        item.setHarvestLevel("pickaxe", material.getHarvestLevel());
        item.setHarvestLevel("axe", material.getHarvestLevel());
        item.setHarvestLevel("shovel", material.getHarvestLevel());
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        //Base
        registry.register(WoodPaxel);
        registry.register(StonePaxel);
        registry.register(IronPaxel);
        registry.register(DiamondPaxel);
        registry.register(GoldPaxel);

        //Obsidian
        registry.register(ObsidianHelmet);
        registry.register(ObsidianChestplate);
        registry.register(ObsidianLeggings);
        registry.register(ObsidianBoots);
        registry.register(ObsidianPaxel);
        registry.register(ObsidianPickaxe);
        registry.register(ObsidianAxe);
        registry.register(ObsidianShovel);
        registry.register(ObsidianHoe);
        registry.register(ObsidianSword);

        //Lazuli
        registry.register(LazuliHelmet);
        registry.register(LazuliChestplate);
        registry.register(LazuliLeggings);
        registry.register(LazuliBoots);
        registry.register(LazuliPaxel);
        registry.register(LazuliPickaxe);
        registry.register(LazuliAxe);
        registry.register(LazuliShovel);
        registry.register(LazuliHoe);
        registry.register(LazuliSword);

        //Osmium
        registry.register(OsmiumHelmet);
        registry.register(OsmiumChestplate);
        registry.register(OsmiumLeggings);
        registry.register(OsmiumBoots);
        registry.register(OsmiumPaxel);
        registry.register(OsmiumPickaxe);
        registry.register(OsmiumAxe);
        registry.register(OsmiumShovel);
        registry.register(OsmiumHoe);
        registry.register(OsmiumSword);

        //Bronze
        registry.register(BronzeHelmet);
        registry.register(BronzeChestplate);
        registry.register(BronzeLeggings);
        registry.register(BronzeBoots);
        registry.register(BronzePaxel);
        registry.register(BronzePickaxe);
        registry.register(BronzeAxe);
        registry.register(BronzeShovel);
        registry.register(BronzeHoe);
        registry.register(BronzeSword);

        //Glowstone
        registry.register(GlowstonePaxel);
        registry.register(GlowstonePickaxe);
        registry.register(GlowstoneAxe);
        registry.register(GlowstoneShovel);
        registry.register(GlowstoneHoe);
        registry.register(GlowstoneSword);
        registry.register(GlowstoneHelmet);
        registry.register(GlowstoneChestplate);
        registry.register(GlowstoneLeggings);
        registry.register(GlowstoneBoots);

        //Steel
        registry.register(SteelPaxel);
        registry.register(SteelPickaxe);
        registry.register(SteelAxe);
        registry.register(SteelShovel);
        registry.register(SteelHoe);
        registry.register(SteelSword);
        registry.register(SteelHelmet);
        registry.register(SteelChestplate);
        registry.register(SteelLeggings);
        registry.register(SteelBoots);
    }

    public static Item init(Item item, String name) {
        return item.setTranslationKey(name).setRegistryName(new ResourceLocation(MekanismTools.MODID, name));
    }
}
