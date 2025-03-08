package mekanism.generators.common.tile;

import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.ChemicalFuel;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.MathUtils;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityGasGenerator extends TileEntityGenerator {

    @SuppressWarnings("removal")
    public static final Predicate<ChemicalStack> HAS_FUEL = chemical -> chemical.getData(IMekanismDataMapTypes.INSTANCE.chemicalFuel()) != null
                                                                           || chemical.hasLegacy(ChemicalAttributes.Fuel.class);//TODO - 1.22 Remove this legacy check

    /**
     * The tank this block is storing fuel in.
     */
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getFuel", "getFuelCapacity", "getFuelNeeded",
                                                                                        "getFuelFilledPercentage"}, docPlaceholder = "fuel tank")
    public FuelTank fuelTank;
    private long burnTicks;
    private int maxBurnTicks;
    private long generationRate = 0;
    private double gasUsedLastTick;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFuelItem", docPlaceholder = "fuel item slot")
    ChemicalInventorySlot fuelSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy item slot")
    EnergyInventorySlot energySlot;

    public TileEntityGasGenerator(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.GAS_BURNING_GENERATOR, pos, state, ChemicalUtil::hydrogenEnergyPerTick);
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSide(facingSupplier);
        builder.addTank(fuelTank = new FuelTank(listener), RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        builder.addSlot(fuelSlot = ChemicalInventorySlot.fill(fuelTank, listener, 17, 35), RelativeSide.FRONT, RelativeSide.LEFT, RelativeSide.BACK, RelativeSide.TOP,
              RelativeSide.BOTTOM);
        builder.addSlot(energySlot = EnergyInventorySlot.drain(getEnergyContainer(), listener, 143, 35), RelativeSide.RIGHT);
        fuelSlot.setSlotOverlay(SlotOverlay.MINUS);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.drainContainer();
        fuelSlot.fillTank();

        if (!fuelTank.isEmpty() && canFunction() && getEnergyContainer().insert(generationRate, Action.SIMULATE, AutomationType.INTERNAL) == 0L) {
            setActive(true);
            if (!fuelTank.isEmpty()) {
                ChemicalFuel fuel = fuelTank.getFuel();
                if (fuel != null) {
                    //Ensure valid data
                    maxBurnTicks = Math.max(1, fuel.burnTicks());
                    generationRate = fuel.energyPerTick();
                }
            }

            long toUse = getToUse();
            long toUseGeneration = MathUtils.multiplyClamped(generationRate, toUse);
            updateMaxOutputRaw(Math.max(ChemicalUtil.hydrogenEnergyPerTick(), toUseGeneration));

            long total = burnTicks + fuelTank.getStored() * maxBurnTicks;
            total -= toUse;
            getEnergyContainer().insert(toUseGeneration, Action.EXECUTE, AutomationType.INTERNAL);
            if (!fuelTank.isEmpty()) {
                //TODO: Improve this as it is sort of hacky
                fuelTank.setStack(fuelTank.getStack().copyWithAmount(total / maxBurnTicks));
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
        return sendUpdatePacket;
    }

    private void reset() {
        burnTicks = 0;
        maxBurnTicks = 0;
        generationRate = 0L;
        updateMaxOutputRaw(ChemicalUtil.hydrogenEnergyPerTick());
    }

    private long getToUse() {
        if (generationRate == 0L || fuelTank.isEmpty()) {
            return 0;
        }
        long max = (long) Math.ceil(256 * (fuelTank.getStored() / (double) fuelTank.getCapacity()));
        max = Math.min(maxBurnTicks * fuelTank.getStored() + burnTicks, max);
        max = Math.min(MathUtils.clampToLong(getEnergyContainer().getNeeded() / (double) generationRate), max);
        return max;
    }

    public long getGenerationRate() {
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
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return type == ContainerType.CHEMICAL;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableLong.create(this::getGenerationRate, value -> generationRate = value));
        container.track(syncableMaxOutput());
        container.track(SyncableDouble.create(this::getUsed, value -> gasUsedLastTick = value));
        container.track(SyncableInt.create(this::getMaxBurnTicks, value -> maxBurnTicks = value));
    }

    //Methods relating to IComputerTile
    @Override
    long getProductionRate() {
        return MathUtils.clampToLong(getGenerationRate() * getUsed() * getMaxBurnTicks());
    }
    //End methods IComputerTile

    //Implementation of gas tank that on no longer being empty updates the output rate of this generator
    public class FuelTank extends VariableCapacityChemicalTank {

        protected FuelTank(@Nullable IContentsListener listener) {
            super(MekanismGeneratorsConfig.generators.gbgTankCapacity, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), HAS_FUEL, null, listener);
        }

        @Override
        public void setStack(@NotNull ChemicalStack stack) {
            boolean wasEmpty = isEmpty();
            super.setStack(stack);
            recheckOutput(stack, wasEmpty);
        }

        @Override
        public void setStackUnchecked(@NotNull ChemicalStack stack) {
            boolean wasEmpty = isEmpty();
            super.setStackUnchecked(stack);
            recheckOutput(stack, wasEmpty);
        }

        private void recheckOutput(@NotNull ChemicalStack stack, boolean wasEmpty) {
            if (wasEmpty && !stack.isEmpty()) {
                ChemicalFuel fuel = getFuel();
                if (fuel != null) {
                    updateMaxOutputRaw(fuel.energyPerTick());
                }
            }
        }

        @Nullable
        @SuppressWarnings("removal")
        public ChemicalFuel getFuel() {
            if (isEmpty()) {
                return null;
            }
            ChemicalStack stack = getStack();
            ChemicalFuel fuel = stack.getData(IMekanismDataMapTypes.INSTANCE.chemicalFuel());
            if (fuel == null) {//TODO - 1.22: Remove this handling of legacy data
                //If there is no fuel in the data map, see if one was set manually on the stack
                ChemicalAttributes.Fuel legacyFuel = stack.getLegacy(ChemicalAttributes.Fuel.class);
                if (legacyFuel != null) {
                    //If it was, convert it to the non legacy type
                    return legacyFuel.asModern();
                }
            }
            return fuel;
        }
    }
}