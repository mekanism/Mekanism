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
		WoodPaxel = new ItemMekanismPaxel(ToolMaterial.WOOD).setUnlocalizedName("WoodPaxel");
		StonePaxel = new ItemMekanismPaxel(ToolMaterial.STONE).setUnlocalizedName("StonePaxel");
		IronPaxel = new ItemMekanismPaxel(ToolMaterial.IRON).setUnlocalizedName("IronPaxel");
		DiamondPaxel = new ItemMekanismPaxel(ToolMaterial.DIAMOND).setUnlocalizedName("DiamondPaxel");
		GoldPaxel = new ItemMekanismPaxel(ToolMaterial.GOLD).setUnlocalizedName("GoldPaxel");
		GlowstonePaxel = new ItemMekanismPaxel(MekanismTools.toolGLOWSTONE2).setUnlocalizedName("GlowstonePaxel");
		GlowstonePickaxe = new ItemMekanismPickaxe(MekanismTools.toolGLOWSTONE).setUnlocalizedName("GlowstonePickaxe");
		GlowstoneAxe = new ItemMekanismAxe(MekanismTools.toolGLOWSTONE).setUnlocalizedName("GlowstoneAxe");
		GlowstoneShovel = new ItemMekanismShovel(MekanismTools.toolGLOWSTONE).setUnlocalizedName("GlowstoneShovel");
		GlowstoneHoe = new ItemMekanismHoe(MekanismTools.toolGLOWSTONE).setUnlocalizedName("GlowstoneHoe");
		GlowstoneSword = new ItemMekanismSword(MekanismTools.toolGLOWSTONE).setUnlocalizedName("GlowstoneSword");
		GlowstoneHelmet = new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, 0).setUnlocalizedName("GlowstoneHelmet");
		GlowstoneChestplate = new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, 1).setUnlocalizedName("GlowstoneChestplate");
		GlowstoneLeggings = new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, 2).setUnlocalizedName("GlowstoneLeggings");
		GlowstoneBoots = new ItemMekanismArmor(MekanismTools.armorGLOWSTONE, 3).setUnlocalizedName("GlowstoneBoots");
		BronzePaxel = new ItemMekanismPaxel(MekanismTools.toolBRONZE2).setUnlocalizedName("BronzePaxel");
		BronzePickaxe = new ItemMekanismPickaxe(MekanismTools.toolBRONZE).setUnlocalizedName("BronzePickaxe");
		BronzeAxe = new ItemMekanismAxe(MekanismTools.toolBRONZE).setUnlocalizedName("BronzeAxe");
		BronzeShovel = new ItemMekanismShovel(MekanismTools.toolBRONZE).setUnlocalizedName("BronzeShovel");
		BronzeHoe = new ItemMekanismHoe(MekanismTools.toolBRONZE).setUnlocalizedName("BronzeHoe");
		BronzeSword = new ItemMekanismSword(MekanismTools.toolBRONZE).setUnlocalizedName("BronzeSword");
		BronzeHelmet = (new ItemMekanismArmor(MekanismTools.armorBRONZE, 0)).setUnlocalizedName("BronzeHelmet");
		BronzeChestplate = (new ItemMekanismArmor(MekanismTools.armorBRONZE, 1)).setUnlocalizedName("BronzeChestplate");
		BronzeLeggings = (new ItemMekanismArmor(MekanismTools.armorBRONZE, 2)).setUnlocalizedName("BronzeLeggings");
		BronzeBoots = (new ItemMekanismArmor(MekanismTools.armorBRONZE, 3)).setUnlocalizedName("BronzeBoots");
		OsmiumPaxel = new ItemMekanismPaxel(MekanismTools.toolOSMIUM2).setUnlocalizedName("OsmiumPaxel");
		OsmiumPickaxe = new ItemMekanismPickaxe(MekanismTools.toolOSMIUM).setUnlocalizedName("OsmiumPickaxe");
		OsmiumAxe = new ItemMekanismAxe(MekanismTools.toolOSMIUM).setUnlocalizedName("OsmiumAxe");
		OsmiumShovel = new ItemMekanismShovel(MekanismTools.toolOSMIUM).setUnlocalizedName("OsmiumShovel");
		OsmiumHoe = new ItemMekanismHoe(MekanismTools.toolOSMIUM).setUnlocalizedName("OsmiumHoe");
		OsmiumSword = new ItemMekanismSword(MekanismTools.toolOSMIUM).setUnlocalizedName("OsmiumSword");
		OsmiumHelmet = (new ItemMekanismArmor(MekanismTools.armorOSMIUM, 0)).setUnlocalizedName("OsmiumHelmet");
		OsmiumChestplate = (new ItemMekanismArmor(MekanismTools.armorOSMIUM, 1)).setUnlocalizedName("OsmiumChestplate");
		OsmiumLeggings = (new ItemMekanismArmor(MekanismTools.armorOSMIUM, 2)).setUnlocalizedName("OsmiumLeggings");
		OsmiumBoots = (new ItemMekanismArmor(MekanismTools.armorOSMIUM, 3)).setUnlocalizedName("OsmiumBoots");
		ObsidianPaxel = new ItemMekanismPaxel(MekanismTools.toolOBSIDIAN2).setUnlocalizedName("ObsidianPaxel");
		ObsidianPickaxe = new ItemMekanismPickaxe(MekanismTools.toolOBSIDIAN).setUnlocalizedName("ObsidianPickaxe");
		ObsidianAxe = new ItemMekanismAxe(MekanismTools.toolOBSIDIAN).setUnlocalizedName("ObsidianAxe");
		ObsidianShovel = new ItemMekanismShovel(MekanismTools.toolOBSIDIAN).setUnlocalizedName("ObsidianShovel");
		ObsidianHoe = new ItemMekanismHoe(MekanismTools.toolOBSIDIAN).setUnlocalizedName("ObsidianHoe");
		ObsidianSword = new ItemMekanismSword(MekanismTools.toolOBSIDIAN).setUnlocalizedName("ObsidianSword");
		ObsidianHelmet = (new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, 0)).setUnlocalizedName("ObsidianHelmet");
		ObsidianChestplate = (new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, 1)).setUnlocalizedName("ObsidianChestplate");
		ObsidianLeggings = (new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, 2)).setUnlocalizedName("ObsidianLeggings");
		ObsidianBoots = (new ItemMekanismArmor(MekanismTools.armorOBSIDIAN, 3)).setUnlocalizedName("ObsidianBoots");
		LazuliPaxel = new ItemMekanismPaxel(MekanismTools.toolLAZULI2).setUnlocalizedName("LazuliPaxel");
		LazuliPickaxe = new ItemMekanismPickaxe(MekanismTools.toolLAZULI).setUnlocalizedName("LazuliPickaxe");
		LazuliAxe = new ItemMekanismAxe(MekanismTools.toolLAZULI).setUnlocalizedName("LazuliAxe");
		LazuliShovel = new ItemMekanismShovel(MekanismTools.toolLAZULI).setUnlocalizedName("LazuliShovel");
		LazuliHoe = new ItemMekanismHoe(MekanismTools.toolLAZULI).setUnlocalizedName("LazuliHoe");
		LazuliSword = new ItemMekanismSword(MekanismTools.toolLAZULI).setUnlocalizedName("LazuliSword");
		LazuliHelmet = (new ItemMekanismArmor(MekanismTools.armorLAZULI, 0)).setUnlocalizedName("LazuliHelmet");
		LazuliChestplate = (new ItemMekanismArmor(MekanismTools.armorLAZULI, 1)).setUnlocalizedName("LazuliChestplate");
		LazuliLeggings = (new ItemMekanismArmor(MekanismTools.armorLAZULI, 2)).setUnlocalizedName("LazuliLeggings");
		LazuliBoots = (new ItemMekanismArmor(MekanismTools.armorLAZULI, 3)).setUnlocalizedName("LazuliBoots");
		SteelPaxel = new ItemMekanismPaxel(MekanismTools.toolSTEEL2).setUnlocalizedName("SteelPaxel");
		SteelPickaxe = new ItemMekanismPickaxe(MekanismTools.toolSTEEL).setUnlocalizedName("SteelPickaxe");
		SteelAxe = new ItemMekanismAxe(MekanismTools.toolSTEEL).setUnlocalizedName("SteelAxe");
		SteelShovel = new ItemMekanismShovel(MekanismTools.toolSTEEL).setUnlocalizedName("SteelShovel");
		SteelHoe = new ItemMekanismHoe(MekanismTools.toolSTEEL).setUnlocalizedName("SteelHoe");
		SteelSword = new ItemMekanismSword(MekanismTools.toolSTEEL).setUnlocalizedName("SteelSword");
		SteelHelmet = new ItemMekanismArmor(MekanismTools.armorSTEEL, 0).setUnlocalizedName("SteelHelmet");
		SteelChestplate = new ItemMekanismArmor(MekanismTools.armorSTEEL, 1).setUnlocalizedName("SteelChestplate");
		SteelLeggings = new ItemMekanismArmor(MekanismTools.armorSTEEL, 2).setUnlocalizedName("SteelLeggings");
		SteelBoots = new ItemMekanismArmor(MekanismTools.armorSTEEL, 3).setUnlocalizedName("SteelBoots");
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
}
