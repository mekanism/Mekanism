package mekanism.client;

import java.io.File;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.Pos3D;
import mekanism.client.SparkleAnimation.INodeChecker;
import mekanism.client.entity.ParticleLaser;
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
import mekanism.client.gui.GuiLogisticalSorter;
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
import mekanism.client.render.item.block.RenderChemicalCrystallizerItem;
import mekanism.client.render.item.block.RenderChemicalDissolutionChamberItem;
import mekanism.client.render.item.block.RenderDigitalMinerItem;
import mekanism.client.render.item.block.RenderEnergyCubeItem;
import mekanism.client.render.item.block.RenderFluidTankItem;
import mekanism.client.render.item.block.RenderPersonalChestItem;
import mekanism.client.render.item.block.RenderQuantumEntangloporterItem;
import mekanism.client.render.item.block.RenderResistiveHeaterItem;
import mekanism.client.render.item.block.RenderSecurityDeskItem;
import mekanism.client.render.item.block.RenderSeismicVibratorItem;
import mekanism.client.render.item.block.RenderSolarNeutronActivatorItem;
import mekanism.client.render.item.gear.RenderArmoredJetpack;
import mekanism.client.render.item.gear.RenderAtomicDisassembler;
import mekanism.client.render.item.gear.RenderFlameThrower;
import mekanism.client.render.item.gear.RenderFreeRunners;
import mekanism.client.render.item.gear.RenderGasMask;
import mekanism.client.render.item.gear.RenderJetpack;
import mekanism.client.render.item.gear.RenderScubaTank;
import mekanism.client.render.obj.MekanismOBJLoader;
import mekanism.client.render.tileentity.RenderBin;
import mekanism.client.render.tileentity.RenderChemicalCrystallizer;
import mekanism.client.render.tileentity.RenderChemicalDissolutionChamber;
import mekanism.client.render.tileentity.RenderConfigurableMachine;
import mekanism.client.render.tileentity.RenderDigitalMiner;
import mekanism.client.render.tileentity.RenderDynamicTank;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.client.render.tileentity.RenderGasTank;
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
import mekanism.common.MekanismBlock;
import mekanism.common.MekanismItem;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.plastic.BlockPlasticFence.PlasticFenceStateMapper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.entity.EntityBabySkeleton;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.entity.EntityFlame;
import mekanism.common.entity.EntityObsidianTNT;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.InventoryPersonalChest;
import mekanism.common.item.IItemRedirectedModel;
import mekanism.common.item.ItemBalloon;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.item.ItemDictionary;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemSeismicReader;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.item.block.machine.ItemBlockPersonalChest;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterMessage;
import mekanism.common.recipe.machines.CombinerRecipe;
import mekanism.common.recipe.machines.CrusherRecipe;
import mekanism.common.recipe.machines.EnrichmentRecipe;
import mekanism.common.recipe.machines.InjectionRecipe;
import mekanism.common.recipe.machines.OsmiumCompressorRecipe;
import mekanism.common.recipe.machines.PurificationRecipe;
import mekanism.common.recipe.machines.SmeltingRecipe;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.TileEntityBoilerValve;
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
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.TileEntityFuelwoodHeater;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
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
import mekanism.common.tile.bin.TileEntityBin;
import mekanism.common.tile.energy_cube.TileEntityEnergyCube;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.fluid_tank.TileEntityFluidTank;
import mekanism.common.tile.gas_tank.TileEntityGasTank;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.prefab.TileEntityDoubleElectricMachine;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import mekanism.common.tile.transmitter.logistical_transporter.TileEntityDiversionTransporter;
import mekanism.common.tile.transmitter.logistical_transporter.TileEntityLogisticalTransporter;
import mekanism.common.tile.transmitter.logistical_transporter.TileEntityRestrictiveTransporter;
import mekanism.common.tile.transmitter.mechanical_pipe.TileEntityMechanicalPipe;
import mekanism.common.tile.transmitter.pressurized_tube.TileEntityPressurizedTube;
import mekanism.common.tile.transmitter.thermodynamic_conductor.TileEntityThermodynamicConductor;
import mekanism.common.tile.transmitter.universal_cable.TileEntityUniversalCable;
import mekanism.common.util.TextComponentGroup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.PlayerEntitySP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

/**
 * Client proxy for the Mekanism mod.
 *
 * @author AidanBrady
 */
