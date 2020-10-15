package mekanism.generators.common.tile;

import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.HeatCapacitorHelper;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.slot.FluidFuelInventorySlot;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityHeatGenerator extends TileEntityGenerator {

    private static final int MAX_FLUID = 24_000;
    private static final int FLUID_RATE = 10;
    private static final double THERMAL_EFFICIENCY = 0.5;
    private static final FloatingLong MAX_PRODUCTION = FloatingLong.createConst(500);
    /**
     * The FluidTank for this generator.
     */
    public BasicFluidTank lavaTank;
    private FloatingLong producingEnergy = FloatingLong.ZERO;
    private double lastTransferLoss;
    private double lastEnvironmentLoss;

    private BasicHeatCapacitor heatCapacitor;
    private FluidFuelInventorySlot fuelSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityHeatGenerator() {
        super(GeneratorsBlocks.HEAT_GENERATOR, MekanismGeneratorsConfig.generators.heatGeneration.get().multiply(2));
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
        builder.addTank(lavaTank = BasicFluidTank.create(MAX_FLUID, fluidStack -> fluidStack.getFluid().isIn(FluidTags.LAVA), this), RelativeSide.LEFT,
              RelativeSide.RIGHT, RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        //Divide the burn time by 20 as that is the ratio of how much a bucket of lava would burn for
        // Eventually we may want to grab the 20 dynamically in case some mod is changing the burn time of a lava bucket
        builder.addSlot(fuelSlot = FluidFuelInventorySlot.forFuel(lavaTank, stack -> ForgeHooks.getBurnTime(stack) / 20, size -> new FluidStack(Fluids.LAVA, size),
              this, 17, 35), RelativeSide.FRONT, RelativeSide.LEFT, RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        builder.addSlot(energySlot = EnergyInventorySlot.drain(getEnergyContainer(), this, 143, 35), RelativeSide.RIGHT);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors() {
        HeatCapacitorHelper builder = HeatCapacitorHelper.forSide(this::getDirection);
        builder.addCapacitor(heatCapacitor = BasicHeatCapacitor.create(10, 5, 100, this));
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.drainContainer();
        fuelSlot.fillOrBurn();
        FloatingLong prev = getEnergyContainer().getEnergy().copy();
        heatCapacitor.handleHeat(getBoost().doubleValue());
        if (MekanismUtils.canFunction(this) && !getEnergyContainer().getNeeded().isZero() &&
            lavaTank.extract(FLUID_RATE, Action.SIMULATE, AutomationType.INTERNAL).getAmount() == FLUID_RATE) {
            setActive(true);
            lavaTank.extract(FLUID_RATE, Action.EXECUTE, AutomationType.INTERNAL);
            heatCapacitor.handleHeat(MekanismGeneratorsConfig.generators.heatGeneration.get().doubleValue());
        } else {
            setActive(false);
        }
        HeatTransfer loss = simulate();
        lastTransferLoss = loss.getAdjacentTransfer();
        lastEnvironmentLoss = loss.getEnvironmentTransfer();
        producingEnergy = getEnergyContainer().getEnergy().subtract(prev);
    }

    private FloatingLong getBoost() {
        if (world == null) {
            return FloatingLong.ZERO;
        }
        //Lava boost
        long lavaSides = Arrays.stream(EnumUtils.DIRECTIONS).filter(side -> world.getFluidState(pos.offset(side)).isTagged(FluidTags.LAVA)).count();
        if (getBlockState().getFluidState().isTagged(FluidTags.LAVA)) {
            //If the heat generator is lava-logged then add it as another side that is adjacent to lava for the heat calculations
            lavaSides++;
        }
        FloatingLong boost = MekanismGeneratorsConfig.generators.heatGenerationLava.get().multiply(lavaSides);
        if (world.getDimensionType().isUltrawarm()) {
            boost = boost.plusEqual(MekanismGeneratorsConfig.generators.heatGenerationNether.get());
        }
        return boost;
    }

    @Override
    public double getInverseInsulation(int capacitor, @Nullable Direction side) {
        return side == Direction.DOWN ? 0 : super.getInverseInsulation(capacitor, side);
    }

    @Nonnull
    @Override
    public HeatTransfer simulate() {
        double temp = getTotalTemperature();
        // 1 - Qc / Qh
        double carnotEfficiency = 1 - Math.min(HeatAPI.AMBIENT_TEMP, temp) / Math.max(HeatAPI.AMBIENT_TEMP, temp);
        double heatLost = THERMAL_EFFICIENCY * (temp - HeatAPI.AMBIENT_TEMP);
        heatCapacitor.handleHeat(-heatLost);
        getEnergyContainer().insert(MAX_PRODUCTION.min(FloatingLong.create(Math.abs(heatLost) * carnotEfficiency)), Action.EXECUTE, AutomationType.INTERNAL);
        return super.simulate();
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(Direction side) {
        if (side == Direction.DOWN) {
            TileEntity adj = MekanismUtils.getTileEntity(getWorld(), pos.down());
            return CapabilityUtils.getCapability(adj, Capabilities.HEAT_HANDLER_CAPABILITY, side.getOpposite()).resolve().orElse(null);
        }
        return null;
    }

    public FloatingLong getProducingEnergy() {
        return producingEnergy;
    }

    public double getLastTransferLoss() {
        return lastTransferLoss;
    }

    public double getLastEnvironmentLoss() {
        return lastEnvironmentLoss;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(lavaTank.getFluidAmount(), lavaTank.getCapacity());
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFloatingLong.create(this::getProducingEnergy, value -> producingEnergy = value));
        container.track(SyncableDouble.create(this::getLastTransferLoss, value -> lastTransferLoss = value));
        container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> lastEnvironmentLoss = value));
    }
}