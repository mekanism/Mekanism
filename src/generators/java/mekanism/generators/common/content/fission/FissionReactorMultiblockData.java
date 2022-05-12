package mekanism.generators.common.content.fission;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.chemical.gas.attribute.GasAttributes.CooledCoolant;
import mekanism.api.chemical.gas.attribute.GasAttributes.HeatedCoolant;
import mekanism.api.heat.HeatAPI;
import mekanism.api.radiation.IRadiationManager;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder;
import mekanism.common.capabilities.fluid.MultiblockFluidTank;
import mekanism.common.capabilities.heat.MultiblockHeatCapacitor;
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
import mekanism.common.registries.MekanismGases;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.fission.FissionReactorValidator.FormedAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class FissionReactorMultiblockData extends MultiblockData implements IValveHandler {

    private static final double INVERSE_INSULATION_COEFFICIENT = 10_000;
    private static final double INVERSE_CONDUCTION_COEFFICIENT = 10;

    private static final double waterConductivity = 0.5;

    private static final int COOLANT_PER_VOLUME = 100_000;
    private static final long HEATED_COOLANT_PER_VOLUME = 1_000_000;
    private static final long FUEL_PER_ASSEMBLY = 8_000;

    public static final double MIN_DAMAGE_TEMPERATURE = 1_200;
    public static final double MAX_DAMAGE_TEMPERATURE = 1_800;
    public static final double MAX_DAMAGE = 100;

    private static final double EXPLOSION_CHANCE = 1D / (512_000);

    public final Set<FormedAssembly> assemblies = new LinkedHashSet<>();
    @ContainerSync
    @SyntheticComputerMethod(getter = "getFuelAssemblies")
    public int fuelAssemblies = 1;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getFuelSurfaceArea")
    public int surfaceArea;

    @ContainerSync
    public IGasTank gasCoolantTank;
    @ContainerSync
    public MultiblockFluidTank<FissionReactorMultiblockData> fluidCoolantTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getFuel", "getFuelCapacity", "getFuelNeeded", "getFuelFilledPercentage"})
    public IGasTank fuelTank;

    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getHeatedCoolant", "getHeatedCoolantCapacity", "getHeatedCoolantNeeded",
                                                                                        "getHeatedCoolantFilledPercentage"})
    public IGasTank heatedCoolantTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getWaste", "getWasteCapacity", "getWasteNeeded", "getWasteFilledPercentage"})
    public IGasTank wasteTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerHeatCapacitorWrapper.class, methodNames = "getTemperature")
    public MultiblockHeatCapacitor<FissionReactorMultiblockData> heatCapacitor;

    private double biomeAmbientTemp;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getEnvironmentalLoss")
    public double lastEnvironmentLoss = 0;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getHeatingRate")
    public long lastBoilRate = 0;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getActualBurnRate")
    public double lastBurnRate = 0;
    private boolean clientBurning;
    @ContainerSync
    public double reactorDamage = 0;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getBurnRate")
    public double rateLimit = MekanismGeneratorsConfig.generators.defaultBurnRate.get();
    public double burnRemaining = 0, partialWaste = 0;
    @ContainerSync
    private boolean active;
    //For use when meltdowns are disabled to make the reactor stop and require going under the threshold
    @ContainerSync
    private boolean forceDisable;

    private AABB hotZone;

    public float prevCoolantScale;
    private float prevFuelScale;
    public float prevHeatedCoolantScale;
    private float prevWasteScale;

    public FissionReactorMultiblockData(TileEntityFissionReactorCasing tile) {
        super(tile);
        //Default biome temp to the ambient temperature at the block we are at
        biomeAmbientTemp = HeatAPI.getAmbientTemp(tile.getLevel(), tile.getTilePos());
        fluidCoolantTank = MultiblockFluidTank.create(this, tile, () -> getVolume() * COOLANT_PER_VOLUME,
              (stack, automationType) -> automationType != AutomationType.EXTERNAL, (stack, automationType) -> isFormed(),
              fluid -> MekanismTags.Fluids.WATER_LOOKUP.contains(fluid.getFluid()) && gasCoolantTank.isEmpty(), null);
        fluidTanks.add(fluidCoolantTank);
        gasCoolantTank = MultiblockChemicalTankBuilder.GAS.create(this, tile, () -> (long) getVolume() * COOLANT_PER_VOLUME,
              (stack, automationType) -> automationType != AutomationType.EXTERNAL, (stack, automationType) -> isFormed(),
              gas -> gas.has(CooledCoolant.class) && fluidCoolantTank.isEmpty());
        fuelTank = MultiblockChemicalTankBuilder.GAS.create(this, tile, () -> fuelAssemblies * FUEL_PER_ASSEMBLY,
              (stack, automationType) -> automationType != AutomationType.EXTERNAL, (stack, automationType) -> isFormed(),
              gas -> gas == MekanismGases.FISSILE_FUEL.getChemical(), ChemicalAttributeValidator.ALWAYS_ALLOW, null);
        heatedCoolantTank = MultiblockChemicalTankBuilder.GAS.create(this, tile, () -> getVolume() * HEATED_COOLANT_PER_VOLUME,
              (stack, automationType) -> isFormed(), (stack, automationType) -> automationType != AutomationType.EXTERNAL,
              gas -> gas == MekanismGases.STEAM.get() || gas.has(HeatedCoolant.class));
        wasteTank = MultiblockChemicalTankBuilder.GAS.create(this, tile, () -> fuelAssemblies * FUEL_PER_ASSEMBLY,
              (stack, automationType) -> isFormed(), (stack, automationType) -> automationType != AutomationType.EXTERNAL,
              gas -> gas == MekanismGases.NUCLEAR_WASTE.getChemical(), ChemicalAttributeValidator.ALWAYS_ALLOW, null);
        Collections.addAll(gasTanks, fuelTank, heatedCoolantTank, wasteTank, gasCoolantTank);
        heatCapacitor = MultiblockHeatCapacitor.create(this, tile, MekanismGeneratorsConfig.generators.fissionCasingHeatCapacity.get(),
              () -> INVERSE_CONDUCTION_COEFFICIENT, () -> INVERSE_INSULATION_COEFFICIENT, () -> biomeAmbientTemp);
        heatCapacitors.add(heatCapacitor);
    }

    @Override
    public void onCreated(Level world) {
        super.onCreated(world);
        biomeAmbientTemp = calculateAverageAmbientTemperature(world);
        // update the heat capacity now that we've read
        heatCapacitor.setHeatCapacity(MekanismGeneratorsConfig.generators.fissionCasingHeatCapacity.get() * locations.size(), true);
        hotZone = new AABB(getMinPos().offset(1, 1, 1), getMaxPos());
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
        if (coolantScale != prevCoolantScale || fuelScale != prevFuelScale || steamScale != prevHeatedCoolantScale || wasteScale != prevWasteScale) {
            needsPacket = true;
            prevCoolantScale = coolantScale;
            prevFuelScale = fuelScale;
            prevHeatedCoolantScale = steamScale;
            prevWasteScale = wasteScale;
        }
        return needsPacket;
    }

    @Override
    public double simulateEnvironment() {
        double invConduction = HeatAPI.AIR_INVERSE_COEFFICIENT + (INVERSE_INSULATION_COEFFICIENT + INVERSE_CONDUCTION_COEFFICIENT);
        double tempToTransfer = (heatCapacitor.getTemperature() - biomeAmbientTemp) / invConduction;
        heatCapacitor.handleHeat(-tempToTransfer * heatCapacitor.getHeatCapacity());
        return Math.max(tempToTransfer, 0);
    }

    @Override
    public void readUpdateTag(CompoundTag tag) {
        super.readUpdateTag(tag);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> prevCoolantScale = scale);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE_ALT, scale -> prevFuelScale = scale);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE_ALT_2, scale -> prevHeatedCoolantScale = scale);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE_ALT_3, scale -> prevWasteScale = scale);
        NBTUtils.setIntIfPresent(tag, NBTConstants.VOLUME, this::setVolume);
        NBTUtils.setFluidStackIfPresent(tag, NBTConstants.FLUID_STORED, value -> fluidCoolantTank.setStack(value));
        NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED, value -> fuelTank.setStack(value));
        NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED_ALT, value -> heatedCoolantTank.setStack(value));
        NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED_ALT_2, value -> wasteTank.setStack(value));
        readValves(tag);
        assemblies.clear();
        if (tag.contains(NBTConstants.ASSEMBLIES, Tag.TAG_LIST)) {
            ListTag list = tag.getList(NBTConstants.ASSEMBLIES, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                assemblies.add(FormedAssembly.read(list.getCompound(i)));
            }
        }
    }

    @Override
    public void writeUpdateTag(CompoundTag tag) {
        super.writeUpdateTag(tag);
        tag.putFloat(NBTConstants.SCALE, prevCoolantScale);
        tag.putFloat(NBTConstants.SCALE_ALT, prevFuelScale);
        tag.putFloat(NBTConstants.SCALE_ALT_2, prevHeatedCoolantScale);
        tag.putFloat(NBTConstants.SCALE_ALT_3, prevWasteScale);
        tag.putInt(NBTConstants.VOLUME, getVolume());
        tag.put(NBTConstants.FLUID_STORED, fluidCoolantTank.getFluid().writeToNBT(new CompoundTag()));
        tag.put(NBTConstants.GAS_STORED, fuelTank.getStack().write(new CompoundTag()));
        tag.put(NBTConstants.GAS_STORED_ALT, heatedCoolantTank.getStack().write(new CompoundTag()));
        tag.put(NBTConstants.GAS_STORED_ALT_2, wasteTank.getStack().write(new CompoundTag()));
        writeValves(tag);
        ListTag list = new ListTag();
        assemblies.forEach(assembly -> list.add(assembly.write()));
        tag.put(NBTConstants.ASSEMBLIES, list);
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
                RadiationManager.INSTANCE.createMeltdown(world, getMinPos(), getMaxPos(), heatCapacitor.getHeat(), EXPLOSION_CHANCE, inventoryID);
            } else if (world.random.nextDouble() < (reactorDamage / MAX_DAMAGE) * MekanismGeneratorsConfig.generators.fissionMeltdownChance.get()) {
                // Otherwise, if our chance is hit either create a meltdown if it is enabled in the config, or force disable the reactor
                if (MekanismGeneratorsConfig.generators.fissionMeltdownsEnabled.get()) {
                    RadiationManager.INSTANCE.createMeltdown(world, getMinPos(), getMaxPos(), heatCapacitor.getHeat(), EXPLOSION_CHANCE, inventoryID);
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

    @Override
    public void meltdownHappened(Level world) {
        if (isFormed()) {
            IRadiationManager radiationManager = MekanismAPI.getRadiationManager();
            //Calculate radiation level and clear any tanks that had radioactive substances and are contributing to the
            // amount of radiation released
            double radiation = getTankRadioactivityAndDump(fuelTank) + getWasteTankRadioactivity(true) +
                               getTankRadioactivityAndDump(gasCoolantTank) + getTankRadioactivityAndDump(heatedCoolantTank);
            radiation *= MekanismGeneratorsConfig.generators.fissionMeltdownRadiationMultiplier.get();
            //When the meltdown actually happens, release radiation into the atmosphere
            radiationManager.radiate(new Coord4D(getBounds().getCenter(), world), radiation);
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
            MultiblockManager<FissionReactorMultiblockData>.CacheWrapper cacheWrapper = MekanismGenerators.fissionReactorManager.inventories.get(inventoryID);
            if (cacheWrapper != null) {
                MultiblockCache<FissionReactorMultiblockData> cache = cacheWrapper.getCache();
                if (cache != null) {
                    cache.sync(this);
                }
            }
        }
    }

    private double getWasteTankRadioactivity(boolean dump) {
        if (wasteTank.isEmpty()) {
            return partialWaste * MekanismGases.NUCLEAR_WASTE.get().get(GasAttributes.Radiation.class).getRadioactivity();
        }
        GasStack stored = wasteTank.getStack();
        if (stored.has(GasAttributes.Radiation.class)) {
            if (dump) {
                //If we want to dump if we have a radioactive substance, then we need to set the tank to empty
                wasteTank.setEmpty();
            }
            return (stored.getAmount() + partialWaste) * stored.get(GasAttributes.Radiation.class).getRadioactivity();
        }
        return 0;
    }

    private double getTankRadioactivityAndDump(IGasTank tank) {
        if (!tank.isEmpty()) {
            GasStack stored = tank.getStack();
            if (stored.has(GasAttributes.Radiation.class)) {
                //If we have a radioactive substance, then we need to set the tank to empty
                tank.setEmpty();
                return stored.getAmount() * stored.get(GasAttributes.Radiation.class).getRadioactivity();
            }
        }
        return 0;
    }

    private void handleCoolant() {
        double temp = heatCapacitor.getTemperature();
        double heat = getBoilEfficiency() * (temp - HeatUtils.BASE_BOIL_TEMP) * heatCapacitor.getHeatCapacity();
        long coolantHeated = 0;

        if (!fluidCoolantTank.isEmpty()) {
            double caseCoolantHeat = heat * waterConductivity;
            coolantHeated = (int) (HeatUtils.getSteamEnergyEfficiency() * caseCoolantHeat / HeatUtils.getWaterThermalEnthalpy());
            coolantHeated = Math.max(0, Math.min(coolantHeated, fluidCoolantTank.getFluidAmount()));
            if (coolantHeated > 0) {
                MekanismUtils.logMismatchedStackSize(fluidCoolantTank.shrinkStack((int) coolantHeated, Action.EXECUTE), coolantHeated);
                // extra steam is dumped
                heatedCoolantTank.insert(MekanismGases.STEAM.getStack(coolantHeated), Action.EXECUTE, AutomationType.INTERNAL);
                caseCoolantHeat = coolantHeated * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
                heatCapacitor.handleHeat(-caseCoolantHeat);
            }
        } else if (!gasCoolantTank.isEmpty()) {
            CooledCoolant coolantType = gasCoolantTank.getStack().get(CooledCoolant.class);
            if (coolantType != null) {
                double caseCoolantHeat = heat * coolantType.getConductivity();
                coolantHeated = (int) (caseCoolantHeat / coolantType.getThermalEnthalpy());
                coolantHeated = Math.max(0, Math.min(coolantHeated, gasCoolantTank.getStored()));
                if (coolantHeated > 0) {
                    MekanismUtils.logMismatchedStackSize(gasCoolantTank.shrinkStack((int) coolantHeated, Action.EXECUTE), coolantHeated);
                    heatedCoolantTank.insert(coolantType.getHeatedGas().getStack(coolantHeated), Action.EXECUTE, AutomationType.INTERNAL);
                    caseCoolantHeat = coolantHeated * coolantType.getThermalEnthalpy();
                    heatCapacitor.handleHeat(-caseCoolantHeat);
                }
            }
        }
        lastBoilRate = coolantHeated;
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
        long newWaste = (long) Math.floor(partialWaste);
        if (newWaste > 0) {
            partialWaste %= 1;
            long leftoverWaste = Math.max(0, newWaste - wasteTank.getNeeded());
            GasStack wasteToAdd = MekanismGases.NUCLEAR_WASTE.getStack(newWaste);
            wasteTank.insert(wasteToAdd, Action.EXECUTE, AutomationType.INTERNAL);
            if (leftoverWaste > 0) {
                double radioactivity = wasteToAdd.getType().get(GasAttributes.Radiation.class).getRadioactivity();
                MekanismAPI.getRadiationManager().radiate(new Coord4D(getBounds().getCenter(), world), leftoverWaste * radioactivity);
            }
        }
        // update previous burn
        lastBurnRate = toBurn;
        if (lastPartialWaste != partialWaste || lastBurnRemaining != burnRemaining) {
            markDirty();
        }
    }

    private void radiateEntities(Level world) {
        IRadiationManager radiationManager = MekanismAPI.getRadiationManager();
        if (radiationManager.isRadiationEnabled() && isBurning() && world.getRandom().nextInt() % 20 == 0) {
            List<LivingEntity> entitiesToRadiate = getWorld().getEntitiesOfClass(LivingEntity.class, hotZone);
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

    @ComputerMethod(nameOverride = "getStatus")
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

    @Override
    protected int getMultiblockRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fuelTank.getStored(), fuelTank.getCapacity());
    }

    public void setRateLimit(double rate) {
        rate = Math.max(Math.min(getMaxBurnRate(), rate), 0);
        if (rateLimit != rate) {
            rateLimit = rate;
            markDirty();
        }
    }

    //Computer related methods
    @ComputerMethod
    private void activate() throws ComputerException {
        if (isActive()) {
            throw new ComputerException("Reactor is already active.");
        } else if (isForceDisabled()) {
            throw new ComputerException("Reactor must reach safe damage and temperature levels before it can be reactivated.");
        }
        setActive(true);
    }

    @ComputerMethod
    private void scram() throws ComputerException {
        if (!isActive()) {
            throw new ComputerException("Scram requires the reactor to be active.");
        }
        setActive(false);
    }

    @ComputerMethod
    private void setBurnRate(double rate) throws ComputerException {
        //Round to two decimal places
        rate = UnitDisplayUtils.roundDecimals(rate);
        long max = getMaxBurnRate();
        if (rate < 0 || rate > max) {
            //Validate bounds even though we can clamp
            throw new ComputerException("Burn Rate '%d' is out of range must be between 0 and %d. (Inclusive)", rate, max);
        }
        setRateLimit(rate);
    }

    @ComputerMethod
    private Object getCoolant() {
        if (fluidCoolantTank.isEmpty() && !gasCoolantTank.isEmpty()) {
            return gasCoolantTank.getStack();
        }
        return fluidCoolantTank.getFluid();
    }

    @ComputerMethod
    private long getCoolantCapacity() {
        if (fluidCoolantTank.isEmpty() && !gasCoolantTank.isEmpty()) {
            return gasCoolantTank.getCapacity();
        }
        return fluidCoolantTank.getCapacity();
    }

    @ComputerMethod
    private long getCoolantNeeded() {
        if (fluidCoolantTank.isEmpty() && !gasCoolantTank.isEmpty()) {
            return gasCoolantTank.getNeeded();
        }
        return fluidCoolantTank.getNeeded();
    }

    @ComputerMethod
    private double getCoolantFilledPercentage() {
        if (fluidCoolantTank.isEmpty() && !gasCoolantTank.isEmpty()) {
            return gasCoolantTank.getStored() / (double) gasCoolantTank.getCapacity();
        }
        return fluidCoolantTank.getFluidAmount() / (double) fluidCoolantTank.getCapacity();
    }

    @ComputerMethod
    private double getHeatCapacity() {
        return heatCapacitor.getHeatCapacity();
    }
    //End computer related methods
}