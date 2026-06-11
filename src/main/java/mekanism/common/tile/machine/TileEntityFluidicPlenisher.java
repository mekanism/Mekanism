package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import mekanism.api.AutomationType;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.Upgrade;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.MekanismLang;
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
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.TypedOutputList;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.UnknownNullability;

public class TileEntityFluidicPlenisher extends TileEntityMekanism implements IConfigurable {

    private static final EnumSet<Direction> dirs = EnumSet.complementOf(EnumSet.of(Direction.UP));
    /// How many ticks it takes to run an operation.
    public static final int BASE_TICKS_REQUIRED = SharedConstants.TICKS_PER_SECOND;
    public static final long MAX_FLUID = 10L * FluidType.BUCKET_VOLUME;

    private final Set<BlockPos> activeNodes = new ObjectLinkedOpenHashSet<>();
    private final Set<BlockPos> usedNodes = new ObjectOpenHashSet<>();
    public boolean finishedCalc;
    public int ticksRequired = BASE_TICKS_REQUIRED;
    /// How many ticks this machine has been operating for.
    public int operatingTicks;
    private boolean usedEnergy = false;

    @UnknownNullability//Initialized via getInitialEnergyContainer
    private MachineEnergyContainer<TileEntityFluidicPlenisher> energyContainer;
    @UnknownNullability//Initialized via getInitialFluidTanks
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getFluid", "getFluidCapacity", "getFluidNeeded",
                                                                                     "getFluidFilledPercentage"}, docPlaceholder = "buffer tank")
    public BasicFluidTank fluidTank;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem", docPlaceholder = "input slot")
    FluidInventorySlot inputSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem", docPlaceholder = "output slot")
    OutputInventorySlot outputSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntityFluidicPlenisher(BlockPos pos, BlockState state) {
        super(MekanismBlocks.FLUIDIC_PLENISHER, pos, state);
    }

    @Override
    protected IContainerHolder<IFluidTank> getInitialFluidTanks(IContentsListener listener) {
        MekContainerHelper<IFluidTank> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(fluidTank = BasicFluidTank.input(MAX_FLUID, this::isValidFluid, listener), RelativeSide.TOP);
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
        builder.addContainer(inputSlot = FluidInventorySlot.fill(fluidTank, listener, 28, 20), RelativeSide.TOP);
        builder.addContainer(outputSlot = OutputInventorySlot.at(listener, 28, 51), RelativeSide.BOTTOM);
        builder.addContainer(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 143, 35), RelativeSide.BACK);
        return builder.build();
    }

    private boolean isValidFluid(FluidResource fluidType) {
        return fluidType.getFluidType().canBePlacedInLevel(getLevel(), worldPosition.below(), fluidType.toStack(FluidType.BUCKET_VOLUME));
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        energySlot.fillContainerOrConvert(null);
        inputSlot.fillTankFromSlot(outputSlot, null);
        int clientEnergyUsed = 0;
        if (canFunction() && fluidTank.amountAsLong() >= FluidType.BUCKET_VOLUME) {
            try (Transaction transaction = Transaction.openRoot()) {
                int energyPerTick = energyContainer.getEnergyPerTick();
                if (energyContainer.extract(energyPerTick, transaction, AutomationType.INTERNAL) == energyPerTick) {
                    operatingTicks++;
                    if (operatingTicks >= ticksRequired) {
                        operatingTicks = 0;
                        FluidResource fluidType = fluidTank.resource();
                        if (finishedCalc) {
                            BlockPos below = getBlockPos().below();
                            //Note: We already validated that the fluid tank is not empty so our resource doesn't represent the empty resource
                            if (canReplace(level, below, false, false) &&
                                fluidTank.extract(fluidType, FluidType.BUCKET_VOLUME, transaction, AutomationType.INTERNAL) == FluidType.BUCKET_VOLUME &&
                                FluidUtil.tryPlaceFluid(fluidType, null, level, below, true)) {
                                level.gameEvent(null, GameEvent.FLUID_PLACE, below);
                                clientEnergyUsed = energyPerTick;
                                transaction.commit();
                            }
                        } else {
                            doPlenish(level, fluidType, transaction);
                            clientEnergyUsed = energyPerTick;
                            transaction.commit();
                        }
                    } else if (!finishedCalc) {
                        clientEnergyUsed = energyPerTick;
                        transaction.commit();
                    }
                }
            }
        }
        usedEnergy = clientEnergyUsed > 0;
        return sendUpdatePacket;
    }

    private void doPlenish(ServerLevel level, FluidResource fluidType, TransactionContext transaction) {
        if (usedNodes.size() >= MekanismConfig.general.maxPlenisherNodes.get()) {
            finishedCalc = true;
            return;
        }
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        if (activeNodes.isEmpty()) {
            if (usedNodes.isEmpty()) {
                mutable.setWithOffset(getBlockPos(), Direction.DOWN);
                if (!canReplace(level, mutable, true, true)) {
                    finishedCalc = true;
                    return;
                }
                activeNodes.add(mutable.immutable());
            } else {
                finishedCalc = true;
                return;
            }
        }
        Set<BlockPos> toRemove = new ObjectOpenHashSet<>();
        for (BlockPos nodePos : activeNodes) {
            if (WorldUtils.isBlockLoaded(level, nodePos)) {
                try (Transaction subTransaction = Transaction.open(transaction)) {
                    if (canReplace(level, nodePos, true, false) &&
                        fluidTank.extract(fluidType, FluidType.BUCKET_VOLUME, subTransaction, AutomationType.INTERNAL) == FluidType.BUCKET_VOLUME &&
                        FluidUtil.tryPlaceFluid(fluidType, null, level, nodePos, true)) {
                        subTransaction.commit();
                    }
                }
                for (Direction dir : dirs) {
                    mutable.setWithOffset(nodePos, dir);
                    if (WorldUtils.isBlockLoaded(level, mutable) && canReplace(level, mutable, true, true)) {
                        activeNodes.add(mutable.immutable());
                    }
                }
                toRemove.add(nodePos);
                break;
            } else {
                toRemove.add(nodePos);
            }
        }
        usedNodes.addAll(toRemove);
        activeNodes.removeAll(toRemove);
    }

    private boolean canReplace(ServerLevel level, BlockPos pos, boolean checkNodes, boolean isPathfinding) {
        if (checkNodes && usedNodes.contains(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return true;
        }
        FluidState currentFluidState = state.getFluidState();
        if (!currentFluidState.isEmpty()) {
            //There is currently a fluid in the spot
            if (currentFluidState.isSource()) {
                //If it is a source return based on if we are path finding
                return isPathfinding;
            }
            //Always return true if it is not a source block
            return true;
        }
        FluidResource fluidType = fluidTank.resource();
        if (fluidType.isEmpty()) {
            //If we are empty, base it off of if it is replaceable in general or if it is a liquid container
            return state.canBeReplaced() || state.getBlock() instanceof LiquidBlockContainer;
        }
        Fluid fluid = fluidType.getFluid();
        if (state.canBeReplaced(fluid)) {
            //If we can replace the block then return so
            return true;
        }
        //Otherwise, just return if it is a liquid container that can support the type of fluid we are offering
        return state.getBlock() instanceof LiquidBlockContainer liquidBlockContainer && liquidBlockContainer.canPlaceLiquid(null, level, pos, state, fluid);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(SerializationConstants.PROGRESS, operatingTicks);
        output.putBoolean(SerializationConstants.FINISHED, finishedCalc);
        if (!activeNodes.isEmpty()) {
            TypedOutputList<BlockPos> activeNodesOutput = output.list(SerializationConstants.ACTIVE_NODES, BlockPos.CODEC);
            for (BlockPos activeNode : activeNodes) {
                activeNodesOutput.add(activeNode);
            }
        }
        if (!usedNodes.isEmpty()) {
            TypedOutputList<BlockPos> usedNodesOutput = output.list(SerializationConstants.USED_NODES, BlockPos.CODEC);
            for (BlockPos usedNode : usedNodes) {
                usedNodesOutput.add(usedNode);
            }
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        operatingTicks = input.getIntOr(SerializationConstants.PROGRESS, operatingTicks);
        finishedCalc = input.getBooleanOr(SerializationConstants.FINISHED, finishedCalc);
        for (BlockPos pos : input.listOrEmpty(SerializationConstants.ACTIVE_NODES, BlockPos.CODEC)) {
            activeNodes.add(pos);
        }
        for (BlockPos pos : input.listOrEmpty(SerializationConstants.USED_NODES, BlockPos.CODEC)) {
            usedNodes.add(pos);
        }
    }

    @Override
    @Deprecated
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard(SerializationConstants.ACTIVE_NODES);
        output.discard(SerializationConstants.USED_NODES);
        output.discard(SerializationConstants.FINISHED);
    }

    public void reset() {
        activeNodes.clear();
        usedNodes.clear();
        finishedCalc = false;
    }

    @Override
    public InteractionResult onSneakRightClick(Level level, Player player) {
        reset();
        player.sendOverlayMessage(MekanismLang.PLENISHER_RESET.translate());
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onRightClick(Level level, Player player) {
        return InteractionResult.PASS;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
        }
    }

    @Override
    public List<Component> getInfo(Upgrade upgrade) {
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
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
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(() -> finishedCalc, value -> finishedCalc = value));
        container.track(SyncableBoolean.create(this::usedEnergy, value -> usedEnergy = value));
    }

    public boolean usedEnergy() {
        return usedEnergy;
    }

    public MachineEnergyContainer<TileEntityFluidicPlenisher> energyContainer() {
        return energyContainer;
    }

    //Methods relating to IComputerTile
    @ComputerMethod(nameOverride = "reset", requiresPublicSecurity = true)
    void resetPlenisher() throws ComputerException {
        validateSecurityIsPublic();
        reset();
    }
    //End methods IComputerTile
}
