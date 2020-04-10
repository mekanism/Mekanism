package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.inventory.AutomationType;
import mekanism.common.FuelHandler;
import mekanism.common.FuelHandler.FuelGas;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.registries.GeneratorsBlocks;

public class TileEntityGasGenerator extends TileEntityGenerator {

    /**
     * The maximum amount of gas this block can store.
     */
    private static final long MAX_GAS = 18_000;
    /**
     * The tank this block is storing fuel in.
     */
    public FuelTank fuelTank;
    private long burnTicks;
    private int maxBurnTicks;
    private FloatingLong generationRate = FloatingLong.ZERO;
    private double gasUsedLastTick;

    private GasInventorySlot fuelSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityGasGenerator() {
        super(GeneratorsBlocks.GAS_BURNING_GENERATOR, MekanismConfig.general.FROM_H2.get().multiply(2));
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack> builder = ChemicalTankHelper.forSideGas(this::getDirection);
        builder.addTank(fuelTank = new FuelTank(this), RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(fuelSlot = GasInventorySlot.fill(fuelTank, this, 17, 35),
              RelativeSide.FRONT, RelativeSide.LEFT, RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        builder.addSlot(energySlot = EnergyInventorySlot.drain(getEnergyContainer(), this, 143, 35), RelativeSide.RIGHT);
        fuelSlot.setSlotOverlay(SlotOverlay.MINUS);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.drainContainer();
        fuelSlot.fillTank();

        boolean operate = (!fuelTank.isEmpty() || burnTicks > 0) && MekanismUtils.canFunction(this);
        if (operate && getEnergyContainer().insert(generationRate, Action.SIMULATE, AutomationType.INTERNAL).isZero()) {
            setActive(true);
            if (!fuelTank.isEmpty()) {
                FuelGas fuel = FuelHandler.getFuel(fuelTank.getType());
                maxBurnTicks = fuel.burnTicks;
                generationRate = fuel.energyPerTick;
            }

            long toUse = getToUse();
            FloatingLong toUseGeneration = generationRate.multiply(toUse);
            output = MekanismConfig.general.FROM_H2.get().max(toUseGeneration).multiply(2);

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
            if (!operate) {
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
        output = MekanismConfig.general.FROM_H2.get().multiply(2);
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

    public double getUsed() {
        return Math.round(gasUsedLastTick * 100) / 100D;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fuelTank.getStored(), fuelTank.getCapacity());
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFloatingLong.create(this::getGenerationRate, value -> generationRate = value));
        container.track(SyncableFloatingLong.create(() -> output, value -> output = value));
        container.track(SyncableDouble.create(this::getUsed, value -> gasUsedLastTick = value));
    }

    //Implementation of gas tank that on no longer being empty updates the output rate of this generator
    private class FuelTank extends BasicGasTank {

        protected FuelTank(@Nullable IMekanismGasHandler gasHandler) {
            super(MAX_GAS, notExternal, alwaysTrueBi, gas -> !FuelHandler.getFuel(gas).isEmpty(), gasHandler);
        }

        @Override
        public void setStack(@Nonnull GasStack stack) {
            boolean wasEmpty = isEmpty();
            super.setStack(stack);
            if (wasEmpty && !stack.isEmpty()) {
                output = FuelHandler.getFuel(getType()).energyPerTick.multiply(2);
            }
        }

        @Override
        protected void setStackUnchecked(@Nonnull GasStack stack) {
            boolean wasEmpty = isEmpty();
            super.setStackUnchecked(stack);
            if (wasEmpty && !stack.isEmpty()) {
                output = FuelHandler.getFuel(getType()).energyPerTick.multiply(2);
            }
        }
    }
}