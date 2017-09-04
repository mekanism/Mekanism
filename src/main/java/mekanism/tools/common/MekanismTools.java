package mekanism.tools.common;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import mekanism.common.Mekanism;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import mekanism.common.config.MekanismConfig.tools;
import mekanism.common.recipe.ShapedMekanismRecipe;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "mekanismtools", name = "MekanismTools", version = "9.3.5", dependencies = "required-after:mekanism", guiFactory = "mekanism.tools.client.gui.ToolsGuiFactory")
public class MekanismTools implements IModule
{
	@SidedProxy(clientSide = "mekanism.tools.client.ToolsClientProxy", serverSide = "mekanism.tools.common.ToolsCommonProxy")
	public static ToolsCommonProxy proxy;
	
	@Instance("mekanismtools")
	public static MekanismTools instance;
	
	/** MekanismTools version number */
	public static Version versionNumber = new Version(9, 3, 5);

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
	
	public static Map<ToolMaterial, Float> AXE_DAMAGE = new HashMap<ToolMaterial, Float>();
	public static Map<ToolMaterial, Float> AXE_SPEED = new HashMap<ToolMaterial, Float>();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		addItems();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		//Add this module to the core list
		Mekanism.modulesLoaded.add(this);
		
		//Register this class to the event bus for special mob spawning (mobs with Mekanism armor/tools)
		MinecraftForge.EVENT_BUS.register(this);

		//Load the proxy
		proxy.loadConfiguration();
		
		//Load this module
		addRecipes();
		
