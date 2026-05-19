package mekanism.generators.common.content.fission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongSupplier;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.CooledCoolant;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.MathUtils;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerHeatCapacitorWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.ResourceUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.block.attribute.AttributeStateFissionPortMode.FissionPortMode;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.MekanismGeneratorsMultiblocks;
import mekanism.generators.common.content.fission.FissionReactorValidator.FormedAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorPort;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.TypedOutputList;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FissionReactorMultiblockData extends MultiblockData implements IValveHandler {

    private static final double INVERSE_INSULATION_COEFFICIENT = 10_000;
    private static final double INVERSE_CONDUCTION_COEFFICIENT = 10;

    private static final double waterConductivity = 0.5;

    public static final double MIN_DAMAGE_TEMPERATURE = 1_200;
    public static final double MAX_DAMAGE_TEMPERATURE = 1_800;
    public static final double MAX_DAMAGE = 100;

    private static final double EXPLOSION_CHANCE = 1D / 512_000;

    private final List<AdvancedCapabilityOutputTarget<ResourceHandler<ChemicalResource>, FissionPortMode>> chemicalOutputTargets = new ArrayList<>();
    public final Set<FormedAssembly> assemblies = new LinkedHashSet<>();
    private final List<IChemicalTank> inputTanks;
    private final List<IChemicalTank> outputWasteTanks;
    private final List<IChemicalTank> outputCoolantTanks;

    @ContainerSync(setter = "setAssemblies")
    @SyntheticComputerMethod(getter = "getFuelAssemblies")
    private int fuelAssemblies = 0;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getFuelSurfaceArea")
    public int surfaceArea;

    @ContainerSync
    public final MergedTank coolantTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getFuel", "getFuelCapacity", "getFuelNeeded",
                                                                                        "getFuelFilledPercentage"}, docPlaceholder = "fuel tank")
    public final IChemicalTank fuelTank;

    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getHeatedCoolant", "getHeatedCoolantCapacity", "getHeatedCoolantNeeded",
                                                                                        "getHeatedCoolantFilledPercentage"}, docPlaceholder = "heated coolant")
    public final IChemicalTank heatedCoolantTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getWaste", "getWasteCapacity", "getWasteNeeded",
                                                                                        "getWasteFilledPercentage"}, docPlaceholder = "waste tank")
    public final IChemicalTank wasteTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerHeatCapacitorWrapper.class, methodNames = "getTemperature", docPlaceholder = "reactor")
    public final VariableHeatCapacitor heatCapacitor;

    private double biomeAmbientTemp;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getEnvironmentalLoss")
    public double lastEnvironmentLoss = 0;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getHeatingRate")
    public int lastBoilRate = 0;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getActualBurnRate", getterDescription = "Actual burn rate as it may be lower if say there is not enough fuel")
    public double lastBurnRate = 0;
    private boolean clientBurning;
    @ContainerSync
    public double reactorDamage = 0;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getBurnRate", getterDescription = "Configured burn rate")
    public double rateLimit = MekanismGeneratorsConfig.generators.defaultBurnRate.get();
    public double burnRemaining = 0, partialWaste = 0;
    @ContainerSync
    private boolean active;
    //For use when meltdowns are disabled to make the reactor stop and require going under the threshold
    @ContainerSync
    private boolean forceDisable;

    private long cooledCoolantCapacity;
    private long heatedCoolantCapacity;
    private long fuelCapacity;

    private AABB hotZone;

    public float prevCoolantScale;
    private float prevFuelScale;
    public float prevHeatedCoolantScale;
    private float prevWasteScale;

    public FissionReactorMultiblockData(TileEntityFissionReactorCasing tile) {
        super(tile);
        //Default biome temp to the ambient temperature at the block we are at
        biomeAmbientTemp = HeatAPI.getAmbientTemp(tile.getLevel(), tile.getBlockPos());
        LongSupplier fuelCapacitySupplier = () -> fuelCapacity;
        coolantTank = MergedTank.create(
              VariableCapacityFluidTank.input(this, () -> cooledCoolantCapacity, fluid -> fluid.is(FluidTags.WATER), this),
              VariableCapacityChemicalTank.input(this, () -> cooledCoolantCapacity, BoilerMultiblockData.IS_COOLED_COOLANT, this)
        );
        fluidTanks.add(coolantTank.getFluidTank());
        fuelTank = VariableCapacityChemicalTank.input(this, fuelCapacitySupplier, chemical -> chemical.is(MekanismChemicals.FISSILE_FUEL),
              ChemicalAttributeValidator.ALWAYS_ALLOW, createSaveAndComparator());
        heatedCoolantTank = VariableCapacityChemicalTank.output(this, () -> heatedCoolantCapacity,
              chemical -> chemical.is(MekanismChemicals.STEAM) || BoilerMultiblockData.IS_HEATED_COOLANT.test(chemical), this);
        wasteTank = VariableCapacityChemicalTank.output(this, fuelCapacitySupplier, chemical -> chemical.is(MekanismChemicals.NUCLEAR_WASTE),
              ChemicalAttributeValidator.ALWAYS_ALLOW, this);
        inputTanks = List.of(fuelTank, coolantTank.getChemicalTank());
        outputWasteTanks = List.of(wasteTank);
        outputCoolantTanks = List.of(heatedCoolantTank);
        Collections.addAll(chemicalTanks, fuelTank, heatedCoolantTank, wasteTank, coolantTank.getChemicalTank());
        heatCapacitor = VariableHeatCapacitor.create(MekanismGeneratorsConfig.generators.fissionCasingHeatCapacity.get(),
              () -> INVERSE_CONDUCTION_COEFFICIENT, () -> INVERSE_INSULATION_COEFFICIENT, () -> biomeAmbientTemp, this);
        heatCapacitors.add(heatCapacitor);
    }

    @Override
    public void onCreated(Level world) {
        super.onCreated(world);
        biomeAmbientTemp = calculateAverageAmbientTemperature(world);
        // update the heat capacity now that we've read
        heatCapacitor.setHeatCapacity(MekanismGeneratorsConfig.generators.fissionCasingHeatCapacity.get() * locations.size(), true);
        hotZone = AABB.encapsulatingFullBlocks(getMinPos().offset(1, 1, 1), getMaxPos().offset(-1, -1, -1));
    }

    @Override
    public boolean tick(ServerLevel world) {
        boolean needsPacket = super.tick(world);
        // burn reactor fuel, create energy
        if (isActive()) {
            burnFuel(world);
        } else {
            lastBurnRate = 0;
        }
        if (isBurning() != clientBurning) {
            needsPacket = true;
            clientBurning = isBurning();
        }
        // handle coolant heating (water -> steam)
        handleCoolant();
        if (!chemicalOutputTargets.isEmpty()) {
            if (!heatedCoolantTank.isEmpty()) {
                ResourceUtils.emit(getActiveOutputs(chemicalOutputTargets, FissionPortMode.OUTPUT_COOLANT), heatedCoolantTank, null);
            }
            if (!wasteTank.isEmpty()) {
                ResourceUtils.emit(getActiveOutputs(chemicalOutputTargets, FissionPortMode.OUTPUT_WASTE), wasteTank, null);
            }
        }
        // external heat dissipation
        lastEnvironmentLoss = simulateEnvironment();
        // update temperature
        updateHeatCapacitors(null);
        handleDamage(world);
        radiateEntities(world);

        // update scales
        float coolantScale;
        if (coolantTank.getCurrentType() == CurrentType.CHEMICAL) {
            coolantScale = MekanismUtils.getScale(prevCoolantScale, coolantTank.getChemicalTank());
        } else {
            coolantScale = MekanismUtils.getScale(prevCoolantScale, coolantTank.getFluidTank());
        }
        float fuelScale = MekanismUtils.getScale(prevFuelScale, fuelTank);
        float steamScale = MekanismUtils.getScale(prevHeatedCoolantScale, heatedCoolantTank), wasteScale = MekanismUtils.getScale(prevWasteScale, wasteTank);
        if (MekanismUtils.scaleChanged(coolantScale, prevCoolantScale) || MekanismUtils.scaleChanged(fuelScale, prevFuelScale) ||
            MekanismUtils.scaleChanged(steamScale, prevHeatedCoolantScale) || MekanismUtils.scaleChanged(wasteScale, prevWasteScale)) {
            needsPacket = true;
            prevCoolantScale = coolantScale;
            prevFuelScale = fuelScale;
            prevHeatedCoolantScale = steamScale;
            prevWasteScale = wasteScale;
        }
        return needsPacket;
    }

    @Override
    protected void updateEjectors(Level world) {
        chemicalOutputTargets.clear();
        for (Map.Entry<BlockPos, ValveData> entry : valves.entrySet()) {
            TileEntityFissionReactorPort tile = WorldUtils.getTileEntity(TileEntityFissionReactorPort.class, world, entry.getKey());
            if (tile != null) {
                ValveData valve = entry.getValue();
                tile.addChemicalTargetCapability(chemicalOutputTargets, valve.side);
                valve.addTank(coolantTank.getFluidTank(), true);
            }
        }
    }

    @Override
    protected boolean hasFluidValveHandling() {
        return true;
    }

    public List<IChemicalTank> getChemicalTanks(FissionPortMode mode) {
        return switch (mode) {
            case INPUT -> inputTanks;
            case OUTPUT_WASTE -> outputWasteTanks;
            case OUTPUT_COOLANT -> outputCoolantTanks;
        };
    }

    @Override
    public double simulateEnvironment() {
        double invConduction = HeatAPI.AIR_INVERSE_COEFFICIENT + (INVERSE_INSULATION_COEFFICIENT + INVERSE_CONDUCTION_COEFFICIENT);
        double tempToTransfer = (heatCapacitor.getTemperature() - biomeAmbientTemp) / invConduction;
        heatCapacitor.handleHeat(-tempToTransfer * heatCapacitor.getHeatCapacity());
        return Math.max(tempToTransfer, 0);
    }

    @Override
    public void readUpdateTag(@NotNull ValueInput input) {
        super.readUpdateTag(input);
        prevCoolantScale = input.getFloatOr(SerializationConstants.SCALE, prevCoolantScale);
        prevFuelScale = input.getFloatOr(SerializationConstants.SCALE_ALT, prevFuelScale);
        prevHeatedCoolantScale = input.getFloatOr(SerializationConstants.SCALE_ALT_2, prevHeatedCoolantScale);
        prevWasteScale = input.getFloatOr(SerializationConstants.SCALE_ALT_3, prevWasteScale);
        input.getInt(SerializationConstants.VOLUME).ifPresent(this::setVolume);
        //TODO - 26.1: Should this be an orElse empty and then set it regardless?
        input.read(SerializationConstants.FLUID, SerializerHelper.OPTIONAL_FLUID_RESOURCE_STACK_CODEC).ifPresent(coolantTank.getFluidTank()::setContentsUnchecked);
        input.read(SerializationConstants.CHEMICAL, SerializerHelper.OPTIONAL_CHEMICAL_RESOURCE_STACK_CODEC).ifPresent(fuelTank::setContentsUnchecked);
        input.read(SerializationConstants.CHEMICAL_STORED_ALT, SerializerHelper.OPTIONAL_CHEMICAL_RESOURCE_STACK_CODEC).ifPresent(heatedCoolantTank::setContentsUnchecked);
        input.read(SerializationConstants.CHEMICAL_STORED_ALT_2, SerializerHelper.OPTIONAL_CHEMICAL_RESOURCE_STACK_CODEC).ifPresent(wasteTank::setContentsUnchecked);
        readValves(input);
        assemblies.clear();
        for (FormedAssembly assembly : input.listOrEmpty(SerializationConstants.ASSEMBLIES, FormedAssembly.CODEC)) {
            assemblies.add(assembly);
        }
    }

    @Override
    public void writeUpdateTag(@NotNull ValueOutput output) {
        super.writeUpdateTag(output);
        output.putFloat(SerializationConstants.SCALE, prevCoolantScale);
        output.putFloat(SerializationConstants.SCALE_ALT, prevFuelScale);
        output.putFloat(SerializationConstants.SCALE_ALT_2, prevHeatedCoolantScale);
        output.putFloat(SerializationConstants.SCALE_ALT_3, prevWasteScale);
        output.putInt(SerializationConstants.VOLUME, getVolume());
        output.store(SerializationConstants.FLUID, SerializerHelper.OPTIONAL_FLUID_RESOURCE_STACK_CODEC, coolantTank.getFluidTank().asStack());
        output.store(SerializationConstants.CHEMICAL, SerializerHelper.OPTIONAL_CHEMICAL_RESOURCE_STACK_CODEC, fuelTank.asStack());
        output.store(SerializationConstants.CHEMICAL_STORED_ALT, SerializerHelper.OPTIONAL_CHEMICAL_RESOURCE_STACK_CODEC, heatedCoolantTank.asStack());
        output.store(SerializationConstants.CHEMICAL_STORED_ALT_2, SerializerHelper.OPTIONAL_CHEMICAL_RESOURCE_STACK_CODEC, wasteTank.asStack());
        writeValves(output);
        if (!assemblies.isEmpty()) {
            TypedOutputList<FormedAssembly> serializedAssemblies = output.list(SerializationConstants.ASSEMBLIES, FormedAssembly.CODEC);
            for (FormedAssembly assembly : assemblies) {
                serializedAssemblies.add(assembly);
            }
        }
    }

    private void handleDamage(Level world) {
        double lastDamage = reactorDamage;
        double temp = heatCapacitor.getTemperature();
        if (temp > MIN_DAMAGE_TEMPERATURE) {
            double damageRate = Math.min(temp, MAX_DAMAGE_TEMPERATURE) / (MIN_DAMAGE_TEMPERATURE * 10);
            reactorDamage += damageRate;
        } else {
            double repairRate = (MIN_DAMAGE_TEMPERATURE - temp) / (MIN_DAMAGE_TEMPERATURE * 100);
            reactorDamage = Math.max(0, reactorDamage - repairRate);
        }
        // consider a meltdown only if we're passed the damage threshold and the temperature is still dangerous
        if (reactorDamage >= MAX_DAMAGE && temp >= MIN_DAMAGE_TEMPERATURE) {
            if (isForceDisabled() && MekanismGeneratorsConfig.generators.fissionMeltdownsEnabled.get()) {
                //If we have meltdowns enabled, and we would have had one before, but they were disabled, just meltdown immediately
                // if we still meet the requirements for a meltdown
                setForceDisable(false);
                createMeltdown(world);
            } else if (world.getRandom().nextDouble() < (reactorDamage / MAX_DAMAGE) * MekanismGeneratorsConfig.generators.fissionMeltdownChance.get()) {
                // Otherwise, if our chance is hit either create a meltdown if it is enabled in the config, or force disable the reactor
                if (MekanismGeneratorsConfig.generators.fissionMeltdownsEnabled.get()) {
                    createMeltdown(world);
                } else {
                    setForceDisable(true);
                }
            }
        } else if (reactorDamage < MAX_DAMAGE && temp < MIN_DAMAGE_TEMPERATURE) {
            //If we are at a safe temperature and damage level, allow enabling the reactor again
            setForceDisable(false);
        }
        if (reactorDamage != lastDamage) {
            markDirty();
        }
    }

    private void createMeltdown(Level world) {
        float radius = MekanismGeneratorsConfig.generators.fissionMeltdownRadius.get();
        world.getData(MekanismAttachmentTypes.MELTDOWN_DATA).createMeltdown(getBounds(), heatCapacitor.getHeat(), EXPLOSION_CHANCE, radius, inventoryID);
    }

    @Override
    public void meltdownHappened(Level world) {
        if (isFormed()) {
            if (RadiationManager.isGlobalRadiationEnabled()) {
                //Calculate radiation level and clear any tanks that had radioactive substances and are contributing to the
                // amount of radiation released
                double radiation = getTankRadioactivityAndDump(fuelTank) + getWasteTankRadioactivity(true) +
                                   getTankRadioactivityAndDump(coolantTank.getChemicalTank()) + getTankRadioactivityAndDump(heatedCoolantTank);
                radiation *= MekanismGeneratorsConfig.generators.fissionMeltdownRadiationMultiplier.get();
                //When the meltdown actually happens, release radiation into the atmosphere
                IRadiationManager.INSTANCE.radiate(world, getBounds().getCenter(), radiation);
            }
            //Dump the heated coolant as "loss" that didn't survive the meltdown
            heatedCoolantTank.setEmpty();
            //Disable the reactor so that if the person rebuilds it, it isn't on by default (QoL)
            active = false;
            //Update reactor damage to the specified level for post meltdown
            reactorDamage = MekanismGeneratorsConfig.generators.fissionPostMeltdownDamage.get();
            //Reset burnRemaining to zero as it is reasonable to have the burnRemaining get wasted when the reactor explodes
            burnRemaining = 0;
            //Reset the partial waste as we just irradiated it and there is not much sense having it exist in limbo
            partialWaste = 0;
            //Reset the heat to the default of the heat capacitor
            heatCapacitor.setHeat(heatCapacitor.getHeatCapacity() * biomeAmbientTemp);
            //Force sync the update to the cache that corresponds to this multiblock
            MultiblockCache<FissionReactorMultiblockData> cache = MultiblockManager.get(getLevel(), MekanismGeneratorsMultiblocks.FISSION_REACTOR).getCache(inventoryID);
            if (cache != null) {
                cache.sync(this);
            }
        }
    }

    /**
     * @apiNote Assumes radiation is enabled instead of checking and returning zero if it is not.
     */
    private double getWasteTankRadioactivity(boolean dump) {
        ChemicalResource wasteType = wasteTank.getResource();
        double wasteRadioactivity;
        if (wasteType.isEmpty()) {
            wasteRadioactivity = MekanismChemicals.NUCLEAR_WASTE.get().getRadioactivity();
        } else {
            //Note: We need to know the baseline radioactivity, and not the scaled amount. So we get it from the chemical
            // instead of directly off the stack
            wasteRadioactivity = wasteType.getRadioactivity();
        }
        if (wasteRadioactivity == 0) {
            return 0;
        }
        long stored = wasteTank.amountAsLong();
        if (dump) {
            //If we want to dump if we have a radioactive substance, then we need to set the tank to empty
            wasteTank.setEmpty();
        }
        return wasteRadioactivity * (stored + partialWaste);
    }

    /**
     * @apiNote Assumes radiation is enabled instead of checking and returning zero if it is not.
     */
    private double getTankRadioactivityAndDump(IChemicalTank tank) {
        if (!tank.isEmpty()) {
            double radioactivity = tank.getResource().getRadioactivity() * tank.amountAsLong();
            if (radioactivity > 0) {
                //If we have a radioactive substance, then we need to set the tank to empty
                tank.setEmpty();
                return radioactivity;
            }
        }
        return 0;
    }

    @Nullable
    private CooledCoolant getCooledCoolant(ChemicalResource resource) {
        return resource.isEmpty() ? null : resource.getData(IMekanismDataMapTypes.INSTANCE.cooledChemicalCoolant());
    }

    private void handleCoolant() {
        CurrentType currentType = this.coolantTank.getCurrentType();
        if (currentType == CurrentType.EMPTY) {
            lastBoilRate = 0;
            return;
        }
        double heat = getBoilEfficiency() * (heatCapacitor.getHeat() - HeatUtils.BASE_BOIL_TEMP * heatCapacitor.getHeatCapacity());
        double coolantEnthalpy;
        double coolantConductivity;
        IResourceContainer<?> coolantTank;
        ChemicalResource heatedCoolant;
        if (currentType == CurrentType.FLUID) {
            coolantTank = this.coolantTank.getFluidTank();
            coolantConductivity = waterConductivity;
            coolantEnthalpy = HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
            heatedCoolant = MekanismChemicals.STEAM.asResource();
        } else {//if (currentType == CurrentType.CHEMICAL)
            IChemicalTank chemicalCoolantTank = this.coolantTank.getChemicalTank();
            ChemicalResource coolant = chemicalCoolantTank.getResource();
            CooledCoolant coolantType = getCooledCoolant(coolant);
            if (coolantType == null) {
                lastBoilRate = 0;
                return;
            }
            coolantTank = chemicalCoolantTank;
            coolantEnthalpy = coolantType.thermalEnthalpy();
            coolantConductivity = coolantType.conductivity();
            heatedCoolant = coolantType.heat();
        }
        double caseCoolantHeat = heat * coolantConductivity;
        lastBoilRate = clampCoolantHeated(caseCoolantHeat / coolantEnthalpy, coolantTank.amountAsInt());
        if (lastBoilRate > 0) {
            try (Transaction transaction = Transaction.openRoot()) {
                //Note: The fluid resource should not be empty here
                if (!tryExtractCoolant(coolantTank, lastBoilRate, transaction)) {// extra steam is dumped
                    heatedCoolantTank.insert(heatedCoolant, lastBoilRate, transaction, AutomationType.INTERNAL);
                    caseCoolantHeat = lastBoilRate * coolantEnthalpy;
                    heatCapacitor.handleHeat(-caseCoolantHeat);
                    transaction.commit();
                } else {
                    //Failed to actually boil
                    lastBoilRate = 0;
                }
            }
        }
    }

    private <RESOURCE extends Resource> boolean tryExtractCoolant(IResourceContainer<RESOURCE> tank, int toBoil, TransactionContext transaction) {
        return tank.extract(tank.getResource(), toBoil, transaction, AutomationType.INTERNAL) != toBoil;
    }

    private int clampCoolantHeated(double heated, int stored) {
        return Mth.clamp(MathUtils.clampToInt(heated), 0, stored);
    }

    private void burnFuel(Level world) {
        double lastPartialWaste = partialWaste;
        double lastBurnRemaining = burnRemaining;
        double storedFuel = fuelTank.amountAsLong() + burnRemaining;
        double toBurn = Math.min(Math.min(rateLimit, storedFuel), fuelAssemblies * MekanismGeneratorsConfig.generators.burnPerAssembly.get());
        storedFuel -= toBurn;
        ChemicalResource fuel = fuelTank.getResource();
        //TODO - 26.1: Re-evaluate this.. it seems weird
        fuelTank.setContentsUnchecked(fuel, Math.min(MathUtils.clampToLong(storedFuel), fuelTank.capacityAsLong(fuel)));
        burnRemaining = storedFuel % 1;
        heatCapacitor.handleHeat(toBurn * MekanismGeneratorsConfig.generators.energyPerFissionFuel.get());
        // handle waste
        partialWaste += toBurn;
        //TODO - 26.1: Check what the old max for partialWaste was, and if it is safe for us to switch from lfloor to floor or if we need to adjust other things as well
        int newWaste = Mth.floor(partialWaste);
        if (newWaste > 0) {
            partialWaste %= 1;
            ChemicalResource waste = MekanismChemicals.NUCLEAR_WASTE.asResource();
            try (Transaction transaction = Transaction.openRoot()) {
                newWaste -= wasteTank.insert(waste, newWaste, transaction, AutomationType.INTERNAL);
                transaction.commit();
            }
            if (newWaste > 0 && RadiationManager.isGlobalRadiationEnabled()) {
                //Check if radiation is enabled in order to allow for short-circuiting when it will NO-OP further down the line anyway
                //Note: We query the radioactivity from the chemical instead of the stack so that we don't multiply it by the stack's size
                double wasteRadioactivity = waste.getRadioactivity();
                if (wasteRadioactivity > 0) {
                    IRadiationManager.INSTANCE.radiate(world, getBounds().getCenter(), newWaste * wasteRadioactivity);
                }
            }
        }
        // update previous burn
        lastBurnRate = toBurn;
        if (lastPartialWaste != partialWaste || lastBurnRemaining != burnRemaining) {
            markDirty();
        }
    }

    private void radiateEntities(Level world) {
        if (RadiationManager.isGlobalRadiationEnabled() && isBurning() && world.getRandom().nextInt() % SharedConstants.TICKS_PER_SECOND == 0) {
            double wasteRadiation = getWasteTankRadioactivity(false) / 3_600F; // divide down to Sv/s
            double magnitude = lastBurnRate + wasteRadiation;
            if (magnitude <= IRadiationManager.INSTANCE.baselineRadiation()) {
                return;
            }
            List<LivingEntity> entitiesToRadiate = getLevel().getEntitiesOfClass(LivingEntity.class, hotZone);
            if (!entitiesToRadiate.isEmpty()) {
                IRadiationManager radiationManager = IRadiationManager.INSTANCE;
                for (LivingEntity entity : entitiesToRadiate) {
                    radiationManager.radiate(entity, magnitude);
                }
            }
        }
    }

    void setForceDisable(boolean forceDisable) {
        if (this.forceDisable != forceDisable) {
            this.forceDisable = forceDisable;
            markDirty();
            if (this.forceDisable) {
                //If we are force disabling it, deactivate the reactor
                setActive(false);
            }
        }
    }

    @ComputerMethod
    public boolean isForceDisabled() {
        return forceDisable;
    }

    @ComputerMethod(nameOverride = "getStatus", methodDescription = "true -> active, false -> off")
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        //Don't allow setting it to active if we are forcibly disabled
        if (this.active != active && (!active || !isForceDisabled())) {
            this.active = active;
            markDirty();
        }
    }

    public boolean isBurning() {
        return lastBurnRate > 0;
    }

    public boolean handlesSound(TileEntityFissionReactorCasing tile) {
        return getBounds().isOnCorner(tile.getBlockPos());
    }

    @ComputerMethod
    public double getBoilEfficiency() {
        if (fuelAssemblies == 0) {
            //If for some reason the assemblies somehow haven't been initialized (even though they have to be to form)
            // just return that it can't boil
            return 0;
        }
        double avgSurfaceArea = surfaceArea / (double) fuelAssemblies;
        return Math.min(1, avgSurfaceArea / MekanismGeneratorsConfig.generators.fissionSurfaceAreaTarget.get());
    }

    @ComputerMethod
    public long getMaxBurnRate() {
        return fuelAssemblies * MekanismGeneratorsConfig.generators.burnPerAssembly.get();
    }

    @ComputerMethod
    public long getDamagePercent() {
        return Math.round((reactorDamage / MAX_DAMAGE) * 100);
    }

    public void setAssemblies(int assemblies) {
        if (this.fuelAssemblies != assemblies) {
            this.fuelAssemblies = assemblies;
            this.fuelCapacity = assemblies * MekanismGeneratorsConfig.generators.maxFuelPerAssembly.get();
        }
    }

    @Override
    public void setVolume(int volume) {
        if (getVolume() != volume) {
            super.setVolume(volume);
            cooledCoolantCapacity = volume * MekanismGeneratorsConfig.generators.fissionCooledCoolantPerTank.get();
            heatedCoolantCapacity = volume * MekanismGeneratorsConfig.generators.fissionHeatedCoolantPerTank.get();
        }
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return ResourceUtils.getRedstoneSignalFromContainer(fuelTank);
    }

    public void setRateLimit(double rate) {
        rate = Mth.clamp(rate, 0, getMaxBurnRate());
        if (rateLimit != rate) {
            rateLimit = rate;
            markDirty();
        }
    }

    //Computer related methods
    @ComputerMethod(methodDescription = "Must be disabled, and if meltdowns are disabled must not have been force disabled")
    void activate() throws ComputerException {
        if (isActive()) {
            throw new ComputerException("Reactor is already active.");
        } else if (isForceDisabled()) {
            throw new ComputerException("Reactor must reach safe damage and temperature levels before it can be reactivated.");
        }
        setActive(true);
    }

    @ComputerMethod(methodDescription = "Must be enabled")
    void scram() throws ComputerException {
        if (!isActive()) {
            throw new ComputerException("Scram requires the reactor to be active.");
        }
        setActive(false);
    }

    @ComputerMethod
    void setBurnRate(double rate) throws ComputerException {
        //Round to two decimal places
        rate = UnitDisplayUtils.roundDecimals(rate);
        long max = getMaxBurnRate();
        if (rate < 0 || rate > max) {
            //Validate bounds even though we can clamp
            throw new ComputerException("Burn Rate '%.2f' is out of range must be between 0 and %d. (Inclusive)", rate, max);
        }
        setRateLimit(rate);
    }

    @ComputerMethod
    LargeResourceStack<?> getCoolant() {
        if (coolantTank.getCurrentType() == CurrentType.CHEMICAL) {
            return coolantTank.getChemicalTank().asStack();
        }
        return coolantTank.getFluidTank().asStack();
    }

    @ComputerMethod
    long getCoolantCapacity() {
        if (coolantTank.getCurrentType() == CurrentType.CHEMICAL) {
            //TODO - 26.1: Should this be current limit or absolute limit
            IChemicalTank chemicalTank = coolantTank.getChemicalTank();
            return chemicalTank.capacityAsLong(chemicalTank.getResource());
        }
        //TODO - 26.1: Should this be current limit or absolute limit
        IFluidTank fluidTank = coolantTank.getFluidTank();
        return fluidTank.capacityAsLong(fluidTank.getResource());
    }

    @ComputerMethod
    long getCoolantNeeded() {
        if (coolantTank.getCurrentType() == CurrentType.CHEMICAL) {
            IChemicalTank chemicalTank = coolantTank.getChemicalTank();
            return chemicalTank.getNeededAsLong(chemicalTank.getResource());
        }
        IFluidTank fluidTank = coolantTank.getFluidTank();
        return fluidTank.getNeededAsLong(fluidTank.getResource());
    }

    @ComputerMethod
    double getCoolantFilledPercentage() {
        if (coolantTank.getCurrentType() == CurrentType.CHEMICAL) {
            IChemicalTank chemicalTank = coolantTank.getChemicalTank();
            return chemicalTank.amountAsLong() / (double) chemicalTank.capacityAsLong(chemicalTank.getResource());
        }
        IFluidTank fluidTank = coolantTank.getFluidTank();
        return fluidTank.amountAsLong() / (double) fluidTank.capacityAsLong(fluidTank.getResource());
    }

    @ComputerMethod
    double getHeatCapacity() {
        return heatCapacitor.getHeatCapacity();
    }
    //End computer related methods
}
