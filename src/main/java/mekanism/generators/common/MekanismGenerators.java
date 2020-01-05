package mekanism.generators.common;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import mekanism.api.MekanismConfig.general;
import mekanism.api.MekanismConfig.generators;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.common.FuelHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.GasTankTier;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.ShapedMekanismRecipe;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.fuels.IFuel;
import cpw.mods.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(modid = "MekanismGenerators", name = "MekanismGenerators", version = "9.1.1", dependencies = "required-after:Mekanism", guiFactory = "mekanism.generators.client.gui.GeneratorsGuiFactory")
public class MekanismGenerators implements IModule
{
	@SidedProxy(clientSide = "mekanism.generators.client.GeneratorsClientProxy", serverSide = "mekanism.generators.common.GeneratorsCommonProxy")
	public static GeneratorsCommonProxy proxy;
	
	@Instance("MekanismGenerators")
	public static MekanismGenerators instance;
	
	/** MekanismGenerators version number */
	public static Version versionNumber = new Version(9, 1, 1);
	
	public static MultiblockManager<SynchronizedTurbineData> turbineManager = new MultiblockManager<SynchronizedTurbineData>("industrialTurbine");

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		GeneratorsBlocks.register();
		GeneratorsItems.register();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		//Add this module to the core list
		Mekanism.modulesLoaded.add(this);
		
		//Register this module's GUI handler in the simple packet protocol
		PacketSimpleGui.handlers.add(1, proxy);
		
