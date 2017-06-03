package mekanism.generators.common;

import mekanism.common.Mekanism;
import mekanism.common.base.IGuiProvider;
import mekanism.common.config.MekanismConfig.generators;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.generators.common.block.states.BlockStateGenerator.GeneratorType;
import mekanism.generators.common.inventory.container.ContainerBioGenerator;
import mekanism.generators.common.inventory.container.ContainerGasGenerator;
import mekanism.generators.common.inventory.container.ContainerHeatGenerator;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Common proxy for the Mekanism Generators module.
 * @author AidanBrady
 *
 */
public class GeneratorsCommonProxy implements IGuiProvider
{
	/**
	 * Register normal tile entities
	 */
	public void registerRegularTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntityReactorFrame.class, "ReactorFrame");
		GameRegistry.registerTileEntity(TileEntityReactorGlass.class, "ReactorGlass");
		GameRegistry.registerTileEntity(TileEntityReactorLaserFocusMatrix.class, "ReactorLaserFocus");
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
	 * Register and load client-only item render information.
	 */
	public void registerItemRenders() {}
	
	/**
	 * Register and load client-only block render information.
	 */
	public void registerBlockRenders() {}
	
	public void preInit() {}

	/**
	 * Set and load the mod's common configuration properties.
	 */
	public void loadConfiguration()
	{
		generators.advancedSolarGeneration = Mekanism.configuration.get("generation", "AdvancedSolarGeneration", 300D).getDouble();
		generators.bioGeneration = Mekanism.configuration.get("generation", "BioGeneration", 350D).getDouble();
		generators.heatGeneration = Mekanism.configuration.get("generation", "HeatGeneration", 150D).getDouble();
		generators.heatGenerationLava = Mekanism.configuration.get("generation", "HeatGenerationLava", 5D).getDouble();
		generators.heatGenerationNether = Mekanism.configuration.get("generation", "HeatGenerationNether", 100D).getDouble();
		generators.solarGeneration = Mekanism.configuration.get("generation", "SolarGeneration", 50D).getDouble();
		
		loadWindConfiguration();
		
		generators.turbineBladesPerCoil = Mekanism.configuration.get("generation", "TurbineBladesPerCoil", 4).getInt();
		generators.turbineVentGasFlow = Mekanism.configuration.get("generation", "TurbineVentGasFlow", 16000D).getDouble();
		generators.turbineDisperserGasFlow = Mekanism.configuration.get("generation", "TurbineDisperserGasFlow", 640D).getDouble();
		generators.condenserRate = Mekanism.configuration.get("generation", "TurbineCondenserFlowRate", 32000).getInt();
		
		generators.energyPerFusionFuel = Mekanism.configuration.get("generation", "EnergyPerFusionFuel", 5E6D).getDouble();
		
		for(GeneratorType type : GeneratorType.getGeneratorsForConfig())
		{
			generators.generatorsManager.setEntry(type.blockName, Mekanism.configuration.get("generators", type.blockName + "Enabled", true).getBoolean());
		}

		if(Mekanism.configuration.hasChanged())
		{
			Mekanism.configuration.save();
		}
	}
	
	private void loadWindConfiguration() 
	{
		generators.windGenerationMin = Mekanism.configuration.get("generation", "WindGenerationMin", 60D).getDouble();
		generators.windGenerationMax = Mekanism.configuration.get("generation", "WindGenerationMax", 480D).getDouble();

		//Ensure max > min to avoid division by zero later
		final int minY = Mekanism.configuration.get("generation", "WindGenerationMinY", 24).getInt();
		final int maxY = Mekanism.configuration.get("generation", "WindGenerationMaxY", 255).getInt();

		generators.windGenerationMinY = minY;
		generators.windGenerationMaxY = Math.max(minY + 1, maxY);
	}

	@Override
	public Object getClientGui(int ID, EntityPlayer player, World world, BlockPos pos)
	{
		return null;
	}

	@Override
	public Container getServerGui(int ID, EntityPlayer player, World world, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

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
			case 15:
				return new ContainerNull(player, (TileEntityContainerBlock)tileEntity);
		}
		
		return null;
	}
}
