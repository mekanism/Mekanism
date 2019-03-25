package mekanism.common;

import java.io.File;
import java.lang.ref.WeakReference;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.Pos3D;
import mekanism.client.SparkleAnimation.INodeChecker;
import mekanism.common.base.IGuiProvider;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.ContainerAdvancedElectricMachine;
import mekanism.common.inventory.container.ContainerChanceMachine;
import mekanism.common.inventory.container.ContainerChemicalCrystallizer;
import mekanism.common.inventory.container.ContainerChemicalDissolutionChamber;
import mekanism.common.inventory.container.ContainerChemicalInfuser;
import mekanism.common.inventory.container.ContainerChemicalOxidizer;
import mekanism.common.inventory.container.ContainerChemicalWasher;
import mekanism.common.inventory.container.ContainerDictionary;
import mekanism.common.inventory.container.ContainerDigitalMiner;
import mekanism.common.inventory.container.ContainerDoubleElectricMachine;
import mekanism.common.inventory.container.ContainerDynamicTank;
import mekanism.common.inventory.container.ContainerElectricMachine;
import mekanism.common.inventory.container.ContainerElectricPump;
import mekanism.common.inventory.container.ContainerElectrolyticSeparator;
import mekanism.common.inventory.container.ContainerEnergyCube;
import mekanism.common.inventory.container.ContainerFactory;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerFluidTank;
import mekanism.common.inventory.container.ContainerFluidicPlenisher;
import mekanism.common.inventory.container.ContainerFormulaicAssemblicator;
import mekanism.common.inventory.container.ContainerFuelwoodHeater;
import mekanism.common.inventory.container.ContainerGasTank;
import mekanism.common.inventory.container.ContainerInductionMatrix;
import mekanism.common.inventory.container.ContainerLaserAmplifier;
import mekanism.common.inventory.container.ContainerLaserTractorBeam;
import mekanism.common.inventory.container.ContainerMetallurgicInfuser;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.inventory.container.ContainerOredictionificator;
import mekanism.common.inventory.container.ContainerPRC;
import mekanism.common.inventory.container.ContainerQuantumEntangloporter;
import mekanism.common.inventory.container.ContainerResistiveHeater;
import mekanism.common.inventory.container.ContainerRotaryCondensentrator;
import mekanism.common.inventory.container.ContainerSecurityDesk;
import mekanism.common.inventory.container.ContainerSeismicVibrator;
import mekanism.common.inventory.container.ContainerSolarNeutronActivator;
import mekanism.common.inventory.container.ContainerTeleporter;
import mekanism.common.inventory.container.ContainerThermalEvaporationController;
import mekanism.common.inventory.container.ContainerUpgradeManagement;
import mekanism.common.inventory.container.robit.ContainerRobitCrafting;
import mekanism.common.inventory.container.robit.ContainerRobitInventory;
import mekanism.common.inventory.container.robit.ContainerRobitMain;
import mekanism.common.inventory.container.robit.ContainerRobitRepair;
import mekanism.common.inventory.container.robit.ContainerRobitSmelting;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterMessage;
import mekanism.common.tile.TileEntityChanceMachine;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.TileEntityChemicalInfuser;
import mekanism.common.tile.TileEntityChemicalOxidizer;
import mekanism.common.tile.TileEntityChemicalWasher;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.TileEntityFuelwoodHeater;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityPRC;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.tile.prefab.TileEntityDoubleElectricMachine;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import mekanism.common.util.UnitDisplayUtils.EnergyType;
import mekanism.common.util.UnitDisplayUtils.TempType;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.FMLInjectionData;

/**
 * Common proxy for the Mekanism mod.
 *
 * @author AidanBrady
 */
public class CommonProxy implements IGuiProvider {

    protected final String[] API_PRESENT_MESSAGE = {"Mekanism API jar detected (Mekanism-<version>-api.jar),",
          "please delete it from your mods folder and restart the game."};

    /**
     * Register tile entities that have special models. Overwritten in client to register TESRs.
     */
    public void registerTESRs() {
    }

    public void handleTeleporterUpdate(PortableTeleporterMessage message) {
    }

