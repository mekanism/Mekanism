package mekanism.client;

import static mekanism.common.block.states.BlockStatePlastic.colorProperty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mcmultipart.client.multipart.MultipartRegistryClient;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.MekanismConfig.client;
import mekanism.api.MekanismConfig.general;
import mekanism.api.Pos3D;
import mekanism.client.SparkleAnimation.INodeChecker;
import mekanism.client.entity.ParticleLaser;
import mekanism.client.gui.GuiAmbientAccumulator;
import mekanism.client.gui.GuiBoilerStats;
import mekanism.client.gui.GuiChemicalCrystallizer;
import mekanism.client.gui.GuiChemicalDissolutionChamber;
import mekanism.client.gui.GuiChemicalInfuser;
import mekanism.client.gui.GuiChemicalInjectionChamber;
import mekanism.client.gui.GuiChemicalOxidizer;
import mekanism.client.gui.GuiChemicalWasher;
import mekanism.client.gui.GuiCombiner;
import mekanism.client.gui.GuiCredits;
import mekanism.client.gui.GuiCrusher;
import mekanism.client.gui.GuiDictionary;
import mekanism.client.gui.GuiDigitalMiner;
import mekanism.client.gui.GuiDynamicTank;
import mekanism.client.gui.GuiElectricPump;
import mekanism.client.gui.GuiElectrolyticSeparator;
import mekanism.client.gui.GuiEnergizedSmelter;
import mekanism.client.gui.GuiEnergyCube;
import mekanism.client.gui.GuiEnrichmentChamber;
import mekanism.client.gui.GuiFactory;
import mekanism.client.gui.GuiFluidTank;
import mekanism.client.gui.GuiFluidicPlenisher;
import mekanism.client.gui.GuiFormulaicAssemblicator;
import mekanism.client.gui.GuiFuelwoodHeater;
import mekanism.client.gui.GuiGasTank;
import mekanism.client.gui.GuiInductionMatrix;
import mekanism.client.gui.GuiLaserAmplifier;
import mekanism.client.gui.GuiLaserTractorBeam;
import mekanism.client.gui.GuiMatrixStats;
import mekanism.client.gui.GuiMetallurgicInfuser;
import mekanism.client.gui.GuiOredictionificator;
import mekanism.client.gui.GuiOsmiumCompressor;
import mekanism.client.gui.GuiPRC;
import mekanism.client.gui.GuiPersonalChest;
import mekanism.client.gui.GuiPrecisionSawmill;
import mekanism.client.gui.GuiPurificationChamber;
import mekanism.client.gui.GuiQuantumEntangloporter;
import mekanism.client.gui.GuiResistiveHeater;
import mekanism.client.gui.GuiRobitCrafting;
import mekanism.client.gui.GuiRobitInventory;
import mekanism.client.gui.GuiRobitMain;
import mekanism.client.gui.GuiRobitRepair;
import mekanism.client.gui.GuiRobitSmelting;
import mekanism.client.gui.GuiRotaryCondensentrator;
import mekanism.client.gui.GuiSecurityDesk;
import mekanism.client.gui.GuiSeismicReader;
import mekanism.client.gui.GuiSeismicVibrator;
import mekanism.client.gui.GuiSideConfiguration;
import mekanism.client.gui.GuiSolarNeutronActivator;
import mekanism.client.gui.GuiTeleporter;
import mekanism.client.gui.GuiThermalEvaporationController;
import mekanism.client.gui.GuiThermoelectricBoiler;
import mekanism.client.gui.GuiTransporterConfig;
import mekanism.client.gui.GuiUpgradeManagement;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.ctm.CTMRegistry;
import mekanism.client.render.entity.RenderBalloon;
import mekanism.client.render.entity.RenderFlame;
import mekanism.client.render.entity.RenderObsidianTNTPrimed;
import mekanism.client.render.entity.RenderRobit;
import mekanism.client.render.item.CustomItemModelFactory;
import mekanism.client.render.obj.MekanismOBJLoader;
import mekanism.client.render.tileentity.RenderBin;
import mekanism.client.render.tileentity.RenderChargepad;
import mekanism.client.render.tileentity.RenderChemicalCrystallizer;
import mekanism.client.render.tileentity.RenderChemicalDissolutionChamber;
import mekanism.client.render.tileentity.RenderConfigurableMachine;
import mekanism.client.render.tileentity.RenderDigitalMiner;
import mekanism.client.render.tileentity.RenderDynamicTank;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.client.render.tileentity.RenderGasTank;
import mekanism.client.render.tileentity.RenderLogisticalSorter;
import mekanism.client.render.tileentity.RenderPersonalChest;
import mekanism.client.render.tileentity.RenderQuantumEntangloporter;
import mekanism.client.render.tileentity.RenderResistiveHeater;
import mekanism.client.render.tileentity.RenderSecurityDesk;
import mekanism.client.render.tileentity.RenderSeismicVibrator;
import mekanism.client.render.tileentity.RenderSolarNeutronActivator;
import mekanism.client.render.tileentity.RenderTeleporter;
import mekanism.client.render.tileentity.RenderThermalEvaporationController;
import mekanism.client.render.tileentity.RenderThermoelectricBoiler;
import mekanism.client.render.transmitter.RenderLogisticalTransporter;
import mekanism.client.render.transmitter.RenderMechanicalPipe;
import mekanism.client.render.transmitter.RenderPressurizedTube;
import mekanism.client.render.transmitter.RenderThermodynamicConductor;
import mekanism.client.render.transmitter.RenderUniversalCable;
import mekanism.common.CommonProxy;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.GasTankTier;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.BlockMachine;
import mekanism.common.block.BlockPlasticFence.PlasticFenceStateMapper;
import mekanism.common.block.states.BlockStateBasic.BasicBlockStateMapper;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateCardboardBox.CardboardBoxStateMapper;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.block.states.BlockStateMachine.MachineBlockStateMapper;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.block.states.BlockStateOre.EnumOreType;
import mekanism.common.block.states.BlockStatePlastic.PlasticBlockStateMapper;
import mekanism.common.entity.EntityBabySkeleton;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.entity.EntityFlame;
import mekanism.common.entity.EntityObsidianTNT;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.InventoryPersonalChest;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.item.ItemBlockGasTank;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemSeismicReader;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multipart.PartDiversionTransporter;
import mekanism.common.multipart.PartLogisticalTransporter;
import mekanism.common.multipart.PartMechanicalPipe;
import mekanism.common.multipart.PartPressurizedTube;
import mekanism.common.multipart.PartRestrictiveTransporter;
import mekanism.common.multipart.PartThermodynamicConductor;
import mekanism.common.multipart.PartUniversalCable;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterMessage;
import mekanism.common.tile.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.TileEntityAdvancedFactory;
import mekanism.common.tile.TileEntityAmbientAccumulator;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.TileEntityBoilerValve;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.TileEntityChemicalInfuser;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.TileEntityChemicalOxidizer;
import mekanism.common.tile.TileEntityChemicalWasher;
import mekanism.common.tile.TileEntityCombiner;
import mekanism.common.tile.TileEntityCrusher;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;
import mekanism.common.tile.TileEntityElectricMachine;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEliteFactory;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.TileEntityFuelwoodHeater;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPRC;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSkeleton;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client proxy for the Mekanism mod.
 * @author AidanBrady
 *
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	public static Map<String, ModelResourceLocation> machineResources = new HashMap<String, ModelResourceLocation>();
	public static Map<String, ModelResourceLocation> basicResources = new HashMap<String, ModelResourceLocation>();
	
	public static final String[] CUSTOM_RENDERS = new String[] {"fluid_tank", "bin_basic", "bin_advanced", "bin_elite", "bin_ultimate", 
		"Jetpack", "FreeRunners", "AtomicDisassembler", "ScubaTank", "GasMask", "ArmoredJetpack", "Flamethrower", "personal_chest",
		"solar_neutron_activator", "chemical_dissolution_chamber", "chemical_crystallizer", "seismic_vibrator", "security_desk",
		"quantum_entangloporter", "resistive_heater", "EnergyCube", "digital_miner", "bin_creative"};
	
	private static final IStateMapper machineMapper = new MachineBlockStateMapper();
	private static final IStateMapper basicMapper = new BasicBlockStateMapper();
	private static final IStateMapper plasticMapper = new PlasticBlockStateMapper();
	private static final IStateMapper fenceMapper = new PlasticFenceStateMapper();
	private static final IStateMapper boxMapper = new CardboardBoxStateMapper();
	
	@Override
	public void loadConfiguration()
	{
		super.loadConfiguration();

		client.enablePlayerSounds = Mekanism.configuration.get("client", "EnablePlayerSounds", true).getBoolean();
		client.enableMachineSounds = Mekanism.configuration.get("client", "EnableMachineSounds", true).getBoolean();
		client.holidays = Mekanism.configuration.get("client", "Holidays", true).getBoolean();
		client.baseSoundVolume = (float)Mekanism.configuration.get("client", "SoundVolume", 1D).getDouble();
		client.machineEffects = Mekanism.configuration.get("client", "MachineEffects", true).getBoolean();
		client.replaceSoundsWhenResuming = Mekanism.configuration.get("client", "ReplaceSoundsWhenResuming", true,
				"If true, will reduce lagging between player sounds. Setting to false will reduce GC load").getBoolean();
		client.renderCTM = Mekanism.configuration.get("client", "CTMRenderer", true).getBoolean();
		client.enableAmbientLighting = Mekanism.configuration.get("client", "EnableAmbientLighting", true).getBoolean();
		client.ambientLightingLevel = Mekanism.configuration.get("client", "AmbientLightingLevel", 15).getInt();
		client.opaqueTransmitters = Mekanism.configuration.get("client", "OpaqueTransmitterRender", false).getBoolean();

		if(Mekanism.configuration.hasChanged())
		{
			Mekanism.configuration.save();
		}
	}

	@Override
	public void openPersonalChest(EntityPlayer entityplayer, int id, int windowId, boolean isBlock, BlockPos pos, EnumHand hand)
	{
		TileEntityPersonalChest tileEntity = (TileEntityPersonalChest)entityplayer.worldObj.getTileEntity(pos);

		if(id == 0)
		{
			if(isBlock)
			{
				FMLClientHandler.instance().displayGuiScreen(entityplayer, new GuiPersonalChest(entityplayer.inventory, tileEntity));
				entityplayer.openContainer.windowId = windowId;
			}
			else {
				ItemStack stack = entityplayer.getHeldItem(hand);

				if(MachineType.get(stack) == MachineType.PERSONAL_CHEST)
				{
					InventoryPersonalChest inventory = new InventoryPersonalChest(entityplayer, hand);
					FMLClientHandler.instance().displayGuiScreen(entityplayer, new GuiPersonalChest(entityplayer.inventory, inventory));
					entityplayer.openContainer.windowId = windowId;
				}
			}
		}
	}

	@Override
	public void registerSpecialTileEntities()
	{
		ClientRegistry.registerTileEntity(TileEntityEnrichmentChamber.class, "EnrichmentChamber", new RenderConfigurableMachine<TileEntityEnrichmentChamber>());
		ClientRegistry.registerTileEntity(TileEntityOsmiumCompressor.class, "OsmiumCompressor", new RenderConfigurableMachine<TileEntityOsmiumCompressor>());
		ClientRegistry.registerTileEntity(TileEntityCombiner.class, "Combiner", new RenderConfigurableMachine<TileEntityCombiner>());
		ClientRegistry.registerTileEntity(TileEntityCrusher.class, "Crusher", new RenderConfigurableMachine<TileEntityCrusher>());
		ClientRegistry.registerTileEntity(TileEntityFactory.class, "SmeltingFactory", new RenderConfigurableMachine<TileEntityFactory>());
		ClientRegistry.registerTileEntity(TileEntityAdvancedFactory.class, "AdvancedSmeltingFactory", new RenderConfigurableMachine<TileEntityAdvancedFactory>());
		ClientRegistry.registerTileEntity(TileEntityEliteFactory.class, "UltimateSmeltingFactory", new RenderConfigurableMachine<TileEntityEliteFactory>());
		ClientRegistry.registerTileEntity(TileEntityPurificationChamber.class, "PurificationChamber", new RenderConfigurableMachine<TileEntityPurificationChamber>());
		ClientRegistry.registerTileEntity(TileEntityEnergizedSmelter.class, "EnergizedSmelter", new RenderConfigurableMachine<TileEntityEnergizedSmelter>());
		ClientRegistry.registerTileEntity(TileEntityMetallurgicInfuser.class, "MetallurgicInfuser", new RenderConfigurableMachine<TileEntityMetallurgicInfuser>());
		ClientRegistry.registerTileEntity(TileEntityGasTank.class, "GasTank", new RenderGasTank());
		ClientRegistry.registerTileEntity(TileEntityEnergyCube.class, "EnergyCube", new RenderEnergyCube());
		ClientRegistry.registerTileEntity(TileEntityPersonalChest.class, "PersonalChest", new RenderPersonalChest());
		ClientRegistry.registerTileEntity(TileEntityDynamicTank.class, "DynamicTank", new RenderDynamicTank());
		ClientRegistry.registerTileEntity(TileEntityDynamicValve.class, "DynamicValve", new RenderDynamicTank());
		ClientRegistry.registerTileEntity(TileEntityChargepad.class, "Chargepad", new RenderChargepad());
		ClientRegistry.registerTileEntity(TileEntityLogisticalSorter.class, "LogisticalSorter", new RenderLogisticalSorter());
		ClientRegistry.registerTileEntity(TileEntityBin.class, "Bin", new RenderBin());
		ClientRegistry.registerTileEntity(TileEntityDigitalMiner.class, "DigitalMiner", new RenderDigitalMiner());
		ClientRegistry.registerTileEntity(TileEntityTeleporter.class, "MekanismTeleporter", new RenderTeleporter());
		ClientRegistry.registerTileEntity(TileEntityChemicalInjectionChamber.class, "ChemicalInjectionChamber", new RenderConfigurableMachine<TileEntityChemicalInjectionChamber>());
		ClientRegistry.registerTileEntity(TileEntityThermalEvaporationController.class, "ThermalEvaporationController", new RenderThermalEvaporationController());
		ClientRegistry.registerTileEntity(TileEntityPrecisionSawmill.class, "PrecisionSawmill", new RenderConfigurableMachine<TileEntityPrecisionSawmill>());
		ClientRegistry.registerTileEntity(TileEntityChemicalCrystallizer.class, "ChemicalCrystallizer", new RenderChemicalCrystallizer());
		ClientRegistry.registerTileEntity(TileEntitySeismicVibrator.class, "SeismicVibrator", new RenderSeismicVibrator());
		ClientRegistry.registerTileEntity(TileEntityPRC.class, "PressurizedReactionChamber", new RenderConfigurableMachine<TileEntityPRC>());
		ClientRegistry.registerTileEntity(TileEntityFluidTank.class, "FluidTank", new RenderFluidTank());
		ClientRegistry.registerTileEntity(TileEntitySolarNeutronActivator.class, "SolarNeutronActivator", new RenderSolarNeutronActivator());
		ClientRegistry.registerTileEntity(TileEntityFormulaicAssemblicator.class, "FormulaicAssemblicator", new RenderConfigurableMachine<TileEntityFormulaicAssemblicator>());
		ClientRegistry.registerTileEntity(TileEntityResistiveHeater.class, "ResistiveHeater", new RenderResistiveHeater());
		ClientRegistry.registerTileEntity(TileEntityBoilerCasing.class, "BoilerCasing", new RenderThermoelectricBoiler());
		ClientRegistry.registerTileEntity(TileEntityBoilerValve.class, "BoilerValve", new RenderThermoelectricBoiler());
		ClientRegistry.registerTileEntity(TileEntitySecurityDesk.class, "SecurityDesk", new RenderSecurityDesk());
		ClientRegistry.registerTileEntity(TileEntityQuantumEntangloporter.class, "QuantumEntangloporter", new RenderQuantumEntangloporter());
		ClientRegistry.registerTileEntity(TileEntityChemicalDissolutionChamber.class, "ChemicalDissolutionChamber", new RenderChemicalDissolutionChamber());
		
		MultipartRegistryClient.bindMultipartSpecialRenderer(PartMechanicalPipe.class, new RenderMechanicalPipe());
		MultipartRegistryClient.bindMultipartSpecialRenderer(PartUniversalCable.class, new RenderUniversalCable());
		MultipartRegistryClient.bindMultipartSpecialRenderer(PartThermodynamicConductor.class, new RenderThermodynamicConductor());
		MultipartRegistryClient.bindMultipartSpecialRenderer(PartLogisticalTransporter.class, new RenderLogisticalTransporter());
		MultipartRegistryClient.bindMultipartSpecialRenderer(PartDiversionTransporter.class, new RenderLogisticalTransporter());
		MultipartRegistryClient.bindMultipartSpecialRenderer(PartRestrictiveTransporter.class, new RenderLogisticalTransporter());
		MultipartRegistryClient.bindMultipartSpecialRenderer(PartPressurizedTube.class, new RenderPressurizedTube());
	}

	@Override
	public void registerItemRenders()
	{
    	registerItemRender(MekanismItems.PartTransmitter);
		registerItemRender(MekanismItems.ElectricBow);
		registerItemRender(MekanismItems.Dust);
		registerItemRender(MekanismItems.Ingot);
		registerItemRender(MekanismItems.EnergyTablet);
		registerItemRender(MekanismItems.SpeedUpgrade);
		registerItemRender(MekanismItems.EnergyUpgrade);
		registerItemRender(MekanismItems.FilterUpgrade);
		registerItemRender(MekanismItems.MufflingUpgrade);
		registerItemRender(MekanismItems.GasUpgrade);
		registerItemRender(MekanismItems.AnchorUpgrade);
		registerItemRender(MekanismItems.Robit);
		registerItemRender(MekanismItems.AtomicDisassembler);
		registerItemRender(MekanismItems.EnrichedAlloy);
		registerItemRender(MekanismItems.ReinforcedAlloy);
		registerItemRender(MekanismItems.AtomicAlloy);
		registerItemRender(MekanismItems.ItemProxy);
		registerItemRender(MekanismItems.ControlCircuit);
		registerItemRender(MekanismItems.EnrichedIron);
		registerItemRender(MekanismItems.CompressedCarbon);
		registerItemRender(MekanismItems.CompressedRedstone);
		registerItemRender(MekanismItems.CompressedDiamond);
		registerItemRender(MekanismItems.CompressedObsidian);
		registerItemRender(MekanismItems.PortableTeleporter);
		registerItemRender(MekanismItems.TeleportationCore);
		registerItemRender(MekanismItems.Clump);
		registerItemRender(MekanismItems.DirtyDust);
		registerItemRender(MekanismItems.Configurator);
		registerItemRender(MekanismItems.NetworkReader);
		registerItemRender(MekanismItems.Jetpack);
		registerItemRender(MekanismItems.Dictionary);
		registerItemRender(MekanismItems.GasMask);
		registerItemRender(MekanismItems.ScubaTank);
		registerItemRender(MekanismItems.Balloon);
		registerItemRender(MekanismItems.Shard);
		registerItemRender(MekanismItems.ElectrolyticCore);
		registerItemRender(MekanismItems.Sawdust);
		registerItemRender(MekanismItems.Salt);
		registerItemRender(MekanismItems.Crystal);
		registerItemRender(MekanismItems.FreeRunners);
		registerItemRender(MekanismItems.ArmoredJetpack);
		registerItemRender(MekanismItems.ConfigurationCard);
		registerItemRender(MekanismItems.SeismicReader);
		registerItemRender(MekanismItems.Substrate);
		registerItemRender(MekanismItems.Polyethene);
		registerItemRender(MekanismItems.BioFuel);
		registerItemRender(MekanismItems.Flamethrower);
		registerItemRender(MekanismItems.GaugeDropper);
		registerItemRender(MekanismItems.TierInstaller);
		registerItemRender(MekanismItems.OtherDust);
		registerItemRender(MekanismItems.GlowPanel);
		
		ModelBakery.registerItemVariants(MekanismItems.WalkieTalkie, ItemWalkieTalkie.OFF_MODEL);
		
		for(int i = 1; i <= 9; i++)
		{
			ModelBakery.registerItemVariants(MekanismItems.WalkieTalkie, ItemWalkieTalkie.getModel(i));
		}
		
		ModelBakery.registerItemVariants(MekanismItems.CraftingFormula, ItemCraftingFormula.MODEL, 
				ItemCraftingFormula.INVALID_MODEL, ItemCraftingFormula.ENCODED_MODEL);
	}
	
	@Override
	public void registerBlockRenders()
	{
		ModelLoader.setCustomStateMapper(MekanismBlocks.MachineBlock, machineMapper);
		ModelLoader.setCustomStateMapper(MekanismBlocks.MachineBlock2, machineMapper);
		ModelLoader.setCustomStateMapper(MekanismBlocks.MachineBlock3, machineMapper);
		ModelLoader.setCustomStateMapper(MekanismBlocks.BasicBlock, basicMapper);
		ModelLoader.setCustomStateMapper(MekanismBlocks.BasicBlock2, basicMapper);
		ModelLoader.setCustomStateMapper(MekanismBlocks.PlasticBlock, plasticMapper);
		ModelLoader.setCustomStateMapper(MekanismBlocks.SlickPlasticBlock, plasticMapper);
		ModelLoader.setCustomStateMapper(MekanismBlocks.GlowPlasticBlock, plasticMapper);
		ModelLoader.setCustomStateMapper(MekanismBlocks.ReinforcedPlasticBlock, plasticMapper);
		ModelLoader.setCustomStateMapper(MekanismBlocks.RoadPlasticBlock, plasticMapper);
		ModelLoader.setCustomStateMapper(MekanismBlocks.PlasticFence, fenceMapper);
		ModelLoader.setCustomStateMapper(MekanismBlocks.CardboardBox, boxMapper);
		
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.ObsidianTNT), 0, new ModelResourceLocation("mekanism:ObsidianTNT", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.SaltBlock), 0, new ModelResourceLocation("mekanism:SaltBlock", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.CardboardBox), 0, new ModelResourceLocation("mekanism:CardboardBox", "storage=false"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.CardboardBox), 1, new ModelResourceLocation("mekanism:CardboardBox", "storage=true"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.EnergyCube), 0, new ModelResourceLocation("mekanism:EnergyCube", "inventory"));

		for(MachineType type : MachineType.values())
		{
			if(!type.isValidMachine())
			{
				continue;
			}
			
			List<ModelResourceLocation> modelsToAdd = new ArrayList<ModelResourceLocation>();
			String resource = "mekanism:" + type.getName();
			RecipeType recipePointer = null;
			
			if(type == MachineType.BASIC_FACTORY || type == MachineType.ADVANCED_FACTORY || type == MachineType.ELITE_FACTORY)
			{
				recipePointer = RecipeType.values()[0];
				resource = "mekanism:" + type.getName() + "_" + recipePointer.getName();
			}
			
			while(true)
			{
				if(machineResources.get(resource) == null)
				{
					List<String> entries = new ArrayList<String>();
					
					if(type.hasActiveTexture())
					{
						entries.add("active=false");
					}
					
					if(type.hasRotations())
					{
						entries.add("facing=north");
					}
					
					String properties = new String();
					
					for(int i = 0; i < entries.size(); i++)
					{
						properties += entries.get(i);
						
						if(i < entries.size()-1)
						{
							properties += ",";
						}
					}
					
					if(Arrays.asList(CUSTOM_RENDERS).contains(type.getName()))
					{
						properties = "inventory";
					}
					
					ModelResourceLocation model = new ModelResourceLocation(resource, properties);
					
					machineResources.put(resource, model);
					modelsToAdd.add(model);
					
					if(type == MachineType.BASIC_FACTORY || type == MachineType.ADVANCED_FACTORY || type == MachineType.ELITE_FACTORY)
					{
						if(recipePointer.ordinal() < RecipeType.values().length-1)
						{
							recipePointer = RecipeType.values()[recipePointer.ordinal()+1];
							resource = "mekanism:" + type.getName() + "_" + recipePointer.getName();
							
							continue;
						}
					}
				}
				
				break;
			}
			
			ModelLoader.registerItemVariants(Item.getItemFromBlock(type.typeBlock.getBlock()), modelsToAdd.toArray(new ModelResourceLocation[] {}));
		}

		for(BasicBlockType type : BasicBlockType.values())
		{
			List<ModelResourceLocation> modelsToAdd = new ArrayList<ModelResourceLocation>();
			String resource = "mekanism:" + type.getName();
			BaseTier tierPointer = null;
			
			if(type.tiers)
			{
				tierPointer = BaseTier.values()[0];
				resource = "mekanism:" + type.getName() + "_" + tierPointer.getName();
			}
			
			while(true)
			{
				if(basicResources.get(resource) == null)
				{
					List<String> entries = new ArrayList<String>();
					
					if(type.hasActiveTexture())
					{
						entries.add("active=false");
					}
					
					if(type.hasRotations() || type == BasicBlockType.THERMAL_EVAPORATION_CONTROLLER)
					{
						entries.add("facing=north");
					}
					
					String properties = new String();
					
					for(int i = 0; i < entries.size(); i++)
					{
						properties += entries.get(i);
						
						if(i < entries.size()-1)
						{
							properties += ",";
						}
					}
					
					if(type == BasicBlockType.BIN || Arrays.asList(CUSTOM_RENDERS).contains(type.getName()))
					{
						properties = "inventory";
					}
					
					ModelResourceLocation model = new ModelResourceLocation(resource, properties);
					
					basicResources.put(resource, model);
					modelsToAdd.add(model);
					
					if(type.tiers)
					{
						if(tierPointer.ordinal() < BaseTier.values().length-1)
						{
							tierPointer = BaseTier.values()[tierPointer.ordinal()+1];
							
							if(type == BasicBlockType.BIN || tierPointer.isObtainable())
							{
								resource = "mekanism:" + type.getName() + "_" + tierPointer.getName();
								
								continue;
							}
						}
					}
				}
				
				break;
			}
			
			ModelLoader.registerItemVariants(Item.getItemFromBlock(type.blockType.getBlock()), modelsToAdd.toArray(new ModelResourceLocation[] {}));
		}

		for(EnumColor color : EnumColor.DYES)
		{
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.PlasticBlock), color.getMetaValue(), new ModelResourceLocation("mekanism:plastic_block", "type=plastic"));
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.SlickPlasticBlock), color.getMetaValue(), new ModelResourceLocation("mekanism:plastic_block", "type=slick"));
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.GlowPlasticBlock), color.getMetaValue(), new ModelResourceLocation("mekanism:plastic_block", "type=glow"));
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.ReinforcedPlasticBlock), color.getMetaValue(), new ModelResourceLocation("mekanism:plastic_block", "type=reinforced"));
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.RoadPlasticBlock), color.getMetaValue(), new ModelResourceLocation("mekanism:plastic_block", "type=road"));
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.PlasticFence), color.getMetaValue(), new ModelResourceLocation("mekanism:PlasticFence", "inventory"));
		}

		for(EnumOreType ore : EnumOreType.values())
		{
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.OreBlock), ore.ordinal(), new ModelResourceLocation("mekanism:OreBlock", "type=" + ore.getName()));
		}
		
		ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(MekanismBlocks.GasTank), new ItemMeshDefinition() {
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack)
			{
				GasTankTier tier = GasTankTier.values()[((ItemBlockGasTank)stack.getItem()).getBaseTier(stack).ordinal()];
				ResourceLocation baseLocation = new ResourceLocation("mekanism", "GasTank");
				
				return new ModelResourceLocation(baseLocation, "facing=north,tier="+tier);
			}
		});
		
		ItemMeshDefinition machineMesher = new ItemMeshDefinition() {
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack)
			{
				MachineType type = MachineType.get(stack);
				
				if(type != null)
				{
					String resource = "mekanism:" + type.getName();
					
					if(type == MachineType.BASIC_FACTORY || type == MachineType.ADVANCED_FACTORY || type == MachineType.ELITE_FACTORY)
					{
						RecipeType recipe = RecipeType.values()[((ItemBlockMachine)stack.getItem()).getRecipeType(stack)];
						resource = "mekanism:" + type.getName() + "_" + recipe.getName();
					}
					
					return machineResources.get(resource);
				}
				
				return null;
			}
		};
		
		ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(MekanismBlocks.MachineBlock), machineMesher);
		ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(MekanismBlocks.MachineBlock2), machineMesher);
		ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(MekanismBlocks.MachineBlock3), machineMesher);
		
		ItemMeshDefinition basicMesher = new ItemMeshDefinition() {
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack)
			{
				BasicBlockType type = BasicBlockType.get(stack);
				
				if(type != null)
				{
					String resource = "mekanism:" + type.getName();
					
					if(type.tiers)
					{
						BaseTier tier = ((ItemBlockBasic)stack.getItem()).getBaseTier(stack);
						resource = "mekanism:" + type.getName() + "_" + tier.getName();
					}
					
					return basicResources.get(resource);
				}
				
				return null;
			}
		};
		
		ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(MekanismBlocks.BasicBlock), basicMesher);
		ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(MekanismBlocks.BasicBlock2), basicMesher);
		
		OBJLoader.INSTANCE.addDomain("mekanism");
	}
	
	public void registerItemRender(Item item)
	{
		MekanismRenderer.registerItemRender("mekanism", item);
	}
	
	@Override
	public GuiScreen getClientGui(int ID, EntityPlayer player, World world, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		switch(ID)
		{
			case 0:
				return new GuiDictionary(player.inventory);
			case 1:
				return new GuiCredits();
			case 2:
				return new GuiDigitalMiner(player.inventory, (TileEntityDigitalMiner)tileEntity);
			case 3:
				return new GuiEnrichmentChamber(player.inventory, (TileEntityElectricMachine)tileEntity);
			case 4:
				return new GuiOsmiumCompressor(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 5:
				return new GuiCombiner(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 6:
				return new GuiCrusher(player.inventory, (TileEntityElectricMachine)tileEntity);
			case 7:
				return new GuiRotaryCondensentrator(player.inventory, (TileEntityRotaryCondensentrator)tileEntity);
			case 8:
				return new GuiEnergyCube(player.inventory, (TileEntityEnergyCube)tileEntity);
			case 9:
				return new GuiSideConfiguration(player, (ISideConfiguration)tileEntity);
			case 10:
				return new GuiGasTank(player.inventory, (TileEntityGasTank)tileEntity);
			case 11:
				return new GuiFactory(player.inventory, (TileEntityFactory)tileEntity);
			case 12:
				return new GuiMetallurgicInfuser(player.inventory, (TileEntityMetallurgicInfuser)tileEntity);
			case 13:
				return new GuiTeleporter(player.inventory, (TileEntityTeleporter)tileEntity);
			case 14:
				ItemStack itemStack = player.getHeldItem(EnumHand.values()[pos.getX()]);

				if(itemStack != null && itemStack.getItem() instanceof ItemPortableTeleporter)
				{
					return new GuiTeleporter(player, EnumHand.values()[pos.getX()], itemStack);
				}
			case 15:
				return new GuiPurificationChamber(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 16:
				return new GuiEnergizedSmelter(player.inventory, (TileEntityElectricMachine)tileEntity);
			case 17:
				return new GuiElectricPump(player.inventory, (TileEntityElectricPump)tileEntity);
			case 18:
				return new GuiDynamicTank(player.inventory, (TileEntityDynamicTank)tileEntity);
			//EMPTY 19, 20
			case 21:
				EntityRobit robit = (EntityRobit)world.getEntityByID(pos.getX());

				if(robit != null)
				{
					return new GuiRobitMain(player.inventory, robit);
				}
			case 22:
				robit = (EntityRobit)world.getEntityByID(pos.getX());

				if(robit != null)
				{
					return new GuiRobitCrafting(player.inventory, robit);
				}
			case 23:
				robit = (EntityRobit)world.getEntityByID(pos.getX());
				
				if(robit != null)
				{
					return new GuiRobitInventory(player.inventory, robit);
				}
			case 24:
				robit = (EntityRobit)world.getEntityByID(pos.getX());

				if(robit != null)
				{
					return new GuiRobitSmelting(player.inventory, robit);
				}
			case 25:
				robit = (EntityRobit)world.getEntityByID(pos.getX());

				if(robit != null)
				{
					return new GuiRobitRepair(player.inventory, robit);
				}
			case 29:
				return new GuiChemicalOxidizer(player.inventory, (TileEntityChemicalOxidizer)tileEntity);
			case 30:
				return new GuiChemicalInfuser(player.inventory, (TileEntityChemicalInfuser)tileEntity);
			case 31:
				return new GuiChemicalInjectionChamber(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 32:
				return new GuiElectrolyticSeparator(player.inventory, (TileEntityElectrolyticSeparator)tileEntity);
			case 33:
				return new GuiThermalEvaporationController(player.inventory, (TileEntityThermalEvaporationController)tileEntity);
			case 34:
				return new GuiPrecisionSawmill(player.inventory, (TileEntityPrecisionSawmill)tileEntity);
			case 35:
				return new GuiChemicalDissolutionChamber(player.inventory, (TileEntityChemicalDissolutionChamber)tileEntity);
			case 36:
				return new GuiChemicalWasher(player.inventory, (TileEntityChemicalWasher)tileEntity);
			case 37:
				return new GuiChemicalCrystallizer(player.inventory, (TileEntityChemicalCrystallizer)tileEntity);
			case 38:
				ItemStack itemStack1 = player.getHeldItem(EnumHand.values()[pos.getX()]);

				if(itemStack1 != null && itemStack1.getItem() instanceof ItemSeismicReader)
				{
					return new GuiSeismicReader(world, new Coord4D(player), itemStack1.copy());
				}
			case 39:
				return new GuiSeismicVibrator(player.inventory, (TileEntitySeismicVibrator)tileEntity);
			case 40:
				return new GuiPRC(player.inventory, (TileEntityPRC)tileEntity);
			case 41:
				return new GuiFluidTank(player.inventory, (TileEntityFluidTank)tileEntity);
			case 42:
				return new GuiFluidicPlenisher(player.inventory, (TileEntityFluidicPlenisher)tileEntity);
			case 43:
				return new GuiUpgradeManagement(player.inventory, (IUpgradeTile)tileEntity);
			case 44:
				return new GuiLaserAmplifier(player.inventory, (TileEntityLaserAmplifier)tileEntity);
			case 45:
				return new GuiLaserTractorBeam(player.inventory, (TileEntityLaserTractorBeam)tileEntity);
			case 46:
				return new GuiQuantumEntangloporter(player.inventory, (TileEntityQuantumEntangloporter)tileEntity);
			case 47:
				return new GuiSolarNeutronActivator(player.inventory, (TileEntitySolarNeutronActivator)tileEntity);
			case 48:
				return new GuiAmbientAccumulator(player, (TileEntityAmbientAccumulator)tileEntity);
			case 49:
				return new GuiInductionMatrix(player.inventory, (TileEntityInductionCasing)tileEntity);
			case 50:
				return new GuiMatrixStats(player.inventory, (TileEntityInductionCasing)tileEntity);
			case 51:
				return new GuiTransporterConfig(player, (ISideConfiguration)tileEntity);
			case 52:
				return new GuiOredictionificator(player.inventory, (TileEntityOredictionificator)tileEntity);
			case 53:
				return new GuiResistiveHeater(player.inventory, (TileEntityResistiveHeater)tileEntity);
			case 54:
				return new GuiThermoelectricBoiler(player.inventory, (TileEntityBoilerCasing)tileEntity);
			case 55:
				return new GuiBoilerStats(player.inventory, (TileEntityBoilerCasing)tileEntity);
			case 56:
				return new GuiFormulaicAssemblicator(player.inventory, (TileEntityFormulaicAssemblicator)tileEntity);
			case 57:
				return new GuiSecurityDesk(player.inventory, (TileEntitySecurityDesk)tileEntity);
			case 58:
				return new GuiFuelwoodHeater(player.inventory, (TileEntityFuelwoodHeater)tileEntity);
		}
		
		return null;
	}
	
	@Override
	public void handleTeleporterUpdate(PortableTeleporterMessage message)
	{
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		
		if(screen instanceof GuiTeleporter && ((GuiTeleporter)screen).itemStack != null)
		{
			GuiTeleporter teleporter = (GuiTeleporter)screen;
			
			teleporter.clientStatus = message.status;
			teleporter.clientFreq = message.frequency;
			teleporter.clientPublicCache = message.publicCache;
			teleporter.clientPrivateCache = message.privateCache;
			
			teleporter.updateButtons();
		}
	}
	
	@Override
	public void addHitEffects(Coord4D coord, RayTraceResult mop)
	{
		if(Minecraft.getMinecraft().theWorld != null)
		{
			Minecraft.getMinecraft().effectRenderer.addBlockHitEffects(coord.getPos(), mop);
		}
	}
	
	@Override
	public void doGenericSparkle(TileEntity tileEntity, INodeChecker checker)
	{
		new SparkleAnimation(tileEntity, checker).run();
	}

	@Override
	public void doMultiblockSparkle(final TileEntityMultiblock<?> tileEntity)
	{
		new SparkleAnimation(tileEntity, new INodeChecker() {
			@Override
			public boolean isNode(TileEntity tile)
			{
				return MultiblockManager.areEqual(tile, tileEntity);
			}
		}).run();
	}

	@Override
	public void init()
	{
		super.init();
		
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
			{
				BlockMachine machine = (BlockMachine)state.getBlock();
				
				if(state.getValue(machine.getMachineBlock().getProperty()) == MachineType.FLUID_TANK)
				{
					EnumColor color = state.getValue(BlockStateMachine.tierProperty).getColor();
					
					return (int)(color.getColor(0)*255) << 16 | (int)(color.getColor(1)*255) << 8 | (int)(color.getColor(2)*255);
				}
				
				return -1;
			}
		}, MekanismBlocks.MachineBlock, MekanismBlocks.MachineBlock2, MekanismBlocks.MachineBlock3);
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
			{
				EnumDyeColor color = state.getValue(colorProperty);
				EnumColor dye = EnumColor.DYES[color.getDyeDamage()];
				
				return (int)(dye.getColor(0)*255) << 16 | (int)(dye.getColor(1)*255) << 8 | (int)(dye.getColor(2)*255);
			}
		}, MekanismBlocks.PlasticBlock, MekanismBlocks.GlowPlasticBlock, MekanismBlocks.RoadPlasticBlock, MekanismBlocks.ReinforcedPlasticBlock, 
		MekanismBlocks.SlickPlasticBlock, MekanismBlocks.PlasticFence);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
			@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) 
			{
				if(MachineType.get(stack) == MachineType.FLUID_TANK)
				{
					EnumColor color = ((ItemBlockMachine)stack.getItem()).getBaseTier(stack).getColor();
					
					return (int)(color.getColor(0)*255) << 16 | (int)(color.getColor(1)*255) << 8 | (int)(color.getColor(2)*255);
				}
				
				return -1;
			}
		}, MekanismBlocks.MachineBlock, MekanismBlocks.MachineBlock2, MekanismBlocks.MachineBlock3);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
			@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) 
			{
				EnumDyeColor dyeColor = EnumDyeColor.byDyeDamage(stack.getItemDamage()&15);
				EnumColor dye = EnumColor.DYES[dyeColor.getDyeDamage()];
				
				return (int)(dye.getColor(0)*255) << 16 | (int)(dye.getColor(1)*255) << 8 | (int)(dye.getColor(2)*255);
			}
		}, MekanismBlocks.PlasticBlock, MekanismBlocks.GlowPlasticBlock, MekanismBlocks.RoadPlasticBlock, MekanismBlocks.ReinforcedPlasticBlock, 
		MekanismBlocks.SlickPlasticBlock, MekanismBlocks.PlasticFence);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
			@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) 
			{
				EnumColor dye = EnumColor.DYES[stack.getItemDamage()];
				
				return (int)(dye.getColor(0)*255) << 16 | (int)(dye.getColor(1)*255) << 8 | (int)(dye.getColor(2)*255);
			}
		}, MekanismItems.Balloon);
		
		MinecraftForge.EVENT_BUS.register(new ClientConnectionHandler());
		MinecraftForge.EVENT_BUS.register(new ClientPlayerTracker());
		MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
		MinecraftForge.EVENT_BUS.register(new RenderTickHandler());
		
		new MekanismKeyHandler();

		HolidayManager.init();
	}
	
	@SubscribeEvent
    public void onModelBake(ModelBakeEvent event) throws IOException 
    {
		for(String s : CUSTOM_RENDERS)
		{
			ModelResourceLocation model = new ModelResourceLocation("mekanism:" + s, "inventory");
	        Object obj = event.getModelRegistry().getObject(model);
	        
	        if(obj instanceof IBakedModel)
	        {
	        	event.getModelRegistry().putObject(model, new CustomItemModelFactory((IBakedModel)obj));
	        }
		}
    }

	@Override
	public void preInit()
	{
		MekanismRenderer.init();
		
		ModelLoaderRegistry.registerLoader(MekanismOBJLoader.INSTANCE);
		
		MinecraftForge.EVENT_BUS.register(new CTMRegistry());
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(MekanismOBJLoader.INSTANCE);
		
		//Register entity rendering handlers
		RenderingRegistry.registerEntityRenderingHandler(EntityObsidianTNT.class, new IRenderFactory<EntityObsidianTNT>() {
			@Override
			public Render<EntityObsidianTNT> createRenderFor(RenderManager manager)
			{
				return new RenderObsidianTNTPrimed(manager);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityRobit.class, new IRenderFactory<EntityRobit>() {
			@Override
			public Render<EntityRobit> createRenderFor(RenderManager manager)
			{
				return new RenderRobit(manager);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityBalloon.class, new IRenderFactory<EntityBalloon>() {
			@Override
			public Render<EntityBalloon> createRenderFor(RenderManager manager)
			{
				return new RenderBalloon(manager);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityBabySkeleton.class, new IRenderFactory<EntityBabySkeleton>() {
			@Override
			public Render<EntitySkeleton> createRenderFor(RenderManager manager)
			{
				return new RenderSkeleton(manager);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityFlame.class, new IRenderFactory<EntityFlame>() {
			@Override
			public Render<? super EntityFlame> createRenderFor(RenderManager manager)
			{
				return new RenderFlame(manager);
			}
		});
		
		//Walkie Talkie dynamic texture
		ModelLoader.setCustomMeshDefinition(MekanismItems.WalkieTalkie, new ItemMeshDefinition() {
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack) 
			{
				if(stack != null && stack.getItem() instanceof ItemWalkieTalkie)
				{
					ItemWalkieTalkie item = (ItemWalkieTalkie)stack.getItem();
					
					if(item.getOn(stack))
					{
						return ItemWalkieTalkie.CHANNEL_MODELS.get(item.getChannel(stack));
					}
				}
				
				return ItemWalkieTalkie.OFF_MODEL;
			}
		});
		
		//Crafting Formula dynamic texture
		ModelLoader.setCustomMeshDefinition(MekanismItems.CraftingFormula, new ItemMeshDefinition() {
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack) 
			{
				if(stack != null && stack.getItem() instanceof ItemCraftingFormula)
				{
					ItemCraftingFormula item = (ItemCraftingFormula)stack.getItem();
					
					if(item.getInventory(stack) == null)
					{
						return ItemCraftingFormula.MODEL;
					}
					else {
						return item.isInvalid(stack) ? ItemCraftingFormula.INVALID_MODEL : ItemCraftingFormula.ENCODED_MODEL;
					}
				}
				
				return ItemCraftingFormula.MODEL;
			}
		});
		
		CTMRegistry.registerCTMs("mekanism", "dynamic_tank", "structural_glass", "dynamic_valve", "teleporter", "teleporter_frame", "induction_casing", "induction_port", "induction_port_output",
				"induction_cell_basic", "induction_cell_advanced", "induction_cell_elite", "induction_cell_ultimate", "induction_provider_basic", "induction_provider_advanced", "induction_provider_elite",
				"induction_provider_ultimate", "thermal_evaporation_controller", "thermal_evaporation_controller_on", "thermal_evaporation_valve", "superheating_element", "superheating_element_on",
				"boiler_casing", "boiler_valve", "thermal_evaporation_valve", "thermal_evaporation_block");
	}

	@Override
	public double getReach(EntityPlayer player)
	{
		return Minecraft.getMinecraft().playerController.getBlockReachDistance();
	}

	@Override
	public boolean isPaused()
	{
		if(FMLClientHandler.instance().getClient().isSingleplayer() && !FMLClientHandler.instance().getClient().getIntegratedServer().getPublic())
		{
			GuiScreen screen = FMLClientHandler.instance().getClient().currentScreen;

			if(screen != null && screen.doesGuiPauseGame())
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public File getMinecraftDir()
	{
		return Minecraft.getMinecraft().mcDataDir;
	}

	@Override
	public void onConfigSync(boolean fromPacket)
	{
		super.onConfigSync(fromPacket);

		if(fromPacket && general.voiceServerEnabled && MekanismClient.voiceClient != null)
		{
			MekanismClient.voiceClient.start();
		}
	}

	@Override
	public EntityPlayer getPlayer(MessageContext context)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			return context.getServerHandler().playerEntity;
		}
		else {
			return Minecraft.getMinecraft().thePlayer;
		}
	}
	
	@Override
	public void handlePacket(Runnable runnable, EntityPlayer player)
	{
		if(player == null || player.worldObj.isRemote)
		{
			Minecraft.getMinecraft().addScheduledTask(runnable);
		}
		else if(player != null && !player.worldObj.isRemote)
		{
			((WorldServer)player.worldObj).addScheduledTask(runnable); //singleplayer
		}
	}

	@Override
	public void renderLaser(World world, Pos3D from, Pos3D to, EnumFacing direction, double energy)
	{
		Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleLaser(world, from, to, direction, energy));
	}
	
	@Override
	public FontRenderer getFontRenderer()
	{
		return Minecraft.getMinecraft().fontRendererObj;
	}
}
