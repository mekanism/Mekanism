package mekanism.generators.common;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import mekanism.api.MekanismConfig.general;
import mekanism.api.MekanismConfig.generators;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.common.FuelHandler;
import mekanism.common.IModule;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.Version;
import mekanism.common.recipe.MekanismRecipe;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import buildcraft.api.fuels.IronEngineFuel;
import cpw.mods.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModAPIManager;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(modid = "MekanismGenerators", name = "MekanismGenerators", version = "8.0.0", dependencies = "required-after:Mekanism", guiFactory = "mekanism.generators.client.gui.GeneratorsGuiFactory")
public class MekanismGenerators implements IModule
{
	/** Mekanism Generators Packet Pipeline */
	public static GeneratorsPacketHandler packetHandler = new GeneratorsPacketHandler();

	@SidedProxy(clientSide = "mekanism.generators.client.GeneratorsClientProxy", serverSide = "mekanism.generators.common.GeneratorsCommonProxy")
	public static GeneratorsCommonProxy proxy;
	
	@Instance("MekanismGenerators")
	public static MekanismGenerators instance;
	
	/** MekanismGenerators version number */
	public static Version versionNumber = new Version(8, 0, 0);

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

		packetHandler.initialize();
		
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
		if(ModAPIManager.INSTANCE.hasAPI("BuildCraftAPI|fuels"))
		{
			for(String s : IronEngineFuel.fuels.keySet())
			{
				Fluid f = FluidRegistry.getFluid(s);

				if(!(f == null || GasRegistry.containsGas(s)))
				{
					GasRegistry.register(new Gas(f));
				}
			}

			IronEngineFuel.addFuel("ethene", (float) (240 * general.TO_BC), 40 * FluidContainerRegistry.BUCKET_VOLUME);
		}
	}

	public void addRecipes()
	{
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 0), new Object[] {
			"III", "WOW", "CFC", Character.valueOf('I'), "ingotIron", Character.valueOf('C'), "ingotCopper", Character.valueOf('O'), "ingotOsmium", Character.valueOf('F'), Blocks.furnace, Character.valueOf('W'), Blocks.planks
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 1), new Object[] {
			"SSS", "AIA", "PEP", Character.valueOf('S'), GeneratorsItems.SolarPanel, Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('I'), Items.iron_ingot, Character.valueOf('P'), "dustOsmium", Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 5), new Object[] {
			"SES", "SES", "III", Character.valueOf('S'), new ItemStack(GeneratorsBlocks.Generator, 1, 1), Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('I'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 4), new Object[] {
			"RER", "BCB", "NEN", Character.valueOf('R'), Items.redstone, Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('B'), MekanismItems.BioFuel, Character.valueOf('C'), "circuitBasic", Character.valueOf('N'), Items.iron_ingot
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 3), new Object[] {
			"PEP", "ICI", "PEP", Character.valueOf('P'), "ingotOsmium", Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), Character.valueOf('C'), MekanismItems.ElectrolyticCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GeneratorsItems.SolarPanel), new Object[] {
			"GGG", "RAR", "PPP", Character.valueOf('G'), Blocks.glass_pane, Character.valueOf('R'), Items.redstone, Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('P'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 6), new Object[] {
			" O ", "OAO", "ECE", Character.valueOf('O'), "ingotOsmium", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('C'), "circuitBasic"
		}));

		FuelHandler.addGas(GasRegistry.getGas("ethene"), 40, general.FROM_H2 + generators.bioGeneration * 80); //1mB hydrogen + 2*bioFuel/tick*200ticks/100mB * 20x efficiency bonus

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
