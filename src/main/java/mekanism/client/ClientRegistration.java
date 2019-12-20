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
import mekanism.client.particle.ParticleLaser;
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
import mekanism.common.MekanismBlock;
import mekanism.common.MekanismFluids;
import mekanism.common.entity.MekanismEntityTypes;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.particle.MekanismParticleType;
import mekanism.common.tile.base.MekanismTileEntityTypes;
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
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_BIN, new RenderBin());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_BIN, new RenderBin());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_BIN, new RenderBin());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_BIN, new RenderBin());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_BIN, new RenderBin());
        //Gas tanks
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_GAS_TANK, new RenderGasTank());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_GAS_TANK, new RenderGasTank());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_GAS_TANK, new RenderGasTank());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_GAS_TANK, new RenderGasTank());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.CREATIVE_GAS_TANK, new RenderGasTank());
        //Energy Cubes
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_ENERGY_CUBE, new RenderEnergyCube());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_ENERGY_CUBE, new RenderEnergyCube());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_ENERGY_CUBE, new RenderEnergyCube());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_ENERGY_CUBE, new RenderEnergyCube());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.CREATIVE_ENERGY_CUBE, new RenderEnergyCube());
        //Fluid Tanks
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_FLUID_TANK, RenderFluidTank.INSTANCE);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_FLUID_TANK, RenderFluidTank.INSTANCE);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_FLUID_TANK, RenderFluidTank.INSTANCE);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_FLUID_TANK, RenderFluidTank.INSTANCE);
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.CREATIVE_FLUID_TANK, RenderFluidTank.INSTANCE);
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
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.RESTRICTIVE_TRANSPORTER, new RenderLogisticalTransporter());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.DIVERSION_TRANSPORTER, new RenderLogisticalTransporter());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_LOGISTICAL_TRANSPORTER, new RenderLogisticalTransporter());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_LOGISTICAL_TRANSPORTER, new RenderLogisticalTransporter());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_LOGISTICAL_TRANSPORTER, new RenderLogisticalTransporter());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_LOGISTICAL_TRANSPORTER, new RenderLogisticalTransporter());
        //Mechanical Pipes
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_MECHANICAL_PIPE, new RenderMechanicalPipe());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_MECHANICAL_PIPE, new RenderMechanicalPipe());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_MECHANICAL_PIPE, new RenderMechanicalPipe());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_MECHANICAL_PIPE, new RenderMechanicalPipe());
        //Pressurized Tubes
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_PRESSURIZED_TUBE, new RenderPressurizedTube());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_PRESSURIZED_TUBE, new RenderPressurizedTube());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_PRESSURIZED_TUBE, new RenderPressurizedTube());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_PRESSURIZED_TUBE, new RenderPressurizedTube());
        //Universal Cables
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_UNIVERSAL_CABLE, new RenderUniversalCable());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_UNIVERSAL_CABLE, new RenderUniversalCable());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_UNIVERSAL_CABLE, new RenderUniversalCable());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_UNIVERSAL_CABLE, new RenderUniversalCable());
        //Thermodynamic Conductors
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_THERMODYNAMIC_CONDUCTOR, new RenderThermodynamicConductor());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_THERMODYNAMIC_CONDUCTOR, new RenderThermodynamicConductor());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_THERMODYNAMIC_CONDUCTOR, new RenderThermodynamicConductor());
        ClientRegistrationUtil.bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_THERMODYNAMIC_CONDUCTOR, new RenderThermodynamicConductor());

        //Block render layers
        //TODO: Re-evaluate the different layers things are set to, as most things are set to cutout, but I believe a good number of these
        // can be solid (so not listed here at all) and some maybe translucent
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.STRUCTURAL_GLASS, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.CHARGEPAD, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.CHEMICAL_CRYSTALLIZER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.CHEMICAL_INFUSER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.CHEMICAL_INJECTION_CHAMBER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.CHEMICAL_OXIDIZER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.CHEMICAL_WASHER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.COMBINER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.CRUSHER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.DIGITAL_MINER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELECTRIC_PUMP, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELECTROLYTIC_SEPARATOR, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ENERGIZED_SMELTER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ENRICHMENT_CHAMBER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.FLUIDIC_PLENISHER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.FORMULAIC_ASSEMBLICATOR, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.FUELWOOD_HEATER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.LASER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.LASER_AMPLIFIER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.LASER_TRACTOR_BEAM, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.LOGISTICAL_SORTER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.METALLURGIC_INFUSER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.OREDICTIONIFICATOR, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.OSMIUM_COMPRESSOR, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.PERSONAL_CHEST, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.PRECISION_SAWMILL, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.PRESSURIZED_REACTION_CHAMBER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.PURIFICATION_CHAMBER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.QUANTUM_ENTANGLOPORTER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.RESISTIVE_HEATER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ROTARY_CONDENSENTRATOR, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.SEISMIC_VIBRATOR, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.SOLAR_NEUTRON_ACTIVATOR, RenderType.func_228643_e_());
        //Bounding Blocks
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BOUNDING_BLOCK, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_BOUNDING_BLOCK, RenderType.func_228643_e_());
        //Energy Cubes
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BASIC_ENERGY_CUBE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_ENERGY_CUBE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELITE_ENERGY_CUBE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ULTIMATE_ENERGY_CUBE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.CREATIVE_ENERGY_CUBE, RenderType.func_228643_e_());
        //Fluid Tanks
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BASIC_FLUID_TANK, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_FLUID_TANK, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELITE_FLUID_TANK, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ULTIMATE_FLUID_TANK, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.CREATIVE_FLUID_TANK, RenderType.func_228643_e_());
        //Factories
        //Combining
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BASIC_COMBINING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_COMBINING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELITE_COMBINING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ULTIMATE_COMBINING_FACTORY, RenderType.func_228643_e_());
        //Compressing
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BASIC_COMPRESSING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_COMPRESSING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELITE_COMPRESSING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ULTIMATE_COMPRESSING_FACTORY, RenderType.func_228643_e_());
        //Crushing
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BASIC_CRUSHING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_CRUSHING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELITE_CRUSHING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ULTIMATE_CRUSHING_FACTORY, RenderType.func_228643_e_());
        //Enriching
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BASIC_ENRICHING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_ENRICHING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELITE_ENRICHING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ULTIMATE_ENRICHING_FACTORY, RenderType.func_228643_e_());
        //Infusing
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BASIC_INFUSING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_INFUSING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELITE_INFUSING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ULTIMATE_INFUSING_FACTORY, RenderType.func_228643_e_());
        //Injecting
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BASIC_INJECTING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_INJECTING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELITE_INJECTING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ULTIMATE_INJECTING_FACTORY, RenderType.func_228643_e_());
        //Purifying
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BASIC_PURIFYING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_PURIFYING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELITE_PURIFYING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ULTIMATE_PURIFYING_FACTORY, RenderType.func_228643_e_());
        //Sawing
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BASIC_SAWING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_SAWING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELITE_SAWING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ULTIMATE_SAWING_FACTORY, RenderType.func_228643_e_());
        //Smelting
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BASIC_SMELTING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_SMELTING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELITE_SMELTING_FACTORY, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ULTIMATE_SMELTING_FACTORY, RenderType.func_228643_e_());
        //Transmitters
        //TODO: Is this even the proper way to convert the canRenderInLayer the transmitters used to use
        //Logistical transporters
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.RESTRICTIVE_TRANSPORTER, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.DIVERSION_TRANSPORTER, renderType -> renderType.equals(RenderType.func_228643_e_()) || renderType.equals(RenderType.func_228645_f_()));
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BASIC_LOGISTICAL_TRANSPORTER, renderType -> renderType.equals(RenderType.func_228643_e_()) || renderType.equals(RenderType.func_228645_f_()));
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_LOGISTICAL_TRANSPORTER, renderType -> renderType.equals(RenderType.func_228643_e_()) || renderType.equals(RenderType.func_228645_f_()));
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELITE_LOGISTICAL_TRANSPORTER, renderType -> renderType.equals(RenderType.func_228643_e_()) || renderType.equals(RenderType.func_228645_f_()));
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ULTIMATE_LOGISTICAL_TRANSPORTER, renderType -> renderType.equals(RenderType.func_228643_e_()) || renderType.equals(RenderType.func_228645_f_()));
        //Mechanical Pipes
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BASIC_MECHANICAL_PIPE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_MECHANICAL_PIPE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELITE_MECHANICAL_PIPE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ULTIMATE_MECHANICAL_PIPE, RenderType.func_228643_e_());
        //Pressurized Tubes
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BASIC_PRESSURIZED_TUBE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_PRESSURIZED_TUBE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELITE_PRESSURIZED_TUBE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ULTIMATE_PRESSURIZED_TUBE, RenderType.func_228643_e_());
        //Universal Cables
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BASIC_UNIVERSAL_CABLE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_UNIVERSAL_CABLE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELITE_UNIVERSAL_CABLE, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ULTIMATE_UNIVERSAL_CABLE, RenderType.func_228643_e_());
        //Thermodynamic Conductors
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.BASIC_THERMODYNAMIC_CONDUCTOR, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ADVANCED_THERMODYNAMIC_CONDUCTOR, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ELITE_THERMODYNAMIC_CONDUCTOR, RenderType.func_228643_e_());
        ClientRegistrationUtil.setRenderLayer(MekanismBlock.ULTIMATE_THERMODYNAMIC_CONDUCTOR, RenderType.func_228643_e_());

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
        Minecraft.getInstance().particles.registerFactory(MekanismParticleType.LASER.getParticleType(), ParticleLaser.Factory::new);
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
              MekanismBlock.BASIC_FLUID_TANK, MekanismBlock.ADVANCED_FLUID_TANK, MekanismBlock.ELITE_FLUID_TANK, MekanismBlock.ULTIMATE_FLUID_TANK,
              MekanismBlock.CREATIVE_FLUID_TANK);
    }
}