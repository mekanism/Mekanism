package net.uberkat.obsidian.common;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import net.minecraftforge.common.*;
import net.minecraft.src.*;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;

/**
 * Obsidian Ingots mod -- adds in Tools, Armor, Weapons, Machines, and Magic. Universal files.
 * @author AidanBrady
 *
 */
@Mod(modid = "ObsidianIngots", name = "Obsidian Ingots", version = "4.0.2")
@NetworkMod(channels = { "ObsidianIngots" }, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class ObsidianIngots
{
	/** Obsidian Ingots logger instance */
	public static Logger logger = Logger.getLogger("Minecraft");
	
	/** Obsidian Ingots proxy instance */
	@SidedProxy(clientSide = "net.uberkat.obsidian.client.ClientProxy", serverSide = "net.uberkat.obsidian.common.CommonProxy")
	public static CommonProxy proxy;
	
    /** Obsidian Ingots mod instance */
	@Instance
    public static ObsidianIngots instance;
    
    /** Obsidian Ingots hooks instance */
    public static ObsidianHooks hooks;
    
	//Initial Declarations
	public static Version versionNumber = new Version(4, 0, 1);
	public static String latestVersionNumber;
	public static String recentNews;
	public static String hostIP = "71.56.58.57";
	public static int hostPort = 3073;
	
    //Enums: Tools
    public static EnumToolMaterial toolOBSIDIAN = EnumHelper.addToolMaterial("OBSIDIAN", 3, 2500, 20F, 10, 50);
    public static EnumToolMaterial toolOBSIDIAN2 = EnumHelper.addToolMaterial("OBSIDIAN2", 3, 3000, 25F, 10, 100);
    public static EnumToolMaterial toolLAZULI = EnumHelper.addToolMaterial("LAZULI", 2, 200, 5.0F, 0, 22);
    public static EnumToolMaterial toolLAZULI2 = EnumHelper.addToolMaterial("LAZULI2", 2, 250, 6.0F, 4, 50);
    public static EnumToolMaterial toolPLATINUM = EnumHelper.addToolMaterial("PLATINUM", 3, 800, 15F, 5, 30);
    public static EnumToolMaterial toolPLATINUM2 = EnumHelper.addToolMaterial("PLATINUM2", 3, 1000, 20F, 5, 60);
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
	public static int platinumOreID = 200;
	public static int platinumBlockID = 201;
	public static int redstoneBlockID = 202;
	public static int obsidianTNTID = 203;
	public static int refinedObsidianID = 204;
	public static int elementizerID = 205;
	public static int enrichmentChamberID = 206;
	public static int platinumCompressorID = 207;
	public static int combinerID = 208;
	public static int crusherID = 209;
	public static int coalBlockID = 210;
	public static int refinedGlowstoneID = 211;
	
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
	public static Item IronDust;
	public static Item GoldDust;
	
	//Glowstone Items
	public static Block RefinedGlowstone;
	public static Item GlowstoneIngot;
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
	public static Block RedstoneBlock;
	public static Item RedstoneIngot;
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
	public static Block PlatinumOre;
	public static Block PlatinumBlock;
	public static Item PlatinumDust;
	public static Item PlatinumIngot;
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
	public static Block RefinedObsidian;
	public static Item ObsidianDust;
	public static Item ObsidianHelmet;
	public static Item ObsidianBody;
	public static Item ObsidianLegs;
	public static Item ObsidianBoots;
	public static Item ObsidianIngot;
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
	public static Item ObsidianArrow;
	public static Item ObsidianBow;
	public static Item LightningRod;
	public static Item Stopwatch;
	public static Item WeatherOrb;
	public static Item EnrichedAlloy;
	
	//Extra Blocks
	public static Block ObsidianTNT;
	public static Block TheoreticalElementizer;
	public static Block EnrichmentChamber;
	public static Block PlatinumCompressor;
	public static Block Combiner;
	public static Block Crusher;
	public static Block CoalBlock;
	
	//Boolean Values
	public static boolean extrasEnabled = true;
	public static boolean oreGenerationEnabled = true;
	
	//Extra data
	public static float ObsidianTNTBlastRadius = 12.0F;
	public static int ObsidianTNTDelay = 100;
	
	/**
	 * Total ticks passed since thePlayer joined theWorld
	 */
	public static int ticksPassed = 0;
	
	/**
	 * Adds all in-game crafting and smelting recipes.
	 */
	public void addRecipes()
	{
		//Crafting Recipes
		//Base
		GameRegistry.addRecipe(new ItemStack(WoodPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Item.axeWood, Character.valueOf('Y'), Item.pickaxeWood, Character.valueOf('Z'), Item.shovelWood, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(StonePaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Item.axeStone, Character.valueOf('Y'), Item.pickaxeStone, Character.valueOf('Z'), Item.shovelStone, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(IronPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Item.axeSteel, Character.valueOf('Y'), Item.pickaxeSteel, Character.valueOf('Z'), Item.shovelSteel, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(DiamondPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Item.axeDiamond, Character.valueOf('Y'), Item.pickaxeDiamond, Character.valueOf('Z'), Item.shovelDiamond, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(GoldPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), Item.axeGold, Character.valueOf('Y'), Item.pickaxeGold, Character.valueOf('Z'), Item.shovelGold, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(WoodKnife), new Object[] {
			" ^", "I ", Character.valueOf('^'), Block.planks, Character.valueOf('I'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(StoneKnife), new Object[] {
			" ^", "I ", Character.valueOf('^'), Block.cobblestone, Character.valueOf('I'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(IronKnife), new Object[] {
			" ^", "I ", Character.valueOf('^'), Item.ingotIron, Character.valueOf('I'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(DiamondKnife), new Object[] {
			" ^", "I ", Character.valueOf('^'), Item.diamond, Character.valueOf('I'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(GoldKnife), new Object[] {
			" ^", "I ", Character.valueOf('^'), Item.ingotGold, Character.valueOf('I'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(Item.coal, 9), new Object[] {
			"*", Character.valueOf('*'), CoalBlock
		});
		GameRegistry.addRecipe(new ItemStack(CoalBlock, 1), new Object[] {
			"***", "***", "***", Character.valueOf('*'), Item.coal
		});
		
		//Obsidian
		GameRegistry.addRecipe(new ItemStack(RefinedObsidian, 0), new Object[] {
			"***", "***", "***", Character.valueOf('*'), ObsidianIngot
		});
		GameRegistry.addRecipe(new ItemStack(ObsidianIngot, 9), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(RefinedObsidian, 0)		
		});
		GameRegistry.addRecipe(new ItemStack(ObsidianHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), ObsidianIngot
		});
		GameRegistry.addRecipe(new ItemStack(ObsidianBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), ObsidianIngot
		});
		GameRegistry.addRecipe(new ItemStack(ObsidianLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), ObsidianIngot
		});
		GameRegistry.addRecipe(new ItemStack(ObsidianBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), ObsidianIngot
		});
		GameRegistry.addRecipe(new ItemStack(ObsidianPaxel, 1), new Object[]{
			"XYZ", " T ", " T ", Character.valueOf('X'), ObsidianAxe, Character.valueOf('Y'), ObsidianPickaxe, Character.valueOf('Z'), ObsidianSpade, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(ObsidianPickaxe, 1), new Object[]{
			"XXX", " T ", " T ", Character.valueOf('X'), ObsidianIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(ObsidianAxe, 1), new Object[]{
			"XX", "XT", " T", Character.valueOf('X'), ObsidianIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(ObsidianSpade, 1), new Object[]{
			"X", "T", "T", Character.valueOf('X'), ObsidianIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(ObsidianHoe, 1), new Object[]{
			"XX", " T", " T", Character.valueOf('X'), ObsidianIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(ObsidianSword, 1), new Object[]{
			"X", "X", "T", Character.valueOf('X'), ObsidianIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(ObsidianKnife, 1), new Object[] {
			" ^", "I ", Character.valueOf('^'), ObsidianIngot, Character.valueOf('I'), Item.stick
		});
		
		//Glowstone
		GameRegistry.addRecipe(new ItemStack(RefinedGlowstone, 1), new Object[] {
			"***", "***", "***", Character.valueOf('*'), GlowstoneIngot
		});
		GameRegistry.addRecipe(new ItemStack(GlowstoneIngot, 9), new Object[] {
			"*", Character.valueOf('*'), RefinedGlowstone
		});
		GameRegistry.addRecipe(new ItemStack(GlowstonePaxel, 1), new Object[]{
			"XYZ", " T ", " T ", Character.valueOf('X'), GlowstoneAxe, Character.valueOf('Y'), GlowstonePickaxe, Character.valueOf('Z'), GlowstoneSpade, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(GlowstonePickaxe, 1), new Object[]{
			"XXX", " T ", " T ", Character.valueOf('X'), GlowstoneIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(GlowstoneAxe, 1), new Object[]{
			"XX", "XT", " T", Character.valueOf('X'), GlowstoneIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(GlowstoneSpade, 1), new Object[]{
			"X", "T", "T", Character.valueOf('X'), GlowstoneIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(GlowstoneHoe, 1), new Object[]{
			"XX", " T", " T", Character.valueOf('X'), GlowstoneIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(GlowstoneSword, 1), new Object[]{
			"X", "X", "T", Character.valueOf('X'), GlowstoneIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(GlowstoneHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), GlowstoneIngot
		});
		GameRegistry.addRecipe(new ItemStack(GlowstoneBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), GlowstoneIngot
		});
		GameRegistry.addRecipe(new ItemStack(GlowstoneLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), GlowstoneIngot
		});
		GameRegistry.addRecipe(new ItemStack(GlowstoneBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), GlowstoneIngot
		});
		GameRegistry.addRecipe(new ItemStack(GlowstoneKnife, 1), new Object[] {
			" ^", "I ", Character.valueOf('^'), GlowstoneIngot, Character.valueOf('I'), Item.stick
		});
		
		//Lazuli
		GameRegistry.addRecipe(new ItemStack(LazuliHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), new ItemStack(Item.dyePowder, 1, 4)
		});
		GameRegistry.addRecipe(new ItemStack(LazuliBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), new ItemStack(Item.dyePowder, 1, 4)
		});
		GameRegistry.addRecipe(new ItemStack(LazuliLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), new ItemStack(Item.dyePowder, 1, 4)
		});
		GameRegistry.addRecipe(new ItemStack(LazuliBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), new ItemStack(Item.dyePowder, 1, 4)
		});
		GameRegistry.addRecipe(new ItemStack(LazuliPaxel, 1), new Object[]{
			"XYZ", " T ", " T ", Character.valueOf('X'), LazuliAxe, Character.valueOf('Y'), LazuliPickaxe, Character.valueOf('Z'), LazuliSpade, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(LazuliPickaxe, 1), new Object[]{
			"XXX", " T ", " T ", Character.valueOf('X'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(LazuliAxe, 1), new Object[]{
			"XX", "XT", " T", Character.valueOf('X'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(LazuliSpade, 1), new Object[]{
			"X", "T", "T", Character.valueOf('X'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(LazuliHoe, 1), new Object[]{
			"XX", " T", " T", Character.valueOf('X'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(LazuliSword, 1), new Object[]{
			"X", "X", "T", Character.valueOf('X'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(LazuliKnife, 1), new Object[] {
			" ^", "I ", Character.valueOf('^'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('I'), Item.stick
		});
		
		//Platinum
		GameRegistry.addRecipe(new ItemStack(PlatinumBlock, 1), new Object[]{
			"XXX", "XXX", "XXX", Character.valueOf('X'), PlatinumIngot
		});
		GameRegistry.addRecipe(new ItemStack(PlatinumPaxel, 1), new Object[]{
			"XYZ", " T ", " T ", Character.valueOf('X'), PlatinumAxe, Character.valueOf('Y'), PlatinumPickaxe, Character.valueOf('Z'), PlatinumSpade, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(PlatinumPickaxe, 1), new Object[]{
			"XXX", " T ", " T ", Character.valueOf('X'), PlatinumIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(PlatinumAxe, 1), new Object[]{
			"XX", "XT", " T", Character.valueOf('X'), PlatinumIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(PlatinumSpade, 1), new Object[]{
			"X", "T", "T", Character.valueOf('X'), PlatinumIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(PlatinumHoe, 1), new Object[]{
			"XX", " T", " T", Character.valueOf('X'), PlatinumIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(PlatinumSword, 1), new Object[]{
			"X", "X", "T", Character.valueOf('X'), PlatinumIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(PlatinumHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), PlatinumIngot
		});
		GameRegistry.addRecipe(new ItemStack(PlatinumBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), PlatinumIngot
		});
		GameRegistry.addRecipe(new ItemStack(PlatinumLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), PlatinumIngot
		});
		GameRegistry.addRecipe(new ItemStack(PlatinumBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), PlatinumIngot
		});
		GameRegistry.addRecipe(new ItemStack(PlatinumIngot, 9), new Object[] {
			"*", Character.valueOf('*'), PlatinumBlock
		});
		GameRegistry.addRecipe(new ItemStack(PlatinumKnife, 1), new Object[] {
			" ^", "I ", Character.valueOf('^'), PlatinumIngot, Character.valueOf('I'), Item.stick
		});
		
		//Redstone
		GameRegistry.addRecipe(new ItemStack(RedstoneBlock, 1), new Object[] {
			"***", "***", "***", Character.valueOf('*'), RedstoneIngot
		});
		GameRegistry.addRecipe(new ItemStack(RedstoneIngot, 9), new Object[] {
			"*", Character.valueOf('*'), RedstoneBlock
		});
		GameRegistry.addRecipe(new ItemStack(RedstonePaxel, 1), new Object[]{
			"XYZ", " T ", " T ", Character.valueOf('X'), RedstoneAxe, Character.valueOf('Y'), RedstonePickaxe, Character.valueOf('Z'), RedstoneSpade, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(RedstonePickaxe, 1), new Object[]{
			"XXX", " T ", " T ", Character.valueOf('X'), RedstoneIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(RedstoneAxe, 1), new Object[]{
			"XX", "XT", " T", Character.valueOf('X'), RedstoneIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(RedstoneSpade, 1), new Object[]{
			"X", "T", "T", Character.valueOf('X'), RedstoneIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(RedstoneHoe, 1), new Object[]{
			"XX", " T", " T", Character.valueOf('X'), RedstoneIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(RedstoneSword, 1), new Object[]{
			"X", "X", "T", Character.valueOf('X'), RedstoneIngot, Character.valueOf('T'), Item.stick
		});
		GameRegistry.addRecipe(new ItemStack(RedstoneHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), RedstoneIngot
		});
		GameRegistry.addRecipe(new ItemStack(RedstoneBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), RedstoneIngot
		});
		GameRegistry.addRecipe(new ItemStack(RedstoneLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), RedstoneIngot
		});
		GameRegistry.addRecipe(new ItemStack(RedstoneBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), RedstoneIngot
		});
		GameRegistry.addRecipe(new ItemStack(RedstoneKnife, 1), new Object[] {
			" ^", "I ", Character.valueOf('^'), RedstoneIngot, Character.valueOf('I'), Item.stick
		});
		
		//Extra
		GameRegistry.addRecipe(new ItemStack(ObsidianTNT, 1), new Object[] {
			"***", "XXX", "***", Character.valueOf('*'), Block.obsidian, Character.valueOf('X'), Block.tnt
		});
		GameRegistry.addRecipe(new ItemStack(ObsidianArrow, 8), new Object[] {
			"A", "B", "C", Character.valueOf('A'), ObsidianIngot, Character.valueOf('B'), Item.stick, Character.valueOf('C'), Item.feather
		});
		GameRegistry.addRecipe(new ItemStack(ObsidianBow, 1), new Object[] {
			" AB", "A B", " AB", Character.valueOf('A'), ObsidianIngot, Character.valueOf('B'), Item.silk
		});
		if(extrasEnabled)
		{
			GameRegistry.addRecipe(new ItemStack(TheoreticalElementizer, 1), new Object[] {
				"SGS", "GDG", "SGS", Character.valueOf('S'), Block.stone, Character.valueOf('G'), Block.glass, Character.valueOf('D'), Block.blockDiamond
			});
			GameRegistry.addShapelessRecipe(new ItemStack(EnrichedAlloy, 1), new Object[] {
				Item.redstone, Item.lightStoneDust, IronDust, GoldDust, ObsidianDust, PlatinumDust
			});
		}
		GameRegistry.addRecipe(new ItemStack(PlatinumCompressor, 1), new Object[] {
			"***", "*P*", "***", Character.valueOf('*'), Item.redstone, Character.valueOf('P'), PlatinumBlock
		});
		GameRegistry.addRecipe(new ItemStack(EnrichmentChamber, 1), new Object[] {
			"***", "*R*", "***", Character.valueOf('*'), PlatinumIngot, Character.valueOf('R'), Item.redstone
		});
		GameRegistry.addRecipe(new ItemStack(Combiner, 1), new Object[] {
			"***", "*P*", "***", Character.valueOf('*'), Block.cobblestone, Character.valueOf('P'), PlatinumBlock
		});
		GameRegistry.addRecipe(new ItemStack(Crusher, 1), new Object[] {
			"***", "*L*", "***", Character.valueOf('*'), PlatinumIngot, Character.valueOf('L'), Item.bucketLava
		});
	
		//Smelting
		GameRegistry.addSmelting(platinumOreID, new ItemStack(PlatinumIngot), 1.0F);
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
		LanguageRegistry.addName(IronDust, "Iron Dust");
		LanguageRegistry.addName(GoldDust, "Gold Dust");
		
		//Obsidian
		LanguageRegistry.addName(RefinedObsidian, "Refined Obsidian");
		LanguageRegistry.addName(ObsidianDust, "Obsidian Dust");
		LanguageRegistry.addName(ObsidianHelmet, "Obsidian Helmet");
		LanguageRegistry.addName(ObsidianBody, "Obsidian Chestplate");
		LanguageRegistry.addName(ObsidianLegs, "Obsidian Leggings");
		LanguageRegistry.addName(ObsidianBoots, "Obsidian Boots");
		LanguageRegistry.addName(ObsidianIngot, "Obsidian Ingot");
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
		LanguageRegistry.addName(PlatinumOre, "Platinum Ore");
		LanguageRegistry.addName(PlatinumBlock, "Platinum Block");
		LanguageRegistry.addName(PlatinumDust, "Platinum Dust");
		LanguageRegistry.addName(PlatinumHelmet, "Platinum Helmet");
		LanguageRegistry.addName(PlatinumBody, "Platinum Chestplate");
		LanguageRegistry.addName(PlatinumLegs, "Platinum Leggings");
		LanguageRegistry.addName(PlatinumBoots, "Platinum Boots");
		LanguageRegistry.addName(PlatinumIngot, "Platinum Ingot");
		LanguageRegistry.addName(PlatinumPaxel, "Platinum Paxel");
		LanguageRegistry.addName(PlatinumPickaxe, "Platinum Pickaxe");
		LanguageRegistry.addName(PlatinumAxe, "Platinum Axe");
		LanguageRegistry.addName(PlatinumSpade, "Platinum Shovel");
		LanguageRegistry.addName(PlatinumHoe, "Platinum Hoe");
		LanguageRegistry.addName(PlatinumSword, "Platinum Sword");
		LanguageRegistry.addName(PlatinumKnife, "Platinum Knife");
		
		//Redstone
		LanguageRegistry.addName(RedstoneBlock, "Redstone Block");
		LanguageRegistry.addName(RedstoneBlock, "Redstone Block");
		LanguageRegistry.addName(RedstoneHelmet, "Redstone Helmet");
		LanguageRegistry.addName(RedstoneBody, "Redstone Chestplate");
		LanguageRegistry.addName(RedstoneLegs, "Redstone Leggings");
		LanguageRegistry.addName(RedstoneBoots, "Redstone Boots");
		LanguageRegistry.addName(RedstoneIngot, "Redstone Ingot");
		LanguageRegistry.addName(RedstonePaxel, "Redstone Paxel");
		LanguageRegistry.addName(RedstonePickaxe, "Redstone Pickaxe");
		LanguageRegistry.addName(RedstoneAxe, "Redstone Axe");
		LanguageRegistry.addName(RedstoneSpade, "Redstone Shovel");
		LanguageRegistry.addName(RedstoneHoe, "Redstone Hoe");
		LanguageRegistry.addName(RedstoneSword, "Redstone Sword");
		LanguageRegistry.addName(RedstoneBlock, "Redstone Block");
		LanguageRegistry.addName(RedstoneKnife, "Redstone Knife");	
		
		//Glowstone
		LanguageRegistry.addName(RefinedGlowstone, "Refined Glowstone Block");
		LanguageRegistry.addName(GlowstoneIngot, "Glowstone Ingot");
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
		LanguageRegistry.addName(ObsidianArrow, "Obsidian Arrow");
		LanguageRegistry.addName(ObsidianBow, "Obsidian Bow");
		LanguageRegistry.addName(ObsidianTNT, "Obsidian TNT");
		if(extrasEnabled == true)
		{
			LanguageRegistry.addName(LightningRod, "Lightning Rod");
			LanguageRegistry.addName(Stopwatch, "Steve's Stopwatch");
			LanguageRegistry.addName(TheoreticalElementizer, "Theoretical Elementizer");
			LanguageRegistry.addName(WeatherOrb, "Weather Orb");
			LanguageRegistry.addName(EnrichedAlloy, "Enriched Alloy");
		}
		LanguageRegistry.addName(EnrichmentChamber, "Enrichment Chamber");
		LanguageRegistry.addName(PlatinumCompressor, "Platinum Compressor");
		LanguageRegistry.addName(Combiner, "Combiner");
		LanguageRegistry.addName(Crusher, "Crusher");
		LanguageRegistry.addName(CoalBlock, "Coal Block");
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
		IronDust.setIconIndex(248);
		GoldDust.setIconIndex(250);
		
		//Glowstone
		GlowstoneHelmet.setIconIndex(4);
		GlowstoneBody.setIconIndex(20);
		GlowstoneLegs.setIconIndex(36);
		GlowstoneBoots.setIconIndex(52);
		GlowstoneIngot.setIconIndex(164);
		GlowstoneIngot.setIconIndex(164);
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
		RedstoneIngot.setIconIndex(163);
		RedstonePaxel.setIconIndex(147);
		RedstonePickaxe.setIconIndex(67);
		RedstoneAxe.setIconIndex(83);
		RedstoneSpade.setIconIndex(99);
		RedstoneHoe.setIconIndex(115);
		RedstoneSword.setIconIndex(131);
		RedstoneKnife.setIconIndex(211);
		
		//Platinum
		PlatinumDust.setIconIndex(242);
		PlatinumHelmet.setIconIndex(2);
		PlatinumBody.setIconIndex(18);
		PlatinumLegs.setIconIndex(34);
		PlatinumBoots.setIconIndex(50);
		PlatinumIngot.setIconIndex(162);
		PlatinumPaxel.setIconIndex(146);
		PlatinumPickaxe.setIconIndex(66);
		PlatinumAxe.setIconIndex(82);
		PlatinumSpade.setIconIndex(98);
		PlatinumHoe.setIconIndex(114);
		PlatinumSword.setIconIndex(130);
		PlatinumKnife.setIconIndex(210);
		
		//Obsidian
		ObsidianDust.setIconIndex(241);
		ObsidianHelmet.setIconIndex(1);
		ObsidianBody.setIconIndex(17);
		ObsidianLegs.setIconIndex(33);
		ObsidianBoots.setIconIndex(49);
		ObsidianIngot.setIconIndex(161);
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
		
		//Extras
		ObsidianArrow.setIconIndex(193);
		ObsidianBow.setIconIndex(177);
		if(extrasEnabled == true)
		{
			LightningRod.setIconIndex(225);
			Stopwatch.setIconIndex(224);
			WeatherOrb.setIconIndex(226);
			EnrichedAlloy.setIconIndex(227);
		}
	}
	/**
	 * Adds and registers all items.
	 */
	public void addItems()
	{
		RedstoneHelmet = (new ItemObsidianArmor(11235, armorREDSTONE, RenderingRegistry.addNewArmourRendererPrefix("redstone"), 0)).setItemName("RedstoneHelmet");
		RedstoneBody = (new ItemObsidianArmor(11236, armorREDSTONE, RenderingRegistry.addNewArmourRendererPrefix("redstone"), 1)).setItemName("RedstoneBody");
		RedstoneLegs = (new ItemObsidianArmor(11237, armorREDSTONE, RenderingRegistry.addNewArmourRendererPrefix("redstone"), 2)).setItemName("RedstoneLegs");
		RedstoneBoots = (new ItemObsidianArmor(11238, armorREDSTONE, RenderingRegistry.addNewArmourRendererPrefix("redstone"), 3)).setItemName("RedstoneBoots");
		RedstoneIngot = new ItemObsidian(11239).setItemName("RedstoneIngot").setTabToDisplayOn(CreativeTabs.tabMaterials);
		RedstonePaxel = new ItemObsidianPaxel(11240, toolREDSTONE2).setItemName("RedstonePaxel");
		RedstonePickaxe = new ItemObsidianPickaxe(11241, toolREDSTONE).setItemName("RedstonePickaxe");
		RedstoneAxe = new ItemObsidianAxe(11242, toolREDSTONE).setItemName("RedstoneAxe");
		RedstoneSpade = new ItemObsidianSpade(11243, toolREDSTONE).setItemName("RedstoneSpade");
		RedstoneHoe = new ItemObsidianHoe(11244, toolREDSTONE).setItemName("RedstoneHoe");
		RedstoneSword = new ItemObsidianSword(11245, toolREDSTONE).setItemName("RedstoneSword");
		PlatinumHelmet = (new ItemObsidianArmor(11246, EnumArmorMaterial.DIAMOND, RenderingRegistry.addNewArmourRendererPrefix("platinum"), 0)).setItemName("PlatinumHelmet");
		PlatinumBody = (new ItemObsidianArmor(11247, EnumArmorMaterial.DIAMOND, RenderingRegistry.addNewArmourRendererPrefix("platinum"), 1)).setItemName("PlatinumBody");
		PlatinumLegs = (new ItemObsidianArmor(11248, EnumArmorMaterial.DIAMOND, RenderingRegistry.addNewArmourRendererPrefix("platinum"), 2)).setItemName("PlatinumLegs");
		PlatinumBoots = (new ItemObsidianArmor(11249, EnumArmorMaterial.DIAMOND, RenderingRegistry.addNewArmourRendererPrefix("platinum"), 3)).setItemName("PlatinumBoots");
		PlatinumIngot = new ItemObsidian(11250).setItemName("PlatinumIngot").setTabToDisplayOn(CreativeTabs.tabMaterials);
		PlatinumPaxel = new ItemObsidianPaxel(11251, toolPLATINUM2).setItemName("PlatinumPaxel");
		PlatinumPickaxe = new ItemObsidianPickaxe(11252, toolPLATINUM).setItemName("PlatinumPickaxe");
		PlatinumAxe = new ItemObsidianAxe(11253, toolPLATINUM).setItemName("PlatinumAxe");
		PlatinumSpade = new ItemObsidianSpade(11254, toolPLATINUM).setItemName("PlatinumSpade");
		PlatinumHoe = new ItemObsidianHoe(11255, toolPLATINUM).setItemName("PlatinumHoe");
		PlatinumSword = new ItemObsidianSword(11256, toolPLATINUM).setItemName("PlatinumSword");
		ObsidianHelmet = (new ItemObsidianArmor(11257, armorOBSIDIAN, RenderingRegistry.addNewArmourRendererPrefix("obsidian"), 0)).setItemName("ObsidianHelmet");
		ObsidianBody = (new ItemObsidianArmor(11258, armorOBSIDIAN, RenderingRegistry.addNewArmourRendererPrefix("obsidian"), 1)).setItemName("ObsidianBody");
		ObsidianLegs = (new ItemObsidianArmor(11259, armorOBSIDIAN, RenderingRegistry.addNewArmourRendererPrefix("obsidian"), 2)).setItemName("ObsidianLegs");
		ObsidianBoots = (new ItemObsidianArmor(11260, armorOBSIDIAN, RenderingRegistry.addNewArmourRendererPrefix("obsidian"), 3)).setItemName("ObsidianBoots");
		ObsidianIngot = new ItemObsidian(11261).setItemName("ObsidianIngot").setTabToDisplayOn(CreativeTabs.tabMaterials);
		ObsidianPaxel = new ItemObsidianPaxel(11262, toolOBSIDIAN2).setItemName("ObsidianPaxel");
		ObsidianPickaxe = new ItemObsidianPickaxe(11263, toolOBSIDIAN).setItemName("ObsidianPickaxe");
		ObsidianAxe = new ItemObsidianAxe(11264, toolOBSIDIAN).setItemName("ObsidianAxe");
		ObsidianSpade = new ItemObsidianSpade(11265, toolOBSIDIAN).setItemName("ObsidianSpade");
		ObsidianHoe = new ItemObsidianHoe(11266, toolOBSIDIAN).setItemName("ObsidianHoe");
		ObsidianSword = new ItemObsidianSword(11267, toolOBSIDIAN).setItemName("ObsidianSword");
		LazuliPaxel = new ItemObsidianPaxel(11268, toolLAZULI2).setItemName("LazuliPaxel");
		LazuliPickaxe = new ItemObsidianPickaxe(11269, toolLAZULI).setItemName("LazuliPickaxe");
		LazuliAxe = new ItemObsidianAxe(11270, toolLAZULI).setItemName("LazuliAxe");
		LazuliSpade = new ItemObsidianSpade(11271, toolLAZULI).setItemName("LazuliSpade");
		LazuliHoe = new ItemObsidianHoe(11272, toolLAZULI).setItemName("LazuliHoe");
		LazuliSword = new ItemObsidianSword(11273, toolLAZULI).setItemName("LazuliSword");
		LazuliHelmet = (new ItemObsidianArmor(11274, armorLAZULI, RenderingRegistry.addNewArmourRendererPrefix("lazuli"), 0)).setItemName("LazuliHelmet");
		LazuliBody = (new ItemObsidianArmor(11275, armorLAZULI, RenderingRegistry.addNewArmourRendererPrefix("lazuli"), 1)).setItemName("LazuliBody");
		LazuliLegs = (new ItemObsidianArmor(11276, armorLAZULI, RenderingRegistry.addNewArmourRendererPrefix("lazuli"), 2)).setItemName("LazuliLegs");
		LazuliBoots = (new ItemObsidianArmor(11277, armorLAZULI, RenderingRegistry.addNewArmourRendererPrefix("lazuli"), 3)).setItemName("LazuliBoots");
		ObsidianArrow = new ItemObsidian(11278).setItemName("ObsidianArrow").setTabToDisplayOn(CreativeTabs.tabCombat);
		ObsidianBow = new ItemObsidianBow(11279).setItemName("ObsidianBow");
		if(extrasEnabled == true)
		{
			LightningRod = new ItemLightningRod(11280).setItemName("LightningRod");
			Stopwatch = new ItemStopwatch(11281).setItemName("Stopwatch");
			WeatherOrb = new ItemWeatherOrb(11282).setItemName("WeatherOrb");
			EnrichedAlloy = new ItemObsidian(11313).setItemName("EnrichedAlloy").setTabToDisplayOn(CreativeTabs.tabMaterials);
		}
		WoodPaxel = new ItemObsidianPaxel(11283, EnumToolMaterial.WOOD).setItemName("WoodPaxel");
		StonePaxel = new ItemObsidianPaxel(11284, EnumToolMaterial.STONE).setItemName("StonePaxel");
		IronPaxel = new ItemObsidianPaxel(11285, EnumToolMaterial.IRON).setItemName("IronPaxel");
		DiamondPaxel = new ItemObsidianPaxel(11286, EnumToolMaterial.EMERALD).setItemName("DiamondPaxel");
		GoldPaxel = new ItemObsidianPaxel(11287, EnumToolMaterial.GOLD).setItemName("GoldPaxel");
		WoodKnife = new ItemObsidianKnife(11288, EnumToolMaterial.WOOD).setItemName("WoodKnife");
		StoneKnife = new ItemObsidianKnife(11289, EnumToolMaterial.STONE).setItemName("StoneKnife");
		IronKnife = new ItemObsidianKnife(11290, EnumToolMaterial.IRON).setItemName("IronKnife");
		DiamondKnife = new ItemObsidianKnife(11291, EnumToolMaterial.EMERALD).setItemName("DiamondKnife");
		GoldKnife = new ItemObsidianKnife(11292, EnumToolMaterial.GOLD).setItemName("GoldKnife");
		ObsidianKnife = new ItemObsidianKnife(11293, toolOBSIDIAN).setItemName("ObsidianKnife");
		LazuliKnife = new ItemObsidianKnife(11294, toolLAZULI).setItemName("LazuliKnife");
		PlatinumKnife = new ItemObsidianKnife(11295, toolPLATINUM).setItemName("PlatinumKnife");
		RedstoneKnife = new ItemObsidianKnife(11296, toolREDSTONE).setItemName("RedstoneKnife");
		ObsidianDust = new ItemObsidian(11297).setItemName("ObsidianDust").setTabToDisplayOn(CreativeTabs.tabMaterials);
		IronDust = new ItemObsidian(11298).setItemName("IronDust").setTabToDisplayOn(CreativeTabs.tabMaterials);
		GoldDust = new ItemObsidian(11299).setItemName("GoldDust").setTabToDisplayOn(CreativeTabs.tabMaterials);
		PlatinumDust = new ItemObsidian(11300).setItemName("PlatinumDust").setTabToDisplayOn(CreativeTabs.tabMaterials);
		GlowstoneIngot = new ItemObsidian(11301).setItemName("GlowstoneIngot").setTabToDisplayOn(CreativeTabs.tabMaterials);
		GlowstonePaxel = new ItemObsidianPaxel(11302, toolGLOWSTONE2).setItemName("GlowstonePaxel");
		GlowstonePickaxe = new ItemObsidianPickaxe(11303, toolGLOWSTONE).setItemName("GlowstonePickaxe");
		GlowstoneAxe = new ItemObsidianAxe(11304, toolGLOWSTONE).setItemName("GlowstoneAxe");
		GlowstoneSpade = new ItemObsidianSpade(11305, toolGLOWSTONE).setItemName("GlowstoneSpade");
		GlowstoneHoe = new ItemObsidianHoe(11306, toolGLOWSTONE).setItemName("GlowstoneHoe");
		GlowstoneSword = new ItemObsidianSword(11307, toolGLOWSTONE).setItemName("GlowstoneSword");
		GlowstoneHelmet = new ItemObsidianArmor(11308, armorGLOWSTONE, RenderingRegistry.addNewArmourRendererPrefix("glowstone"), 0).setItemName("GlowstoneHelmet");
		GlowstoneBody = new ItemObsidianArmor(11309, armorGLOWSTONE, RenderingRegistry.addNewArmourRendererPrefix("glowstone"), 1).setItemName("GlowstoneBody");
		GlowstoneLegs = new ItemObsidianArmor(11310, armorGLOWSTONE, RenderingRegistry.addNewArmourRendererPrefix("glowstone"), 2).setItemName("GlowstoneLegs");
		GlowstoneBoots = new ItemObsidianArmor(11311, armorGLOWSTONE, RenderingRegistry.addNewArmourRendererPrefix("glowstone"), 3).setItemName("GlowstoneBoots");
		GlowstoneKnife = new ItemObsidianKnife(11312, toolGLOWSTONE).setItemName("GlowstoneKnife");

	}
	/**
	 * Adds and registers all blocks.
	 */
	public void addBlocks()
	{
		//Declarations
		RedstoneBlock = new BlockBase(redstoneBlockID, 1).setBlockName("RedstoneBlock").setCreativeTab(CreativeTabs.tabBlock);
		PlatinumOre = new BlockBase(platinumOreID, 3).setBlockName("PlatinumOre").setCreativeTab(CreativeTabs.tabBlock);
		PlatinumBlock = new BlockBase(platinumBlockID, 2).setBlockName("PlatinumBlock").setCreativeTab(CreativeTabs.tabBlock);
		RefinedObsidian = new BlockBase(refinedObsidianID, 0).setBlockName("RefinedObsidian").setCreativeTab(CreativeTabs.tabBlock);
		ObsidianTNT = new BlockObsidianTNT(obsidianTNTID).setBlockName("ObsidianTNT").setCreativeTab(CreativeTabs.tabBlock);
		if(extrasEnabled == true)
		{
			TheoreticalElementizer = new BlockTheoreticalElementizer(elementizerID).setBlockName("TheoreticalElementizer").setCreativeTab(CreativeTabs.tabBlock);
		}
		EnrichmentChamber = new BlockEnrichmentChamber(enrichmentChamberID).setBlockName("EnrichmentChamberIdle").setCreativeTab(CreativeTabs.tabBlock);
		PlatinumCompressor = new BlockPlatinumCompressor(platinumCompressorID).setBlockName("PlatinumCompressorIdle").setCreativeTab(CreativeTabs.tabBlock);
		Combiner = new BlockCombiner(combinerID).setBlockName("CombinerIdle").setCreativeTab(CreativeTabs.tabBlock);
		Crusher = new BlockCrusher(crusherID).setBlockName("CrusherIdle").setCreativeTab(CreativeTabs.tabBlock);
		CoalBlock = new BlockBase(coalBlockID, 10).setBlockName("CoalBlock").setCreativeTab(CreativeTabs.tabBlock);
		RefinedGlowstone = new BlockBase(refinedGlowstoneID, 11).setBlockName("RefinedGlowstone").setCreativeTab(CreativeTabs.tabBlock);
		
		//Registrations
		GameRegistry.registerBlock(RedstoneBlock);
		GameRegistry.registerBlock(PlatinumOre);
		GameRegistry.registerBlock(PlatinumBlock);
		GameRegistry.registerBlock(RefinedObsidian);
		GameRegistry.registerBlock(ObsidianTNT);
		if(extrasEnabled == true)
		{
			GameRegistry.registerBlock(TheoreticalElementizer);
		}
		GameRegistry.registerBlock(EnrichmentChamber);
		GameRegistry.registerBlock(PlatinumCompressor);
		GameRegistry.registerBlock(Combiner);
		GameRegistry.registerBlock(Crusher);
		GameRegistry.registerBlock(CoalBlock);
		GameRegistry.registerBlock(RefinedGlowstone);
	}
	/**
	 * Adds and registers all entities and tile entities.
	 */
	public void addEntities()
	{
		//Entity IDs
		EntityRegistry.registerGlobalEntityID(EntityObsidianArrow.class, "ObsidianArrow", EntityRegistry.findGlobalUniqueEntityId());
		EntityRegistry.registerGlobalEntityID(EntityObsidianTNT.class, "ObsidianTNT", EntityRegistry.findGlobalUniqueEntityId());
		EntityRegistry.registerGlobalEntityID(EntityKnife.class, "Knife", EntityRegistry.findGlobalUniqueEntityId());
		
		//Registrations
		EntityRegistry.registerModEntity(EntityObsidianArrow.class, "ObsidianArrow", 50, this, 40, 5, true);
		EntityRegistry.registerModEntity(EntityObsidianTNT.class, "ObsidianTNT", 51, this, 40, 5, true);
		EntityRegistry.registerModEntity(EntityKnife.class, "Knife", 52, this, 40, 5, true);
		
		//Tile entities
		GameRegistry.registerTileEntity(TileEntityEnrichmentChamber.class, "EnrichmentChamber");
		GameRegistry.registerTileEntity(TileEntityPlatinumCompressor.class, "PlatinumCompressor");
		GameRegistry.registerTileEntity(TileEntityCombiner.class, "Combiner");
		GameRegistry.registerTileEntity(TileEntityCrusher.class, "Crusher");
		GameRegistry.registerTileEntity(TileEntityTheoreticalElementizer.class, "TheoreticalElementizer");
	}
	
	@PreInit
	public void load(FMLPreInitializationEvent event) 
	{
		//Register the mod's ore handler
		GameRegistry.registerWorldGenerator(new OreHandler());
		//Register the mod's GUI handler
		NetworkRegistry.instance().registerGuiHandler(this, new CommonGuiHandler());
		//Set the mod's instance
		instance = this;
		System.out.println("[ObsidianIngots] Version " + versionNumber + " initializing...");
		proxy.registerRenderInformation();
		proxy.setProperties();
		proxy.loadUtilities();
		proxy.loadTickHandler();
		
		//Hook with mods Obsidian Ingots has implemented
		hooks = new ObsidianHooks();
		hooks.hook();
		System.out.println("[ObsidianIngots] Hooking complete.");
	
		//Add all items
		addItems();
		System.out.println("[ObsidianIngots] Items loaded.");
		
		//Add all blocks
		addBlocks();
		System.out.println("[ObsidianIngots] Blocks loaded.");
		
		//Set item and block names
		addNames();
		System.out.println("[ObsidianIngots] Names loaded.");
		
		//Set item and block textures
		addTextures();
		System.out.println("[ObsidianIngots] Textures loaded.");
		
		//Set item and block recipes
		addRecipes();
		System.out.println("[ObsidianIngots] Recipes loaded.");
		
		//Set up entities to run on SSP and SMP
		addEntities();
		System.out.println("[ObsidianIngots] Entities loaded.");
		
		//Success message
		logger.info("[ObsidianIngots] Mod loaded.");
	}
}