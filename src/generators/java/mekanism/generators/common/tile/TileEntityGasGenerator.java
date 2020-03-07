package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
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
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.registries.GeneratorsBlocks;

public class TileEntityGasGenerator extends TileEntityGenerator {

    /**
     * The maximum amount of gas this block can store.
     */
    private static final int MAX_GAS = 18_000;
    /**
     * The tank this block is storing fuel in.
     */
    public FuelTank fuelTank;
    private int burnTicks = 0;
    private int maxBurnTicks;
    private double generationRate = 0;
    private double gasUsedLastTick;

    private GasInventorySlot fuelSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityGasGenerator() {
        super(GeneratorsBlocks.GAS_BURNING_GENERATOR, MekanismConfig.general.FROM_H2.get() * 2);
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
        builder.addSlot(energySlot = EnergyInventorySlot.charge(this, 143, 35), RelativeSide.RIGHT);
        fuelSlot.setSlotOverlay(SlotOverlay.MINUS);
        return builder.build();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isRemote()) {
            energySlot.charge(this);
            fuelSlot.fillTank();

            boolean operate = canOperate();
            if (operate && getEnergy() + generationRate < getMaxEnergy()) {
                setActive(true);
                if (fuelTank.getStored() != 0) {
                    FuelGas fuel = FuelHandler.getFuel(fuelTank.getType());
                    maxBurnTicks = fuel.burnTicks;
                    generationRate = fuel.energyPerTick;
                }

                int toUse = getToUse();
                output = Math.max(MekanismConfig.general.FROM_H2.get() * 2, generationRate * toUse * 2);

                int total = burnTicks + fuelTank.getStored() * maxBurnTicks;
                total -= toUse;
                setEnergy(getEnergy() + generationRate * toUse);

                if (!fuelTank.isEmpty()) {
                    //TODO: Improve this as it is sort of hacky
                    fuelTank.setStack(new GasStack(fuelTank.getStack(), total / maxBurnTicks));
                }
                burnTicks = total % maxBurnTicks;
                gasUsedLastTick = (double) toUse / (double) maxBurnTicks;
            } else {
                if (!operate) {
                    reset();
                }
                gasUsedLastTick = 0;
                setActive(false);
            }
        }
    }

    private void reset() {
        burnTicks = 0;
        maxBurnTicks = 0;
        generationRate = 0;
        output = MekanismConfig.general.FROM_H2.get() * 2;
    }

    private int getToUse() {
        if (generationRate == 0 || fuelTank.isEmpty()) {
            return 0;
        }
        int max = (int) Math.ceil(((float) fuelTank.getStored() / (float) fuelTank.getCapacity()) * 256F);
        max = Math.min((fuelTank.getStored() * maxBurnTicks) + burnTicks, max);
        max = (int) Math.min(getNeededEnergy() / generationRate, max);
        return max;
    }

    @Override
    public boolean canOperate() {
        return (!fuelTank.isEmpty() || burnTicks > 0) && MekanismUtils.canFunction(this);
    }

    public double getGenerationRate() {
        return generationRate;
    }

    public double getUsed() {
        return Math.round(gasUsedLastTick*100)/100D;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fuelTank.getStored(), fuelTank.getCapacity());
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(this::getGenerationRate, value -> generationRate = value));
        container.track(SyncableDouble.create(() -> output, value -> output = value));
        container.track(SyncableDouble.create(this::getUsed, value -> gasUsedLastTick = value));
    }

    //Implementation of gas tank that on no longer being empty updates the output rate of this generator
    private class FuelTank extends BasicGasTank {

        protected FuelTank(@Nullable IMekanismGasHandler gasHandler) {
            super(MAX_GAS, manualOnly, alwaysTrueBi, gas -> !FuelHandler.getFuel(gas).isEmpty(), gasHandler);
        }

        @Override
        public void setStack(@Nonnull GasStack stack) {
            boolean wasEmpty = isEmpty();
            super.setStack(stack);
            if (wasEmpty && !stack.isEmpty()) {
                output = FuelHandler.getFuel(getType()).energyPerTick * 2;
            }
        }

        @Override
        protected void setStackUnchecked(@Nonnull GasStack stack) {
            boolean wasEmpty = isEmpty();
            super.setStackUnchecked(stack);
            if (wasEmpty && !stack.isEmpty()) {
                output = FuelHandler.getFuel(getType()).energyPerTick * 2;
            }
        }
    }
}