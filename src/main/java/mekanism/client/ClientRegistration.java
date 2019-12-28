package mekanism.client;

import java.util.Map;
import java.util.function.Function;
import mekanism.api.block.IColoredBlock;
import mekanism.client.gui.GuiBoilerStats;
import mekanism.client.gui.GuiCombiner;
import mekanism.client.gui.GuiCrusher;
import mekanism.client.gui.GuiDictionary;
import mekanism.client.gui.GuiDigitalMiner;
import mekanism.client.gui.GuiDigitalMinerConfig;
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
import mekanism.client.gui.GuiPersonalChestItem;
import mekanism.client.gui.GuiPersonalChestTile;
import mekanism.client.gui.GuiPortableTeleporter;
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
import mekanism.client.gui.filter.GuiMFilterSelect;
import mekanism.client.gui.filter.GuiMItemStackFilter;
import mekanism.client.gui.filter.GuiMMaterialFilter;
import mekanism.client.gui.filter.GuiMModIDFilter;
import mekanism.client.gui.filter.GuiMOreDictFilter;
import mekanism.client.gui.filter.GuiOredictionificatorFilter;
import mekanism.client.gui.filter.GuiTFilterSelect;
import mekanism.client.gui.filter.GuiTItemStackFilter;
import mekanism.client.gui.filter.GuiTMaterialFilter;
import mekanism.client.gui.filter.GuiTModIDFilter;
import mekanism.client.gui.filter.GuiTOreDictFilter;
import mekanism.client.gui.robit.GuiRobitCrafting;
import mekanism.client.gui.robit.GuiRobitInventory;
import mekanism.client.gui.robit.GuiRobitMain;
import mekanism.client.gui.robit.GuiRobitRepair;
import mekanism.client.gui.robit.GuiRobitSmelting;
import mekanism.client.particle.JetpackFlameParticle;
import mekanism.client.particle.JetpackSmokeParticle;
import mekanism.client.particle.LaserParticle;
import mekanism.client.particle.ScubaBubbleParticle;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.entity.RenderFlame;
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
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.registries.MekanismTileEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistration {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        //Register entity rendering handlers
        ClientRegistrationUtil.registerEntityRenderingHandler(MekanismEntityTypes.ROBIT, RenderRobit::new);
        ClientRegistrationUtil.registerEntityRenderingHandler(MekanismEntityTypes.FLAME, RenderFlame::new);

        //TODO: Evaluate extending TileEntityRendererAnimation instead of TileEntityRenderer
        //Register TileEntityRenderers
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BOILER_CASING, new RenderThermoelectricBoiler());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BOILER_VALVE, new RenderThermoelectricBoiler());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.CHEMICAL_CRYSTALLIZER, new RenderChemicalCrystallizer());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.CHEMICAL_DISSOLUTION_CHAMBER, new RenderChemicalDissolutionChamber());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.CHEMICAL_INJECTION_CHAMBER, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.COMBINER, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.CRUSHER, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.DIGITAL_MINER, new RenderDigitalMiner());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.DYNAMIC_TANK, new RenderDynamicTank());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.DYNAMIC_VALVE, new RenderDynamicTank());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ENERGIZED_SMELTER, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ENRICHMENT_CHAMBER, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.FORMULAIC_ASSEMBLICATOR, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.METALLURGIC_INFUSER, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.OSMIUM_COMPRESSOR, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.PRESSURIZED_REACTION_CHAMBER, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.PERSONAL_CHEST, new RenderPersonalChest());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.PRECISION_SAWMILL, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.PURIFICATION_CHAMBER, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.QUANTUM_ENTANGLOPORTER, new RenderQuantumEntangloporter());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.RESISTIVE_HEATER, new RenderResistiveHeater());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.SECURITY_DESK, new RenderSecurityDesk());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.SEISMIC_VIBRATOR, new RenderSeismicVibrator());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.SOLAR_NEUTRON_ACTIVATOR, new RenderSolarNeutronActivator());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.TELEPORTER, new RenderTeleporter());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.THERMAL_EVAPORATION_CONTROLLER, new RenderThermalEvaporationController());
        //Bins
        RenderBin binRenderer = new RenderBin();
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_BIN, binRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_BIN, binRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_BIN, binRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_BIN, binRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.CREATIVE_BIN, binRenderer);
        //Gas tanks
        RenderGasTank gasTankRenderer = new RenderGasTank();
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_GAS_TANK, gasTankRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_GAS_TANK, gasTankRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_GAS_TANK, gasTankRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_GAS_TANK, gasTankRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.CREATIVE_GAS_TANK, gasTankRenderer);
        //Energy Cubes
        RenderEnergyCube energyCubeRenderer = new RenderEnergyCube();
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_ENERGY_CUBE, energyCubeRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_ENERGY_CUBE, energyCubeRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_ENERGY_CUBE, energyCubeRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_ENERGY_CUBE, energyCubeRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.CREATIVE_ENERGY_CUBE, energyCubeRenderer);
        //Fluid Tanks
        RenderFluidTank fluidTankRenderer = new RenderFluidTank();
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_FLUID_TANK, fluidTankRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_FLUID_TANK, fluidTankRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_FLUID_TANK, fluidTankRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_FLUID_TANK, fluidTankRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.CREATIVE_FLUID_TANK, fluidTankRenderer);
        //Factories
        //Combining
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_COMBINING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_COMBINING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_COMBINING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_COMBINING_FACTORY, new RenderConfigurableMachine<>());
        //Compressing
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_COMPRESSING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_COMPRESSING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_COMPRESSING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_COMPRESSING_FACTORY, new RenderConfigurableMachine<>());
        //Crushing
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_CRUSHING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_CRUSHING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_CRUSHING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_CRUSHING_FACTORY, new RenderConfigurableMachine<>());
        //Enriching
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_ENRICHING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_ENRICHING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_ENRICHING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_ENRICHING_FACTORY, new RenderConfigurableMachine<>());
        //Infusing
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_INFUSING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_INFUSING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_INFUSING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_INFUSING_FACTORY, new RenderConfigurableMachine<>());
        //Injecting
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_INJECTING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_INJECTING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_INJECTING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_INJECTING_FACTORY, new RenderConfigurableMachine<>());
        //Purifying
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_PURIFYING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_PURIFYING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_PURIFYING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_PURIFYING_FACTORY, new RenderConfigurableMachine<>());
        //Sawing
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_SAWING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_SAWING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_SAWING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_SAWING_FACTORY, new RenderConfigurableMachine<>());
        //Smelting
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_SMELTING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_SMELTING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_SMELTING_FACTORY, new RenderConfigurableMachine<>());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_SMELTING_FACTORY, new RenderConfigurableMachine<>());
        //Transmitters
        //Logistical transporters
        RenderLogisticalTransporter logisticalTransporterRenderer = new RenderLogisticalTransporter();
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.RESTRICTIVE_TRANSPORTER, logisticalTransporterRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.DIVERSION_TRANSPORTER, logisticalTransporterRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_LOGISTICAL_TRANSPORTER, logisticalTransporterRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_LOGISTICAL_TRANSPORTER, logisticalTransporterRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_LOGISTICAL_TRANSPORTER, logisticalTransporterRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_LOGISTICAL_TRANSPORTER, logisticalTransporterRenderer);
        //Mechanical Pipes
        RenderMechanicalPipe mechanicalPipeRenderer = new RenderMechanicalPipe();
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_MECHANICAL_PIPE, mechanicalPipeRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_MECHANICAL_PIPE, mechanicalPipeRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_MECHANICAL_PIPE, mechanicalPipeRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_MECHANICAL_PIPE, mechanicalPipeRenderer);
        //Pressurized Tubes
        RenderPressurizedTube pressurizedTubeRenderer = new RenderPressurizedTube();
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_PRESSURIZED_TUBE, pressurizedTubeRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_PRESSURIZED_TUBE, pressurizedTubeRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_PRESSURIZED_TUBE, pressurizedTubeRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_PRESSURIZED_TUBE, pressurizedTubeRenderer);
        //Universal Cables
        RenderUniversalCable universalCableRenderer = new RenderUniversalCable();
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_UNIVERSAL_CABLE, universalCableRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_UNIVERSAL_CABLE, universalCableRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_UNIVERSAL_CABLE, universalCableRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_UNIVERSAL_CABLE, universalCableRenderer);
        //Thermodynamic Conductors
        RenderThermodynamicConductor thermodynamicConductorRenderer = new RenderThermodynamicConductor();
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_THERMODYNAMIC_CONDUCTOR, thermodynamicConductorRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_THERMODYNAMIC_CONDUCTOR, thermodynamicConductorRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_THERMODYNAMIC_CONDUCTOR, thermodynamicConductorRenderer);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_THERMODYNAMIC_CONDUCTOR, thermodynamicConductorRenderer);

        //TODO: 1.15 Double check render layer stuff given by the time we are ready for the first 1.15 alpha we should know the states better of the remaining things
        //Block render layers
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.STRUCTURAL_GLASS, RenderType.func_228643_e_());
        //TODO: Verify these at some point
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.LASER_AMPLIFIER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.LASER_TRACTOR_BEAM, RenderType.func_228643_e_());
        //Fluid Tanks
        //TODO: Remove??
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.BASIC_FLUID_TANK, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ADVANCED_FLUID_TANK, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ELITE_FLUID_TANK, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ULTIMATE_FLUID_TANK, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.CREATIVE_FLUID_TANK, RenderType.func_228643_e_());
        //Transmitters
        //TODO: Is this even the proper way to convert the canRenderInLayer the transmitters used to use
        //TODO: The transmitters can probably be removed from here given setRenderLayer is only needed for blocks and not TERs
        // though I am leaving it for now in case we decide to make transmitters use both
        //Logistical transporters
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.RESTRICTIVE_TRANSPORTER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.DIVERSION_TRANSPORTER, renderType -> renderType.equals(RenderType.func_228643_e_()) || renderType.equals(RenderType.func_228645_f_()));
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER, renderType -> renderType.equals(RenderType.func_228643_e_()) || renderType.equals(RenderType.func_228645_f_()));
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER, renderType -> renderType.equals(RenderType.func_228643_e_()) || renderType.equals(RenderType.func_228645_f_()));
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER, renderType -> renderType.equals(RenderType.func_228643_e_()) || renderType.equals(RenderType.func_228645_f_()));
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER, renderType -> renderType.equals(RenderType.func_228643_e_()) || renderType.equals(RenderType.func_228645_f_()));
        //Mechanical Pipes
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.BASIC_MECHANICAL_PIPE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ADVANCED_MECHANICAL_PIPE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ELITE_MECHANICAL_PIPE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ULTIMATE_MECHANICAL_PIPE, RenderType.func_228643_e_());
        //Pressurized Tubes
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.BASIC_PRESSURIZED_TUBE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ADVANCED_PRESSURIZED_TUBE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ELITE_PRESSURIZED_TUBE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE, RenderType.func_228643_e_());
        //Universal Cables
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.BASIC_UNIVERSAL_CABLE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ADVANCED_UNIVERSAL_CABLE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ELITE_UNIVERSAL_CABLE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE, RenderType.func_228643_e_());
        //Thermodynamic Conductors
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR, RenderType.func_228643_e_());
        //Fluids
        ClientRegistrationUtil.setRenderLayer(MekanismFluids.HYDROGEN, RenderType.func_228645_f_());
        ClientRegistrationUtil.setRenderLayer(MekanismFluids.OXYGEN, RenderType.func_228645_f_());
        ClientRegistrationUtil.setRenderLayer(MekanismFluids.CHLORINE, RenderType.func_228645_f_());
        ClientRegistrationUtil.setRenderLayer(MekanismFluids.SULFUR_DIOXIDE, RenderType.func_228645_f_());
        ClientRegistrationUtil.setRenderLayer(MekanismFluids.SULFUR_TRIOXIDE, RenderType.func_228645_f_());
        ClientRegistrationUtil.setRenderLayer(MekanismFluids.SULFURIC_ACID, RenderType.func_228645_f_());
        ClientRegistrationUtil.setRenderLayer(MekanismFluids.HYDROGEN_CHLORIDE, RenderType.func_228645_f_());
        ClientRegistrationUtil.setRenderLayer(MekanismFluids.ETHENE, RenderType.func_228645_f_());
        ClientRegistrationUtil.setRenderLayer(MekanismFluids.SODIUM, RenderType.func_228645_f_());
        ClientRegistrationUtil.setRenderLayer(MekanismFluids.BRINE, RenderType.func_228645_f_());
        ClientRegistrationUtil.setRenderLayer(MekanismFluids.DEUTERIUM, RenderType.func_228645_f_());
        ClientRegistrationUtil.setRenderLayer(MekanismFluids.TRITIUM, RenderType.func_228645_f_());
        ClientRegistrationUtil.setRenderLayer(MekanismFluids.FUSION_FUEL, RenderType.func_228645_f_());
        ClientRegistrationUtil.setRenderLayer(MekanismFluids.LITHIUM, RenderType.func_228645_f_());
        ClientRegistrationUtil.setRenderLayer(MekanismFluids.STEAM, RenderType.func_228645_f_());
        ClientRegistrationUtil.setRenderLayer(MekanismFluids.HEAVY_WATER, RenderType.func_228645_f_());
    }

    @SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DICTIONARY, GuiDictionary::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PORTABLE_TELEPORTER, GuiPortableTeleporter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SEISMIC_READER, GuiSeismicReader::new);

        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.MAIN_ROBIT, GuiRobitMain::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.INVENTORY_ROBIT, GuiRobitInventory::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SMELTING_ROBIT, GuiRobitSmelting::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CRAFTING_ROBIT, GuiRobitCrafting::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.REPAIR_ROBIT, GuiRobitRepair::new);

        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_CRYSTALLIZER, GuiChemicalCrystallizer::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_DISSOLUTION_CHAMBER, GuiChemicalDissolutionChamber::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_INFUSER, GuiChemicalInfuser::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_INJECTION_CHAMBER, GuiChemicalInjectionChamber::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_OXIDIZER, GuiChemicalOxidizer::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_WASHER, GuiChemicalWasher::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.COMBINER, GuiCombiner::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CRUSHER, GuiCrusher::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DIGITAL_MINER, GuiDigitalMiner::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DYNAMIC_TANK, GuiDynamicTank::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ELECTRIC_PUMP, GuiElectricPump::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ELECTROLYTIC_SEPARATOR, GuiElectrolyticSeparator::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ENERGIZED_SMELTER, GuiEnergizedSmelter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ENRICHMENT_CHAMBER, GuiEnrichmentChamber::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.FLUIDIC_PLENISHER, GuiFluidicPlenisher::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.FORMULAIC_ASSEMBLICATOR, GuiFormulaicAssemblicator::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.FUELWOOD_HEATER, GuiFuelwoodHeater::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.LASER_AMPLIFIER, GuiLaserAmplifier::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.LASER_TRACTOR_BEAM, GuiLaserTractorBeam::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.METALLURGIC_INFUSER, GuiMetallurgicInfuser::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.OREDICTIONIFICATOR, GuiOredictionificator::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.OSMIUM_COMPRESSOR, GuiOsmiumCompressor::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PRECISION_SAWMILL, GuiPrecisionSawmill::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PRESSURIZED_REACTION_CHAMBER, GuiPRC::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PURIFICATION_CHAMBER, GuiPurificationChamber::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QUANTUM_ENTANGLOPORTER, GuiQuantumEntangloporter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.RESISTIVE_HEATER, GuiResistiveHeater::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ROTARY_CONDENSENTRATOR, GuiRotaryCondensentrator::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SECURITY_DESK, GuiSecurityDesk::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SEISMIC_VIBRATOR, GuiSeismicVibrator::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SOLAR_NEUTRON_ACTIVATOR, GuiSolarNeutronActivator::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.TELEPORTER, GuiTeleporter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.THERMAL_EVAPORATION_CONTROLLER, GuiThermalEvaporationController::new);

        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.FACTORY, GuiFactory::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.GAS_TANK, GuiGasTank::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.FLUID_TANK, GuiFluidTank::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ENERGY_CUBE, GuiEnergyCube::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.INDUCTION_MATRIX, GuiInductionMatrix::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.THERMOELECTRIC_BOILER, GuiThermoelectricBoiler::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PERSONAL_CHEST_ITEM, GuiPersonalChestItem::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PERSONAL_CHEST_BLOCK, GuiPersonalChestTile::new);

        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DIGITAL_MINER_CONFIG, GuiDigitalMinerConfig::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.LOGISTICAL_SORTER, GuiLogisticalSorter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DM_FILTER_SELECT, GuiMFilterSelect::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.LS_FILTER_SELECT, GuiTFilterSelect::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DM_TAG_FILTER, GuiMOreDictFilter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.LS_TAG_FILTER, GuiTOreDictFilter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DM_MOD_ID_FILTER, GuiMModIDFilter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.LS_MOD_ID_FILTER, GuiTModIDFilter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DM_MATERIAL_FILTER, GuiMMaterialFilter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.LS_MATERIAL_FILTER, GuiTMaterialFilter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DM_ITEMSTACK_FILTER, GuiMItemStackFilter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.LS_ITEMSTACK_FILTER, GuiTItemStackFilter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.OREDICTIONIFICATOR_FILTER, GuiOredictionificatorFilter::new);

        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.UPGRADE_MANAGEMENT, GuiUpgradeManagement::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SIDE_CONFIGURATION, GuiSideConfiguration::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.TRANSPORTER_CONFIGURATION, GuiTransporterConfig::new);

        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.BOILER_STATS, GuiBoilerStats::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.MATRIX_STATS, GuiMatrixStats::new);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
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

        registerItemStackModel(modelRegistry, "basic_energy_cube", model -> RenderEnergyCubeItem.model = model);
        registerItemStackModel(modelRegistry, "advanced_energy_cube", model -> RenderEnergyCubeItem.model = model);
        registerItemStackModel(modelRegistry, "elite_energy_cube", model -> RenderEnergyCubeItem.model = model);
        registerItemStackModel(modelRegistry, "ultimate_energy_cube", model -> RenderEnergyCubeItem.model = model);
        registerItemStackModel(modelRegistry, "creative_energy_cube", model -> RenderEnergyCubeItem.model = model);

        registerItemStackModel(modelRegistry, "basic_fluid_tank", model -> RenderFluidTankItem.model = model);
        registerItemStackModel(modelRegistry, "advanced_fluid_tank", model -> RenderFluidTankItem.model = model);
        registerItemStackModel(modelRegistry, "elite_fluid_tank", model -> RenderFluidTankItem.model = model);
        registerItemStackModel(modelRegistry, "ultimate_fluid_tank", model -> RenderFluidTankItem.model = model);
        registerItemStackModel(modelRegistry, "creative_fluid_tank", model -> RenderFluidTankItem.model = model);
    }

    private static void registerItemStackModel(Map<ResourceLocation, IBakedModel> modelRegistry, String type, Function<ItemLayerWrapper, IBakedModel> setModel) {
        ModelResourceLocation resourceLocation = ClientRegistrationUtil.getInventoryMRL(Mekanism::rl, type);
        modelRegistry.put(resourceLocation, setModel.apply(new ItemLayerWrapper(modelRegistry.get(resourceLocation))));
    }

    @SubscribeEvent
    public static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particles.registerFactory(MekanismParticleTypes.LASER.getParticleType(), LaserParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(MekanismParticleTypes.JETPACK_FLAME.getParticleType(), JetpackFlameParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(MekanismParticleTypes.JETPACK_SMOKE.getParticleType(), JetpackSmokeParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(MekanismParticleTypes.SCUBA_BUBBLE.getParticleType(), ScubaBubbleParticle.Factory::new);
    }

    @SubscribeEvent
    public static void registerItemColorHandlers(ColorHandlerEvent.Item event) {
        ClientRegistrationUtil.registerBlockColorHandler(event.getBlockColors(), event.getItemColors(), (state, worldIn, pos, tintIndex) -> {
                  Block block = state.getBlock();
                  if (block instanceof IColoredBlock) {
                      return MekanismRenderer.getColorARGB(((IColoredBlock) block).getColor(), 1);
                  }
                  return -1;
              }, (stack, tintIndex) -> {
                  Item item = stack.getItem();
                  if (item instanceof BlockItem) {
                      Block block = ((BlockItem) item).getBlock();
                      if (block instanceof IColoredBlock) {
                          return MekanismRenderer.getColorARGB(((IColoredBlock) block).getColor(), 1);
                      }
                  }
                  return -1;
              },
              //Fluid Tank
              MekanismBlocks.BASIC_FLUID_TANK, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismBlocks.ELITE_FLUID_TANK, MekanismBlocks.ULTIMATE_FLUID_TANK,
              MekanismBlocks.CREATIVE_FLUID_TANK);
    }
}