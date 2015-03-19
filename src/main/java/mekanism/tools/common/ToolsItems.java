package mekanism.tools.common;

import mekanism.common.Mekanism;
import mekanism.tools.item.ItemMekanismArmor;
import mekanism.tools.item.ItemMekanismAxe;
import mekanism.tools.item.ItemMekanismHoe;
import mekanism.tools.item.ItemMekanismPaxel;
import mekanism.tools.item.ItemMekanismPickaxe;
import mekanism.tools.item.ItemMekanismShovel;
import mekanism.tools.item.ItemMekanismSword;

import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("MekanismTools")
public class ToolsItems
{
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

	public static void initializeItems()
	{
		WoodPaxel = new ItemMekanismPaxel(ToolMaterial.WOOD).setUnlocalizedName("WoodPaxel");
		StonePaxel = new ItemMekanismPaxel(ToolMaterial.STONE).setUnlocalizedName("StonePaxel");
		IronPaxel = new ItemMekanismPaxel(ToolMaterial.IRON).setUnlocalizedName("IronPaxel");
		DiamondPaxel = new ItemMekanismPaxel(ToolMaterial.EMERALD).setUnlocalizedName("DiamondPaxel");
		GoldPaxel = new ItemMekanismPaxel(ToolMaterial.GOLD).setUnlocalizedName("GoldPaxel");
		GlowstonePaxel = new ItemMekanismPaxel(MekanismTools.toolGLOWSTONE2).setUnlocalizedName("GlowstonePaxel");
		GlowstonePickaxe = new ItemMekanismPickaxe(MekanismTools.toolGLOWSTONE).setUnlocalizedName("GlowstonePickaxe");
		GlowstoneAxe = new ItemMekanismAxe(MekanismTools.toolGLOWSTONE).setUnlocalizedName("GlowstoneAxe");
		GlowstoneShovel = new ItemMekanismShovel(MekanismTools.toolGLOWSTONE).setUnlocalizedName("GlowstoneShovel");
		GlowstoneHoe = new ItemMekanismHoe(MekanismTools.toolGLOWSTONE).setUnlocalizedName("GlowstoneHoe");
		GlowstoneSword = new ItemMekanismSword(MekanismTools.toolGLOWSTONE).setUnlocalizedName("GlowstoneSword");
		GlowstoneHelmet = new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, Mekanism.proxy.getArmorIndex("glowstone"), 0).setUnlocalizedName("GlowstoneHelmet");
		GlowstoneChestplate = new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, Mekanism.proxy.getArmorIndex("glowstone"), 1).setUnlocalizedName("GlowstoneChestplate");
		GlowstoneLeggings = new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, Mekanism.proxy.getArmorIndex("glowstone"), 2).setUnlocalizedName("GlowstoneLeggings");
		GlowstoneBoots = new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, Mekanism.proxy.getArmorIndex("glowstone"), 3).setUnlocalizedName("GlowstoneBoots");
		BronzePaxel = new ItemMekanismPaxel(MekanismTools.toolBRONZE2).setUnlocalizedName("BronzePaxel");
		BronzePickaxe = new ItemMekanismPickaxe(MekanismTools.toolBRONZE).setUnlocalizedName("BronzePickaxe");
		BronzeAxe = new ItemMekanismAxe(MekanismTools.toolBRONZE).setUnlocalizedName("BronzeAxe");
		BronzeShovel = new ItemMekanismShovel(MekanismTools.toolBRONZE).setUnlocalizedName("BronzeShovel");
		BronzeHoe = new ItemMekanismHoe(MekanismTools.toolBRONZE).setUnlocalizedName("BronzeHoe");
		BronzeSword = new ItemMekanismSword(MekanismTools.toolBRONZE).setUnlocalizedName("BronzeSword");
		BronzeHelmet = (new ItemMekanismArmor(MekanismTools.armorBRONZE, Mekanism.proxy.getArmorIndex("bronze"), 0)).setUnlocalizedName("BronzeHelmet");
		BronzeChestplate = (new ItemMekanismArmor(MekanismTools.armorBRONZE, Mekanism.proxy.getArmorIndex("bronze"), 1)).setUnlocalizedName("BronzeChestplate");
		BronzeLeggings = (new ItemMekanismArmor(MekanismTools.armorBRONZE, Mekanism.proxy.getArmorIndex("bronze"), 2)).setUnlocalizedName("BronzeLeggings");
		BronzeBoots = (new ItemMekanismArmor(MekanismTools.armorBRONZE, Mekanism.proxy.getArmorIndex("bronze"), 3)).setUnlocalizedName("BronzeBoots");
		OsmiumPaxel = new ItemMekanismPaxel(MekanismTools.toolOSMIUM2).setUnlocalizedName("OsmiumPaxel");
		OsmiumPickaxe = new ItemMekanismPickaxe(MekanismTools.toolOSMIUM).setUnlocalizedName("OsmiumPickaxe");
		OsmiumAxe = new ItemMekanismAxe(MekanismTools.toolOSMIUM).setUnlocalizedName("OsmiumAxe");
		OsmiumShovel = new ItemMekanismShovel(MekanismTools.toolOSMIUM).setUnlocalizedName("OsmiumShovel");
		OsmiumHoe = new ItemMekanismHoe(MekanismTools.toolOSMIUM).setUnlocalizedName("OsmiumHoe");
		OsmiumSword = new ItemMekanismSword(MekanismTools.toolOSMIUM).setUnlocalizedName("OsmiumSword");
		OsmiumHelmet = (new ItemMekanismArmor(MekanismTools.armorOSMIUM, Mekanism.proxy.getArmorIndex("osmium"), 0)).setUnlocalizedName("OsmiumHelmet");
		OsmiumChestplate = (new ItemMekanismArmor(MekanismTools.armorOSMIUM, Mekanism.proxy.getArmorIndex("osmium"), 1)).setUnlocalizedName("OsmiumChestplate");
		OsmiumLeggings = (new ItemMekanismArmor(MekanismTools.armorOSMIUM, Mekanism.proxy.getArmorIndex("osmium"), 2)).setUnlocalizedName("OsmiumLeggings");
		OsmiumBoots = (new ItemMekanismArmor(MekanismTools.armorOSMIUM, Mekanism.proxy.getArmorIndex("osmium"), 3)).setUnlocalizedName("OsmiumBoots");
		ObsidianPaxel = new ItemMekanismPaxel(MekanismTools.toolOBSIDIAN2).setUnlocalizedName("ObsidianPaxel");
		ObsidianPickaxe = new ItemMekanismPickaxe(MekanismTools.toolOBSIDIAN).setUnlocalizedName("ObsidianPickaxe");
		ObsidianAxe = new ItemMekanismAxe(MekanismTools.toolOBSIDIAN).setUnlocalizedName("ObsidianAxe");
		ObsidianShovel = new ItemMekanismShovel(MekanismTools.toolOBSIDIAN).setUnlocalizedName("ObsidianShovel");
		ObsidianHoe = new ItemMekanismHoe(MekanismTools.toolOBSIDIAN).setUnlocalizedName("ObsidianHoe");
		ObsidianSword = new ItemMekanismSword(MekanismTools.toolOBSIDIAN).setUnlocalizedName("ObsidianSword");
		ObsidianHelmet = (new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, Mekanism.proxy.getArmorIndex("obsidian"), 0)).setUnlocalizedName("ObsidianHelmet");
		ObsidianChestplate = (new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, Mekanism.proxy.getArmorIndex("obsidian"), 1)).setUnlocalizedName("ObsidianChestplate");
		ObsidianLeggings = (new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, Mekanism.proxy.getArmorIndex("obsidian"), 2)).setUnlocalizedName("ObsidianLeggings");
		ObsidianBoots = (new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, Mekanism.proxy.getArmorIndex("obsidian"), 3)).setUnlocalizedName("ObsidianBoots");
		LazuliPaxel = new ItemMekanismPaxel(MekanismTools.toolLAZULI2).setUnlocalizedName("LazuliPaxel");
		LazuliPickaxe = new ItemMekanismPickaxe(MekanismTools.toolLAZULI).setUnlocalizedName("LazuliPickaxe");
		LazuliAxe = new ItemMekanismAxe(MekanismTools.toolLAZULI).setUnlocalizedName("LazuliAxe");
		LazuliShovel = new ItemMekanismShovel(MekanismTools.toolLAZULI).setUnlocalizedName("LazuliShovel");
		LazuliHoe = new ItemMekanismHoe(MekanismTools.toolLAZULI).setUnlocalizedName("LazuliHoe");
		LazuliSword = new ItemMekanismSword(MekanismTools.toolLAZULI).setUnlocalizedName("LazuliSword");
		LazuliHelmet = (new ItemMekanismArmor(MekanismTools.armorLAZULI, Mekanism.proxy.getArmorIndex("lazuli"), 0)).setUnlocalizedName("LazuliHelmet");
		LazuliChestplate = (new ItemMekanismArmor(MekanismTools.armorLAZULI, Mekanism.proxy.getArmorIndex("lazuli"), 1)).setUnlocalizedName("LazuliChestplate");
		LazuliLeggings = (new ItemMekanismArmor(MekanismTools.armorLAZULI, Mekanism.proxy.getArmorIndex("lazuli"), 2)).setUnlocalizedName("LazuliLeggings");
		LazuliBoots = (new ItemMekanismArmor(MekanismTools.armorLAZULI, Mekanism.proxy.getArmorIndex("lazuli"), 3)).setUnlocalizedName("LazuliBoots");
		SteelPaxel = new ItemMekanismPaxel(MekanismTools.toolSTEEL2).setUnlocalizedName("SteelPaxel");
		SteelPickaxe = new ItemMekanismPickaxe(MekanismTools.toolSTEEL).setUnlocalizedName("SteelPickaxe");
		SteelAxe = new ItemMekanismAxe(MekanismTools.toolSTEEL).setUnlocalizedName("SteelAxe");
		SteelShovel = new ItemMekanismShovel(MekanismTools.toolSTEEL).setUnlocalizedName("SteelShovel");
		SteelHoe = new ItemMekanismHoe(MekanismTools.toolSTEEL).setUnlocalizedName("SteelHoe");
		SteelSword = new ItemMekanismSword(MekanismTools.toolSTEEL).setUnlocalizedName("SteelSword");
		SteelHelmet = new ItemMekanismArmor(MekanismTools.armorSTEEL, Mekanism.proxy.getArmorIndex("steel"), 0).setUnlocalizedName("SteelHelmet");
		SteelChestplate = new ItemMekanismArmor(MekanismTools.armorSTEEL, Mekanism.proxy.getArmorIndex("steel"), 1).setUnlocalizedName("SteelChestplate");
		SteelLeggings = new ItemMekanismArmor(MekanismTools.armorSTEEL, Mekanism.proxy.getArmorIndex("steel"), 2).setUnlocalizedName("SteelLeggings");
		SteelBoots = new ItemMekanismArmor(MekanismTools.armorSTEEL, Mekanism.proxy.getArmorIndex("steel"), 3).setUnlocalizedName("SteelBoots");
	}

	public static void setHarvestLevels()
	{
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
		setPaxelHarvest(DiamondPaxel, ToolMaterial.EMERALD);
		setPaxelHarvest(GoldPaxel, ToolMaterial.GOLD);
	}
	
	private static void setPaxelHarvest(Item item, ToolMaterial material)
	{
		item.setHarvestLevel("pickaxe", material.getHarvestLevel());
		item.setHarvestLevel("axe", material.getHarvestLevel());
		item.setHarvestLevel("shovel", material.getHarvestLevel());
	}

	public static void register()
	{
		//Base
		GameRegistry.registerItem(WoodPaxel, "WoodPaxel");
		GameRegistry.registerItem(StonePaxel, "StonePaxel");
		GameRegistry.registerItem(IronPaxel, "IronPaxel");
		GameRegistry.registerItem(DiamondPaxel, "DiamondPaxel");
		GameRegistry.registerItem(GoldPaxel, "GoldPaxel");

		//Obsidian
		GameRegistry.registerItem(ObsidianHelmet, "ObsidianHelmet");
		GameRegistry.registerItem(ObsidianChestplate, "ObsidianChestplate");
		GameRegistry.registerItem(ObsidianLeggings, "ObsidianLeggings");
		GameRegistry.registerItem(ObsidianBoots, "ObsidianBoots");
		GameRegistry.registerItem(ObsidianPaxel, "ObsidianPaxel");
		GameRegistry.registerItem(ObsidianPickaxe, "ObsidianPickaxe");
		GameRegistry.registerItem(ObsidianAxe, "ObsidianAxe");
		GameRegistry.registerItem(ObsidianShovel, "ObsidianShovel");
		GameRegistry.registerItem(ObsidianHoe, "ObsidianHoe");
		GameRegistry.registerItem(ObsidianSword, "ObsidianSword");

		//Lazuli
		GameRegistry.registerItem(LazuliHelmet, "LapisLazuliHelmet");
		GameRegistry.registerItem(LazuliChestplate, "LapisLazuliChestplate");
		GameRegistry.registerItem(LazuliLeggings, "LapisLazuliLeggings");
		GameRegistry.registerItem(LazuliBoots, "LapisLazuliBoots");
		GameRegistry.registerItem(LazuliPaxel, "LapisLazuliPaxel");
		GameRegistry.registerItem(LazuliPickaxe, "LapisLazuliPickaxe");
		GameRegistry.registerItem(LazuliAxe, "LapisLazuliAxe");
		GameRegistry.registerItem(LazuliShovel, "LapisLazuliShovel");
		GameRegistry.registerItem(LazuliHoe, "LapisLazuliHoe");
		GameRegistry.registerItem(LazuliSword, "LapisLazuliSword");

		//Osmium
		GameRegistry.registerItem(OsmiumHelmet, "OsmiumHelmet");
		GameRegistry.registerItem(OsmiumChestplate, "OsmiumChestplate");
		GameRegistry.registerItem(OsmiumLeggings, "OsmiumLeggings");
		GameRegistry.registerItem(OsmiumBoots, "OsmiumBoots");
		GameRegistry.registerItem(OsmiumPaxel, "OsmiumPaxel");
		GameRegistry.registerItem(OsmiumPickaxe, "OsmiumPickaxe");
		GameRegistry.registerItem(OsmiumAxe, "OsmiumAxe");
		GameRegistry.registerItem(OsmiumShovel, "OsmiumShovel");
		GameRegistry.registerItem(OsmiumHoe, "OsmiumHoe");
		GameRegistry.registerItem(OsmiumSword, "OsmiumSword");

		//Bronze
		GameRegistry.registerItem(BronzeHelmet, "BronzeHelmet");
		GameRegistry.registerItem(BronzeChestplate, "BronzeChestplate");
		GameRegistry.registerItem(BronzeLeggings, "BronzeLeggings");
		GameRegistry.registerItem(BronzeBoots, "BronzeBoots");
		GameRegistry.registerItem(BronzePaxel, "BronzePaxel");
		GameRegistry.registerItem(BronzePickaxe, "BronzePickaxe");
		GameRegistry.registerItem(BronzeAxe, "BronzeAxe");
		GameRegistry.registerItem(BronzeShovel, "BronzeShovel");
		GameRegistry.registerItem(BronzeHoe, "BronzeHoe");
		GameRegistry.registerItem(BronzeSword, "BronzeSword");

		//Glowstone
		GameRegistry.registerItem(GlowstonePaxel, "GlowstonePaxel");
		GameRegistry.registerItem(GlowstonePickaxe, "GlowstonePickaxe");
		GameRegistry.registerItem(GlowstoneAxe, "GlowstoneAxe");
		GameRegistry.registerItem(GlowstoneShovel, "GlowstoneShovel");
		GameRegistry.registerItem(GlowstoneHoe, "GlowstoneHoe");
		GameRegistry.registerItem(GlowstoneSword, "GlowstoneSword");
		GameRegistry.registerItem(GlowstoneHelmet, "GlowstoneHelmet");
		GameRegistry.registerItem(GlowstoneChestplate, "GlowstoneChestplate");
		GameRegistry.registerItem(GlowstoneLeggings, "GlowstoneLeggings");
		GameRegistry.registerItem(GlowstoneBoots, "GlowstoneBoots");

		//Steel
		GameRegistry.registerItem(SteelPaxel, "SteelPaxel");
		GameRegistry.registerItem(SteelPickaxe, "SteelPickaxe");
		GameRegistry.registerItem(SteelAxe, "SteelAxe");
		GameRegistry.registerItem(SteelShovel, "SteelShovel");
		GameRegistry.registerItem(SteelHoe, "SteelHoe");
		GameRegistry.registerItem(SteelSword, "SteelSword");
		GameRegistry.registerItem(SteelHelmet, "SteelHelmet");
		GameRegistry.registerItem(SteelChestplate, "SteelChestplate");
		GameRegistry.registerItem(SteelLeggings, "SteelLeggings");
		GameRegistry.registerItem(SteelBoots, "SteelBoots");
	}
}
