package mekanism.client;

import java.io.File;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.client;
import mekanism.api.MekanismConfig.general;
import mekanism.api.Pos3D;
import mekanism.client.SparkleAnimation.INodeChecker;
import mekanism.client.entity.EntityLaser;
import mekanism.client.gui.GuiAmbientAccumulator;
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
import mekanism.client.gui.GuiElectricChest;
import mekanism.client.gui.GuiElectricPump;
import mekanism.client.gui.GuiElectrolyticSeparator;
import mekanism.client.gui.GuiEnergizedSmelter;
import mekanism.client.gui.GuiEnergyCube;
import mekanism.client.gui.GuiEnrichmentChamber;
import mekanism.client.gui.GuiEntangledBlock;
import mekanism.client.gui.GuiFactory;
import mekanism.client.gui.GuiFluidicPlenisher;
import mekanism.client.gui.GuiGasTank;
import mekanism.client.gui.GuiInductionMatrix;
import mekanism.client.gui.GuiLaserAmplifier;
import mekanism.client.gui.GuiLaserTractorBeam;
import mekanism.client.gui.GuiMatrixStats;
import mekanism.client.gui.GuiMetallurgicInfuser;
import mekanism.client.gui.GuiOredictionificator;
import mekanism.client.gui.GuiOsmiumCompressor;
import mekanism.client.gui.GuiPRC;
import mekanism.client.gui.GuiPasswordEnter;
import mekanism.client.gui.GuiPasswordModify;
import mekanism.client.gui.GuiPortableTank;
import mekanism.client.gui.GuiPrecisionSawmill;
import mekanism.client.gui.GuiPurificationChamber;
import mekanism.client.gui.GuiRobitCrafting;
import mekanism.client.gui.GuiRobitInventory;
import mekanism.client.gui.GuiRobitMain;
import mekanism.client.gui.GuiRobitRepair;
import mekanism.client.gui.GuiRobitSmelting;
import mekanism.client.gui.GuiRotaryCondensentrator;
import mekanism.client.gui.GuiSeismicReader;
import mekanism.client.gui.GuiSeismicVibrator;
import mekanism.client.gui.GuiSideConfiguration;
import mekanism.client.gui.GuiSolarEvaporationController;
import mekanism.client.gui.GuiSolarNeutronActivator;
import mekanism.client.gui.GuiTeleporter;
import mekanism.client.gui.GuiTransporterConfig;
import mekanism.client.gui.GuiUpgradeManagement;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderGlowPanel;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.block.BasicRenderingHandler;
import mekanism.client.render.block.CTMRenderingHandler;
import mekanism.client.render.block.MachineRenderingHandler;
import mekanism.client.render.block.PlasticRenderingHandler;
import mekanism.client.render.entity.RenderBalloon;
import mekanism.client.render.entity.RenderFlame;
import mekanism.client.render.entity.RenderObsidianTNTPrimed;
import mekanism.client.render.entity.RenderRobit;
import mekanism.client.render.item.ItemRenderingHandler;
import mekanism.client.render.tileentity.RenderBin;
import mekanism.client.render.tileentity.RenderChargepad;
import mekanism.client.render.tileentity.RenderChemicalCrystallizer;
import mekanism.client.render.tileentity.RenderChemicalDissolutionChamber;
import mekanism.client.render.tileentity.RenderChemicalInfuser;
import mekanism.client.render.tileentity.RenderChemicalOxidizer;
import mekanism.client.render.tileentity.RenderChemicalWasher;
import mekanism.client.render.tileentity.RenderConfigurableMachine;
import mekanism.client.render.tileentity.RenderDigitalMiner;
import mekanism.client.render.tileentity.RenderDynamicTank;
import mekanism.client.render.tileentity.RenderElectricChest;
import mekanism.client.render.tileentity.RenderElectricPump;
import mekanism.client.render.tileentity.RenderElectrolyticSeparator;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.client.render.tileentity.RenderFluidicPlenisher;
import mekanism.client.render.tileentity.RenderGasTank;
import mekanism.client.render.tileentity.RenderLaser;
import mekanism.client.render.tileentity.RenderLaserAmplifier;
import mekanism.client.render.tileentity.RenderLaserTractorBeam;
import mekanism.client.render.tileentity.RenderLogisticalSorter;
import mekanism.client.render.tileentity.RenderMetallurgicInfuser;
import mekanism.client.render.tileentity.RenderObsidianTNT;
import mekanism.client.render.tileentity.RenderPortableTank;
import mekanism.client.render.tileentity.RenderPressurizedReactionChamber;
import mekanism.client.render.tileentity.RenderRotaryCondensentrator;
import mekanism.client.render.tileentity.RenderSalinationController;
import mekanism.client.render.tileentity.RenderSeismicVibrator;
import mekanism.client.render.tileentity.RenderSolarNeutronActivator;
import mekanism.client.render.tileentity.RenderTeleporter;
import mekanism.common.CommonProxy;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.base.IElectricChest;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.entity.EntityBabySkeleton;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.entity.EntityFlame;
import mekanism.common.entity.EntityObsidianTNT;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.InventoryElectricChest;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemSeismicReader;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterMessage;
import mekanism.common.tile.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.TileEntityAdvancedFactory;
import mekanism.common.tile.TileEntityAmbientAccumulator;
import mekanism.common.tile.TileEntityBin;
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
import mekanism.common.tile.TileEntityElectricChest;
import mekanism.common.tile.TileEntityElectricMachine;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEliteFactory;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityEntangledBlock;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionPort;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.tile.TileEntityLaser;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.tile.TileEntityObsidianTNT;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPRC;
import mekanism.common.tile.TileEntityPortableTank;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.tile.TileEntitySolarEvaporationController;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import mekanism.common.tile.TileEntityTeleporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Client proxy for the Mekanism mod.
 * @author AidanBrady
 *
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void loadConfiguration()
	{
		super.loadConfiguration();

		client.enablePlayerSounds = Mekanism.configuration.get("client", "EnablePlayerSounds", true).getBoolean(true);
		client.enableMachineSounds = Mekanism.configuration.get("client", "EnableMachineSounds", true).getBoolean(true);
		client.fancyUniversalCableRender = Mekanism.configuration.get("client", "FancyUniversalCableRender", true).getBoolean(true);
		client.holidays = Mekanism.configuration.get("client", "Holidays", true).getBoolean(true);
		client.baseSoundVolume = (float)Mekanism.configuration.get("client", "SoundVolume", 1D).getDouble(1D);
		client.machineEffects = Mekanism.configuration.get("client", "MachineEffects", true).getBoolean(true);
		client.oldTransmitterRender = Mekanism.configuration.get("client", "OldTransmitterRender", false).getBoolean();
		client.replaceSoundsWhenResuming = Mekanism.configuration.get("client", "ReplaceSoundsWhenResuming", true,
				"If true, will reduce lagging between player sounds. Setting to false will reduce GC load").getBoolean();
		client.renderCTM = Mekanism.configuration.get("client", "CTMRenderer", true).getBoolean();

		if(Mekanism.configuration.hasChanged())
		{
			Mekanism.configuration.save();
		}
	}

	@Override
	public int getArmorIndex(String string)
	{
		return RenderingRegistry.addNewArmourRendererPrefix(string);
	}

	@Override
	public void openElectricChest(EntityPlayer entityplayer, int id, int windowId, boolean isBlock, int x, int y, int z)
	{
		TileEntityElectricChest tileEntity = (TileEntityElectricChest)entityplayer.worldObj.getTileEntity(x, y, z);

		if(id == 0)
		{
			if(isBlock)
			{
				FMLClientHandler.instance().displayGuiScreen(entityplayer, new GuiElectricChest(entityplayer.inventory, tileEntity));
				entityplayer.openContainer.windowId = windowId;
			}
			else {
				ItemStack stack = entityplayer.getCurrentEquippedItem();

				if(stack != null && stack.getItem() instanceof IElectricChest && MachineType.get(stack) == MachineType.ELECTRIC_CHEST)
				{
					InventoryElectricChest inventory = new InventoryElectricChest(entityplayer);
					FMLClientHandler.instance().displayGuiScreen(entityplayer, new GuiElectricChest(entityplayer.inventory, inventory));
					entityplayer.openContainer.windowId = windowId;
				}
			}
		}
		else if(id == 1)
		{
			if(isBlock)
			{
				FMLClientHandler.instance().displayGuiScreen(entityplayer, new GuiPasswordEnter(tileEntity));
			}
			else {
				ItemStack stack = entityplayer.getCurrentEquippedItem();

				if(stack != null && stack.getItem() instanceof IElectricChest && MachineType.get(stack) == MachineType.ELECTRIC_CHEST)
				{
					FMLClientHandler.instance().displayGuiScreen(entityplayer, new GuiPasswordEnter(stack));
				}
			}
		}
		else if(id == 2)
		{
			if(isBlock)
			{
				FMLClientHandler.instance().displayGuiScreen(entityplayer, new GuiPasswordModify(tileEntity));
			}
			else {
				ItemStack stack = entityplayer.getCurrentEquippedItem();

				if(stack != null && stack.getItem() instanceof IElectricChest && MachineType.get(stack) == MachineType.ELECTRIC_CHEST)
				{
					FMLClientHandler.instance().displayGuiScreen(entityplayer, new GuiPasswordModify(stack));
				}
			}
		}
	}

	@Override
	public void registerSpecialTileEntities()
	{
		ClientRegistry.registerTileEntity(TileEntityEnrichmentChamber.class, "EnrichmentChamber", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityOsmiumCompressor.class, "OsmiumCompressor", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityCombiner.class, "Combiner", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityCrusher.class, "Crusher", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityFactory.class, "SmeltingFactory", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityAdvancedFactory.class, "AdvancedSmeltingFactory", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityEliteFactory.class, "UltimateSmeltingFactory", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityPurificationChamber.class, "PurificationChamber", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityEnergizedSmelter.class, "EnergizedSmelter", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityMetallurgicInfuser.class, "MetallurgicInfuser", new RenderMetallurgicInfuser());
		ClientRegistry.registerTileEntity(TileEntityObsidianTNT.class, "ObsidianTNT", new RenderObsidianTNT());
		ClientRegistry.registerTileEntity(TileEntityGasTank.class, "GasTank", new RenderGasTank());
		ClientRegistry.registerTileEntity(TileEntityEnergyCube.class, "EnergyCube", new RenderEnergyCube());
		ClientRegistry.registerTileEntity(TileEntityElectricPump.class, "ElectricPump", new RenderElectricPump());
		ClientRegistry.registerTileEntity(TileEntityElectricChest.class, "ElectricChest", new RenderElectricChest());
		ClientRegistry.registerTileEntity(TileEntityDynamicTank.class, "DynamicTank", new RenderDynamicTank());
		ClientRegistry.registerTileEntity(TileEntityDynamicValve.class, "DynamicValve", new RenderDynamicTank());
		ClientRegistry.registerTileEntity(TileEntityChargepad.class, "Chargepad", new RenderChargepad());
		ClientRegistry.registerTileEntity(TileEntityLogisticalSorter.class, "LogisticalSorter", new RenderLogisticalSorter());
		ClientRegistry.registerTileEntity(TileEntityBin.class, "Bin", new RenderBin());
		ClientRegistry.registerTileEntity(TileEntityDigitalMiner.class, "DigitalMiner", new RenderDigitalMiner());
		ClientRegistry.registerTileEntity(TileEntityRotaryCondensentrator.class, "RotaryCondensentrator", new RenderRotaryCondensentrator());
		ClientRegistry.registerTileEntity(TileEntityTeleporter.class, "MekanismTeleporter", new RenderTeleporter());
		ClientRegistry.registerTileEntity(TileEntityChemicalOxidizer.class, "ChemicalOxidizer", new RenderChemicalOxidizer());
		ClientRegistry.registerTileEntity(TileEntityChemicalInfuser.class, "ChemicalInfuser", new RenderChemicalInfuser());
		ClientRegistry.registerTileEntity(TileEntityChemicalInjectionChamber.class, "ChemicalInjectionChamber", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityElectrolyticSeparator.class, "ElectrolyticSeparator", new RenderElectrolyticSeparator());
		ClientRegistry.registerTileEntity(TileEntitySolarEvaporationController.class, "SalinationController", new RenderSalinationController());
		ClientRegistry.registerTileEntity(TileEntityPrecisionSawmill.class, "PrecisionSawmill", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityChemicalDissolutionChamber.class, "ChemicalDissolutionChamber", new RenderChemicalDissolutionChamber());
		ClientRegistry.registerTileEntity(TileEntityChemicalWasher.class, "ChemicalWasher", new RenderChemicalWasher());
		ClientRegistry.registerTileEntity(TileEntityChemicalCrystallizer.class, "ChemicalCrystallizer", new RenderChemicalCrystallizer());
		ClientRegistry.registerTileEntity(TileEntitySeismicVibrator.class, "SeismicVibrator", new RenderSeismicVibrator());
		ClientRegistry.registerTileEntity(TileEntityPRC.class, "PressurizedReactionChamber", new RenderPressurizedReactionChamber());
		ClientRegistry.registerTileEntity(TileEntityPortableTank.class, "PortableTank", new RenderPortableTank());
		ClientRegistry.registerTileEntity(TileEntityFluidicPlenisher.class, "FluidicPlenisher", new RenderFluidicPlenisher());
		ClientRegistry.registerTileEntity(TileEntityLaser.class, "Laser", new RenderLaser());
		ClientRegistry.registerTileEntity(TileEntityLaserAmplifier.class, "LaserAmplifier", new RenderLaserAmplifier());
		ClientRegistry.registerTileEntity(TileEntityLaserTractorBeam.class, "LaserTractorBeam", new RenderLaserTractorBeam());
		ClientRegistry.registerTileEntity(TileEntitySolarNeutronActivator.class, "SolarNeutronActivator", new RenderSolarNeutronActivator());
		GameRegistry.registerTileEntity(TileEntityAmbientAccumulator.class, "AmbientAccumulator");
		GameRegistry.registerTileEntity(TileEntityInductionCasing.class, "InductionCasing");
		GameRegistry.registerTileEntity(TileEntityInductionPort.class, "InductionPort");
		GameRegistry.registerTileEntity(TileEntityInductionCell.class, "InductionCell");
		GameRegistry.registerTileEntity(TileEntityInductionProvider.class, "InductionProvider");
		GameRegistry.registerTileEntity(TileEntityOredictionificator.class, "Oredictionificator");
	}

	@Override
	public void registerRenderInformation()
	{
		RenderPartTransmitter.init();
		RenderGlowPanel.init();

		//Register entity rendering handlers
		RenderingRegistry.registerEntityRenderingHandler(EntityObsidianTNT.class, new RenderObsidianTNTPrimed());
		RenderingRegistry.registerEntityRenderingHandler(EntityRobit.class, new RenderRobit());
		RenderingRegistry.registerEntityRenderingHandler(EntityBalloon.class, new RenderBalloon());
		RenderingRegistry.registerEntityRenderingHandler(EntityBabySkeleton.class, new RenderSkeleton());
		RenderingRegistry.registerEntityRenderingHandler(EntityFlame.class, new RenderFlame());

		//Register item handler
		ItemRenderingHandler handler = new ItemRenderingHandler();

		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(MekanismBlocks.EnergyCube), handler);
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(MekanismBlocks.MachineBlock), handler);
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(MekanismBlocks.MachineBlock2), handler);
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(MekanismBlocks.MachineBlock3), handler);
		MinecraftForgeClient.registerItemRenderer(MekanismItems.Robit, handler);
		MinecraftForgeClient.registerItemRenderer(MekanismItems.WalkieTalkie, handler);
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(MekanismBlocks.GasTank), handler);
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(MekanismBlocks.ObsidianTNT), handler);
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(MekanismBlocks.BasicBlock), handler);
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(MekanismBlocks.BasicBlock2), handler);
		MinecraftForgeClient.registerItemRenderer(MekanismItems.Jetpack, handler);
		MinecraftForgeClient.registerItemRenderer(MekanismItems.ArmoredJetpack, handler);
		MinecraftForgeClient.registerItemRenderer(MekanismItems.PartTransmitter, handler);
		MinecraftForgeClient.registerItemRenderer(MekanismItems.GasMask, handler);
		MinecraftForgeClient.registerItemRenderer(MekanismItems.ScubaTank, handler);
		MinecraftForgeClient.registerItemRenderer(MekanismItems.Balloon, handler);
		MinecraftForgeClient.registerItemRenderer(MekanismItems.FreeRunners, handler);
		MinecraftForgeClient.registerItemRenderer(MekanismItems.AtomicDisassembler, handler);
		MinecraftForgeClient.registerItemRenderer(MekanismItems.GlowPanel, handler);
		MinecraftForgeClient.registerItemRenderer(MekanismItems.Flamethrower, handler);

		//Register block handlers
		RenderingRegistry.registerBlockHandler(new MachineRenderingHandler());
		RenderingRegistry.registerBlockHandler(new BasicRenderingHandler());
		RenderingRegistry.registerBlockHandler(new PlasticRenderingHandler());
		RenderingRegistry.registerBlockHandler(new CTMRenderingHandler());

		Mekanism.logger.info("Render registrations complete.");
	}

	@Override
	public GuiScreen getClientGui(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);

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
				ItemStack itemStack = player.getCurrentEquippedItem();

				if(itemStack != null && itemStack.getItem() instanceof ItemPortableTeleporter)
				{
					return new GuiTeleporter(player, itemStack);
				}
			case 15:
				return new GuiPurificationChamber(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 16:
				return new GuiEnergizedSmelter(player.inventory, (TileEntityElectricMachine)tileEntity);
			case 17:
				return new GuiElectricPump(player.inventory, (TileEntityElectricPump)tileEntity);
			case 18:
				return new GuiDynamicTank(player.inventory, (TileEntityDynamicTank)tileEntity);
			case 19:
				return new GuiPasswordEnter((TileEntityElectricChest)tileEntity);
			case 20:
				return new GuiPasswordModify((TileEntityElectricChest)tileEntity);
			case 21:
				EntityRobit robit = (EntityRobit)world.getEntityByID(x);

				if(robit != null)
				{
					return new GuiRobitMain(player.inventory, robit);
				}
			case 22:
				return new GuiRobitCrafting(player.inventory, world, x);
			case 23:
				EntityRobit robit1 = (EntityRobit)world.getEntityByID(x);

				if(robit1 != null)
				{
					return new GuiRobitInventory(player.inventory, robit1);
				}
			case 24:
				EntityRobit robit2 = (EntityRobit)world.getEntityByID(x);

				if(robit2 != null)
				{
					return new GuiRobitSmelting(player.inventory, robit2);
				}
			case 25:
				return new GuiRobitRepair(player.inventory, world, x);
			case 29:
				return new GuiChemicalOxidizer(player.inventory, (TileEntityChemicalOxidizer)tileEntity);
			case 30:
				return new GuiChemicalInfuser(player.inventory, (TileEntityChemicalInfuser)tileEntity);
			case 31:
				return new GuiChemicalInjectionChamber(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 32:
				return new GuiElectrolyticSeparator(player.inventory, (TileEntityElectrolyticSeparator)tileEntity);
			case 33:
				return new GuiSolarEvaporationController(player.inventory, (TileEntitySolarEvaporationController)tileEntity);
			case 34:
				return new GuiPrecisionSawmill(player.inventory, (TileEntityPrecisionSawmill)tileEntity);
			case 35:
				return new GuiChemicalDissolutionChamber(player.inventory, (TileEntityChemicalDissolutionChamber)tileEntity);
			case 36:
				return new GuiChemicalWasher(player.inventory, (TileEntityChemicalWasher)tileEntity);
			case 37:
				return new GuiChemicalCrystallizer(player.inventory, (TileEntityChemicalCrystallizer)tileEntity);
			case 38:
				ItemStack itemStack1 = player.getCurrentEquippedItem().copy();

				if(itemStack1 != null && itemStack1.getItem() instanceof ItemSeismicReader)
				{
					return new GuiSeismicReader(world, new Coord4D(player), itemStack1);
				}
			case 39:
				return new GuiSeismicVibrator(player.inventory, (TileEntitySeismicVibrator)tileEntity);
			case 40:
				return new GuiPRC(player.inventory, (TileEntityPRC)tileEntity);
			case 41:
				return new GuiPortableTank(player.inventory, (TileEntityPortableTank)tileEntity);
			case 42:
				return new GuiFluidicPlenisher(player.inventory, (TileEntityFluidicPlenisher)tileEntity);
			case 43:
				return new GuiUpgradeManagement(player.inventory, (IUpgradeTile)tileEntity);
			case 44:
				return new GuiLaserAmplifier(player.inventory, (TileEntityLaserAmplifier)tileEntity);
			case 45:
				return new GuiLaserTractorBeam(player.inventory, (TileEntityLaserTractorBeam)tileEntity);
			case 46:
				return new GuiEntangledBlock(player.inventory, (TileEntityEntangledBlock)tileEntity);
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
	public void addHitEffects(Coord4D coord, MovingObjectPosition mop)
	{
		if(Minecraft.getMinecraft().theWorld != null)
		{
			Minecraft.getMinecraft().effectRenderer.addBlockHitEffects(coord.xCoord, coord.yCoord, coord.zCoord, mop);
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
	public void loadUtilities()
	{
		super.loadUtilities();
		
		FMLCommonHandler.instance().bus().register(new ClientConnectionHandler());
		FMLCommonHandler.instance().bus().register(new ClientPlayerTracker());
		FMLCommonHandler.instance().bus().register(new ClientTickHandler());
		FMLCommonHandler.instance().bus().register(new RenderTickHandler());
		
		new MekanismKeyHandler();

		HolidayManager.init();
	}

	@Override
	public void preInit()
	{
		MekanismRenderer.init();
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
	public void renderLaser(World world, Pos3D from, Pos3D to, ForgeDirection direction, double energy)
	{
		Minecraft.getMinecraft().effectRenderer.addEffect(new EntityLaser(world, from, to, direction, energy));
	}
}
