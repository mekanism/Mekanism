package mekanism.tools.common;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import mekanism.api.MekanismConfig.tools;
import mekanism.common.IModule;
import mekanism.common.Mekanism;
import mekanism.common.Version;
import mekanism.common.recipe.MekanismRecipe;

import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
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
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.WoodPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Items.wooden_axe, Character.valueOf('Y'), Items.wooden_pickaxe, Character.valueOf('Z'), Items.wooden_shovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.StonePaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Items.stone_axe, Character.valueOf('Y'), Items.stone_pickaxe, Character.valueOf('Z'), Items.stone_shovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.IronPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Items.iron_axe, Character.valueOf('Y'), Items.iron_pickaxe, Character.valueOf('Z'), Items.iron_shovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.DiamondPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Items.diamond_axe, Character.valueOf('Y'), Items.diamond_pickaxe, Character.valueOf('Z'), Items.diamond_shovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.GoldPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Items.golden_axe, Character.valueOf('Y'), Items.golden_pickaxe, Character.valueOf('Z'), Items.golden_shovel, Character.valueOf('T'), Items.stick
		}));
		
		//Obsidian
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.ObsidianHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.ObsidianChestplate, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.ObsidianLeggings, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.ObsidianBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.ObsidianPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), MekanismToolsItems.ObsidianAxe, Character.valueOf('Y'), MekanismToolsItems.ObsidianPickaxe, Character.valueOf('Z'), MekanismToolsItems.ObsidianShovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.ObsidianPickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.ObsidianAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.ObsidianShovel, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.ObsidianHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.ObsidianSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Items.stick
		}));
		
		//Glowstone
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.GlowstonePaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), MekanismToolsItems.GlowstoneAxe, Character.valueOf('Y'), MekanismToolsItems.GlowstonePickaxe, Character.valueOf('Z'), MekanismToolsItems.GlowstoneShovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.GlowstonePickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.GlowstoneAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.GlowstoneShovel, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.GlowstoneHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.GlowstoneSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.GlowstoneHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.GlowstoneChestplate, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.GlowstoneLeggings, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.GlowstoneBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		
		//Lazuli
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.LazuliHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), new ItemStack(Items.dye, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.LazuliChestplate, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), new ItemStack(Items.dye, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.LazuliLeggings, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), new ItemStack(Items.dye, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.LazuliBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), new ItemStack(Items.dye, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.LazuliPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), MekanismToolsItems.LazuliAxe, Character.valueOf('Y'), MekanismToolsItems.LazuliPickaxe, Character.valueOf('Z'), MekanismToolsItems.LazuliShovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.LazuliPickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), new ItemStack(Items.dye, 1, 4), Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.LazuliAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), new ItemStack(Items.dye, 1, 4), Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.LazuliShovel, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), new ItemStack(Items.dye, 1, 4), Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.LazuliHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), new ItemStack(Items.dye, 1, 4), Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.LazuliSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), new ItemStack(Items.dye, 1, 4), Character.valueOf('T'), Items.stick
		}));
		
		//Osmium
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.OsmiumPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), MekanismToolsItems.OsmiumAxe, Character.valueOf('Y'), MekanismToolsItems.OsmiumPickaxe, Character.valueOf('Z'), MekanismToolsItems.OsmiumShovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.OsmiumPickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.OsmiumAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.OsmiumShovel, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.OsmiumHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.OsmiumSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.OsmiumHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.OsmiumChestplate, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.OsmiumLeggings, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.OsmiumBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotOsmium"
		}));
		
		//Bronze
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.BronzePaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), MekanismToolsItems.BronzeAxe, Character.valueOf('Y'), MekanismToolsItems.BronzePickaxe, Character.valueOf('Z'), MekanismToolsItems.BronzeShovel, Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.BronzePickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.BronzeAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.BronzeShovel, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.BronzeHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.BronzeSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.BronzeHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotBronze"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.BronzeChestplate, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotBronze"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.BronzeLeggings, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotBronze"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.BronzeBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotBronze"
		}));
		
		//Steel
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.SteelPaxel, 1), new Object[] {
			"XYZ", " I ", " I ", Character.valueOf('X'), MekanismToolsItems.SteelAxe, Character.valueOf('Y'), MekanismToolsItems.SteelPickaxe, Character.valueOf('Z'), MekanismToolsItems.SteelShovel, Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.SteelPickaxe, 1), new Object[] {
			"XXX", " I ", " I ", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.SteelAxe, 1), new Object[] {
			"XX", "XI", " I", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.SteelShovel, 1), new Object[] {
			"X", "I", "I", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.SteelHoe, 1), new Object[] {
			"XX", " I", " I", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.SteelSword, 1), new Object[] {
			"X", "X", "I", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.SteelHelmet, 1), new Object[] {
			"***", "I I", Character.valueOf('*'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.SteelChestplate, 1), new Object[] {
			"I I", "*I*", "***", Character.valueOf('*'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.SteelLeggings, 1), new Object[] {
			"I*I", "* *", "* *", Character.valueOf('*'), "ingotSteel", Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismToolsItems.SteelBoots, 1), new Object[] {
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
		Mekanism.configuration.save();

		MekanismToolsItems.initializeItems();
		MekanismToolsItems.setHarvestLevels();

		//Registrations
		//Base
		GameRegistry.registerItem(MekanismToolsItems.WoodPaxel, "WoodPaxel");
		GameRegistry.registerItem(MekanismToolsItems.StonePaxel, "StonePaxel");
		GameRegistry.registerItem(MekanismToolsItems.IronPaxel, "IronPaxel");
		GameRegistry.registerItem(MekanismToolsItems.DiamondPaxel, "DiamondPaxel");
		GameRegistry.registerItem(MekanismToolsItems.GoldPaxel, "GoldPaxel");
		
		//Obsidian
		GameRegistry.registerItem(MekanismToolsItems.ObsidianHelmet, "ObsidianHelmet");
		GameRegistry.registerItem(MekanismToolsItems.ObsidianChestplate, "ObsidianChestplate");
		GameRegistry.registerItem(MekanismToolsItems.ObsidianLeggings, "ObsidianLeggings");
		GameRegistry.registerItem(MekanismToolsItems.ObsidianBoots, "ObsidianBoots");
		GameRegistry.registerItem(MekanismToolsItems.ObsidianPaxel, "ObsidianPaxel");
		GameRegistry.registerItem(MekanismToolsItems.ObsidianPickaxe, "ObsidianPickaxe");
		GameRegistry.registerItem(MekanismToolsItems.ObsidianAxe, "ObsidianAxe");
		GameRegistry.registerItem(MekanismToolsItems.ObsidianShovel, "ObsidianShovel");
		GameRegistry.registerItem(MekanismToolsItems.ObsidianHoe, "ObsidianHoe");
		GameRegistry.registerItem(MekanismToolsItems.ObsidianSword, "ObsidianSword");
		
		//Lazuli
		GameRegistry.registerItem(MekanismToolsItems.LazuliHelmet, "LapisLazuliHelmet");
		GameRegistry.registerItem(MekanismToolsItems.LazuliChestplate, "LapisLazuliChestplate");
		GameRegistry.registerItem(MekanismToolsItems.LazuliLeggings, "LapisLazuliLeggings");
		GameRegistry.registerItem(MekanismToolsItems.LazuliBoots, "LapisLazuliBoots");
		GameRegistry.registerItem(MekanismToolsItems.LazuliPaxel, "LapisLazuliPaxel");
		GameRegistry.registerItem(MekanismToolsItems.LazuliPickaxe, "LapisLazuliPickaxe");
		GameRegistry.registerItem(MekanismToolsItems.LazuliAxe, "LapisLazuliAxe");
		GameRegistry.registerItem(MekanismToolsItems.LazuliShovel, "LapisLazuliShovel");
		GameRegistry.registerItem(MekanismToolsItems.LazuliHoe, "LapisLazuliHoe");
		GameRegistry.registerItem(MekanismToolsItems.LazuliSword, "LapisLazuliSword");
		
		//Osmium
		GameRegistry.registerItem(MekanismToolsItems.OsmiumHelmet, "OsmiumHelmet");
		GameRegistry.registerItem(MekanismToolsItems.OsmiumChestplate, "OsmiumChestplate");
		GameRegistry.registerItem(MekanismToolsItems.OsmiumLeggings, "OsmiumLeggings");
		GameRegistry.registerItem(MekanismToolsItems.OsmiumBoots, "OsmiumBoots");
		GameRegistry.registerItem(MekanismToolsItems.OsmiumPaxel, "OsmiumPaxel");
		GameRegistry.registerItem(MekanismToolsItems.OsmiumPickaxe, "OsmiumPickaxe");
		GameRegistry.registerItem(MekanismToolsItems.OsmiumAxe, "OsmiumAxe");
		GameRegistry.registerItem(MekanismToolsItems.OsmiumShovel, "OsmiumShovel");
		GameRegistry.registerItem(MekanismToolsItems.OsmiumHoe, "OsmiumHoe");
		GameRegistry.registerItem(MekanismToolsItems.OsmiumSword, "OsmiumSword");
		
		//Redstone
		GameRegistry.registerItem(MekanismToolsItems.BronzeHelmet, "BronzeHelmet");
		GameRegistry.registerItem(MekanismToolsItems.BronzeChestplate, "BronzeChestplate");
		GameRegistry.registerItem(MekanismToolsItems.BronzeLeggings, "BronzeLeggings");
		GameRegistry.registerItem(MekanismToolsItems.BronzeBoots, "BronzeBoots");
		GameRegistry.registerItem(MekanismToolsItems.BronzePaxel, "BronzePaxel");
		GameRegistry.registerItem(MekanismToolsItems.BronzePickaxe, "BronzePickaxe");
		GameRegistry.registerItem(MekanismToolsItems.BronzeAxe, "BronzeAxe");
		GameRegistry.registerItem(MekanismToolsItems.BronzeShovel, "BronzeShovel");
		GameRegistry.registerItem(MekanismToolsItems.BronzeHoe, "BronzeHoe");
		GameRegistry.registerItem(MekanismToolsItems.BronzeSword, "BronzeSword");
		
		//Glowstone
		GameRegistry.registerItem(MekanismToolsItems.GlowstonePaxel, "GlowstonePaxel");
		GameRegistry.registerItem(MekanismToolsItems.GlowstonePickaxe, "GlowstonePickaxe");
		GameRegistry.registerItem(MekanismToolsItems.GlowstoneAxe, "GlowstoneAxe");
		GameRegistry.registerItem(MekanismToolsItems.GlowstoneShovel, "GlowstoneShovel");
		GameRegistry.registerItem(MekanismToolsItems.GlowstoneHoe, "GlowstoneHoe");
		GameRegistry.registerItem(MekanismToolsItems.GlowstoneSword, "GlowstoneSword");
		GameRegistry.registerItem(MekanismToolsItems.GlowstoneHelmet, "GlowstoneHelmet");
		GameRegistry.registerItem(MekanismToolsItems.GlowstoneChestplate, "GlowstoneChestplate");
		GameRegistry.registerItem(MekanismToolsItems.GlowstoneLeggings, "GlowstoneLeggings");
		GameRegistry.registerItem(MekanismToolsItems.GlowstoneBoots, "GlowstoneBoots");
		
		//Steel
		GameRegistry.registerItem(MekanismToolsItems.SteelPaxel, "SteelPaxel");
		GameRegistry.registerItem(MekanismToolsItems.SteelPickaxe, "SteelPickaxe");
		GameRegistry.registerItem(MekanismToolsItems.SteelAxe, "SteelAxe");
		GameRegistry.registerItem(MekanismToolsItems.SteelShovel, "SteelShovel");
		GameRegistry.registerItem(MekanismToolsItems.SteelHoe, "SteelHoe");
		GameRegistry.registerItem(MekanismToolsItems.SteelSword, "SteelSword");
		GameRegistry.registerItem(MekanismToolsItems.SteelHelmet, "SteelHelmet");
		GameRegistry.registerItem(MekanismToolsItems.SteelChestplate, "SteelChestplate");
		GameRegistry.registerItem(MekanismToolsItems.SteelLeggings, "SteelLeggings");
		GameRegistry.registerItem(MekanismToolsItems.SteelBoots, "SteelBoots");
	}

	@SubscribeEvent
	public void onLivingSpecialSpawn(LivingSpawnEvent event)
	{
		double chance = event.world.rand.nextDouble();
		int armorType = event.world.rand.nextInt(4);
		
		if(chance < tools.armorSpawnRate)
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
					if(event.entityLiving instanceof EntityZombie && sword < 50) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(MekanismToolsItems.GlowstoneSword));
					if(helmet < 50) event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(MekanismToolsItems.GlowstoneHelmet));
					if(chestplate < 50) event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(MekanismToolsItems.GlowstoneChestplate));
					if(leggings < 50) event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(MekanismToolsItems.GlowstoneLeggings));
					if(boots < 50) event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(MekanismToolsItems.GlowstoneBoots));
				}
				else if(armorType == 1)
				{
					if(event.entityLiving instanceof EntityZombie && sword < 50) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(MekanismToolsItems.LazuliSword));
					if(helmet < 50) event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(MekanismToolsItems.LazuliHelmet));
					if(chestplate < 50) event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(MekanismToolsItems.LazuliChestplate));
					if(leggings < 50) event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(MekanismToolsItems.LazuliLeggings));
					if(boots < 50) event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(MekanismToolsItems.LazuliBoots));
				}
				else if(armorType == 2)
				{
					if(event.entityLiving instanceof EntityZombie && sword < 50) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(MekanismToolsItems.OsmiumSword));
					if(helmet < 50) event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(MekanismToolsItems.OsmiumHelmet));
					if(chestplate < 50) event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(MekanismToolsItems.OsmiumChestplate));
					if(leggings < 50) event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(MekanismToolsItems.OsmiumLeggings));
					if(boots < 50) event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(MekanismToolsItems.OsmiumBoots));
				}
				else if(armorType == 3)
				{
					if(event.entityLiving instanceof EntityZombie && sword < 50) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(MekanismToolsItems.SteelSword));
					if(helmet < 50) event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(MekanismToolsItems.SteelHelmet));
					if(chestplate < 50) event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(MekanismToolsItems.SteelChestplate));
					if(leggings < 50) event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(MekanismToolsItems.SteelLeggings));
					if(boots < 50) event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(MekanismToolsItems.SteelBoots));
				}
				else if(armorType == 4)
				{
					if(event.entityLiving instanceof EntityZombie && sword < 50) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(MekanismToolsItems.BronzeSword));
					if(helmet < 50) event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(MekanismToolsItems.BronzeHelmet));
					if(chestplate < 50) event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(MekanismToolsItems.BronzeChestplate));
					if(leggings < 50) event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(MekanismToolsItems.BronzeLeggings));
					if(boots < 50) event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(MekanismToolsItems.BronzeBoots));
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
		dataStream.writeDouble(tools.armorSpawnRate);
	}

	@Override
	public void readConfig(ByteBuf dataStream) throws IOException
	{
		tools.armorSpawnRate = dataStream.readDouble();
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