@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    private static final IStateMapper fenceMapper = new PlasticFenceStateMapper();

    @Override
    public void loadConfiguration() {
        super.loadConfiguration();
        MekanismConfig.current().client.load(Mekanism.configuration);
        if (Mekanism.configuration.hasChanged()) {
            Mekanism.configuration.save();
        }
    }

    @Override
    public void registerTESRs() {
        //TODO?
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBin.class, new RenderBin());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBoilerCasing.class, new RenderThermoelectricBoiler());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBoilerValve.class, new RenderThermoelectricBoiler());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChemicalCrystallizer.class, new RenderChemicalCrystallizer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChemicalDissolutionChamber.class, new RenderChemicalDissolutionChamber());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChemicalInjectionChamber.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCombiner.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrusher.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDigitalMiner.class, new RenderDigitalMiner());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDiversionTransporter.class, new RenderLogisticalTransporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDynamicTank.class, new RenderDynamicTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDynamicValve.class, new RenderDynamicTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnergizedSmelter.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnergyCube.class, new RenderEnergyCube());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnrichmentChamber.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFactory.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFluidTank.class, RenderFluidTank.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFormulaicAssemblicator.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGasTank.class, new RenderGasTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLogisticalTransporter.class, new RenderLogisticalTransporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMechanicalPipe.class, new RenderMechanicalPipe());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMetallurgicInfuser.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityOsmiumCompressor.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPRC.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPersonalChest.class, new RenderPersonalChest());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPrecisionSawmill.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPressurizedTube.class, new RenderPressurizedTube());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPurificationChamber.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityQuantumEntangloporter.class, new RenderQuantumEntangloporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityResistiveHeater.class, new RenderResistiveHeater());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRestrictiveTransporter.class, new RenderLogisticalTransporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySecurityDesk.class, new RenderSecurityDesk());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySeismicVibrator.class, new RenderSeismicVibrator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySolarNeutronActivator.class, new RenderSolarNeutronActivator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTeleporter.class, new RenderTeleporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThermalEvaporationController.class, new RenderThermalEvaporationController());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThermodynamicConductor.class, new RenderThermodynamicConductor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityUniversalCable.class, new RenderUniversalCable());
    }

    @Override
    public void registerItemRenders() {
        //TODO: Figure out how many of these can just be done through json now
        registerItemRender(MekanismItem.ELECTRIC_BOW);
        registerItemRender(MekanismItem.ENERGY_TABLET);
        registerItemRender(MekanismItem.SPEED_UPGRADE);
        registerItemRender(MekanismItem.ENERGY_UPGRADE);
        registerItemRender(MekanismItem.FILTER_UPGRADE);
        registerItemRender(MekanismItem.MUFFLING_UPGRADE);
        registerItemRender(MekanismItem.GAS_UPGRADE);
        registerItemRender(MekanismItem.ANCHOR_UPGRADE);
        registerItemRender(MekanismItem.ROBIT);
        registerItemRender(MekanismItem.ATOMIC_DISASSEMBLER);
        registerItemRender(MekanismItem.ENRICHED_ALLOY);
        registerItemRender(MekanismItem.REINFORCED_ALLOY);
        registerItemRender(MekanismItem.ATOMIC_ALLOY);
        registerItemRender(MekanismItem.ITEM_PROXY);
        registerItemRender(MekanismItem.ENRICHED_IRON);
        registerItemRender(MekanismItem.COMPRESSED_CARBON);
        registerItemRender(MekanismItem.COMPRESSED_REDSTONE);
        registerItemRender(MekanismItem.COMPRESSED_DIAMOND);
        registerItemRender(MekanismItem.COMPRESSED_OBSIDIAN);
        registerItemRender(MekanismItem.PORTABLE_TELEPORTER);
        registerItemRender(MekanismItem.TELEPORTATION_CORE);
        registerItemRender(MekanismItem.CONFIGURATOR);
        registerItemRender(MekanismItem.NETWORK_READER);
        registerItemRender(MekanismItem.JETPACK);
        registerItemRender(MekanismItem.DICTIONARY);
        registerItemRender(MekanismItem.GAS_MASK);
        registerItemRender(MekanismItem.SCUBA_TANK);
        registerItemRender(MekanismItem.ELECTROLYTIC_CORE);
        registerItemRender(MekanismItem.SAWDUST);
        registerItemRender(MekanismItem.SALT);
        registerItemRender(MekanismItem.FREE_RUNNERS);
        registerItemRender(MekanismItem.ARMORED_JETPACK);
        registerItemRender(MekanismItem.CONFIGURATION_CARD);
        registerItemRender(MekanismItem.SEISMIC_READER);
        registerItemRender(MekanismItem.SUBSTRATE);
        registerItemRender(MekanismItem.BIO_FUEL);
        registerItemRender(MekanismItem.FLAMETHROWER);
        registerItemRender(MekanismItem.GAUGE_DROPPER);

        registerItemRender(MekanismItem.BASIC_CONTROL_CIRCUIT);
        registerItemRender(MekanismItem.ADVANCED_CONTROL_CIRCUIT);
        registerItemRender(MekanismItem.ELITE_CONTROL_CIRCUIT);
        registerItemRender(MekanismItem.ULTIMATE_CONTROL_CIRCUIT);

        registerItemRender(MekanismItem.BASIC_TIER_INSTALLER);
        registerItemRender(MekanismItem.ADVANCED_TIER_INSTALLER);
        registerItemRender(MekanismItem.ELITE_TIER_INSTALLER);
        registerItemRender(MekanismItem.ULTIMATE_TIER_INSTALLER);

        registerItemRender(MekanismItem.HDPE_PELLET);
        registerItemRender(MekanismItem.HDPE_ROD);
        registerItemRender(MekanismItem.HDPE_SHEET);
        registerItemRender(MekanismItem.HDPE_STICK);

        registerItemRender(MekanismItem.IRON_CRYSTAL);
        registerItemRender(MekanismItem.GOLD_CRYSTAL);
        registerItemRender(MekanismItem.OSMIUM_CRYSTAL);
        registerItemRender(MekanismItem.COPPER_CRYSTAL);
        registerItemRender(MekanismItem.TIN_CRYSTAL);
        registerItemRender(MekanismItem.SILVER_CRYSTAL);
        registerItemRender(MekanismItem.LEAD_CRYSTAL);

        registerItemRender(MekanismItem.IRON_SHARD);
        registerItemRender(MekanismItem.GOLD_SHARD);
        registerItemRender(MekanismItem.OSMIUM_SHARD);
        registerItemRender(MekanismItem.COPPER_SHARD);
        registerItemRender(MekanismItem.TIN_SHARD);
        registerItemRender(MekanismItem.SILVER_SHARD);
        registerItemRender(MekanismItem.LEAD_SHARD);

        registerItemRender(MekanismItem.IRON_CLUMP);
        registerItemRender(MekanismItem.GOLD_CLUMP);
        registerItemRender(MekanismItem.OSMIUM_CLUMP);
        registerItemRender(MekanismItem.COPPER_CLUMP);
        registerItemRender(MekanismItem.TIN_CLUMP);
        registerItemRender(MekanismItem.SILVER_CLUMP);
        registerItemRender(MekanismItem.LEAD_CLUMP);

        registerItemRender(MekanismItem.DIRTY_IRON_DUST);
        registerItemRender(MekanismItem.DIRTY_GOLD_DUST);
        registerItemRender(MekanismItem.DIRTY_OSMIUM_DUST);
        registerItemRender(MekanismItem.DIRTY_COPPER_DUST);
        registerItemRender(MekanismItem.DIRTY_TIN_DUST);
        registerItemRender(MekanismItem.DIRTY_SILVER_DUST);
        registerItemRender(MekanismItem.DIRTY_LEAD_DUST);

        registerItemRender(MekanismItem.IRON_DUST);
        registerItemRender(MekanismItem.GOLD_DUST);
        registerItemRender(MekanismItem.OSMIUM_DUST);
        registerItemRender(MekanismItem.COPPER_DUST);
        registerItemRender(MekanismItem.TIN_DUST);
        registerItemRender(MekanismItem.SILVER_DUST);
        registerItemRender(MekanismItem.LEAD_DUST);

        registerItemRender(MekanismItem.DIAMOND_DUST);
        registerItemRender(MekanismItem.STEEL_DUST);
        registerItemRender(MekanismItem.SULFUR_DUST);
        registerItemRender(MekanismItem.LITHIUM_DUST);
        registerItemRender(MekanismItem.REFINED_OBSIDIAN_DUST);
        registerItemRender(MekanismItem.OBSIDIAN_DUST);

        registerItemRender(MekanismItem.REFINED_OBSIDIAN_INGOT);
        registerItemRender(MekanismItem.OSMIUM_INGOT);
        registerItemRender(MekanismItem.BRONZE_INGOT);
        registerItemRender(MekanismItem.REFINED_GLOWSTONE_INGOT);
        registerItemRender(MekanismItem.STEEL_INGOT);
        registerItemRender(MekanismItem.COPPER_INGOT);
        registerItemRender(MekanismItem.TIN_INGOT);

        registerItemRender(MekanismItem.REFINED_OBSIDIAN_NUGGET);
        registerItemRender(MekanismItem.OSMIUM_NUGGET);
        registerItemRender(MekanismItem.BRONZE_NUGGET);
        registerItemRender(MekanismItem.REFINED_GLOWSTONE_NUGGET);
        registerItemRender(MekanismItem.STEEL_NUGGET);
        registerItemRender(MekanismItem.COPPER_NUGGET);
        registerItemRender(MekanismItem.TIN_NUGGET);

        setCustomModelResourceLocation(getInventoryMRL("balloon"), MekanismItem.BLACK_BALLOON, MekanismItem.RED_BALLOON, MekanismItem.GREEN_BALLOON,
              MekanismItem.BROWN_BALLOON, MekanismItem.BLUE_BALLOON, MekanismItem.PURPLE_BALLOON, MekanismItem.CYAN_BALLOON, MekanismItem.LIGHT_GRAY_BALLOON,
              MekanismItem.GRAY_BALLOON, MekanismItem.PINK_BALLOON, MekanismItem.LIME_BALLOON, MekanismItem.YELLOW_BALLOON, MekanismItem.LIGHT_BLUE_BALLOON,
              MekanismItem.MAGENTA_BALLOON, MekanismItem.ORANGE_BALLOON, MekanismItem.WHITE_BALLOON);

        ModelBakery.registerItemVariants(MekanismItem.WALKIE_TALKIE.getItem(), ItemWalkieTalkie.OFF_MODEL);

        for (int i = 1; i <= 9; i++) {
            ModelBakery.registerItemVariants(MekanismItem.WALKIE_TALKIE.getItem(), ItemWalkieTalkie.getModel(i));
        }

        ModelBakery.registerItemVariants(MekanismItem.CRAFTING_FORMULA.getItem(), ItemCraftingFormula.MODEL, ItemCraftingFormula.INVALID_MODEL, ItemCraftingFormula.ENCODED_MODEL);

        MekanismItem.JETPACK.getItem().setTileEntityItemStackRenderer(new RenderJetpack());
        MekanismItem.ARMORED_JETPACK.getItem().setTileEntityItemStackRenderer(new RenderArmoredJetpack());
        MekanismItem.GAS_MASK.getItem().setTileEntityItemStackRenderer(new RenderGasMask());
        MekanismItem.SCUBA_TANK.getItem().setTileEntityItemStackRenderer(new RenderScubaTank());
        MekanismItem.FREE_RUNNERS.getItem().setTileEntityItemStackRenderer(new RenderFreeRunners());
        MekanismItem.ATOMIC_DISASSEMBLER.getItem().setTileEntityItemStackRenderer(new RenderAtomicDisassembler());
        MekanismItem.FLAMETHROWER.getItem().setTileEntityItemStackRenderer(new RenderFlameThrower());
        //Energy cubes
        MekanismBlock.BASIC_ENERGY_CUBE.getItem().setTileEntityItemStackRenderer(new RenderEnergyCubeItem());
        MekanismBlock.ADVANCED_ENERGY_CUBE.getItem().setTileEntityItemStackRenderer(new RenderEnergyCubeItem());
        MekanismBlock.ELITE_ENERGY_CUBE.getItem().setTileEntityItemStackRenderer(new RenderEnergyCubeItem());
        MekanismBlock.ULTIMATE_ENERGY_CUBE.getItem().setTileEntityItemStackRenderer(new RenderEnergyCubeItem());
        MekanismBlock.CREATIVE_ENERGY_CUBE.getItem().setTileEntityItemStackRenderer(new RenderEnergyCubeItem());

        //Used to be machine blocks
        MekanismBlock.CHEMICAL_CRYSTALLIZER.getItem().setTileEntityItemStackRenderer(new RenderChemicalCrystallizerItem());
        MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER.getItem().setTileEntityItemStackRenderer(new RenderChemicalDissolutionChamberItem());
        MekanismBlock.DIGITAL_MINER.getItem().setTileEntityItemStackRenderer(new RenderDigitalMinerItem());
        MekanismBlock.PERSONAL_CHEST.getItem().setTileEntityItemStackRenderer(new RenderPersonalChestItem());
        MekanismBlock.QUANTUM_ENTANGLOPORTER.getItem().setTileEntityItemStackRenderer(new RenderQuantumEntangloporterItem());
        MekanismBlock.RESISTIVE_HEATER.getItem().setTileEntityItemStackRenderer(new RenderResistiveHeaterItem());
        MekanismBlock.SEISMIC_VIBRATOR.getItem().setTileEntityItemStackRenderer(new RenderSeismicVibratorItem());
        MekanismBlock.SOLAR_NEUTRON_ACTIVATOR.getItem().setTileEntityItemStackRenderer(new RenderSolarNeutronActivatorItem());
        //Fluid Tank
        MekanismBlock.BASIC_FLUID_TANK.getItem().setTileEntityItemStackRenderer(new RenderFluidTankItem());
        MekanismBlock.ADVANCED_FLUID_TANK.getItem().setTileEntityItemStackRenderer(new RenderFluidTankItem());
        MekanismBlock.ELITE_FLUID_TANK.getItem().setTileEntityItemStackRenderer(new RenderFluidTankItem());
        MekanismBlock.ULTIMATE_FLUID_TANK.getItem().setTileEntityItemStackRenderer(new RenderFluidTankItem());
        MekanismBlock.CREATIVE_FLUID_TANK.getItem().setTileEntityItemStackRenderer(new RenderFluidTankItem());

        //Used to be basic blocks
        MekanismBlock.SECURITY_DESK.getItem().setTileEntityItemStackRenderer(new RenderSecurityDeskItem());

        //Register the item inventory model locations for the various blocks
        for (MekanismBlock mekanismBlock : MekanismBlock.values()) {
            BlockItem item = mekanismBlock.getItem();
            if (item instanceof IItemRedirectedModel) {
                ModelLoader.setCustomModelResourceLocation(item, 0, getInventoryMRL(((IItemRedirectedModel) item).getRedirectLocation()));
            } else {
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        }
        //TODO: Maybe have it be two different items/blocks one for full one for not given metadata is going away?
        ModelLoader.setCustomModelResourceLocation(MekanismBlock.CARDBOARD_BOX.getItem(), 0, new ModelResourceLocation(new ResourceLocation(Mekanism.MODID, "cardboard_box"), "storage=false"));
        ModelLoader.setCustomModelResourceLocation(MekanismBlock.CARDBOARD_BOX.getItem(), 1, new ModelResourceLocation(new ResourceLocation(Mekanism.MODID, "cardboard_box"), "storage=true"));
    }

    private void setCustomStateMapper(IStateMapper mapper, MekanismBlock... blocks) {
        for (MekanismBlock mekanismBlock : blocks) {
            ModelLoader.setCustomStateMapper(mekanismBlock.getBlock(), mapper);
        }
    }

    private void setCustomModelResourceLocation(ModelResourceLocation model, MekanismItem... items) {
        for (MekanismItem mekanismItem : items) {
            ModelLoader.setCustomModelResourceLocation(mekanismItem.getItem(), 0, model);
        }
    }

    @Override
    public void registerBlockRenders() {
        //TODO: Redo all of these. Lots can probably just be done with json now. It is probably a good idea to do the ones, that can be done
        // in json with it, EVEN if it requires more skeleton json files.

        setCustomStateMapper(fenceMapper, MekanismBlock.BLACK_PLASTIC_FENCE, MekanismBlock.RED_PLASTIC_FENCE, MekanismBlock.GREEN_PLASTIC_FENCE,
              MekanismBlock.BROWN_PLASTIC_FENCE, MekanismBlock.BLUE_PLASTIC_FENCE, MekanismBlock.PURPLE_PLASTIC_FENCE, MekanismBlock.CYAN_PLASTIC_FENCE,
              MekanismBlock.LIGHT_GRAY_PLASTIC_FENCE, MekanismBlock.GRAY_PLASTIC_FENCE, MekanismBlock.PINK_PLASTIC_FENCE, MekanismBlock.LIME_PLASTIC_FENCE,
              MekanismBlock.YELLOW_PLASTIC_FENCE, MekanismBlock.LIGHT_BLUE_PLASTIC_FENCE, MekanismBlock.MAGENTA_PLASTIC_FENCE, MekanismBlock.ORANGE_PLASTIC_FENCE,
              MekanismBlock.WHITE_PLASTIC_FENCE);

        setCustomStateMapper(new StateMapperBase() {
            @Nonnull
            @Override
            protected ModelResourceLocation getModelResourceLocation(@Nonnull BlockState state) {
                return new ModelResourceLocation(new ResourceLocation(Mekanism.MODID, "fluid_tank"), "");
            }
        }, MekanismBlock.BASIC_FLUID_TANK, MekanismBlock.ADVANCED_FLUID_TANK, MekanismBlock.ELITE_FLUID_TANK, MekanismBlock.ULTIMATE_FLUID_TANK, MekanismBlock.CREATIVE_FLUID_TANK);

        setCustomStateMapper(new StateMapperBase() {
            @Nonnull
            @Override
            protected ModelResourceLocation getModelResourceLocation(@Nonnull BlockState state) {
                return new ModelResourceLocation(new ResourceLocation(Mekanism.MODID, "energy_cube"), "");
            }
        }, MekanismBlock.BASIC_ENERGY_CUBE, MekanismBlock.ADVANCED_ENERGY_CUBE, MekanismBlock.ELITE_ENERGY_CUBE, MekanismBlock.ULTIMATE_ENERGY_CUBE, MekanismBlock.CREATIVE_ENERGY_CUBE);

        setCustomTransmitterMeshDefinition(MekanismBlock.BASIC_UNIVERSAL_CABLE, MekanismBlock.ADVANCED_UNIVERSAL_CABLE, MekanismBlock.ELITE_UNIVERSAL_CABLE,
              MekanismBlock.ULTIMATE_UNIVERSAL_CABLE, MekanismBlock.BASIC_MECHANICAL_PIPE, MekanismBlock.ADVANCED_MECHANICAL_PIPE, MekanismBlock.ELITE_MECHANICAL_PIPE,
              MekanismBlock.ULTIMATE_MECHANICAL_PIPE, MekanismBlock.BASIC_PRESSURIZED_TUBE, MekanismBlock.ADVANCED_PRESSURIZED_TUBE, MekanismBlock.ELITE_PRESSURIZED_TUBE,
              MekanismBlock.ULTIMATE_PRESSURIZED_TUBE, MekanismBlock.BASIC_LOGISTICAL_TRANSPORTER, MekanismBlock.ADVANCED_LOGISTICAL_TRANSPORTER,
              MekanismBlock.ELITE_LOGISTICAL_TRANSPORTER, MekanismBlock.ULTIMATE_LOGISTICAL_TRANSPORTER, MekanismBlock.RESTRICTIVE_TRANSPORTER,
              MekanismBlock.DIVERSION_TRANSPORTER, MekanismBlock.BASIC_THERMODYNAMIC_CONDUCTOR, MekanismBlock.ADVANCED_THERMODYNAMIC_CONDUCTOR,
              MekanismBlock.ELITE_THERMODYNAMIC_CONDUCTOR, MekanismBlock.ULTIMATE_THERMODYNAMIC_CONDUCTOR);

        //Walkie Talkie dynamic texture
        ModelLoader.setCustomMeshDefinition(MekanismItem.WALKIE_TALKIE.getItem(), stack -> {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemWalkieTalkie) {
                ItemWalkieTalkie item = (ItemWalkieTalkie) stack.getItem();
                if (item.getOn(stack)) {
                    return ItemWalkieTalkie.CHANNEL_MODELS.get(item.getChannel(stack));
                }
            }
            return ItemWalkieTalkie.OFF_MODEL;
        });

        //Crafting Formula dynamic texture
        ModelLoader.setCustomMeshDefinition(MekanismItem.CRAFTING_FORMULA.getItem(), stack -> {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemCraftingFormula) {
                ItemCraftingFormula item = (ItemCraftingFormula) stack.getItem();
                if (item.getInventory(stack) == null) {
                    return ItemCraftingFormula.MODEL;
                }
                return item.isInvalid(stack) ? ItemCraftingFormula.INVALID_MODEL : ItemCraftingFormula.ENCODED_MODEL;
            }
            return ItemCraftingFormula.MODEL;
        });

        OBJLoader.INSTANCE.addDomain(Mekanism.MODID);
    }

    private void setCustomTransmitterMeshDefinition(MekanismBlock... transmitters) {
        for (MekanismBlock transmitter : transmitters) {
            ModelLoader.setCustomMeshDefinition(transmitter.getItem(), stack -> new ModelResourceLocation(transmitter.getBlock().getRegistryName(), "inventory"));
        }
    }

    public void registerItemRender(MekanismItem item) {
        ModelLoader.setCustomModelResourceLocation(item.getItem(), 0, new ModelResourceLocation(item.getItem().getRegistryName(), "inventory"));
    }

    private Screen getClientItemGui(PlayerEntity player, BlockPos pos) {
        int currentItem = pos.getX();
        int handOrdinal = pos.getY();
        if (currentItem < 0 || currentItem >= player.inventory.mainInventory.size() || handOrdinal < 0 || handOrdinal >= Hand.values().length) {
            //If it is out of bounds don't do anything
            return null;
        }
        ItemStack stack = player.inventory.getStackInSlot(currentItem);
        if (stack.isEmpty()) {
            return null;
        }
        Hand hand = Hand.values()[handOrdinal];
        int guiID = pos.getZ();
        switch (guiID) {
            case 0:
                if (stack.getItem() instanceof ItemDictionary) {
                    return new GuiDictionary(player.inventory);
                }
                break;
            case 14:
                if (stack.getItem() instanceof ItemPortableTeleporter) {
                    return new GuiTeleporter(player, hand, stack);
                }
                break;
            case 19:
                if (stack.getItem() instanceof ItemBlockPersonalChest) {
                    //Ensure the item didn't change. From testing even if it did things still seemed to work properly but better safe than sorry
                    return new GuiPersonalChest(player.inventory, new InventoryPersonalChest(stack, hand));
                }
                break;
            case 38:
                if (stack.getItem() instanceof ItemSeismicReader) {
                    return new GuiSeismicReader(player.world, new Coord4D(player), stack.copy());
                }
                break;
        }
        return null;
    }

    private Screen getClientEntityGui(PlayerEntity player, World world, BlockPos pos) {
        int entityID = pos.getX();
        Entity entity = world.getEntityByID(entityID);
        if (entity == null) {
            return null;
        }
        int guiID = pos.getY();
        switch (guiID) {
            case 21:
                if (entity instanceof EntityRobit) {
                    return new GuiRobitMain(player.inventory, (EntityRobit) entity);
                }
                break;
            case 22:
                if (entity instanceof EntityRobit) {
                    return new GuiRobitCrafting(player.inventory, (EntityRobit) entity);
                }
                break;
            case 23:
                if (entity instanceof EntityRobit) {
                    return new GuiRobitInventory(player.inventory, (EntityRobit) entity);
                }
                break;
            case 24:
                if (entity instanceof EntityRobit) {
                    return new GuiRobitSmelting(player.inventory, (EntityRobit) entity);
                }
                break;
            case 25:
                if (entity instanceof EntityRobit) {
                    return new GuiRobitRepair(player.inventory, (EntityRobit) entity);
                }
                break;
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Screen getClientGui(int ID, PlayerEntity player, World world, BlockPos pos) {
        //TODO: Replace magic numbers here and in sub methods with static lookup ints
        if (ID == 0) {
            return getClientItemGui(player, pos);
        } else if (ID == 1) {
            return getClientEntityGui(player, world, pos);
        }
        TileEntity tileEntity = world.getTileEntity(pos);
        switch (ID) {
            //0, 1 USED BEFORE SWITCH
            case 2:
                return new GuiDigitalMiner(player.inventory, (TileEntityDigitalMiner) tileEntity);
            case 3:
                return new GuiEnrichmentChamber(player.inventory, (TileEntityElectricMachine<EnrichmentRecipe>) tileEntity);
            case 4:
                return new GuiOsmiumCompressor(player.inventory, (TileEntityAdvancedElectricMachine<OsmiumCompressorRecipe>) tileEntity);
            case 5:
                return new GuiCombiner(player.inventory, (TileEntityDoubleElectricMachine<CombinerRecipe>) tileEntity);
            case 6:
                return new GuiCrusher(player.inventory, (TileEntityElectricMachine<CrusherRecipe>) tileEntity);
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
            //EMPTY 14
            case 15:
                return new GuiPurificationChamber(player.inventory, (TileEntityAdvancedElectricMachine<PurificationRecipe>) tileEntity);
            case 16:
                return new GuiEnergizedSmelter(player.inventory, (TileEntityElectricMachine<SmeltingRecipe>) tileEntity);
            case 17:
                return new GuiElectricPump(player.inventory, (TileEntityElectricPump) tileEntity);
            case 18:
                return new GuiDynamicTank(player.inventory, (TileEntityDynamicTank) tileEntity);
            case 19:
                return new GuiPersonalChest(player.inventory, (TileEntityPersonalChest) tileEntity);
            //EMPTY 20, 21, 22, 23, 24, 25
            case 29:
                return new GuiChemicalOxidizer(player.inventory, (TileEntityChemicalOxidizer) tileEntity);
            case 30:
                return new GuiChemicalInfuser(player.inventory, (TileEntityChemicalInfuser) tileEntity);
            case 31:
                return new GuiChemicalInjectionChamber(player.inventory, (TileEntityAdvancedElectricMachine<InjectionRecipe>) tileEntity);
            case 32:
                return new GuiElectrolyticSeparator(player.inventory, (TileEntityElectrolyticSeparator) tileEntity);
            case 33:
                return new GuiThermalEvaporationController(player.inventory, (TileEntityThermalEvaporationController) tileEntity);
            case 34:
                return new GuiPrecisionSawmill(player.inventory, (TileEntityPrecisionSawmill) tileEntity);
            case 35:
                return new GuiChemicalDissolutionChamber(player.inventory, (TileEntityChemicalDissolutionChamber) tileEntity);
            case 36:
                return new GuiChemicalWasher(player.inventory, (TileEntityChemicalWasher) tileEntity);
            case 37:
                return new GuiChemicalCrystallizer(player.inventory, (TileEntityChemicalCrystallizer) tileEntity);
            //EMPTY 38
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
            //EMPTY 48
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
            case 59:
                return new GuiLogisticalSorter(player, (TileEntityLogisticalSorter) tileEntity);
        }
        return null;
    }

    @Override
    public void handleTeleporterUpdate(PortableTeleporterMessage message) {
        Screen screen = Minecraft.getInstance().currentScreen;

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
        if (Minecraft.getInstance().world != null) {
            Minecraft.getInstance().effectRenderer.addBlockHitEffects(coord.getPos(), mop);
        }
    }

    private void doSparkle(TileEntity tileEntity, SparkleAnimation anim) {
        PlayerEntitySP player = Minecraft.getInstance().player;
        // If player is within 16 blocks (256 = 16^2), show the status message/sparkles
        if (tileEntity.getPos().distanceSq(player.getPosition()) <= 256) {
            if (MekanismConfig.current().client.enableMultiblockFormationParticles.val()) {
                anim.run();
            } else {
                player.sendStatusMessage(new TextComponentGroup(TextFormatting.BLUE).translation("chat.mek.multiblockformed"), true);
            }
        }
    }

    @Override
    public void doMultiblockSparkle(TileEntity tileEntity, BlockPos renderLoc, int length, int width, int height, INodeChecker checker) {
        doSparkle(tileEntity, new SparkleAnimation(tileEntity, renderLoc, length, width, height, checker));
    }

    @Override
    public void doMultiblockSparkle(TileEntity tileEntity, BlockPos corner1, BlockPos corner2, INodeChecker checker) {
        doSparkle(tileEntity, new SparkleAnimation(tileEntity, corner1, corner2, checker));
    }

    private void registerBlockColorHandler(IBlockColor blockColor, IItemColor itemColor, MekanismBlock... blocks) {
        for (MekanismBlock mekanismBlock : blocks) {
            Minecraft.getInstance().getBlockColors().registerBlockColorHandler(blockColor, mekanismBlock.getBlock());
            Minecraft.getInstance().getItemColors().registerItemColorHandler(itemColor, mekanismBlock.getItem());
        }
    }

    private void registerItemColorHandler(IItemColor itemColor, MekanismItem... items) {
        for (MekanismItem mekanismItem : items) {
            Minecraft.getInstance().getItemColors().registerItemColorHandler(itemColor, mekanismItem.getItem());
        }
    }

    @Override
    public void init() {
        super.init();
        registerBlockColorHandler((state, worldIn, pos, tintIndex) -> {
                  Block block = state.getBlock();
                  if (block instanceof IColoredBlock) {
                      return MekanismRenderer.getColorARGB(((IColoredBlock) block).getColor(), 1);
                  }
                  return -1;
              }, (stack, tintIndex) -> {
                  Item item = stack.getItem();
                  if (item instanceof BlockItem) {
                      //TODO: Fix Glow panel item coloring
                      Block block = ((BlockItem) item).getBlock();
                      if (block instanceof IColoredBlock) {
                          return MekanismRenderer.getColorARGB(((IColoredBlock) block).getColor(), 1);
                      }
                  }
                  return -1;
              },
              //Fluid Tank
              MekanismBlock.BASIC_FLUID_TANK, MekanismBlock.ADVANCED_FLUID_TANK, MekanismBlock.ELITE_FLUID_TANK, MekanismBlock.ULTIMATE_FLUID_TANK, MekanismBlock.CREATIVE_FLUID_TANK,
              //Plastic Blocks
              MekanismBlock.BLACK_PLASTIC_BLOCK, MekanismBlock.RED_PLASTIC_BLOCK, MekanismBlock.GREEN_PLASTIC_BLOCK, MekanismBlock.BROWN_PLASTIC_BLOCK,
              MekanismBlock.BLUE_PLASTIC_BLOCK, MekanismBlock.PURPLE_PLASTIC_BLOCK, MekanismBlock.CYAN_PLASTIC_BLOCK, MekanismBlock.LIGHT_GRAY_PLASTIC_BLOCK,
              MekanismBlock.GRAY_PLASTIC_BLOCK, MekanismBlock.PINK_PLASTIC_BLOCK, MekanismBlock.LIME_PLASTIC_BLOCK, MekanismBlock.YELLOW_PLASTIC_BLOCK,
              MekanismBlock.LIGHT_BLUE_PLASTIC_BLOCK, MekanismBlock.MAGENTA_PLASTIC_BLOCK, MekanismBlock.ORANGE_PLASTIC_BLOCK, MekanismBlock.WHITE_PLASTIC_BLOCK,
              //Slick Plastic Blocks
              MekanismBlock.BLACK_SLICK_PLASTIC_BLOCK, MekanismBlock.RED_SLICK_PLASTIC_BLOCK, MekanismBlock.GREEN_SLICK_PLASTIC_BLOCK,
              MekanismBlock.BROWN_SLICK_PLASTIC_BLOCK, MekanismBlock.BLUE_SLICK_PLASTIC_BLOCK, MekanismBlock.PURPLE_SLICK_PLASTIC_BLOCK, MekanismBlock.CYAN_SLICK_PLASTIC_BLOCK,
              MekanismBlock.LIGHT_GRAY_SLICK_PLASTIC_BLOCK, MekanismBlock.GRAY_SLICK_PLASTIC_BLOCK, MekanismBlock.PINK_SLICK_PLASTIC_BLOCK,
              MekanismBlock.LIME_SLICK_PLASTIC_BLOCK, MekanismBlock.YELLOW_SLICK_PLASTIC_BLOCK, MekanismBlock.LIGHT_BLUE_SLICK_PLASTIC_BLOCK,
              MekanismBlock.MAGENTA_SLICK_PLASTIC_BLOCK, MekanismBlock.ORANGE_SLICK_PLASTIC_BLOCK, MekanismBlock.WHITE_SLICK_PLASTIC_BLOCK,
              //Plastic Glow Blocks
              MekanismBlock.BLACK_PLASTIC_GLOW_BLOCK, MekanismBlock.RED_PLASTIC_GLOW_BLOCK, MekanismBlock.GREEN_PLASTIC_GLOW_BLOCK, MekanismBlock.BROWN_PLASTIC_GLOW_BLOCK,
              MekanismBlock.BLUE_PLASTIC_GLOW_BLOCK, MekanismBlock.PURPLE_PLASTIC_GLOW_BLOCK, MekanismBlock.CYAN_PLASTIC_GLOW_BLOCK, MekanismBlock.LIGHT_GRAY_PLASTIC_GLOW_BLOCK,
              MekanismBlock.GRAY_PLASTIC_GLOW_BLOCK, MekanismBlock.PINK_PLASTIC_GLOW_BLOCK, MekanismBlock.LIME_PLASTIC_GLOW_BLOCK, MekanismBlock.YELLOW_PLASTIC_GLOW_BLOCK,
              MekanismBlock.LIGHT_BLUE_PLASTIC_GLOW_BLOCK, MekanismBlock.MAGENTA_PLASTIC_GLOW_BLOCK, MekanismBlock.ORANGE_PLASTIC_GLOW_BLOCK, MekanismBlock.WHITE_PLASTIC_GLOW_BLOCK,
              //Reinforced Plastic Blocks
              MekanismBlock.BLACK_REINFORCED_PLASTIC_BLOCK, MekanismBlock.RED_REINFORCED_PLASTIC_BLOCK, MekanismBlock.GREEN_REINFORCED_PLASTIC_BLOCK,
              MekanismBlock.BROWN_REINFORCED_PLASTIC_BLOCK, MekanismBlock.BLUE_REINFORCED_PLASTIC_BLOCK, MekanismBlock.PURPLE_REINFORCED_PLASTIC_BLOCK,
              MekanismBlock.CYAN_REINFORCED_PLASTIC_BLOCK, MekanismBlock.LIGHT_GRAY_REINFORCED_PLASTIC_BLOCK, MekanismBlock.GRAY_REINFORCED_PLASTIC_BLOCK,
              MekanismBlock.PINK_REINFORCED_PLASTIC_BLOCK, MekanismBlock.LIME_REINFORCED_PLASTIC_BLOCK, MekanismBlock.YELLOW_REINFORCED_PLASTIC_BLOCK,
              MekanismBlock.LIGHT_BLUE_REINFORCED_PLASTIC_BLOCK, MekanismBlock.MAGENTA_REINFORCED_PLASTIC_BLOCK, MekanismBlock.ORANGE_REINFORCED_PLASTIC_BLOCK,
              MekanismBlock.WHITE_REINFORCED_PLASTIC_BLOCK,
              //Plastic Road
              MekanismBlock.BLACK_PLASTIC_ROAD, MekanismBlock.RED_PLASTIC_ROAD, MekanismBlock.GREEN_PLASTIC_ROAD, MekanismBlock.BROWN_PLASTIC_ROAD,
              MekanismBlock.BLUE_PLASTIC_ROAD, MekanismBlock.PURPLE_PLASTIC_ROAD, MekanismBlock.CYAN_PLASTIC_ROAD, MekanismBlock.LIGHT_GRAY_PLASTIC_ROAD,
              MekanismBlock.GRAY_PLASTIC_ROAD, MekanismBlock.PINK_PLASTIC_ROAD, MekanismBlock.LIME_PLASTIC_ROAD, MekanismBlock.YELLOW_PLASTIC_ROAD,
              MekanismBlock.LIGHT_BLUE_PLASTIC_ROAD, MekanismBlock.MAGENTA_PLASTIC_ROAD, MekanismBlock.ORANGE_PLASTIC_ROAD, MekanismBlock.WHITE_PLASTIC_ROAD,
              //Plastic Fences
              MekanismBlock.BLACK_PLASTIC_FENCE, MekanismBlock.RED_PLASTIC_FENCE, MekanismBlock.GREEN_PLASTIC_FENCE, MekanismBlock.BROWN_PLASTIC_FENCE,
              MekanismBlock.BLUE_PLASTIC_FENCE, MekanismBlock.PURPLE_PLASTIC_FENCE, MekanismBlock.CYAN_PLASTIC_FENCE, MekanismBlock.LIGHT_GRAY_PLASTIC_FENCE,
              MekanismBlock.GRAY_PLASTIC_FENCE, MekanismBlock.PINK_PLASTIC_FENCE, MekanismBlock.LIME_PLASTIC_FENCE, MekanismBlock.YELLOW_PLASTIC_FENCE,
              MekanismBlock.LIGHT_BLUE_PLASTIC_FENCE, MekanismBlock.MAGENTA_PLASTIC_FENCE, MekanismBlock.ORANGE_PLASTIC_FENCE, MekanismBlock.WHITE_PLASTIC_FENCE,
              //Glow Panels
              MekanismBlock.BLACK_GLOW_PANEL, MekanismBlock.RED_GLOW_PANEL, MekanismBlock.GREEN_GLOW_PANEL, MekanismBlock.BROWN_GLOW_PANEL,
              MekanismBlock.BLUE_GLOW_PANEL, MekanismBlock.PURPLE_GLOW_PANEL, MekanismBlock.CYAN_GLOW_PANEL, MekanismBlock.LIGHT_GRAY_GLOW_PANEL,
              MekanismBlock.GRAY_GLOW_PANEL, MekanismBlock.PINK_GLOW_PANEL, MekanismBlock.LIME_GLOW_PANEL, MekanismBlock.YELLOW_GLOW_PANEL,
              MekanismBlock.LIGHT_BLUE_GLOW_PANEL, MekanismBlock.MAGENTA_GLOW_PANEL, MekanismBlock.ORANGE_GLOW_PANEL, MekanismBlock.WHITE_GLOW_PANEL);
        registerItemColorHandler((stack, tintIndex) -> {
                  Item item = stack.getItem();
                  if (item instanceof ItemBalloon) {
                      ItemBalloon balloon = (ItemBalloon) item;
                      return MekanismRenderer.getColorARGB(balloon.getColor(), 1);
                  }
                  return -1;
              }, MekanismItem.BLACK_BALLOON, MekanismItem.RED_BALLOON, MekanismItem.GREEN_BALLOON, MekanismItem.BROWN_BALLOON, MekanismItem.BLUE_BALLOON,
              MekanismItem.PURPLE_BALLOON, MekanismItem.CYAN_BALLOON, MekanismItem.LIGHT_GRAY_BALLOON, MekanismItem.GRAY_BALLOON, MekanismItem.PINK_BALLOON,
              MekanismItem.LIME_BALLOON, MekanismItem.YELLOW_BALLOON, MekanismItem.LIGHT_BLUE_BALLOON, MekanismItem.MAGENTA_BALLOON, MekanismItem.ORANGE_BALLOON,
              MekanismItem.WHITE_BALLOON);

        MinecraftForge.EVENT_BUS.register(new ClientConnectionHandler());
        MinecraftForge.EVENT_BUS.register(new ClientPlayerTracker());
        MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
        MinecraftForge.EVENT_BUS.register(new RenderTickHandler());
        MinecraftForge.EVENT_BUS.register(SoundHandler.class);

        new MekanismKeyHandler();

        HolidayManager.init();
    }

    @Override
    public void onConfigSync(boolean fromPacket) {
        super.onConfigSync(fromPacket);
        if (fromPacket && MekanismConfig.current().general.voiceServerEnabled.val() && MekanismClient.voiceClient != null) {
            MekanismClient.voiceClient.start();
        }
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        Registry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        registerItemStackModel(modelRegistry, "jetpack", model -> RenderJetpack.model = model);
        registerItemStackModel(modelRegistry, "jetpack_armored", model -> RenderArmoredJetpack.model = model);
        registerItemStackModel(modelRegistry, "gas_mask", model -> RenderGasMask.model = model);
        registerItemStackModel(modelRegistry, "scuba_tank", model -> RenderScubaTank.model = model);
        registerItemStackModel(modelRegistry, "free_runners", model -> RenderFreeRunners.model = model);
        registerItemStackModel(modelRegistry, "atomic_disassembler", model -> RenderAtomicDisassembler.model = model);
        registerItemStackModel(modelRegistry, "flamethrower", model -> RenderFlameThrower.model = model);
        registerItemStackModel(modelRegistry, "digital_miner", model -> RenderDigitalMinerItem.model = model);
        registerItemStackModel(modelRegistry, "solar_neutron_activator", model -> RenderSolarNeutronActivatorItem.model = model);
        registerItemStackModel(modelRegistry, "chemical_dissolution_chamber", model -> RenderChemicalDissolutionChamberItem.model = model);
        registerItemStackModel(modelRegistry, "chemical_crystallizer", model -> RenderChemicalCrystallizerItem.model = model);
        registerItemStackModel(modelRegistry, "seismic_vibrator", model -> RenderSeismicVibratorItem.model = model);
        registerItemStackModel(modelRegistry, "quantum_entangloporter", model -> RenderQuantumEntangloporterItem.model = model);
        registerItemStackModel(modelRegistry, "resistive_heater", model -> RenderResistiveHeaterItem.model = model);
        registerItemStackModel(modelRegistry, "personal_chest", model -> RenderPersonalChestItem.model = model);
        registerItemStackModel(modelRegistry, "security_desk", model -> RenderSecurityDeskItem.model = model);

        //TODO: Does tier matter
        registerItemStackModel(modelRegistry, "energy_cube", model -> RenderEnergyCubeItem.model = model);
        registerItemStackModel(modelRegistry, "fluid_tank", model -> RenderFluidTankItem.model = model);
    }

    private ModelResourceLocation getInventoryMRL(String type) {
        return new ModelResourceLocation(new ResourceLocation(Mekanism.MODID, type), "inventory");
    }

    private void registerItemStackModel(Registry<ModelResourceLocation, IBakedModel> modelRegistry, String type, Function<ItemLayerWrapper, IBakedModel> setModel) {
        ModelResourceLocation resourceLocation = getInventoryMRL(type);
        modelRegistry.putObject(resourceLocation, setModel.apply(new ItemLayerWrapper(modelRegistry.getObject(resourceLocation))));
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
        RenderingRegistry.registerEntityRenderingHandler(EntityBabySkeleton.class, SkeletonRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFlame.class, RenderFlame::new);
    }

    @Override
    public double getReach(PlayerEntity player) {
        return Minecraft.getInstance().playerController.getBlockReachDistance();
    }

    @Override
    public boolean isPaused() {
        if (FMLClientHandler.instance().getClient().isSingleplayer() && !FMLClientHandler.instance().getClient().getIntegratedServer().getPublic()) {
            Screen screen = FMLClientHandler.instance().getClient().currentScreen;
            return screen != null && screen.doesGuiPauseGame();
        }
        return false;
    }

    @Override
    public File getMinecraftDir() {
        return Minecraft.getInstance().gameDir;
    }

    @Override
    public PlayerEntity getPlayer(MessageContext context) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            return context.getServerHandler().player;
        }
        return Minecraft.getInstance().player;
    }

    @Override
    public void handlePacket(Runnable runnable, PlayerEntity player) {
        if (player == null || player.world.isRemote) {
            Minecraft.getInstance().addScheduledTask(runnable);
        } else {
            //Single player
            if (player.world instanceof ServerWorld) {
                ((ServerWorld) player.world).addScheduledTask(runnable);
            } else {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if (server != null) {
                    server.addScheduledTask(runnable);
                } else {
                    Mekanism.logger.error("Packet handler wanted to set a scheduled task, but we couldn't find a way to set one.");
                    Mekanism.logger.error("Player = {}, World = {}", player, player.world);
                }
            }
        }
    }

    @Override
    public void renderLaser(World world, Pos3D from, Pos3D to, Direction direction, double energy) {
        Minecraft.getInstance().effectRenderer.addEffect(new ParticleLaser(world, from, to, direction, energy));
    }

    @Override
    public FontRenderer getFontRenderer() {
        return Minecraft.getInstance().fontRenderer;
    }

    @Override
    public void throwApiPresentException() {
        throw new ApiJarPresentException(API_PRESENT_MESSAGE);
    }
}