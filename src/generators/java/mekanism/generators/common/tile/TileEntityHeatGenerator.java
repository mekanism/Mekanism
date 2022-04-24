package mekanism.generators.common.tile;

import java.util.Arrays;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.HeatCapacitorHelper;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerHeatCapacitorWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.slot.FluidFuelInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityHeatGenerator extends TileEntityGenerator {

    public static final int MAX_FLUID = 24_000;
    private static final double THERMAL_EFFICIENCY = 0.5;
    private static final FloatingLong MAX_PRODUCTION = FloatingLong.createConst(500);

    /**
     * The FluidTank for this generator.
     */
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getLava", "getLavaCapacity", "getLavaNeeded", "getLavaFilledPercentage"})
    public BasicFluidTank lavaTank;
    private FloatingLong producingEnergy = FloatingLong.ZERO;
    private double lastTransferLoss;
    private double lastEnvironmentLoss;

    @WrappingComputerMethod(wrapper = ComputerHeatCapacitorWrapper.class, methodNames = "getTemperature")
    private BasicHeatCapacitor heatCapacitor;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFuelItem")
    private FluidFuelInventorySlot fuelSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityHeatGenerator(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.HEAT_GENERATOR, pos, state, MekanismGeneratorsConfig.generators.heatGeneration.get().multiply(2));
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
        builder.addTank(lavaTank = BasicFluidTank.create(MAX_FLUID, fluidStack -> MekanismTags.Fluids.LAVA_LOOKUP.contains(fluidStack.getFluid()), listener),
              RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        //Divide the burn time by 20 as that is the ratio of how much a bucket of lava would burn for
        // Eventually we may want to grab the 20 dynamically in case some mod is changing the burn time of a lava bucket
        builder.addSlot(fuelSlot = FluidFuelInventorySlot.forFuel(lavaTank, stack -> ForgeHooks.getBurnTime(stack, null) / 20, size -> new FluidStack(Fluids.LAVA, size),
              listener, 17, 35), RelativeSide.FRONT, RelativeSide.LEFT, RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        builder.addSlot(energySlot = EnergyInventorySlot.drain(getEnergyContainer(), listener, 143, 35), RelativeSide.RIGHT);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        HeatCapacitorHelper builder = HeatCapacitorHelper.forSide(this::getDirection);
        builder.addCapacitor(heatCapacitor = BasicHeatCapacitor.create(10, 5, 100, ambientTemperature, listener));
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.drainContainer();
        fuelSlot.fillOrBurn();
        FloatingLong prev = getEnergyContainer().getEnergy().copyAsConst();
        heatCapacitor.handleHeat(getBoost().doubleValue());
        if (MekanismUtils.canFunction(this) && !getEnergyContainer().getNeeded().isZero()) {
            int fluidRate = MekanismGeneratorsConfig.generators.heatGenerationFluidRate.get();
            if (lavaTank.extract(fluidRate, Action.SIMULATE, AutomationType.INTERNAL).getAmount() == fluidRate) {
                setActive(true);
                lavaTank.extract(fluidRate, Action.EXECUTE, AutomationType.INTERNAL);
                heatCapacitor.handleHeat(MekanismGeneratorsConfig.generators.heatGeneration.get().doubleValue());
            } else {
                setActive(false);
            }
        } else {
            setActive(false);
        }
        HeatTransfer loss = simulate();
        lastTransferLoss = loss.adjacentTransfer();
        lastEnvironmentLoss = loss.environmentTransfer();
        producingEnergy = getEnergyContainer().getEnergy().subtract(prev);
    }

    private FloatingLong getBoost() {
        if (level == null) {
            return FloatingLong.ZERO;
        }
        FloatingLong boost;
        FloatingLong passiveLavaAmount = MekanismGeneratorsConfig.generators.heatGenerationLava.get();
        if (passiveLavaAmount.isZero()) {
            //If neighboring lava blocks produce no energy, don't bother checking the sides for them
            boost = FloatingLong.ZERO;
        } else {
            //Otherwise, calculate boost to apply from lava
            long lavaSides = Arrays.stream(EnumUtils.DIRECTIONS).filter(side -> {
                //Only check and add loaded neighbors to the which sides have lava on them
                Optional<FluidState> fluidState = WorldUtils.getFluidState(level, worldPosition.relative(side));
                return fluidState.isPresent() && fluidState.get().is(FluidTags.LAVA);
            }).count();
            if (getBlockState().getFluidState().is(FluidTags.LAVA)) {
                //If the heat generator is lava-logged then add it as another side that is adjacent to lava for the heat calculations
                lavaSides++;
            }
            boost = passiveLavaAmount.multiply(lavaSides);
        }
        if (level.dimensionType().ultraWarm()) {
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
        double ambientTemp = ambientTemperature.getAsDouble();
        double temp = getTotalTemperature();
        // 1 - Qc / Qh
        double carnotEfficiency = 1 - Math.min(ambientTemp, temp) / Math.max(ambientTemp, temp);
        double heatLost = THERMAL_EFFICIENCY * (temp - ambientTemp);
        heatCapacitor.handleHeat(-heatLost);
        getEnergyContainer().insert(MAX_PRODUCTION.min(FloatingLong.create(Math.abs(heatLost) * carnotEfficiency)), Action.EXECUTE, AutomationType.INTERNAL);
        return super.simulate();
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(@Nonnull Direction side) {
        if (side == Direction.DOWN) {
            BlockEntity adj = WorldUtils.getTileEntity(getLevel(), worldPosition.below());
            return CapabilityUtils.getCapability(adj, Capabilities.HEAT_HANDLER_CAPABILITY, side.getOpposite()).resolve().orElse(null);
        }
        return null;
    }

    @ComputerMethod(nameOverride = "getProductionRate")
    public FloatingLong getProducingEnergy() {
        return producingEnergy;
    }

    @ComputerMethod(nameOverride = "getTransferLoss")
    public double getLastTransferLoss() {
        return lastTransferLoss;
    }

    @ComputerMethod(nameOverride = "getEnvironmentalLoss")
    public double getLastEnvironmentLoss() {
        return lastEnvironmentLoss;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(lavaTank.getFluidAmount(), lavaTank.getCapacity());
    }

    @Override
    protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
        return type == SubstanceType.FLUID;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFloatingLong.create(this::getProducingEnergy, value -> producingEnergy = value));
        container.track(SyncableDouble.create(this::getLastTransferLoss, value -> lastTransferLoss = value));
        container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> lastEnvironmentLoss = value));
    }
}