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

@Mod(modid = "MekanismTools", name = "MekanismTools", version = "5.0.3", dependencies = "required-after:Mekanism")
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
    public static EnumToolMaterial toolPLATINUM = EnumHelper.addToolMaterial("PLATINUM", 2, 500, 10F, 4, 30);
    public static EnumToolMaterial toolPLATINUM2 = EnumHelper.addToolMaterial("PLATINUM2", 3, 700, 12F, 5, 40);
    public static EnumToolMaterial toolBRONZE = EnumHelper.addToolMaterial("BRONZE", 3, 1000, 16F, 6, 100);
    public static EnumToolMaterial toolBRONZE2 = EnumHelper.addToolMaterial("BRONZE2", 3, 1400, 22F, 10, 100);
    public static EnumToolMaterial toolGLOWSTONE = EnumHelper.addToolMaterial("GLOWSTONE", 2, 300, 14F, 5, 80);
    public static EnumToolMaterial toolGLOWSTONE2 = EnumHelper.addToolMaterial("GLOWSTONE2", 2, 450, 18F, 5, 100);
    public static EnumToolMaterial toolSTEEL = EnumHelper.addToolMaterial("STEEL", 3, 850, 14F, 4, 100);
    public static EnumToolMaterial toolSTEEL2 = EnumHelper.addToolMaterial("STEEL2", 3, 1250, 18F, 8, 100);
    
    //Enums: Armor
    public static EnumArmorMaterial armorOBSIDIAN = EnumHelper.addArmorMaterial("OBSIDIAN", 50, new int[]{5, 12, 8, 5}, 50);
    public static EnumArmorMaterial armorLAZULI = EnumHelper.addArmorMaterial("LAZULI", 13, new int[]{2, 5, 4, 2}, 50);
    public static EnumArmorMaterial armorPLATINUM = EnumHelper.addArmorMaterial("PLATINUM", 30, new int[]{3, 9, 7, 3}, 50);
    public static EnumArmorMaterial armorBRONZE = EnumHelper.addArmorMaterial("BRONZE", 42, new int[]{4, 10, 8, 4}, 50);
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
			"***", "* *", Character.valueOf('*'), "ingotObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), ObsidianAxe, Character.valueOf('Y'), ObsidianPickaxe, Character.valueOf('Z'), ObsidianSpade, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianPickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotObsidian", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotObsidian", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianSpade, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotObsidian", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotObsidian", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotObsidian", Character.valueOf('T'), Item.stick
		}));
		
		//Glowstone
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstonePaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), GlowstoneAxe, Character.valueOf('Y'), GlowstonePickaxe, Character.valueOf('Z'), GlowstoneSpade, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstonePickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotGlowstone", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotGlowstone", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneSpade, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotGlowstone", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotGlowstone", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotGlowstone", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(GlowstoneBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotGlowstone"
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
		
		//Platinum
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumPaxel, 1), new Object[] {
			"XYZ", " T ", " T ", Character.valueOf('X'), PlatinumAxe, Character.valueOf('Y'), PlatinumPickaxe, Character.valueOf('Z'), PlatinumSpade, Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumPickaxe, 1), new Object[] {
			"XXX", " T ", " T ", Character.valueOf('X'), "ingotPlatinum", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumAxe, 1), new Object[] {
			"XX", "XT", " T", Character.valueOf('X'), "ingotPlatinum", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumSpade, 1), new Object[] {
			"X", "T", "T", Character.valueOf('X'), "ingotPlatinum", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumHoe, 1), new Object[] {
			"XX", " T", " T", Character.valueOf('X'), "ingotPlatinum", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumSword, 1), new Object[] {
			"X", "X", "T", Character.valueOf('X'), "ingotPlatinum", Character.valueOf('T'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumHelmet, 1), new Object[] {
			"***", "* *", Character.valueOf('*'), "ingotPlatinum"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumBody, 1), new Object[] {
			"* *", "***", "***", Character.valueOf('*'), "ingotPlatinum"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumLegs, 1), new Object[] {
			"***", "* *", "* *", Character.valueOf('*'), "ingotPlatinum"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PlatinumBoots, 1), new Object[] {
			"* *", "* *", Character.valueOf('*'), "ingotPlatinum"
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
		//Bronze
		BronzeHelmet = (new ItemMekanismArmor(11400, armorBRONZE, Mekanism.proxy.getArmorIndex("bronze"), 0)).setItemName("BronzeHelmet");
		BronzeBody = (new ItemMekanismArmor(11401, armorBRONZE, Mekanism.proxy.getArmorIndex("bronze"), 1)).setItemName("BronzeBody");
		BronzeLegs = (new ItemMekanismArmor(11402, armorBRONZE, Mekanism.proxy.getArmorIndex("bronze"), 2)).setItemName("BronzeLegs");
		BronzeBoots = (new ItemMekanismArmor(11403, armorBRONZE, Mekanism.proxy.getArmorIndex("bronze"), 3)).setItemName("BronzeBoots");
		BronzePaxel = new ItemMekanismPaxel(11404, toolBRONZE2).setItemName("BronzePaxel");
		BronzePickaxe = new ItemMekanismPickaxe(11405, toolBRONZE).setItemName("BronzePickaxe");
		BronzeAxe = new ItemMekanismAxe(11406, toolBRONZE).setItemName("BronzeAxe");
		BronzeSpade = new ItemMekanismSpade(11407, toolBRONZE).setItemName("BronzeSpade");
		BronzeHoe = new ItemMekanismHoe(11408, toolBRONZE).setItemName("BronzeHoe");
		BronzeSword = new ItemMekanismSword(11409, toolBRONZE).setItemName("BronzeSword");
		
		//Platinum
		PlatinumHelmet = (new ItemMekanismArmor(11410, EnumArmorMaterial.DIAMOND, Mekanism.proxy.getArmorIndex("platinum"), 0)).setItemName("PlatinumHelmet");
		PlatinumBody = (new ItemMekanismArmor(11411, EnumArmorMaterial.DIAMOND, Mekanism.proxy.getArmorIndex("platinum"), 1)).setItemName("PlatinumBody");
		PlatinumLegs = (new ItemMekanismArmor(11412, EnumArmorMaterial.DIAMOND, Mekanism.proxy.getArmorIndex("platinum"), 2)).setItemName("PlatinumLegs");
		PlatinumBoots = (new ItemMekanismArmor(11413, EnumArmorMaterial.DIAMOND, Mekanism.proxy.getArmorIndex("platinum"), 3)).setItemName("PlatinumBoots");
		PlatinumPaxel = new ItemMekanismPaxel(11414, toolPLATINUM2).setItemName("PlatinumPaxel");
		PlatinumPickaxe = new ItemMekanismPickaxe(11415, toolPLATINUM).setItemName("PlatinumPickaxe");
		PlatinumAxe = new ItemMekanismAxe(11416, toolPLATINUM).setItemName("PlatinumAxe");
		PlatinumSpade = new ItemMekanismSpade(11417, toolPLATINUM).setItemName("PlatinumSpade");
		PlatinumHoe = new ItemMekanismHoe(11418, toolPLATINUM).setItemName("PlatinumHoe");
		PlatinumSword = new ItemMekanismSword(11419, toolPLATINUM).setItemName("PlatinumSword");
		
		//Obsidian
		ObsidianHelmet = (new ItemMekanismArmor(11420, armorOBSIDIAN, Mekanism.proxy.getArmorIndex("obsidian"), 0)).setItemName("ObsidianHelmet");
		ObsidianBody = (new ItemMekanismArmor(11421, armorOBSIDIAN, Mekanism.proxy.getArmorIndex("obsidian"), 1)).setItemName("ObsidianBody");
		ObsidianLegs = (new ItemMekanismArmor(11422, armorOBSIDIAN, Mekanism.proxy.getArmorIndex("obsidian"), 2)).setItemName("ObsidianLegs");
		ObsidianBoots = (new ItemMekanismArmor(11423, armorOBSIDIAN, Mekanism.proxy.getArmorIndex("obsidian"), 3)).setItemName("ObsidianBoots");
		ObsidianPaxel = new ItemMekanismPaxel(11424, toolOBSIDIAN2).setItemName("ObsidianPaxel");
		ObsidianPickaxe = new ItemMekanismPickaxe(11425, toolOBSIDIAN).setItemName("ObsidianPickaxe");
		ObsidianAxe = new ItemMekanismAxe(11426, toolOBSIDIAN).setItemName("ObsidianAxe");
		ObsidianSpade = new ItemMekanismSpade(11427, toolOBSIDIAN).setItemName("ObsidianSpade");
		ObsidianHoe = new ItemMekanismHoe(11428, toolOBSIDIAN).setItemName("ObsidianHoe");
		ObsidianSword = new ItemMekanismSword(11429, toolOBSIDIAN).setItemName("ObsidianSword");
		
		//Lazuli
		LazuliPaxel = new ItemMekanismPaxel(11430, toolLAZULI2).setItemName("LazuliPaxel");
		LazuliPickaxe = new ItemMekanismPickaxe(11431, toolLAZULI).setItemName("LazuliPickaxe");
		LazuliAxe = new ItemMekanismAxe(11432, toolLAZULI).setItemName("LazuliAxe");
		LazuliSpade = new ItemMekanismSpade(11433, toolLAZULI).setItemName("LazuliSpade");
		LazuliHoe = new ItemMekanismHoe(11434, toolLAZULI).setItemName("LazuliHoe");
		LazuliSword = new ItemMekanismSword(11435, toolLAZULI).setItemName("LazuliSword");
		LazuliHelmet = (new ItemMekanismArmor(11436, armorLAZULI, Mekanism.proxy.getArmorIndex("lazuli"), 0)).setItemName("LazuliHelmet");
		LazuliBody = (new ItemMekanismArmor(11437, armorLAZULI, Mekanism.proxy.getArmorIndex("lazuli"), 1)).setItemName("LazuliBody");
		LazuliLegs = (new ItemMekanismArmor(11438, armorLAZULI, Mekanism.proxy.getArmorIndex("lazuli"), 2)).setItemName("LazuliLegs");
		LazuliBoots = (new ItemMekanismArmor(11439, armorLAZULI, Mekanism.proxy.getArmorIndex("lazuli"), 3)).setItemName("LazuliBoots");
		
		//Glowstone
		GlowstonePaxel = new ItemMekanismPaxel(11440, toolGLOWSTONE2).setItemName("GlowstonePaxel");
		GlowstonePickaxe = new ItemMekanismPickaxe(11441, toolGLOWSTONE).setItemName("GlowstonePickaxe");
		GlowstoneAxe = new ItemMekanismAxe(11442, toolGLOWSTONE).setItemName("GlowstoneAxe");
		GlowstoneSpade = new ItemMekanismSpade(11443, toolGLOWSTONE).setItemName("GlowstoneSpade");
		GlowstoneHoe = new ItemMekanismHoe(11444, toolGLOWSTONE).setItemName("GlowstoneHoe");
		GlowstoneSword = new ItemMekanismSword(11445, toolGLOWSTONE).setItemName("GlowstoneSword");
		GlowstoneHelmet = new ItemMekanismArmor(11446, armorGLOWSTONE, Mekanism.proxy.getArmorIndex("glowstone"), 0).setItemName("GlowstoneHelmet");
		GlowstoneBody = new ItemMekanismArmor(11447, armorGLOWSTONE, Mekanism.proxy.getArmorIndex("glowstone"), 1).setItemName("GlowstoneBody");
		GlowstoneLegs = new ItemMekanismArmor(11448, armorGLOWSTONE, Mekanism.proxy.getArmorIndex("glowstone"), 2).setItemName("GlowstoneLegs");
		GlowstoneBoots = new ItemMekanismArmor(11449, armorGLOWSTONE, Mekanism.proxy.getArmorIndex("glowstone"), 3).setItemName("GlowstoneBoots");
		
		//Base Paxels
		WoodPaxel = new ItemMekanismPaxel(11450, EnumToolMaterial.WOOD).setItemName("WoodPaxel");
		StonePaxel = new ItemMekanismPaxel(11451, EnumToolMaterial.STONE).setItemName("StonePaxel");
		IronPaxel = new ItemMekanismPaxel(11452, EnumToolMaterial.IRON).setItemName("IronPaxel");
		DiamondPaxel = new ItemMekanismPaxel(11453, EnumToolMaterial.EMERALD).setItemName("DiamondPaxel");
		GoldPaxel = new ItemMekanismPaxel(11454, EnumToolMaterial.GOLD).setItemName("GoldPaxel");
		
		//Steel
		SteelPaxel = new ItemMekanismPaxel(11455, toolSTEEL2).setItemName("SteelPaxel");
		SteelPickaxe = new ItemMekanismPickaxe(11456, toolSTEEL).setItemName("SteelPickaxe");
		SteelAxe = new ItemMekanismAxe(11457, toolSTEEL).setItemName("SteelAxe");
		SteelSpade = new ItemMekanismSpade(11458, toolSTEEL).setItemName("SteelSpade");
		SteelHoe = new ItemMekanismHoe(11459, toolSTEEL).setItemName("SteelHoe");
		SteelSword = new ItemMekanismSword(11460, toolSTEEL).setItemName("SteelSword");
		SteelHelmet = new ItemMekanismArmor(11461, armorSTEEL, Mekanism.proxy.getArmorIndex("steel"), 0).setItemName("SteelHelmet");
		SteelBody = new ItemMekanismArmor(11462, armorSTEEL, Mekanism.proxy.getArmorIndex("steel"), 1).setItemName("SteelBody");
		SteelLegs = new ItemMekanismArmor(11463, armorSTEEL, Mekanism.proxy.getArmorIndex("steel"), 2).setItemName("SteelLegs");
		SteelBoots = new ItemMekanismArmor(11464, armorSTEEL, Mekanism.proxy.getArmorIndex("steel"), 3).setItemName("SteelBoots");
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
					if(event.entityLiving instanceof EntityZombie) event.entityLiving.setCurrentItemOrArmor(0, new ItemStack(PlatinumSword));
					event.entityLiving.setCurrentItemOrArmor(1, new ItemStack(PlatinumHelmet));
					event.entityLiving.setCurrentItemOrArmor(2, new ItemStack(PlatinumBody));
					event.entityLiving.setCurrentItemOrArmor(3, new ItemStack(PlatinumLegs));
					event.entityLiving.setCurrentItemOrArmor(4, new ItemStack(PlatinumBoots));
				}
			}
		}
	}
}
