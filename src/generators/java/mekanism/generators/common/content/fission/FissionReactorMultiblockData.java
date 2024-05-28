package mekanism.generators.common.content.fission;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.LongSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes.CooledCoolant;
import mekanism.api.chemical.gas.attribute.GasAttributes.HeatedCoolant;
import mekanism.api.chemical.gas.attribute.GasAttributes.Radiation;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.MathUtils;
import mekanism.api.radiation.IRadiationManager;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
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
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.attribute.AttributeStateFissionPortMode.FissionPortMode;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.fission.FissionReactorValidator.FormedAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorPort;
import net.minecraft.SharedConstants;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;

public class FissionReactorMultiblockData extends MultiblockData implements IValveHandler {

    private static final double INVERSE_INSULATION_COEFFICIENT = 10_000;
    private static final double INVERSE_CONDUCTION_COEFFICIENT = 10;

    private static final double waterConductivity = 0.5;

    public static final double MIN_DAMAGE_TEMPERATURE = 1_200;
    public static final double MAX_DAMAGE_TEMPERATURE = 1_800;
    public static final double MAX_DAMAGE = 100;

    private static final double EXPLOSION_CHANCE = 1D / 512_000;

    private final List<AdvancedCapabilityOutputTarget<IGasHandler, FissionPortMode>> gasOutputTargets = new ArrayList<>();
    public final Set<FormedAssembly> assemblies = new LinkedHashSet<>();
    private final List<IGasTank> inputTanks;
    private final List<IGasTank> outputWasteTanks;
    private final List<IGasTank> outputCoolantTanks;

    @ContainerSync(setter = "setAssemblies")
    @SyntheticComputerMethod(getter = "getFuelAssemblies")
    private int fuelAssemblies = 0;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getFuelSurfaceArea")
    public int surfaceArea;

