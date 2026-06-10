package mekanism.generators.common.tile;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.ChemicalFuel;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.ChemicalInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class TileEntityGasGenerator extends TileEntityGenerator {

    public static final Predicate<ChemicalResource> HAS_FUEL = chemical -> chemical.getData(IMekanismDataMapTypes.INSTANCE.chemicalFuel()) != null;

    /**
     * The tank this block is storing fuel in.
     */
    @UnknownNullability//Initialized via getInitialChemicalTanks
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getFuel", "getFuelCapacity", "getFuelNeeded",
                                                                                        "getFuelFilledPercentage"}, docPlaceholder = "fuel tank")
    FuelTank fuelTank;
    @Nullable
    private ChemicalFuel cachedFuel = null;
    private int gasUsedLastTick;

    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFuelItem", docPlaceholder = "fuel item slot")
    ChemicalInventorySlot fuelSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy item slot")
    EnergyInventorySlot energySlot;

    public TileEntityGasGenerator(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.GAS_BURNING_GENERATOR, pos, state);
    }

    @Override
    public IContainerHolder<IChemicalTank> getInitialChemicalTanks(IContentsListener listener) {
        MekContainerHelper<IChemicalTank> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(fuelTank = new FuelTank(listener), RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        return builder.build();
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(fuelSlot = ChemicalInventorySlot.fill(fuelTank, listener, 17, 35), RelativeSide.FRONT, RelativeSide.LEFT, RelativeSide.BACK, RelativeSide.TOP,
              RelativeSide.BOTTOM);
        builder.addContainer(energySlot = EnergyInventorySlot.drain(energyContainer(), listener, 143, 35), RelativeSide.RIGHT);
        fuelSlot.setSlotOverlay(SlotOverlay.MINUS);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        energySlot.drainContainerIntoSlot(null);
        fuelSlot.fillTankFromSlot(null);
        gasUsedLastTick = 0;

        if (!fuelTank.isEmpty() && canFunction() && cachedFuel != null) {
            ChemicalResource fuel = fuelTank.resource();
            int availableFuel;
            try (Transaction simulation = Transaction.openRoot()) {
                availableFuel = fuelTank.extract(fuel, fuelTank.amountAsInt(), simulation, AutomationType.INTERNAL);
            }
            if (availableFuel > 0) {
                //how full the tank is, poor-man's "pressure" measurement
                double fullness = fuelTank.amountAsLong() / (double) fuelTank.capacityAsLong(fuel);
                int energyDensity = cachedFuel.energyDensity();
                //maximum amount that can be produced AND stored
                int maxEnergyThisTick = MathUtils.multiplyClamped(energyDensity, Math.min(Mth.ceil(cachedFuel.maxBurnPerTick() * fullness), availableFuel));
                if (maxEnergyThisTick > 0) {
                    try (Transaction transaction = Transaction.openRoot()) {
                        int inserted = energyContainer().insert(maxEnergyThisTick, transaction, AutomationType.INTERNAL);
                        if (inserted > 0) {
                            //calculate the mB for this amount of energy, rounded up
                            int mbThisTick = Math.ceilDiv(inserted, energyDensity);
                            if (fuelTank.extract(fuel, mbThisTick, transaction, AutomationType.INTERNAL) == mbThisTick) {
                                gasUsedLastTick = mbThisTick;
                                transaction.commit();
                            }
                        }
                    }
                }
            }
        }

        setActive(gasUsedLastTick != 0);
        return sendUpdatePacket;
    }

    public IChemicalTank getFuelTank() {
        return fuelTank;
    }

    @ComputerMethod(nameOverride = "getBurnRate")
    public int getUsed() {
        return gasUsedLastTick;
    }

    @Override
    public int getRedstoneLevel() {
        return ContainerType.CHEMICAL.getRedstoneSignalFromContainer(fuelTank);
    }

    @Override
    protected boolean makesComparatorDirty(IContainerType<?, ?> type) {
        return type == ContainerType.CHEMICAL;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(this::getUsed, value -> gasUsedLastTick = value));
    }

    @Nullable
    public ChemicalFuel getCachedFuel() {
        return this.cachedFuel;
    }

    //Methods relating to IComputerTile
    @Override
    int getProductionRate() {
        if (cachedFuel == null) {
            return 0;
        }
        return MathUtils.multiplyClamped(cachedFuel.energyDensity(), getUsed());
    }
    //End methods IComputerTile

    //Implementation of gas tank that on no longer being empty updates the cached fuel
    private class FuelTank extends VariableCapacityChemicalTank {

        protected FuelTank(@Nullable IContentsListener listener) {
            super(MekanismGeneratorsConfig.generators.gbgTankCapacity, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), HAS_FUEL, null, null, null, listener);
        }

        @Override
        protected void onContentsChanged(LargeResourceStack<ChemicalResource> originalState) {
            super.onContentsChanged(originalState);
            ChemicalResource newType = resource();
            if (!newType.isEmpty() && !originalState.matches(newType)) {
                //Check if the type changed (as this method might have been called from the amount changing)
                cachedFuel = newType.getData(IMekanismDataMapTypes.INSTANCE.chemicalFuel());
            }
        }
    }
}