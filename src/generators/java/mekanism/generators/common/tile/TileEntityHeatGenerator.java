package mekanism.generators.common.tile;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.math.MathUtils;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.HeatCapacitorHelper;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.listener.ConfigBasedCachedLongSupplier;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerHeatCapacitorWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.slot.FluidFuelInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityHeatGenerator extends TileEntityGenerator {

    public static final double HEAT_CAPACITY = 10;
    public static final double INVERSE_CONDUCTION_COEFFICIENT = 5;
    public static final double INVERSE_INSULATION_COEFFICIENT = 100;
    private static final double THERMAL_EFFICIENCY = 0.5;
    //Default configs this is 510 compared to the previous 500
    private static final ConfigBasedCachedLongSupplier MAX_PRODUCTION = new ConfigBasedCachedLongSupplier(() -> {
        long passiveMax = MekanismGeneratorsConfig.generators.heatGenerationLava.get() * (EnumUtils.DIRECTIONS.length + 1);
        passiveMax += MekanismGeneratorsConfig.generators.heatGenerationNether.get();
        return passiveMax + MekanismGeneratorsConfig.generators.heatGeneration.get();
    }, MekanismGeneratorsConfig.generators.heatGeneration, MekanismGeneratorsConfig.generators.heatGenerationLava, MekanismGeneratorsConfig.generators.heatGenerationNether);

    /**
     * The FluidTank for this generator.
     */
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getLava", "getLavaCapacity", "getLavaNeeded",
                                                                                     "getLavaFilledPercentage"}, docPlaceholder = "lava tank")
    public BasicFluidTank lavaTank;
    private long producingEnergy = 0;
    private double lastTransferLoss;
    private double lastEnvironmentLoss;

    @WrappingComputerMethod(wrapper = ComputerHeatCapacitorWrapper.class, methodNames = "getTemperature", docPlaceholder = "generator")
    BasicHeatCapacitor heatCapacitor;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFuelItem", docPlaceholder = "fuel item slot")
    FluidFuelInventorySlot fuelSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy item slot")
    EnergyInventorySlot energySlot;

    public TileEntityHeatGenerator(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.HEAT_GENERATOR, pos, state, MAX_PRODUCTION);
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        FluidTankHelper builder = FluidTankHelper.forSide(facingSupplier);
        builder.addTank(lavaTank = VariableCapacityFluidTank.input(MekanismGeneratorsConfig.generators.heatTankCapacity,
                    fluidStack -> fluidStack.is(FluidTags.LAVA), listener), RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.BACK,
              RelativeSide.TOP, RelativeSide.BOTTOM);
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        //Divide the burn time by 20 as that is the ratio of how much a bucket of lava would burn for
        //TODO: Eventually we may want to grab the 20 dynamically in case some mod is changing the burn time of a lava bucket
        builder.addSlot(fuelSlot = FluidFuelInventorySlot.forFuel(lavaTank, stack -> stack.getBurnTime(null) / 20, size -> new FluidStack(Fluids.LAVA, size),
              listener, 17, 35), RelativeSide.FRONT, RelativeSide.LEFT, RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        builder.addSlot(energySlot = EnergyInventorySlot.drain(getEnergyContainer(), listener, 143, 35), RelativeSide.RIGHT);
        return builder.build();
    }

    @NotNull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        HeatCapacitorHelper builder = HeatCapacitorHelper.forSide(facingSupplier);
        builder.addCapacitor(heatCapacitor = BasicHeatCapacitor.create(HEAT_CAPACITY, INVERSE_CONDUCTION_COEFFICIENT, INVERSE_INSULATION_COEFFICIENT, ambientTemperature, listener));
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.drainContainer();
        fuelSlot.fillOrBurn();
        long prev = getEnergyContainer().getEnergy();
        heatCapacitor.handleHeat(getBoost());
        if (canFunction() && getEnergyContainer().getNeeded() > 0L) {
            int fluidRate = MekanismGeneratorsConfig.generators.heatGenerationFluidRate.get();
            if (lavaTank.extract(fluidRate, Action.SIMULATE, AutomationType.INTERNAL).getAmount() == fluidRate) {
                setActive(true);
                lavaTank.extract(fluidRate, Action.EXECUTE, AutomationType.INTERNAL);
                heatCapacitor.handleHeat(MekanismGeneratorsConfig.generators.heatGeneration.get());
            } else {
                setActive(false);
            }
        } else {
            setActive(false);
        }
        HeatTransfer loss = simulate();
        lastTransferLoss = loss.adjacentTransfer();
        lastEnvironmentLoss = loss.environmentTransfer();
        producingEnergy = getEnergyContainer().getEnergy() - prev;
        return sendUpdatePacket;
    }

    private double getBoost() {
        if (level == null) {
            return 0L;
        }
        long boost;
        long passiveLavaAmount = MekanismGeneratorsConfig.generators.heatGenerationLava.get();
        if (passiveLavaAmount == 0L) {
            //If neighboring lava blocks produce no energy, don't bother checking the sides for them
            boost = 0L;
        } else {
            //Otherwise, calculate boost to apply from lava
            //Only check and add loaded neighbors to the which sides have lava on them
            MutableBlockPos mutable = new MutableBlockPos();
            int lavaSides = 0;
            for (Direction dir : EnumUtils.DIRECTIONS) {
                //Only check and add loaded neighbors to the which sides have lava on them
                mutable.setWithOffset(worldPosition, dir);
                if (WorldUtils.getFluidState(level, mutable).filter(state -> state.is(FluidTags.LAVA)).isPresent()) {
                    lavaSides++;
                }
            }
            if (getBlockState().getFluidState().is(FluidTags.LAVA)) {
                //If the heat generator is lava-logged then add it as another side that is adjacent to lava for the heat calculations
                lavaSides++;
            }
            boost = passiveLavaAmount * lavaSides;
        }
        if (level.dimensionType().ultraWarm()) {
            boost += MekanismGeneratorsConfig.generators.heatGenerationNether.get();
        }
        return boost;
    }

    @Override
    public double getInverseInsulation(int capacitor, @Nullable Direction side) {
        return side == Direction.DOWN ? HeatAPI.DEFAULT_INVERSE_INSULATION : super.getInverseInsulation(capacitor, side);
    }

    @Override
    public double getTotalInverseInsulation(@Nullable Direction side) {
        return side == Direction.DOWN ? HeatAPI.DEFAULT_INVERSE_INSULATION : super.getTotalInverseInsulation(side);
    }

    @NotNull
    @Override
    public HeatTransfer simulate() {
        double ambientTemp = ambientTemperature.getAsDouble();
        double temp = getTotalTemperature();
        // 1 - Qc / Qh
        double carnotEfficiency = 1 - Math.min(ambientTemp, temp) / Math.max(ambientTemp, temp);
        double heatLost = THERMAL_EFFICIENCY * (temp - ambientTemp);
        heatCapacitor.handleHeat(-heatLost);
        long energyFromHeat = MathUtils.clampToLong(Math.abs(heatLost) * carnotEfficiency);
        getEnergyContainer().insert(Math.min(energyFromHeat, MAX_PRODUCTION.getAsLong()), Action.EXECUTE, AutomationType.INTERNAL);
        return super.simulate();
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(@NotNull Direction side) {
        return side == Direction.DOWN ? getAdjacentUnchecked(side) : null;
    }

    @Override
    public long getProductionRate() {
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
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return type == ContainerType.FLUID;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableLong.create(this::getProductionRate, value -> producingEnergy = value));
        container.track(SyncableDouble.create(this::getLastTransferLoss, value -> lastTransferLoss = value));
        container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> lastEnvironmentLoss = value));
    }
}