		//Finalization
		Mekanism.logger.info("Loaded MekanismTools module.");
	}
	
	public void addRecipes()
	{
		//Crafting Recipes
		//Base
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.WoodPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Items.WOODEN_AXE, Character.valueOf('Y'), Items.WOODEN_PICKAXE, Character.valueOf('Z'), Items.WOODEN_SHOVEL, Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.StonePaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Items.STONE_AXE, Character.valueOf('Y'), Items.STONE_PICKAXE, Character.valueOf('Z'), Items.STONE_SHOVEL, Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.IronPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Items.IRON_AXE, Character.valueOf('Y'), Items.IRON_PICKAXE, Character.valueOf('Z'), Items.IRON_SHOVEL, Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.DiamondPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Items.DIAMOND_AXE, Character.valueOf('Y'), Items.DIAMOND_PICKAXE, Character.valueOf('Z'), Items.DIAMOND_SHOVEL, Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.GoldPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Items.GOLDEN_AXE, Character.valueOf('Y'), Items.GOLDEN_PICKAXE, Character.valueOf('Z'), Items.GOLDEN_SHOVEL, Character.valueOf('T'), Items.STICK
		}));
		
		//Obsidian
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.ObsidianHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.ObsidianChestplate, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.ObsidianLeggings, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.ObsidianBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.ObsidianPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), ToolsItems.ObsidianAxe, Character.valueOf('Y'), ToolsItems.ObsidianPickaxe, Character.valueOf('Z'), ToolsItems.ObsidianShovel, Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.ObsidianPickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.ObsidianAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.ObsidianShovel, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.ObsidianHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.ObsidianSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Items.STICK
		}));
		
		//Glowstone
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.GlowstonePaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), ToolsItems.GlowstoneAxe, Character.valueOf('Y'), ToolsItems.GlowstonePickaxe, Character.valueOf('Z'), ToolsItems.GlowstoneShovel, Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.GlowstonePickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.GlowstoneAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.GlowstoneShovel, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.GlowstoneHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.GlowstoneSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.GlowstoneHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.GlowstoneChestplate, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.GlowstoneLeggings, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.GlowstoneBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		
		//Lazuli
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.LazuliHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), new ItemStack(Items.DYE, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.LazuliChestplate, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), new ItemStack(Items.DYE, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.LazuliLeggings, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), new ItemStack(Items.DYE, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.LazuliBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), new ItemStack(Items.DYE, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.LazuliPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), ToolsItems.LazuliAxe, Character.valueOf('Y'), ToolsItems.LazuliPickaxe, Character.valueOf('Z'), ToolsItems.LazuliShovel, Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.LazuliPickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), new ItemStack(Items.DYE, 1, 4), Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.LazuliAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), new ItemStack(Items.DYE, 1, 4), Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.LazuliShovel, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), new ItemStack(Items.DYE, 1, 4), Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.LazuliHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), new ItemStack(Items.DYE, 1, 4), Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.LazuliSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), new ItemStack(Items.DYE, 1, 4), Character.valueOf('T'), Items.STICK
		}));
		
		//Osmium
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.OsmiumPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), ToolsItems.OsmiumAxe, Character.valueOf('Y'), ToolsItems.OsmiumPickaxe, Character.valueOf('Z'), ToolsItems.OsmiumShovel, Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.OsmiumPickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.OsmiumAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.OsmiumShovel, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.OsmiumHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.OsmiumSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.OsmiumHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.OsmiumChestplate, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.OsmiumLeggings, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.OsmiumBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotOsmium"
		}));
		
		//Bronze
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.BronzePaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), ToolsItems.BronzeAxe, Character.valueOf('Y'), ToolsItems.BronzePickaxe, Character.valueOf('Z'), ToolsItems.BronzeShovel, Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.BronzePickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.BronzeAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.BronzeShovel, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.BronzeHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.BronzeSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Items.STICK
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.BronzeHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotBronze"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.BronzeChestplate, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotBronze"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.BronzeLeggings, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotBronze"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.BronzeBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotBronze"
		}));
		
		//Steel
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.SteelPaxel, 1), new Object[] {
			"XYZ", " I ", " I ", Character.valueOf('X'), ToolsItems.SteelAxe, Character.valueOf('Y'), ToolsItems.SteelPickaxe, Character.valueOf('Z'), ToolsItems.SteelShovel, Character.valueOf('I'), Items.IRON_INGOT
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.SteelPickaxe, 1), new Object[] {
			"XXX", " I ", " I ", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Items.IRON_INGOT
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.SteelAxe, 1), new Object[] {
			"XX", "XI", " I", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Items.IRON_INGOT
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.SteelShovel, 1), new Object[] {
			"X", "I", "I", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Items.IRON_INGOT
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.SteelHoe, 1), new Object[] {
			"XX", " I", " I", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Items.IRON_INGOT
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.SteelSword, 1), new Object[] {
			"X", "X", "I", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Items.IRON_INGOT
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.SteelHelmet, 1), new Object[] {
			"***", "I I", Character.valueOf('*'), "ingotSteel", Character.valueOf('I'), Items.IRON_INGOT
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.SteelChestplate, 1), new Object[] {
			"I I", "*I*", "***", Character.valueOf('*'), "ingotSteel", Character.valueOf('I'), Items.IRON_INGOT
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.SteelLeggings, 1), new Object[] {
			"I*I", "* *", "* *", Character.valueOf('*'), "ingotSteel", Character.valueOf('I'), Items.IRON_INGOT
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(ToolsItems.SteelBoots, 1), new Object[] {
			"I *", "* I", Character.valueOf('*'), "ingotSteel", Character.valueOf('I'), Items.IRON_INGOT
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
		
		AXE_DAMAGE.put(toolOBSIDIAN, (float)Mekanism.configuration.get("tools.tool-balance.obsidian.regular", "axeAttackDamage", 12D).getDouble());
		AXE_SPEED.put(toolOBSIDIAN, (float)Mekanism.configuration.get("tools.tool-balance.obsidian.regular", "axeAttackSpeed", -2D).getDouble());

		AXE_DAMAGE.put(toolLAZULI, (float)Mekanism.configuration.get("tools.tool-balance.lazuli.regular", "axeAttackDamage", 8D).getDouble());
		AXE_SPEED.put(toolLAZULI, (float)Mekanism.configuration.get("tools.tool-balance.lazuli.regular", "axeAttackSpeed", -3.1D).getDouble());
		
		AXE_DAMAGE.put(toolOSMIUM, (float)Mekanism.configuration.get("tools.tool-balance.osmium.regular", "axeAttackDamage", 8D).getDouble());
		AXE_SPEED.put(toolOSMIUM, (float)Mekanism.configuration.get("tools.tool-balance.osmium.regular", "axeAttackSpeed", -3D).getDouble());
		
		AXE_DAMAGE.put(toolBRONZE, (float)Mekanism.configuration.get("tools.tool-balance.bronze.regular", "axeAttackDamage", 8D).getDouble());
		AXE_SPEED.put(toolBRONZE, (float)Mekanism.configuration.get("tools.tool-balance.bronze.regular", "axeAttackSpeed", -3.1D).getDouble());
		
		AXE_DAMAGE.put(toolGLOWSTONE, (float)Mekanism.configuration.get("tools.tool-balance.glowstone.regular", "axeAttackDamage", 8D).getDouble());
		AXE_SPEED.put(toolGLOWSTONE, (float)Mekanism.configuration.get("tools.tool-balance.glowstone.regular", "axeAttackSpeed", -3.1D).getDouble());
		
		AXE_DAMAGE.put(toolSTEEL, (float)Mekanism.configuration.get("tools.tool-balance.steel.regular", "axeAttackDamage", 8D).getDouble());
		AXE_SPEED.put(toolSTEEL, (float)Mekanism.configuration.get("tools.tool-balance.steel.regular", "axeAttackSpeed", -3D).getDouble());
		
		//Armors
		armorOBSIDIAN = EnumHelper.addArmorMaterial("OBSIDIAN", "TODO"
				, Mekanism.configuration.get("tools.armor-balance.obsidian", "durability", 50).getInt()
				, new int[]
				{
						Mekanism.configuration.get("tools.armor-balance.obsidian.protection", "feet", 5).getInt(),
						Mekanism.configuration.get("tools.armor-balance.obsidian.protection", "legs", 8).getInt(),
						Mekanism.configuration.get("tools.armor-balance.obsidian.protection", "chest", 12).getInt(),
						Mekanism.configuration.get("tools.armor-balance.obsidian.protection", "head", 5).getInt()
				}
				, Mekanism.configuration.get("tools.armor-balance.obsidian", "enchantability", 40).getInt()
				, SoundEvents.ITEM_ARMOR_EQUIP_IRON
				, (float)Mekanism.configuration.get("tools.armor-balance.obsidian", "toughness", 4D).getDouble()
		);
		armorLAZULI = EnumHelper.addArmorMaterial("LAZULI", "TODO"
				, Mekanism.configuration.get("tools.armor-balance.lapis", "durability", 13).getInt()
				, new int[]
				{
						Mekanism.configuration.get("tools.armor-balance.lapis.protection", "feet", 2).getInt(),
						Mekanism.configuration.get("tools.armor-balance.lapis.protection", "legs", 6).getInt(),
						Mekanism.configuration.get("tools.armor-balance.lapis.protection", "chest", 5).getInt(),
						Mekanism.configuration.get("tools.armor-balance.lapis.protection", "head", 2).getInt()
				}
				, Mekanism.configuration.get("tools.armor-balance.lapis", "enchantability", 8).getInt()
				, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND
				, (float)Mekanism.configuration.get("tools.armor-balance.lapis", "toughness", 0D).getDouble()
		);
		armorOSMIUM = EnumHelper.addArmorMaterial("OSMIUM", "TODO"
				, Mekanism.configuration.get("tools.armor-balance.osmium", "durability", 30).getInt()
				, new int[]
				{
						Mekanism.configuration.get("tools.armor-balance.osmium.protection", "feet", 3).getInt(),
						Mekanism.configuration.get("tools.armor-balance.osmium.protection", "legs", 6).getInt(),
						Mekanism.configuration.get("tools.armor-balance.osmium.protection", "chest", 5).getInt(),
						Mekanism.configuration.get("tools.armor-balance.osmium.protection", "head", 3).getInt()
				}
				, Mekanism.configuration.get("tools.armor-balance.osmium", "enchantability", 12).getInt()
				, SoundEvents.ITEM_ARMOR_EQUIP_IRON
				, Mekanism.configuration.get("tools.armor-balance.osmium", "toughness", 1).getInt()
		);
		armorBRONZE = EnumHelper.addArmorMaterial("BRONZE", "TODO"
				, Mekanism.configuration.get("tools.armor-balance.bronze", "durability", 35).getInt()
				, new int[]
				{
						Mekanism.configuration.get("tools.armor-balance.bronze.protection", "feet", 2).getInt(),
						Mekanism.configuration.get("tools.armor-balance.bronze.protection", "legs", 5).getInt(),
						Mekanism.configuration.get("tools.armor-balance.bronze.protection", "chest", 6).getInt(),
						Mekanism.configuration.get("tools.armor-balance.bronze.protection", "head", 3).getInt()
				}
				, Mekanism.configuration.get("tools.armor-balance.bronze", "enchantability", 10).getInt()
				, SoundEvents.ITEM_ARMOR_EQUIP_IRON
				, (float)Mekanism.configuration.get("tools.armor-balance.bronze", "toughness", 0D).getDouble()
		);
		armorGLOWSTONE = EnumHelper.addArmorMaterial("GLOWSTONE", "TODO"
				, Mekanism.configuration.get("tools.armor-balance.glowstone", "durability", 18).getInt()
				, new int[]
				{
						Mekanism.configuration.get("tools.armor-balance.glowstone.protection", "feet", 3).getInt(),
						Mekanism.configuration.get("tools.armor-balance.glowstone.protection", "legs", 6).getInt(),
						Mekanism.configuration.get("tools.armor-balance.glowstone.protection", "chest", 7).getInt(),
						Mekanism.configuration.get("tools.armor-balance.glowstone.protection", "head", 3).getInt()
				}
				, Mekanism.configuration.get("tools.armor-balance.glowstone", "enchantability", 18).getInt()
				, SoundEvents.ITEM_ARMOR_EQUIP_IRON
				, (float)Mekanism.configuration.get("tools.armor-balance.glowstone", "toughness", 0D).getDouble()
		);
		armorSTEEL = EnumHelper.addArmorMaterial("STEEL", "TODO"
				, Mekanism.configuration.get("tools.armor-balance.steel", "durability", 40).getInt()
				, new int[]
				{
						Mekanism.configuration.get("tools.armor-balance.steel.protection", "feet", 3).getInt(),
						Mekanism.configuration.get("tools.armor-balance.steel.protection", "legs", 6).getInt(),
						Mekanism.configuration.get("tools.armor-balance.steel.protection", "chest", 7).getInt(),
						Mekanism.configuration.get("tools.armor-balance.steel.protection", "head", 3).getInt()
				}
				, Mekanism.configuration.get("tools.armor-balance.steel", "enchantability", 10).getInt()
				, SoundEvents.ITEM_ARMOR_EQUIP_IRON
				, (float)Mekanism.configuration.get("tools.armor-balance.steel", "toughness", 1D).getDouble()
		);
		
		if(Mekanism.configuration.hasChanged())
		{
			Mekanism.configuration.save();
		}

		ToolsItems.initializeItems();
		ToolsItems.setHarvestLevels();
		ToolsItems.register();
	}

	@SubscribeEvent
	public void onLivingSpecialSpawn(LivingSpawnEvent event)
	{
		double chance = event.getWorld().rand.nextDouble();
		int armorType = event.getWorld().rand.nextInt(4);
		
		if(chance < tools.armorSpawnRate)
		{
			if(event.getEntityLiving() instanceof EntityZombie || event.getEntityLiving() instanceof EntitySkeleton)
			{
				int sword = event.getWorld().rand.nextInt(100);
				int helmet = event.getWorld().rand.nextInt(100);
				int chestplate = event.getWorld().rand.nextInt(100);
				int leggings = event.getWorld().rand.nextInt(100);
				int boots = event.getWorld().rand.nextInt(100);
				
				if(armorType == 0)
				{
					if(event.getEntityLiving() instanceof EntityZombie && sword < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ToolsItems.GlowstoneSword));
					if(helmet < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ToolsItems.GlowstoneHelmet));
					if(chestplate < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ToolsItems.GlowstoneChestplate));
					if(leggings < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ToolsItems.GlowstoneLeggings));
					if(boots < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ToolsItems.GlowstoneBoots));
				}
				else if(armorType == 1)
				{
					if(event.getEntityLiving() instanceof EntityZombie && sword < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ToolsItems.LazuliSword));
					if(helmet < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ToolsItems.LazuliHelmet));
					if(chestplate < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ToolsItems.LazuliChestplate));
					if(leggings < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ToolsItems.LazuliLeggings));
					if(boots < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ToolsItems.LazuliBoots));
				}
				else if(armorType == 2)
				{
					if(event.getEntityLiving() instanceof EntityZombie && sword < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ToolsItems.OsmiumSword));
					if(helmet < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ToolsItems.OsmiumHelmet));
					if(chestplate < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ToolsItems.OsmiumChestplate));
					if(leggings < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ToolsItems.OsmiumLeggings));
					if(boots < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ToolsItems.OsmiumBoots));
				}
				else if(armorType == 3)
				{
					if(event.getEntityLiving() instanceof EntityZombie && sword < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ToolsItems.SteelSword));
					if(helmet < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ToolsItems.SteelHelmet));
					if(chestplate < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ToolsItems.SteelChestplate));
					if(leggings < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ToolsItems.SteelLeggings));
					if(boots < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ToolsItems.SteelBoots));
				}
				else if(armorType == 4)
				{
					if(event.getEntityLiving() instanceof EntityZombie && sword < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ToolsItems.BronzeSword));
					if(helmet < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ToolsItems.BronzeHelmet));
					if(chestplate < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ToolsItems.BronzeChestplate));
					if(leggings < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ToolsItems.BronzeLeggings));
					if(boots < 50) event.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ToolsItems.BronzeBoots));
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
	
	@Override
	public void resetClient() {}

	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event)
	{
		if(event.getModID().equals("MekanismTools"))
		{
			proxy.loadConfiguration();
		}
	}
}
