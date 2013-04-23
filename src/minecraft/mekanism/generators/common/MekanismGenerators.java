package mekanism.generators.common;

import mekanism.api.InfuseObject;
import mekanism.api.InfusionType;
import mekanism.common.IModule;
import mekanism.common.ItemMekanism;
import mekanism.common.Mekanism;
import mekanism.common.RecipeHandler;
import mekanism.common.Version;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "MekanismGenerators", name = "MekanismGenerators", version = "5.5.5", dependencies = "required-after:Mekanism")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class MekanismGenerators implements IModule
{
	@SidedProxy(clientSide = "mekanism.generators.client.GeneratorsClientProxy", serverSide = "mekanism.generators.common.GeneratorsCommonProxy")
	public static GeneratorsCommonProxy proxy;
	
	@Instance("MekanismGenerators")
	public static MekanismGenerators instance;
	
	/** MekanismGenerators version number */
	public static Version versionNumber = new Version(5, 5, 5);
	
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
		//Add this module to the core list
		Mekanism.modulesLoaded.add(this);
		
		//Set up the GUI handler
		NetworkRegistry.instance().registerGuiHandler(this, new GeneratorsGuiHandler());
		
		//Load the proxy
		proxy.loadConfiguration();
		proxy.registerSpecialTileEntities();
		proxy.registerRenderInformation();
		
		//Load this module
		addBlocks();
		addItems();
		addNames();
		addRecipes();
		addEntities();
		
		//Finalization
		Mekanism.logger.info("[MekanismGenerators] Loaded module.");
	}
	
	public void addRecipes()
	{
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Generator, 1, 0), new Object[] {
			"PPP", "EeE", "IRI", Character.valueOf('P'), "ingotOsmium", Character.valueOf('E'), Mekanism.EnrichedAlloy, Character.valueOf('e'), Mekanism.EnrichedIron, Character.valueOf('I'), Item.ingotIron, Character.valueOf('R'), Item.redstone
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Generator, 1, 1), new Object[] {
			"SSS", "AIA", "PEP", Character.valueOf('S'), SolarPanel, Character.valueOf('A'), Mekanism.EnrichedAlloy, Character.valueOf('I'), Item.ingotIron, Character.valueOf('P'), "dustOsmium", Character.valueOf('E'), Mekanism.EnergyTablet.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Generator, 1, 5), new Object[] {
			"SES", "SES", "III", Character.valueOf('S'), new ItemStack(Generator, 1, 1), Character.valueOf('E'), Mekanism.EnrichedAlloy, Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Generator, 1, 4), new Object[] {
			"RER", "BIB", "NEN", Character.valueOf('R'), Item.redstone, Character.valueOf('E'), Mekanism.EnrichedAlloy, Character.valueOf('B'), BioFuel, Character.valueOf('I'), new ItemStack(Mekanism.BasicBlock, 1, 8), Character.valueOf('N'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Generator, 1, 2), new Object[] {
			"IRI", "ECE", "IRI", Character.valueOf('I'), Item.ingotIron, Character.valueOf('R'), Item.redstone, Character.valueOf('E'), Mekanism.EnrichedAlloy, Character.valueOf('C'), ElectrolyticCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Generator, 1, 3), new Object[] {
			"PEP", "ICI", "PEP", Character.valueOf('P'), "ingotOsmium", Character.valueOf('E'), Mekanism.EnrichedAlloy, Character.valueOf('I'), new ItemStack(Mekanism.BasicBlock, 1, 8), Character.valueOf('C'), ElectrolyticCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ElectrolyticCore), new Object[] {
			"EPE", "IEG", "EPE", Character.valueOf('E'), Mekanism.EnrichedAlloy, Character.valueOf('P'), "dustOsmium", Character.valueOf('I'), "dustIron", Character.valueOf('G'), "dustGold" 
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SolarPanel), new Object[] {
			"GGG", "RAR", "PPP", Character.valueOf('G'), Block.thinGlass, Character.valueOf('R'), Item.redstone, Character.valueOf('A'), Mekanism.EnrichedAlloy, Character.valueOf('P'), "ingotOsmium"
		}));
		
		//BioFuel Crusher Recipes
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.sapling), new ItemStack(BioFuel, 2));
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.tallGrass), new ItemStack(BioFuel, 4));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.reed), new ItemStack(BioFuel, 2));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.seeds), new ItemStack(BioFuel, 2));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.wheat), new ItemStack(BioFuel, 4));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.pumpkinSeeds), new ItemStack(BioFuel, 2));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.melonSeeds), new ItemStack(BioFuel, 2));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.appleRed), new ItemStack(BioFuel, 4));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.bread), new ItemStack(BioFuel, 4));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.potato), new ItemStack(BioFuel, 4));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.carrot), new ItemStack(BioFuel, 4));
        
        for(int i = 0; i < BlockLeaves.LEAF_TYPES.length; i++)
        {
        	RecipeHandler.addCrusherRecipe(new ItemStack(Block.sapling, 1, i), new ItemStack(BioFuel, 2));
        }
        
        Mekanism.infusions.put(new ItemStack(BioFuel), new InfuseObject(InfusionType.BIO, 5));
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
	
	public void addEntities()
	{
		GameRegistry.registerTileEntity(TileEntitySolarGenerator.class, "SolarGenerator");
	}
	
	public void addBlocks()
	{
		//Declarations
		Generator = new BlockGenerator(generatorID).setUnlocalizedName("Generator");
		
		Item.itemsList[generatorID] = new ItemBlockGenerator(generatorID - 256, Generator).setUnlocalizedName("Generator");
	}
	
	public void addItems()
	{
		//Declarations
		Mekanism.configuration.load();
		SolarPanel = new ItemMekanism(Mekanism.configuration.getItem("SolarPanel", 11300).getInt()).setUnlocalizedName("SolarPanel");
		BioFuel = new ItemMekanism(Mekanism.configuration.getItem("BioFuel", 11301).getInt()).setUnlocalizedName("BioFuel");
		ElectrolyticCore = new ItemMekanism(Mekanism.configuration.getItem("ElectrolyticCore", 11302).getInt()).setUnlocalizedName("ElectrolyticCore");
		Mekanism.configuration.save();
		
		//Registrations
		GameRegistry.registerItem(SolarPanel, "SolarPanel");
		GameRegistry.registerItem(BioFuel, "BioFuel");
		GameRegistry.registerItem(ElectrolyticCore, "ElectrolyticCore");
		
		
		//Ore Dictionary
		OreDictionary.registerOre("itemBioFuel", new ItemStack(BioFuel));
	}

	@Override
	public Version getVersion() 
	{
		return versionNumber;
	}

	@Override
	public String getName()
	{
		return "Generators";
	}
}
