package mekanism.tools.common;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import mekanism.common.IModule;
import mekanism.common.Mekanism;
import mekanism.common.Version;
import mekanism.common.recipe.MekanismRecipe;
import mekanism.tools.item.ItemMekanismArmor;
import mekanism.tools.item.ItemMekanismAxe;
import mekanism.tools.item.ItemMekanismHoe;
import mekanism.tools.item.ItemMekanismPaxel;
import mekanism.tools.item.ItemMekanismPickaxe;
import mekanism.tools.item.ItemMekanismShovel;
import mekanism.tools.item.ItemMekanismSword;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import cpw.mods.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "MekanismTools", name = "MekanismTools", version = "8.0.0", dependencies = "required-after:Mekanism", guiFactory = "mekanism.tools.client.gui.ToolsGuiFactory")
public class MekanismTools implements IModule
{
	@SidedProxy(clientSide = "mekanism.tools.client.ToolsClientProxy", serverSide = "mekanism.tools.common.ToolsCommonProxy")
	public static ToolsCommonProxy proxy;
	
	@Instance("MekanismTools")
	public static MekanismTools instance;
	
	/** MekanismTools version number */
	public static Version versionNumber = new Version(8, 0, 0);

	//Enums: Tools
	public static ToolMaterial toolOBSIDIAN;
	public static ToolMaterial toolOBSIDIAN2;
	public static ToolMaterial toolLAZULI;
	public static ToolMaterial toolLAZULI2;
	public static ToolMaterial toolOSMIUM;
	public static ToolMaterial toolOSMIUM2;
	public static ToolMaterial toolBRONZE;
	public static ToolMaterial toolBRONZE2;
	public static ToolMaterial toolGLOWSTONE;
	public static ToolMaterial toolGLOWSTONE2;
	public static ToolMaterial toolSTEEL;
	public static ToolMaterial toolSTEEL2;

	//Enums: Armor
	public static ArmorMaterial armorOBSIDIAN;
	public static ArmorMaterial armorLAZULI;
	public static ArmorMaterial armorOSMIUM;
	public static ArmorMaterial armorBRONZE;
	public static ArmorMaterial armorGLOWSTONE;
	public static ArmorMaterial armorSTEEL;

	//Base Items
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
	
	//Redstone Items
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
	public static Item ObsidianHelmet;
	public static Item ObsidianChestplate;
	public static Item ObsidianLeggings;
	public static Item ObsidianBoots;
	public static Item ObsidianPaxel;
	public static Item ObsidianPickaxe;
	public static Item ObsidianAxe;
	public static Item ObsidianShovel;
	public static Item ObsidianHoe;
	public static Item ObsidianSword;
	
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
	
	//Tools Configuration
	public static double armorSpawnRate;

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		//Add this module to the core list
		Mekanism.modulesLoaded.add(this);
		
