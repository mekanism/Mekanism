package mekanism.client;

import static mekanism.common.block.states.BlockStatePlastic.colorProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Pos3D;
import mekanism.client.SparkleAnimation.INodeChecker;
import mekanism.client.entity.ParticleLaser;
import mekanism.client.gui.GuiAmbientAccumulator;
import mekanism.client.gui.GuiBoilerStats;
import mekanism.client.gui.GuiCombiner;
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
import mekanism.client.gui.chemical.GuiChemicalCrystallizer;
import mekanism.client.gui.chemical.GuiChemicalDissolutionChamber;
import mekanism.client.gui.chemical.GuiChemicalInfuser;
import mekanism.client.gui.chemical.GuiChemicalInjectionChamber;
import mekanism.client.gui.chemical.GuiChemicalOxidizer;
import mekanism.client.gui.chemical.GuiChemicalWasher;
import mekanism.client.gui.robit.GuiRobitCrafting;
import mekanism.client.gui.robit.GuiRobitInventory;
import mekanism.client.gui.robit.GuiRobitMain;
import mekanism.client.gui.robit.GuiRobitRepair;
import mekanism.client.gui.robit.GuiRobitSmelting;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.entity.RenderBalloon;
import mekanism.client.render.entity.RenderFlame;
import mekanism.client.render.entity.RenderObsidianTNTPrimed;
import mekanism.client.render.entity.RenderRobit;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.RenderEnergyCubeItem;
import mekanism.client.render.item.basicblock.RenderBasicBlockItem;
import mekanism.client.render.item.gear.RenderArmoredJetpack;
import mekanism.client.render.item.gear.RenderAtomicDisassembler;
import mekanism.client.render.item.gear.RenderFlameThrower;
import mekanism.client.render.item.gear.RenderFreeRunners;
import mekanism.client.render.item.gear.RenderGasMask;
import mekanism.client.render.item.gear.RenderJetpack;
import mekanism.client.render.item.gear.RenderScubaTank;
import mekanism.client.render.item.machine.RenderMachineItem;
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
import mekanism.client.sound.SoundHandler;
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
import mekanism.common.block.states.BlockStateTransmitter.TransmitterStateMapper;
import mekanism.common.block.states.BlockStateTransmitter.TransmitterType;
import mekanism.common.config.MekanismConfig.client;
import mekanism.common.entity.EntityBabySkeleton;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.entity.EntityFlame;
import mekanism.common.entity.EntityObsidianTNT;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.InventoryPersonalChest;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.item.ItemBlockGasTank;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemBlockTransmitter;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemSeismicReader;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterMessage;
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
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.prefab.TileEntityDoubleElectricMachine;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import mekanism.common.tile.transmitter.TileEntityRestrictiveTransporter;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import mekanism.common.util.TextComponentGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.entity.RenderSkeleton;
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
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client proxy for the Mekanism mod.
 *
 * @author AidanBrady
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    private static final IStateMapper machineMapper = new MachineBlockStateMapper();
    private static final IStateMapper basicMapper = new BasicBlockStateMapper();
    private static final IStateMapper plasticMapper = new PlasticBlockStateMapper();
    private static final IStateMapper fenceMapper = new PlasticFenceStateMapper();
    private static final IStateMapper boxMapper = new CardboardBoxStateMapper();
    private static final IStateMapper transmitterMapper = new TransmitterStateMapper();
    public static Map<String, ModelResourceLocation> machineResources = new HashMap<>();
    public static Map<String, ModelResourceLocation> basicResources = new HashMap<>();
    public static Map<String, ModelResourceLocation> transmitterResources = new HashMap<>();

    @Override
    public void loadConfiguration() {
        super.loadConfiguration();

        client.enablePlayerSounds = Mekanism.configuration
              .get(Configuration.CATEGORY_CLIENT, "EnablePlayerSounds", true,
                    "Play sounds for Jetpack/Gas Mask/Flamethrower (all players).").getBoolean();
        client.enableMachineSounds = Mekanism.configuration
              .get(Configuration.CATEGORY_CLIENT, "EnableMachineSounds", true,
                    "If enabled machines play their sounds while running.").getBoolean();
        client.holidays = Mekanism.configuration
              .get(Configuration.CATEGORY_CLIENT, "Holidays", true, "Christmas/New Years greetings in chat.")
              .getBoolean();
        client.baseSoundVolume = (float) Mekanism.configuration
              .get(Configuration.CATEGORY_CLIENT, "SoundVolume", 1D,
                    "Adjust Mekanica sounds' base volume. < 1 is softer, higher is louder.").getDouble();
        client.machineEffects = Mekanism.configuration
              .get(Configuration.CATEGORY_CLIENT, "MachineEffects", true, "Show particles when machines active.")
              .getBoolean();
        client.enableAmbientLighting = Mekanism.configuration
              .get(Configuration.CATEGORY_CLIENT, "EnableAmbientLighting", true,
                    "Should active machines produce block light.").getBoolean();
        client.ambientLightingLevel = Mekanism.configuration
              .get(Configuration.CATEGORY_CLIENT, "AmbientLightingLevel", 15,
                    "How much light to produce if ambient lighting is enabled.", 1, 15).getInt();
        client.opaqueTransmitters = Mekanism.configuration
              .get(Configuration.CATEGORY_CLIENT, "OpaqueTransmitterRender", false,
                    "If true, don't render Cables/Pipes/Tubes as transparent and don't render their contents.")
              .getBoolean();
        client.allowConfiguratorModeScroll = Mekanism.configuration
              .get(Configuration.CATEGORY_CLIENT, "ConfiguratorModeScroll", true,
                    "Allow sneak+scroll to change Configurator modes.").getBoolean();
        client.enableMultiblockFormationParticles = Mekanism.configuration
              .get(Configuration.CATEGORY_CLIENT, "MultiblockFormParticles", true,
                    "Set to false to prevent particle spam when loading multiblocks (notification message will still display).")
              .getBoolean();

        if (Mekanism.configuration.hasChanged()) {
            Mekanism.configuration.save();
        }
    }

    @Override
    public void openPersonalChest(EntityPlayer entityplayer, int id, int windowId, boolean isBlock, BlockPos pos,
          EnumHand hand) {
        TileEntityPersonalChest tileEntity = (TileEntityPersonalChest) entityplayer.world.getTileEntity(pos);

        if (id == 0) {
            if (isBlock) {
                FMLClientHandler.instance()
                      .displayGuiScreen(entityplayer, new GuiPersonalChest(entityplayer.inventory, tileEntity));
                entityplayer.openContainer.windowId = windowId;
            } else {
                ItemStack stack = entityplayer.getHeldItem(hand);

                if (MachineType.get(stack) == MachineType.PERSONAL_CHEST) {
                    InventoryPersonalChest inventory = new InventoryPersonalChest(entityplayer, hand);
                    FMLClientHandler.instance()
                          .displayGuiScreen(entityplayer, new GuiPersonalChest(entityplayer.inventory, inventory));
                    entityplayer.openContainer.windowId = windowId;
                }
            }
        }
    }

    @Override
    public void registerTESRs() {
        ClientRegistry
              .bindTileEntitySpecialRenderer(TileEntityAdvancedFactory.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBin.class, new RenderBin());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBoilerCasing.class, new RenderThermoelectricBoiler());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBoilerValve.class, new RenderThermoelectricBoiler());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChargepad.class, new RenderChargepad());
        ClientRegistry
              .bindTileEntitySpecialRenderer(TileEntityChemicalCrystallizer.class, new RenderChemicalCrystallizer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChemicalDissolutionChamber.class,
              new RenderChemicalDissolutionChamber());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChemicalInjectionChamber.class,
              new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCombiner.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrusher.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDigitalMiner.class, new RenderDigitalMiner());
        ClientRegistry
              .bindTileEntitySpecialRenderer(TileEntityDiversionTransporter.class, new RenderLogisticalTransporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDynamicTank.class, new RenderDynamicTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDynamicValve.class, new RenderDynamicTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEliteFactory.class, new RenderConfigurableMachine<>());
        ClientRegistry
              .bindTileEntitySpecialRenderer(TileEntityEnergizedSmelter.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnergyCube.class, new RenderEnergyCube());
        ClientRegistry
              .bindTileEntitySpecialRenderer(TileEntityEnrichmentChamber.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFactory.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFluidTank.class, RenderFluidTank.INSTANCE);
        ClientRegistry
              .bindTileEntitySpecialRenderer(TileEntityFormulaicAssemblicator.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGasTank.class, new RenderGasTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLogisticalSorter.class, new RenderLogisticalSorter());
        ClientRegistry
              .bindTileEntitySpecialRenderer(TileEntityLogisticalTransporter.class, new RenderLogisticalTransporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMechanicalPipe.class, new RenderMechanicalPipe());
        ClientRegistry
              .bindTileEntitySpecialRenderer(TileEntityMetallurgicInfuser.class, new RenderConfigurableMachine<>());
        ClientRegistry
              .bindTileEntitySpecialRenderer(TileEntityOsmiumCompressor.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPRC.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPersonalChest.class, new RenderPersonalChest());
        ClientRegistry
              .bindTileEntitySpecialRenderer(TileEntityPrecisionSawmill.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPressurizedTube.class, new RenderPressurizedTube());
        ClientRegistry
              .bindTileEntitySpecialRenderer(TileEntityPurificationChamber.class, new RenderConfigurableMachine<>());
        ClientRegistry
              .bindTileEntitySpecialRenderer(TileEntityQuantumEntangloporter.class, new RenderQuantumEntangloporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityResistiveHeater.class, new RenderResistiveHeater());
        ClientRegistry
              .bindTileEntitySpecialRenderer(TileEntityRestrictiveTransporter.class, new RenderLogisticalTransporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySecurityDesk.class, new RenderSecurityDesk());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySeismicVibrator.class, new RenderSeismicVibrator());
        ClientRegistry
              .bindTileEntitySpecialRenderer(TileEntitySolarNeutronActivator.class, new RenderSolarNeutronActivator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTeleporter.class, new RenderTeleporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThermalEvaporationController.class,
              new RenderThermalEvaporationController());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThermodynamicConductor.class,
              new RenderThermodynamicConductor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityUniversalCable.class, new RenderUniversalCable());
    }

    @Override
    public void registerItemRenders() {
        registerItemRender(MekanismItems.ElectricBow);
        registerItemRender(MekanismItems.Dust);
        registerItemRender(MekanismItems.Ingot);
        registerItemRender(MekanismItems.Nugget);
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

        ModelBakery.registerItemVariants(MekanismItems.WalkieTalkie, ItemWalkieTalkie.OFF_MODEL);

        for (int i = 1; i <= 9; i++) {
            ModelBakery.registerItemVariants(MekanismItems.WalkieTalkie, ItemWalkieTalkie.getModel(i));
        }

        ModelBakery.registerItemVariants(MekanismItems.CraftingFormula, ItemCraftingFormula.MODEL,
              ItemCraftingFormula.INVALID_MODEL, ItemCraftingFormula.ENCODED_MODEL);

        MekanismItems.Jetpack.setTileEntityItemStackRenderer(new RenderJetpack());
        MekanismItems.ArmoredJetpack.setTileEntityItemStackRenderer(new RenderArmoredJetpack());
        MekanismItems.GasMask.setTileEntityItemStackRenderer(new RenderGasMask());
        MekanismItems.ScubaTank.setTileEntityItemStackRenderer(new RenderScubaTank());
        MekanismItems.FreeRunners.setTileEntityItemStackRenderer(new RenderFreeRunners());
        MekanismItems.AtomicDisassembler.setTileEntityItemStackRenderer(new RenderAtomicDisassembler());
        MekanismItems.Flamethrower.setTileEntityItemStackRenderer(new RenderFlameThrower());
        Item.getItemFromBlock(MekanismBlocks.EnergyCube).setTileEntityItemStackRenderer(new RenderEnergyCubeItem());
        Item.getItemFromBlock(MekanismBlocks.MachineBlock).setTileEntityItemStackRenderer(new RenderMachineItem());
        Item.getItemFromBlock(MekanismBlocks.MachineBlock2).setTileEntityItemStackRenderer(new RenderMachineItem());
        Item.getItemFromBlock(MekanismBlocks.MachineBlock3).setTileEntityItemStackRenderer(new RenderMachineItem());
        Item.getItemFromBlock(MekanismBlocks.BasicBlock2).setTileEntityItemStackRenderer(new RenderBasicBlockItem());

    }

    private ModelResourceLocation getInventoryMRL(String type) {
        return new ModelResourceLocation("mekanism:" + type, "inventory");
    }

    @Override
    public void registerBlockRenders() {
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
        ModelLoader.setCustomStateMapper(MekanismBlocks.Transmitter, transmitterMapper);

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.ObsidianTNT), 0,
              getInventoryMRL("ObsidianTNT"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.SaltBlock), 0,
              getInventoryMRL("SaltBlock"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.CardboardBox), 0,
              new ModelResourceLocation("mekanism:CardboardBox", "storage=false"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.CardboardBox), 1,
              new ModelResourceLocation("mekanism:CardboardBox", "storage=true"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.EnergyCube), 0,
              getInventoryMRL("EnergyCube"));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.MachineBlock), 4,
              getInventoryMRL("digital_miner"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.MachineBlock), 13,
              getInventoryMRL("personal_chest"));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.MachineBlock2), 6,
              getInventoryMRL("chemical_dissolution_chamber"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.MachineBlock2), 8,
              getInventoryMRL("chemical_crystallizer"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.MachineBlock2), 9,
              getInventoryMRL("seismic_vibrator"));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.MachineBlock2), 11,
              getInventoryMRL("fluid_tank"));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.MachineBlock3), 0,
              getInventoryMRL("quantum_entangloporter"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.MachineBlock3), 1,
              getInventoryMRL("solar_neutron_activator"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.MachineBlock3), 4,
              getInventoryMRL("resistive_heater"));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.BasicBlock2), 9,
              getInventoryMRL("security_desk"));

        for (int i = 0; i < EnumColor.DYES.length; i++) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.GlowPanel), i,
                  getInventoryMRL("glowpanel"));
        }

        for (MachineType type : MachineType.values()) {
            if (!type.isValidMachine()) {
                continue;
            }

            List<ModelResourceLocation> modelsToAdd = new ArrayList<>();
            String resource = "mekanism:" + type.getName();
            RecipeType recipePointer = null;

            if (type == MachineType.BASIC_FACTORY || type == MachineType.ADVANCED_FACTORY
                  || type == MachineType.ELITE_FACTORY) {
                recipePointer = RecipeType.values()[0];
                resource = "mekanism:" + type.getName() + "_" + recipePointer.getName();
            }

            while (true) {
                if (machineResources.get(resource) == null) {
                    List<String> entries = new ArrayList<>();

                    if (type.hasActiveTexture()) {
                        entries.add("active=false");
                    }

                    if (type.hasRotations()) {
                        entries.add("facing=north");
                    }

                    String properties = getProperties(entries);

                    ModelResourceLocation model = new ModelResourceLocation(resource, properties);

                    machineResources.put(resource, model);
                    modelsToAdd.add(model);

                    if (type == MachineType.BASIC_FACTORY || type == MachineType.ADVANCED_FACTORY
                          || type == MachineType.ELITE_FACTORY) {
                        if (recipePointer.ordinal() < RecipeType.values().length - 1) {
                            recipePointer = RecipeType.values()[recipePointer.ordinal() + 1];
                            resource = "mekanism:" + type.getName() + "_" + recipePointer.getName();

                            continue;
                        }
                    }
                }

                break;
            }

            ModelLoader.registerItemVariants(Item.getItemFromBlock(type.typeBlock.getBlock()),
                  modelsToAdd.toArray(new ModelResourceLocation[]{}));
        }

        for (BasicBlockType type : BasicBlockType.values()) {
            List<ModelResourceLocation> modelsToAdd = new ArrayList<>();
            String resource = "mekanism:" + type.getName();
            BaseTier tierPointer = null;

            if (type.tiers) {
                tierPointer = BaseTier.values()[0];
                resource = "mekanism:" + type.getName() + "_" + tierPointer.getName();
            }

            while (true) {
                if (basicResources.get(resource) == null) {
                    List<String> entries = new ArrayList<>();

                    if (type.hasActiveTexture()) {
                        entries.add("active=false");
                    }

                    if (type.hasRotations() || type == BasicBlockType.THERMAL_EVAPORATION_CONTROLLER) {
                        entries.add("facing=north");
                    }

                    //TODO: Is this check against bin's needed
                    String properties = type == BasicBlockType.BIN ? "inventory" : getProperties(entries);

                    ModelResourceLocation model = new ModelResourceLocation(resource, properties);

                    basicResources.put(resource, model);
                    modelsToAdd.add(model);

                    if (type.tiers) {
                        if (tierPointer.ordinal() < BaseTier.values().length - 1) {
                            tierPointer = BaseTier.values()[tierPointer.ordinal() + 1];

                            if (type == BasicBlockType.BIN || tierPointer.isObtainable()) {
                                resource = "mekanism:" + type.getName() + "_" + tierPointer.getName();

                                continue;
                            }
                        }
                    }
                }

                break;
            }

            ModelLoader.registerItemVariants(Item.getItemFromBlock(type.blockType.getBlock()),
                  modelsToAdd.toArray(new ModelResourceLocation[]{}));
        }

        for (TransmitterType type : TransmitterType.values()) {
            List<ModelResourceLocation> modelsToAdd = new ArrayList<>();
            String resource = "mekanism:" + type.getName();
            BaseTier tierPointer = null;

            if (type.hasTiers()) {
                tierPointer = BaseTier.values()[0];
                resource = "mekanism:" + type.getName() + "_" + tierPointer.getName();
            }

            while (true) {
                if (transmitterResources.get(resource) == null) {
                    String properties = "inventory";
                    ModelResourceLocation model = new ModelResourceLocation(resource, properties);

                    transmitterResources.put(resource, model);
                    modelsToAdd.add(model);

                    if (type.hasTiers()) {
                        if (tierPointer.ordinal() < BaseTier.values().length - 1) {
                            tierPointer = BaseTier.values()[tierPointer.ordinal() + 1];

                            if (tierPointer.isObtainable()) {
                                resource = "mekanism:" + type.getName() + "_" + tierPointer.getName();

                                continue;
                            }
                        }
                    }
                }

                break;
            }

            ModelLoader.registerItemVariants(Item.getItemFromBlock(MekanismBlocks.Transmitter),
                  modelsToAdd.toArray(new ModelResourceLocation[]{}));
        }

        for (EnumColor color : EnumColor.DYES) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.PlasticBlock),
                  color.getMetaValue(), new ModelResourceLocation("mekanism:plastic_block", "type=plastic"));
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.SlickPlasticBlock),
                  color.getMetaValue(), new ModelResourceLocation("mekanism:plastic_block", "type=slick"));
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.GlowPlasticBlock),
                  color.getMetaValue(), new ModelResourceLocation("mekanism:plastic_block", "type=glow"));
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.ReinforcedPlasticBlock),
                  color.getMetaValue(), new ModelResourceLocation("mekanism:plastic_block", "type=reinforced"));
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.RoadPlasticBlock),
                  color.getMetaValue(), new ModelResourceLocation("mekanism:plastic_block", "type=road"));
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.PlasticFence),
                  color.getMetaValue(), getInventoryMRL("PlasticFence"));
        }

        for (EnumOreType ore : EnumOreType.values()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MekanismBlocks.OreBlock), ore.ordinal(),
                  new ModelResourceLocation("mekanism:OreBlock", "type=" + ore.getName()));
        }

        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(MekanismBlocks.GasTank), stack ->
        {
            GasTankTier tier = GasTankTier.values()[((ItemBlockGasTank) stack.getItem()).getBaseTier(stack).ordinal()];
            ResourceLocation baseLocation = new ResourceLocation("mekanism", "GasTank");

            return new ModelResourceLocation(baseLocation, "facing=north,tier=" + tier);
        });

        ItemMeshDefinition machineMesher = stack ->
        {
            MachineType type = MachineType.get(stack);

            if (type != null) {
                String resource = "mekanism:" + type.getName();

                if (type == MachineType.BASIC_FACTORY || type == MachineType.ADVANCED_FACTORY
                      || type == MachineType.ELITE_FACTORY) {
                    RecipeType recipe = RecipeType.values()[((ItemBlockMachine) stack.getItem()).getRecipeType(stack)];
                    resource = "mekanism:" + type.getName() + "_" + recipe.getName();
                }

                return machineResources.get(resource);
            }

            return null;
        };

        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(MekanismBlocks.MachineBlock), machineMesher);
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(MekanismBlocks.MachineBlock2), machineMesher);
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(MekanismBlocks.MachineBlock3), machineMesher);

        ItemMeshDefinition basicMesher = stack ->
        {
            BasicBlockType type = BasicBlockType.get(stack);

            if (type != null) {
                String resource = "mekanism:" + type.getName();

                if (type.tiers) {
                    BaseTier tier = ((ItemBlockBasic) stack.getItem()).getBaseTier(stack);
                    resource = "mekanism:" + type.getName() + "_" + tier.getName();
                }

                return basicResources.get(resource);
            }

            return null;
        };

        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(MekanismBlocks.BasicBlock), basicMesher);
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(MekanismBlocks.BasicBlock2), basicMesher);

        ItemMeshDefinition transmitterMesher = stack ->
        {
            TransmitterType type = TransmitterType.get(stack.getItemDamage());

            if (type != null) {
                String resource = "mekanism:" + type.getName();

                if (type.hasTiers()) {
                    BaseTier tier = ((ItemBlockTransmitter) stack.getItem()).getBaseTier(stack);
                    resource = "mekanism:" + type.getName() + "_" + tier.getName();
                }

                return transmitterResources.get(resource);
            }

            return null;
        };

        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(MekanismBlocks.Transmitter), transmitterMesher);

        //Walkie Talkie dynamic texture
        ModelLoader.setCustomMeshDefinition(MekanismItems.WalkieTalkie, stack ->
        {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemWalkieTalkie) {
                ItemWalkieTalkie item = (ItemWalkieTalkie) stack.getItem();

                if (item.getOn(stack)) {
                    return ItemWalkieTalkie.CHANNEL_MODELS.get(item.getChannel(stack));
                }
            }

            return ItemWalkieTalkie.OFF_MODEL;
        });

        //Crafting Formula dynamic texture
        ModelLoader.setCustomMeshDefinition(MekanismItems.CraftingFormula, stack ->
        {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemCraftingFormula) {
                ItemCraftingFormula item = (ItemCraftingFormula) stack.getItem();

                if (item.getInventory(stack) == null) {
                    return ItemCraftingFormula.MODEL;
                } else {
                    return item.isInvalid(stack) ? ItemCraftingFormula.INVALID_MODEL
                          : ItemCraftingFormula.ENCODED_MODEL;
                }
            }

            return ItemCraftingFormula.MODEL;
        });

        OBJLoader.INSTANCE.addDomain("mekanism");
    }

    public void registerItemRender(Item item) {
        MekanismRenderer.registerItemRender("mekanism", item);
    }

    private String getProperties(List<String> entries) {
        StringBuilder properties = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            properties.append(entries.get(i));
            if (i < entries.size() - 1) {
                properties.append(",");
            }
        }
        return properties.toString();
    }

    @Override
    public GuiScreen getClientGui(int ID, EntityPlayer player, World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);

        switch (ID) {
            case 0:
                return new GuiDictionary(player.inventory);
            case 1:
                break; // Used to be credits UI
            case 2:
                return new GuiDigitalMiner(player.inventory, (TileEntityDigitalMiner) tileEntity);
            case 3:
                return new GuiEnrichmentChamber(player.inventory, (TileEntityElectricMachine) tileEntity);
            case 4:
                return new GuiOsmiumCompressor(player.inventory, (TileEntityAdvancedElectricMachine) tileEntity);
            case 5:
                return new GuiCombiner(player.inventory, (TileEntityDoubleElectricMachine) tileEntity);
            case 6:
                return new GuiCrusher(player.inventory, (TileEntityElectricMachine) tileEntity);
            case 7:
                return new GuiRotaryCondensentrator(player.inventory, (TileEntityRotaryCondensentrator) tileEntity);
            case 8:
                return new GuiEnergyCube(player.inventory, (TileEntityEnergyCube) tileEntity);
            case 9:
                return new GuiSideConfiguration(player, (ISideConfiguration) tileEntity);
            case 10:
                return new GuiGasTank(player.inventory, (TileEntityGasTank) tileEntity);
            case 11:
                return new GuiFactory(player.inventory, (TileEntityFactory) tileEntity);
            case 12:
                return new GuiMetallurgicInfuser(player.inventory, (TileEntityMetallurgicInfuser) tileEntity);
            case 13:
                return new GuiTeleporter(player.inventory, (TileEntityTeleporter) tileEntity);
            case 14:
                ItemStack itemStack = player.getHeldItem(EnumHand.values()[pos.getX()]);

                if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemPortableTeleporter) {
                    return new GuiTeleporter(player, EnumHand.values()[pos.getX()], itemStack);
                }

                return null;
            case 15:
                return new GuiPurificationChamber(player.inventory, (TileEntityAdvancedElectricMachine) tileEntity);
            case 16:
                return new GuiEnergizedSmelter(player.inventory, (TileEntityElectricMachine) tileEntity);
            case 17:
                return new GuiElectricPump(player.inventory, (TileEntityElectricPump) tileEntity);
            case 18:
                return new GuiDynamicTank(player.inventory, (TileEntityDynamicTank) tileEntity);
            //EMPTY 19, 20
            case 21:
                EntityRobit robit = (EntityRobit) world.getEntityByID(pos.getX());

                if (robit != null) {
                    return new GuiRobitMain(player.inventory, robit);
                }

                return null;
            case 22:
                robit = (EntityRobit) world.getEntityByID(pos.getX());

                if (robit != null) {
                    return new GuiRobitCrafting(player.inventory, robit);
                }

                return null;
            case 23:
                robit = (EntityRobit) world.getEntityByID(pos.getX());

                if (robit != null) {
                    return new GuiRobitInventory(player.inventory, robit);
                }

                return null;
            case 24:
                robit = (EntityRobit) world.getEntityByID(pos.getX());

                if (robit != null) {
                    return new GuiRobitSmelting(player.inventory, robit);
                }

                return null;
            case 25:
                robit = (EntityRobit) world.getEntityByID(pos.getX());

                if (robit != null) {
                    return new GuiRobitRepair(player.inventory, robit);
                }

                return null;
            case 29:
                return new GuiChemicalOxidizer(player.inventory, (TileEntityChemicalOxidizer) tileEntity);
            case 30:
                return new GuiChemicalInfuser(player.inventory, (TileEntityChemicalInfuser) tileEntity);
            case 31:
                return new GuiChemicalInjectionChamber(player.inventory,
                      (TileEntityAdvancedElectricMachine) tileEntity);
            case 32:
                return new GuiElectrolyticSeparator(player.inventory, (TileEntityElectrolyticSeparator) tileEntity);
            case 33:
                return new GuiThermalEvaporationController(player.inventory,
                      (TileEntityThermalEvaporationController) tileEntity);
            case 34:
                return new GuiPrecisionSawmill(player.inventory, (TileEntityPrecisionSawmill) tileEntity);
            case 35:
                return new GuiChemicalDissolutionChamber(player.inventory,
                      (TileEntityChemicalDissolutionChamber) tileEntity);
            case 36:
                return new GuiChemicalWasher(player.inventory, (TileEntityChemicalWasher) tileEntity);
            case 37:
                return new GuiChemicalCrystallizer(player.inventory, (TileEntityChemicalCrystallizer) tileEntity);
            case 38:
                ItemStack itemStack1 = player.getHeldItem(EnumHand.values()[pos.getX()]);

                if (!itemStack1.isEmpty() && itemStack1.getItem() instanceof ItemSeismicReader) {
                    return new GuiSeismicReader(world, new Coord4D(player), itemStack1.copy());
                }

                return null;
            case 39:
                return new GuiSeismicVibrator(player.inventory, (TileEntitySeismicVibrator) tileEntity);
            case 40:
                return new GuiPRC(player.inventory, (TileEntityPRC) tileEntity);
            case 41:
                return new GuiFluidTank(player.inventory, (TileEntityFluidTank) tileEntity);
            case 42:
                return new GuiFluidicPlenisher(player.inventory, (TileEntityFluidicPlenisher) tileEntity);
            case 43:
                return new GuiUpgradeManagement(player.inventory, (IUpgradeTile) tileEntity);
            case 44:
                return new GuiLaserAmplifier(player.inventory, (TileEntityLaserAmplifier) tileEntity);
            case 45:
                return new GuiLaserTractorBeam(player.inventory, (TileEntityLaserTractorBeam) tileEntity);
            case 46:
                return new GuiQuantumEntangloporter(player.inventory, (TileEntityQuantumEntangloporter) tileEntity);
            case 47:
                return new GuiSolarNeutronActivator(player.inventory, (TileEntitySolarNeutronActivator) tileEntity);
            case 48:
                return new GuiAmbientAccumulator(player, (TileEntityAmbientAccumulator) tileEntity);
            case 49:
                return new GuiInductionMatrix(player.inventory, (TileEntityInductionCasing) tileEntity);
            case 50:
                return new GuiMatrixStats(player.inventory, (TileEntityInductionCasing) tileEntity);
            case 51:
                return new GuiTransporterConfig(player, (ISideConfiguration) tileEntity);
            case 52:
                return new GuiOredictionificator(player.inventory, (TileEntityOredictionificator) tileEntity);
            case 53:
                return new GuiResistiveHeater(player.inventory, (TileEntityResistiveHeater) tileEntity);
            case 54:
                return new GuiThermoelectricBoiler(player.inventory, (TileEntityBoilerCasing) tileEntity);
            case 55:
                return new GuiBoilerStats(player.inventory, (TileEntityBoilerCasing) tileEntity);
            case 56:
                return new GuiFormulaicAssemblicator(player.inventory, (TileEntityFormulaicAssemblicator) tileEntity);
            case 57:
                return new GuiSecurityDesk(player.inventory, (TileEntitySecurityDesk) tileEntity);
            case 58:
                return new GuiFuelwoodHeater(player.inventory, (TileEntityFuelwoodHeater) tileEntity);
        }

        return null;
    }

    @Override
    public void handleTeleporterUpdate(PortableTeleporterMessage message) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;

        if (screen instanceof GuiTeleporter && !((GuiTeleporter) screen).isStackEmpty()) {
            GuiTeleporter teleporter = (GuiTeleporter) screen;

            teleporter.setStatus(message.status);
            teleporter.setFrequency(message.frequency);
            teleporter.setPublicCache(message.publicCache);
            teleporter.setPrivateCache(message.privateCache);

            teleporter.updateButtons();
        }
    }

    @Override
    public void addHitEffects(Coord4D coord, RayTraceResult mop) {
        if (Minecraft.getMinecraft().world != null) {
            Minecraft.getMinecraft().effectRenderer.addBlockHitEffects(coord.getPos(), mop);
        }
    }

    @Override
    public void doGenericSparkle(TileEntity tileEntity, INodeChecker checker) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (tileEntity.getPos().distanceSq(player.getPosition()) <= 100) {
            player.sendStatusMessage(
                  new TextComponentGroup(TextFormatting.BLUE).translation("chat.mek.multiblockformed"), true);
            if (client.enableMultiblockFormationParticles) {
                new SparkleAnimation(tileEntity, checker).run();
            }
        }
    }

    @Override
    public void doMultiblockSparkle(final TileEntityMultiblock<?> tileEntity) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (tileEntity.getPos().distanceSq(player.getPosition()) <= 100) {
            player.sendStatusMessage(
                  new TextComponentGroup(TextFormatting.BLUE).translation("chat.mek.multiblockformed"), true);
            if (client.enableMultiblockFormationParticles) {
                new SparkleAnimation(tileEntity, tile -> MultiblockManager.areEqual(tile, tileEntity)).run();
            }
        }
    }

    @Override
    public void init() {
        super.init();

        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, worldIn, pos, tintIndex) ->
        {
            BlockMachine machine = (BlockMachine) state.getBlock();

            if (state.getValue(machine.getMachineBlock().getProperty()) == MachineType.FLUID_TANK) {
                EnumColor color = state.getValue(BlockStateMachine.tierProperty).getColor();

                return (int) (color.getColor(0) * 255) << 16 | (int) (color.getColor(1) * 255) << 8 | (int) (
                      color.getColor(2) * 255);
            }

            return -1;
        }, MekanismBlocks.MachineBlock, MekanismBlocks.MachineBlock2, MekanismBlocks.MachineBlock3);
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, worldIn, pos, tintIndex) ->
              {
                  EnumDyeColor color = state.getValue(colorProperty);
                  EnumColor dye = EnumColor.DYES[color.getDyeDamage()];

                  return (int) (dye.getColor(0) * 255) << 16 | (int) (dye.getColor(1) * 255) << 8 | (int) (dye.getColor(2)
                        * 255);
              }, MekanismBlocks.PlasticBlock, MekanismBlocks.GlowPlasticBlock, MekanismBlocks.RoadPlasticBlock,
              MekanismBlocks.ReinforcedPlasticBlock,
              MekanismBlocks.SlickPlasticBlock, MekanismBlocks.PlasticFence);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack, tintIndex) ->
        {
            if (MachineType.get(stack) == MachineType.FLUID_TANK) {
                EnumColor color = ((ItemBlockMachine) stack.getItem()).getBaseTier(stack).getColor();

                return (int) (color.getColor(0) * 255) << 16 | (int) (color.getColor(1) * 255) << 8 | (int) (
                      color.getColor(2) * 255);
            }

            return -1;
        }, MekanismBlocks.MachineBlock, MekanismBlocks.MachineBlock2, MekanismBlocks.MachineBlock3);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack, tintIndex) ->
              {
                  EnumDyeColor dyeColor = EnumDyeColor.byDyeDamage(stack.getItemDamage() & 15);
                  EnumColor dye = EnumColor.DYES[dyeColor.getDyeDamage()];

                  return (int) (dye.getColor(0) * 255) << 16 | (int) (dye.getColor(1) * 255) << 8 | (int) (dye.getColor(2)
                        * 255);
              }, MekanismBlocks.PlasticBlock, MekanismBlocks.GlowPlasticBlock, MekanismBlocks.RoadPlasticBlock,
              MekanismBlocks.ReinforcedPlasticBlock,
              MekanismBlocks.SlickPlasticBlock, MekanismBlocks.PlasticFence);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack, tintIndex) ->
        {
            EnumColor dye = EnumColor.DYES[stack.getItemDamage()];

            return (int) (dye.getColor(0) * 255) << 16 | (int) (dye.getColor(1) * 255) << 8 | (int) (dye.getColor(2)
                  * 255);
        }, MekanismItems.Balloon);

        MinecraftForge.EVENT_BUS.register(new ClientPlayerTracker());
        MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
        MinecraftForge.EVENT_BUS.register(new RenderTickHandler());
        MinecraftForge.EVENT_BUS.register(SoundHandler.class);

        new MekanismKeyHandler();

        HolidayManager.init();
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        ModelResourceLocation ERL = getInventoryMRL("EnergyCube");
        modelRegistry.putObject(ERL, RenderEnergyCubeItem.model = new ItemLayerWrapper(modelRegistry.getObject(ERL)));

        ModelResourceLocation JetpackRL = getInventoryMRL("Jetpack");
        modelRegistry
              .putObject(JetpackRL, RenderJetpack.model = new ItemLayerWrapper(modelRegistry.getObject(JetpackRL)));

        ModelResourceLocation ArmorJetpackRL = getInventoryMRL("ArmoredJetpack");
        modelRegistry.putObject(ArmorJetpackRL,
              RenderArmoredJetpack.model = new ItemLayerWrapper(modelRegistry.getObject(ArmorJetpackRL)));

        ModelResourceLocation GasMaskRL = getInventoryMRL("GasMask");
        modelRegistry
              .putObject(GasMaskRL, RenderGasMask.model = new ItemLayerWrapper(modelRegistry.getObject(GasMaskRL)));

        ModelResourceLocation ScubaTankRL = getInventoryMRL("ScubaTank");
        modelRegistry.putObject(ScubaTankRL,
              RenderScubaTank.model = new ItemLayerWrapper(modelRegistry.getObject(ScubaTankRL)));

        ModelResourceLocation FreeRunnerRL = getInventoryMRL("FreeRunners");
        modelRegistry.putObject(FreeRunnerRL,
              RenderFreeRunners.model = new ItemLayerWrapper(modelRegistry.getObject(FreeRunnerRL)));

        ModelResourceLocation AtomicDisassemblerRL = getInventoryMRL("AtomicDisassembler");
        modelRegistry.putObject(AtomicDisassemblerRL,
              RenderAtomicDisassembler.model = new ItemLayerWrapper(modelRegistry.getObject(AtomicDisassemblerRL)));

        ModelResourceLocation FlamethrowerRL = getInventoryMRL("Flamethrower");
        modelRegistry.putObject(FlamethrowerRL,
              RenderFlameThrower.model = new ItemLayerWrapper(modelRegistry.getObject(FlamethrowerRL)));

        machineModelBake(modelRegistry, "digital_miner", MachineType.DIGITAL_MINER);
        machineModelBake(modelRegistry, "solar_neutron_activator", MachineType.SOLAR_NEUTRON_ACTIVATOR);
        machineModelBake(modelRegistry, "chemical_dissolution_chamber", MachineType.CHEMICAL_DISSOLUTION_CHAMBER);
        machineModelBake(modelRegistry, "chemical_crystallizer", MachineType.CHEMICAL_CRYSTALLIZER);
        machineModelBake(modelRegistry, "seismic_vibrator", MachineType.SEISMIC_VIBRATOR);
        machineModelBake(modelRegistry, "quantum_entangloporter", MachineType.QUANTUM_ENTANGLOPORTER);
        machineModelBake(modelRegistry, "resistive_heater", MachineType.RESISTIVE_HEATER);
        machineModelBake(modelRegistry, "personal_chest", MachineType.PERSONAL_CHEST);

        machineModelBake(modelRegistry, "fluid_tank", MachineType.FLUID_TANK);

        //basicBlockModelBake(modelRegistry, "bin", BasicBlockType.BIN);
        basicBlockModelBake(modelRegistry, "security_desk", BasicBlockType.SECURITY_DESK);
    }

    private void machineModelBake(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry, String type,
          MachineType machineType) {
        ModelResourceLocation modelResourceLocation = getInventoryMRL(type);
        ItemLayerWrapper itemLayerWrapper = new ItemLayerWrapper(modelRegistry.getObject(modelResourceLocation));
        RenderMachineItem.modelMap.put(machineType, itemLayerWrapper);
        modelRegistry.putObject(modelResourceLocation, itemLayerWrapper);
    }

    private void basicBlockModelBake(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry, String type,
          BasicBlockType basicType) {
        ModelResourceLocation modelResourceLocation = getInventoryMRL(type);
        ItemLayerWrapper itemLayerWrapper = new ItemLayerWrapper(modelRegistry.getObject(modelResourceLocation));
        RenderBasicBlockItem.modelMap.put(basicType, itemLayerWrapper);
        modelRegistry.putObject(modelResourceLocation, itemLayerWrapper);
    }

    @Override
    public void preInit() {
        MekanismRenderer.init();

        ModelLoaderRegistry.registerLoader(MekanismOBJLoader.INSTANCE);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(MekanismOBJLoader.INSTANCE);

        //Register entity rendering handlers
        RenderingRegistry.registerEntityRenderingHandler(EntityObsidianTNT.class, RenderObsidianTNTPrimed::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityRobit.class, RenderRobit::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBalloon.class, RenderBalloon::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBabySkeleton.class, RenderSkeleton::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFlame.class, RenderFlame::new);
    }

    @Override
    public double getReach(EntityPlayer player) {
        return Minecraft.getMinecraft().playerController.getBlockReachDistance();
    }

    @Override
    public boolean isPaused() {
        if (FMLClientHandler.instance().getClient().isSingleplayer() && !FMLClientHandler.instance().getClient()
              .getIntegratedServer().getPublic()) {
            GuiScreen screen = FMLClientHandler.instance().getClient().currentScreen;

            return screen != null && screen.doesGuiPauseGame();
        }

        return false;
    }

    @Override
    public File getMinecraftDir() {
        return Minecraft.getMinecraft().gameDir;
    }

    @Override
    public EntityPlayer getPlayer(MessageContext context) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            return context.getServerHandler().player;
        } else {
            return Minecraft.getMinecraft().player;
        }
    }

    @Override
    public void handlePacket(Runnable runnable, EntityPlayer player) {
        if (player == null || player.world.isRemote) {
            Minecraft.getMinecraft().addScheduledTask(runnable);
        } else {
            ((WorldServer) player.world).addScheduledTask(runnable); //singleplayer
        }
    }

    @Override
    public void renderLaser(World world, Pos3D from, Pos3D to, EnumFacing direction, double energy) {
        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleLaser(world, from, to, direction, energy));
    }

    @Override
    public FontRenderer getFontRenderer() {
        return Minecraft.getMinecraft().fontRenderer;
    }

    @Override
    public void throwApiPresentException() {
        throw new ApiJarPresentException(API_PRESENT_MESSAGE);
    }
}
