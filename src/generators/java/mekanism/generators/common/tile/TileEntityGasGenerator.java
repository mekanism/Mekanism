package mekanism.generators.common.tile;

import com.google.common.primitives.Ints;
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
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.holder.IContainerHolder;
import mekanism.common.capabilities.holder.MekContainerHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.ChemicalInventorySlot;
import mekanism.common.util.ResourceUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityGasGenerator extends TileEntityGenerator {

    public static final Predicate<ChemicalResource> HAS_FUEL = chemical -> chemical.getData(IMekanismDataMapTypes.INSTANCE.chemicalFuel()) != null;

    /**
     * The tank this block is storing fuel in.
     */
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getFuel", "getFuelCapacity", "getFuelNeeded",
                                                                                        "getFuelFilledPercentage"}, docPlaceholder = "fuel tank")
    public FuelTank fuelTank;
    @Nullable
    private ChemicalFuel cachedFuel = null;
    private long gasUsedLastTick;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFuelItem", docPlaceholder = "fuel item slot")
    ChemicalInventorySlot fuelSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy item slot")
    EnergyInventorySlot energySlot;

    public TileEntityGasGenerator(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.GAS_BURNING_GENERATOR, pos, state);
    }

    @NotNull
    @Override
    public IContainerHolder<IChemicalTank> getInitialChemicalTanks(IContentsListener listener) {
        MekContainerHelper<IChemicalTank> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(fuelTank = new FuelTank(listener), RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        return builder.build();
    }

    @NotNull
    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(fuelSlot = ChemicalInventorySlot.fill(fuelTank, listener, 17, 35), RelativeSide.FRONT, RelativeSide.LEFT, RelativeSide.BACK, RelativeSide.TOP,
              RelativeSide.BOTTOM);
        builder.addContainer(energySlot = EnergyInventorySlot.drain(getEnergyContainer(), listener, 143, 35), RelativeSide.RIGHT);
        fuelSlot.setSlotOverlay(SlotOverlay.MINUS);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.drainContainer();
        fuelSlot.fillTank();
        gasUsedLastTick = 0;

        if (!fuelTank.isEmpty() && canFunction() && cachedFuel != null) {
            ChemicalResource fuel = fuelTank.resource();

            //how full the tank is, poor-man's "pressure" measurement
            double fullness = fuelTank.amountAsLong() / (double) fuelTank.capacityAsLong(fuel);

            long energyDensity = cachedFuel.energyDensity();
            //maximum amount that can be produced AND stored
            long maxJoulesThisTick = energyDensity * Math.min((long) Math.ceil(cachedFuel.maxBurnPerTick() * fullness), fuelTank.amountAsLong());
            if (maxJoulesThisTick > 0) {
                try (Transaction transaction = Transaction.openRoot()) {
                    long inserted = getEnergyContainer().insert(maxJoulesThisTick, transaction, AutomationType.INTERNAL);
                    if (inserted > 0) {
                        //calculate the mB for this amount of energy, rounded up
                        long mbThisTick = Math.ceilDiv(inserted, energyDensity);
                        //TODO - 26.1: Figure out this long to int conversion. We should make it so that the math doesn't cause issues
                        //TODO - 26.1: Do we want to validate anything about the value we extracted from the fuel tank?
                        gasUsedLastTick = fuelTank.extract(fuel, Ints.saturatedCast(mbThisTick), transaction, AutomationType.INTERNAL);
                        transaction.commit();
                    }
                }
            }
        }

        setActive(gasUsedLastTick != 0);
        return sendUpdatePacket;
    }

    @ComputerMethod(nameOverride = "getBurnRate")
    public long getUsed() {
        return gasUsedLastTick;
    }

    @Override
    public int getRedstoneLevel() {
        return ResourceUtils.getRedstoneSignalFromContainer(fuelTank);
    }

    @Override
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return type == ContainerType.CHEMICAL;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableLong.create(this::getUsed, value -> gasUsedLastTick = value));
    }

    @Nullable
    public ChemicalFuel getCachedFuel() {
        return this.cachedFuel;
    }

    //Methods relating to IComputerTile
    @Override
    long getProductionRate() {
        if (cachedFuel == null) {
            return 0;
        }
        return MathUtils.clampToLong(cachedFuel.energyDensity() * getUsed());
    }
    //End methods IComputerTile

    //Implementation of gas tank that on no longer being empty updates the cached fuel
    private class FuelTank extends VariableCapacityChemicalTank {

        protected FuelTank(@Nullable IContentsListener listener) {
            super(MekanismGeneratorsConfig.generators.gbgTankCapacity, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), HAS_FUEL, null, listener);
        }

        @Override
        protected void onContentsChanged(@NotNull LargeResourceStack<ChemicalResource> originalState) {
            super.onContentsChanged(originalState);
            ChemicalResource newType = resource();
            if (!newType.isEmpty() && !originalState.matches(newType)) {
                cachedFuel = newType.getData(IMekanismDataMapTypes.INSTANCE.chemicalFuel());
            }
        }
    }
}