    @ContainerSync
    public IGasTank gasCoolantTank;
    @ContainerSync
    public VariableCapacityFluidTank fluidCoolantTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getFuel", "getFuelCapacity", "getFuelNeeded",
                                                                                        "getFuelFilledPercentage"}, docPlaceholder = "fuel tank")
    public IGasTank fuelTank;

    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getHeatedCoolant", "getHeatedCoolantCapacity", "getHeatedCoolantNeeded",
                                                                                        "getHeatedCoolantFilledPercentage"}, docPlaceholder = "heated coolant")
    public IGasTank heatedCoolantTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getWaste", "getWasteCapacity", "getWasteNeeded",
                                                                                        "getWasteFilledPercentage"}, docPlaceholder = "waste tank")
    public IGasTank wasteTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerHeatCapacitorWrapper.class, methodNames = "getTemperature", docPlaceholder = "reactor")
    public VariableHeatCapacitor heatCapacitor;

    private double biomeAmbientTemp;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getEnvironmentalLoss")
    public double lastEnvironmentLoss = 0;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getHeatingRate")
    public long lastBoilRate = 0;
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

    private int cooledCoolantCapacity;
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
        fluidCoolantTank = VariableCapacityFluidTank.input(this, () -> cooledCoolantCapacity,
              fluid -> fluid.is(FluidTags.WATER) && gasCoolantTank.isEmpty(), this);
        fluidTanks.add(fluidCoolantTank);
        gasCoolantTank = MultiblockChemicalTankBuilder.GAS.input(this, () -> cooledCoolantCapacity,
              gas -> gas.has(CooledCoolant.class) && fluidCoolantTank.isEmpty(), this);
        fuelTank = MultiblockChemicalTankBuilder.GAS.input(this, fuelCapacitySupplier, gas -> gas == MekanismGases.FISSILE_FUEL.getChemical(),
              ChemicalAttributeValidator.ALWAYS_ALLOW, createSaveAndComparator());
        heatedCoolantTank = MultiblockChemicalTankBuilder.GAS.output(this, () -> heatedCoolantCapacity,
              gas -> gas == MekanismGases.STEAM.get() || gas.has(HeatedCoolant.class), this);
        wasteTank = MultiblockChemicalTankBuilder.GAS.output(this, fuelCapacitySupplier,
              gas -> gas == MekanismGases.NUCLEAR_WASTE.getChemical(), ChemicalAttributeValidator.ALWAYS_ALLOW, this);
        inputTanks = List.of(fuelTank, gasCoolantTank);
        outputWasteTanks = List.of(wasteTank);
        outputCoolantTanks = List.of(heatedCoolantTank);
        Collections.addAll(gasTanks, fuelTank, heatedCoolantTank, wasteTank, gasCoolantTank);
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
    public boolean tick(Level world) {
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
        if (!gasOutputTargets.isEmpty()) {
            if (!heatedCoolantTank.isEmpty()) {
                ChemicalUtil.emit(getActiveOutputs(gasOutputTargets, FissionPortMode.OUTPUT_COOLANT), heatedCoolantTank);
            }
            if (!wasteTank.isEmpty()) {
                ChemicalUtil.emit(getActiveOutputs(gasOutputTargets, FissionPortMode.OUTPUT_WASTE), wasteTank);
            }
        }
        // external heat dissipation
        lastEnvironmentLoss = simulateEnvironment();
        // update temperature
        updateHeatCapacitors(null);
        handleDamage(world);
        radiateEntities(world);

        // update scales
        float coolantScale = MekanismUtils.getScale(prevCoolantScale, fluidCoolantTank);
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
        gasOutputTargets.clear();
        for (ValveData valve : valves) {
            TileEntityFissionReactorPort tile = WorldUtils.getTileEntity(TileEntityFissionReactorPort.class, world, valve.location);
            if (tile != null) {
                tile.addGasTargetCapability(gasOutputTargets, valve.side);
            }
        }
    }

    public List<IGasTank> getGasTanks(FissionPortMode mode) {
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
    public void readUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.readUpdateTag(tag, provider);
        NBTUtils.setFloatIfPresent(tag, SerializationConstants.SCALE, scale -> prevCoolantScale = scale);
        NBTUtils.setFloatIfPresent(tag, SerializationConstants.SCALE_ALT, scale -> prevFuelScale = scale);
        NBTUtils.setFloatIfPresent(tag, SerializationConstants.SCALE_ALT_2, scale -> prevHeatedCoolantScale = scale);
        NBTUtils.setFloatIfPresent(tag, SerializationConstants.SCALE_ALT_3, scale -> prevWasteScale = scale);
        NBTUtils.setIntIfPresent(tag, SerializationConstants.VOLUME, this::setVolume);
        NBTUtils.setFluidStackIfPresent(provider, tag, SerializationConstants.FLUID, value -> fluidCoolantTank.setStack(value));
        NBTUtils.setGasStackIfPresent(provider, tag, SerializationConstants.GAS, value -> fuelTank.setStack(value));
        NBTUtils.setGasStackIfPresent(provider, tag, SerializationConstants.GAS_STORED_ALT, value -> heatedCoolantTank.setStack(value));
        NBTUtils.setGasStackIfPresent(provider, tag, SerializationConstants.GAS_STORED_ALT_2, value -> wasteTank.setStack(value));
        readValves(tag);
        assemblies.clear();
        if (tag.contains(SerializationConstants.ASSEMBLIES, Tag.TAG_LIST)) {
            ListTag list = tag.getList(SerializationConstants.ASSEMBLIES, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                FormedAssembly assembly = FormedAssembly.read(list.getCompound(i));
                if (assembly != null) {
                    assemblies.add(assembly);
                }
            }
        }
    }

    @Override
    public void writeUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeUpdateTag(tag, provider);
        tag.putFloat(SerializationConstants.SCALE, prevCoolantScale);
        tag.putFloat(SerializationConstants.SCALE_ALT, prevFuelScale);
        tag.putFloat(SerializationConstants.SCALE_ALT_2, prevHeatedCoolantScale);
        tag.putFloat(SerializationConstants.SCALE_ALT_3, prevWasteScale);
        tag.putInt(SerializationConstants.VOLUME, getVolume());
        tag.put(SerializationConstants.FLUID, fluidCoolantTank.getFluid().saveOptional(provider));
        tag.put(SerializationConstants.GAS, fuelTank.getStack().saveOptional(provider));
        tag.put(SerializationConstants.GAS_STORED_ALT, heatedCoolantTank.getStack().saveOptional(provider));
        tag.put(SerializationConstants.GAS_STORED_ALT_2, wasteTank.getStack().saveOptional(provider));
        writeValves(tag);
        ListTag list = new ListTag();
        for (FormedAssembly assembly : assemblies) {
            list.add(assembly.write());
        }
        tag.put(SerializationConstants.ASSEMBLIES, list);
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
            } else if (world.random.nextDouble() < (reactorDamage / MAX_DAMAGE) * MekanismGeneratorsConfig.generators.fissionMeltdownChance.get()) {
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
        RadiationManager.get().createMeltdown(world, getMinPos(), getMaxPos(), heatCapacitor.getHeat(), EXPLOSION_CHANCE,
              MekanismGeneratorsConfig.generators.fissionMeltdownRadius.get(), inventoryID);
    }

    @Override
    public void meltdownHappened(Level world) {
        if (isFormed()) {
            IRadiationManager radiationManager = IRadiationManager.INSTANCE;
            if (radiationManager.isRadiationEnabled()) {
                //Calculate radiation level and clear any tanks that had radioactive substances and are contributing to the
                // amount of radiation released
                double radiation = getTankRadioactivityAndDump(fuelTank) + getWasteTankRadioactivity(true) +
                                   getTankRadioactivityAndDump(gasCoolantTank) + getTankRadioactivityAndDump(heatedCoolantTank);
                radiation *= MekanismGeneratorsConfig.generators.fissionMeltdownRadiationMultiplier.get();
                //When the meltdown actually happens, release radiation into the atmosphere
                radiationManager.radiate(GlobalPos.of(world.dimension(), getBounds().getCenter()), radiation);
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
            MultiblockCache<FissionReactorMultiblockData> cache = MekanismGenerators.fissionReactorManager.getCache(inventoryID);
            if (cache != null) {
                cache.sync(this);
            }
        }
    }

    /**
     * @apiNote Assumes radiation is enabled instead of checking and returning zero if it is not.
     */
    private double getWasteTankRadioactivity(boolean dump) {
        GasStack stored = wasteTank.getStack();
        Radiation attribute;
        if (stored.isEmpty()) {
            attribute = MekanismGases.NUCLEAR_WASTE.get().get(Radiation.class);
        } else {
            attribute = stored.get(Radiation.class);
        }
        if (attribute == null) {
            return 0;
        } else if (dump) {
            //If we want to dump if we have a radioactive substance, then we need to set the tank to empty
            wasteTank.setEmpty();
        }
        return (stored.getAmount() + partialWaste) * attribute.getRadioactivity();
    }

    /**
     * @apiNote Assumes radiation is enabled instead of checking and returning zero if it is not.
     */
    private double getTankRadioactivityAndDump(IGasTank tank) {
        if (!tank.isEmpty()) {
            GasStack stored = tank.getStack();
            Radiation attribute = stored.get(Radiation.class);
            if (attribute != null) {
                //If we have a radioactive substance, then we need to set the tank to empty
                tank.setEmpty();
                return stored.getAmount() * attribute.getRadioactivity();
            }
        }
        return 0;
    }

    private void handleCoolant() {
        double temp = heatCapacitor.getTemperature();
        double heat = getBoilEfficiency() * (temp - HeatUtils.BASE_BOIL_TEMP) * heatCapacitor.getHeatCapacity();

        if (!fluidCoolantTank.isEmpty()) {
            double caseCoolantHeat = heat * waterConductivity;
            lastBoilRate = clampCoolantHeated(HeatUtils.getSteamEnergyEfficiency() * caseCoolantHeat / HeatUtils.getWaterThermalEnthalpy(),
                  fluidCoolantTank.getFluidAmount());
            if (lastBoilRate > 0) {
                MekanismUtils.logMismatchedStackSize(fluidCoolantTank.shrinkStack((int) lastBoilRate, Action.EXECUTE), lastBoilRate);
                // extra steam is dumped
                heatedCoolantTank.insert(MekanismGases.STEAM.getStack(lastBoilRate), Action.EXECUTE, AutomationType.INTERNAL);
                caseCoolantHeat = lastBoilRate * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
                heatCapacitor.handleHeat(-caseCoolantHeat);
            }
        } else if (!gasCoolantTank.isEmpty()) {
            CooledCoolant coolantType = gasCoolantTank.getStack().get(CooledCoolant.class);
            if (coolantType != null) {
                double caseCoolantHeat = heat * coolantType.getConductivity();
                lastBoilRate = clampCoolantHeated(caseCoolantHeat / coolantType.getThermalEnthalpy(), gasCoolantTank.getStored());
                if (lastBoilRate > 0) {
                    MekanismUtils.logMismatchedStackSize(gasCoolantTank.shrinkStack(lastBoilRate, Action.EXECUTE), lastBoilRate);
                    heatedCoolantTank.insert(coolantType.getHeatedGas().getStack(lastBoilRate), Action.EXECUTE, AutomationType.INTERNAL);
                    caseCoolantHeat = lastBoilRate * coolantType.getThermalEnthalpy();
                    heatCapacitor.handleHeat(-caseCoolantHeat);
                }
            }
        }
    }

    private long clampCoolantHeated(double heated, long stored) {
        long heatedLong = MathUtils.clampToLong(heated);
        if (heatedLong < 0) {
            return 0;
        } else if (heatedLong > stored) {
            return stored;
        }
        return heatedLong;
    }

    private void burnFuel(Level world) {
        double lastPartialWaste = partialWaste;
        double lastBurnRemaining = burnRemaining;
        double storedFuel = fuelTank.getStored() + burnRemaining;
        double toBurn = Math.min(Math.min(rateLimit, storedFuel), fuelAssemblies * MekanismGeneratorsConfig.generators.burnPerAssembly.get());
        storedFuel -= toBurn;
        fuelTank.setStackSize((long) storedFuel, Action.EXECUTE);
        burnRemaining = storedFuel % 1;
        heatCapacitor.handleHeat(toBurn * MekanismGeneratorsConfig.generators.energyPerFissionFuel.get().doubleValue());
        // handle waste
        partialWaste += toBurn;
        long newWaste = Mth.lfloor(partialWaste);
        if (newWaste > 0) {
            partialWaste %= 1;
            long leftoverWaste = Math.max(0, newWaste - wasteTank.getNeeded());
            GasStack wasteToAdd = MekanismGases.NUCLEAR_WASTE.getStack(newWaste);
            wasteTank.insert(wasteToAdd, Action.EXECUTE, AutomationType.INTERNAL);
            if (leftoverWaste > 0 && IRadiationManager.INSTANCE.isRadiationEnabled()) {
                //Check if radiation is enabled in order to allow for short-circuiting when it will NO-OP further down the line anyway
                Radiation attribute = wasteToAdd.get(Radiation.class);
                if (attribute != null) {
                    IRadiationManager.INSTANCE.radiate(GlobalPos.of(world.dimension(), getBounds().getCenter()), leftoverWaste * attribute.getRadioactivity());
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
        IRadiationManager radiationManager = IRadiationManager.INSTANCE;
        if (radiationManager.isRadiationEnabled() && isBurning() && world.getRandom().nextInt() % SharedConstants.TICKS_PER_SECOND == 0) {
            List<LivingEntity> entitiesToRadiate = getLevel().getEntitiesOfClass(LivingEntity.class, hotZone);
            if (!entitiesToRadiate.isEmpty()) {
                double wasteRadiation = getWasteTankRadioactivity(false) / 3_600F; // divide down to Sv/s
                double magnitude = lastBurnRate + wasteRadiation;
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
        double avgSurfaceArea = (double) surfaceArea / (double) fuelAssemblies;
        return Math.min(1, avgSurfaceArea / MekanismGeneratorsConfig.generators.fissionSurfaceAreaTarget.get());
    }

    @ComputerMethod
    public long getMaxBurnRate() {
        return fuelAssemblies * MekanismGeneratorsConfig.generators.burnPerAssembly.get();
    }

    @ComputerMethod
    public long getDamagePercent() {
        return Math.round((reactorDamage / FissionReactorMultiblockData.MAX_DAMAGE) * 100);
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
        return MekanismUtils.redstoneLevelFromContents(fuelTank.getStored(), fuelTank.getCapacity());
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
    Either<ChemicalStack<?>, FluidStack> getCoolant() {
        if (fluidCoolantTank.isEmpty() && !gasCoolantTank.isEmpty()) {
            return Either.left(gasCoolantTank.getStack());
        }
        return Either.right(fluidCoolantTank.getFluid());
    }

    @ComputerMethod
    long getCoolantCapacity() {
        if (fluidCoolantTank.isEmpty() && !gasCoolantTank.isEmpty()) {
            return gasCoolantTank.getCapacity();
        }
        return fluidCoolantTank.getCapacity();
    }

    @ComputerMethod
    long getCoolantNeeded() {
        if (fluidCoolantTank.isEmpty() && !gasCoolantTank.isEmpty()) {
            return gasCoolantTank.getNeeded();
        }
        return fluidCoolantTank.getNeeded();
    }

    @ComputerMethod
    double getCoolantFilledPercentage() {
        if (fluidCoolantTank.isEmpty() && !gasCoolantTank.isEmpty()) {
            return gasCoolantTank.getStored() / (double) gasCoolantTank.getCapacity();
        }
        return fluidCoolantTank.getFluidAmount() / (double) fluidCoolantTank.getCapacity();
    }

    @ComputerMethod
    double getHeatCapacity() {
        return heatCapacitor.getHeatCapacity();
    }
    //End computer related methods
}
