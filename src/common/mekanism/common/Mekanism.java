package mekanism.common;

import ic2.api.Ic2Recipes;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;


import mekanism.api.ItemMachineUpgrade;
import mekanism.client.SoundHandler;
import net.minecraftforge.common.*;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingSpecialSpawnEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraft.src.*;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;

/**
 * Mekanism mod -- adds in Tools, Armor, Weapons, Machines, and Magic. Universal source.
 * @author AidanBrady
 *
 */
@Mod(modid = "Mekanism", name = "Mekanism", version = "5.0.0")
@NetworkMod(channels = {"Mekanism"}, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class Mekanism
{
	/** Mekanism logger instance */
	public static Logger logger = Logger.getLogger("Minecraft");
	
	/** Mekanism proxy instance */
	@SidedProxy(clientSide = "mekanism.client.ClientProxy", serverSide = "mekanism.common.CommonProxy")
	public static CommonProxy proxy;
	
    /** Mekanism mod instance */
	@Instance("Mekanism")
    public static Mekanism instance;
    
    /** Mekanism hooks instance */
    public static MekanismHooks hooks;
    
    /** Mekanism configuration instance */
    public static Configuration configuration;
    
	/** Mekanism version number */
	public static Version versionNumber = new Version(5, 0, 0);
	
	/** Mekanism creative tab */
	public static CreativeTabMekanism tabMekanism = new CreativeTabMekanism();
	
	/** The latest version number which is received from the Mekanism server */
	public static String latestVersionNumber;
	
	/** The recent news which is received from the Mekanism server */
	public static String recentNews;
	
	/** The main MachineryManager instance that is used by all machines */
	public static MachineryManager manager;

	@SideOnly(Side.CLIENT)
	/** The main SoundHandler instance that is used by all audio sources */
	public static SoundHandler audioHandler;
	
	/** The IP used to connect to the Mekanism server */
	public static String hostIP = "71.56.58.57";
	
	/** The port used to connect to the Mekanism server */
	public static int hostPort = 3073;
	
    //Enums: Tools
    public static EnumToolMaterial toolOBSIDIAN = EnumHelper.addToolMaterial("OBSIDIAN", 3, 2500, 20F, 10, 50);
    public static EnumToolMaterial toolOBSIDIAN2 = EnumHelper.addToolMaterial("OBSIDIAN2", 3, 3000, 25F, 10, 100);
    public static EnumToolMaterial toolLAZULI = EnumHelper.addToolMaterial("LAZULI", 2, 200, 5.0F, 0, 22);
    public static EnumToolMaterial toolLAZULI2 = EnumHelper.addToolMaterial("LAZULI2", 2, 250, 6.0F, 4, 50);
    public static EnumToolMaterial toolPLATINUM = EnumHelper.addToolMaterial("PLATINUM", 2, 500, 10F, 4, 30);
    public static EnumToolMaterial toolPLATINUM2 = EnumHelper.addToolMaterial("PLATINUM2", 3, 700, 12F, 5, 40);
    public static EnumToolMaterial toolREDSTONE = EnumHelper.addToolMaterial("REDSTONE", 2, 250, 10F, 6, 50);
    public static EnumToolMaterial toolREDSTONE2 = EnumHelper.addToolMaterial("REDSTONE2", 2, 400, 12F, 6, 60);
    public static EnumToolMaterial toolGLOWSTONE = EnumHelper.addToolMaterial("GLOWSTONE", 2, 300, 14, 5, 80);
    public static EnumToolMaterial toolGLOWSTONE2 = EnumHelper.addToolMaterial("GLOWSTONE2", 2, 450, 18, 5, 100);
    
    //Enums: Armor
    public static EnumArmorMaterial armorOBSIDIAN = EnumHelper.addArmorMaterial("OBSIDIAN", 50, new int[]{5, 12, 8, 5}, 50);
    public static EnumArmorMaterial armorLAZULI = EnumHelper.addArmorMaterial("LAZULI", 13, new int[]{2, 5, 4, 2}, 50);
    public static EnumArmorMaterial armorPLATINUM = EnumHelper.addArmorMaterial("PLATINUM", 30, new int[]{4, 10, 7, 4}, 50);
    public static EnumArmorMaterial armorREDSTONE = EnumHelper.addArmorMaterial("REDSTONE", 16, new int[]{2, 7, 5, 3}, 50);
    public static EnumArmorMaterial armorGLOWSTONE = EnumHelper.addArmorMaterial("GLOWSTONE", 18, new int[]{3, 7, 6, 3}, 50);
    
	//Block IDs
    public static int multiBlockID = 3000;
    public static int machineBlockID = 3001;
    public static int oreBlockID = 3002;
	public static int obsidianTNTID = 3003;
	public static int powerUnitID = 3004;
	public static int generatorID = 3005;
	
	//Base Items
	public static Item WoodPaxel;
	public static Item StonePaxel;
	public static Item IronPaxel;
	public static Item DiamondPaxel;
	public static Item GoldPaxel;
	public static Item WoodKnife;
	public static Item StoneKnife;
	public static Item IronKnife;
	public static Item DiamondKnife;
	public static Item GoldKnife;
	
	//Glowstone Items
	public static Item GlowstonePaxel;
	public static Item GlowstonePickaxe;
	public static Item GlowstoneAxe;
	public static Item GlowstoneSpade;
	public static Item GlowstoneHoe;
	public static Item GlowstoneSword;
	public static Item GlowstoneHelmet;
	public static Item GlowstoneBody;
	public static Item GlowstoneLegs;
	public static Item GlowstoneBoots;
	public static Item GlowstoneKnife;
	
	//Redstone Items
	public static Item RedstonePaxel;
	public static Item RedstonePickaxe;
	public static Item RedstoneAxe;
	public static Item RedstoneSpade;
	public static Item RedstoneHoe;
	public static Item RedstoneSword;
	public static Item RedstoneHelmet;
	public static Item RedstoneBody;
	public static Item RedstoneLegs;
	public static Item RedstoneBoots;
	public static Item RedstoneKnife;
	
	//Platinum Items
	public static Item PlatinumPaxel;
	public static Item PlatinumPickaxe;
	public static Item PlatinumAxe;
	public static Item PlatinumSpade;
	public static Item PlatinumHoe;
	public static Item PlatinumSword;
	public static Item PlatinumHelmet;
	public static Item PlatinumBody;
	public static Item PlatinumLegs;
	public static Item PlatinumBoots;
	public static Item PlatinumKnife;
	
	//Obsidian Items
	public static Item ObsidianHelmet;
	public static Item ObsidianBody;
	public static Item ObsidianLegs;
	public static Item ObsidianBoots;
	public static Item ObsidianPaxel;
	public static Item ObsidianPickaxe;
	public static Item ObsidianAxe;
	public static Item ObsidianSpade;
	public static Item ObsidianHoe;
	public static Item ObsidianSword;
	public static Item ObsidianKnife;
	
	//Lazuli Items
	public static Item LazuliPaxel;
	public static Item LazuliPickaxe;
	public static Item LazuliAxe;
	public static Item LazuliSpade;
	public static Item LazuliHoe;
	public static Item LazuliSword;
	public static Item LazuliHelmet;
	public static Item LazuliBody;
	public static Item LazuliLegs;
	public static Item LazuliBoots;
	public static Item LazuliKnife;
	
	//Extra Items
	public static ItemEnergizedBow EnergizedBow;
	public static Item LightningRod;
	public static Item Stopwatch;
	public static Item WeatherOrb;
	public static Item EnrichedAlloy;
	public static ItemEnergized EnergyTablet;
	public static ItemEnergized EnergyOrb;
	public static ItemEnergized EnergyCube;
	public static Item SpeedUpgrade;
	public static Item EnergyUpgrade;
	public static Item UltimateUpgrade;
	public static ItemNuclearDisassembler NuclearDisassembler;
	public static Item NuclearCore;
	public static Item SolarPanel;
	public static ItemStorageTank HydrogenTank;
	public static ItemStorageTank OxygenTank;
	
	//Extra Blocks
	public static Block MultiBlock;
	public static Block MachineBlock;
	public static Block OreBlock;
	public static Block ObsidianTNT;
	public static Block PowerUnit;
	public static Block Generator;
	
	//MultiID Items
	public static Item Dust;
	public static Item Ingot;
	
	//Boolean Values
	public static boolean extrasEnabled = true;
	public static boolean oreGenerationEnabled = true;
	
	//Extra data
	public static float ObsidianTNTBlastRadius = 12.0F;
	public static int ObsidianTNTDelay = 100;
	
	/** Total ticks passed since thePlayer joined theWorld */
	public static int ticksPassed = 0;
	
	public static int ANIMATED_TEXTURE_INDEX = 240;
	public static int BOW_TEXTURE_INDEX = 177;
	
	/**
	 * Adds all in-game crafting and smelting recipes.
	 */
	public void addRecipes()
	{
		//Crafting Recipes
		//Base
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(WoodPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Item.axeWood, Character.valueOf('Y'), Item.pickaxeWood, Character.valueOf('Z'), Item.shovelWood, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(StonePaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Item.axeStone, Character.valueOf('Y'), Item.pickaxeStone, Character.valueOf('Z'), Item.shovelStone, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(IronPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Item.axeSteel, Character.valueOf('Y'), Item.pickaxeSteel, Character.valueOf('Z'), Item.shovelSteel, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(DiamondPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Item.axeDiamond, Character.valueOf('Y'), Item.pickaxeDiamond, Character.valueOf('Z'), Item.shovelDiamond, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GoldPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Item.axeGold, Character.valueOf('Y'), Item.pickaxeGold, Character.valueOf('Z'), Item.shovelGold, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(WoodKnife), new Object[] {
			" ^", "I ", Character.valueOf('^'), Block.planks, Character.valueOf('I'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(StoneKnife), new Object[] {
			" ^", "I ", Character.valueOf('^'), Block.cobblestone, Character.valueOf('I'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(IronKnife), new Object[] {
			" ^", "I ", Character.valueOf('^'), Item.ingotIron, Character.valueOf('I'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(DiamondKnife), new Object[] {
			" ^", "I ", Character.valueOf('^'), Item.diamond, Character.valueOf('I'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GoldKnife), new Object[] {
			" ^", "I ", Character.valueOf('^'), Item.ingotGold, Character.valueOf('I'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Item.coal, 9), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(MultiBlock, 1, 3)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MultiBlock, 1, 3), new Object[] {
			"***", "***", "***", Character.valueOf('*'), Item.coal
		}));
		
		//Obsidian
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MultiBlock, 1, 2), new Object[] {
			"***", "***", "***", Character.valueOf('*'), new ItemStack(Ingot, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Ingot, 9, 0), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(MultiBlock, 1, 2)	
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), new ItemStack(Ingot, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), new ItemStack(Ingot, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), new ItemStack(Ingot, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), new ItemStack(Ingot, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), ObsidianAxe, Character.valueOf('Y'), ObsidianPickaxe, Character.valueOf('Z'), ObsidianSpade, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianPickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), new ItemStack(Ingot, 1, 0), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), new ItemStack(Ingot, 1, 0), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianSpade, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), new ItemStack(Ingot, 1, 0), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), new ItemStack(Ingot, 1, 0), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), new ItemStack(Ingot, 1, 0), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianKnife, 1), new Object[] {
			" ^", "I ", Character.valueOf('^'), new ItemStack(Ingot, 1, 0), Character.valueOf('I'), Item.stick
		}));
		
		//Glowstone
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MultiBlock, 1, 4), new Object[] {
			"***", "***", "***", Character.valueOf('*'), new ItemStack(Ingot, 1, 3)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Ingot, 9, 3), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(MultiBlock, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstonePaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), GlowstoneAxe, Character.valueOf('Y'), GlowstonePickaxe, Character.valueOf('Z'), GlowstoneSpade, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstonePickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), new ItemStack(Ingot, 1, 3), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), new ItemStack(Ingot, 1, 3), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneSpade, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), new ItemStack(Ingot, 1, 3), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), new ItemStack(Ingot, 1, 3), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), new ItemStack(Ingot, 1, 3), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), new ItemStack(Ingot, 1, 3)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), new ItemStack(Ingot, 1, 3)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), new ItemStack(Ingot, 1, 3)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), new ItemStack(Ingot, 1, 3)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneKnife, 1), new Object[] {
			" ^", "I ", Character.valueOf('^'), new ItemStack(Ingot, 1, 3), Character.valueOf('I'), Item.stick
		}));
		
		//Lazuli
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(LazuliHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), new ItemStack(Item.dyePowder, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(LazuliBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), new ItemStack(Item.dyePowder, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(LazuliLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), new ItemStack(Item.dyePowder, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(LazuliBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), new ItemStack(Item.dyePowder, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(LazuliPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), LazuliAxe, Character.valueOf('Y'), LazuliPickaxe, Character.valueOf('Z'), LazuliSpade, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(LazuliPickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(LazuliAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(LazuliSpade, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(LazuliHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(LazuliSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(LazuliKnife, 1), new Object[] {
			" ^", "I ", Character.valueOf('^'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('I'), Item.stick
		}));
		
		//Platinum
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MultiBlock, 1, 0), new Object[] {
			"XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(Ingot, 1, 1)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), PlatinumAxe, Character.valueOf('Y'), PlatinumPickaxe, Character.valueOf('Z'), PlatinumSpade, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumPickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), new ItemStack(Ingot, 1, 1), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), new ItemStack(Ingot, 1, 1), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumSpade, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), new ItemStack(Ingot, 1, 1), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), new ItemStack(Ingot, 1, 1), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), new ItemStack(Ingot, 1, 1), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), new ItemStack(Ingot, 1, 1)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), new ItemStack(Ingot, 1, 1)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), new ItemStack(Ingot, 1, 1)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), new ItemStack(Ingot, 1, 1)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Ingot, 9, 1), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(MultiBlock, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumKnife, 1), new Object[] {
			" ^", "I ", Character.valueOf('^'), new ItemStack(Ingot, 1, 1), Character.valueOf('I'), Item.stick
		}));
		
		//Redstone
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MultiBlock, 1, 1), new Object[] {
			"***", "***", "***", Character.valueOf('*'), new ItemStack(Ingot, 1, 2)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Ingot, 9, 2), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(MultiBlock, 1, 1)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(RedstonePaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), RedstoneAxe, Character.valueOf('Y'), RedstonePickaxe, Character.valueOf('Z'), RedstoneSpade, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(RedstonePickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), new ItemStack(Ingot, 1, 2), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(RedstoneAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), new ItemStack(Ingot, 1, 2), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(RedstoneSpade, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), new ItemStack(Ingot, 1, 2), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(RedstoneHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), new ItemStack(Ingot, 1, 2), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(RedstoneSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), new ItemStack(Ingot, 1, 2), Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(RedstoneHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), new ItemStack(Ingot, 1, 2)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(RedstoneBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), new ItemStack(Ingot, 1, 2)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(RedstoneLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), new ItemStack(Ingot, 1, 2)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(RedstoneBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), new ItemStack(Ingot, 1, 2)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(RedstoneKnife, 1), new Object[] {
			" ^", "I ", Character.valueOf('^'), new ItemStack(Ingot, 1, 2), Character.valueOf('I'), Item.stick
		}));
		
		//Extra
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianTNT, 1), new Object[] {
			"***", "XXX", "***", Character.valueOf('*'), Block.obsidian, Character.valueOf('X'), Block.tnt
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(EnergizedBow.getUnchargedItem(), new Object[] {
			" AB", "A B", " AB", Character.valueOf('A'), new ItemStack(Ingot, 1, 0), Character.valueOf('B'), Item.silk
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(EnergyCube.getUnchargedItem(), new Object[] {
			"RAR", "APA", "RAR", Character.valueOf('R'), Item.redstone, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('P'), new ItemStack(Dust, 1, 2)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(EnergyTablet.getUnchargedItem(), new Object[] {
			"RCR", "ECE", "RCR", Character.valueOf('C'), EnergyCube.getUnchargedItem(), Character.valueOf('R'), Item.redstone, Character.valueOf('E'), EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(EnergyOrb.getUnchargedItem(), new Object[] {
			"ECE", "CCC", "ECE", Character.valueOf('E'), EnrichedAlloy, Character.valueOf('C'), EnergyCube.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PowerUnit, 1, 0), new Object[] {
			"CEC", "EPE", "CEC", Character.valueOf('C'), EnergyCube.getUnchargedItem(), Character.valueOf('E'), EnrichedAlloy, Character.valueOf('P'), new ItemStack(MultiBlock, 1, 0) 
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PowerUnit, 1, 1), new Object[] {
			"ECE", "CPC", "ECE", Character.valueOf('E'), EnrichedAlloy, Character.valueOf('C'), EnergyCube.getUnchargedItem(), Character.valueOf('P'), new ItemStack(PowerUnit, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 0), new Object[] {
			"***", "*R*", "***", Character.valueOf('*'), new ItemStack(Ingot, 1, 1), Character.valueOf('R'), Item.redstone
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 1), new Object[] {
			"***", "*P*", "***", Character.valueOf('*'), Item.redstone, Character.valueOf('P'), new ItemStack(MultiBlock, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 2), new Object[] {
			"***", "*P*", "***", Character.valueOf('*'), Block.cobblestone, Character.valueOf('P'), new ItemStack(MultiBlock, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 3), new Object[] {
			"***", "*L*", "***", Character.valueOf('*'), new ItemStack(Ingot, 1, 1), Character.valueOf('L'), Item.bucketLava
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SpeedUpgrade), new Object[] {
			"PAP", "ARA", "PAP", Character.valueOf('P'), new ItemStack(Dust, 1, 2), Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), Item.emerald
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(EnergyUpgrade), new Object[] {
			"RAR", "AEA", "RAR", Character.valueOf('R'), Item.redstone, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), Item.emerald
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(UltimateUpgrade), new Object[] {
			"ERA", "RDR", "ARS", Character.valueOf('E'), EnergyUpgrade, Character.valueOf('R'), Item.redstone, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('D'), Item.diamond, Character.valueOf('S'), SpeedUpgrade
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Generator, 1, 0), new Object[] {
			"GGG", "ECE", "IRI", Character.valueOf('G'), Item.lightStoneDust, Character.valueOf('E'), EnrichedAlloy, Character.valueOf('C'), new ItemStack(MultiBlock, 1, 3), Character.valueOf('I'), Item.ingotIron, Character.valueOf('R'), Item.redstone
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(NuclearCore), new Object[] {
			"AOA", "PDP", "AOA", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('O'), new ItemStack(Dust, 1, 3), Character.valueOf('P'), new ItemStack(Dust, 1, 2), Character.valueOf('D'), Item.diamond
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(NuclearDisassembler.getUnchargedItem(), new Object[] {
			"AEA", "ACA", " O ", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('C'), NuclearCore, Character.valueOf('O'), new ItemStack(Ingot, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(EnrichedAlloy), new Object[] {
			" R ", "RIR", " R ", Character.valueOf('R'), Item.redstone, Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SolarPanel), new Object[] {
			"GGG", "RAR", "PPP", Character.valueOf('G'), Block.thinGlass, Character.valueOf('R'), Item.redstone, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('P'), new ItemStack(Ingot, 1, 1)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Generator, 1, 1), new Object[] {
			"SSS", "AIA", "PEP", Character.valueOf('S'), SolarPanel, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('I'), Block.blockSteel, Character.valueOf('P'), new ItemStack(Dust, 1, 2), Character.valueOf('E'), EnergyTablet.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MultiBlock, 1, 5), new Object[] {
			"PAP", "AIA", "PAP", Character.valueOf('P'), new ItemStack(Ingot, 1, 1), Character.valueOf('A'), EnrichedAlloy, Character.valueOf('I'), Block.blockSteel
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(HydrogenTank.getEmptyItem(), new Object[] {
			"III", "IDI", "III", Character.valueOf('I'), Item.ingotIron, Character.valueOf('D'), "dustIron"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(OxygenTank.getEmptyItem(), new Object[] {
			"III", "IGI", "III", Character.valueOf('I'), Item.ingotIron, Character.valueOf('G'), "dustGold"
		}));
		
		if(extrasEnabled)
		{
			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 4), new Object[] {
				"SGS", "GDG", "SGS", Character.valueOf('S'), EnrichedAlloy, Character.valueOf('G'), Block.glass, Character.valueOf('D'), Block.blockDiamond
			}));
		}
	
		//Furnace Recipes
		GameRegistry.addSmelting(new ItemStack(OreBlock, 1, 0).itemID, new ItemStack(Ingot, 2, 1), 1.0F);
		GameRegistry.addSmelting(new ItemStack(Dust, 1, 2).itemID, new ItemStack(Ingot, 1, 1), 1.0F);
		
		//Enrichment Chamber Recipes
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Dust, 1, 4), new ItemStack(Item.diamond));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(OreBlock, 1, 0), new ItemStack(Dust, 2, 2));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreRedstone), new ItemStack(Item.redstone, 2));
        RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.obsidian), new ItemStack(Dust, 1, 3));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreIron), new ItemStack(Dust, 2, 0));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreGold), new ItemStack(Dust, 2, 1));
		
		//Platinum Compressor Recipes
		RecipeHandler.addPlatinumCompressorRecipe(new ItemStack(Item.redstone), new ItemStack(Ingot, 1, 2));
		RecipeHandler.addPlatinumCompressorRecipe(new ItemStack(Item.lightStoneDust), new ItemStack(Ingot, 1, 3));
        RecipeHandler.addPlatinumCompressorRecipe(new ItemStack(Dust, 1, 3), new ItemStack(Ingot, 1, 0));
		
		//Combiner Recipes
		RecipeHandler.addCombinerRecipe(new ItemStack(Item.redstone, 4), new ItemStack(Block.oreRedstone));
		RecipeHandler.addCombinerRecipe(new ItemStack(Item.redstone), new ItemStack(Ingot, 1, 2));
		RecipeHandler.addCombinerRecipe(new ItemStack(Dust, 2, 2), new ItemStack(OreBlock, 1, 0));
		RecipeHandler.addCombinerRecipe(new ItemStack(Item.diamond), new ItemStack(Block.oreDiamond));
		RecipeHandler.addCombinerRecipe(new ItemStack(Item.dyePowder, 4, 4), new ItemStack(Block.oreLapis));
        RecipeHandler.addCombinerRecipe(new ItemStack(Dust, 1, 3), new ItemStack(Block.obsidian));
		RecipeHandler.addCombinerRecipe(new ItemStack(Dust, 2, 0), new ItemStack(Block.oreIron));
		RecipeHandler.addCombinerRecipe(new ItemStack(Dust, 2, 1), new ItemStack(Block.oreGold));
		
		//Crusher Recipes
		RecipeHandler.addCrusherRecipe(new ItemStack(Item.diamond), new ItemStack(Dust, 1, 4));
        RecipeHandler.addCrusherRecipe(new ItemStack(Ingot, 1, 2), new ItemStack(Item.redstone));
        RecipeHandler.addCrusherRecipe(new ItemStack(Ingot, 1, 1), new ItemStack(Dust, 1, 2));
        RecipeHandler.addCrusherRecipe(new ItemStack(Ingot, 1, 3), new ItemStack(Item.lightStoneDust));
        RecipeHandler.addCrusherRecipe(new ItemStack(Ingot, 1, 0), new ItemStack(Dust, 1, 3));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.ingotIron), new ItemStack(Dust, 1, 0));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.ingotGold), new ItemStack(Dust, 1, 1));
        
        //Theoretical Elementizer Recipes
        RecipeHandler.addTheoreticalElementizerRecipe(new ItemStack(EnrichedAlloy), new ItemStack(TileEntityTheoreticalElementizer.getRandomMagicItem()));
	}
	
	/**
	 * Adds all item and block names.
	 */
	public void addNames()
	{
		//Base
		LanguageRegistry.addName(WoodPaxel, "Wood Paxel");
		LanguageRegistry.addName(StonePaxel, "Stone Paxel");
		LanguageRegistry.addName(IronPaxel, "Iron Paxel");
		LanguageRegistry.addName(DiamondPaxel, "Diamond Paxel");
		LanguageRegistry.addName(GoldPaxel, "Gold Paxel");
		LanguageRegistry.addName(WoodKnife, "Wood Knife");
		LanguageRegistry.addName(StoneKnife, "Stone Knife");
		LanguageRegistry.addName(IronKnife, "Iron Knife");
		LanguageRegistry.addName(DiamondKnife, "Diamond Knife");
		LanguageRegistry.addName(GoldKnife, "Gold Knife");
		
		//Obsidian
		LanguageRegistry.addName(ObsidianHelmet, "Obsidian Helmet");
		LanguageRegistry.addName(ObsidianBody, "Obsidian Chestplate");
		LanguageRegistry.addName(ObsidianLegs, "Obsidian Leggings");
		LanguageRegistry.addName(ObsidianBoots, "Obsidian Boots");
		LanguageRegistry.addName(ObsidianPaxel, "Obsidian Paxel");
		LanguageRegistry.addName(ObsidianPickaxe, "Obsidian Pickaxe");
		LanguageRegistry.addName(ObsidianAxe, "Obsidian Axe");
		LanguageRegistry.addName(ObsidianSpade, "Obsidian Shovel");
		LanguageRegistry.addName(ObsidianHoe, "Obsidian Hoe");
		LanguageRegistry.addName(ObsidianSword, "Obsidian Sword");
		LanguageRegistry.addName(ObsidianKnife, "Obsidian Knife");
		
		//Lazuli
		LanguageRegistry.addName(LazuliHelmet, "Lapis Lazuli Helmet");
		LanguageRegistry.addName(LazuliBody, "Lapis Lazuli Chestplate");
		LanguageRegistry.addName(LazuliLegs, "Lapis Lazuli Leggings");
		LanguageRegistry.addName(LazuliBoots, "Lapis Lazuli Boots");
		LanguageRegistry.addName(LazuliPaxel, "Lapis Lazuli Paxel");
		LanguageRegistry.addName(LazuliPickaxe, "Lapis Lazuli Pickaxe");
		LanguageRegistry.addName(LazuliAxe, "Lapis Lazuli Axe");
		LanguageRegistry.addName(LazuliSpade, "Lapis Lazuli Shovel");
		LanguageRegistry.addName(LazuliHoe, "Lapis Lazuli Hoe");
		LanguageRegistry.addName(LazuliSword, "Lapis Lazuli Sword");
		LanguageRegistry.addName(LazuliKnife, "Lazuli Knife");
		
		//Platinum
		LanguageRegistry.addName(PlatinumHelmet, "Platinum Helmet");
		LanguageRegistry.addName(PlatinumBody, "Platinum Chestplate");
		LanguageRegistry.addName(PlatinumLegs, "Platinum Leggings");
		LanguageRegistry.addName(PlatinumBoots, "Platinum Boots");
		LanguageRegistry.addName(PlatinumPaxel, "Platinum Paxel");
		LanguageRegistry.addName(PlatinumPickaxe, "Platinum Pickaxe");
		LanguageRegistry.addName(PlatinumAxe, "Platinum Axe");
		LanguageRegistry.addName(PlatinumSpade, "Platinum Shovel");
		LanguageRegistry.addName(PlatinumHoe, "Platinum Hoe");
		LanguageRegistry.addName(PlatinumSword, "Platinum Sword");
		LanguageRegistry.addName(PlatinumKnife, "Platinum Knife");
		
		//Redstone
		LanguageRegistry.addName(RedstoneHelmet, "Redstone Helmet");
		LanguageRegistry.addName(RedstoneBody, "Redstone Chestplate");
		LanguageRegistry.addName(RedstoneLegs, "Redstone Leggings");
		LanguageRegistry.addName(RedstoneBoots, "Redstone Boots");
		LanguageRegistry.addName(RedstonePaxel, "Redstone Paxel");
		LanguageRegistry.addName(RedstonePickaxe, "Redstone Pickaxe");
		LanguageRegistry.addName(RedstoneAxe, "Redstone Axe");
		LanguageRegistry.addName(RedstoneSpade, "Redstone Shovel");
		LanguageRegistry.addName(RedstoneHoe, "Redstone Hoe");
		LanguageRegistry.addName(RedstoneSword, "Redstone Sword");
		LanguageRegistry.addName(RedstoneKnife, "Redstone Knife");	
		
		//Glowstone
		LanguageRegistry.addName(GlowstonePaxel, "Glowstone Paxel");
		LanguageRegistry.addName(GlowstonePickaxe, "Glowstone Pickaxe");
		LanguageRegistry.addName(GlowstoneAxe, "Glowstone Axe");
		LanguageRegistry.addName(GlowstoneSpade, "Glowstone Shovel");
		LanguageRegistry.addName(GlowstoneHoe, "Glowstone Hoe");
		LanguageRegistry.addName(GlowstoneSword, "Glowstone Sword");
		LanguageRegistry.addName(GlowstoneHelmet, "Glowstone Helmet");
		LanguageRegistry.addName(GlowstoneBody, "Glowstone Chestplate");
		LanguageRegistry.addName(GlowstoneLegs, "Glowstone Leggings");
		LanguageRegistry.addName(GlowstoneBoots, "Glowstone Boots");
		LanguageRegistry.addName(GlowstoneKnife, "Glowstone Knife");
		
		//Extras
		LanguageRegistry.addName(EnergizedBow, "Energized Bow");
		LanguageRegistry.addName(ObsidianTNT, "Obsidian TNT");
		
		if(extrasEnabled == true)
		{
			LanguageRegistry.addName(LightningRod, "Lightning Rod");
			LanguageRegistry.addName(Stopwatch, "Steve's Stopwatch");
			LanguageRegistry.addName(WeatherOrb, "Weather Orb");
			LanguageRegistry.addName(EnrichedAlloy, "Enriched Alloy");
		}
		
		LanguageRegistry.addName(EnergyTablet, "Energy Tablet");
		LanguageRegistry.addName(EnergyOrb, "Energy Orb");
		LanguageRegistry.addName(EnergyCube, "Energy Cube");
		LanguageRegistry.addName(SpeedUpgrade, "Speed Upgrade");
		LanguageRegistry.addName(EnergyUpgrade, "Energy Upgrade");
		LanguageRegistry.addName(UltimateUpgrade, "Ultimate Upgrade");
		LanguageRegistry.addName(NuclearDisassembler, "Nuclear Disassembler");
		LanguageRegistry.addName(NuclearCore, "Nuclear Core");
		LanguageRegistry.addName(EnergizedBow, "Energized Bow");
		LanguageRegistry.addName(SolarPanel, "Solar Panel");
		LanguageRegistry.addName(HydrogenTank, "Hydrogen Tank");
		LanguageRegistry.addName(OxygenTank, "Oxygen Tank");
		
		//Localization for MultiBlock
		LanguageRegistry.instance().addStringLocalization("tile.MultiBlock.PlatinumBlock.name", "Platinum Block");
		LanguageRegistry.instance().addStringLocalization("tile.MultiBlock.RedstoneBlock.name", "Redstone Block");
		LanguageRegistry.instance().addStringLocalization("tile.MultiBlock.RefinedObsidian.name", "Refined Obsidian");
		LanguageRegistry.instance().addStringLocalization("tile.MultiBlock.CoalBlock.name", "Coal Block");
		LanguageRegistry.instance().addStringLocalization("tile.MultiBlock.RefinedGlowstone.name", "Refined Glowstone");
		LanguageRegistry.instance().addStringLocalization("tile.MultiBlock.ReinforcedIron.name", "Reinforced Iron");
		
		//Localization for MachineBlock
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.EnrichmentChamber.name", "Enrichment Chamber");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.PlatinumCompressor.name", "Platinum Compressor");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.Combiner.name", "Combiner");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.Crusher.name", "Crusher");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.TheoreticalElementizer.name", "Theoretical Elementizer");
		
		//Localization for OreBlock
		LanguageRegistry.instance().addStringLocalization("tile.OreBlock.PlatinumOre.name", "Platinum Ore");
		
		//Localization for PowerUnit
		LanguageRegistry.instance().addStringLocalization("tile.PowerUnit.PowerUnit.name", "Power Unit");
		LanguageRegistry.instance().addStringLocalization("tile.PowerUnit.AdvancedPowerUnit.name", "Advanced Power Unit");
		
		//Localization for Generator
		LanguageRegistry.instance().addStringLocalization("tile.Generator.HeatGenerator.name", "Heat Generator");
		LanguageRegistry.instance().addStringLocalization("tile.Generator.SolarGenerator.name", "Solar Generator");
		LanguageRegistry.instance().addStringLocalization("tile.Generator.ElectrolyticSeparator.name", "Electrolytic Separator");
		LanguageRegistry.instance().addStringLocalization("tile.Generator.HydrogenGenerator.name", "Hydrogen Generator");
		
		//Localization for Dust
		LanguageRegistry.instance().addStringLocalization("item.ironDust.name", "Iron Dust");
		LanguageRegistry.instance().addStringLocalization("item.goldDust.name", "Gold Dust");
		LanguageRegistry.instance().addStringLocalization("item.platinumDust.name", "Platinum Dust");
		LanguageRegistry.instance().addStringLocalization("item.obsidianDust.name", "Obsidian Dust");
		LanguageRegistry.instance().addStringLocalization("item.diamondDust.name", "Diamond Dust");
		
		//Localization for Ingot
		LanguageRegistry.instance().addStringLocalization("item.obsidianIngot.name", "Obsidian Ingot");
		LanguageRegistry.instance().addStringLocalization("item.platinumIngot.name", "Platinum Ingot");
		LanguageRegistry.instance().addStringLocalization("item.redstoneIngot.name", "Redstone Ingot");
		LanguageRegistry.instance().addStringLocalization("item.glowstoneIngot.name", "Glowstone Ingot");
	}
	
	/**
	 * Adds all item textures from the sprite sheet.
	 */
	public void addTextures()
	{
		//Base
		WoodPaxel.setIconIndex(150);
		StonePaxel.setIconIndex(151);
		IronPaxel.setIconIndex(152);
		DiamondPaxel.setIconIndex(153);
		GoldPaxel.setIconIndex(154);
		WoodKnife.setIconIndex(214);
		StoneKnife.setIconIndex(215);
		IronKnife.setIconIndex(216);
		DiamondKnife.setIconIndex(217);
		GoldKnife.setIconIndex(218);
		
		//Glowstone
		GlowstoneHelmet.setIconIndex(4);
		GlowstoneBody.setIconIndex(20);
		GlowstoneLegs.setIconIndex(36);
		GlowstoneBoots.setIconIndex(52);
		GlowstonePaxel.setIconIndex(148);
		GlowstonePickaxe.setIconIndex(68);
		GlowstoneAxe.setIconIndex(84);
		GlowstoneSpade.setIconIndex(100);
		GlowstoneHoe.setIconIndex(116);
		GlowstoneSword.setIconIndex(132);
		GlowstoneKnife.setIconIndex(212);
		
		//Redstone
		RedstoneHelmet.setIconIndex(3);
		RedstoneBody.setIconIndex(19);
		RedstoneLegs.setIconIndex(35);
		RedstoneBoots.setIconIndex(51);
		RedstonePaxel.setIconIndex(147);
		RedstonePickaxe.setIconIndex(67);
		RedstoneAxe.setIconIndex(83);
		RedstoneSpade.setIconIndex(99);
		RedstoneHoe.setIconIndex(115);
		RedstoneSword.setIconIndex(131);
		RedstoneKnife.setIconIndex(211);
		
		//Platinum
		PlatinumHelmet.setIconIndex(2);
		PlatinumBody.setIconIndex(18);
		PlatinumLegs.setIconIndex(34);
		PlatinumBoots.setIconIndex(50);
		PlatinumPaxel.setIconIndex(146);
		PlatinumPickaxe.setIconIndex(66);
		PlatinumAxe.setIconIndex(82);
		PlatinumSpade.setIconIndex(98);
		PlatinumHoe.setIconIndex(114);
		PlatinumSword.setIconIndex(130);
		PlatinumKnife.setIconIndex(210);
		
		//Obsidian
		ObsidianHelmet.setIconIndex(1);
		ObsidianBody.setIconIndex(17);
		ObsidianLegs.setIconIndex(33);
		ObsidianBoots.setIconIndex(49);
		ObsidianPaxel.setIconIndex(145);
		ObsidianPickaxe.setIconIndex(65);
		ObsidianAxe.setIconIndex(81);
		ObsidianSpade.setIconIndex(97);
		ObsidianHoe.setIconIndex(113);
		ObsidianSword.setIconIndex(129);
		ObsidianKnife.setIconIndex(209);
		
		//Lazuli
		LazuliPaxel.setIconIndex(144);
		LazuliPickaxe.setIconIndex(64);
		LazuliAxe.setIconIndex(80);
		LazuliSpade.setIconIndex(96);
		LazuliHoe.setIconIndex(112);
		LazuliSword.setIconIndex(128);
		LazuliHelmet.setIconIndex(0);
		LazuliBody.setIconIndex(16);
		LazuliLegs.setIconIndex(32);
		LazuliBoots.setIconIndex(48);
		LazuliKnife.setIconIndex(208);
		
		if(extrasEnabled == true)
		{
			LightningRod.setIconIndex(225);
			Stopwatch.setIconIndex(224);
			WeatherOrb.setIconIndex(226);
			EnrichedAlloy.setIconIndex(227);
		}
		
		EnergyTablet.setIconIndex(228);
		EnergyOrb.setIconIndex(229);
		EnergyCube.setIconIndex(230);
		SpeedUpgrade.setIconIndex(232);
		EnergyUpgrade.setIconIndex(231);
		UltimateUpgrade.setIconIndex(233);	
		NuclearDisassembler.setIconIndex(253);
		NuclearCore.setIconIndex(254);
		EnergizedBow.setIconIndex(252);
		SolarPanel.setIconIndex(255);
		HydrogenTank.setIconIndex(251);
		OxygenTank.setIconIndex(239);
	}
	
	/**
	 * Adds and registers all items.
	 */
	public void addItems()
	{
		RedstoneHelmet = (new ItemMekanismArmor(11235, armorREDSTONE, proxy.getArmorIndex("redstone"), 0)).setItemName("RedstoneHelmet");
		RedstoneBody = (new ItemMekanismArmor(11236, armorREDSTONE, proxy.getArmorIndex("redstone"), 1)).setItemName("RedstoneBody");
		RedstoneLegs = (new ItemMekanismArmor(11237, armorREDSTONE, proxy.getArmorIndex("redstone"), 2)).setItemName("RedstoneLegs");
		RedstoneBoots = (new ItemMekanismArmor(11238, armorREDSTONE, proxy.getArmorIndex("redstone"), 3)).setItemName("RedstoneBoots");
		RedstonePaxel = new ItemMekanismPaxel(11239, toolREDSTONE2).setItemName("RedstonePaxel");
		RedstonePickaxe = new ItemMekanismPickaxe(11240, toolREDSTONE).setItemName("RedstonePickaxe");
		RedstoneAxe = new ItemMekanismAxe(11241, toolREDSTONE).setItemName("RedstoneAxe");
		RedstoneSpade = new ItemMekanismSpade(11242, toolREDSTONE).setItemName("RedstoneSpade");
		RedstoneHoe = new ItemMekanismHoe(11243, toolREDSTONE).setItemName("RedstoneHoe");
		RedstoneSword = new ItemMekanismSword(11244, toolREDSTONE).setItemName("RedstoneSword");
		PlatinumHelmet = (new ItemMekanismArmor(11245, EnumArmorMaterial.DIAMOND, proxy.getArmorIndex("platinum"), 0)).setItemName("PlatinumHelmet");
		PlatinumBody = (new ItemMekanismArmor(11246, EnumArmorMaterial.DIAMOND, proxy.getArmorIndex("platinum"), 1)).setItemName("PlatinumBody");
		PlatinumLegs = (new ItemMekanismArmor(11247, EnumArmorMaterial.DIAMOND, proxy.getArmorIndex("platinum"), 2)).setItemName("PlatinumLegs");
		PlatinumBoots = (new ItemMekanismArmor(11248, EnumArmorMaterial.DIAMOND, proxy.getArmorIndex("platinum"), 3)).setItemName("PlatinumBoots");
		PlatinumPaxel = new ItemMekanismPaxel(11249, toolPLATINUM2).setItemName("PlatinumPaxel");
		PlatinumPickaxe = new ItemMekanismPickaxe(11250, toolPLATINUM).setItemName("PlatinumPickaxe");
		PlatinumAxe = new ItemMekanismAxe(11251, toolPLATINUM).setItemName("PlatinumAxe");
		PlatinumSpade = new ItemMekanismSpade(11252, toolPLATINUM).setItemName("PlatinumSpade");
		PlatinumHoe = new ItemMekanismHoe(11253, toolPLATINUM).setItemName("PlatinumHoe");
		PlatinumSword = new ItemMekanismSword(11254, toolPLATINUM).setItemName("PlatinumSword");
		ObsidianHelmet = (new ItemMekanismArmor(11255, armorOBSIDIAN, proxy.getArmorIndex("obsidian"), 0)).setItemName("ObsidianHelmet");
		ObsidianBody = (new ItemMekanismArmor(11256, armorOBSIDIAN, proxy.getArmorIndex("obsidian"), 1)).setItemName("ObsidianBody");
		ObsidianLegs = (new ItemMekanismArmor(11257, armorOBSIDIAN, proxy.getArmorIndex("obsidian"), 2)).setItemName("ObsidianLegs");
		ObsidianBoots = (new ItemMekanismArmor(11258, armorOBSIDIAN, proxy.getArmorIndex("obsidian"), 3)).setItemName("ObsidianBoots");
		ObsidianPaxel = new ItemMekanismPaxel(11259, toolOBSIDIAN2).setItemName("ObsidianPaxel");
		ObsidianPickaxe = new ItemMekanismPickaxe(11260, toolOBSIDIAN).setItemName("ObsidianPickaxe");
		ObsidianAxe = new ItemMekanismAxe(11261, toolOBSIDIAN).setItemName("ObsidianAxe");
		ObsidianSpade = new ItemMekanismSpade(11262, toolOBSIDIAN).setItemName("ObsidianSpade");
		ObsidianHoe = new ItemMekanismHoe(11263, toolOBSIDIAN).setItemName("ObsidianHoe");
		ObsidianSword = new ItemMekanismSword(11264, toolOBSIDIAN).setItemName("ObsidianSword");
		LazuliPaxel = new ItemMekanismPaxel(11265, toolLAZULI2).setItemName("LazuliPaxel");
		LazuliPickaxe = new ItemMekanismPickaxe(11266, toolLAZULI).setItemName("LazuliPickaxe");
		LazuliAxe = new ItemMekanismAxe(11267, toolLAZULI).setItemName("LazuliAxe");
		LazuliSpade = new ItemMekanismSpade(11268, toolLAZULI).setItemName("LazuliSpade");
		LazuliHoe = new ItemMekanismHoe(11269, toolLAZULI).setItemName("LazuliHoe");
		LazuliSword = new ItemMekanismSword(11270, toolLAZULI).setItemName("LazuliSword");
		LazuliHelmet = (new ItemMekanismArmor(11271, armorLAZULI, proxy.getArmorIndex("lazuli"), 0)).setItemName("LazuliHelmet");
		LazuliBody = (new ItemMekanismArmor(11272, armorLAZULI, proxy.getArmorIndex("lazuli"), 1)).setItemName("LazuliBody");
		LazuliLegs = (new ItemMekanismArmor(11273, armorLAZULI, proxy.getArmorIndex("lazuli"), 2)).setItemName("LazuliLegs");
		LazuliBoots = (new ItemMekanismArmor(11274, armorLAZULI, proxy.getArmorIndex("lazuli"), 3)).setItemName("LazuliBoots");
		EnergizedBow = (ItemEnergizedBow) new ItemEnergizedBow(11275).setItemName("EnergizedBow");
		if(extrasEnabled == true)
		{
			LightningRod = new ItemLightningRod(11276).setItemName("LightningRod");
			Stopwatch = new ItemStopwatch(11277).setItemName("Stopwatch");
			WeatherOrb = new ItemWeatherOrb(11278).setItemName("WeatherOrb");
		}
		WoodPaxel = new ItemMekanismPaxel(11279, EnumToolMaterial.WOOD).setItemName("WoodPaxel");
		StonePaxel = new ItemMekanismPaxel(11280, EnumToolMaterial.STONE).setItemName("StonePaxel");
		IronPaxel = new ItemMekanismPaxel(11281, EnumToolMaterial.IRON).setItemName("IronPaxel");
		DiamondPaxel = new ItemMekanismPaxel(11282, EnumToolMaterial.EMERALD).setItemName("DiamondPaxel");
		GoldPaxel = new ItemMekanismPaxel(11283, EnumToolMaterial.GOLD).setItemName("GoldPaxel");
		WoodKnife = new ItemMekanismKnife(11284, EnumToolMaterial.WOOD).setItemName("WoodKnife");
		StoneKnife = new ItemMekanismKnife(11285, EnumToolMaterial.STONE).setItemName("StoneKnife");
		IronKnife = new ItemMekanismKnife(11286, EnumToolMaterial.IRON).setItemName("IronKnife");
		DiamondKnife = new ItemMekanismKnife(11287, EnumToolMaterial.EMERALD).setItemName("DiamondKnife");
		GoldKnife = new ItemMekanismKnife(11288, EnumToolMaterial.GOLD).setItemName("GoldKnife");
		ObsidianKnife = new ItemMekanismKnife(11289, toolOBSIDIAN).setItemName("ObsidianKnife");
		LazuliKnife = new ItemMekanismKnife(11290, toolLAZULI).setItemName("LazuliKnife");
		PlatinumKnife = new ItemMekanismKnife(11291, toolPLATINUM).setItemName("PlatinumKnife");
		RedstoneKnife = new ItemMekanismKnife(11292, toolREDSTONE).setItemName("RedstoneKnife");
		Dust = new ItemDust(11293-256);
		Ingot = new ItemIngot(11294-256);
		GlowstonePaxel = new ItemMekanismPaxel(11295, toolGLOWSTONE2).setItemName("GlowstonePaxel");
		GlowstonePickaxe = new ItemMekanismPickaxe(11296, toolGLOWSTONE).setItemName("GlowstonePickaxe");
		GlowstoneAxe = new ItemMekanismAxe(11297, toolGLOWSTONE).setItemName("GlowstoneAxe");
		GlowstoneSpade = new ItemMekanismSpade(11298, toolGLOWSTONE).setItemName("GlowstoneSpade");
		GlowstoneHoe = new ItemMekanismHoe(11299, toolGLOWSTONE).setItemName("GlowstoneHoe");
		GlowstoneSword = new ItemMekanismSword(11300, toolGLOWSTONE).setItemName("GlowstoneSword");
		GlowstoneHelmet = new ItemMekanismArmor(11301, armorGLOWSTONE, proxy.getArmorIndex("glowstone"), 0).setItemName("GlowstoneHelmet");
		GlowstoneBody = new ItemMekanismArmor(11302, armorGLOWSTONE, proxy.getArmorIndex("glowstone"), 1).setItemName("GlowstoneBody");
		GlowstoneLegs = new ItemMekanismArmor(11303, armorGLOWSTONE, proxy.getArmorIndex("glowstone"), 2).setItemName("GlowstoneLegs");
		GlowstoneBoots = new ItemMekanismArmor(11304, armorGLOWSTONE, proxy.getArmorIndex("glowstone"), 3).setItemName("GlowstoneBoots");
		GlowstoneKnife = new ItemMekanismKnife(11305, toolGLOWSTONE).setItemName("GlowstoneKnife");
		EnergyTablet = (ItemEnergized) new ItemEnergized(11306, 50000, 100, 500).setItemName("EnergyTablet");
		EnergyOrb = (ItemEnergized) new ItemEnergized(11307, 15000000, 1000, 150000).setItemName("EnergyOrb");
		EnergyCube = (ItemEnergized) new ItemEnergized(11308, 12000, 100, 120).setItemName("EnergyCube");
		SpeedUpgrade = new ItemMachineUpgrade(11309, 0, 150).setItemName("SpeedUpgrade");
		EnergyUpgrade = new ItemMachineUpgrade(11310, 1000, 0).setItemName("EnergyUpgrade");
		UltimateUpgrade = new ItemMachineUpgrade(11311, 2500, 180).setItemName("UltimateUpgrade");
		NuclearDisassembler = (ItemNuclearDisassembler) new ItemNuclearDisassembler(11312).setItemName("NuclearDisassembler");
		NuclearCore = new ItemMekanism(11313).setItemName("NuclearCore");
		SolarPanel = new ItemMekanism(11314).setItemName("SolarPanel");
		EnrichedAlloy = new ItemMekanism(11315).setItemName("EnrichedAlloy");
		HydrogenTank = (ItemHydrogenTank) new ItemHydrogenTank(11316).setItemName("HydrogenTank");
		OxygenTank = (ItemOxygenTank) new ItemOxygenTank(11317).setItemName("OxygenTank");
	}
	
	/**
	 * Adds and registers all blocks.
	 */
	public void addBlocks()
	{
		//Declarations
		MultiBlock = new BlockMulti(multiBlockID).setBlockName("MultiBlock");
		MachineBlock = new BlockMachine(machineBlockID).setBlockName("MachineBlock");
		OreBlock = new BlockOre(oreBlockID).setBlockName("OreBlock");
		PowerUnit = new BlockPowerUnit(powerUnitID).setBlockName("PowerUnit");
		Generator = new BlockGenerator(generatorID).setBlockName("Generator");
		ObsidianTNT = new BlockObsidianTNT(obsidianTNTID).setBlockName("ObsidianTNT").setCreativeTab(tabMekanism);
		
		//Registrations
		GameRegistry.registerBlock(ObsidianTNT);
		
		//Add block items into itemsList for blocks with common IDs.
		Item.itemsList[multiBlockID] = new ItemBlockMulti(multiBlockID - 256, MultiBlock).setItemName("MultiBlock");
		Item.itemsList[machineBlockID] = new ItemBlockMachine(machineBlockID - 256, MachineBlock).setItemName("MachineBlock");
		Item.itemsList[oreBlockID] = new ItemBlockOre(oreBlockID - 256, OreBlock).setItemName("OreBlock");
		Item.itemsList[powerUnitID] = new ItemBlockPowerUnit(powerUnitID - 256, PowerUnit).setItemName("PowerUnit");
		Item.itemsList[generatorID] = new ItemBlockGenerator(generatorID - 256, Generator).setItemName("Generator");
	}
	
	/**
	 * Integrates the mod with other mods -- registering items and blocks with the Forge Ore Dictionary
	 * and adding machine recipes with other items' corresponding resources.
	 */
	public void addIntegratedItems()
	{
		OreDictionary.registerOre("dustIron", new ItemStack(Dust, 1, 0));
		OreDictionary.registerOre("dustGold", new ItemStack(Dust, 1, 1));
		OreDictionary.registerOre("dustPlatinum", new ItemStack(Dust, 1, 2));
		OreDictionary.registerOre("dustObsidian", new ItemStack(Dust, 1, 3));
		
		//Damn you RichardG and your weird Ore Dictionary standards
		OreDictionary.registerOre("itemDustIron", new ItemStack(Dust, 1, 0));
		OreDictionary.registerOre("itemDustGold", new ItemStack(Dust, 1, 1));
		OreDictionary.registerOre("itemDustPlatinum", new ItemStack(Dust, 1, 2));
		OreDictionary.registerOre("itemDustObsidian", new ItemStack(Dust, 1, 3));
		
		OreDictionary.registerOre("ingotObsidian", new ItemStack(Ingot, 1, 0));
		OreDictionary.registerOre("ingotPlatinum", new ItemStack(Ingot, 1, 1));
		OreDictionary.registerOre("ingotRedstone", new ItemStack(Ingot, 1, 2));
		OreDictionary.registerOre("ingotGlowstone", new ItemStack(Ingot, 1, 3));
		
		OreDictionary.registerOre("orePlatinum", new ItemStack(OreBlock, 1, 0));
		
		if(hooks.IC2Loaded)
		{
			if(!hooks.RailcraftLoaded)
			{
				Ic2Recipes.addMaceratorRecipe(new ItemStack(Block.obsidian), new ItemStack(Dust, 1, 3));
			}
			ItemStack dustIron = hooks.IC2IronDust.copy();
			dustIron.stackSize = 2;
			ItemStack dustGold = hooks.IC2GoldDust.copy();
			dustGold.stackSize = 2;
			RecipeHandler.addCombinerRecipe(dustIron, new ItemStack(Block.oreIron));
			RecipeHandler.addCombinerRecipe(dustGold, new ItemStack(Block.oreGold));
		}
		
		if(hooks.RailcraftLoaded)
		{
			RecipeHandler.addPlatinumCompressorRecipe(hooks.RailcraftObsidianDust, new ItemStack(Ingot, 1, 0));
			RecipeHandler.addCombinerRecipe(hooks.RailcraftObsidianDust, new ItemStack(Block.obsidian));
		}
	}
	
	/**
	 * Adds and registers all entities and tile entities.
	 */
	public void addEntities()
	{
		//Entity IDs
		EntityRegistry.registerGlobalEntityID(EntityObsidianTNT.class, "ObsidianTNT", EntityRegistry.findGlobalUniqueEntityId());
		EntityRegistry.registerGlobalEntityID(EntityKnife.class, "Knife", EntityRegistry.findGlobalUniqueEntityId());
		
		//Registrations
		EntityRegistry.registerModEntity(EntityObsidianTNT.class, "ObsidianTNT", 51, this, 40, 5, true);
		EntityRegistry.registerModEntity(EntityKnife.class, "Knife", 52, this, 40, 5, true);
		
		//Tile entities
		GameRegistry.registerTileEntity(TileEntityEnrichmentChamber.class, "EnrichmentChamber");
		GameRegistry.registerTileEntity(TileEntityPlatinumCompressor.class, "PlatinumCompressor");
		GameRegistry.registerTileEntity(TileEntityCombiner.class, "Combiner");
		GameRegistry.registerTileEntity(TileEntityCrusher.class, "Crusher");
		GameRegistry.registerTileEntity(TileEntityTheoreticalElementizer.class, "TheoreticalElementizer");
		GameRegistry.registerTileEntity(TileEntityPowerUnit.class, "PowerUnit");
		GameRegistry.registerTileEntity(TileEntityAdvancedPowerUnit.class, "AdvancedPowerUnit");
		GameRegistry.registerTileEntity(TileEntityHeatGenerator.class, "HeatGenerator");
		GameRegistry.registerTileEntity(TileEntitySolarGenerator.class, "SolarGenerator");
		GameRegistry.registerTileEntity(TileEntityElectrolyticSeparator.class, "ElectrolyticSeparator");
		GameRegistry.registerTileEntity(TileEntityHydrogenGenerator.class, "HydrogenGenerator");
	}
	
	/**
	 * Registers the server command handler.
	 */
	@SideOnly(Side.SERVER)
	public void registerServerCommands()
	{
		ServerCommandHandler.initialize();
	}
	
	@PreInit
	public void preInit(FMLPreInitializationEvent event)
	{
		//Set the mod's configuration
		configuration = new Configuration(event.getSuggestedConfigurationFile());
	}
	
	@PostInit
	public void postInit(FMLPostInitializationEvent event)
	{
		hooks = new MekanismHooks();
		hooks.hook();
		addIntegratedItems();
		
		System.out.println("[Mekanism] Hooking complete.");
		
		proxy.loadSoundHandler();
	}
	
	@Init
	public void init(FMLInitializationEvent event) 
	{
		//Register the mod's ore handler
		GameRegistry.registerWorldGenerator(new OreHandler());
		//Register the mod's GUI handler
		NetworkRegistry.instance().registerGuiHandler(this, new CommonGuiHandler());
		//Register the MachineryManager
		manager = new MachineryManager();
		System.out.println("[Mekanism] Version " + versionNumber + " initializing...");
		new ThreadGetData();
		proxy.registerRenderInformation();
		proxy.loadConfiguration();
		proxy.loadUtilities();
		proxy.loadTickHandler();
		
		MinecraftForge.EVENT_BUS.register(this);
		
		LanguageRegistry.instance().addStringLocalization("itemGroup.tabMekanism", "Mekanism");
		
		//Attempt to load server commands
		try {
			registerServerCommands();
		} catch(NoSuchMethodError e) {}

		//Add all items
		addItems();
		System.out.println("[Mekanism] Items loaded.");
		
		//Add all blocks
		addBlocks();
		System.out.println("[Mekanism] Blocks loaded.");
		
		//Set item and block names
		addNames();
		System.out.println("[Mekanism] Names loaded.");
		
		//Set item and block textures
		addTextures();
		System.out.println("[Mekanism] Textures loaded.");
		
		//Set item and block recipes
		addRecipes();
		System.out.println("[Mekanism] Recipes loaded.");
		
		//Set up entities to run on SSP and SMP
		addEntities();
		System.out.println("[Mekanism] Entities loaded.");
		
		//Success message
		logger.info("[Mekanism] Mod loaded.");
	}
	
	@ForgeSubscribe
	public void onLivingSpecialSpawn(LivingSpecialSpawnEvent event)
	{
		Random random = new Random();
		
		int chance = random.nextInt(100);
		int secondChance = random.nextInt(4);
		
		if(chance < 5)
		{
			if(event.entityLiving instanceof EntityZombie || event.entityLiving instanceof EntitySkeleton)
			{
				if(secondChance == 0)
				{
					if(event.entityLiving instanceof EntityZombie) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(Mekanism.GlowstoneSword));
					event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(Mekanism.GlowstoneHelmet));
					event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(Mekanism.GlowstoneBody));
					event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(Mekanism.GlowstoneLegs));
					event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(Mekanism.GlowstoneBoots));
				}
				else if(secondChance == 1)
				{
					if(event.entityLiving instanceof EntityZombie) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(Mekanism.LazuliSword));
					event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(Mekanism.LazuliHelmet));
					event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(Mekanism.LazuliBody));
					event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(Mekanism.LazuliLegs));
					event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(Mekanism.LazuliBoots));
				}
				else if(secondChance == 2)
				{
					if(event.entityLiving instanceof EntityZombie) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(Mekanism.RedstoneSword));
					event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(Mekanism.RedstoneHelmet));
					event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(Mekanism.RedstoneBody));
					event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(Mekanism.RedstoneLegs));
					event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(Mekanism.RedstoneBoots));
				}
				else if(secondChance == 3)
				{
					if(event.entityLiving instanceof EntityZombie) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(Mekanism.PlatinumSword));
					event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(Mekanism.PlatinumHelmet));
					event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(Mekanism.PlatinumBody));
					event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(Mekanism.PlatinumLegs));
					event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(Mekanism.PlatinumBoots));
				}
			}
		}
	}
}