    /**
     * Handles an PERSONAL_CHEST_CLIENT_OPEN packet via the proxy, not handled on the server-side.
     *
     * @param entityplayer - player the packet was sent from
     * @param id - the gui ID to open
     * @param windowId - the container-specific window ID
     * @param isBlock - if the chest is a block
     * @param pos - coordinates
     */
    public void openPersonalChest(EntityPlayer entityplayer, int id, int windowId, boolean isBlock, BlockPos pos,
          EnumHand hand) {
    }

    /**
     * Register and load client-only item render information.
     */
    public void registerItemRenders() {
    }

    /**
     * Register and load client-only block render information.
     */
    public void registerBlockRenders() {
    }

    /**
     * Set and load the mod's common configuration properties.
     */
    public void loadConfiguration() {
        general.controlCircuitOreDict = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "ControlCircuitOreDict", true,
                    "Enables recipes using Control Circuits to use OreDict'd Control Circuits from other mods.")
              .getBoolean();
        general.logPackets = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "LogPackets", false, "Log Mekanica packet names. Debug setting.")
              .getBoolean();
        general.dynamicTankEasterEgg = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "DynamicTankEasterEgg", false, "Audible sparkles.").getBoolean();
        general.cardboardSpawners = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "AllowSpawnerBoxPickup", true,
                    "Allows vanilla spawners to be moved with a Cardboard Box.").getBoolean();
        general.enableWorldRegeneration = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "EnableWorldRegeneration", false,
                    "Allows chunks to retrogen Mekanica ore blocks.").getBoolean();
        general.spawnBabySkeletons = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "SpawnBabySkeletons", true,
                    "Enable the spawning of baby skeletons. Think baby zombies but skeletons.")
              .getBoolean();
        general.obsidianTNTDelay = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "ObsidianTNTDelay", 100, "Fuse time for Obsidian TNT.")
              .getInt();
        general.obsidianTNTBlastRadius = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "ObsidianTNTBlastRadius", 12,
                    "Radius of the explosion of Obsidian TNT.").getInt();
        general.UPDATE_DELAY = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ClientUpdateDelay", 10,
              "How many ticks must pass until a block's active state can sync with the client.")
              .getInt();
        general.osmiumPerChunk = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "OsmiumPerChunk", 12, "Chance that osmium generates in a chunk.", 0,
                    Integer.MAX_VALUE).getInt();
        general.osmiumMaxVeinSize = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "OsmiumVeinSize", 8, "Max number of blocks in an osmium vein.", 1,
                    Integer.MAX_VALUE)
              .getInt();
        general.copperPerChunk = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "CopperPerChunk", 16, "Chance that copper generates in a chunk.", 0,
                    Integer.MAX_VALUE)
              .getInt();
        general.copperMaxVeinSize = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "CopperVeinSize", 8, "Max number of blocks in a copper vein.", 1,
                    Integer.MAX_VALUE)
              .getInt();
        general.tinPerChunk = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "TinPerChunk", 14, "Chance that tin generates in a chunk.", 0,
                    Integer.MAX_VALUE).getInt();
        general.tinMaxVeinSize = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "TinVeinSize", 8, "Max number of blocks in a tin vein.", 1,
                    Integer.MAX_VALUE).getInt();
        general.saltPerChunk = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "SaltPerChunk", 2, "Chance that salt generates in a chunk.", 0,
                    Integer.MAX_VALUE).getInt();
        general.saltMaxVeinSize = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "SaltVeinSize", 6, "Max number of blocks in a salt vein.", 1,
                    Integer.MAX_VALUE).getInt();
        general.userWorldGenVersion = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "WorldRegenVersion", 0,
              "Change this value to cause Mekanica to regen its ore in all loaded chunks.")
              .getInt();
        general.FROM_IC2 = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "JoulesToEU", 10D,
              "Conversion multiplier from EU to Joules (EU * JoulesToEU = Joules)").getDouble();
        general.TO_IC2 = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EUToJoules", .1D,
              "Conversion multiplier from Joules to EU (Joules * EUToJoules = EU)").getDouble();
        general.FROM_RF = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "JoulesToRF", 2.5D,
              "Conversion multiplier from RF to Joules (RF * JoulesToRF = Joules)").getDouble();
        general.TO_RF = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "RFToJoules", 0.4D,
              "Conversion multiplier from Joules to RF (Joules * RFToJoules = RF)").getDouble();
        general.FROM_TESLA = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "JoulesToTesla", 2.5D,
              "Conversion multiplier from Tesla to Joules (Tesla * JoulesToTesla = Joules)")
              .getDouble();
        general.TO_TESLA = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "TeslaToJoules", 0.4D,
              "Conversion multiplier from Joules to Tesla (Joules * TeslaToJoules = Tesla)")
              .getDouble();
        general.FROM_FORGE = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "JoulesToForge", 2.5D,
              "Conversion multiplier from Forge Energy to Joules (FE * JoulesToForge = Joules)")
              .getDouble();
        general.TO_FORGE = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ForgeToJoules", 0.4D,
              "Conversion multiplier from Joules to Forge Energy (Joules * ForgeToJoules = FE)")
              .getDouble();
        general.FROM_H2 = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "HydrogenEnergyDensity", 200D,
              "How much energy is produced per mB of Hydrogen, also affects Electrolytic Separator usage, Ethylene burn rate and Gas generator energy capacity")
              .getDouble();
        general.ETHENE_BURN_TIME = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EthyleneBurnTime", 40,
              "Burn time for Ethylene (1mB hydrogen + 2*bioFuel/tick*200ticks/100mB * 20x efficiency bonus).").getInt();
        general.ENERGY_PER_REDSTONE = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "EnergyPerRedstone", 10000D,
                    "How much energy (Joules) a piece of redstone gives in machines.").getDouble();
        general.DISASSEMBLER_USAGE = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "DisassemblerEnergyUsage", 10,
                    "Base Energy (Joules) usage of the Atomic Disassembler.").getInt();
        //If this is less than 1, upgrades make machines worse. If less than 0, I don't even know.
        general.maxUpgradeMultiplier = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "UpgradeModifier", 10,
                    "Base factor for working out machine performance with upgrades - UpgradeModifier * (UpgradesInstalled/UpgradesPossible).",
                    1, Integer.MAX_VALUE).getInt();
        general.minerSilkMultiplier = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "MinerSilkMultiplier", 6,
                    "Energy multiplier for using silk touch mode with the Digital Miner.").getDouble();
        general.prefilledGasTanks = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "PrefilledGasTanks", true,
                    "Add filled creative gas tanks to creative/JEI.").getBoolean();
        general.armoredJetpackDamageRatio = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "ArmoredJetpackDamageRatio", 0.8,
                    "Damage absorb ratio of the Armored Jetpack.").getDouble();
        general.armoredJetpackDamageMax = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "ArmoredJepackDamageMax", 115,
                    "Max damage the Armored Jetpack can absorb.").getInt();
        general.aestheticWorldDamage = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "AestheticWorldDamage", true,
                    "If enabled, lasers can break blocks and the flamethrower starts fires.").getBoolean();
        general.opsBypassRestrictions = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "OpsBypassRestrictions", false,
                    "Ops can bypass the block security restrictions if enabled.").getBoolean();
        general.maxJetpackGas = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "MaxJetpackGas", 24000, "Jetpack Gas Tank capacity in mB.")
              .getInt();
        general.maxScubaGas = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "MaxScubaGas", 24000, "Scuba Tank Gas Tank capacity in mB.")
              .getInt();
        general.maxFlamethrowerGas = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "MaxFlamethrowerGas", 24000, "Flamethrower Gas Tank capacity in mB.")
              .getInt();
        general.maxPumpRange = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "MaxPumpRange", 80,
              "Maximum block distance to pull fluid from for the Electric Pump.").getInt();
        general.pumpWaterSources = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "PumpWaterSources", false,
              "If enabled makes Water and Heavy Water blocks be removed from the world on pump.")
              .getBoolean();
        general.maxPlenisherNodes = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "MaxPlenisherNodes", 4000,
                    "Fluidic Plenisher stops after this many blocks.").getInt();
        general.evaporationHeatDissipation = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "EvaporationHeatDissipation", 0.02D,
                    "Thermal Evaporation Tower heat loss per tick.").getDouble();
        general.evaporationTempMultiplier = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "EvaporationTempMultiplier", 0.1D,
                    "Temperature to amount produced ratio for Thermal Evaporation Tower.").getDouble();
        general.evaporationSolarMultiplier = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "EvaporationSolarMultiplier", 0.2D,
                    "Heat to absorb per Solar Panel array of Thermal Evaporation Tower.").getDouble();
        general.evaporationMaxTemp = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "EvaporationMaxTemp", 3000D,
                    "Max Temperature of the Thermal Evaporation Tower.").getDouble();
        general.energyPerHeat = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EnergyPerHeat", 1000D,
              "Joules required by the Resistive Heater to produce one unit of heat. Also affects Thermoelectric Boiler's Water->Steam rate.")
              .getDouble();
        general.maxEnergyPerSteam = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "MaxEnergyPerSteam", 100D,
                    "Maximum Joules per mB of Steam. Also affects Thermoelectric Boiler.").getDouble();
        general.superheatingHeatTransfer = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "SuperheatingHeatTransfer", 10000D,
                    "Amount of heat each Boiler heating element produces.").getDouble();
        general.heatPerFuelTick = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "HeatPerFuelTick", 4D,
              "Amount of heat produced per fuel tick of a fuel's burn time in the Fuelwood Heater.")
              .getDouble();
        general.allowTransmitterAlloyUpgrade = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "AllowTransmitterAlloyUpgrade", true,
                    "Allow right clicking on Cables/Pipes/Tubes with alloys to upgrade the tier.").getBoolean();
        general.allowChunkloading = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "AllowChunkloading", true,
                    "Disable to make the anchor upgrade not do anything.").getBoolean();
        general.allowProtection = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "AllowProtection", true,
              "Enable the security system for players to prevent others from accessing their machines. Does NOT affect Frequencies.")
              .getBoolean();
        general.portableTeleporterDelay = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "PortableTeleporterDelay", 0,
                    "Delay in ticks before a player is teleported after clicking the Teleport button in the portable teleporter.")
              .getInt();
        general.quantumEntangloporterEnergyTransfer = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "QuantumEntangloporterEnergyTransfer", 16000000D,
                    "Maximum buffer of an Entangoloporter frequency.").getDouble();

        general.blacklistIC2 = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "BlacklistIC2Power", false,
              "Disables IC2 power integration. Requires world restart (server-side option in SMP).").getBoolean();
        general.blacklistRF = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "BlacklistRFPower", false,
              "Disables Thermal Expansion RedstoneFlux power integration. Requires world restart (server-side option in SMP).")
              .getBoolean();
        general.blacklistTesla = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "BlacklistTeslaPower", false,
                    "Disables Tesla power integration. Requires world restart (server-side option in SMP).")
              .getBoolean();
        general.blacklistForge = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "BlacklistForgePower", false,
                    "Disables Forge Energy (FE,IF,uF,CF) power integration. Requires world restart (server-side option in SMP).")
              .getBoolean();

        String s = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "EnergyType", "RF", "Displayed energy type in Mekanica GUIs.",
                    new String[]{"J", "RF", "EU", "T"})
              .getString().trim().toLowerCase();

        switch (s) {
            case "joules":
                general.energyUnit = EnergyType.J;
                break;
            case "eu":
            case "ic2":
                general.energyUnit = EnergyType.EU;
                break;
            case "tesla":
                general.energyUnit = EnergyType.T;
                break;
            default:
                general.energyUnit = EnergyType.RF;
        }

        s = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "Temperature Units", "K",
                    "Displayed temperature unit in Mekanica GUIs.", new String[]{"K", "C", "R", "F"})
              .getString();

        if (s != null) {
            if (s.trim().equalsIgnoreCase("k") || s.trim().equalsIgnoreCase("kelvin")) {
                general.tempUnit = TempType.K;
            } else if (s.trim().equalsIgnoreCase("c") || s.trim().equalsIgnoreCase("celsius") || s.trim()
                  .equalsIgnoreCase("centigrade")) {
                general.tempUnit = TempType.C;
            } else if (s.trim().equalsIgnoreCase("r") || s.trim().equalsIgnoreCase("rankine")) {
                general.tempUnit = TempType.R;
            } else if (s.trim().equalsIgnoreCase("f") || s.trim().equalsIgnoreCase("fahrenheit")) {
                general.tempUnit = TempType.F;
            } else if (s.trim().equalsIgnoreCase("a") || s.trim().equalsIgnoreCase("ambient") || s.trim()
                  .equalsIgnoreCase("stp")) {
                general.tempUnit = TempType.STP;
            }
        }

        general.laserRange = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "LaserRange", 64, "How far (in blocks) a laser can travel.")
              .getInt();
        general.laserEnergyNeededPerHardness = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "LaserDiggingEnergy", 100000,
                    "Energy needed to destroy or attract blocks with a Laser (per block hardness level).")
              .getInt();
        general.destroyDisabledBlocks = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "DestroyDisabledBlocks", true,
                    "If machine is disabled in config, do we set its block to air if it is found in world?")
              .getBoolean();
        general.digitalMinerMaxRadius = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "DigitalMinerMaxRadius", 32,
                    "Maximum radius in blocks that the Digital Miner can reach. "
                          + "(Increasing this may have negative effects on stability and/or performance. "
                          + "We strongly recommend you leave it at the default value.)",
                    1, Integer.MAX_VALUE).getInt();

        for (MachineType type : BlockStateMachine.MachineType.getValidMachines()) {
            general.machinesManager.setEntry(type.blockName,
                  Mekanism.configuration.get("machines", type.blockName + "Enabled", true,
                        "Allow " + type.blockName + " to be used/crafted.").getBoolean());
        }

        general.sawdustChancePlank = Mekanism.configuration
              .get(Configuration.CATEGORY_GENERAL, "SawdustChancePlank", 0.25D,
                    "Chance of producing sawdust per operation in the precision sawmill when turning planks into sticks.")
              .getDouble();
        general.sawdustChanceLog = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "SawdustChanceLog", 1D,
              "Chance of producing sawdust per operation in the precision sawmill when turning logs into planks.")
              .getDouble();

        usage.enrichmentChamberUsage = Mekanism.configuration
              .get("usage", "EnrichmentChamberUsage", 50D, "Energy per operation tick (Joules).").getDouble();
        usage.osmiumCompressorUsage = Mekanism.configuration
              .get("usage", "OsmiumCompressorUsage", 100D, "Energy per operation tick (Joules).").getDouble();
        usage.combinerUsage = Mekanism.configuration
              .get("usage", "CombinerUsage", 50D, "Energy per operation tick (Joules).").getDouble();
        usage.crusherUsage = Mekanism.configuration
              .get("usage", "CrusherUsage", 50D, "Energy per operation tick (Joules).").getDouble();
        usage.metallurgicInfuserUsage = Mekanism.configuration
              .get("usage", "MetallurgicInfuserUsage", 50D, "Energy per operation tick (Joules).").getDouble();
        usage.purificationChamberUsage = Mekanism.configuration
              .get("usage", "PurificationChamberUsage", 200D, "Energy per operation tick (Joules).")
              .getDouble();
        usage.energizedSmelterUsage = Mekanism.configuration
              .get("usage", "EnergizedSmelterUsage", 50D, "Energy per operation tick (Joules).").getDouble();
        usage.digitalMinerUsage = Mekanism.configuration
              .get("usage", "DigitalMinerUsage", 100D, "Energy per operation tick (Joules).").getDouble();
        usage.electricPumpUsage = Mekanism.configuration
              .get("usage", "ElectricPumpUsage", 100D, "Energy per operation tick (Joules).").getDouble();
        usage.rotaryCondensentratorUsage = Mekanism.configuration
              .get("usage", "RotaryCondensentratorUsage", 50D, "Energy per operation tick (Joules).")
              .getDouble();
        usage.oxidationChamberUsage = Mekanism.configuration
              .get("usage", "OxidationChamberUsage", 200D, "Energy per operation tick (Joules).").getDouble();
        usage.chemicalInfuserUsage = Mekanism.configuration
              .get("usage", "ChemicalInfuserUsage", 200D, "Energy per operation tick (Joules).").getDouble();
        usage.chemicalInjectionChamberUsage = Mekanism.configuration
              .get("usage", "ChemicalInjectionChamberUsage", 400D, "Energy per operation tick (Joules).")
              .getDouble();
        usage.precisionSawmillUsage = Mekanism.configuration
              .get("usage", "PrecisionSawmillUsage", 50D, "Energy per operation tick (Joules).").getDouble();
        usage.chemicalDissolutionChamberUsage = Mekanism.configuration
              .get("usage", "ChemicalDissolutionChamberUsage", 400D, "Energy per operation tick (Joules).").getDouble();
        usage.chemicalWasherUsage = Mekanism.configuration
              .get("usage", "ChemicalWasherUsage", 200D, "Energy per operation tick (Joules).").getDouble();
        usage.chemicalCrystallizerUsage = Mekanism.configuration
              .get("usage", "ChemicalCrystallizerUsage", 400D, "Energy per operation tick (Joules).")
              .getDouble();
        usage.seismicVibratorUsage = Mekanism.configuration
              .get("usage", "SeismicVibratorUsage", 50D, "Energy per operation tick (Joules).").getDouble();
        usage.pressurizedReactionBaseUsage = Mekanism.configuration
              .get("usage", "PressurizedReactionBaseUsage", 5D, "Energy per operation tick (Joules).")
              .getDouble();
        usage.fluidicPlenisherUsage = Mekanism.configuration
              .get("usage", "FluidicPlenisherUsage", 100D, "Energy per operation tick (Joules).").getDouble();
        usage.laserUsage = Mekanism.configuration
              .get("usage", "LaserUsage", 5000D, "Energy per operation tick (Joules).").getDouble();
        usage.heavyWaterElectrolysisUsage = Mekanism.configuration.get("usage", "HeavyWaterElectrolysisUsage", 800D,
              "Energy needed for one [recipe unit] of heavy water production (Joules).")
              .getDouble();
        usage.formulaicAssemblicatorUsage = Mekanism.configuration
              .get("usage", "FormulaicAssemblicatorUsage", 100D, "Energy per operation tick (Joules).")
              .getDouble();
        usage.teleporterBaseUsage = Mekanism.configuration
              .get("usage", "TeleporterBaseUsage", 1000, "Base Joules cost for a teleportation.")
              .getInt();
        usage.teleporterDistanceUsage = Mekanism.configuration.get("usage", "TeleporterDistanceUsage", 10,
              "Joules per unit of distance travelled during teleportation - sqrt(xDiff^2 + yDiff^2 + zDiff^2).")
              .getInt();
        usage.teleporterDimensionPenalty = Mekanism.configuration
              .get("usage", "TeleporterDimensionPenalty", 10000,
                    "Flat additional cost for interdimensional teleportation.").getInt();

        Tier.loadConfig();

        if (Mekanism.configuration.hasChanged()) {
            Mekanism.configuration.save();
        }
    }

    /**
     * Set up and load the utilities this mod uses.
     */
    public void init() {
        MinecraftForge.EVENT_BUS.register(Mekanism.worldTickHandler);
    }

    /**
     * Whether or not the game is paused.
     */
    public boolean isPaused() {
        return false;
    }

    /**
     * Adds block hit effects on the client side.
     */
    public void addHitEffects(Coord4D coord, RayTraceResult mop) {
    }

    /**
     * Does a generic creation animation, starting from the rendering block.
     */
    public void doGenericSparkle(TileEntity tileEntity, INodeChecker checker) {
    }

    /**
     * Does the multiblock creation animation, starting from the rendering block.
     */
    public void doMultiblockSparkle(TileEntityMultiblock<?> tileEntity) {
    }

    @Override
    public Object getClientGui(int ID, EntityPlayer player, World world, BlockPos pos) {
        return null;
    }

    @Override
    public Container getServerGui(int ID, EntityPlayer player, World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);

        switch (ID) {
            case 0:
                return new ContainerDictionary(player.inventory);
            case 2:
                return new ContainerDigitalMiner(player.inventory, (TileEntityDigitalMiner) tileEntity);
            case 3:
                return new ContainerElectricMachine(player.inventory, (TileEntityElectricMachine) tileEntity);
            case 4:
                return new ContainerAdvancedElectricMachine(player.inventory,
                      (TileEntityAdvancedElectricMachine) tileEntity);
            case 5:
                return new ContainerDoubleElectricMachine(player.inventory,
                      (TileEntityDoubleElectricMachine) tileEntity);
            case 6:
                return new ContainerElectricMachine(player.inventory, (TileEntityElectricMachine) tileEntity);
            case 7:
                return new ContainerRotaryCondensentrator(player.inventory,
                      (TileEntityRotaryCondensentrator) tileEntity);
            case 8:
                return new ContainerEnergyCube(player.inventory, (TileEntityEnergyCube) tileEntity);
            case 9:
                return new ContainerNull(player, (TileEntityContainerBlock) tileEntity);
            case 10:
                return new ContainerGasTank(player.inventory, (TileEntityGasTank) tileEntity);
            case 11:
                return new ContainerFactory(player.inventory, (TileEntityFactory) tileEntity);
            case 12:
                return new ContainerMetallurgicInfuser(player.inventory, (TileEntityMetallurgicInfuser) tileEntity);
            case 13:
                return new ContainerTeleporter(player.inventory, (TileEntityTeleporter) tileEntity);
            case 14:
                ItemStack itemStack = player.getHeldItem(EnumHand.values()[pos.getX()]);

                if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemPortableTeleporter) {
                    return new ContainerNull();
                }

                return null;
            case 15:
                return new ContainerAdvancedElectricMachine(player.inventory,
                      (TileEntityAdvancedElectricMachine) tileEntity);
            case 16:
                return new ContainerElectricMachine(player.inventory, (TileEntityElectricMachine) tileEntity);
            case 17:
                return new ContainerElectricPump(player.inventory, (TileEntityElectricPump) tileEntity);
            case 18:
                return new ContainerDynamicTank(player.inventory, (TileEntityDynamicTank) tileEntity);
            case 21:
                EntityRobit robit = (EntityRobit) world.getEntityByID(pos.getX());

                if (robit != null) {
                    return new ContainerRobitMain(player.inventory, robit);
                }

                return null;
            case 22:
                robit = (EntityRobit) world.getEntityByID(pos.getX());

                if (robit != null) {
                    return new ContainerRobitCrafting(player.inventory, robit);
                }

                return null;
            case 23:
                robit = (EntityRobit) world.getEntityByID(pos.getX());

                if (robit != null) {
                    return new ContainerRobitInventory(player.inventory, robit);
                }

                return null;
            case 24:
                robit = (EntityRobit) world.getEntityByID(pos.getX());

                if (robit != null) {
                    return new ContainerRobitSmelting(player.inventory, robit);
                }

                return null;
            case 25:
                robit = (EntityRobit) world.getEntityByID(pos.getX());

                if (robit != null) {
                    return new ContainerRobitRepair(player.inventory, robit);
                }

                return null;
            case 26:
                return new ContainerNull(player, (TileEntityContainerBlock) tileEntity);
            case 27:
                return new ContainerFilter(player.inventory, (TileEntityContainerBlock) tileEntity);
            case 28:
                return new ContainerFilter(player.inventory, (TileEntityContainerBlock) tileEntity);
            case 29:
                return new ContainerChemicalOxidizer(player.inventory, (TileEntityChemicalOxidizer) tileEntity);
            case 30:
                return new ContainerChemicalInfuser(player.inventory, (TileEntityChemicalInfuser) tileEntity);
            case 31:
                return new ContainerAdvancedElectricMachine(player.inventory,
                      (TileEntityAdvancedElectricMachine) tileEntity);
            case 32:
                return new ContainerElectrolyticSeparator(player.inventory,
                      (TileEntityElectrolyticSeparator) tileEntity);
            case 33:
                return new ContainerThermalEvaporationController(player.inventory,
                      (TileEntityThermalEvaporationController) tileEntity);
            case 34:
                return new ContainerChanceMachine(player.inventory, (TileEntityChanceMachine) tileEntity);
            case 35:
                return new ContainerChemicalDissolutionChamber(player.inventory,
                      (TileEntityChemicalDissolutionChamber) tileEntity);
            case 36:
                return new ContainerChemicalWasher(player.inventory, (TileEntityChemicalWasher) tileEntity);
            case 37:
                return new ContainerChemicalCrystallizer(player.inventory, (TileEntityChemicalCrystallizer) tileEntity);
            case 39:
                return new ContainerSeismicVibrator(player.inventory, (TileEntitySeismicVibrator) tileEntity);
            case 40:
                return new ContainerPRC(player.inventory, (TileEntityPRC) tileEntity);
            case 41:
                return new ContainerFluidTank(player.inventory, (TileEntityFluidTank) tileEntity);
            case 42:
                return new ContainerFluidicPlenisher(player.inventory, (TileEntityFluidicPlenisher) tileEntity);
            case 43:
                return new ContainerUpgradeManagement(player.inventory, (IUpgradeTile) tileEntity);
            case 44:
                return new ContainerLaserAmplifier(player.inventory, (TileEntityLaserAmplifier) tileEntity);
            case 45:
                return new ContainerLaserTractorBeam(player.inventory, (TileEntityLaserTractorBeam) tileEntity);
            case 46:
                return new ContainerQuantumEntangloporter(player.inventory,
                      (TileEntityQuantumEntangloporter) tileEntity);
            case 47:
                return new ContainerSolarNeutronActivator(player.inventory,
                      (TileEntitySolarNeutronActivator) tileEntity);
            case 48:
                return new ContainerNull(player, (TileEntityContainerBlock) tileEntity);
            case 49:
                return new ContainerInductionMatrix(player.inventory, (TileEntityInductionCasing) tileEntity);
            case 50:
                return new ContainerNull(player, (TileEntityContainerBlock) tileEntity);
            case 51:
                return new ContainerNull(player, (TileEntityContainerBlock) tileEntity);
            case 52:
                return new ContainerOredictionificator(player.inventory, (TileEntityOredictionificator) tileEntity);
            case 53:
                return new ContainerResistiveHeater(player.inventory, (TileEntityResistiveHeater) tileEntity);
            case 54:
                return new ContainerFilter(player.inventory, (TileEntityContainerBlock) tileEntity);
            case 55:
                return new ContainerNull(player, (TileEntityContainerBlock) tileEntity);
            case 56:
                return new ContainerFormulaicAssemblicator(player.inventory,
                      (TileEntityFormulaicAssemblicator) tileEntity);
            case 57:
                return new ContainerSecurityDesk(player.inventory, (TileEntitySecurityDesk) tileEntity);
            case 58:
                return new ContainerFuelwoodHeater(player.inventory, (TileEntityFuelwoodHeater) tileEntity);
        }

        return null;
    }

    public void preInit() {
    }

    public double getReach(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            return player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        }

        return 0;
    }

    /**
     * Gets the Minecraft base directory.
     *
     * @return base directory
     */
    public File getMinecraftDir() {
        return (File) FMLInjectionData.data()[6];
    }

    public void onConfigSync(boolean fromPacket) {
        if (general.cardboardSpawners) {
            MekanismAPI.removeBoxBlacklist(Blocks.MOB_SPAWNER, 0);
        } else {
            MekanismAPI.addBoxBlacklist(Blocks.MOB_SPAWNER, 0);
        }

        BlockStateMachine.MachineType.updateAllUsages();

        if (fromPacket) {
            Mekanism.logger.info("Received config from server.");
        }
    }

    public final WeakReference<EntityPlayer> getDummyPlayer(WorldServer world) {
        return MekFakePlayer.getInstance(world);
    }

    public final WeakReference<EntityPlayer> getDummyPlayer(WorldServer world, double x, double y, double z) {
        return MekFakePlayer.getInstance(world, x, y, z);
    }

    public final WeakReference<EntityPlayer> getDummyPlayer(WorldServer world, BlockPos pos) {
        return getDummyPlayer(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public EntityPlayer getPlayer(MessageContext context) {
        return context.getServerHandler().player;
    }

    public void handlePacket(Runnable runnable, EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            ((WorldServer) player.world).addScheduledTask(runnable);
        }
    }

    public int getGuiId(Block block, int metadata) {
        if (MachineType.get(block, metadata) != null) {
            return MachineType.get(block, metadata).guiId;
        } else if (block == MekanismBlocks.GasTank) {
            return 10;
        } else if (block == MekanismBlocks.EnergyCube) {
            return 8;
        }

        return -1;
    }

    public void renderLaser(World world, Pos3D from, Pos3D to, EnumFacing direction, double energy) {
    }

    public Object getFontRenderer() {
        return null;
    }

    public void throwApiPresentException() {
        throw new RuntimeException(String.join(" ", API_PRESENT_MESSAGE));
    }
}
