package mekanism.tools.common;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.src.*;
import net.minecraftforge.common.EnumHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingSpecialSpawnEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import mekanism.common.Mekanism;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "MekanismTools", name = "MekanismTools", version = "5.4.0", dependencies = "required-after:Mekanism")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class MekanismTools 
{
	@Instance("MekanismTools")
	public static MekanismTools instance;
	
    //Enums: Tools
    public static EnumToolMaterial toolOBSIDIAN = EnumHelper.addToolMaterial("OBSIDIAN", 3, 2500, 20F, 10, 100);
    public static EnumToolMaterial toolOBSIDIAN2 = EnumHelper.addToolMaterial("OBSIDIAN2", 3, 3000, 25F, 10, 100);
    public static EnumToolMaterial toolLAZULI = EnumHelper.addToolMaterial("LAZULI", 2, 200, 5F, 2, 22);
    public static EnumToolMaterial toolLAZULI2 = EnumHelper.addToolMaterial("LAZULI2", 2, 250, 6F, 4, 50);
    public static EnumToolMaterial toolOSMIUM = EnumHelper.addToolMaterial("OSMIUM", 2, 500, 10F, 4, 30);
    public static EnumToolMaterial toolOSMIUM2 = EnumHelper.addToolMaterial("OSMIUM2", 3, 700, 12F, 5, 40);
    public static EnumToolMaterial toolBRONZE = EnumHelper.addToolMaterial("BRONZE", 2, 800, 14F, 6, 100);
    public static EnumToolMaterial toolBRONZE2 = EnumHelper.addToolMaterial("BRONZE2", 3, 1100, 16F, 10, 100);
    public static EnumToolMaterial toolGLOWSTONE = EnumHelper.addToolMaterial("GLOWSTONE", 2, 300, 14F, 5, 80);
    public static EnumToolMaterial toolGLOWSTONE2 = EnumHelper.addToolMaterial("GLOWSTONE2", 2, 450, 18F, 5, 100);
    public static EnumToolMaterial toolSTEEL = EnumHelper.addToolMaterial("STEEL", 3, 850, 14F, 4, 100);
    public static EnumToolMaterial toolSTEEL2 = EnumHelper.addToolMaterial("STEEL2", 3, 1250, 18F, 8, 100);
    
    //Enums: Armor
    public static EnumArmorMaterial armorOBSIDIAN = EnumHelper.addArmorMaterial("OBSIDIAN", 50, new int[]{5, 12, 8, 5}, 50);
    public static EnumArmorMaterial armorLAZULI = EnumHelper.addArmorMaterial("LAZULI", 13, new int[]{2, 5, 4, 2}, 50);
    public static EnumArmorMaterial armorOSMIUM = EnumHelper.addArmorMaterial("OSMIUM", 30, new int[]{3, 9, 7, 3}, 50);
    public static EnumArmorMaterial armorBRONZE = EnumHelper.addArmorMaterial("BRONZE", 35, new int[]{3, 8, 6, 2}, 50);
    public static EnumArmorMaterial armorGLOWSTONE = EnumHelper.addArmorMaterial("GLOWSTONE", 18, new int[]{3, 7, 6, 3}, 50);
    public static EnumArmorMaterial armorSTEEL = EnumHelper.addArmorMaterial("STEEL", 40, new int[] {4, 10, 8, 4}, 50);
    
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
	public static Item GlowstoneSpade;
	public static Item GlowstoneHoe;
	public static Item GlowstoneSword;
	public static Item GlowstoneHelmet;
	public static Item GlowstoneBody;
	public static Item GlowstoneLegs;
	public static Item GlowstoneBoots;
	
	//Redstone Items
	public static Item BronzePaxel;
	public static Item BronzePickaxe;
	public static Item BronzeAxe;
	public static Item BronzeSpade;
	public static Item BronzeHoe;
	public static Item BronzeSword;
	public static Item BronzeHelmet;
	public static Item BronzeBody;
	public static Item BronzeLegs;
	public static Item BronzeBoots;
	
	//Osmium Items
	public static Item OsmiumPaxel;
	public static Item OsmiumPickaxe;
	public static Item OsmiumAxe;
	public static Item OsmiumSpade;
	public static Item OsmiumHoe;
	public static Item OsmiumSword;
	public static Item OsmiumHelmet;
	public static Item OsmiumBody;
	public static Item OsmiumLegs;
	public static Item OsmiumBoots;
	
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
	
	//Steel Items
	public static Item SteelPaxel;
	public static Item SteelPickaxe;
	public static Item SteelAxe;
	public static Item SteelSpade;
	public static Item SteelHoe;
	public static Item SteelSword;
	public static Item SteelHelmet;
	public static Item SteelBody;
	public static Item SteelLegs;
	public static Item SteelBoots;
	
	@Init
	public void init(FMLInitializationEvent event)
	{
		//Register this class to the event bus for special mob spawning (mobs with Mekanism armor/tools)
		MinecraftForge.EVENT_BUS.register(this);
		
		//Load this module
		addItems();
		addTextures();
		addNames();
		addRecipes();
		
		//Finalization
		Mekanism.logger.info("[MekanismTools] Loaded module.");
	}
	
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
		
		//Obsidian
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), ObsidianAxe, Character.valueOf('Y'), ObsidianPickaxe, Character.valueOf('Z'), ObsidianSpade, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianPickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianSpade, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotRefinedObsidian", Character.valueOf('T'), Item.stick
		}));
		
		//Glowstone
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstonePaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), GlowstoneAxe, Character.valueOf('Y'), GlowstonePickaxe, Character.valueOf('Z'), GlowstoneSpade, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstonePickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneSpade, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotRefinedGlowstone", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotRefinedGlowstone"
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
		
		//Osmium
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(OsmiumPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), OsmiumAxe, Character.valueOf('Y'), OsmiumPickaxe, Character.valueOf('Z'), OsmiumSpade, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(OsmiumPickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(OsmiumAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(OsmiumSpade, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(OsmiumHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(OsmiumSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotOsmium", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(OsmiumHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(OsmiumBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(OsmiumLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(OsmiumBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotOsmium"
		}));
		
		//Bronze
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BronzePaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), BronzeAxe, Character.valueOf('Y'), BronzePickaxe, Character.valueOf('Z'), BronzeSpade, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BronzePickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BronzeAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BronzeSpade, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BronzeHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BronzeSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotBronze", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BronzeHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotBronze"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BronzeBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotBronze"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BronzeLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotBronze"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BronzeBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotBronze"
		}));
		
		//Steel
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SteelPaxel, 1), new Object[] {
			"XYZ", " I ", " I ", Character.valueOf('X'), SteelAxe, Character.valueOf('Y'), SteelPickaxe, Character.valueOf('Z'), SteelSpade, Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SteelPickaxe, 1), new Object[] {
			"XXX", " I ", " I ", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SteelAxe, 1), new Object[] {
			"XX", "XI", " I", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SteelSpade, 1), new Object[] {
			"X", "I", "I", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SteelHoe, 1), new Object[] {
			"XX", " I", " I", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SteelSword, 1), new Object[] {
			"X", "X", "I", Character.valueOf('X'), "ingotSteel", Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SteelHelmet, 1), new Object[] {
			"***", "I I", Character.valueOf('*'), "ingotSteel", Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SteelBody, 1), new Object[] {
			"I I", "*I*", "***", Character.valueOf('*'), "ingotSteel", Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SteelLegs, 1), new Object[] {
			"I*I", "* *", "* *", Character.valueOf('*'), "ingotSteel", Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SteelBoots, 1), new Object[] {
			"I *", "* I", Character.valueOf('*'), "ingotSteel", Character.valueOf('I'), Item.ingotIron
		}));
	}
	
	public void addNames()
	{
		//Base
		LanguageRegistry.addName(WoodPaxel, "Wood Paxel");
		LanguageRegistry.addName(StonePaxel, "Stone Paxel");
		LanguageRegistry.addName(IronPaxel, "Iron Paxel");
		LanguageRegistry.addName(DiamondPaxel, "Diamond Paxel");
		LanguageRegistry.addName(GoldPaxel, "Gold Paxel");
		
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
		
		//Osmium
		LanguageRegistry.addName(OsmiumHelmet, "Osmium Helmet");
		LanguageRegistry.addName(OsmiumBody, "Osmium Chestplate");
		LanguageRegistry.addName(OsmiumLegs, "Osmium Leggings");
		LanguageRegistry.addName(OsmiumBoots, "Osmium Boots");
		LanguageRegistry.addName(OsmiumPaxel, "Osmium Paxel");
		LanguageRegistry.addName(OsmiumPickaxe, "Osmium Pickaxe");
		LanguageRegistry.addName(OsmiumAxe, "Osmium Axe");
		LanguageRegistry.addName(OsmiumSpade, "Osmium Shovel");
		LanguageRegistry.addName(OsmiumHoe, "Osmium Hoe");
		LanguageRegistry.addName(OsmiumSword, "Osmium Sword");
		
		//Redstone
		LanguageRegistry.addName(BronzeHelmet, "Bronze Helmet");
		LanguageRegistry.addName(BronzeBody, "Bronze Chestplate");
		LanguageRegistry.addName(BronzeLegs, "Bronze Leggings");
		LanguageRegistry.addName(BronzeBoots, "Bronze Boots");
		LanguageRegistry.addName(BronzePaxel, "Bronze Paxel");
		LanguageRegistry.addName(BronzePickaxe, "Bronze Pickaxe");
		LanguageRegistry.addName(BronzeAxe, "Bronze Axe");
		LanguageRegistry.addName(BronzeSpade, "Bronze Shovel");
		LanguageRegistry.addName(BronzeHoe, "Bronze Hoe");
		LanguageRegistry.addName(BronzeSword, "Bronze Sword");
		
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
		
		//Steel
		LanguageRegistry.addName(SteelPaxel, "Steel Paxel");
		LanguageRegistry.addName(SteelPickaxe, "Steel Pickaxe");
		LanguageRegistry.addName(SteelAxe, "Steel Axe");
		LanguageRegistry.addName(SteelSpade, "Steel Shovel");
		LanguageRegistry.addName(SteelHoe, "Steel Hoe");
		LanguageRegistry.addName(SteelSword, "Steel Sword");
		LanguageRegistry.addName(SteelHelmet, "Steel Helmet");
		LanguageRegistry.addName(SteelBody, "Steel Chestplate");
		LanguageRegistry.addName(SteelLegs, "Steel Leggings");
		LanguageRegistry.addName(SteelBoots, "Steel Boots");
	}
	
	public void addTextures()
	{
		//Base
		WoodPaxel.setIconIndex(150);
		StonePaxel.setIconIndex(151);
		IronPaxel.setIconIndex(152);
		DiamondPaxel.setIconIndex(153);
		GoldPaxel.setIconIndex(154);
		
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
		
		//Redstone
		BronzeHelmet.setIconIndex(3);
		BronzeBody.setIconIndex(19);
		BronzeLegs.setIconIndex(35);
		BronzeBoots.setIconIndex(51);
		BronzePaxel.setIconIndex(147);
		BronzePickaxe.setIconIndex(67);
		BronzeAxe.setIconIndex(83);
		BronzeSpade.setIconIndex(99);
		BronzeHoe.setIconIndex(115);
		BronzeSword.setIconIndex(131);
		
		//Osmium
		OsmiumHelmet.setIconIndex(2);
		OsmiumBody.setIconIndex(18);
		OsmiumLegs.setIconIndex(34);
		OsmiumBoots.setIconIndex(50);
		OsmiumPaxel.setIconIndex(146);
		OsmiumPickaxe.setIconIndex(66);
		OsmiumAxe.setIconIndex(82);
		OsmiumSpade.setIconIndex(98);
		OsmiumHoe.setIconIndex(114);
		OsmiumSword.setIconIndex(130);
		
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
		
		//Steel
		SteelHelmet.setIconIndex(5);
		SteelBody.setIconIndex(21);
		SteelLegs.setIconIndex(37);
		SteelBoots.setIconIndex(53);
		SteelPaxel.setIconIndex(149);
		SteelPickaxe.setIconIndex(69);
		SteelAxe.setIconIndex(85);
		SteelSpade.setIconIndex(101);
		SteelHoe.setIconIndex(117);
		SteelSword.setIconIndex(133);
	}
	
	public void addItems()
	{
		Mekanism.configuration.load();
		//Bronze
		BronzeHelmet = (new ItemMekanismArmor(Mekanism.configuration.getItem("BronzeHelmet", 11400).getInt(), armorBRONZE, Mekanism.proxy.getArmorIndex("bronze"), 0)).setItemName("BronzeHelmet");
		BronzeBody = (new ItemMekanismArmor(Mekanism.configuration.getItem("BronzeBody", 11401).getInt(), armorBRONZE, Mekanism.proxy.getArmorIndex("bronze"), 1)).setItemName("BronzeBody");
		BronzeLegs = (new ItemMekanismArmor(Mekanism.configuration.getItem("BronzeLegs", 11402).getInt(), armorBRONZE, Mekanism.proxy.getArmorIndex("bronze"), 2)).setItemName("BronzeLegs");
		BronzeBoots = (new ItemMekanismArmor(Mekanism.configuration.getItem("BronzeBoots", 11403).getInt(), armorBRONZE, Mekanism.proxy.getArmorIndex("bronze"), 3)).setItemName("BronzeBoots");
		BronzePaxel = new ItemMekanismPaxel(Mekanism.configuration.getItem("BronzePaxel", 11404).getInt(), toolBRONZE2).setItemName("BronzePaxel");
		BronzePickaxe = new ItemMekanismPickaxe(Mekanism.configuration.getItem("BronzePickaxe", 11405).getInt(), toolBRONZE).setItemName("BronzePickaxe");
		BronzeAxe = new ItemMekanismAxe(Mekanism.configuration.getItem("BronzeAxe", 11406).getInt(), toolBRONZE).setItemName("BronzeAxe");
		BronzeSpade = new ItemMekanismSpade(Mekanism.configuration.getItem("BronzeSpade", 11407).getInt(), toolBRONZE).setItemName("BronzeSpade");
		BronzeHoe = new ItemMekanismHoe(Mekanism.configuration.getItem("BronzeHoe", 11408).getInt(), toolBRONZE).setItemName("BronzeHoe");
		BronzeSword = new ItemMekanismSword(Mekanism.configuration.getItem("BronzeSword", 11409).getInt(), toolBRONZE).setItemName("BronzeSword");
		
		//Osmium
		OsmiumHelmet = (new ItemMekanismArmor(Mekanism.configuration.getItem("OsmiumHelmet", 11410).getInt(), EnumArmorMaterial.DIAMOND, Mekanism.proxy.getArmorIndex("osmium"), 0)).setItemName("OsmiumHelmet");
		OsmiumBody = (new ItemMekanismArmor(Mekanism.configuration.getItem("OsmiumBody", 11411).getInt(), EnumArmorMaterial.DIAMOND, Mekanism.proxy.getArmorIndex("osmium"), 1)).setItemName("OsmiumBody");
		OsmiumLegs = (new ItemMekanismArmor(Mekanism.configuration.getItem("OsmiumLegs", 11412).getInt(), EnumArmorMaterial.DIAMOND, Mekanism.proxy.getArmorIndex("osmium"), 2)).setItemName("OsmiumLegs");
		OsmiumBoots = (new ItemMekanismArmor(Mekanism.configuration.getItem("OsmiumBoots", 11413).getInt(), EnumArmorMaterial.DIAMOND, Mekanism.proxy.getArmorIndex("osmium"), 3)).setItemName("OsmiumBoots");
		OsmiumPaxel = new ItemMekanismPaxel(Mekanism.configuration.getItem("OsmiumPaxel", 11414).getInt(), toolOSMIUM2).setItemName("OsmiumPaxel");
		OsmiumPickaxe = new ItemMekanismPickaxe(Mekanism.configuration.getItem("OsmiumPickaxe", 11415).getInt(), toolOSMIUM).setItemName("OsmiumPickaxe");
		OsmiumAxe = new ItemMekanismAxe(Mekanism.configuration.getItem("OsmiumAxe", 11416).getInt(), toolOSMIUM).setItemName("OsmiumAxe");
		OsmiumSpade = new ItemMekanismSpade(Mekanism.configuration.getItem("OsmiumSpade", 11417).getInt(), toolOSMIUM).setItemName("OsmiumSpade");
		OsmiumHoe = new ItemMekanismHoe(Mekanism.configuration.getItem("OsmiumHoe", 11418).getInt(), toolOSMIUM).setItemName("OsmiumHoe");
		OsmiumSword = new ItemMekanismSword(Mekanism.configuration.getItem("OsmiumSword", 11419).getInt(), toolOSMIUM).setItemName("OsmiumSword");
		
		//Obsidian
		ObsidianHelmet = (new ItemMekanismArmor(Mekanism.configuration.getItem("ObsidianHelmet", 11420).getInt(), armorOBSIDIAN, Mekanism.proxy.getArmorIndex("obsidian"), 0)).setItemName("ObsidianHelmet");
		ObsidianBody = (new ItemMekanismArmor(Mekanism.configuration.getItem("ObsidianBody", 11421).getInt(), armorOBSIDIAN, Mekanism.proxy.getArmorIndex("obsidian"), 1)).setItemName("ObsidianBody");
		ObsidianLegs = (new ItemMekanismArmor(Mekanism.configuration.getItem("ObsidianLegs", 11422).getInt(), armorOBSIDIAN, Mekanism.proxy.getArmorIndex("obsidian"), 2)).setItemName("ObsidianLegs");
		ObsidianBoots = (new ItemMekanismArmor(Mekanism.configuration.getItem("ObsidianBoots", 11423).getInt(), armorOBSIDIAN, Mekanism.proxy.getArmorIndex("obsidian"), 3)).setItemName("ObsidianBoots");
		ObsidianPaxel = new ItemMekanismPaxel(Mekanism.configuration.getItem("ObsidianPaxel", 11424).getInt(), toolOBSIDIAN2).setItemName("ObsidianPaxel");
		ObsidianPickaxe = new ItemMekanismPickaxe(Mekanism.configuration.getItem("ObsidianPickaxe", 11425).getInt(), toolOBSIDIAN).setItemName("ObsidianPickaxe");
		ObsidianAxe = new ItemMekanismAxe(Mekanism.configuration.getItem("ObsidianAxe", 11426).getInt(), toolOBSIDIAN).setItemName("ObsidianAxe");
		ObsidianSpade = new ItemMekanismSpade(Mekanism.configuration.getItem("ObsidianSpade", 11427).getInt(), toolOBSIDIAN).setItemName("ObsidianSpade");
		ObsidianHoe = new ItemMekanismHoe(Mekanism.configuration.getItem("ObsidianHoe", 11428).getInt(), toolOBSIDIAN).setItemName("ObsidianHoe");
		ObsidianSword = new ItemMekanismSword(Mekanism.configuration.getItem("ObsidianSword", 11429).getInt(), toolOBSIDIAN).setItemName("ObsidianSword");
		
		//Lazuli
		LazuliPaxel = new ItemMekanismPaxel(Mekanism.configuration.getItem("LazuliPaxel", 11430).getInt(), toolLAZULI2).setItemName("LazuliPaxel");
		LazuliPickaxe = new ItemMekanismPickaxe(Mekanism.configuration.getItem("LazuliPickaxe", 11431).getInt(), toolLAZULI).setItemName("LazuliPickaxe");
		LazuliAxe = new ItemMekanismAxe(Mekanism.configuration.getItem("LazuliAxe", 11432).getInt(), toolLAZULI).setItemName("LazuliAxe");
		LazuliSpade = new ItemMekanismSpade(Mekanism.configuration.getItem("LazuliSpade", 11433).getInt(), toolLAZULI).setItemName("LazuliSpade");
		LazuliHoe = new ItemMekanismHoe(Mekanism.configuration.getItem("LazuliHoe", 11434).getInt(), toolLAZULI).setItemName("LazuliHoe");
		LazuliSword = new ItemMekanismSword(Mekanism.configuration.getItem("LazuliSword", 11435).getInt(), toolLAZULI).setItemName("LazuliSword");
		LazuliHelmet = (new ItemMekanismArmor(Mekanism.configuration.getItem("LazuliHelmet", 11436).getInt(), armorLAZULI, Mekanism.proxy.getArmorIndex("lazuli"), 0)).setItemName("LazuliHelmet");
		LazuliBody = (new ItemMekanismArmor(Mekanism.configuration.getItem("LazuliBody", 11437).getInt(), armorLAZULI, Mekanism.proxy.getArmorIndex("lazuli"), 1)).setItemName("LazuliBody");
		LazuliLegs = (new ItemMekanismArmor(Mekanism.configuration.getItem("LazuliLegs", 11438).getInt(), armorLAZULI, Mekanism.proxy.getArmorIndex("lazuli"), 2)).setItemName("LazuliLegs");
		LazuliBoots = (new ItemMekanismArmor(Mekanism.configuration.getItem("LazuliBoots", 11439).getInt(), armorLAZULI, Mekanism.proxy.getArmorIndex("lazuli"), 3)).setItemName("LazuliBoots");
		
		//Glowstone
		GlowstonePaxel = new ItemMekanismPaxel(Mekanism.configuration.getItem("GlowstonePaxel", 11440).getInt(), toolGLOWSTONE2).setItemName("GlowstonePaxel");
		GlowstonePickaxe = new ItemMekanismPickaxe(Mekanism.configuration.getItem("GlowstonePickaxe", 11441).getInt(), toolGLOWSTONE).setItemName("GlowstonePickaxe");
		GlowstoneAxe = new ItemMekanismAxe(Mekanism.configuration.getItem("GlowstoneAxe", 11442).getInt(), toolGLOWSTONE).setItemName("GlowstoneAxe");
		GlowstoneSpade = new ItemMekanismSpade(Mekanism.configuration.getItem("GlowstoneSpade", 11443).getInt(), toolGLOWSTONE).setItemName("GlowstoneSpade");
		GlowstoneHoe = new ItemMekanismHoe(Mekanism.configuration.getItem("GlowstoneHoe", 11444).getInt(), toolGLOWSTONE).setItemName("GlowstoneHoe");
		GlowstoneSword = new ItemMekanismSword(Mekanism.configuration.getItem("GlowstoneSword", 11445).getInt(), toolGLOWSTONE).setItemName("GlowstoneSword");
		GlowstoneHelmet = new ItemMekanismArmor(Mekanism.configuration.getItem("GlowstoneHelmet", 11446).getInt(), armorGLOWSTONE, Mekanism.proxy.getArmorIndex("glowstone"), 0).setItemName("GlowstoneHelmet");
		GlowstoneBody = new ItemMekanismArmor(Mekanism.configuration.getItem("GlowstoneBody", 11447).getInt(), armorGLOWSTONE, Mekanism.proxy.getArmorIndex("glowstone"), 1).setItemName("GlowstoneBody");
		GlowstoneLegs = new ItemMekanismArmor(Mekanism.configuration.getItem("GlowstoneLegs", 11448).getInt(), armorGLOWSTONE, Mekanism.proxy.getArmorIndex("glowstone"), 2).setItemName("GlowstoneLegs");
		GlowstoneBoots = new ItemMekanismArmor(Mekanism.configuration.getItem("GlowstoneBoots", 11449).getInt(), armorGLOWSTONE, Mekanism.proxy.getArmorIndex("glowstone"), 3).setItemName("GlowstoneBoots");
		
		//Base Paxels
		WoodPaxel = new ItemMekanismPaxel(Mekanism.configuration.getItem("WoodPaxel", 11450).getInt(), EnumToolMaterial.WOOD).setItemName("WoodPaxel");
		StonePaxel = new ItemMekanismPaxel(Mekanism.configuration.getItem("StonePaxel", 11451).getInt(), EnumToolMaterial.STONE).setItemName("StonePaxel");
		IronPaxel = new ItemMekanismPaxel(Mekanism.configuration.getItem("IronPaxel", 11452).getInt(), EnumToolMaterial.IRON).setItemName("IronPaxel");
		DiamondPaxel = new ItemMekanismPaxel(Mekanism.configuration.getItem("DiamondPaxel", 11453).getInt(), EnumToolMaterial.EMERALD).setItemName("DiamondPaxel");
		GoldPaxel = new ItemMekanismPaxel(Mekanism.configuration.getItem("GoldPaxel", 11454).getInt(), EnumToolMaterial.GOLD).setItemName("GoldPaxel");
		
		//Steel
		SteelPaxel = new ItemMekanismPaxel(Mekanism.configuration.getItem("SteelPaxel", 11455).getInt(), toolSTEEL2).setItemName("SteelPaxel");
		SteelPickaxe = new ItemMekanismPickaxe(Mekanism.configuration.getItem("SteelPickaxe", 11456).getInt(), toolSTEEL).setItemName("SteelPickaxe");
		SteelAxe = new ItemMekanismAxe(Mekanism.configuration.getItem("SteelAxe", 11457).getInt(), toolSTEEL).setItemName("SteelAxe");
		SteelSpade = new ItemMekanismSpade(Mekanism.configuration.getItem("SteelSpade", 11458).getInt(), toolSTEEL).setItemName("SteelSpade");
		SteelHoe = new ItemMekanismHoe(Mekanism.configuration.getItem("SteelHoe", 11459).getInt(), toolSTEEL).setItemName("SteelHoe");
		SteelSword = new ItemMekanismSword(Mekanism.configuration.getItem("SteelSword", 11460).getInt(), toolSTEEL).setItemName("SteelSword");
		SteelHelmet = new ItemMekanismArmor(Mekanism.configuration.getItem("SteelHelmet", 11461).getInt(), armorSTEEL, Mekanism.proxy.getArmorIndex("steel"), 0).setItemName("SteelHelmet");
		SteelBody = new ItemMekanismArmor(Mekanism.configuration.getItem("SteelBody", 11462).getInt(), armorSTEEL, Mekanism.proxy.getArmorIndex("steel"), 1).setItemName("SteelBody");
		SteelLegs = new ItemMekanismArmor(Mekanism.configuration.getItem("SteelLegs", 11463).getInt(), armorSTEEL, Mekanism.proxy.getArmorIndex("steel"), 2).setItemName("SteelLegs");
		SteelBoots = new ItemMekanismArmor(Mekanism.configuration.getItem("SteelBoots", 11464).getInt(), armorSTEEL, Mekanism.proxy.getArmorIndex("steel"), 3).setItemName("SteelBoots");
		Mekanism.configuration.save();
	}
	
	@ForgeSubscribe
	public void onLivingSpecialSpawn(LivingSpecialSpawnEvent event)
	{
		Random random = new Random();
		
		int chance = random.nextInt(100);
		int secondChance = random.nextInt(3);
		
		if(chance < 4)
		{
			if(event.entityLiving instanceof EntityZombie || event.entityLiving instanceof EntitySkeleton)
			{
				if(secondChance == 0)
				{
					if(event.entityLiving instanceof EntityZombie) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(GlowstoneSword));
					event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(GlowstoneHelmet));
					event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(GlowstoneBody));
					event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(GlowstoneLegs));
					event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(GlowstoneBoots));
				}
				else if(secondChance == 1)
				{
					if(event.entityLiving instanceof EntityZombie) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(LazuliSword));
					event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(LazuliHelmet));
					event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(LazuliBody));
					event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(LazuliLegs));
					event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(LazuliBoots));
				}
				else if(secondChance == 2)
				{
					if(event.entityLiving instanceof EntityZombie) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(OsmiumSword));
					event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(OsmiumHelmet));
					event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(OsmiumBody));
					event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(OsmiumLegs));
					event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(OsmiumBoots));
				}
			}
		}
	}
}
