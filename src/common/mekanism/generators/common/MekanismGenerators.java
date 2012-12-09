package mekanism.generators.common;

import java.util.Random;

import net.minecraft.src.*;
import net.minecraftforge.common.EnumHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingSpecialSpawnEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import mekanism.common.CommonProxy;
import mekanism.common.ItemMekanism;
import mekanism.common.Mekanism;
import mekanism.common.RecipeHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "MekanismGenerators", name = "MekanismGenerators", version = "5.0.0", dependencies = "required-after:Mekanism")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class MekanismGenerators
{
	@SidedProxy(clientSide = "mekanism.generators.client.GeneratorsClientProxy", serverSide = "mekanism.generators.common.GeneratorsCommonProxy")
	public static GeneratorsCommonProxy proxy;
	
	@Instance("MekanismGenerators")
	public static MekanismGenerators instance;
	
	//Items
	public static Item BioFuel;
	public static Item ElectrolyticCore;
	public static Item SolarPanel;
	
	//Blocks
	public static Block Generator;
	
	//Block IDs
	public static int generatorID = 3005;
	
	@Init
	public void init(FMLInitializationEvent event)
	{
		//Set up the GUI handler
		NetworkRegistry.instance().registerGuiHandler(this, new GeneratorsGuiHandler());
		
		//Load the proxy
		proxy.loadConfiguration();
		proxy.registerSpecialTileEntities();
		proxy.registerRenderInformation();
		
		//Load this module
		addBlocks();
		addItems();
		addTextures();
		addNames();
		addRecipes();
		addEntities();
		
		//Finalization
		Mekanism.logger.info("[MekanismGenerators] Loaded module.");
	}
	
	public void addRecipes()
	{
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Generator, 1, 0), new Object[] {
			"GGG", "ECE", "IRI", Character.valueOf('G'), Item.lightStoneDust, Character.valueOf('E'), Mekanism.EnrichedAlloy, Character.valueOf('C'), new ItemStack(Mekanism.BasicBlock, 1, 3), Character.valueOf('I'), Item.ingotIron, Character.valueOf('R'), Item.redstone
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Generator, 1, 1), new Object[] {
			"SSS", "AIA", "PEP", Character.valueOf('S'), SolarPanel, Character.valueOf('A'), Mekanism.EnrichedAlloy, Character.valueOf('I'), Item.ingotIron, Character.valueOf('P'), "dustPlatinum", Character.valueOf('E'), Mekanism.EnergyTablet.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Generator, 1, 5), new Object[] {
			"SES", "SES", "III", Character.valueOf('S'), new ItemStack(Generator, 1, 1), Character.valueOf('E'), Mekanism.EnrichedAlloy, Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Generator, 1, 4), new Object[] {
			"RER", "BIB", "NEN", Character.valueOf('R'), Item.redstone, Character.valueOf('E'), Mekanism.EnrichedAlloy, Character.valueOf('B'), BioFuel, Character.valueOf('I'), new ItemStack(Mekanism.BasicBlock, 1, 5), Character.valueOf('N'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Generator, 1, 2), new Object[] {
			"IRI", "ECE", "IRI", Character.valueOf('I'), Item.ingotIron, Character.valueOf('R'), Item.redstone, Character.valueOf('E'), Mekanism.EnrichedAlloy, Character.valueOf('C'), ElectrolyticCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Generator, 1, 3), new Object[] {
			"PEP", "ICI", "PEP", Character.valueOf('P'), "ingotPlatinum", Character.valueOf('E'), Mekanism.EnrichedAlloy, Character.valueOf('I'), new ItemStack(Mekanism.BasicBlock, 1, 5), Character.valueOf('C'), ElectrolyticCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ElectrolyticCore), new Object[] {
			"EPE", "IEG", "EPE", Character.valueOf('E'), Mekanism.EnrichedAlloy, Character.valueOf('P'), "dustPlatinum", Character.valueOf('I'), "dustIron", Character.valueOf('G'), "dustGold" 
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SolarPanel), new Object[] {
			"GGG", "RAR", "PPP", Character.valueOf('G'), Block.thinGlass, Character.valueOf('R'), Item.redstone, Character.valueOf('A'), Mekanism.EnrichedAlloy, Character.valueOf('P'), "ingotPlatinum"
		}));
		
		//BioFuel Crusher Recipes
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.sapling), new ItemStack(BioFuel, 2));
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.tallGrass), new ItemStack(BioFuel, 2));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.seeds), new ItemStack(BioFuel, 1));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.wheat), new ItemStack(BioFuel, 2));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.pumpkinSeeds), new ItemStack(BioFuel, 1));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.melonSeeds), new ItemStack(BioFuel, 1));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.appleRed), new ItemStack(BioFuel, 3));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.bread), new ItemStack(BioFuel, 3));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.potato), new ItemStack(BioFuel, 2));
        
        for(int i = 0; i < BlockLeaves.LEAF_TYPES.length; i++)
        {
        	RecipeHandler.addCrusherRecipe(new ItemStack(Block.sapling, 1, i), new ItemStack(BioFuel, 2));
        }
	}
	
	public void addNames()
	{
		LanguageRegistry.addName(BioFuel, "Bio Fuel");
		LanguageRegistry.addName(ElectrolyticCore, "Electrolytic Core");
		LanguageRegistry.addName(SolarPanel, "Solar Panel");
		
		//Localization for Generator
		LanguageRegistry.instance().addStringLocalization("tile.Generator.HeatGenerator.name", "Heat Generator");
		LanguageRegistry.instance().addStringLocalization("tile.Generator.SolarGenerator.name", "Solar Generator");
		LanguageRegistry.instance().addStringLocalization("tile.Generator.ElectrolyticSeparator.name", "Electrolytic Separator");
		LanguageRegistry.instance().addStringLocalization("tile.Generator.HydrogenGenerator.name", "Hydrogen Generator");
		LanguageRegistry.instance().addStringLocalization("tile.Generator.BioGenerator.name", "Bio-Generator");
		LanguageRegistry.instance().addStringLocalization("tile.Generator.AdvancedSolarGenerator.name", "Advanced Solar Generator");
	}
	
	public void addTextures()
	{
		SolarPanel.setIconIndex(239);
		BioFuel.setIconIndex(237);
		ElectrolyticCore.setIconIndex(238);
	}
	
	public void addEntities()
	{
		GameRegistry.registerTileEntity(TileEntityHeatGenerator.class, "HeatGenerator");
		GameRegistry.registerTileEntity(TileEntitySolarGenerator.class, "SolarGenerator");
		GameRegistry.registerTileEntity(TileEntityElectrolyticSeparator.class, "ElectrolyticSeparator");
		GameRegistry.registerTileEntity(TileEntityHydrogenGenerator.class, "HydrogenGenerator");
	}
	
	public void addBlocks()
	{
		Generator = new BlockGenerator(generatorID).setBlockName("Generator");
		
		Item.itemsList[generatorID] = new ItemBlockGenerator(generatorID - 256, Generator).setItemName("Generator");
	}
	
	public void addItems()
	{
		SolarPanel = new ItemMekanism(11300).setItemName("SolarPanel");
		BioFuel = new ItemMekanism(11301).setItemName("BioFuel");
		ElectrolyticCore = new ItemMekanism(11302).setItemName("ElectrolyticCore");
	}
}
