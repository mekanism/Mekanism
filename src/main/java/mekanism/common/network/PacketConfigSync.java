package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.MekanismConfig.general;
import mekanism.api.MekanismConfig.machines;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.MekanismConfig.recipes;
import mekanism.api.util.UnitDisplayUtils.EnergyType;
import mekanism.common.Mekanism;
import mekanism.common.Tier;
import mekanism.common.base.IModule;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketConfigSync.ConfigSyncMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketConfigSync implements IMessageHandler<ConfigSyncMessage, IMessage>
{
	@Override
	public IMessage onMessage(ConfigSyncMessage message, MessageContext context) 
	{
		return null;
	}
	
	public static class ConfigSyncMessage implements IMessage
	{
		public ConfigSyncMessage() {}
		
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeBoolean(general.controlCircuitOreDict);
			dataStream.writeBoolean(general.logPackets);
			dataStream.writeBoolean(general.dynamicTankEasterEgg);
			dataStream.writeBoolean(general.voiceServerEnabled);
			dataStream.writeBoolean(general.cardboardSpawners);
			dataStream.writeBoolean(general.spawnBabySkeletons);
			dataStream.writeBoolean(general.enableBoPProgression);
			dataStream.writeInt(general.obsidianTNTDelay);
			dataStream.writeInt(general.obsidianTNTBlastRadius);
			dataStream.writeInt(general.UPDATE_DELAY);
			dataStream.writeDouble(general.FROM_IC2);
			dataStream.writeDouble(general.TO_IC2);
			dataStream.writeDouble(general.FROM_TE);
			dataStream.writeDouble(general.TO_TE);
			dataStream.writeDouble(general.FROM_H2);
			dataStream.writeInt(general.ETHENE_BURN_TIME);
			dataStream.writeInt(general.METHANE_BURN_TIME);
			dataStream.writeDouble(general.ENERGY_PER_REDSTONE);
			dataStream.writeDouble(general.DISASSEMBLER_USAGE);
			dataStream.writeInt(general.VOICE_PORT);
			dataStream.writeInt(general.maxUpgradeMultiplier);
			dataStream.writeInt(general.energyUnit.ordinal());
			dataStream.writeDouble(general.minerSilkMultiplier);
			dataStream.writeBoolean(general.blacklistIC2);
			dataStream.writeBoolean(general.blacklistRF);
			dataStream.writeDouble(general.armoredJetpackDamageRatio);
			dataStream.writeInt(general.armoredJetpackDamageMax);
			dataStream.writeBoolean(general.aestheticWorldDamage);
			dataStream.writeBoolean(general.opsBypassRestrictions);
			dataStream.writeDouble(general.thermalEvaporationSpeed);
			dataStream.writeInt(general.maxJetpackGas);
			dataStream.writeInt(general.maxScubaGas);
			dataStream.writeInt(general.maxFlamethrowerGas);
			dataStream.writeInt(general.maxPumpRange);
			dataStream.writeBoolean(general.pumpWaterSources);
			dataStream.writeInt(general.maxPlenisherNodes);
			dataStream.writeDouble(general.evaporationHeatDissipation);
			dataStream.writeDouble(general.evaporationTempMultiplier);
			dataStream.writeDouble(general.evaporationSolarMultiplier);
			dataStream.writeDouble(general.evaporationMaxTemp);
			dataStream.writeDouble(general.energyPerHeat);
			dataStream.writeDouble(general.maxEnergyPerSteam);
			dataStream.writeDouble(general.superheatingHeatTransfer);
			dataStream.writeDouble(general.heatPerFuelTick);
			dataStream.writeBoolean(general.allowTransmitterAlloyUpgrade);
			dataStream.writeBoolean(general.allowProtection);
			dataStream.writeBoolean(general.EnableQuartzCompat);
			dataStream.writeBoolean(general.EnableDiamondCompat);
			dataStream.writeBoolean(general.EnablePoorOresCompat);
			dataStream.writeBoolean(general.OreDictOsmium);
			dataStream.writeBoolean(general.OreDictPlatinum);
			dataStream.writeBoolean(general.enableSiliconCompat);
			
			for(MachineType type : MachineType.getValidMachines())
			{
				dataStream.writeBoolean(machines.isEnabled(type.name));
			}
	
			dataStream.writeDouble(usage.enrichmentChamberUsage);
			dataStream.writeDouble(usage.osmiumCompressorUsage);
			dataStream.writeDouble(usage.combinerUsage);
			dataStream.writeDouble(usage.crusherUsage);
			dataStream.writeDouble(usage.factoryUsage);
			dataStream.writeDouble(usage.metallurgicInfuserUsage);
			dataStream.writeDouble(usage.purificationChamberUsage);
			dataStream.writeDouble(usage.energizedSmelterUsage);
			dataStream.writeDouble(usage.digitalMinerUsage);
			dataStream.writeDouble(usage.electricPumpUsage);
			dataStream.writeDouble(usage.rotaryCondensentratorUsage);
			dataStream.writeDouble(usage.oxidationChamberUsage);
			dataStream.writeDouble(usage.chemicalInfuserUsage);
			dataStream.writeDouble(usage.chemicalInjectionChamberUsage);
			dataStream.writeDouble(usage.precisionSawmillUsage);
			dataStream.writeDouble(usage.chemicalDissolutionChamberUsage);
			dataStream.writeDouble(usage.chemicalWasherUsage);
			dataStream.writeDouble(usage.chemicalCrystallizerUsage);
			dataStream.writeDouble(usage.seismicVibratorUsage);
			dataStream.writeDouble(usage.fluidicPlenisherUsage);
			dataStream.writeDouble(usage.gasCentrifugeUsage);
			dataStream.writeDouble(usage.heavyWaterElectrolysisUsage);
			dataStream.writeDouble(usage.formulaicAssemblicatorUsage);
			dataStream.writeBoolean(recipes.enableOsmiumBlock);
			dataStream.writeBoolean(recipes.enableBronzeBlock);
			dataStream.writeBoolean(recipes.enableRefinedObsidianBlock);
			dataStream.writeBoolean(recipes.enableCharcoalBlock);
			dataStream.writeBoolean(recipes.enableRefinedGlowstoneBlock);
			dataStream.writeBoolean(recipes.enableSteelBlock);
			dataStream.writeBoolean(recipes.enableCopperBlock);
			dataStream.writeBoolean(recipes.enableTinBlock);
			dataStream.writeBoolean(recipes.enableBins);
			dataStream.writeBoolean(recipes.enableTeleporterFrame);
			dataStream.writeBoolean(recipes.enableSteelCasing);
			dataStream.writeBoolean(recipes.enableDynamicTank);
			dataStream.writeBoolean(recipes.enableDynamicGlass);
			dataStream.writeBoolean(recipes.enableDynamicValve);
			dataStream.writeBoolean(recipes.enableThermalEvaporationController);
			dataStream.writeBoolean(recipes.enableThermalEvaporationValve);
			dataStream.writeBoolean(recipes.enableThermalEvaporationBlock);
			dataStream.writeBoolean(recipes.enableInductionCasing);
			dataStream.writeBoolean(recipes.enableInductionPorts);
			dataStream.writeBoolean(recipes.enableInductionCells);
			dataStream.writeBoolean(recipes.enableInductionProviders);
			dataStream.writeBoolean(recipes.enableSuperheatingElement);
			dataStream.writeBoolean(recipes.enablePressureDispenser);
			dataStream.writeBoolean(recipes.enableBoilerCasing);
			dataStream.writeBoolean(recipes.enableBoilerValve);
			dataStream.writeBoolean(recipes.enableSecurityDesk);
			dataStream.writeBoolean(recipes.enableEnrichmentChamber);
			dataStream.writeBoolean(recipes.enableOsmiumCompressor);
			dataStream.writeBoolean(recipes.enableCombiner);
			dataStream.writeBoolean(recipes.enableCrusher);
			dataStream.writeBoolean(recipes.enableDigitalMiner);
			dataStream.writeBoolean(recipes.enableFactories);
			dataStream.writeBoolean(recipes.enableMetallurgicInfuser);
			dataStream.writeBoolean(recipes.enablePurificationChamber);
			dataStream.writeBoolean(recipes.enableEnergizedSmelter);
			dataStream.writeBoolean(recipes.enableTeleporterBlock);
			dataStream.writeBoolean(recipes.enableElectricPump);
			dataStream.writeBoolean(recipes.enablePersonalChest);
			dataStream.writeBoolean(recipes.enableChargePad);
			dataStream.writeBoolean(recipes.enableLogisticalSorter);
			dataStream.writeBoolean(recipes.enableRotaryCondensentrator);
			dataStream.writeBoolean(recipes.enableChemicalOxidiser);
			dataStream.writeBoolean(recipes.enableChemicalInfuser);
			dataStream.writeBoolean(recipes.enableChemicalInjection);
			dataStream.writeBoolean(recipes.enableElectrolyticSeparator);
			dataStream.writeBoolean(recipes.enableElectrolyticSeparator);
			dataStream.writeBoolean(recipes.enableCardboardBox);
			dataStream.writeBoolean(recipes.enableSawdusttoPaper);
			dataStream.writeBoolean(recipes.enablePrecisionSawmill);
			dataStream.writeBoolean(recipes.enableChemicaDissolution);
			dataStream.writeBoolean(recipes.enableChemicalWasher);
			dataStream.writeBoolean(recipes.enableChemicalCrystallizer);
			dataStream.writeBoolean(recipes.enableSeismicVibrator);
			dataStream.writeBoolean(recipes.enablePressurizedReactorChamber);
			dataStream.writeBoolean(recipes.enableLiquidTanks);
			dataStream.writeBoolean(recipes.enableFluidPlenisher);
			dataStream.writeBoolean(recipes.enableLaser);
			dataStream.writeBoolean(recipes.enableLaserAmplifyier);
			dataStream.writeBoolean(recipes.enableLaserTractorBeam);
			dataStream.writeBoolean(recipes.enableEntangloporter);
			dataStream.writeBoolean(recipes.enableSolarNeutronActivator);
			dataStream.writeBoolean(recipes.enableOredictionator);
			dataStream.writeBoolean(recipes.enableResistiveHeater);
			dataStream.writeBoolean(recipes.enableFormulaicAssembler);
			dataStream.writeBoolean(recipes.enableFuelwoodHeater);
			dataStream.writeBoolean(recipes.enableEnergyCubes);
			dataStream.writeBoolean(recipes.enableObsidianTNT);
			dataStream.writeBoolean(recipes.enableGasTanks);
			dataStream.writeBoolean(recipes.enablePlasticBlocks);
			dataStream.writeBoolean(recipes.enableSaltBlock);
			dataStream.writeBoolean(recipes.enableUniversalCables);
			dataStream.writeBoolean(recipes.enableMechanicalPipes);
			dataStream.writeBoolean(recipes.enablePressurizedTubes);
			dataStream.writeBoolean(recipes.enableLogisticalTransporter);
			dataStream.writeBoolean(recipes.enableThermoConductors);
			dataStream.writeBoolean(recipes.enableElectricBow);
			dataStream.writeBoolean(recipes.enableEnergyTablet);
			dataStream.writeBoolean(recipes.enableMachineUpgrades);
			dataStream.writeBoolean(recipes.enableRobit);
			dataStream.writeBoolean(recipes.enableAtomicDisassembler);
			dataStream.writeBoolean(recipes.enableCircuits);
			dataStream.writeBoolean(recipes.enablePortableTeleporter);
			dataStream.writeBoolean(recipes.enableTeleporterCore);
			dataStream.writeBoolean(recipes.enableConfigurator);
			dataStream.writeBoolean(recipes.enableNetworkReader);
			dataStream.writeBoolean(recipes.enableWalkieTalkie);
			dataStream.writeBoolean(recipes.enableJetpacks);
			dataStream.writeBoolean(recipes.enableScubaSet);
			dataStream.writeBoolean(recipes.enableFreeRunners);
			dataStream.writeBoolean(recipes.enableConfigurationCard);
			dataStream.writeBoolean(recipes.enableCraftingFormula);
			dataStream.writeBoolean(recipes.enableSeismicReader);
			dataStream.writeBoolean(recipes.enableHDPEParts);
			dataStream.writeBoolean(recipes.enableFlamethrower);
			dataStream.writeBoolean(recipes.enableGaugeDropper);
			dataStream.writeBoolean(recipes.enableTierInstaller);


			
			Tier.writeConfig(dataStream);
	
			try {
				for(IModule module : Mekanism.modulesLoaded)
				{
					module.writeConfig(dataStream);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			general.controlCircuitOreDict = dataStream.readBoolean();
			general.logPackets = dataStream.readBoolean();
			general.dynamicTankEasterEgg = dataStream.readBoolean();
			general.voiceServerEnabled = dataStream.readBoolean();
			general.cardboardSpawners = dataStream.readBoolean();
			general.spawnBabySkeletons = dataStream.readBoolean();
			general.enableBoPProgression = dataStream.readBoolean();
			general.obsidianTNTDelay = dataStream.readInt();
			general.obsidianTNTBlastRadius = dataStream.readInt();
			general.UPDATE_DELAY = dataStream.readInt();
			general.FROM_IC2 = dataStream.readDouble();
			general.TO_IC2 = dataStream.readDouble();
			general.FROM_TE = dataStream.readDouble();
			general.TO_TE = dataStream.readDouble();
			general.FROM_H2 = dataStream.readDouble();
			general.ETHENE_BURN_TIME = dataStream.readInt();
			general.METHANE_BURN_TIME = dataStream.readInt();
			general.ENERGY_PER_REDSTONE = dataStream.readDouble();
			general.DISASSEMBLER_USAGE = dataStream.readDouble();
			general.VOICE_PORT = dataStream.readInt();
			general.maxUpgradeMultiplier = dataStream.readInt();
			general.energyUnit = EnergyType.values()[dataStream.readInt()];
			general.minerSilkMultiplier = dataStream.readDouble();
			general.blacklistIC2 = dataStream.readBoolean();
			general.blacklistRF = dataStream.readBoolean();
			general.armoredJetpackDamageRatio = dataStream.readDouble();
			general.armoredJetpackDamageMax = dataStream.readInt();
			general.aestheticWorldDamage = dataStream.readBoolean();
			general.opsBypassRestrictions = dataStream.readBoolean();
			general.thermalEvaporationSpeed = dataStream.readDouble();
			general.maxJetpackGas = dataStream.readInt();
			general.maxScubaGas = dataStream.readInt();
			general.maxFlamethrowerGas = dataStream.readInt();
			general.maxPumpRange = dataStream.readInt();
			general.pumpWaterSources = dataStream.readBoolean();
			general.maxPlenisherNodes = dataStream.readInt();
			general.evaporationHeatDissipation = dataStream.readDouble();
			general.evaporationTempMultiplier = dataStream.readDouble();
			general.evaporationSolarMultiplier = dataStream.readDouble();
			general.evaporationMaxTemp = dataStream.readDouble();
			general.energyPerHeat = dataStream.readDouble();
			general.maxEnergyPerSteam = dataStream.readDouble();
			general.superheatingHeatTransfer = dataStream.readDouble();
			general.heatPerFuelTick = dataStream.readDouble();
			general.allowTransmitterAlloyUpgrade = dataStream.readBoolean();
			general.allowProtection = dataStream.readBoolean();
			general.EnableQuartzCompat = dataStream.readBoolean();
			general.EnableDiamondCompat = dataStream.readBoolean();
			general.EnablePoorOresCompat = dataStream.readBoolean();
			general.OreDictOsmium = dataStream.readBoolean();
			general.OreDictPlatinum = dataStream.readBoolean();
			general.enableSiliconCompat = dataStream.readBoolean();
			
			for(MachineType type : MachineType.getValidMachines())
			{
				machines.setEntry(type.name, dataStream.readBoolean());
			}
	
			usage.enrichmentChamberUsage = dataStream.readDouble();
			usage.osmiumCompressorUsage = dataStream.readDouble();
			usage.combinerUsage = dataStream.readDouble();
			usage.crusherUsage = dataStream.readDouble();
			usage.factoryUsage = dataStream.readDouble();
			usage.metallurgicInfuserUsage = dataStream.readDouble();
			usage.purificationChamberUsage = dataStream.readDouble();
			usage.energizedSmelterUsage = dataStream.readDouble();
			usage.digitalMinerUsage = dataStream.readDouble();
			usage.electricPumpUsage = dataStream.readDouble();
			usage.rotaryCondensentratorUsage = dataStream.readDouble();
			usage.oxidationChamberUsage = dataStream.readDouble();
			usage.chemicalInfuserUsage = dataStream.readDouble();
			usage.chemicalInjectionChamberUsage = dataStream.readDouble();
			usage.precisionSawmillUsage = dataStream.readDouble();
			usage.chemicalDissolutionChamberUsage = dataStream.readDouble();
			usage.chemicalWasherUsage = dataStream.readDouble();
			usage.chemicalCrystallizerUsage = dataStream.readDouble();
			usage.seismicVibratorUsage = dataStream.readDouble();
			usage.fluidicPlenisherUsage = dataStream.readDouble();
			usage.gasCentrifugeUsage = dataStream.readDouble();
			usage.heavyWaterElectrolysisUsage = dataStream.readDouble();
			usage.formulaicAssemblicatorUsage = dataStream.readDouble();
			recipes.enableOsmiumBlock = dataStream.readBoolean();
			recipes.enableBronzeBlock = dataStream.readBoolean();
			recipes.enableRefinedObsidianBlock = dataStream.readBoolean();
			recipes.enableCharcoalBlock = dataStream.readBoolean();
			recipes.enableRefinedGlowstoneBlock = dataStream.readBoolean();
			recipes.enableSteelBlock = dataStream.readBoolean();
			recipes.enableCopperBlock = dataStream.readBoolean();
			recipes.enableTinBlock = dataStream.readBoolean();
			recipes.enableBins = dataStream.readBoolean();
			recipes.enableTeleporterFrame = dataStream.readBoolean();
			recipes.enableSteelCasing = dataStream.readBoolean();
			recipes.enableDynamicTank = dataStream.readBoolean();
			recipes.enableDynamicGlass = dataStream.readBoolean();
			recipes.enableDynamicValve = dataStream.readBoolean();
			recipes.enableThermalEvaporationController = dataStream.readBoolean();
			recipes.enableThermalEvaporationValve = dataStream.readBoolean();
			recipes.enableThermalEvaporationBlock = dataStream.readBoolean();
			recipes.enableInductionCasing = dataStream.readBoolean();
			recipes.enableInductionPorts = dataStream.readBoolean();
			recipes.enableInductionCells = dataStream.readBoolean();
			recipes.enableInductionProviders = dataStream.readBoolean();
			recipes.enableSuperheatingElement = dataStream.readBoolean();
			recipes.enablePressureDispenser = dataStream.readBoolean();
			recipes.enableBoilerCasing = dataStream.readBoolean();
			recipes.enableBoilerValve = dataStream.readBoolean();
			recipes.enableSecurityDesk = dataStream.readBoolean();
			recipes.enableEnrichmentChamber = dataStream.readBoolean();
			recipes.enableOsmiumCompressor = dataStream.readBoolean();
			recipes.enableCombiner = dataStream.readBoolean();
			recipes.enableCrusher = dataStream.readBoolean();
			recipes.enableDigitalMiner = dataStream.readBoolean();
			recipes.enableFactories = dataStream.readBoolean();
			recipes.enableMetallurgicInfuser = dataStream.readBoolean();
			recipes.enablePurificationChamber = dataStream.readBoolean();
			recipes.enableEnergizedSmelter = dataStream.readBoolean();
			recipes.enableTeleporterBlock = dataStream.readBoolean();
			recipes.enableElectricPump = dataStream.readBoolean();
			recipes.enablePersonalChest = dataStream.readBoolean();
			recipes.enableChargePad = dataStream.readBoolean();
			recipes.enableLogisticalSorter = dataStream.readBoolean();
			recipes.enableRotaryCondensentrator = dataStream.readBoolean();
			recipes.enableChemicalOxidiser = dataStream.readBoolean();
			recipes.enableChemicalInfuser = dataStream.readBoolean();
			recipes.enableChemicalInjection = dataStream.readBoolean();
			recipes.enableElectrolyticSeparator = dataStream.readBoolean();
			recipes.enableElectrolyticSeparator = dataStream.readBoolean();
			recipes.enableCardboardBox = dataStream.readBoolean();
			recipes.enableSawdusttoPaper = dataStream.readBoolean();
			recipes.enablePrecisionSawmill = dataStream.readBoolean();
			recipes.enableChemicaDissolution = dataStream.readBoolean();
			recipes.enableChemicalWasher = dataStream.readBoolean();
			recipes.enableChemicalCrystallizer = dataStream.readBoolean();
			recipes.enableSeismicVibrator = dataStream.readBoolean();
			recipes.enablePressurizedReactorChamber = dataStream.readBoolean();
			recipes.enableLiquidTanks = dataStream.readBoolean();
			recipes.enableFluidPlenisher = dataStream.readBoolean();
			recipes.enableLaser = dataStream.readBoolean();
			recipes.enableLaserAmplifyier = dataStream.readBoolean();
			recipes.enableLaserTractorBeam = dataStream.readBoolean();
			recipes.enableEntangloporter = dataStream.readBoolean();
			recipes.enableSolarNeutronActivator = dataStream.readBoolean();
			recipes.enableOredictionator = dataStream.readBoolean();
			recipes.enableResistiveHeater = dataStream.readBoolean();
			recipes.enableFormulaicAssembler = dataStream.readBoolean();
			recipes.enableFuelwoodHeater = dataStream.readBoolean();
			recipes.enableEnergyCubes = dataStream.readBoolean();
			recipes.enableObsidianTNT = dataStream.readBoolean();
			recipes.enableGasTanks = dataStream.readBoolean();
			recipes.enablePlasticBlocks = dataStream.readBoolean();
			recipes.enableSaltBlock = dataStream.readBoolean();
			recipes.enableUniversalCables = dataStream.readBoolean();
			recipes.enableMechanicalPipes = dataStream.readBoolean();
			recipes.enablePressurizedTubes = dataStream.readBoolean();
			recipes.enableLogisticalTransporter = dataStream.readBoolean();
			recipes.enableThermoConductors = dataStream.readBoolean();
			recipes.enableElectricBow = dataStream.readBoolean();
			recipes.enableEnergyTablet = dataStream.readBoolean();
			recipes.enableMachineUpgrades = dataStream.readBoolean();
			recipes.enableRobit = dataStream.readBoolean();
			recipes.enableAtomicDisassembler = dataStream.readBoolean();
			recipes.enableCircuits = dataStream.readBoolean();
			recipes.enablePortableTeleporter = dataStream.readBoolean();
			recipes.enableTeleporterCore = dataStream.readBoolean();
			recipes.enableConfigurator = dataStream.readBoolean();
			recipes.enableNetworkReader = dataStream.readBoolean();
			recipes.enableWalkieTalkie = dataStream.readBoolean();
			recipes.enableJetpacks = dataStream.readBoolean();
			recipes.enableScubaSet = dataStream.readBoolean();
			recipes.enableFreeRunners = dataStream.readBoolean();
			recipes.enableConfigurationCard = dataStream.readBoolean();
			recipes.enableCraftingFormula = dataStream.readBoolean();
			recipes.enableSeismicReader = dataStream.readBoolean();
			recipes.enableHDPEParts = dataStream.readBoolean();
			recipes.enableFlamethrower = dataStream.readBoolean();
			recipes.enableGaugeDropper = dataStream.readBoolean();
			recipes.enableTierInstaller = dataStream.readBoolean();

			
			Tier.readConfig(dataStream);
	
			try {
				for(IModule module : Mekanism.modulesLoaded)
				{
					module.readConfig(dataStream);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
	
			Mekanism.proxy.onConfigSync(true);
		}
	}
}
