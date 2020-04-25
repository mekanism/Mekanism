package mekanism.generators.common.content.fusion;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.HeatAPI;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.registries.GeneratorsGases;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorBlock;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class FusionReactor {

    private static final int MAX_INJECTION = 98;//this is the effective cap in the GUI, as text field is limited to 2 chars
    //Reaction characteristics
    private static double burnTemperature = 100_000_000;
    private static double burnRatio = 1;
    //Thermal characteristics
    private static double plasmaHeatCapacity = 100;
    public static double caseHeatCapacity = 1;
    public static double inverseInsulation = 100_000;
    private static double thermocoupleEfficiency = 0.05;
    //Heat transfer metrics
    private static double plasmaCaseConductivity = 0.2;
    private static double caseWaterConductivity = 0.3;
    private static double caseAirConductivity = 0.1;
    public TileEntityFusionReactorController controller;
    private Set<TileEntityFusionReactorBlock> reactorBlocks = new ObjectOpenHashSet<>();
    private Set<ITileHeatHandler> heatHandlers = new ObjectOpenHashSet<>();
    //Last values of temperature
    private double lastPlasmaTemperature = HeatAPI.AMBIENT_TEMP;
    private double lastCaseTemperature = HeatAPI.AMBIENT_TEMP;
    private int injectionRate = 0;
    private boolean burning = false;

    public boolean formed = false;

    public FusionReactor(TileEntityFusionReactorController c) {
        controller = c;
    }

    public void addTemperatureFromEnergyInput(FloatingLong energyAdded) {
        if (isBurning()) {
            setPlasmaTemp(getPlasmaTemp() + energyAdded.divide(plasmaHeatCapacity).doubleValue());
        } else {
            setPlasmaTemp(getPlasmaTemp() + energyAdded.divide(plasmaHeatCapacity).multiply(10).doubleValue());
        }
    }

    private boolean hasHohlraum() {
        if (controller != null && !controller.getReactorSlot().isEmpty()) {
            ItemStack hohlraum = controller.getReactorSlot().getStack();
            if (hohlraum.getItem() instanceof ItemHohlraum) {
                Optional<IGasHandler> capability = MekanismUtils.toOptional(hohlraum.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
                if (capability.isPresent()) {
                    IGasHandler gasHandlerItem = capability.get();
                    if (gasHandlerItem.getGasTankCount() > 0) {
                        //Validate something didn't go terribly wrong and we actually do have the tank we expect to have
                        return gasHandlerItem.getGasInTank(0).getAmount() == gasHandlerItem.getGasTankCapacity(0);
                    }
                }
            }
        }
        return false;
    }

    public void simulateServer() {
        //Only thermal transfer happens unless we're hot enough to burn.
        if (getPlasmaTemp() >= burnTemperature) {
            //If we're not burning yet we need a hohlraum to ignite
            if (!burning && hasHohlraum()) {
                vaporiseHohlraum();
            }

            //Only inject fuel if we're burning
            if (burning) {
                injectFuel();
                long fuelBurned = burnFuel();
                if (fuelBurned == 0) {
                    setBurning(false);
                }
            }
        } else {
            setBurning(false);
        }

        //Perform the heat transfer calculations
        transferHeat();

        if (burning) {
            kill();
        }
        updateTemperatures();
    }

    public void updateTemperatures() {
        lastPlasmaTemperature = getPlasmaTemp();
        lastCaseTemperature = getHeatCapacitor().getTemperature();
    }

    private void vaporiseHohlraum() {
        IInventorySlot reactorSlot = controller.getReactorSlot();
        ItemStack hohlraum = reactorSlot.getStack();
        Optional<IGasHandler> capability = MekanismUtils.toOptional(hohlraum.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            if (gasHandlerItem.getGasTankCount() > 0) {
                getFuelTank().insert(gasHandlerItem.getGasInTank(0), Action.EXECUTE, AutomationType.INTERNAL);
                lastPlasmaTemperature = getPlasmaTemp();
                reactorSlot.setStack(ItemStack.EMPTY);
                setBurning(true);
            }
        }
    }

    private void injectFuel() {
        long amountNeeded = getFuelTank().getNeeded();
        long amountAvailable = 2 * Math.min(getDeuteriumTank().getStored(), getTritiumTank().getStored());
        long amountToInject = Math.min(amountNeeded, Math.min(amountAvailable, injectionRate));
        amountToInject -= amountToInject % 2;
        long injectingAmount = amountToInject / 2;
        if (getDeuteriumTank().shrinkStack(injectingAmount, Action.EXECUTE) != injectingAmount) {
            MekanismUtils.logMismatchedStackSize();
        }
        if (getTritiumTank().shrinkStack(injectingAmount, Action.EXECUTE) != injectingAmount) {
            MekanismUtils.logMismatchedStackSize();
        }
        getFuelTank().insert(GeneratorsGases.FUSION_FUEL.getGasStack(amountToInject), Action.EXECUTE, AutomationType.INTERNAL);
    }

    private long burnFuel() {
        long fuelBurned = (long) Math.min(getFuelTank().getStored(), Math.max(0, lastPlasmaTemperature - burnTemperature) * burnRatio);
        if (getFuelTank().shrinkStack(fuelBurned, Action.EXECUTE) != fuelBurned) {
            MekanismUtils.logMismatchedStackSize();
        }
        setPlasmaTemp(getPlasmaTemp() + MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().multiply(fuelBurned).divide(plasmaHeatCapacity).doubleValue());
        return fuelBurned;
    }

    private void transferHeat() {
        //Transfer from plasma to casing
        double plasmaCaseHeat = plasmaCaseConductivity * (lastPlasmaTemperature - lastCaseTemperature);
        setPlasmaTemp(getPlasmaTemp() - plasmaCaseHeat / plasmaHeatCapacity);
        getHeatCapacitor().handleHeat(plasmaCaseHeat);

        //Transfer from casing to water if necessary
        double caseWaterHeat = caseWaterConductivity * (lastCaseTemperature - HeatAPI.AMBIENT_TEMP);
        int waterToVaporize = (int) (HeatUtils.getFluidThermalEfficiency() * caseWaterHeat / HeatUtils.getWaterThermalEnthalpy());
        waterToVaporize = Math.min(waterToVaporize, Math.min(getWaterTank().getFluidAmount(), MathUtils.clampToInt(getSteamTank().getNeeded())));
        if (waterToVaporize > 0) {
            if (getWaterTank().shrinkStack(waterToVaporize, Action.EXECUTE) != waterToVaporize) {
                MekanismUtils.logMismatchedStackSize();
            }
            getSteamTank().insert(MekanismGases.STEAM.getGasStack(waterToVaporize), Action.EXECUTE, AutomationType.INTERNAL);
            caseWaterHeat = waterToVaporize * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getFluidThermalEfficiency();
            getHeatCapacitor().handleHeat(-caseWaterHeat);
        }

        for (ITileHeatHandler source : heatHandlers) {
            source.simulate();
        }

        //Passive energy generation
        double caseAirHeat = caseAirConductivity * (lastCaseTemperature - HeatAPI.AMBIENT_TEMP);
        getHeatCapacitor().handleHeat(-caseAirHeat);
        controller.energyContainer.insert(FloatingLong.create(caseAirHeat * thermocoupleEfficiency), Action.EXECUTE, AutomationType.INTERNAL);
    }

    public BasicHeatCapacitor getHeatCapacitor() {
        return controller.heatCapacitor;
    }

    public IExtendedFluidTank getWaterTank() {
        return controller.waterTank;
    }

    public IGasTank getSteamTank() {
        return controller.steamTank;
    }

    public IGasTank getDeuteriumTank() {
        return controller.deuteriumTank;
    }

    public IGasTank getTritiumTank() {
        return controller.tritiumTank;
    }

    public IGasTank getFuelTank() {
        return controller.fuelTank;
    }

    public double getLastPlasmaTemp() {
        return lastPlasmaTemperature;
    }

    public void setLastPlasmaTemp(double temp) {
        lastPlasmaTemperature = temp;
    }

    public double getPlasmaTemp() {
        return controller.plasmaTemperature;
    }

    public void setPlasmaTemp(double temp) {
        controller.plasmaTemperature = temp;
    }

    public double getLastCaseTemp() {
        return lastCaseTemperature;
    }

    public void setLastCaseTemp(double temp) {
        lastCaseTemperature = temp;
    }

    private void kill() {
        AxisAlignedBB death_zone = new AxisAlignedBB(controller.getPos().getX() - 1, controller.getPos().getY() - 3,
              controller.getPos().getZ() - 1, controller.getPos().getX() + 2, controller.getPos().getY(), controller.getPos().getZ() + 2);
        List<Entity> entitiesToDie = controller.getWorld().getEntitiesWithinAABB(Entity.class, death_zone);

        for (Entity entity : entitiesToDie) {
            entity.attackEntityFrom(DamageSource.MAGIC, 50_000F);
        }
    }

    private void unformMultiblock(boolean keepBurning) {
        for (TileEntityFusionReactorBlock block : reactorBlocks) {
            block.setReactor(null);
        }

        //Don't remove from controller
        controller.setReactor(this);
        reactorBlocks.clear();
        formed = false;
        setBurning(burning && keepBurning);
    }

    public void formMultiblock(boolean keepBurning) {
        Coord4D controllerPosition = Coord4D.get(controller);
        Coord4D centreOfReactor = controllerPosition.offset(Direction.DOWN, 2);
        unformMultiblock(true);
        reactorBlocks.add(controller);

        if (!createFrame(centreOfReactor) || !addSides(centreOfReactor) || !centerIsClear(centreOfReactor)) {
            unformMultiblock(keepBurning);
            if (!controller.isRemote() && !controller.isRemoved()) {
                //Only set it to inactive and update the controller if we aren't unforming the multiblock due to removing it
                controller.setActive(false);
                controller.sendUpdatePacket();
            }
            return;
        }

        formed = true;

        if (!controller.isRemote()) {
            controller.setActive(true);
            controller.sendUpdatePacket();
        }
    }

    private boolean createFrame(Coord4D center) {
        int[][] positions = new int[][]{
              {+2, +2, +0}, {+2, +1, +1}, {+2, +0, +2}, {+2, -1, +1}, {+2, -2, +0}, {+2, -1, -1}, {+2, +0, -2}, {+2, +1, -1}, {+1, +2, +1}, {+1, +1, +2}, {+1, -1, +2},
              {+1, -2, +1}, {+1, -2, -1}, {+1, -1, -2}, {+1, +1, -2}, {+1, +2, -1}, {+0, +2, +2}, {+0, -2, +2}, {+0, -2, -2}, {+0, +2, -2}, {-1, +2, +1}, {-1, +1, +2},
              {-1, -1, +2}, {-1, -2, +1}, {-1, -2, -1}, {-1, -1, -2}, {-1, +1, -2}, {-1, +2, -1}, {-2, +2, +0}, {-2, +1, +1}, {-2, +0, +2}, {-2, -1, +1}, {-2, -2, +0},
              {-2, -1, -1}, {-2, +0, -2}, {-2, +1, -1},};
        BlockPos centerPos = center.getPos();
        for (int[] coords : positions) {
            TileEntityFusionReactorBlock tile = MekanismUtils.getTileEntity(TileEntityFusionReactorBlock.class, controller.getWorld(), centerPos.add(coords[0], coords[1], coords[2]));
            if (tile == null || !tile.isFrame()) {
                return false;
            }
            reactorBlocks.add(tile);
            tile.setReactor(this);
        }
        return true;
    }

    private boolean addSides(Coord4D center) {
        int[][] positions = new int[][]{
              {+2, +0, +0}, {+2, +1, +0}, {+2, +0, +1}, {+2, -1, +0}, {+2, +0, -1}, //EAST
              {-2, +0, +0}, {-2, +1, +0}, {-2, +0, +1}, {-2, -1, +0}, {-2, +0, -1}, //WEST
              {+0, +2, +0}, {+1, +2, +0}, {+0, +2, +1}, {-1, +2, +0}, {+0, +2, -1}, //TOP
              {+0, -2, +0}, {+1, -2, +0}, {+0, -2, +1}, {-1, -2, +0}, {+0, -2, -1}, //BOTTOM
              {+0, +0, +2}, {+1, +0, +2}, {+0, +1, +2}, {-1, +0, +2}, {+0, -1, +2}, //SOUTH
              {+0, +0, -2}, {+1, +0, -2}, {+0, +1, -2}, {-1, +0, -2}, {+0, -1, -2}, //NORTH
        };
        BlockPos centerPos = center.getPos();
        for (int[] coords : positions) {
            TileEntity tile = MekanismUtils.getTileEntity(controller.getWorld(), centerPos.add(coords[0], coords[1], coords[2]));
            if (CapabilityUtils.getCapability(tile, Capabilities.LASER_RECEPTOR_CAPABILITY, null).isPresent() &&
                !(coords[1] == 0 && (coords[0] == 0 || coords[2] == 0))) {
                return false;
            }
            if (tile instanceof TileEntityFusionReactorBlock) {
                TileEntityFusionReactorBlock tileReactor = (TileEntityFusionReactorBlock) tile;
                reactorBlocks.add(tileReactor);
                tileReactor.setReactor(this);
                heatHandlers.add(tileReactor);
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean centerIsClear(Coord4D center) {
        BlockPos centerPos = center.getPos();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos transPos = centerPos.add(x, y, z);
                    BlockState state = controller.getWorld().getBlockState(transPos);
                    Block tile = state.getBlock();
                    if (!tile.isAir(state, controller.getWorld(), transPos)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isFormed() {
        return formed;
    }

    public int getInjectionRate() {
        return injectionRate;
    }

    public void setInjectionRate(int rate) {
        injectionRate = rate;
        int capRate = Math.min(Math.max(1, rate), MAX_INJECTION);
        capRate -= capRate % 2;
        controller.updateMaxCapacities(capRate);
        if (controller.getWorld() != null && !controller.isRemote()) {
            if (!controller.waterTank.isEmpty()) {
                controller.waterTank.setStackSize(Math.min(controller.waterTank.getFluidAmount(), controller.waterTank.getCapacity()), Action.EXECUTE);
            }
            if (!controller.steamTank.isEmpty()) {
                controller.steamTank.setStackSize(Math.min(controller.steamTank.getStored(), controller.steamTank.getCapacity()), Action.EXECUTE);
            }
        }
    }

    public boolean isBurning() {
        return burning;
    }

    public void setBurning(boolean burn) {
        burning = burn;
    }

    public int getMinInjectionRate(boolean active) {
        double k = active ? caseWaterConductivity : 0;
        double aMin = burnTemperature * burnRatio * plasmaCaseConductivity * (k + caseAirConductivity) /
                      (MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().doubleValue() * burnRatio * (plasmaCaseConductivity + k + caseAirConductivity) -
                       plasmaCaseConductivity * (k + caseAirConductivity));
        return (int) (2 * Math.ceil(aMin / 2D));
    }

    public double getMaxPlasmaTemperature(boolean active) {
        double k = active ? caseWaterConductivity : 0;
        return injectionRate * MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().doubleValue() / plasmaCaseConductivity *
               (plasmaCaseConductivity + k + caseAirConductivity) / (k + caseAirConductivity);
    }

    public double getMaxCasingTemperature(boolean active) {
        double k = active ? caseWaterConductivity : 0;
        return MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().multiply(injectionRate).divide(k + caseAirConductivity).doubleValue();
    }

    public double getIgnitionTemperature(boolean active) {
        double k = active ? caseWaterConductivity : 0;
        double energyPerFusionFuel = MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().doubleValue();
        return burnTemperature * energyPerFusionFuel * burnRatio * (plasmaCaseConductivity + k + caseAirConductivity) /
               (energyPerFusionFuel * burnRatio * (plasmaCaseConductivity + k + caseAirConductivity) - plasmaCaseConductivity * (k + caseAirConductivity));
    }

    public FloatingLong getPassiveGeneration(boolean active, boolean current) {
        double temperature = current ? getLastCaseTemp() : getMaxCasingTemperature(active);
        return FloatingLong.create(thermocoupleEfficiency * caseAirConductivity * temperature);
    }

    public long getSteamPerTick(boolean current) {
        double temperature = current ? getLastCaseTemp() : getMaxCasingTemperature(true);
        return MathUtils.clampToLong(HeatUtils.getFluidThermalEfficiency() * caseWaterConductivity * temperature / HeatUtils.getWaterThermalEnthalpy());
    }

    public static double getInverseConductionCoefficient() {
        return 1 / caseAirConductivity;
    }
}