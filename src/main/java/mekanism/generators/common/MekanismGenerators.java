package mekanism.generators.common;

import buildcraft.api.mj.MjAPI;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

import mekanism.api.MekanismAPI;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.common.FuelHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismFluids;
import mekanism.common.MekanismItems;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.GasTankTier;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import mekanism.common.config.TypeConfigManager;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.config.MekanismConfig.generators;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.ShapedMekanismRecipe;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.block.states.BlockStateGenerator.GeneratorType;
import mekanism.generators.common.block.states.BlockStateReactor.ReactorBlockType;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.oredict.OreDictionary;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.fuels.IFuel;

@Mod(modid = "mekanismgenerators", name = "MekanismGenerators", version = "9.3.5", dependencies = "required-after:mekanism", guiFactory = "mekanism.generators.client.gui.GeneratorsGuiFactory")
public class MekanismGenerators implements IModule
{
	@SidedProxy(clientSide = "mekanism.generators.client.GeneratorsClientProxy", serverSide = "mekanism.generators.common.GeneratorsCommonProxy")
	public static GeneratorsCommonProxy proxy;
	
	@Instance("mekanismgenerators")
	public static MekanismGenerators instance;
	
	/** MekanismGenerators version number */
	public static Version versionNumber = new Version(9, 3, 5);
	
	public static MultiblockManager<SynchronizedTurbineData> turbineManager = new MultiblockManager<SynchronizedTurbineData>("industrialTurbine");

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		GeneratorsBlocks.register();
		GeneratorsItems.register();
		
		proxy.preInit();
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
		MinecraftForge.EVENT_BUS.register(this);

