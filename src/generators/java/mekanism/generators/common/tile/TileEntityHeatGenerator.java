package mekanism.generators.common.tile;

import com.google.common.primitives.Ints;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.attachments.containers.type.IContainerType;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.config.listener.ConfigBasedCachedIntSupplier;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerHeatCapacitorWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.slot.FluidFuelInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityHeatGenerator extends TileEntityGenerator {

    public static final double HEAT_CAPACITY = 10;
    public static final double INVERSE_CONDUCTION_COEFFICIENT = 5;
    public static final double INVERSE_INSULATION_COEFFICIENT = 100;
    private static final double THERMAL_EFFICIENCY = 0.5;
    //Default configs this is 510 compared to the previous 500
    private static final ConfigBasedCachedIntSupplier MAX_PRODUCTION = new ConfigBasedCachedIntSupplier(() -> {
        int passiveMax = MekanismGeneratorsConfig.generators.heatGenerationLava.get() * (EnumUtils.DIRECTIONS.length + 1);
        passiveMax = MathUtils.addClamped(passiveMax, MekanismGeneratorsConfig.generators.heatGenerationNether.get());
        return MathUtils.addClamped(passiveMax, MekanismGeneratorsConfig.generators.heatGeneration.get());
    }, MekanismGeneratorsConfig.generators.heatGeneration, MekanismGeneratorsConfig.generators.heatGenerationLava, MekanismGeneratorsConfig.generators.heatGenerationNether);

    /**
     * The FluidTank for this generator.
     */
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getLava", "getLavaCapacity", "getLavaNeeded",
                                                                                     "getLavaFilledPercentage"}, docPlaceholder = "lava tank")
    public BasicFluidTank lavaTank;
    private int producingEnergy = 0;
    private double lastTransferLoss;
    private double lastEnvironmentLoss;

    @WrappingComputerMethod(wrapper = ComputerHeatCapacitorWrapper.class, methodNames = "getTemperature", docPlaceholder = "generator")
    BasicHeatCapacitor heatCapacitor;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFuelItem", docPlaceholder = "fuel item slot")
    FluidFuelInventorySlot fuelSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy item slot")
    EnergyInventorySlot energySlot;

    public TileEntityHeatGenerator(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.HEAT_GENERATOR, pos, state);
    }

    @NotNull
    @Override
    protected IContainerHolder<IFluidTank> getInitialFluidTanks(IContentsListener listener) {
        MekContainerHelper<IFluidTank> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(lavaTank = VariableCapacityFluidTank.input(MekanismGeneratorsConfig.generators.heatTankCapacity,
                    fluidStack -> fluidStack.is(FluidTags.LAVA), listener), RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.BACK,
              RelativeSide.TOP, RelativeSide.BOTTOM);
        return builder.build();
    }

    @NotNull
    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSide(facingSupplier);
        //Divide the burn time by 20 as that is the ratio of how much a bucket of lava would burn for
        //TODO: Eventually we may want to grab the 20 dynamically in case some mod is changing the burn time of a lava bucket
        builder.addContainer(fuelSlot = FluidFuelInventorySlot.forFuel(lavaTank, itemType -> level == null ? 0 : itemType.toStack().getBurnTime(null, level.fuelValues()) / 20,
              Fluids.LAVA.builtInRegistryHolder(), listener, 17, 35), RelativeSide.FRONT, RelativeSide.LEFT, RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        builder.addContainer(energySlot = EnergyInventorySlot.drain(energyContainer(), listener, 143, 35), RelativeSide.RIGHT);
        return builder.build();
    }

    @NotNull
    @Override
    protected IContainerHolder<IHeatCapacitor> getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        MekContainerHelper<IHeatCapacitor> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(heatCapacitor = BasicHeatCapacitor.create(HEAT_CAPACITY, INVERSE_CONDUCTION_COEFFICIENT, INVERSE_INSULATION_COEFFICIENT, ambientTemperature, listener));
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.drainContainerIntoSlot(null);
        fuelSlot.fillOrBurn();
        long prev = energyContainer().energy();
        heatCapacitor.handleHeat(getBoost());
        FluidResource lavaResource = lavaTank.resource();
        boolean isActive = false;
        if (canFunction() && !lavaResource.isEmpty() && energyContainer().getNeeded() > 0) {
            int fluidRate = MekanismGeneratorsConfig.generators.heatGenerationFluidRate.get();
            try (Transaction transaction = Transaction.openRoot()) {
                if (lavaTank.extract(lavaResource, fluidRate, transaction, AutomationType.INTERNAL) == fluidRate) {
                    isActive = true;
                    heatCapacitor.handleHeat(MekanismGeneratorsConfig.generators.heatGeneration.get());
                    transaction.commit();
                }
            }
        }
        setActive(isActive);
        HeatTransfer loss = simulate();
        lastTransferLoss = loss.adjacentTransfer();
        lastEnvironmentLoss = loss.environmentTransfer();
        producingEnergy = Math.max(0, Ints.saturatedCast(energyContainer().energy() - prev));
        return sendUpdatePacket;
    }

    private int getBoost() {
        if (level == null) {
            return 0;
        }
        int boost;
        int passiveLavaAmount = MekanismGeneratorsConfig.generators.heatGenerationLava.get();
        if (passiveLavaAmount == 0) {
            //If neighboring lava blocks produce no energy, don't bother checking the sides for them
            boost = 0;
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
        if (level.dimension() == Level.NETHER) {
            return MathUtils.addClamped(boost, MekanismGeneratorsConfig.generators.heatGenerationNether.get());
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
        int energyFromHeat = MathUtils.clampToInt(Math.abs(heatLost) * carnotEfficiency);
        try (Transaction transaction = Transaction.openRoot()) {
            energyContainer().insert(Math.min(energyFromHeat, MAX_PRODUCTION.getAsInt()), transaction, AutomationType.INTERNAL);
            transaction.commit();
        }
        return super.simulate();
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(@NotNull Direction side) {
        return side == Direction.DOWN ? getAdjacentUnchecked(side) : null;
    }

    @Override
    public int getProductionRate() {
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
        return ContainerType.FLUID.getRedstoneSignalFromContainer(lavaTank);
    }

    @Override
    protected boolean makesComparatorDirty(IContainerType<?, ?> type) {
        return type == ContainerType.FLUID;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(this::getProductionRate, value -> producingEnergy = value));
        container.track(SyncableDouble.create(this::getLastTransferLoss, value -> lastTransferLoss = value));
        container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> lastEnvironmentLoss = value));
    }
}
