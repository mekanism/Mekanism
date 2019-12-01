package mekanism.generators.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.LaserManager;
import mekanism.common.Mekanism;
import mekanism.common.MekanismFluids;
import mekanism.common.MekanismGases;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.item.ItemHohlraum;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FusionReactor {

    public static final int MAX_INJECTION = 98;//this is the effective cap in the GUI, as text field is limited to 2 chars
    //Reaction characteristics
    public static double burnTemperature = TemperatureUnit.AMBIENT.convertFromK(1E8, true);
    public static double burnRatio = 1;
    //Thermal characteristics
    public static double plasmaHeatCapacity = 100;
    public static double caseHeatCapacity = 1;
    public static double enthalpyOfVaporization = 10;
    public static double thermocoupleEfficiency = 0.05;
    public static double steamTransferEfficiency = 0.1;
    //Heat transfer metrics
    public static double plasmaCaseConductivity = 0.2;
    public static double caseWaterConductivity = 0.3;
    public static double caseAirConductivity = 0.1;
    public TileEntityReactorController controller;
    public Set<TileEntityReactorBlock> reactorBlocks = new HashSet<>();
    public Set<IHeatTransfer> heatTransfers = new HashSet<>();
    //Current stores of temperature - internally uses ambient-relative kelvin units
    public double plasmaTemperature;
    public double caseTemperature;
    //Last values of temperature
    public double lastPlasmaTemperature;
    public double lastCaseTemperature;
    public double heatToAbsorb = 0;
    public int injectionRate = 0;
    public boolean burning = false;
    public boolean activelyCooled = true;

    public boolean updatedThisTick;

    public boolean formed = false;

    public FusionReactor(TileEntityReactorController c) {
        controller = c;
    }

    public void addTemperatureFromEnergyInput(double energyAdded) {
        plasmaTemperature += energyAdded / plasmaHeatCapacity * (isBurning() ? 1 : 10);
    }

    public boolean hasHohlraum() {
        if (controller != null && !controller.getReactorSlot().isEmpty()) {
            ItemStack hohlraum = controller.getReactorSlot().getStack();
            if (hohlraum.getItem() instanceof ItemHohlraum) {
                GasStack gasStack = ((ItemHohlraum) hohlraum.getItem()).getGas(hohlraum);
                return !gasStack.isEmpty() && gasStack.getType() == MekanismGases.FUSION_FUEL.getGas() && gasStack.getAmount() == ItemHohlraum.MAX_GAS;
            }
        }
        return false;
    }

    public void simulate() {
        if (controller.getWorld().isRemote) {
            lastPlasmaTemperature = plasmaTemperature;
            lastCaseTemperature = caseTemperature;
            return;
        }

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
                    burning = false;
                }
            }
        } else {
            burning = false;
        }

        //Perform the heat transfer calculations
        transferHeat();

        if (burning) {
            kill();
        }
        updateTemperatures();
    }

    public void updateTemperatures() {
        lastPlasmaTemperature = plasmaTemperature < 1E-1 ? 0 : plasmaTemperature;
        lastCaseTemperature = caseTemperature < 1E-1 ? 0 : caseTemperature;
    }

    public void vaporiseHohlraum() {
        IInventorySlot reactorSlot = controller.getReactorSlot();
        ItemStack hohlraum = reactorSlot.getStack();
        getFuelTank().fill(((ItemHohlraum) hohlraum.getItem()).getGas(hohlraum), Action.EXECUTE);
        lastPlasmaTemperature = plasmaTemperature;
        reactorSlot.setStack(ItemStack.EMPTY);
        burning = true;
    }

    public void injectFuel() {
        int amountNeeded = getFuelTank().getNeeded();
        int amountAvailable = 2 * Math.min(getDeuteriumTank().getStored(), getTritiumTank().getStored());
        int amountToInject = Math.min(amountNeeded, Math.min(amountAvailable, injectionRate));
        amountToInject -= amountToInject % 2;
        getDeuteriumTank().drain(amountToInject / 2, Action.EXECUTE);
        getTritiumTank().drain(amountToInject / 2, Action.EXECUTE);
        getFuelTank().fill(MekanismGases.FUSION_FUEL.getGasStack(amountToInject), Action.EXECUTE);
    }

    public int burnFuel() {
        int fuelBurned = (int) Math.min(getFuelTank().getStored(), Math.max(0, lastPlasmaTemperature - burnTemperature) * burnRatio);
        getFuelTank().drain(fuelBurned, Action.EXECUTE);
        plasmaTemperature += MekanismGeneratorsConfig.generators.energyPerFusionFuel.get() * fuelBurned / plasmaHeatCapacity;
        return fuelBurned;
    }

    public void transferHeat() {
        //Transfer from plasma to casing
        double plasmaCaseHeat = plasmaCaseConductivity * (lastPlasmaTemperature - lastCaseTemperature);
        plasmaTemperature -= plasmaCaseHeat / plasmaHeatCapacity;
        caseTemperature += plasmaCaseHeat / caseHeatCapacity;

        //Transfer from casing to water if necessary
        if (activelyCooled) {
            double caseWaterHeat = caseWaterConductivity * lastCaseTemperature;
            int waterToVaporize = (int) (steamTransferEfficiency * caseWaterHeat / enthalpyOfVaporization);
            waterToVaporize = Math.min(waterToVaporize, Math.min(getWaterTank().getFluidAmount(), getSteamTank().getCapacity() - getSteamTank().getFluidAmount()));
            if (waterToVaporize > 0) {
                getWaterTank().drain(waterToVaporize, FluidAction.EXECUTE);
                getSteamTank().fill(MekanismFluids.STEAM.getFluidStack(waterToVaporize), FluidAction.EXECUTE);
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

    @Nullable
    public FluidTank getWaterTank() {
        return controller == null ? null : controller.waterTank;
    }

    @Nullable
    public FluidTank getSteamTank() {
        return controller == null ? null : controller.steamTank;
    }

    public GasTank getDeuteriumTank() {
        return controller.deuteriumTank;
    }

    public GasTank getTritiumTank() {
        return controller.tritiumTank;
    }

    public GasTank getFuelTank() {
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

    public void setPlasmaTemp(double temp) {
        plasmaTemperature = temp;
    }

    public double getCaseTemp() {
        return lastCaseTemperature;
    }

    public void setCaseTemp(double temp) {
        caseTemperature = temp;
    }

    public double getBufferSize() {
        return controller.getMaxEnergy();
    }

    public void kill() {
        AxisAlignedBB death_zone = new AxisAlignedBB(controller.getPos().getX() - 1, controller.getPos().getY() - 3,
              controller.getPos().getZ() - 1, controller.getPos().getX() + 2, controller.getPos().getY(), controller.getPos().getZ() + 2);
        List<Entity> entitiesToDie = controller.getWorld().getEntitiesWithinAABB(Entity.class, death_zone);

        for (Entity entity : entitiesToDie) {
            entity.attackEntityFrom(DamageSource.MAGIC, 50000F);
        }
    }

    public void unformMultiblock(boolean keepBurning) {
        for (TileEntityReactorBlock block : reactorBlocks) {
            block.setReactor(null);
        }

        //Don't remove from controller
        controller.setReactor(this);
        reactorBlocks.clear();
        formed = false;
        burning = burning && keepBurning;

        if (!controller.getWorld().isRemote) {
            Mekanism.packetHandler.sendToDimension(new PacketTileEntity(controller), controller.getWorld().getDimension().getType());
        }
    }

    public void formMultiblock(boolean keepBurning) {
        updatedThisTick = true;
        Coord4D controllerPosition = Coord4D.get(controller);
        Coord4D centreOfReactor = controllerPosition.offset(Direction.DOWN, 2);
        unformMultiblock(true);
        reactorBlocks.add(controller);

        if (!createFrame(centreOfReactor) || !addSides(centreOfReactor) || !centerIsClear(centreOfReactor)) {
            unformMultiblock(keepBurning);
            return;
        }

        formed = true;

        if (!controller.getWorld().isRemote) {
            Mekanism.packetHandler.sendToDimension(new PacketTileEntity(controller), controller.getWorld().getDimension().getType());
        }
    }

    public boolean createFrame(Coord4D center) {
        int[][] positions = new int[][]{
              {+2, +2, +0}, {+2, +1, +1}, {+2, +0, +2}, {+2, -1, +1}, {+2, -2, +0}, {+2, -1, -1}, {+2, +0, -2}, {+2, +1, -1}, {+1, +2, +1}, {+1, +1, +2}, {+1, -1, +2},
              {+1, -2, +1}, {+1, -2, -1}, {+1, -1, -2}, {+1, +1, -2}, {+1, +2, -1}, {+0, +2, +2}, {+0, -2, +2}, {+0, -2, -2}, {+0, +2, -2}, {-1, +2, +1}, {-1, +1, +2},
              {-1, -1, +2}, {-1, -2, +1}, {-1, -2, -1}, {-1, -1, -2}, {-1, +1, -2}, {-1, +2, -1}, {-2, +2, +0}, {-2, +1, +1}, {-2, +0, +2}, {-2, -1, +1}, {-2, -2, +0},
              {-2, -1, -1}, {-2, +0, -2}, {-2, +1, -1},};
        BlockPos centerPos = center.getPos();
        for (int[] coords : positions) {
            TileEntityReactorBlock tile = MekanismUtils.getTileEntity(TileEntityReactorBlock.class, controller.getWorld(), centerPos.add(coords[0], coords[1], coords[2]));;
            if (tile == null || !tile.isFrame()) {
                return false;
            }
            reactorBlocks.add(tile);
            tile.setReactor(this);
        }
        return true;
    }

    public boolean addSides(Coord4D center) {
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

    public boolean centerIsClear(Coord4D center) {
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

        controller.waterTank.setCapacity(TileEntityReactorController.MAX_WATER * capRate);
        controller.steamTank.setCapacity(TileEntityReactorController.MAX_STEAM * capRate);

        FluidStack waterTankFluid = controller.waterTank.getFluid();
        if (!waterTankFluid.isEmpty()) {
            waterTankFluid.setAmount(Math.min(waterTankFluid.getAmount(), controller.waterTank.getCapacity()));
        }
        FluidStack steamTankFluid = controller.steamTank.getFluid();
        if (!steamTankFluid.isEmpty()) {
            steamTankFluid.setAmount(Math.min(steamTankFluid.getAmount(), controller.steamTank.getCapacity()));
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