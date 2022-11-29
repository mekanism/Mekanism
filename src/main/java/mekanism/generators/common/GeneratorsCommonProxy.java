package mekanism.generators.common;

import mekanism.api.MekanismConfig.generators;
import mekanism.api.MekanismConfig.generatorsrecipes;
import mekanism.common.Mekanism;
import mekanism.common.base.IGuiProvider;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.generators.common.inventory.container.ContainerBioGenerator;
import mekanism.generators.common.inventory.container.ContainerGasGenerator;
import mekanism.generators.common.inventory.container.ContainerHeatGenerator;
import mekanism.generators.common.inventory.container.ContainerNeutronCapture;
import mekanism.generators.common.inventory.container.ContainerReactorController;
import mekanism.generators.common.inventory.container.ContainerSolarGenerator;
import mekanism.generators.common.inventory.container.ContainerWindGenerator;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import mekanism.generators.common.tile.reactor.TileEntityReactorFrame;
import mekanism.generators.common.tile.reactor.TileEntityReactorGlass;
import mekanism.generators.common.tile.reactor.TileEntityReactorLaserFocusMatrix;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorNeutronCapture;
import mekanism.generators.common.tile.reactor.TileEntityReactorPort;
import mekanism.generators.common.tile.turbine.TileEntityElectromagneticCoil;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntitySaturatingCondenser;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import mekanism.generators.common.tile.turbine.TileEntityTurbineValve;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Common proxy for the Mekanism Generators module.
 * @author AidanBrady
 *
 */
public class GeneratorsCommonProxy implements IGuiProvider
{
	public static int GENERATOR_RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
	public static List<Integer> dimid = null;