		//Set up the GUI handler
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GeneratorsGuiHandler());
		FMLCommonHandler.instance().bus().register(this);

		//Load the proxy
		proxy.loadConfiguration();
		proxy.registerRegularTileEntities();
		proxy.registerSpecialTileEntities();
		proxy.registerRenderInformation();
		
		addRecipes();
		
		//Finalization
		Mekanism.logger.info("Loaded MekanismGenerators module.");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		if(FuelHandler.BCPresent() && BuildcraftFuelRegistry.fuel != null)
		{
			for(IFuel s : BuildcraftFuelRegistry.fuel.getFuels())
			{
				if(!(s.getFluid() == null || GasRegistry.containsGas(s.getFluid().getName())))
				{
					GasRegistry.register(new Gas(s.getFluid()));
				}
			}

			BuildcraftFuelRegistry.fuel.addFuel(FluidRegistry.getFluid("ethene"), (int)(240 * general.TO_TE), 40 * FluidContainerRegistry.BUCKET_VOLUME);
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustGold"))
		{
			RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("CARBON"), 10, MekanismUtils.size(ore, 4), GeneratorsItems.Hohlraum.getEmptyItem());
		}
	}
	
	public void addRecipes()
	{
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 0), new Object[] {
			"III", "WOW", "CFC", Character.valueOf('I'), "ingotIron", Character.valueOf('C'), "ingotCopper", Character.valueOf('O'), "ingotOsmium", Character.valueOf('F'), Blocks.furnace, Character.valueOf('W'), "plankWood"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 1), new Object[] {
			"SSS", "AIA", "PEP", Character.valueOf('S'), GeneratorsItems.SolarPanel, Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('I'), "ingotIron", Character.valueOf('P'), "dustOsmium", Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 5), new Object[] {
			"SES", "SES", "III", Character.valueOf('S'), new ItemStack(GeneratorsBlocks.Generator, 1, 1), Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('I'), "ingotIron"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 4), new Object[] {
			"RER", "BCB", "NEN", Character.valueOf('R'), "dustRedstone", Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('B'), MekanismItems.BioFuel, Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('N'), "ingotIron"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 3), new Object[] {
			"PEP", "ICI", "PEP", Character.valueOf('P'), "ingotOsmium", Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), Character.valueOf('C'), MekanismItems.ElectrolyticCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsItems.SolarPanel), new Object[] {
			"GGG", "RAR", "PPP", Character.valueOf('G'), "paneGlass", Character.valueOf('R'), "dustRedstone", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('P'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 6), new Object[] {
			" O ", "OAO", "ECE", Character.valueOf('O'), "ingotOsmium", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsItems.TurbineBlade), new Object[] {
			" S ", "SAS", " S ", Character.valueOf('S'), "ingotSteel", Character.valueOf('A'), MekanismItems.EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 7), new Object[] {
			"SAS", "SAS", "SAS", Character.valueOf('S'), "ingotSteel", Character.valueOf('A'), MekanismItems.EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 8), new Object[] {
			"SAS", "CAC", "SAS", Character.valueOf('S'), "ingotSteel", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ADVANCED)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 9), new Object[] {
			"SGS", "GEG", "SGS", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), "ingotGold", Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 4, 10), new Object[] {
			" S ", "SOS", " S ", Character.valueOf('S'), "ingotSteel", Character.valueOf('O'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 2, 11), new Object[] {
			" I ", "ICI", " I ", Character.valueOf('I'), new ItemStack(GeneratorsBlocks.Generator, 1, 10), Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ADVANCED)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 2, 12), new Object[] {
			" I ", "IFI", " I ", Character.valueOf('I'), new ItemStack(GeneratorsBlocks.Generator, 1, 10), Character.valueOf('F'), Blocks.iron_bars
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 13), new Object[] {
			"STS", "TBT", "STS", Character.valueOf('S'), "ingotSteel", Character.valueOf('T'), "ingotTin", Character.valueOf('B'), Items.bucket
		}));
		
		//Reactor Recipes
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Reactor, 4, 1), new Object[] {
			" C ", "CAC", " C ", Character.valueOf('C'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), Character.valueOf('A'), "alloyUltimate"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Reactor, 2, 3), new Object[] {
			" I ", "ICI", " I ", Character.valueOf('I'), new ItemStack(GeneratorsBlocks.Reactor, 1, 1), Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ULTIMATE)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.ReactorGlass, 4, 0), new Object[] {
			" I ", "IGI", " I ", Character.valueOf('I'), new ItemStack(GeneratorsBlocks.Reactor, 1, 1), Character.valueOf('G'), "blockGlass"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Reactor, 1, 0), new Object[] {
			"CGC", "ITI", "III", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ULTIMATE), Character.valueOf('G'), "paneGlass", Character.valueOf('I'), new ItemStack(GeneratorsBlocks.Reactor, 1, 1), Character.valueOf('T'), MekanismUtils.getEmptyGasTank(GasTankTier.BASIC)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.ReactorGlass, 2, 1), new Object[] {
			" I ", "ILI", " I ", Character.valueOf('I'), new ItemStack(GeneratorsBlocks.ReactorGlass, 1, 0), Character.valueOf('L'), "blockRedstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Reactor, 1, 4), new Object[] {
			" R ", "RFR", " R ", Character.valueOf('R'), "dustRedstone", Character.valueOf('F'), new ItemStack(GeneratorsBlocks.Reactor, 1, 1)
		}));

		FuelHandler.addGas(GasRegistry.getGas("ethene"), general.ETHENE_BURN_TIME, general.FROM_H2 + generators.bioGeneration * 2 * general.ETHENE_BURN_TIME); //1mB hydrogen + 2*bioFuel/tick*200ticks/100mB * 20x efficiency bonus
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
	
	@Override
	public void writeConfig(ByteBuf dataStream) throws IOException
	{
		dataStream.writeDouble(generators.advancedSolarGeneration);
		dataStream.writeDouble(generators.bioGeneration);
		dataStream.writeDouble(generators.heatGeneration);
		dataStream.writeDouble(generators.heatGenerationLava);
		dataStream.writeDouble(generators.heatGenerationNether);
		dataStream.writeDouble(generators.solarGeneration);
		
		dataStream.writeDouble(generators.windGenerationMin);
		dataStream.writeDouble(generators.windGenerationMax);
		
		dataStream.writeInt(generators.windGenerationMinY);
		dataStream.writeInt(generators.windGenerationMaxY);
		
		dataStream.writeInt(generators.turbineBladesPerCoil);
		dataStream.writeDouble(generators.turbineVentGasFlow);
		dataStream.writeDouble(generators.turbineDisperserGasFlow);
		dataStream.writeInt(generators.condenserRate);
	}

	@Override
	public void readConfig(ByteBuf dataStream) throws IOException
	{
		generators.advancedSolarGeneration = dataStream.readDouble();
		generators.bioGeneration = dataStream.readDouble();
		generators.heatGeneration = dataStream.readDouble();
		generators.heatGenerationLava = dataStream.readDouble();
		generators.heatGenerationNether = dataStream.readDouble();
		generators.solarGeneration = dataStream.readDouble();
		
		generators.windGenerationMin = dataStream.readDouble();
		generators.windGenerationMax = dataStream.readDouble();
		
		generators.windGenerationMinY = dataStream.readInt();
		generators.windGenerationMaxY = dataStream.readInt();
		
		generators.turbineBladesPerCoil = dataStream.readInt();
		generators.turbineVentGasFlow = dataStream.readDouble();
		generators.turbineDisperserGasFlow = dataStream.readDouble();
		generators.condenserRate = dataStream.readInt();
	}
	
	@Override
	public void resetClient()
	{
		SynchronizedTurbineData.clientRotationMap.clear();
	}

	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event)
	{
		if(event.modID.equals("MekanismGenerators"))
		{
			proxy.loadConfiguration();
		}
	}
}
