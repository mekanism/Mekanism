package mekanism.generators.common.tile;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes.Fuel;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.chemical.variable.VariableCapacityChemicalTankBuilder.VariableCapacityGasTank;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityGasGenerator extends TileEntityGenerator {

    /**
     * The tank this block is storing fuel in.
     */
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getFuel", "getFuelCapacity", "getFuelNeeded", "getFuelFilledPercentage"}, docPlaceholder = "fuel tank")
    public FuelTank fuelTank;
    private long burnTicks;
    private int maxBurnTicks;
    private FloatingLong generationRate = FloatingLong.ZERO;
    private double gasUsedLastTick;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFuelItem", docPlaceholder = "fuel item slot")
    GasInventorySlot fuelSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy item slot")
    EnergyInventorySlot energySlot;

    public TileEntityGasGenerator(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.GAS_BURNING_GENERATOR, pos, state, MekanismConfig.general.FROM_H2);
    }

    @NotNull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSide(this::getDirection);
        builder.addTank(fuelTank = new FuelTank(listener), RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(fuelSlot = GasInventorySlot.fill(fuelTank, listener, 17, 35), RelativeSide.FRONT, RelativeSide.LEFT, RelativeSide.BACK, RelativeSide.TOP,
              RelativeSide.BOTTOM);
        builder.addSlot(energySlot = EnergyInventorySlot.drain(getEnergyContainer(), listener, 143, 35), RelativeSide.RIGHT);
        fuelSlot.setSlotOverlay(SlotOverlay.MINUS);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.drainContainer();
        fuelSlot.fillTank();

        if (!fuelTank.isEmpty() && MekanismUtils.canFunction(this) && getEnergyContainer().insert(generationRate, Action.SIMULATE, AutomationType.INTERNAL).isZero()) {
            setActive(true);
            if (!fuelTank.isEmpty()) {
                fuelTank.getType().ifAttributePresent(Fuel.class, fuel -> {
                    //Ensure valid data
                    maxBurnTicks = Math.max(1, fuel.getBurnTicks());
                    generationRate = fuel.getEnergyPerTick();
                });
            }

            long toUse = getToUse();
            FloatingLong toUseGeneration = generationRate.multiply(toUse);
            updateMaxOutputRaw(MekanismConfig.general.FROM_H2.get().max(toUseGeneration));

            long total = burnTicks + fuelTank.getStored() * maxBurnTicks;
            total -= toUse;
            getEnergyContainer().insert(toUseGeneration, Action.EXECUTE, AutomationType.INTERNAL);
            if (!fuelTank.isEmpty()) {
                //TODO: Improve this as it is sort of hacky
                fuelTank.setStack(new GasStack(fuelTank.getStack(), total / maxBurnTicks));
            }
            burnTicks = total % maxBurnTicks;
            gasUsedLastTick = toUse / (double) maxBurnTicks;
        } else {
            if (fuelTank.isEmpty() && burnTicks == 0) {
                reset();
            }
            gasUsedLastTick = 0;
            setActive(false);
        }
    }

    private void reset() {
        burnTicks = 0;
        maxBurnTicks = 0;
        generationRate = FloatingLong.ZERO;
        updateMaxOutputRaw(MekanismConfig.general.FROM_H2.get());
    }

    private long getToUse() {
        if (generationRate.isZero() || fuelTank.isEmpty()) {
            return 0;
        }
        long max = (long) Math.ceil(256 * (fuelTank.getStored() / (double) fuelTank.getCapacity()));
        max = Math.min(maxBurnTicks * fuelTank.getStored() + burnTicks, max);
        max = Math.min(getEnergyContainer().getNeeded().divide(generationRate).intValue(), max);
        return max;
    }

    public FloatingLong getGenerationRate() {
        return generationRate;
    }

    @ComputerMethod(nameOverride = "getBurnRate")
    public double getUsed() {
        return Math.round(gasUsedLastTick * 100) / 100D;
    }

    public int getMaxBurnTicks() {
        return maxBurnTicks;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fuelTank.getStored(), fuelTank.getCapacity());
    }

    @Override
    protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
        return type == SubstanceType.GAS;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFloatingLong.create(this::getGenerationRate, value -> generationRate = value));
        container.track(syncableMaxOutput());
        container.track(SyncableDouble.create(this::getUsed, value -> gasUsedLastTick = value));
        container.track(SyncableInt.create(this::getMaxBurnTicks, value -> maxBurnTicks = value));
    }

    //Methods relating to IComputerTile
    FloatingLong getProductionRate() {
        return getGenerationRate().multiply(getUsed()).multiply(getMaxBurnTicks());
    }
    //End methods IComputerTile

    //Implementation of gas tank that on no longer being empty updates the output rate of this generator
    private class FuelTank extends VariableCapacityGasTank {

        protected FuelTank(@Nullable IContentsListener listener) {
            super(MekanismGeneratorsConfig.generators.gbgTankCapacity, ChemicalTankBuilder.GAS.notExternal, ChemicalTankBuilder.GAS.alwaysTrueBi,
                  gas -> gas.has(Fuel.class), null, listener);
        }

        @Override
        public void setStack(@NotNull GasStack stack) {
            boolean wasEmpty = isEmpty();
            super.setStack(stack);
            recheckOutput(stack, wasEmpty);
        }

        @Override
        public void setStackUnchecked(@NotNull GasStack stack) {
            boolean wasEmpty = isEmpty();
            super.setStackUnchecked(stack);
            recheckOutput(stack, wasEmpty);
        }

        private void recheckOutput(@NotNull GasStack stack, boolean wasEmpty) {
            if (wasEmpty && !stack.isEmpty()) {
                getType().ifAttributePresent(Fuel.class, fuel -> updateMaxOutputRaw(fuel.getEnergyPerTick()));
            }
        }
    }
}
