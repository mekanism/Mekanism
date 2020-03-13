package mekanism.generators.common;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.LaserManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
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

    private static final int MAX_INJECTION = 98;//this is the effective cap in the GUI, as text field is limited to 2 chars
    //Reaction characteristics
    private static double burnTemperature = TemperatureUnit.AMBIENT.convertFromK(1E8, true);
    private static double burnRatio = 1;
    //Thermal characteristics
    private static double plasmaHeatCapacity = 100;
    private static double caseHeatCapacity = 1;
    private static double enthalpyOfVaporization = 10;
    private static double thermocoupleEfficiency = 0.05;
    private static double steamTransferEfficiency = 0.1;
    //Heat transfer metrics
    private static double plasmaCaseConductivity = 0.2;
    private static double caseWaterConductivity = 0.3;
    private static double caseAirConductivity = 0.1;
    public TileEntityReactorController controller;
    private Set<TileEntityReactorBlock> reactorBlocks = new ObjectOpenHashSet<>();
    private Set<IHeatTransfer> heatTransfers = new ObjectOpenHashSet<>();
    //Current stores of temperature - internally uses ambient-relative kelvin units
    private double plasmaTemperature;
    private double caseTemperature;
    //Last values of temperature
    private double lastPlasmaTemperature;
    private double lastCaseTemperature;
    private double heatToAbsorb = 0;
    private int injectionRate = 0;
    private boolean burning = false;
    private boolean activelyCooled = true;

    private boolean updatedThisTick;

    public boolean formed = false;

    public FusionReactor(TileEntityReactorController c) {
        controller = c;
    }

    public void addTemperatureFromEnergyInput(double energyAdded) {
        plasmaTemperature += energyAdded / plasmaHeatCapacity * (isBurning() ? 1 : 10);
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
        updatedThisTick = false;

        //Only thermal transfer happens unless we're hot enough to burn.
        if (plasmaTemperature >= burnTemperature) {
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
        lastPlasmaTemperature = plasmaTemperature < 0.1 ? 0 : plasmaTemperature;
        lastCaseTemperature = caseTemperature < 0.1 ? 0 : caseTemperature;
    }

    private void vaporiseHohlraum() {
        IInventorySlot reactorSlot = controller.getReactorSlot();
        ItemStack hohlraum = reactorSlot.getStack();
        Optional<IGasHandler> capability = MekanismUtils.toOptional(hohlraum.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            if (gasHandlerItem.getGasTankCount() > 0) {
                getFuelTank().insert(gasHandlerItem.getGasInTank(0), Action.EXECUTE, AutomationType.INTERNAL);
                lastPlasmaTemperature = plasmaTemperature;
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
        int fuelBurned = (int) Math.min(getFuelTank().getStored(), Math.max(0, lastPlasmaTemperature - burnTemperature) * burnRatio);
        if (getFuelTank().shrinkStack(fuelBurned, Action.EXECUTE) != fuelBurned) {
            //TODO: Print warning/error
        }
        plasmaTemperature += MekanismGeneratorsConfig.generators.energyPerFusionFuel.get() * fuelBurned / plasmaHeatCapacity;
        return fuelBurned;
    }

    private void transferHeat() {
        //Transfer from plasma to casing
        double plasmaCaseHeat = plasmaCaseConductivity * (lastPlasmaTemperature - lastCaseTemperature);
        plasmaTemperature -= plasmaCaseHeat / plasmaHeatCapacity;
        caseTemperature += plasmaCaseHeat / caseHeatCapacity;

        //Transfer from casing to water if necessary
        if (activelyCooled) {
            double caseWaterHeat = caseWaterConductivity * lastCaseTemperature;
            int waterToVaporize = (int) (steamTransferEfficiency * caseWaterHeat / enthalpyOfVaporization);
            waterToVaporize = Math.min(waterToVaporize, Math.min(getWaterTank().getFluidAmount(), getSteamTank().getNeeded()));
            if (waterToVaporize > 0) {
                if (getWaterTank().shrinkStack(waterToVaporize, Action.EXECUTE) != waterToVaporize) {
                    //TODO: Print warning/error
                }
                getSteamTank().insert(MekanismGases.STEAM.getGasStack(waterToVaporize), Action.EXECUTE, AutomationType.INTERNAL);
            }

            caseWaterHeat = waterToVaporize * enthalpyOfVaporization / steamTransferEfficiency;
            caseTemperature -= caseWaterHeat / caseHeatCapacity;
            for (IHeatTransfer source : heatTransfers) {
                source.simulateHeat();
            }
            applyTemperatureChange();
        }

        //Transfer from casing to environment
        double caseAirHeat = caseAirConductivity * lastCaseTemperature;
        caseTemperature -= caseAirHeat / caseHeatCapacity;
        setBufferedEnergy(getBufferedEnergy() + caseAirHeat * thermocoupleEfficiency);
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

    public double getBufferedEnergy() {
        return controller.getEnergy();
    }

    public void setBufferedEnergy(double energy) {
        controller.setEnergy(energy);
    }

    public double getPlasmaTemp() {
        return lastPlasmaTemperature;
    }

    public void setLastPlasmaTemp(double temp) {
        lastPlasmaTemperature = temp;
    }

    public void setPlasmaTemp(double temp) {
        plasmaTemperature = temp;
    }

    public double getCaseTemp() {
        return lastCaseTemperature;
    }

    public void setLastCaseTemp(double temp) {
        lastCaseTemperature = temp;
    }

    public void setCaseTemp(double temp) {
        caseTemperature = temp;
    }

    public double getBufferSize() {
        return controller.getMaxEnergy();
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
        updatedThisTick = true;
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
                reactorBlocks.add((TileEntityReactorBlock) tile);
                ((TileEntityReactorBlock) tile).setReactor(this);
                //TODO: Does this need to check capability instead
                if (tile instanceof IHeatTransfer) {
                    heatTransfers.add((IHeatTransfer) tile);
                }
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
                      (MekanismGeneratorsConfig.generators.energyPerFusionFuel.get() * burnRatio * (plasmaCaseConductivity + k + caseAirConductivity) -
                       plasmaCaseConductivity * (k + caseAirConductivity));
        return (int) (2 * Math.ceil(aMin / 2D));
    }

    public double getMaxPlasmaTemperature(boolean active) {
        double k = active ? caseWaterConductivity : 0;
        return injectionRate * MekanismGeneratorsConfig.generators.energyPerFusionFuel.get() / plasmaCaseConductivity *
               (plasmaCaseConductivity + k + caseAirConductivity) / (k + caseAirConductivity);
    }

    public double getMaxCasingTemperature(boolean active) {
        double k = active ? caseWaterConductivity : 0;
        return injectionRate * MekanismGeneratorsConfig.generators.energyPerFusionFuel.get() / (k + caseAirConductivity);
    }

    public double getIgnitionTemperature(boolean active) {
        double k = active ? caseWaterConductivity : 0;
        return burnTemperature * MekanismGeneratorsConfig.generators.energyPerFusionFuel.get() * burnRatio * (plasmaCaseConductivity + k + caseAirConductivity) /
               (MekanismGeneratorsConfig.generators.energyPerFusionFuel.get() * burnRatio * (plasmaCaseConductivity + k + caseAirConductivity) -
                plasmaCaseConductivity * (k + caseAirConductivity));
    }

    public double getPassiveGeneration(boolean active, boolean current) {
        double temperature = current ? caseTemperature : getMaxCasingTemperature(active);
        return thermocoupleEfficiency * caseAirConductivity * temperature;
    }

    public int getSteamPerTick(boolean current) {
        double temperature = current ? caseTemperature : getMaxCasingTemperature(true);
        return (int) (steamTransferEfficiency * caseWaterConductivity * temperature / enthalpyOfVaporization);
    }

    public double getTemp() {
        return lastCaseTemperature;
    }

    public double getInverseConductionCoefficient() {
        return 1 / caseAirConductivity;
    }

    public double getInsulationCoefficient(Direction side) {
        return 100_000;
    }

    public void transferHeatTo(double heat) {
        heatToAbsorb += heat;
    }

    public double[] simulateHeat() {
        return null;
    }

    public double applyTemperatureChange() {
        caseTemperature += heatToAbsorb / caseHeatCapacity;
        heatToAbsorb = 0;
        return caseTemperature;
    }

    public boolean canConnectHeat(Direction side) {
        return false;
    }

    public IHeatTransfer getAdjacent(Direction side) {
        return null;
    }
}