	/**
	 * Register normal tile entities
	 */
	public void registerRegularTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntityReactorFrame.class, "ReactorFrame");
		GameRegistry.registerTileEntity(TileEntityReactorGlass.class, "ReactorGlass");
		GameRegistry.registerTileEntity(TileEntityReactorLaserFocusMatrix.class, "ReactorLaserFocus");
		GameRegistry.registerTileEntity(TileEntityReactorNeutronCapture.class, "ReactorNeutronCapture");
		GameRegistry.registerTileEntity(TileEntityReactorPort.class, "ReactorPort");
		GameRegistry.registerTileEntity(TileEntityReactorLogicAdapter.class, "ReactorLogicAdapter");
		GameRegistry.registerTileEntity(TileEntityRotationalComplex.class, "RotationalComplex");
		GameRegistry.registerTileEntity(TileEntityElectromagneticCoil.class, "ElectromagneticCoil");
		GameRegistry.registerTileEntity(TileEntitySaturatingCondenser.class, "SaturatingCondenser");
	}

	/**
	 * Register tile entities that have special models. Overwritten in client to register TESRs.
	 */
	public void registerSpecialTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntityAdvancedSolarGenerator.class, "AdvancedSolarGenerator");
		GameRegistry.registerTileEntity(TileEntitySolarGenerator.class, "SolarGenerator");
		GameRegistry.registerTileEntity(TileEntityBioGenerator.class, "BioGenerator");
		GameRegistry.registerTileEntity(TileEntityHeatGenerator.class, "HeatGenerator");
		GameRegistry.registerTileEntity(TileEntityGasGenerator.class, "GasGenerator");
		GameRegistry.registerTileEntity(TileEntityWindGenerator.class, "WindTurbine");
		GameRegistry.registerTileEntity(TileEntityReactorController.class, "ReactorController");
		GameRegistry.registerTileEntity(TileEntityTurbineRotor.class, "TurbineRod");
		GameRegistry.registerTileEntity(TileEntityTurbineCasing.class, "TurbineCasing");
		GameRegistry.registerTileEntity(TileEntityTurbineValve.class, "TurbineValve");
		GameRegistry.registerTileEntity(TileEntityTurbineVent.class, "TurbineVent");
	}

	/**
	 * Register and load client-only render information.
	 */
	public void registerRenderInformation() {}

	/**
	 * Set and load the mod's common configuration properties.
	 */
	public void loadConfiguration()
	{
		generators.advancedSolarGeneration = Mekanism.configurationgenerators.get("generation", "AdvancedSolarGeneration", 300D).getDouble();
		generators.bioGeneration = Mekanism.configurationgenerators.get("generation", "BioGeneration", 100D).getDouble();
		generators.ethanolMultiplier = Mekanism.configurationgenerators.get("generation", "EthanolMultiplier", 5D).getDouble();
		generators.heatGeneration = Mekanism.configurationgenerators.get("generation", "HeatGeneration", 150D).getDouble();
		generators.heatGenerationLava = Mekanism.configurationgenerators.get("generation", "HeatGenerationLava", 5D).getDouble();
		generators.heatGenerationNether = Mekanism.configurationgenerators.get("generation", "HeatGenerationNether", 100D).getDouble();
		generators.heatGenerationFluidRate = Mekanism.configurationgenerators.get("generation", "HeatGenerationFluidRate", 10).getInt();
		generators.solarGeneration = Mekanism.configurationgenerators.get("generation", "SolarGeneration", 50D).getDouble();
		
		loadWindConfiguration();
		loadwinddimension();
		generators.turbineBladesPerCoil = Mekanism.configurationgenerators.get("generation", "TurbineBladesPerCoil", 4).getInt();
		generators.turbineVentGasFlow = Mekanism.configurationgenerators.get("generation", "TurbineVentGasFlow", 16000D).getDouble();
		generators.turbineDisperserGasFlow = Mekanism.configurationgenerators.get("generation", "TurbineDisperserGasFlow", 640D).getDouble();
		generators.condenserRate = Mekanism.configurationgenerators.get("generation", "TurbineCondenserFlowRate", 32000).getInt();
		generators.enableWindmillWhitelist = Mekanism.configurationgenerators.get("generation", "EnableWindmillWhitelist", true).getBoolean();

		generatorsrecipes.enableHeatGenerator = Mekanism.configurationrecipes.get("generationrecipes","enableHeatGenerator", true).getBoolean();
		generatorsrecipes.enableSolarGenerator = Mekanism.configurationrecipes.get("generationrecipes","enableSolarGenerator", true).getBoolean();
		generatorsrecipes.enableGasGenerator = Mekanism.configurationrecipes.get("generationrecipes","enableGasGenerator", true).getBoolean();
		generatorsrecipes.enableBioGenerator = Mekanism.configurationrecipes.get("generationrecipes","enableBioGenerator", true).getBoolean();
		generatorsrecipes.enableAdvSolarGenerator = Mekanism.configurationrecipes.get("generationrecipes","enableAdvSolarGenerator", true).getBoolean();
		generatorsrecipes.enableWindGenerator = Mekanism.configurationrecipes.get("generationrecipes","enableWindGenerator", true).getBoolean();
		generatorsrecipes.enableTurbineRotor = Mekanism.configurationrecipes.get("generationrecipes","enableTurbineRotor", true).getBoolean();
		generatorsrecipes.enableRotationalComplex = Mekanism.configurationrecipes.get("generationrecipes","enableRotationalComplex", true).getBoolean();
		generatorsrecipes.enableElectromagneticCoil = Mekanism.configurationrecipes.get("generationrecipes","enableElectromagneticCoil", true).getBoolean();
		generatorsrecipes.enableTurbineCasing = Mekanism.configurationrecipes.get("generationrecipes","enableTurbineCasing", true).getBoolean();
		generatorsrecipes.enableTurbineValve = Mekanism.configurationrecipes.get("generationrecipes","enableTurbineValve", true).getBoolean();
		generatorsrecipes.enableTurbineVent = Mekanism.configurationrecipes.get("generationrecipes","enableTurbineVent", true).getBoolean();
		generatorsrecipes.enableSaturatingCondenser = Mekanism.configurationrecipes.get("generationrecipes","enableSaturatingCondenser", true).getBoolean();
		generatorsrecipes.enableReactorController = Mekanism.configurationrecipes.get("generationrecipes","enableReactorController", true).getBoolean();
		generatorsrecipes.enableReactorFrame = Mekanism.configurationrecipes.get("generationrecipes","enableReactorFrame", true).getBoolean();
		generatorsrecipes.enableReactorPort = Mekanism.configurationrecipes.get("generationrecipes","enableReactorPort", true).getBoolean();
		generatorsrecipes.enableReactorAdapter = Mekanism.configurationrecipes.get("generationrecipes","enableReactorAdapter", true).getBoolean();
		generatorsrecipes.enableReactorGlass = Mekanism.configurationrecipes.get("generationrecipes","enableReactorGlass", true).getBoolean();
		generatorsrecipes.enableReactorMatrix = Mekanism.configurationrecipes.get("generationrecipes","enableReactorMatrix", true).getBoolean();
		generatorsrecipes.enableSolarPanel = Mekanism.configurationrecipes.get("generationrecipes","enableSolarPanel", true).getBoolean();
		generatorsrecipes.enableTurbineBlade = Mekanism.configurationrecipes.get("generationrecipes","enableTurbineBlade", true).getBoolean();
		if(Mekanism.configurationgenerators.hasChanged())
		{
			Mekanism.configurationgenerators.save();
		}
		if(Mekanism.configurationrecipes.hasChanged())
		{
			Mekanism.configurationrecipes.save();
		}
	}

	public void loadwinddimension()
	{
		String[] windid = {"0"};
		generators.winddimensionids = Arrays.asList(Mekanism.configurationgenerators.getStringList("winddimensionids", "generation", windid, "List of dimension id to be whitelisted"));
		dimid = generators.winddimensionids.stream().map(Integer::parseInt).collect(Collectors.toList());
		System.out.println("Windmill whitelist : " + dimid);
	}

	private void loadWindConfiguration() 
	{
		generators.windGenerationMin = Mekanism.configurationgenerators.get("generation", "WindGenerationMin", 60D).getDouble();
		generators.windGenerationMax = Mekanism.configurationgenerators.get("generation", "WindGenerationMax", 480D).getDouble();

		//Ensure max > min to avoid division by zero later
		final int minY = Mekanism.configurationgenerators.get("generation", "WindGenerationMinY", 24).getInt();
		final int maxY = Mekanism.configurationgenerators.get("generation", "WindGenerationMaxY", 255).getInt();

		generators.windGenerationMinY = minY;
		generators.windGenerationMaxY = Math.max(minY + 1, maxY);
	}

	@Override
	public Object getClientGui(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	@Override
	public Container getServerGui(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);

		switch(ID)
		{
			case 0:
				return new ContainerHeatGenerator(player.inventory, (TileEntityHeatGenerator)tileEntity);
			case 1:
				return new ContainerSolarGenerator(player.inventory, (TileEntitySolarGenerator)tileEntity);
			case 3:
				return new ContainerGasGenerator(player.inventory, (TileEntityGasGenerator)tileEntity);
			case 4:
				return new ContainerBioGenerator(player.inventory, (TileEntityBioGenerator)tileEntity);
			case 5:
				return new ContainerWindGenerator(player.inventory, (TileEntityWindGenerator)tileEntity);
			case 6:
				return new ContainerFilter(player.inventory, (TileEntityTurbineCasing)tileEntity);
			case 7:
				return new ContainerNull(player, (TileEntityTurbineCasing)tileEntity);
			case 10:
				return new ContainerReactorController(player.inventory, (TileEntityReactorController)tileEntity);
			case 11:
			case 12:
			case 13:
				return new ContainerNull(player, (TileEntityContainerBlock)tileEntity);
			case 14:
				return new ContainerNeutronCapture(player.inventory, (TileEntityReactorNeutronCapture)tileEntity);
			case 15:
				return new ContainerNull(player, (TileEntityContainerBlock)tileEntity);
		}
		
		return null;
	}
}
