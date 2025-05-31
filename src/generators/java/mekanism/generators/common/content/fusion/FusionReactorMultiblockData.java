package mekanism.generators.common.content.fusion;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.capabilities.proxy.AutomatedResourceHandler;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tile.prefab.TileEntityStructuralMultiblock;
import mekanism.common.util.EnergyUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.ResourceUtils;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsChemicals;
import mekanism.generators.common.registries.GeneratorsDamageTypes;
import mekanism.generators.common.registries.GeneratorsItems;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorBlock;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorPort;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class FusionReactorMultiblockData extends MultiblockData {

    public static final String HEAT_TAB = "heat";
    public static final String FUEL_TAB = "fuel";
    public static final String STATS_TAB = "stats";

    public static final int MAX_INJECTION = 98;//this is the effective cap in the GUI, as text field is limited to 2 chars
    //Reaction characteristics
    private static final double burnTemperature = 100_000_000;
    private static final double burnRatio = 1;
    //Thermal characteristics
    private static final long plasmaHeatCapacity = 100;
    private static final double caseHeatCapacity = 1;
    private static final double inverseInsulation = 100_000;
    //Heat transfer metrics
    private static final double plasmaCaseConductivity = 0.2;

    private final List<CapabilityOutputTarget<EnergyHandler>> energyOutputTargets = new ArrayList<>();
    private final List<CapabilityOutputTarget<ResourceHandler<ChemicalResource>>> chemicalOutputTargets = new ArrayList<>();
    private final Set<ITileHeatHandler> heatHandlers = new ObjectOpenHashSet<>();

    @ContainerSync
    private boolean burning = false;

    @ContainerSync
    private final IEnergyContainer energyContainer;
    private final IHeatCapacitor heatCapacitor;

    @ContainerSync(tags = HEAT_TAB)
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getWater", "getWaterCapacity", "getWaterNeeded",
                                                                                     "getWaterFilledPercentage"}, docPlaceholder = "water tank")
    public IFluidTank waterTank;
    @ContainerSync(tags = HEAT_TAB)
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getSteam", "getSteamCapacity", "getSteamNeeded",
                                                                                        "getSteamFilledPercentage"}, docPlaceholder = "steam tank")
    public IChemicalTank steamTank;

    private double biomeAmbientTemp;
    @ContainerSync(tags = HEAT_TAB)
    private double lastPlasmaTemperature;
    @ContainerSync
    private double lastCaseTemperature;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getEnvironmentalLoss")
    public double lastEnvironmentLoss;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getTransferLoss")
    public double lastTransferLoss;

    @ContainerSync(tags = FUEL_TAB)
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getDeuterium", "getDeuteriumCapacity", "getDeuteriumNeeded",
                                                                                        "getDeuteriumFilledPercentage"}, docPlaceholder = "deuterium tank")
    public IChemicalTank deuteriumTank;
    @ContainerSync(tags = FUEL_TAB)
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getTritium", "getTritiumCapacity", "getTritiumNeeded",
                                                                                        "getTritiumFilledPercentage"}, docPlaceholder = "tritium tank")
    public IChemicalTank tritiumTank;
    @ContainerSync(tags = FUEL_TAB)
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getDTFuel", "getDTFuelCapacity", "getDTFuelNeeded",
                                                                                        "getDTFuelFilledPercentage"}, docPlaceholder = "fuel tank")
    public IChemicalTank fuelTank;
    @ContainerSync(tags = {FUEL_TAB, HEAT_TAB, STATS_TAB}, getter = "getInjectionRate", setter = "setInjectionRate")
    private int injectionRate = 2;
    @ContainerSync(tags = {FUEL_TAB, HEAT_TAB, STATS_TAB})
    private int lastBurned;

    private double plasmaTemperature;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getHohlraum", docPlaceholder = "Hohlraum slot")
    final BasicInventorySlot reactorSlot;

    private final PlasmaJournal plasmaJournal = new PlasmaJournal();
    private boolean clientBurning;
    private double clientTemp;

    private long maxWater;
    private long maxSteam;

    @Nullable
    private AABB deathZone;

    public FusionReactorMultiblockData(TileEntityFusionReactorBlock tile) {
        super(tile);
        //Default biome temp to the ambient temperature at the block we are at
        biomeAmbientTemp = HeatAPI.getAmbientTemp(tile.getLevel(), tile.getBlockPos());
        lastPlasmaTemperature = biomeAmbientTemp;
        lastCaseTemperature = biomeAmbientTemp;
        plasmaTemperature = biomeAmbientTemp;
        chemicalTanks.add(deuteriumTank = VariableCapacityChemicalTank.input(this, MekanismGeneratorsConfig.generators.fusionFuelCapacity,
              chemical -> chemical.is(GeneratorTags.Chemicals.DEUTERIUM), this));
        chemicalTanks.add(tritiumTank = VariableCapacityChemicalTank.input(this, MekanismGeneratorsConfig.generators.fusionFuelCapacity,
              chemical -> chemical.is(GeneratorTags.Chemicals.TRITIUM), this));
        chemicalTanks.add(fuelTank = VariableCapacityChemicalTank.input(this, MekanismGeneratorsConfig.generators.fusionFuelCapacity,
              chemical -> chemical.is(GeneratorTags.Chemicals.FUSION_FUEL), createSaveAndComparator()));
        chemicalTanks.add(steamTank = VariableCapacityChemicalTank.output(this, this::getMaxSteam, chemical -> chemical.is(MekanismChemicals.STEAM), this));
        fluidTanks.add(waterTank = VariableCapacityFluidTank.input(this, this::getMaxWater, fluid -> fluid.is(FluidTags.WATER), this));
        energyContainer = VariableCapacityEnergyContainer.output(MekanismGeneratorsConfig.generators.fusionEnergyCapacity, this);
        heatCapacitors.add(heatCapacitor = VariableHeatCapacitor.create(caseHeatCapacity, FusionReactorMultiblockData::getInverseConductionCoefficient,
              () -> inverseInsulation, () -> biomeAmbientTemp, this));
        inventorySlots.add(reactorSlot = BasicInventorySlot.at(ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), GeneratorsItems.HOHLRAUM::is, this, 85, 39));
    }

    @Override
    public IEnergyContainer energyContainer() {
        return energyContainer;
    }

    @Override
    public void onCreated(Level world) {
        super.onCreated(world);
        biomeAmbientTemp = calculateAverageAmbientTemperature(world);
        deathZone = AABB.encapsulatingFullBlocks(getMinPos().offset(1, 1, 1), getMaxPos().offset(-1, -1, -1));
    }

    @Override
    public boolean allowsStructuralGuiAccess(TileEntityStructuralMultiblock multiblock) {
        return false;
    }

    @Override
    public void readUpdateTag(ValueInput input) {
        super.readUpdateTag(input);
        lastPlasmaTemperature = input.getDoubleOr(SerializationConstants.PLASMA_TEMP, getPlasmaTemp());
        setBurning(input.getBooleanOr(SerializationConstants.BURNING, isBurning()));
    }

    @Override
    public void writeUpdateTag(ValueOutput output) {
        super.writeUpdateTag(output);
        output.putDouble(SerializationConstants.PLASMA_TEMP, getLastPlasmaTemp());
        output.putBoolean(SerializationConstants.BURNING, isBurning());
    }

    public void addTemperatureFromEnergyInput(long energyAdded, TransactionContext transaction) {
        if (isBurning()) {
            plasmaJournal.incrementPlasmaTemperature((double) energyAdded / plasmaHeatCapacity, transaction);
        } else {
            plasmaJournal.incrementPlasmaTemperature(((double) energyAdded / plasmaHeatCapacity) * 10, transaction);
        }
    }

    @Override
    public boolean tick(ServerLevel world) {
        boolean needsPacket = super.tick(world);
        int fuelBurned = 0;
        //Only thermal transfer happens unless we're hot enough to burn.
        if (getPlasmaTemp() >= burnTemperature) {
            //If we're not burning, yet we need a hohlraum to ignite
            if (!burning) {
                vaporiseHohlraum();
            }

            //Only inject fuel if we're burning
            if (isBurning()) {
                injectFuel();
                fuelBurned = burnFuel();
                if (fuelBurned == 0) {
                    setBurning(false);
                }
            }
        } else {
            setBurning(false);
        }

        if (lastBurned != fuelBurned) {
            lastBurned = fuelBurned;
        }

        //Perform the heat transfer calculations
        transferHeat();
        updateHeatCapacitors(null);
        updateTemperatures();

        if (!energyOutputTargets.isEmpty() && !energyContainer.isEmpty()) {
            EnergyUtils.emit(getActiveOutputs(energyOutputTargets), energyContainer, null);
        }

        if (!chemicalOutputTargets.isEmpty() && !steamTank.isEmpty()) {
            ResourceUtils.emit(getActiveOutputs(chemicalOutputTargets), steamTank, null);
        }

        if (isBurning()) {
            kill(world);
        }

        if (isBurning() != clientBurning || Math.abs(getLastPlasmaTemp() - clientTemp) > 1_000_000) {
            clientBurning = isBurning();
            clientTemp = getLastPlasmaTemp();
            needsPacket = true;
        }
        return needsPacket;
    }

    @Override
    protected void updateEjectors(Level world) {
        heatHandlers.clear();
        energyOutputTargets.clear();
        chemicalOutputTargets.clear();
        for (Map.Entry<BlockPos, ValveData> entry : valves.entrySet()) {
            TileEntityFusionReactorPort tile = WorldUtils.getTileEntity(TileEntityFusionReactorPort.class, world, entry.getKey());
            if (tile != null) {
                heatHandlers.add(tile);
                Direction side = entry.getValue().side;
                tile.addEnergyTargetCapability(energyOutputTargets, side);
                tile.addChemicalTargetCapability(chemicalOutputTargets, side);
            }
        }
    }

    public void updateTemperatures() {
        lastPlasmaTemperature = getPlasmaTemp();
        lastCaseTemperature = heatCapacitor.getTemperature();
    }

    private void kill(ServerLevel world) {
        if (deathZone != null && world.getRandom().nextInt() % SharedConstants.TICKS_PER_SECOND == 0) {
            DamageSource damageSource = GeneratorsDamageTypes.FUSION.source(world, deathZone.getCenter());
            for (Entity entity : world.getEntitiesOfClass(Entity.class, deathZone)) {
                entity.hurtServer(world, damageSource, 50_000F);
            }
        }
    }

    private void vaporiseHohlraum() {
        if (GeneratorsItems.HOHLRAUM.is(reactorSlot.resource())) {
            ResourceHandler<ChemicalResource> handler = AutomatedResourceHandler.manual(Capabilities.CHEMICAL.getCapability(reactorSlot.asItemAccess()));
            if (handler != null) {
                //Validate that the handler has some fusion fuel in it
                try (Transaction transaction = Transaction.openRoot()) {
                    ChemicalResource fuelType = GeneratorsChemicals.FUSION_FUEL.asResource();
                    int availableFuel = handler.extract(fuelType, fuelTank.getNeededAsInt(ChemicalResource.EMPTY), transaction);
                    if (availableFuel > 0 && fuelTank.insert(fuelType, availableFuel, transaction, AutomationType.INTERNAL) == availableFuel) {
                        lastPlasmaTemperature = getPlasmaTemp();
                        ContainerType.ITEM.clearContents(reactorSlot, transaction);
                        setBurning(true);
                        transaction.commit();
                    }
                }
            }
        }
    }

    private void injectFuel() {
        int amountNeeded = fuelTank.getNeededAsInt(ChemicalResource.EMPTY);
        int amountAvailable = 2 * Math.min(deuteriumTank.amountAsInt(), tritiumTank.amountAsInt());
        int amountToInject = Math.min(amountNeeded, Math.min(amountAvailable, injectionRate));
        amountToInject -= amountToInject % 2;
        int injectingAmount = amountToInject / 2;
        if (injectingAmount > 0) {
            try (Transaction transaction = Transaction.openRoot()) {
                //Note: We don't have to validate if the deuterium or tritium resources are empty, as if either is, then the injecting amount will be zero
                if (deuteriumTank.extract(deuteriumTank.resource(), injectingAmount, transaction, AutomationType.MANUAL) == injectingAmount &&
                    tritiumTank.extract(tritiumTank.resource(), injectingAmount, transaction, AutomationType.MANUAL) == injectingAmount &&
                    fuelTank.insert(GeneratorsChemicals.FUSION_FUEL.asResource(), amountToInject, transaction, AutomationType.MANUAL) == amountToInject) {
                    //Only inject if we actually are able to transfer the proper amounts
                    transaction.commit();
                }
            }
        }
    }

    private int burnFuel() {
        ChemicalResource fuel = fuelTank.resource();
        if (fuel.isEmpty()) {
            //Nothing to burn
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int fuelBurned = Math.clamp(MathUtils.clampToInt((lastPlasmaTemperature - burnTemperature) * burnRatio), 0, fuelTank.amountAsInt());
            int fuelUsed = fuelTank.extract(fuel, fuelBurned, transaction, AutomationType.INTERNAL);
            if (fuelUsed < fuelBurned) {//Failed to actually burn anything
                return 0;
            }
            plasmaJournal.incrementPlasmaTemperature(MathUtils.multiplyClamped(MekanismGeneratorsConfig.generators.energyPerFusionFuel.get(), fuelBurned) / (double) plasmaHeatCapacity, transaction);
            transaction.commit();
            return fuelBurned;
        }
    }

    private void transferHeat() {
        //Transfer from plasma to casing
        double plasmaCaseHeat = plasmaCaseConductivity * (plasmaTemperature - heatCapacitor.getTemperature());
        if (Math.abs(plasmaCaseHeat) > HeatAPI.EPSILON) {
            setPlasmaTemp(getPlasmaTemp() - plasmaCaseHeat / plasmaHeatCapacity);
            heatCapacitor.handleHeat(plasmaCaseHeat);
        }

        //Transfer from casing to water if necessary
        double caseWaterHeat = MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() * (heatCapacitor.getTemperature() - biomeAmbientTemp);
        double lostToWater = 0;
        if (Math.abs(caseWaterHeat) > HeatAPI.EPSILON) {
            int waterToVaporize = (int) (HeatUtils.getSteamEnergyEfficiency() * caseWaterHeat / HeatUtils.getWaterThermalEnthalpy());
            FluidResource water = waterTank.resource();
            if (!water.isEmpty()) {
                try (Transaction transaction = Transaction.openRoot()) {
                    ChemicalResource steam = MekanismChemicals.STEAM.asResource();
                    int vaporized = waterTank.extract(water, Math.min(waterToVaporize, steamTank.getNeededAsInt(steam)), transaction, AutomationType.INTERNAL);
                    if (vaporized > 0) {
                        //Note: We don't validate the full amount could be inserted as we allow venting the excess steam
                        steamTank.insert(steam, vaporized, transaction, AutomationType.INTERNAL);
                        lostToWater = vaporized * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
                        heatCapacitor.handleHeat(-lostToWater);
                        transaction.commit();
                    }
                }
            }
        }

        //HeatTransfer heatTransfer = simulate();
        //lastEnvironmentLoss = heatTransfer.environmentTransfer();
        //lastTransferLoss = heatTransfer.adjacentTransfer();
        lastTransferLoss = simulateAdjacent() + lostToWater;
        lastEnvironmentLoss = 0;

        //Passive energy generation
        double caseAirHeat = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get() * (heatCapacitor.getTemperature() - biomeAmbientTemp);
        if (Math.abs(caseAirHeat) > HeatAPI.EPSILON) {
            heatCapacitor.handleHeat(-caseAirHeat);
            lastEnvironmentLoss = caseAirHeat;
            try (Transaction transaction = Transaction.openRoot()) {
                energyContainer.insert(MathUtils.clampToInt(caseAirHeat * MekanismGeneratorsConfig.generators.fusionThermocoupleEfficiency.get()), transaction, AutomationType.INTERNAL);
                transaction.commit();
            }
        }
    }

    @Override
    public HeatTransfer simulate() {
        throw new UnsupportedOperationException("I'm special");
    }

    @Override
    public double simulateAdjacent() {
        double adjacentTransfer = 0;
        for (ITileHeatHandler source : heatHandlers) {
            adjacentTransfer += source.simulateAdjacent();
        }
        return adjacentTransfer;
    }

    @ComputerMethod(nameOverride = "getPlasmaTemperature")
    public double getLastPlasmaTemp() {
        return lastPlasmaTemperature;
    }

    @ComputerMethod(nameOverride = "getCaseTemperature")
    public double getLastCaseTemp() {
        return lastCaseTemperature;
    }

    public double getPlasmaTemp() {
        return plasmaTemperature;
    }

    public void setPlasmaTemp(double temp) {
        if (!Mth.equal(plasmaTemperature, temp)) {
            plasmaTemperature = temp;
            markDirty();
        }
    }

    @ComputerMethod
    public int getInjectionRate() {
        return injectionRate;
    }

    public void setInjectionRate(int rate) {
        if (injectionRate != rate) {
            injectionRate = rate;
            //TODO - 26.1: Should these configs be limited to ints?
            maxWater = injectionRate * MekanismGeneratorsConfig.generators.fusionWaterPerInjection.get();
            maxSteam = injectionRate * MekanismGeneratorsConfig.generators.fusionSteamPerInjection.get();
            if (!isRemote()) {
                ContainerType.FLUID.clampContents(waterTank, null);
                ContainerType.CHEMICAL.clampContents(steamTank, null);
            }
            markDirty();
        }
    }

    private long getMaxWater() {
        return maxWater;
    }

    private long getMaxSteam() {
        return maxSteam;
    }

    @ComputerMethod(nameOverride = "isIgnited", methodDescription = "Checks if a reaction is occurring.")
    public boolean isBurning() {
        return burning;
    }

    public void setBurning(boolean burn) {
        if (burning != burn) {
            burning = burn;
            markDirty();
        }
    }

    public double getCaseTemp() {
        return heatCapacitor.getTemperature();
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return ContainerType.CHEMICAL.getRedstoneSignalFromContainer(fuelTank);
    }

    @ComputerMethod(methodDescription = "true -> water cooled, false -> air cooled")
    public int getMinInjectionRate(boolean active) {
        double k = active ? MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() : 0;
        double caseAirConductivity = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
        double aMin = burnTemperature * burnRatio * plasmaCaseConductivity * (k + caseAirConductivity) /
                      (MekanismGeneratorsConfig.generators.energyPerFusionFuel.get() * burnRatio * (plasmaCaseConductivity + k + caseAirConductivity) -
                       plasmaCaseConductivity * (k + caseAirConductivity));
        return 2 * Mth.ceil(aMin / 2D);
    }

    @ComputerMethod(methodDescription = "true -> water cooled, false -> air cooled")
    public double getMaxPlasmaTemperature(boolean active) {
        double k = active ? MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() : 0;
        double caseAirConductivity = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
        int injectionRate = Math.max(this.injectionRate, lastBurned);
        return injectionRate * MekanismGeneratorsConfig.generators.energyPerFusionFuel.get() / plasmaCaseConductivity *
               (plasmaCaseConductivity + k + caseAirConductivity) / (k + caseAirConductivity);
    }

    @ComputerMethod(methodDescription = "true -> water cooled, false -> air cooled")
    public double getMaxCasingTemperature(boolean active) {
        double k = active ? MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() : 0;
        int injectionRate = Math.max(this.injectionRate, lastBurned);
        return MathUtils.multiplyClamped(MekanismGeneratorsConfig.generators.energyPerFusionFuel.get(), injectionRate)
               / (k + MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get());
    }

    @ComputerMethod(methodDescription = "true -> water cooled, false -> air cooled")
    public double getIgnitionTemperature(boolean active) {
        double k = active ? MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() : 0;
        double caseAirConductivity = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
        double energyPerFusionFuel = MekanismGeneratorsConfig.generators.energyPerFusionFuel.get();
        return burnTemperature * energyPerFusionFuel * burnRatio * (plasmaCaseConductivity + k + caseAirConductivity) /
               (energyPerFusionFuel * burnRatio * (plasmaCaseConductivity + k + caseAirConductivity) - plasmaCaseConductivity * (k + caseAirConductivity));
    }

    public long getPassiveGeneration(boolean active, boolean current) {
        double temperature = current ? getLastCaseTemp() : getMaxCasingTemperature(active);
        return MathUtils.clampToLong(MekanismGeneratorsConfig.generators.fusionThermocoupleEfficiency.get() *
                                     MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get() * temperature);
    }

    public int getSteamPerTick(boolean current) {
        double temperature = current ? getLastCaseTemp() : getMaxCasingTemperature(true);
        return MathUtils.clampToInt(HeatUtils.getSteamEnergyEfficiency() * MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() * temperature / HeatUtils.getWaterThermalEnthalpy());
    }

    private static double getInverseConductionCoefficient() {
        return 1 / MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
    }

    //Computer related methods
    @ComputerMethod(nameOverride = "setInjectionRate")
    void computerSetInjectionRate(int rate) throws ComputerException {
        if (rate < 0 || rate > MAX_INJECTION) {
            //Validate bounds even though we can clamp
            throw new ComputerException("Injection Rate '%d' is out of range must be an even number between 0 and %d. (Inclusive)", rate, MAX_INJECTION);
        } else if (rate % 2 != 0) {
            //Validate it is even
            throw new ComputerException("Injection Rate '%d' must be an even number between 0 and %d. (Inclusive)", rate, MAX_INJECTION);
        }
        setInjectionRate(rate);
    }

    @ComputerMethod
    long getPassiveGeneration(boolean active) {
        return getPassiveGeneration(active, false);
    }

    @ComputerMethod
    long getProductionRate() {
        return getPassiveGeneration(false, true);
    }
    //End computer related methods

    private class PlasmaJournal extends SnapshotJournal<Double> {

        private double temperatureDiff;

        public void incrementPlasmaTemperature(double temperatureDiff, TransactionContext transaction) {
            updateSnapshots(transaction);
            this.temperatureDiff += temperatureDiff;
        }

        @Override
        protected Double createSnapshot() {
            return temperatureDiff;
        }

        @Override
        protected void revertToSnapshot(Double snapshot) {
            temperatureDiff = snapshot;
        }

        @Override
        protected void onRootCommit(Double originalState) {
            super.onRootCommit(originalState);
            if (!Mth.equal(originalState, temperatureDiff)) {
                setPlasmaTemp(getPlasmaTemp() + temperatureDiff);
            }
        }
    }
}
