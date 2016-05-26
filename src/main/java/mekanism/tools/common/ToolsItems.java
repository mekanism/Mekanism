package mekanism.tools.common;

import mekanism.tools.item.ItemMekanismArmor;
import mekanism.tools.item.ItemMekanismAxe;
import mekanism.tools.item.ItemMekanismHoe;
import mekanism.tools.item.ItemMekanismPaxel;
import mekanism.tools.item.ItemMekanismPickaxe;
import mekanism.tools.item.ItemMekanismShovel;
import mekanism.tools.item.ItemMekanismSword;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

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
		GlowstoneHelmet = init(new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, 0), "GlowstoneHelmet");
		GlowstoneChestplate = init(new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, 1), "GlowstoneChestplate");
		GlowstoneLeggings = init(new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, 2), "GlowstoneLeggings");
		GlowstoneBoots = init(new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, 3), "GlowstoneBoots");
		BronzePaxel = init(new ItemMekanismPaxel(MekanismTools.toolBRONZE2), "BronzePaxel");
		BronzePickaxe = init(new ItemMekanismPickaxe(MekanismTools.toolBRONZE), "BronzePickaxe");
		BronzeAxe = init(new ItemMekanismAxe(MekanismTools.toolBRONZE), "BronzeAxe");
		BronzeShovel = init(new ItemMekanismShovel(MekanismTools.toolBRONZE), "BronzeShovel");
		BronzeHoe = init(new ItemMekanismHoe(MekanismTools.toolBRONZE), "BronzeHoe");
		BronzeSword = init(new ItemMekanismSword(MekanismTools.toolBRONZE), "BronzeSword");
		BronzeHelmet = init(new ItemMekanismArmor(MekanismTools.armorBRONZE, 0), "BronzeHelmet");
		BronzeChestplate = init(new ItemMekanismArmor(MekanismTools.armorBRONZE, 1), "BronzeChestplate");
		BronzeLeggings = init(new ItemMekanismArmor(MekanismTools.armorBRONZE, 2), "BronzeLeggings");
		BronzeBoots = init(new ItemMekanismArmor(MekanismTools.armorBRONZE, 3), "BronzeBoots");
		OsmiumPaxel = init(new ItemMekanismPaxel(MekanismTools.toolOSMIUM2), "OsmiumPaxel");
		OsmiumPickaxe = init(new ItemMekanismPickaxe(MekanismTools.toolOSMIUM), "OsmiumPickaxe");
		OsmiumAxe = init(new ItemMekanismAxe(MekanismTools.toolOSMIUM), "OsmiumAxe");
		OsmiumShovel = init(new ItemMekanismShovel(MekanismTools.toolOSMIUM), "OsmiumShovel");
		OsmiumHoe = init(new ItemMekanismHoe(MekanismTools.toolOSMIUM), "OsmiumHoe");
		OsmiumSword = init(new ItemMekanismSword(MekanismTools.toolOSMIUM), "OsmiumSword");
		OsmiumHelmet = init(new ItemMekanismArmor(MekanismTools.armorOSMIUM, 0), "OsmiumHelmet");
		OsmiumChestplate = init(new ItemMekanismArmor(MekanismTools.armorOSMIUM, 1), "OsmiumChestplate");
		OsmiumLeggings = init(new ItemMekanismArmor(MekanismTools.armorOSMIUM, 2), "OsmiumLeggings");
		OsmiumBoots = init(new ItemMekanismArmor(MekanismTools.armorOSMIUM, 3), "OsmiumBoots");
		ObsidianPaxel = init(new ItemMekanismPaxel(MekanismTools.toolOBSIDIAN2), "ObsidianPaxel");
		ObsidianPickaxe = init(new ItemMekanismPickaxe(MekanismTools.toolOBSIDIAN), "ObsidianPickaxe");
		ObsidianAxe = init(new ItemMekanismAxe(MekanismTools.toolOBSIDIAN), "ObsidianAxe");
		ObsidianShovel = init(new ItemMekanismShovel(MekanismTools.toolOBSIDIAN), "ObsidianShovel");
		ObsidianHoe = init(new ItemMekanismHoe(MekanismTools.toolOBSIDIAN), "ObsidianHoe");
		ObsidianSword = init(new ItemMekanismSword(MekanismTools.toolOBSIDIAN), "ObsidianSword");
		ObsidianHelmet = init(new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, 0), "ObsidianHelmet");
		ObsidianChestplate = init(new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, 1), "ObsidianChestplate");
		ObsidianLeggings = init(new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, 2), "ObsidianLeggings");
		ObsidianBoots = init(new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, 3), "ObsidianBoots");
		LazuliPaxel = init(new ItemMekanismPaxel(MekanismTools.toolLAZULI2), "LapisLazuliPaxel");
		LazuliPickaxe = init(new ItemMekanismPickaxe(MekanismTools.toolLAZULI), "LapisLazuliPickaxe");
		LazuliAxe = init(new ItemMekanismAxe(MekanismTools.toolLAZULI), "LapisLazuliAxe");
		LazuliShovel = init(new ItemMekanismShovel(MekanismTools.toolLAZULI), "LapisLazuliShovel");
		LazuliHoe = init(new ItemMekanismHoe(MekanismTools.toolLAZULI), "LapisLazuliHoe");
		LazuliSword = init(new ItemMekanismSword(MekanismTools.toolLAZULI), "LapisLazuliSword");
		LazuliHelmet = init(new ItemMekanismArmor(MekanismTools.armorLAZULI, 0), "LapisLazuliHelmet");
		LazuliChestplate = init(new ItemMekanismArmor(MekanismTools.armorLAZULI, 1), "LapisLazuliChestplate");
		LazuliLeggings = init(new ItemMekanismArmor(MekanismTools.armorLAZULI, 2), "LapisLazuliLeggings");
		LazuliBoots = init(new ItemMekanismArmor(MekanismTools.armorLAZULI, 3), "LapisLazuliBoots");
		SteelPaxel = init(new ItemMekanismPaxel(MekanismTools.toolSTEEL2), "SteelPaxel");
		SteelPickaxe = init(new ItemMekanismPickaxe(MekanismTools.toolSTEEL), "SteelPickaxe");
		SteelAxe = init(new ItemMekanismAxe(MekanismTools.toolSTEEL), "SteelAxe");
		SteelShovel = init(new ItemMekanismShovel(MekanismTools.toolSTEEL), "SteelShovel");
		SteelHoe = init(new ItemMekanismHoe(MekanismTools.toolSTEEL), "SteelHoe");
		SteelSword = init(new ItemMekanismSword(MekanismTools.toolSTEEL), "SteelSword");
		SteelHelmet = init(new ItemMekanismArmor(MekanismTools.armorSTEEL, 0), "SteelHelmet");
		SteelChestplate = init(new ItemMekanismArmor(MekanismTools.armorSTEEL, 1), "SteelChestplate");
		SteelLeggings = init(new ItemMekanismArmor(MekanismTools.armorSTEEL, 2), "SteelLeggings");
		SteelBoots = init(new ItemMekanismArmor(MekanismTools.armorSTEEL, 3), "SteelBoots");
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
		setPaxelHarvest(DiamondPaxel, ToolMaterial.DIAMOND);
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
		GameRegistry.register(WoodPaxel);
		GameRegistry.register(StonePaxel);
		GameRegistry.register(IronPaxel);
		GameRegistry.register(DiamondPaxel);
		GameRegistry.register(GoldPaxel);

		//Obsidian
		GameRegistry.register(ObsidianHelmet);
		GameRegistry.register(ObsidianChestplate);
		GameRegistry.register(ObsidianLeggings);
		GameRegistry.register(ObsidianBoots);
		GameRegistry.register(ObsidianPaxel);
		GameRegistry.register(ObsidianPickaxe);
		GameRegistry.register(ObsidianAxe);
		GameRegistry.register(ObsidianShovel);
		GameRegistry.register(ObsidianHoe);
		GameRegistry.register(ObsidianSword);

		//Lazuli
		GameRegistry.register(LazuliHelmet);
		GameRegistry.register(LazuliChestplate);
		GameRegistry.register(LazuliLeggings);
		GameRegistry.register(LazuliBoots);
		GameRegistry.register(LazuliPaxel);
		GameRegistry.register(LazuliPickaxe);
		GameRegistry.register(LazuliAxe);
		GameRegistry.register(LazuliShovel);
		GameRegistry.register(LazuliHoe);
		GameRegistry.register(LazuliSword);

		//Osmium
		GameRegistry.register(OsmiumHelmet);
		GameRegistry.register(OsmiumChestplate);
		GameRegistry.register(OsmiumLeggings);
		GameRegistry.register(OsmiumBoots);
		GameRegistry.register(OsmiumPaxel);
		GameRegistry.register(OsmiumPickaxe);
		GameRegistry.register(OsmiumAxe);
		GameRegistry.register(OsmiumShovel);
		GameRegistry.register(OsmiumHoe);
		GameRegistry.register(OsmiumSword);

		//Bronze
		GameRegistry.register(BronzeHelmet);
		GameRegistry.register(BronzeChestplate);
		GameRegistry.register(BronzeLeggings);
		GameRegistry.register(BronzeBoots);
		GameRegistry.register(BronzePaxel);
		GameRegistry.register(BronzePickaxe);
		GameRegistry.register(BronzeAxe);
		GameRegistry.register(BronzeShovel);
		GameRegistry.register(BronzeHoe);
		GameRegistry.register(BronzeSword);

		//Glowstone
		GameRegistry.register(GlowstonePaxel);
		GameRegistry.register(GlowstonePickaxe);
		GameRegistry.register(GlowstoneAxe);
		GameRegistry.register(GlowstoneShovel);
		GameRegistry.register(GlowstoneHoe);
		GameRegistry.register(GlowstoneSword);
		GameRegistry.register(GlowstoneHelmet);
		GameRegistry.register(GlowstoneChestplate);
		GameRegistry.register(GlowstoneLeggings);
		GameRegistry.register(GlowstoneBoots);

		//Steel
		GameRegistry.register(SteelPaxel);
		GameRegistry.register(SteelPickaxe);
		GameRegistry.register(SteelAxe);
		GameRegistry.register(SteelShovel);
		GameRegistry.register(SteelHoe);
		GameRegistry.register(SteelSword);
		GameRegistry.register(SteelHelmet);
		GameRegistry.register(SteelChestplate);
		GameRegistry.register(SteelLeggings);
		GameRegistry.register(SteelBoots);
		
		MekanismTools.proxy.registerItemRenders();
	}
	
	public static Item init(Item item, String name)
	{
		return item.setUnlocalizedName(name).setRegistryName("mekanismtools:" + name);
	}
}