		//Load the proxy
		proxy.loadConfiguration();
		proxy.registerRegularTileEntities();
		proxy.registerSpecialTileEntities();
		
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
				if(s.getFluid() != null && !GasRegistry.containsGas(s.getFluid().getFluid().getName()))
				{
					GasRegistry.register(new Gas(s.getFluid().getFluid()));
				}
			}

			BuildcraftFuelRegistry.fuel.addFuel(MekanismFluids.Ethene.getFluid(), (long)(240 * general.TO_RF / 20 * MjAPI.MJ), 40 * Fluid.BUCKET_VOLUME);
		}
		
		//Update the config-dependent recipes after the recipes have actually been added in the first place
		TypeConfigManager.updateConfigRecipes(GeneratorType.getGeneratorsForConfig(), generators.generatorsManager);
		
		for(ItemStack ore : OreDictionary.getOres("dustGold"))
		{
			RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("CARBON"), 10, MekanismUtils.size(ore, 4), GeneratorsItems.Hohlraum.getEmptyItem());
		}
	}
	
	public void addRecipes()
	{
		GeneratorType.HEAT_GENERATOR.addRecipe(new ShapedMekanismRecipe(GeneratorType.HEAT_GENERATOR.getStack(), new Object[] {
			"III", "WOW", "CFC", Character.valueOf('I'), "ingotIron", Character.valueOf('C'), "ingotCopper", Character.valueOf('O'), "ingotOsmium", Character.valueOf('F'), Blocks.FURNACE, Character.valueOf('W'), "plankWood"
		}));
		GeneratorType.SOLAR_GENERATOR.addRecipe(new ShapedMekanismRecipe(GeneratorType.SOLAR_GENERATOR.getStack(), new Object[] {
			"SSS", "AIA", "PEP", Character.valueOf('S'), GeneratorsItems.SolarPanel, Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('I'), "ingotIron", Character.valueOf('P'), "dustOsmium", Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem()
		}));
		GeneratorType.ADVANCED_SOLAR_GENERATOR.addRecipe(new ShapedMekanismRecipe(GeneratorType.ADVANCED_SOLAR_GENERATOR.getStack(), new Object[] {
			"SES", "SES", "III", Character.valueOf('S'), GeneratorType.SOLAR_GENERATOR.getStack(), Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('I'), "ingotIron"
		}));
		GeneratorType.BIO_GENERATOR.addRecipe(new ShapedMekanismRecipe(GeneratorType.BIO_GENERATOR.getStack(), new Object[] {
			"RER", "BCB", "NEN", Character.valueOf('R'), "dustRedstone", Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('B'), MekanismItems.BioFuel, Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('N'), "ingotIron"
		}));
		GeneratorType.GAS_GENERATOR.addRecipe(new ShapedMekanismRecipe(GeneratorType.GAS_GENERATOR.getStack(), new Object[] {
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
			" I ", "IFI", " I ", Character.valueOf('I'), new ItemStack(GeneratorsBlocks.Generator, 1, 10), Character.valueOf('F'), Blocks.IRON_BARS
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 13), new Object[] {
			"STS", "TBT", "STS", Character.valueOf('S'), "ingotSteel", Character.valueOf('T'), "ingotTin", Character.valueOf('B'), Items.BUCKET
		}));
		
		//Reactor Recipes
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(ReactorBlockType.REACTOR_FRAME.getStack(4), new Object[] {
			" C ", "CAC", " C ", Character.valueOf('C'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), Character.valueOf('A'), "alloyUltimate"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(ReactorBlockType.REACTOR_PORT.getStack(2), new Object[] {
			" I ", "ICI", " I ", Character.valueOf('I'), ReactorBlockType.REACTOR_FRAME.getStack(1), Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ULTIMATE)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(ReactorBlockType.REACTOR_GLASS.getStack(4), new Object[] {
			" I ", "IGI", " I ", Character.valueOf('I'), ReactorBlockType.REACTOR_FRAME.getStack(1), Character.valueOf('G'), "blockGlass"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(ReactorBlockType.REACTOR_CONTROLLER.getStack(1), new Object[] {
			"CGC", "ITI", "III", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ULTIMATE), Character.valueOf('G'), "paneGlass", Character.valueOf('I'), ReactorBlockType.REACTOR_FRAME.getStack(1), Character.valueOf('T'), MekanismUtils.getEmptyGasTank(GasTankTier.BASIC)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(ReactorBlockType.LASER_FOCUS_MATRIX.getStack(2), new Object[] {
			" I ", "ILI", " I ", Character.valueOf('I'), ReactorBlockType.REACTOR_GLASS.getStack(1), Character.valueOf('L'), "blockRedstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(ReactorBlockType.REACTOR_LOGIC_ADAPTER.getStack(1), new Object[] {
			" R ", "RFR", " R ", Character.valueOf('R'), "dustRedstone", Character.valueOf('F'), ReactorBlockType.REACTOR_FRAME.getStack(1)
		}));

		FuelHandler.addGas(MekanismFluids.Ethene, general.ETHENE_BURN_TIME, general.FROM_H2 + generators.bioGeneration * 2 * general.ETHENE_BURN_TIME); //1mB hydrogen + 2*bioFuel/tick*200ticks/100mB * 20x efficiency bonus
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
		
		dataStream.writeDouble(generators.energyPerFusionFuel);
		
		for(GeneratorType type : GeneratorType.getGeneratorsForConfig())
		{
			dataStream.writeBoolean(generators.generatorsManager.isEnabled(type.blockName));
		}
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
		
		generators.energyPerFusionFuel = dataStream.readDouble();
		
		for(GeneratorType type : GeneratorType.getGeneratorsForConfig())
		{
			generators.generatorsManager.setEntry(type.blockName, dataStream.readBoolean());
		}
	}
	
	@Override
	public void resetClient()
	{
		SynchronizedTurbineData.clientRotationMap.clear();
	}

	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event)
	{
		if(event.getModID().equals("mekanismgenerators"))
		{
			proxy.loadConfiguration();
			TypeConfigManager.updateConfigRecipes(GeneratorType.getGeneratorsForConfig(), generators.generatorsManager);
		}
	}

	@SubscribeEvent
	public void onBlacklistUpdate(MekanismAPI.BoxBlacklistEvent event)
	{
		// Mekanism Generators multiblock structures
		MekanismAPI.addBoxBlacklist(GeneratorsBlocks.Generator, 5); // Advanced Solar Generator
		MekanismAPI.addBoxBlacklist(GeneratorsBlocks.Generator, 6); // Wind Generator
	}
}
