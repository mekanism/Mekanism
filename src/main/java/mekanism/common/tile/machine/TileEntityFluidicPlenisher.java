package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.Upgrade;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
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
import mekanism.common.util.NBTUtils;
import mekanism.common.util.UpgradeUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public class TileEntityFluidicPlenisher extends TileEntityMekanism implements IConfigurable {

    private static final EnumSet<Direction> dirs = EnumSet.complementOf(EnumSet.of(Direction.UP));
    /**
     * How many ticks it takes to run an operation.
     */
    public static final int BASE_TICKS_REQUIRED = SharedConstants.TICKS_PER_SECOND;
    public static final int MAX_FLUID = 10 * FluidType.BUCKET_VOLUME;

    private final Set<BlockPos> activeNodes = new ObjectLinkedOpenHashSet<>();
    private final Set<BlockPos> usedNodes = new ObjectOpenHashSet<>();
    public boolean finishedCalc;
    public int ticksRequired = BASE_TICKS_REQUIRED;
    /**
     * How many ticks this machine has been operating for.
     */
    public int operatingTicks;
    private boolean usedEnergy = false;

    private MachineEnergyContainer<TileEntityFluidicPlenisher> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getFluid", "getFluidCapacity", "getFluidNeeded",
                                                                                     "getFluidFilledPercentage"}, docPlaceholder = "buffer tank")
    public BasicFluidTank fluidTank;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem", docPlaceholder = "input slot")
    FluidInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem", docPlaceholder = "output slot")
    OutputInventorySlot outputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntityFluidicPlenisher(BlockPos pos, BlockState state) {
        super(MekanismBlocks.FLUIDIC_PLENISHER, pos, state);
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        FluidTankHelper builder = FluidTankHelper.forSide(facingSupplier);
        builder.addTank(fluidTank = BasicFluidTank.input(MAX_FLUID, this::isValidFluid, listener), RelativeSide.TOP);
        return builder.build();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(facingSupplier);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener), RelativeSide.BACK);
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        builder.addSlot(inputSlot = FluidInventorySlot.fill(fluidTank, listener, 28, 20), RelativeSide.TOP);
        builder.addSlot(outputSlot = OutputInventorySlot.at(listener, 28, 51), RelativeSide.BOTTOM);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 143, 35), RelativeSide.BACK);
        return builder.build();
    }

    private boolean isValidFluid(@NotNull FluidStack stack) {
        return stack.getFluidType().canBePlacedInLevel(getLevel(), worldPosition.below(), stack);
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        inputSlot.fillTank(outputSlot);
        long clientEnergyUsed = 0L;
        if (canFunction() && !fluidTank.isEmpty()) {
            long energyPerTick = energyContainer.getEnergyPerTick();
            if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL) == energyPerTick) {
                if (!finishedCalc) {
                    clientEnergyUsed = energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                }
                operatingTicks++;
                if (operatingTicks >= ticksRequired) {
                    operatingTicks = 0;
                    if (finishedCalc) {
                        BlockPos below = getBlockPos().below();
                        if (canReplace(below, false, false) && canExtractBucket() &&
                            WorldUtils.tryPlaceContainedLiquid(null, level, below, fluidTank.getFluid(), null)) {
                            level.gameEvent(null, GameEvent.FLUID_PLACE, below);
                            clientEnergyUsed = energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                            fluidTank.extract(FluidType.BUCKET_VOLUME, Action.EXECUTE, AutomationType.INTERNAL);
                        }
                    } else {
                        doPlenish();
                    }
                }
            }
        }
        usedEnergy = clientEnergyUsed > 0L;
        return sendUpdatePacket;
    }

    private boolean canExtractBucket() {
        return fluidTank.extract(FluidType.BUCKET_VOLUME, Action.SIMULATE, AutomationType.INTERNAL).getAmount() == FluidType.BUCKET_VOLUME;
    }

    private void doPlenish() {
        if (usedNodes.size() >= MekanismConfig.general.maxPlenisherNodes.get()) {
            finishedCalc = true;
            return;
        }
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        if (activeNodes.isEmpty()) {
            if (usedNodes.isEmpty()) {
                mutable.setWithOffset(getBlockPos(), Direction.DOWN);
                if (!canReplace(mutable, true, true)) {
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
                if (canReplace(nodePos, true, false) && canExtractBucket() &&
                    WorldUtils.tryPlaceContainedLiquid(null, level, nodePos, fluidTank.getFluid(), null)) {
                    level.gameEvent(null, GameEvent.FLUID_PLACE, nodePos);
                    fluidTank.extract(FluidType.BUCKET_VOLUME, Action.EXECUTE, AutomationType.INTERNAL);
                }
                for (Direction dir : dirs) {
                    mutable.setWithOffset(nodePos, dir);
                    if (WorldUtils.isBlockLoaded(level, mutable) && canReplace(mutable, true, true)) {
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

    private boolean canReplace(BlockPos pos, boolean checkNodes, boolean isPathfinding) {
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
        FluidStack stack = fluidTank.getFluid();
        if (stack.isEmpty()) {
            //If we are empty, base it off of if it is replaceable in general or if it is a liquid container
            return state.canBeReplaced() || state.getBlock() instanceof LiquidBlockContainer;
        }
        Fluid fluid = stack.getFluid();
        if (state.canBeReplaced(fluid)) {
            //If we can replace the block then return so
            return true;
        }
        //Otherwise, just return if it is a liquid container that can support the type of fluid we are offering
        return state.getBlock() instanceof LiquidBlockContainer liquidBlockContainer && liquidBlockContainer.canPlaceLiquid(null, level, pos, state, fluid);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        nbtTags.putInt(SerializationConstants.PROGRESS, operatingTicks);
        nbtTags.putBoolean(SerializationConstants.FINISHED, finishedCalc);
        if (!activeNodes.isEmpty()) {
            nbtTags.put(SerializationConstants.ACTIVE_NODES, NBTUtils.writeBlockPositions(activeNodes));
        }
        if (!usedNodes.isEmpty()) {
            nbtTags.put(SerializationConstants.USED_NODES, NBTUtils.writeBlockPositions(usedNodes));
        }
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        operatingTicks = nbt.getInt(SerializationConstants.PROGRESS);
        finishedCalc = nbt.getBoolean(SerializationConstants.FINISHED);
        NBTUtils.readBlockPositions(nbt, SerializationConstants.ACTIVE_NODES, activeNodes);
        NBTUtils.readBlockPositions(nbt, SerializationConstants.USED_NODES, usedNodes);
    }

    @Override
    @Deprecated
    public void removeComponentsFromTag(@NotNull CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(SerializationConstants.ACTIVE_NODES);
        tag.remove(SerializationConstants.USED_NODES);
        tag.remove(SerializationConstants.FINISHED);
    }

    public void reset() {
        activeNodes.clear();
        usedNodes.clear();
        finishedCalc = false;
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        reset();
        player.displayClientMessage(MekanismLang.PLENISHER_RESET.translate(), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onRightClick(Player player) {
        return InteractionResult.PASS;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
        }
    }

    @NotNull
    @Override
    public List<Component> getInfo(@NotNull Upgrade upgrade) {
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }

    @Override
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
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

    public MachineEnergyContainer<TileEntityFluidicPlenisher> getEnergyContainer() {
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
