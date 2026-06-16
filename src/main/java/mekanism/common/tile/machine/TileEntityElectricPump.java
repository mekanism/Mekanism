package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mekanism.api.AutomationType;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.Upgrade;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.capabilities.holder.energy.BasicEnergyHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableResource;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.ResourceUtils;
import mekanism.common.util.UpgradeUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.TypedOutputList;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class TileEntityElectricPump extends TileEntityMekanism implements IConfigurable {

    /// How many ticks it takes to run an operation.
    private static final int BASE_TICKS_REQUIRED = 19;
    public static final long MAX_FLUID = 10L * FluidType.BUCKET_VOLUME;
    private static final int BASE_OUTPUT_RATE = 256;

    /// This pump's tank
    @UnknownNullability//Initialized via getInitialFluidTanks
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getFluid", "getFluidCapacity", "getFluidNeeded",
                                                                                     "getFluidFilledPercentage"}, docPlaceholder = "buffer tank")
    public BasicFluidTank fluidTank;
    /// The type of fluid this pump is pumping
    private FluidResource activeType = FluidResource.EMPTY;
    public int ticksRequired = BASE_TICKS_REQUIRED;
    /// How many ticks this machine has been operating for.
    public int operatingTicks;
    private boolean usedEnergy = false;
    private int outputRate = BASE_OUTPUT_RATE;
    /// The nodes that have full sources near them or in them
    private final Set<BlockPos> recurringNodes = new ObjectOpenHashSet<>();
    @Nullable
    private BlockCapabilityCache<ResourceHandler<FluidResource>, @Nullable Direction> fluidHandlerAbove;

    @UnknownNullability//Initialized via getInitialEnergyContainer
    private MachineEnergyContainer<TileEntityElectricPump> energyContainer;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem", docPlaceholder = "input slot")
    FluidInventorySlot inputSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem", docPlaceholder = "output slot")
    OutputInventorySlot outputSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntityElectricPump(BlockPos pos, BlockState state) {
        super(MekanismBlocks.ELECTRIC_PUMP, pos, state);
    }

    @Override
    protected IContainerHolder<IFluidTank> getInitialFluidTanks(IContentsListener listener) {
        MekContainerHelper<IFluidTank> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(fluidTank = BasicFluidTank.output(MAX_FLUID, listener), RelativeSide.TOP);
        return builder.build();
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainer(IContentsListener listener) {
        energyContainer = MachineEnergyContainer.input(this, listener);
        return new BasicEnergyHolder(energyContainer, facingSupplier, BACK_ONLY);
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(inputSlot = FluidInventorySlot.drain(fluidTank, listener, 28, 20), RelativeSide.TOP);
        builder.addContainer(outputSlot = OutputInventorySlot.at(listener, 28, 51), RelativeSide.BOTTOM);
        builder.addContainer(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 143, 35), RelativeSide.BACK);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        energySlot.fillContainerOrConvert(null);
        inputSlot.drainTankIntoSlot(outputSlot, null);
        int clientEnergyUsed = 0;
        if (canFunction() && (fluidTank.isEmpty() || estimateIncrementAmount() <= fluidTank.getNeededAsInt(FluidResource.EMPTY))) {
            int energyPerTick = energyContainer.getEnergyPerTick();
            try (Transaction transaction = Transaction.openRoot()) {
                if (energyContainer.extract(energyPerTick, transaction, AutomationType.INTERNAL) == energyPerTick) {
                    if (!activeType.isEmpty()) {
                        //If we have an active type of fluid, use energy. This can cause there to be ticks where there isn't actually
                        // anything to suck that use energy, but those will balance out with the first set of ticks where it doesn't
                        // use any energy until it actually picks up the first block
                        clientEnergyUsed = energyPerTick;
                    }
                    operatingTicks++;
                    if (operatingTicks >= ticksRequired) {
                        operatingTicks = 0;
                        if (suck(level, transaction)) {
                            clientEnergyUsed = energyPerTick;
                        } else {
                            reset();
                        }
                    }
                    if (clientEnergyUsed > 0) {
                        transaction.commit();
                    }
                }
            }
        }
        usedEnergy = clientEnergyUsed > 0;
        if (!fluidTank.isEmpty()) {
            if (fluidHandlerAbove == null) {
                fluidHandlerAbove = Capabilities.FLUID.createCache(level, worldPosition.above(), Direction.DOWN);
            }
            ResourceUtils.emit(fluidHandlerAbove.getCapability(), fluidTank, outputRate, null);
        }
        return sendUpdatePacket;
    }

    @Override
    public void setLevel(Level world) {
        super.setLevel(world);
        //Invalidate the cache as if the level changed then it might no longer be valid
        fluidHandlerAbove = null;
    }

    public int estimateIncrementAmount() {
        return fluidTank.resource().is(MekanismFluids.HEAVY_WATER) ? MekanismConfig.general.pumpHeavyWaterAmount.get() : FluidType.BUCKET_VOLUME;
    }

    private boolean suck(ServerLevel level, TransactionContext transaction) {
        boolean hasFilter = getUpgrades(Upgrade.FILTER) > 0;
        //First see if there are any fluid blocks under the pump - if so, suck and adds the location to the recurring list
        if (suck(level, worldPosition.relative(Direction.DOWN), hasFilter, true, transaction)) {
            return true;
        }
        //Even though we can add to recurring in the above for loop, we always then exit and don't get to here if we did so
        List<BlockPos> tempPumpList = new ArrayList<>(recurringNodes);
        Collections.shuffle(tempPumpList);
        //Finally, go over the recurring list of nodes and see if there is a fluid block available to suck - if not, will iterate around the recurring block, attempt to suck,
        //and then add the adjacent block to the recurring list
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (BlockPos tempPumpPos : tempPumpList) {
            if (suck(level, tempPumpPos, hasFilter, false, transaction)) {
                return true;
            }
            //Add all the blocks surrounding this recurring node to the recurring node list
            for (Direction orientation : EnumUtils.DIRECTIONS) {
                mutable.setWithOffset(tempPumpPos, orientation);
                if (WorldUtils.distanceBetween(worldPosition, mutable) <= MekanismConfig.general.maxPumpRange.get()) {
                    if (suck(level, mutable, hasFilter, true, transaction)) {
                        return true;
                    }
                }
            }
            recurringNodes.remove(tempPumpPos);
        }
        return false;
    }

    /// @implNote This could potentially be somewhat replaced with FluidUtil#tryPickupFluid by using a custom resource handler that wraps the type that is passed to it,
    /// but because of our handling for infinite fluid sources, it currently seems pointless to bother.
    private boolean suck(ServerLevel level, BlockPos pos, boolean hasFilter, boolean addRecurring, TransactionContext transaction) {
        //Note: we get the block state from the world so that we can get the proper block in case it is fluid logged
        Optional<BlockState> state = WorldUtils.getBlockState(level, pos);
        if (state.isEmpty()) {
            return false;
        }
        BlockState blockState = state.get();
        FluidState fluidState = blockState.getFluidState();
        //Just in case someone does weird things and has a fluid state that is empty and a source only allow collecting from non-empty sources
        if (fluidState.isEmpty() || !fluidState.isSource() || !(blockState.getBlock() instanceof BucketPickup bucketPickup)) {
            return false;
        }
        boolean pickedUpFluid = false;
        Fluid sourceFluid = fluidState.getType();
        try (Transaction subTransaction = Transaction.open(transaction)) {
            FluidStack fluidStack = getOutput(sourceFluid, hasFilter);
            if (!activeType.isEmpty() && !activeType.matches(fluidStack)) {
                return false;
            }
            FluidResource fluidType = FluidResource.of(fluidStack);
            int amountProduced = fluidStack.amount();
            int inserted = fluidTank.insert(fluidType, amountProduced, subTransaction, AutomationType.INTERNAL);
            if (inserted < amountProduced) {
                //If we can't insert everything that we would pump up, just return that we couldn't suck
                return false;
            } else if (isInfiniteSource(level, sourceFluid)) {
                //If it is an infinite source, we can just go ahead and commit and mark it as having been sucked
                subTransaction.commit();
                suck(level, fluidType, pos, addRecurring);
                return true;
            }
            //If it can be picked up by a bucket, and we actually want to pick it up, do so to update the fluid type we are doing
            //Note we only attempt taking if it is not water, or we want to pump water sources
            // otherwise we assume the type from the fluid state is correct
            ItemStack pickedUpStack = bucketPickup.pickupBlock(null, level, pos, blockState);
            if (pickedUpStack.isEmpty()) {
                //Couldn't actually pick it up, exit
                return false;
            } else if (pickedUpStack.getItem() instanceof BucketItem bucket) {
                //This isn't the best validation check given it may not return a bucket, but it is good enough for now
                if (sourceFluid == bucket.content) {
                    //Same type as expected, commit the insertion and mark things as having happened
                    subTransaction.commit();
                    suck(level, fluidType, pos, addRecurring);
                    return true;
                }
                sourceFluid = bucket.content;
                pickedUpFluid = true;
            }
        }
        if (pickedUpFluid) {
            try (Transaction subTransaction = Transaction.open(transaction)) {
                //Update the fluid stack in case something somehow changed about the type making sure that we replace to heavy water if we got heavy water
                FluidStack fluidStack = getOutput(sourceFluid, hasFilter);
                //Note: We don't validate the active type matching, as if the tank is empty we would rather try inserting it
                // rather than voiding the picked up fluid
                FluidResource fluidType = FluidResource.of(fluidStack);
                int amountProduced = fluidStack.amount();
                int inserted = fluidTank.insert(fluidType, amountProduced, subTransaction, AutomationType.INTERNAL);
                if (inserted > 0) {
                    subTransaction.commit();
                    suck(level, fluidType, pos, addRecurring);
                    if (inserted < amountProduced) {
                        //If we can't insert everything that we would pump up, log a warning
                        Mekanism.logger.warn("Fluid removed without successfully picking the full thing up. Fluid {} at {} in {} was valid, but after picking up was {}. "
                                             + "Accepted {} out of attempted {}.", fluidState.getType(), pos, level.dimension().identifier(), sourceFluid, inserted, amountProduced);
                    }
                    return true;
                }
            }
        }
        Mekanism.logger.warn("Fluid removed without successfully picking up. Fluid {} at {} in {} was valid, but after picking up was {}.",
              fluidState.getType(), pos, level.dimension().identifier(), sourceFluid);
        return false;
    }

    private boolean isInfiniteSource(ServerLevel level, Fluid sourceFluid) {
        if (!MekanismConfig.general.pumpInfiniteFluidSources.get()) {
            if (sourceFluid == Fluids.WATER) {
                //If we don't pump infinite sources, only pump it if water conversion is turned off
                return level.getGameRules().get(GameRules.WATER_SOURCE_CONVERSION);
            } else if (sourceFluid == Fluids.LAVA) {
                //If we don't pump infinite sources, only pump it if lava conversion is turned off
                return level.getGameRules().get(GameRules.LAVA_SOURCE_CONVERSION);
            }
        }
        return false;
    }

    private FluidStack getOutput(Fluid sourceFluid, boolean hasFilter) {
        if (hasFilter && sourceFluid == Fluids.WATER) {
            return MekanismFluids.HEAVY_WATER.asStack(MekanismConfig.general.pumpHeavyWaterAmount.get());
        }
        return new FluidStack(sourceFluid, FluidType.BUCKET_VOLUME);
    }

    private void suck(ServerLevel level, FluidResource fluidType, BlockPos pos, boolean addRecurring) {
        activeType = fluidType;
        if (addRecurring) {
            recurringNodes.add(pos.immutable());
        }
        level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
    }

    public void reset() {
        activeType = FluidResource.EMPTY;
        recurringNodes.clear();
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(SerializationConstants.PROGRESS, operatingTicks);
        if (!activeType.isEmpty()) {
            output.store(SerializationConstants.FLUID, FluidResource.CODEC, activeType);
        }
        if (!recurringNodes.isEmpty()) {
            TypedOutputList<BlockPos> recurringNodesOutput = output.list(SerializationConstants.RECURRING_NODES, BlockPos.CODEC);
            for (BlockPos recurringNode : recurringNodes) {
                recurringNodesOutput.add(recurringNode);
            }
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        operatingTicks = input.getIntOr(SerializationConstants.PROGRESS, operatingTicks);
        activeType = input.read(SerializationConstants.FLUID, FluidResource.CODEC).orElse(FluidResource.EMPTY);
        //TODO - 26.2: Do we want to support loading the old format for this and the plenisher where it was all smashed in a single int array?
        for (BlockPos pos : input.listOrEmpty(SerializationConstants.RECURRING_NODES, BlockPos.CODEC)) {
            recurringNodes.add(pos);
        }
    }

    @Override
    @Deprecated
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard(SerializationConstants.RECURRING_NODES);
    }

    @Override
    public InteractionResult onSneakRightClick(Level level, Player player) {
        reset();
        player.sendOverlayMessage(MekanismLang.PUMP_RESET.translate());
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onRightClick(Level level, Player player) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean supportsMode(RedstoneControl mode) {
        return true;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
            outputRate = BASE_OUTPUT_RATE * (1 + getUpgrades(Upgrade.SPEED));
        }
    }

    @Override
    public int getRedstoneLevel() {
        return ContainerType.FLUID.getRedstoneSignalFromContainer(fluidTank);
    }

    @Override
    protected boolean makesComparatorDirty(IContainerType<?, ?> type) {
        return type == ContainerType.FLUID;
    }

    @Override
    public List<Component> getInfo(Upgrade upgrade) {
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
    }

    public MachineEnergyContainer<TileEntityElectricPump> energyContainer() {
        return energyContainer;
    }

    public boolean usedEnergy() {
        return usedEnergy;
    }

    public FluidResource getActiveType() {
        return this.activeType;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(this::usedEnergy, value -> usedEnergy = value));
        container.track(SyncableResource.createFluid(this::getActiveType, value -> activeType = value));
    }

    //Methods relating to IComputerTile
    @ComputerMethod(nameOverride = "reset", requiresPublicSecurity = true)
    void resetPump() throws ComputerException {
        validateSecurityIsPublic();
        reset();
    }
    //End methods IComputerTile
}
