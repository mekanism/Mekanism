package mekanism.generators.common;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.HeatPacket;
import mekanism.api.heat.HeatPacket.TransferType;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.FloatingLong;
import mekanism.common.LaserManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.registries.GeneratorsGases;
import mekanism.generators.common.tile.reactor.TileEntityReactorBlock;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
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

    private static final FloatingLong POINT_ONE = FloatingLong.createConst(0.1);

    private static final int MAX_INJECTION = 98;//this is the effective cap in the GUI, as text field is limited to 2 chars
    //Reaction characteristics
    private static FloatingLong burnTemperature = FloatingLong.createConst(100_000_000);
    private static FloatingLong burnRatio = FloatingLong.ONE;
    //Thermal characteristics
    private static FloatingLong plasmaHeatCapacity = FloatingLong.createConst(100);
    public static FloatingLong caseHeatCapacity = FloatingLong.ONE;
    public static FloatingLong inverseInsulation = FloatingLong.createConst(100_000);
    private static FloatingLong thermocoupleEfficiency = FloatingLong.createConst(0.05);
    private static FloatingLong steamTransferEfficiency = FloatingLong.createConst(0.2);
    //Heat transfer metrics
    private static FloatingLong plasmaCaseConductivity = FloatingLong.createConst(0.2);
    private static FloatingLong caseWaterConductivity = FloatingLong.createConst(0.3);
    private static FloatingLong caseAirConductivity = POINT_ONE;
    public TileEntityReactorController controller;
    private Set<TileEntityReactorBlock> reactorBlocks = new ObjectOpenHashSet<>();
    private Set<ITileHeatHandler> heatHandlers = new ObjectOpenHashSet<>();
    //Current plasma temperature - internally uses ambient-relative kelvin units
    private FloatingLong plasmaTemperature = HeatAPI.AMBIENT_TEMP;
    //Last values of temperature
    private FloatingLong lastPlasmaTemperature = HeatAPI.AMBIENT_TEMP;
    private FloatingLong lastCaseTemperature = HeatAPI.AMBIENT_TEMP;
    private int injectionRate = 0;
    private boolean burning = false;

    public boolean formed = false;

    public FusionReactor(TileEntityReactorController c) {
        controller = c;
    }

    public void addTemperatureFromEnergyInput(FloatingLong energyAdded) {
        if (isBurning()) {
            plasmaTemperature = plasmaTemperature.plusEqual(energyAdded.divide(plasmaHeatCapacity));
        } else {
            plasmaTemperature = plasmaTemperature.plusEqual(energyAdded.divide(plasmaHeatCapacity).multiply(10));
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
        if (!burnTemperature.greaterThan(plasmaTemperature)) {
            //If we're not burning yet we need a hohlraum to ignite
            if (!burning && hasHohlraum()) {
                vaporiseHohlraum();
            }

            //Only inject fuel if we're burning
            if (burning) {
                injectFuel();
                int fuelBurned = burnFuel();
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
        lastPlasmaTemperature = plasmaTemperature.smallerThan(POINT_ONE) ? FloatingLong.ZERO : plasmaTemperature.copyAsConst();
        lastCaseTemperature = getHeatCapacitor().getTemperature().smallerThan(POINT_ONE) ? FloatingLong.ZERO : getHeatCapacitor().getTemperature().copyAsConst();
    }

    private void vaporiseHohlraum() {
        IInventorySlot reactorSlot = controller.getReactorSlot();
        ItemStack hohlraum = reactorSlot.getStack();
        Optional<IGasHandler> capability = MekanismUtils.toOptional(hohlraum.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            if (gasHandlerItem.getGasTankCount() > 0) {
                getFuelTank().insert(gasHandlerItem.getGasInTank(0), Action.EXECUTE, AutomationType.INTERNAL);
                lastPlasmaTemperature = plasmaTemperature.copyAsConst();
                reactorSlot.setStack(ItemStack.EMPTY);
                setBurning(true);
            }
        }
    }

    private void injectFuel() {
        int amountNeeded = getFuelTank().getNeeded();
        int amountAvailable = 2 * Math.min(getDeuteriumTank().getStored(), getTritiumTank().getStored());
        int amountToInject = Math.min(amountNeeded, Math.min(amountAvailable, injectionRate));
        amountToInject -= amountToInject % 2;
        int injectingAmount = amountToInject / 2;
        if (getDeuteriumTank().shrinkStack(injectingAmount, Action.EXECUTE) != injectingAmount) {
            //TODO: Print warning/error
        }
        if (getTritiumTank().shrinkStack(injectingAmount, Action.EXECUTE) != injectingAmount) {
            //TODO: Print warning/error
        }
        getFuelTank().insert(GeneratorsGases.FUSION_FUEL.getGasStack(amountToInject), Action.EXECUTE, AutomationType.INTERNAL);
    }

    private int burnFuel() {
        FloatingLong temp = lastPlasmaTemperature.smallerThan(burnTemperature) ? FloatingLong.ZERO : lastPlasmaTemperature.subtract(burnTemperature);
        int fuelBurned = (int) Math.min(getFuelTank().getStored(), temp.multiply(burnRatio).doubleValue());
        if (getFuelTank().shrinkStack(fuelBurned, Action.EXECUTE) != fuelBurned) {
            //TODO: Print warning/error
        }
        plasmaTemperature = plasmaTemperature.plusEqual(MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().multiply(fuelBurned).divide(plasmaHeatCapacity));
        return fuelBurned;
    }

    private void transferHeat() {
        //Transfer from plasma to casing
        FloatingLong plasmaCaseHeat = plasmaCaseConductivity.multiply(lastPlasmaTemperature.subtract(lastCaseTemperature));
        plasmaTemperature = plasmaTemperature.subtract(plasmaCaseHeat).divide(plasmaHeatCapacity);

        getHeatCapacitor().handleHeat(new HeatPacket(TransferType.EMIT, plasmaCaseHeat));

        //Transfer from casing to water if necessary
        FloatingLong caseWaterHeat = caseWaterConductivity.multiply(lastCaseTemperature);
        int waterToVaporize = steamTransferEfficiency.multiply(caseWaterHeat).divide(HeatUtils.getVaporizationEnthalpy()).intValue();
        waterToVaporize = Math.min(waterToVaporize, Math.min(getWaterTank().getFluidAmount(), getSteamTank().getNeeded()));
        if (waterToVaporize > 0) {
            if (getWaterTank().shrinkStack(waterToVaporize, Action.EXECUTE) != waterToVaporize) {
                //TODO: Print warning/error
            }
            getSteamTank().insert(MekanismGases.STEAM.getGasStack(waterToVaporize), Action.EXECUTE, AutomationType.INTERNAL);
        }

        caseWaterHeat = HeatUtils.getVaporizationEnthalpy().divide(steamTransferEfficiency).multiply(waterToVaporize);
        getHeatCapacitor().handleHeat(new HeatPacket(TransferType.EMIT, caseWaterHeat));

        for (ITileHeatHandler source : heatHandlers) {
            source.simulate();
        }

        //Passive energy generation
        FloatingLong caseAirHeat = caseAirConductivity.multiply(lastCaseTemperature);
        getHeatCapacitor().handleHeat(new HeatPacket(TransferType.EMIT, caseAirHeat));
        controller.energyContainer.insert(caseAirHeat.multiply(thermocoupleEfficiency), Action.EXECUTE, AutomationType.INTERNAL);
    }

    public BasicHeatCapacitor getHeatCapacitor() {
        return controller.heatCapacitor;
    }

    public IExtendedFluidTank getWaterTank() {
        return controller.waterTank;
    }

    public IChemicalTank<Gas, GasStack> getSteamTank() {
        return controller.steamTank;
    }

    public IChemicalTank<Gas, GasStack> getDeuteriumTank() {
        return controller.deuteriumTank;
    }

    public IChemicalTank<Gas, GasStack> getTritiumTank() {
        return controller.tritiumTank;
    }

    public IChemicalTank<Gas, GasStack> getFuelTank() {
        return controller.fuelTank;
    }

    public FloatingLong getPlasmaTemp() {
        return lastPlasmaTemperature;
    }

    public void setLastPlasmaTemp(FloatingLong temp) {
        lastPlasmaTemperature = temp;
    }

    public void setPlasmaTemp(FloatingLong temp) {
        plasmaTemperature = temp;
    }

    public FloatingLong getCaseTemp() {
        return lastCaseTemperature;
    }

    public void setLastCaseTemp(FloatingLong temp) {
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
        for (TileEntityReactorBlock block : reactorBlocks) {
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
            TileEntityReactorBlock tile = MekanismUtils.getTileEntity(TileEntityReactorBlock.class, controller.getWorld(), centerPos.add(coords[0], coords[1], coords[2]));
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
            if (LaserManager.isReceptor(tile, null) && !(coords[1] == 0 && (coords[0] == 0 || coords[2] == 0))) {
                return false;
            }
            if (tile instanceof TileEntityReactorBlock) {
                TileEntityReactorBlock tileReactor = (TileEntityReactorBlock) tile;
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
        FloatingLong k = active ? caseWaterConductivity : FloatingLong.ZERO;
        FloatingLong aMin = burnTemperature.multiply(burnRatio).timesEqual(plasmaCaseConductivity).timesEqual(k.add(caseAirConductivity))
              .divideEquals(MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().multiply(burnRatio)
              .timesEqual(plasmaCaseConductivity.add(k).plusEqual(caseAirConductivity)).minusEqual(plasmaCaseConductivity.multiply(k.add(caseAirConductivity))));
        return aMin.divide(2).ceil().multiply(2).intValue();
    }

    public FloatingLong getMaxPlasmaTemperature(boolean active) {
        FloatingLong k = active ? caseWaterConductivity : FloatingLong.ZERO;
        return MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().divide(plasmaCaseConductivity).timesEqual(
              plasmaCaseConductivity.add(k).add(caseAirConductivity)).divideEquals(k.add(caseAirConductivity)).multiply(injectionRate);
    }

    public FloatingLong getMaxCasingTemperature(boolean active) {
        FloatingLong k = active ? caseWaterConductivity : FloatingLong.ZERO;
        return MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().multiply(injectionRate).divideEquals(k.add(caseAirConductivity));
    }

    // burn temp x energy per fuel x burn ratio x (plasma case conductivity + k + air conductivity) / (
    public FloatingLong getIgnitionTemperature(boolean active) {
        FloatingLong k = active ? caseWaterConductivity : FloatingLong.ZERO;
        FloatingLong totalConductivity = plasmaCaseConductivity.add(k).add(caseAirConductivity);
        FloatingLong energyPerFusionFuel = MekanismGeneratorsConfig.generators.energyPerFusionFuel.get();
        return burnTemperature.multiply(energyPerFusionFuel).timesEqual(burnRatio).timesEqual(totalConductivity).divideEquals(
              energyPerFusionFuel.multiply(burnRatio).timesEqual(totalConductivity).minusEqual(plasmaCaseConductivity.multiply(k.add(caseAirConductivity))));
    }

    // thermocouple efficiency x air conductivity x temp
    public FloatingLong getPassiveGeneration(boolean active, boolean current) {
        FloatingLong temperature = current ? getCaseTemp() : getMaxCasingTemperature(active);
        return thermocoupleEfficiency.multiply(caseAirConductivity).timesEqual(temperature);
    }

    // steam efficiency x water conductivity x temp / water enthalpy
    public int getSteamPerTick(boolean current) {
        FloatingLong temperature = current ? getCaseTemp() : getMaxCasingTemperature(true);
        //TODO: Switch this to long, when we move gases to being able to be stored as longs
        return steamTransferEfficiency.multiply(caseWaterConductivity).timesEqual(temperature).divideEquals(HeatUtils.getVaporizationEnthalpy()).intValue();
    }

    public static FloatingLong getInverseConductionCoefficient() {
        return FloatingLong.ONE.divide(caseAirConductivity);
    }
}