		//Register this class to the event bus for special mob spawning (mobs with Mekanism armor/tools)
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);

		//Load the proxy
		proxy.loadConfiguration();
		
		//Load this module
		addItems();
		addRecipes();
		
		//Finalization
		Mekanism.logger.info("Loaded MekanismTools module.");
	}
	
	public void addRecipes()
	{
		//Crafting Recipes
		//Base
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(WoodPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Items.wooden_axe, Character.valueOf('Y'), Items.wooden_pickaxe, Character.valueOf('Z'), Items.wooden_shovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(StonePaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Items.stone_axe, Character.valueOf('Y'), Items.stone_pickaxe, Character.valueOf('Z'), Items.stone_shovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(IronPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Items.iron_axe, Character.valueOf('Y'), Items.iron_pickaxe, Character.valueOf('Z'), Items.iron_shovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(DiamondPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Items.diamond_axe, Character.valueOf('Y'), Items.diamond_pickaxe, Character.valueOf('Z'), Items.diamond_shovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GoldPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Items.golden_axe, Character.valueOf('Y'), Items.golden_pickaxe, Character.valueOf('Z'), Items.golden_shovel, Character.valueOf('T'), Items.stick
		}));
		
		//Obsidian
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ObsidianHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ObsidianChestplate, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ObsidianLeggings, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ObsidianBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ObsidianPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), ObsidianAxe, Character.valueOf('Y'), ObsidianPickaxe, Character.valueOf('Z'), ObsidianShovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ObsidianPickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ObsidianAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ObsidianShovel, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ObsidianHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ObsidianSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Items.stick
		}));
		
		//Glowstone
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GlowstonePaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), GlowstoneAxe, Character.valueOf('Y'), GlowstonePickaxe, Character.valueOf('Z'), GlowstoneShovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GlowstonePickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GlowstoneAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GlowstoneShovel, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GlowstoneHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GlowstoneSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GlowstoneHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GlowstoneChestplate, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GlowstoneLeggings, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GlowstoneBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		
		//Lazuli
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(LazuliHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), new ItemStack(Items.dye, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(LazuliChestplate, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), new ItemStack(Items.dye, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(LazuliLeggings, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), new ItemStack(Items.dye, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(LazuliBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), new ItemStack(Items.dye, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(LazuliPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), LazuliAxe, Character.valueOf('Y'), LazuliPickaxe, Character.valueOf('Z'), LazuliShovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(LazuliPickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), new ItemStack(Items.dye, 1, 4), Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(LazuliAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), new ItemStack(Items.dye, 1, 4), Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(LazuliShovel, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), new ItemStack(Items.dye, 1, 4), Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(LazuliHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), new ItemStack(Items.dye, 1, 4), Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(LazuliSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), new ItemStack(Items.dye, 1, 4), Character.valueOf('T'), Items.stick
		}));
		
		//Osmium
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(OsmiumPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), OsmiumAxe, Character.valueOf('Y'), OsmiumPickaxe, Character.valueOf('Z'), OsmiumShovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(OsmiumPickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(OsmiumAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(OsmiumShovel, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(OsmiumHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(OsmiumSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(OsmiumHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(OsmiumChestplate, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(OsmiumLeggings, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(OsmiumBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotOsmium"
		}));
		
		//Bronze
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BronzePaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), BronzeAxe, Character.valueOf('Y'), BronzePickaxe, Character.valueOf('Z'), BronzeShovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BronzePickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BronzeAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BronzeShovel, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BronzeHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BronzeSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BronzeHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotBronze"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BronzeChestplate, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotBronze"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BronzeLeggings, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotBronze"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BronzeBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotBronze"
		}));
		
		//Steel
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(SteelPaxel, 1), new Object[] {
			"XYZ", " I ", " I ", Character.valueOf('X'), SteelAxe, Character.valueOf('Y'), SteelPickaxe, Character.valueOf('Z'), SteelShovel, Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(SteelPickaxe, 1), new Object[] {
			"XXX", " I ", " I ", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(SteelAxe, 1), new Object[] {
			"XX", "XI", " I", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(SteelShovel, 1), new Object[] {
			"X", "I", "I", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(SteelHoe, 1), new Object[] {
			"XX", " I", " I", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(SteelSword, 1), new Object[] {
			"X", "X", "I", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(SteelHelmet, 1), new Object[] {
			"***", "I I", Character.valueOf('*'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(SteelChestplate, 1), new Object[] {
			"I I", "*I*", "***", Character.valueOf('*'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(SteelLeggings, 1), new Object[] {
			"I*I", "* *", "* *", Character.valueOf('*'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(SteelBoots, 1), new Object[] {
			"I *", "* I", Character.valueOf('*'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
	}
	
	public void addItems()
	{
		//Tools
		toolOBSIDIAN = EnumHelper.addToolMaterial("OBSIDIAN"
				, Mekanism.configuration.get("tools.tool-balance.obsidian.regular", "harvestLevel", 3).getInt()
				, Mekanism.configuration.get("tools.tool-balance.obsidian.regular", "maxUses", 2500).getInt()
				, (float)Mekanism.configuration.get("tools.tool-balance.obsidian.regular", "efficiency", 20d).getDouble(0)
				, Mekanism.configuration.get("tools.tool-balance.obsidian.regular", "damage", 10).getInt()
				, Mekanism.configuration.get("tools.tool-balance.obsidian.regular", "enchantability", 40).getInt()
		);
		toolOBSIDIAN2 = EnumHelper.addToolMaterial("OBSIDIAN2"
				, Mekanism.configuration.get("tools.tool-balance.obsidian.paxel", "harvestLevel", 3).getInt()
				, Mekanism.configuration.get("tools.tool-balance.obsidian.paxel", "maxUses", 3000).getInt()
				, (float)Mekanism.configuration.get("tools.tool-balance.obsidian.paxel", "efficiency", 25d).getDouble(0)
				, Mekanism.configuration.get("tools.tool-balance.obsidian.paxel", "damage", 10).getInt()
				, Mekanism.configuration.get("tools.tool-balance.obsidian.paxel", "enchantability", 50).getInt()
		);
		toolLAZULI = EnumHelper.addToolMaterial("LAZULI"
				, Mekanism.configuration.get("tools.tool-balance.lapis.regular", "harvestLevel", 2).getInt()
				, Mekanism.configuration.get("tools.tool-balance.lapis.regular", "maxUses", 200).getInt()
				, (float)Mekanism.configuration.get("tools.tool-balance.lapis.regular", "efficiency", 5d).getDouble(0)
				, Mekanism.configuration.get("tools.tool-balance.lapis.regular", "damage", 2).getInt()
				, Mekanism.configuration.get("tools.tool-balance.lapis.regular", "enchantability", 8).getInt()
		);
		toolLAZULI2 = EnumHelper.addToolMaterial("LAZULI2"
				, Mekanism.configuration.get("tools.tool-balance.lapis.paxel", "harvestLevel", 2).getInt()
				, Mekanism.configuration.get("tools.tool-balance.lapis.paxel", "maxUses", 250).getInt()
				, (float)Mekanism.configuration.get("tools.tool-balance.lapis.paxel", "efficiency", 6d).getDouble(0)
				, Mekanism.configuration.get("tools.tool-balance.lapis.paxel", "damage", 4).getInt()
				, Mekanism.configuration.get("tools.tool-balance.lapis.paxel", "enchantability", 10).getInt()
		);
		toolOSMIUM = EnumHelper.addToolMaterial("OSMIUM"
				, Mekanism.configuration.get("tools.tool-balance.osmium.regular", "harvestLevel", 2).getInt()
				, Mekanism.configuration.get("tools.tool-balance.osmium.regular", "maxUses", 500).getInt()
				, (float)Mekanism.configuration.get("tools.tool-balance.osmium.regular", "efficiency", 10d).getDouble(0)
				, Mekanism.configuration.get("tools.tool-balance.osmium.regular", "damage", 4).getInt()
				, Mekanism.configuration.get("tools.tool-balance.osmium.regular", "enchantability", 12).getInt()
		);
		toolOSMIUM2 = EnumHelper.addToolMaterial("OSMIUM2"
				, Mekanism.configuration.get("tools.tool-balance.osmium.paxel", "harvestLevel", 3).getInt()
				, Mekanism.configuration.get("tools.tool-balance.osmium.paxel", "maxUses", 700).getInt()
				, (float)Mekanism.configuration.get("tools.tool-balance.osmium.paxel", "efficiency", 12d).getDouble(0)
				, Mekanism.configuration.get("tools.tool-balance.osmium.paxel", "damage", 5).getInt()
				, Mekanism.configuration.get("tools.tool-balance.osmium.paxel", "enchantability", 16).getInt()
		);
		toolBRONZE = EnumHelper.addToolMaterial("BRONZE"
				, Mekanism.configuration.get("tools.tool-balance.bronze.regular", "harvestLevel", 2).getInt()
				, Mekanism.configuration.get("tools.tool-balance.bronze.regular", "maxUses", 800).getInt()
				, (float)Mekanism.configuration.get("tools.tool-balance.bronze.regular", "efficiency", 14d).getDouble(0)
				, Mekanism.configuration.get("tools.tool-balance.bronze.regular", "damage", 6).getInt()
				, Mekanism.configuration.get("tools.tool-balance.bronze.regular", "enchantability", 10).getInt()
		);
		toolBRONZE2 = EnumHelper.addToolMaterial("BRONZE2"
				, Mekanism.configuration.get("tools.tool-balance.bronze.paxel", "harvestLevel", 3).getInt()
				, Mekanism.configuration.get("tools.tool-balance.bronze.paxel", "maxUses", 1100).getInt()
				, (float)Mekanism.configuration.get("tools.tool-balance.bronze.paxel", "efficiency", 16d).getDouble(0)
				, Mekanism.configuration.get("tools.tool-balance.bronze.paxel", "damage", 10).getInt()
				, Mekanism.configuration.get("tools.tool-balance.bronze.paxel", "enchantability", 14).getInt()
		);
		toolGLOWSTONE = EnumHelper.addToolMaterial("GLOWSTONE"
				, Mekanism.configuration.get("tools.tool-balance.glowstone.regular", "harvestLevel", 2).getInt()
				, Mekanism.configuration.get("tools.tool-balance.glowstone.regular", "maxUses", 300).getInt()
				, (float)Mekanism.configuration.get("tools.tool-balance.glowstone.regular", "efficiency", 14d).getDouble(0)
				, Mekanism.configuration.get("tools.tool-balance.glowstone.regular", "damage", 5).getInt()
				, Mekanism.configuration.get("tools.tool-balance.glowstone.regular", "enchantability", 18).getInt()
		);
		toolGLOWSTONE2 = EnumHelper.addToolMaterial("GLOWSTONE2"
				, Mekanism.configuration.get("tools.tool-balance.glowstone.paxel", "harvestLevel", 2).getInt()
				, Mekanism.configuration.get("tools.tool-balance.glowstone.paxel", "maxUses", 450).getInt()
				, (float)Mekanism.configuration.get("tools.tool-balance.glowstone.paxel", "efficiency", 18d).getDouble(0)
				, Mekanism.configuration.get("tools.tool-balance.glowstone.paxel", "damage", 5).getInt()
				, Mekanism.configuration.get("tools.tool-balance.glowstone.paxel", "enchantability", 22).getInt()
		);
		toolSTEEL = EnumHelper.addToolMaterial("STEEL"
				, Mekanism.configuration.get("tools.tool-balance.steel.regular", "harvestLevel", 3).getInt()
				, Mekanism.configuration.get("tools.tool-balance.steel.regular", "maxUses", 850).getInt()
				, (float)Mekanism.configuration.get("tools.tool-balance.steel.regular", "efficiency", 14d).getDouble(0)
				, Mekanism.configuration.get("tools.tool-balance.steel.regular", "damage", 4).getInt()
				, Mekanism.configuration.get("tools.tool-balance.steel.regular", "enchantability", 10).getInt()
		);
		toolSTEEL2 = EnumHelper.addToolMaterial("STEEL2"
				, Mekanism.configuration.get("tools.tool-balance.steel.paxel", "harvestLevel", 3).getInt()
				, Mekanism.configuration.get("tools.tool-balance.steel.paxel", "maxUses", 1250).getInt()
				, (float)Mekanism.configuration.get("tools.tool-balance.steel.paxel", "efficiency", 18d).getDouble(0)
				, Mekanism.configuration.get("tools.tool-balance.steel.paxel", "damage", 8).getInt()
				, Mekanism.configuration.get("tools.tool-balance.steel.paxel", "enchantability", 14).getInt()
		);

		//Armors
		armorOBSIDIAN = EnumHelper.addArmorMaterial("OBSIDIAN"
				, Mekanism.configuration.get("tools.armor-balance.obsidian", "durability", 50).getInt()
				, new int[]
				{
						Mekanism.configuration.get("tools.armor-balance.obsidian.protection", "head", 5).getInt()
						, Mekanism.configuration.get("tools.armor-balance.obsidian.protection", "chest", 12).getInt()
						, Mekanism.configuration.get("tools.armor-balance.obsidian.protection", "legs", 8).getInt()
						, Mekanism.configuration.get("tools.armor-balance.obsidian.protection", "feet", 5).getInt()
				}
				, Mekanism.configuration.get("tools.armor-balance.obsidian", "enchantability", 40).getInt()
		);
		armorLAZULI = EnumHelper.addArmorMaterial("LAZULI"
				, Mekanism.configuration.get("tools.armor-balance.lapis", "durability", 13).getInt()
				, new int[]
				{
						Mekanism.configuration.get("tools.armor-balance.lapis.protection", "head", 2).getInt()
						, Mekanism.configuration.get("tools.armor-balance.lapis.protection", "chest", 5).getInt()
						, Mekanism.configuration.get("tools.armor-balance.lapis.protection", "legs", 6).getInt()
						, Mekanism.configuration.get("tools.armor-balance.lapis.protection", "feet", 2).getInt()
				}
				, Mekanism.configuration.get("tools.armor-balance.lapis", "enchantability", 8).getInt()
		);
		armorOSMIUM = EnumHelper.addArmorMaterial("OSMIUM"
				, Mekanism.configuration.get("tools.armor-balance.osmium", "durability", 30).getInt()
				, new int[]
				{
						Mekanism.configuration.get("tools.armor-balance.osmium.protection", "head", 3).getInt()
						, Mekanism.configuration.get("tools.armor-balance.osmium.protection", "chest", 5).getInt()
						, Mekanism.configuration.get("tools.armor-balance.osmium.protection", "legs", 6).getInt()
						, Mekanism.configuration.get("tools.armor-balance.osmium.protection", "feet", 3).getInt()
				}
				, Mekanism.configuration.get("tools.armor-balance.osmium", "enchantability", 12).getInt()
		);
		armorBRONZE = EnumHelper.addArmorMaterial("BRONZE"
				, Mekanism.configuration.get("tools.armor-balance.bronze", "durability", 35).getInt()
				, new int[]
				{
						Mekanism.configuration.get("tools.armor-balance.bronze.protection", "head", 3).getInt()
						, Mekanism.configuration.get("tools.armor-balance.bronze.protection", "chest", 6).getInt()
						, Mekanism.configuration.get("tools.armor-balance.bronze.protection", "legs", 5).getInt()
						, Mekanism.configuration.get("tools.armor-balance.bronze.protection", "feet", 2).getInt()
				}
				, Mekanism.configuration.get("tools.armor-balance.bronze", "enchantability", 10).getInt()
		);
		armorGLOWSTONE = EnumHelper.addArmorMaterial("GLOWSTONE"
				, Mekanism.configuration.get("tools.armor-balance.glowstone", "durability", 18).getInt()
				, new int[]
				{
						Mekanism.configuration.get("tools.armor-balance.glowstone.protection", "head", 3).getInt()
						, Mekanism.configuration.get("tools.armor-balance.glowstone.protection", "chest", 7).getInt()
						, Mekanism.configuration.get("tools.armor-balance.glowstone.protection", "legs", 6).getInt()
						, Mekanism.configuration.get("tools.armor-balance.glowstone.protection", "feet", 3).getInt()
				}
				, Mekanism.configuration.get("tools.armor-balance.glowstone", "enchantability", 18).getInt()
		);
		armorSTEEL = EnumHelper.addArmorMaterial("STEEL"
				, Mekanism.configuration.get("tools.armor-balance.steel", "durability", 40).getInt()
				, new int[]
				{
						Mekanism.configuration.get("tools.armor-balance.steel.protection", "head", 3).getInt()
						, Mekanism.configuration.get("tools.armor-balance.steel.protection", "chest", 7).getInt()
						, Mekanism.configuration.get("tools.armor-balance.steel.protection", "legs", 6).getInt()
						, Mekanism.configuration.get("tools.armor-balance.steel.protection", "feet", 3).getInt()
				}
				, Mekanism.configuration.get("tools.armor-balance.steel", "enchantability", 10).getInt()
		);

		//Bronze
		BronzeHelmet = (new ItemMekanismArmor(armorBRONZE, Mekanism.proxy.getArmorIndex("bronze"), 0)).setUnlocalizedName("BronzeHelmet");
		BronzeChestplate = (new ItemMekanismArmor(armorBRONZE, Mekanism.proxy.getArmorIndex("bronze"), 1)).setUnlocalizedName("BronzeChestplate");
		BronzeLeggings = (new ItemMekanismArmor(armorBRONZE, Mekanism.proxy.getArmorIndex("bronze"), 2)).setUnlocalizedName("BronzeLeggings");
		BronzeBoots = (new ItemMekanismArmor(armorBRONZE, Mekanism.proxy.getArmorIndex("bronze"), 3)).setUnlocalizedName("BronzeBoots");
		BronzePaxel = new ItemMekanismPaxel(toolBRONZE2).setUnlocalizedName("BronzePaxel");
		BronzePickaxe = new ItemMekanismPickaxe(toolBRONZE).setUnlocalizedName("BronzePickaxe");
		BronzeAxe = new ItemMekanismAxe(toolBRONZE).setUnlocalizedName("BronzeAxe");
		BronzeShovel = new ItemMekanismShovel(toolBRONZE).setUnlocalizedName("BronzeShovel");
		BronzeHoe = new ItemMekanismHoe(toolBRONZE).setUnlocalizedName("BronzeHoe");
		BronzeSword = new ItemMekanismSword(toolBRONZE).setUnlocalizedName("BronzeSword");
		
		BronzePaxel.setHarvestLevel("paxel", toolBRONZE2.getHarvestLevel());
		BronzePickaxe.setHarvestLevel("pickaxe", toolBRONZE.getHarvestLevel());
		BronzeAxe.setHarvestLevel("axe", toolBRONZE.getHarvestLevel());
		BronzeShovel.setHarvestLevel("shovel", toolBRONZE.getHarvestLevel());
		
		//Osmium
		OsmiumHelmet = (new ItemMekanismArmor(armorOSMIUM, Mekanism.proxy.getArmorIndex("osmium"), 0)).setUnlocalizedName("OsmiumHelmet");
		OsmiumChestplate = (new ItemMekanismArmor(armorOSMIUM, Mekanism.proxy.getArmorIndex("osmium"), 1)).setUnlocalizedName("OsmiumChestplate");
		OsmiumLeggings = (new ItemMekanismArmor(armorOSMIUM, Mekanism.proxy.getArmorIndex("osmium"), 2)).setUnlocalizedName("OsmiumLeggings");
		OsmiumBoots = (new ItemMekanismArmor(armorOSMIUM, Mekanism.proxy.getArmorIndex("osmium"), 3)).setUnlocalizedName("OsmiumBoots");
		OsmiumPaxel = new ItemMekanismPaxel(toolOSMIUM2).setUnlocalizedName("OsmiumPaxel");
		OsmiumPickaxe = new ItemMekanismPickaxe(toolOSMIUM).setUnlocalizedName("OsmiumPickaxe");
		OsmiumAxe = new ItemMekanismAxe(toolOSMIUM).setUnlocalizedName("OsmiumAxe");
		OsmiumShovel = new ItemMekanismShovel(toolOSMIUM).setUnlocalizedName("OsmiumShovel");
		OsmiumHoe = new ItemMekanismHoe(toolOSMIUM).setUnlocalizedName("OsmiumHoe");
		OsmiumSword = new ItemMekanismSword(toolOSMIUM).setUnlocalizedName("OsmiumSword");
		
		OsmiumPaxel.setHarvestLevel("paxel", toolOSMIUM2.getHarvestLevel());
		OsmiumPickaxe.setHarvestLevel("pickaxe", toolOSMIUM.getHarvestLevel());
		OsmiumAxe.setHarvestLevel("axe", toolOSMIUM.getHarvestLevel());
		OsmiumShovel.setHarvestLevel("shovel", toolOSMIUM.getHarvestLevel());
		
		//Obsidian
		ObsidianHelmet = (new ItemMekanismArmor(armorOBSIDIAN, Mekanism.proxy.getArmorIndex("obsidian"), 0)).setUnlocalizedName("ObsidianHelmet");
		ObsidianChestplate = (new ItemMekanismArmor(armorOBSIDIAN, Mekanism.proxy.getArmorIndex("obsidian"), 1)).setUnlocalizedName("ObsidianChestplate");
		ObsidianLeggings = (new ItemMekanismArmor(armorOBSIDIAN, Mekanism.proxy.getArmorIndex("obsidian"), 2)).setUnlocalizedName("ObsidianLeggings");
		ObsidianBoots = (new ItemMekanismArmor(armorOBSIDIAN, Mekanism.proxy.getArmorIndex("obsidian"), 3)).setUnlocalizedName("ObsidianBoots");
		ObsidianPaxel = new ItemMekanismPaxel(toolOBSIDIAN2).setUnlocalizedName("ObsidianPaxel");
		ObsidianPickaxe = new ItemMekanismPickaxe(toolOBSIDIAN).setUnlocalizedName("ObsidianPickaxe");
		ObsidianAxe = new ItemMekanismAxe(toolOBSIDIAN).setUnlocalizedName("ObsidianAxe");
		ObsidianShovel = new ItemMekanismShovel(toolOBSIDIAN).setUnlocalizedName("ObsidianShovel");
		ObsidianHoe = new ItemMekanismHoe(toolOBSIDIAN).setUnlocalizedName("ObsidianHoe");
		ObsidianSword = new ItemMekanismSword(toolOBSIDIAN).setUnlocalizedName("ObsidianSword");
		
		ObsidianPaxel.setHarvestLevel("paxel", toolOBSIDIAN2.getHarvestLevel());
		ObsidianPickaxe.setHarvestLevel("pickaxe", toolOBSIDIAN.getHarvestLevel());
		ObsidianAxe.setHarvestLevel("axe", toolOBSIDIAN.getHarvestLevel());
		ObsidianShovel.setHarvestLevel("shovel", toolOBSIDIAN.getHarvestLevel());
		
		//Lazuli
		LazuliHelmet = (new ItemMekanismArmor(armorLAZULI, Mekanism.proxy.getArmorIndex("lazuli"), 0)).setUnlocalizedName("LazuliHelmet");
		LazuliChestplate = (new ItemMekanismArmor(armorLAZULI, Mekanism.proxy.getArmorIndex("lazuli"), 1)).setUnlocalizedName("LazuliChestplate");
		LazuliLeggings = (new ItemMekanismArmor(armorLAZULI, Mekanism.proxy.getArmorIndex("lazuli"), 2)).setUnlocalizedName("LazuliLeggings");
		LazuliBoots = (new ItemMekanismArmor(armorLAZULI, Mekanism.proxy.getArmorIndex("lazuli"), 3)).setUnlocalizedName("LazuliBoots");
		LazuliPaxel = new ItemMekanismPaxel(toolLAZULI2).setUnlocalizedName("LazuliPaxel");
		LazuliPickaxe = new ItemMekanismPickaxe(toolLAZULI).setUnlocalizedName("LazuliPickaxe");
		LazuliAxe = new ItemMekanismAxe(toolLAZULI).setUnlocalizedName("LazuliAxe");
		LazuliShovel = new ItemMekanismShovel(toolLAZULI).setUnlocalizedName("LazuliShovel");
		LazuliHoe = new ItemMekanismHoe(toolLAZULI).setUnlocalizedName("LazuliHoe");
		LazuliSword = new ItemMekanismSword(toolLAZULI).setUnlocalizedName("LazuliSword");
		
		LazuliPaxel.setHarvestLevel("paxel", toolLAZULI2.getHarvestLevel());
		LazuliPickaxe.setHarvestLevel("pickaxe", toolLAZULI.getHarvestLevel());
		LazuliAxe.setHarvestLevel("axe", toolLAZULI.getHarvestLevel());
		LazuliShovel.setHarvestLevel("shovel", toolLAZULI.getHarvestLevel());
		
		//Glowstone
		GlowstoneHelmet = new ItemMekanismArmor(armorGLOWSTONE, Mekanism.proxy.getArmorIndex("glowstone"), 0).setUnlocalizedName("GlowstoneHelmet");
		GlowstoneChestplate = new ItemMekanismArmor(armorGLOWSTONE, Mekanism.proxy.getArmorIndex("glowstone"), 1).setUnlocalizedName("GlowstoneChestplate");
		GlowstoneLeggings = new ItemMekanismArmor(armorGLOWSTONE, Mekanism.proxy.getArmorIndex("glowstone"), 2).setUnlocalizedName("GlowstoneLeggings");
		GlowstoneBoots = new ItemMekanismArmor(armorGLOWSTONE, Mekanism.proxy.getArmorIndex("glowstone"), 3).setUnlocalizedName("GlowstoneBoots");
		GlowstonePaxel = new ItemMekanismPaxel(toolGLOWSTONE2).setUnlocalizedName("GlowstonePaxel");
		GlowstonePickaxe = new ItemMekanismPickaxe(toolGLOWSTONE).setUnlocalizedName("GlowstonePickaxe");
		GlowstoneAxe = new ItemMekanismAxe(toolGLOWSTONE).setUnlocalizedName("GlowstoneAxe");
		GlowstoneShovel = new ItemMekanismShovel(toolGLOWSTONE).setUnlocalizedName("GlowstoneShovel");
		GlowstoneHoe = new ItemMekanismHoe(toolGLOWSTONE).setUnlocalizedName("GlowstoneHoe");
		GlowstoneSword = new ItemMekanismSword(toolGLOWSTONE).setUnlocalizedName("GlowstoneSword");
		
		GlowstonePaxel.setHarvestLevel("paxel", toolGLOWSTONE2.getHarvestLevel());
		GlowstonePickaxe.setHarvestLevel("pickaxe", toolGLOWSTONE.getHarvestLevel());
		GlowstoneAxe.setHarvestLevel("axe", toolGLOWSTONE.getHarvestLevel());
		GlowstoneShovel.setHarvestLevel("shovel", toolGLOWSTONE.getHarvestLevel());
		
		//Steel
		SteelHelmet = new ItemMekanismArmor(armorSTEEL, Mekanism.proxy.getArmorIndex("steel"), 0).setUnlocalizedName("SteelHelmet");
		SteelChestplate = new ItemMekanismArmor(armorSTEEL, Mekanism.proxy.getArmorIndex("steel"), 1).setUnlocalizedName("SteelChestplate");
		SteelLeggings = new ItemMekanismArmor(armorSTEEL, Mekanism.proxy.getArmorIndex("steel"), 2).setUnlocalizedName("SteelLeggings");
		SteelBoots = new ItemMekanismArmor(armorSTEEL, Mekanism.proxy.getArmorIndex("steel"), 3).setUnlocalizedName("SteelBoots");
		SteelPaxel = new ItemMekanismPaxel(toolSTEEL2).setUnlocalizedName("SteelPaxel");
		SteelPickaxe = new ItemMekanismPickaxe(toolSTEEL).setUnlocalizedName("SteelPickaxe");
		SteelAxe = new ItemMekanismAxe(toolSTEEL).setUnlocalizedName("SteelAxe");
		SteelShovel = new ItemMekanismShovel(toolSTEEL).setUnlocalizedName("SteelShovel");
		SteelHoe = new ItemMekanismHoe(toolSTEEL).setUnlocalizedName("SteelHoe");
		SteelSword = new ItemMekanismSword(toolSTEEL).setUnlocalizedName("SteelSword");
		
		SteelPaxel.setHarvestLevel("paxel", toolSTEEL2.getHarvestLevel());
		SteelPickaxe.setHarvestLevel("pickaxe", toolSTEEL.getHarvestLevel());
		SteelAxe.setHarvestLevel("axe", toolSTEEL.getHarvestLevel());
		SteelShovel.setHarvestLevel("shovel", toolSTEEL.getHarvestLevel());
		
		//Base Paxels
		WoodPaxel = new ItemMekanismPaxel(ToolMaterial.WOOD).setUnlocalizedName("WoodPaxel");
		StonePaxel = new ItemMekanismPaxel(ToolMaterial.STONE).setUnlocalizedName("StonePaxel");
		IronPaxel = new ItemMekanismPaxel(ToolMaterial.IRON).setUnlocalizedName("IronPaxel");
		DiamondPaxel = new ItemMekanismPaxel(ToolMaterial.EMERALD).setUnlocalizedName("DiamondPaxel");
		GoldPaxel = new ItemMekanismPaxel(ToolMaterial.GOLD).setUnlocalizedName("GoldPaxel");
		
		WoodPaxel.setHarvestLevel("paxel", ToolMaterial.WOOD.getHarvestLevel());
		StonePaxel.setHarvestLevel("paxel", ToolMaterial.STONE.getHarvestLevel());
		IronPaxel.setHarvestLevel("paxel", ToolMaterial.IRON.getHarvestLevel());
		DiamondPaxel.setHarvestLevel("paxel", ToolMaterial.EMERALD.getHarvestLevel());
		GoldPaxel.setHarvestLevel("paxel", ToolMaterial.GOLD.getHarvestLevel());
		
		Mekanism.configuration.save();
		
		//Registrations
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
		
		//Redstone
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
	
	@SubscribeEvent
	public void onLivingSpecialSpawn(LivingSpawnEvent event)
	{
		double chance = event.world.rand.nextDouble();
		int armorType = event.world.rand.nextInt(4);
		
		if(chance < armorSpawnRate)
		{
			if(event.entityLiving instanceof EntityZombie || event.entityLiving instanceof EntitySkeleton)
			{
				int sword = event.world.rand.nextInt(100);
				int helmet = event.world.rand.nextInt(100);
				int chestplate = event.world.rand.nextInt(100);
				int leggings = event.world.rand.nextInt(100);
				int boots = event.world.rand.nextInt(100);
				
				if(armorType == 0)
				{
					if(event.entityLiving instanceof EntityZombie && sword < 50) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(GlowstoneSword));
					if(helmet < 50) event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(GlowstoneHelmet));
					if(chestplate < 50) event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(GlowstoneChestplate));
					if(leggings < 50) event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(GlowstoneLeggings));
					if(boots < 50) event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(GlowstoneBoots));
				}
				else if(armorType == 1)
				{
					if(event.entityLiving instanceof EntityZombie && sword < 50) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(LazuliSword));
					if(helmet < 50) event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(LazuliHelmet));
					if(chestplate < 50) event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(LazuliChestplate));
					if(leggings < 50) event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(LazuliLeggings));
					if(boots < 50) event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(LazuliBoots));
				}
				else if(armorType == 2)
				{
					if(event.entityLiving instanceof EntityZombie && sword < 50) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(OsmiumSword));
					if(helmet < 50) event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(OsmiumHelmet));
					if(chestplate < 50) event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(OsmiumChestplate));
					if(leggings < 50) event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(OsmiumLeggings));
					if(boots < 50) event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(OsmiumBoots));
				}
				else if(armorType == 3)
				{
					if(event.entityLiving instanceof EntityZombie && sword < 50) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(SteelSword));
					if(helmet < 50) event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(SteelHelmet));
					if(chestplate < 50) event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(SteelChestplate));
					if(leggings < 50) event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(SteelLeggings));
					if(boots < 50) event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(SteelBoots));
				}
				else if(armorType == 4)
				{
					if(event.entityLiving instanceof EntityZombie && sword < 50) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(BronzeSword));
					if(helmet < 50) event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(BronzeHelmet));
					if(chestplate < 50) event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(BronzeChestplate));
					if(leggings < 50) event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(BronzeLeggings));
					if(boots < 50) event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(BronzeBoots));
				}
			}
		}
	}

	@Override
	public Version getVersion()
	{
		return versionNumber;
	}

	@Override
	public String getName() 
	{
		return "Tools";
	}

	@Override
	public void writeConfig(ByteBuf dataStream) throws IOException
	{
		dataStream.writeDouble(armorSpawnRate);
	}

	@Override
	public void readConfig(ByteBuf dataStream) throws IOException
	{
		armorSpawnRate = dataStream.readDouble();
	}

	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event)
	{
		if(event.modID.equals("MekanismTools"))
		{
			proxy.loadConfiguration();
		}
	}
}
