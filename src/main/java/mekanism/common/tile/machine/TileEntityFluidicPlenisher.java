package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.math.FloatingLong;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
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
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityFluidicPlenisher extends TileEntityMekanism implements IConfigurable {

    private static final EnumSet<Direction> dirs = EnumSet.complementOf(EnumSet.of(Direction.UP));
    /**
     * How many ticks it takes to run an operation.
     */
    public static final int BASE_TICKS_REQUIRED = 20;
    private final Set<BlockPos> activeNodes = new ObjectLinkedOpenHashSet<>();
    private final Set<BlockPos> usedNodes = new ObjectOpenHashSet<>();
    public boolean finishedCalc;
    public int ticksRequired = BASE_TICKS_REQUIRED;
    /**
     * How many ticks this machine has been operating for.
     */
    public int operatingTicks;

    private MachineEnergyContainer<TileEntityFluidicPlenisher> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getFluid", "getFluidCapacity", "getFluidNeeded", "getFluidFilledPercentage"})
    public BasicFluidTank fluidTank;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem")
    private FluidInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem")
    private OutputInventorySlot outputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityFluidicPlenisher(BlockPos pos, BlockState state) {
        super(MekanismBlocks.FLUIDIC_PLENISHER, pos, state);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE_CAPABILITY, this));
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
        builder.addTank(fluidTank = BasicFluidTank.input(10_000, this::isValidFluid, listener), RelativeSide.TOP);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener), RelativeSide.BACK);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(inputSlot = FluidInventorySlot.fill(fluidTank, listener, 28, 20), RelativeSide.TOP);
        builder.addSlot(outputSlot = OutputInventorySlot.at(listener, 28, 51), RelativeSide.BOTTOM);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 143, 35), RelativeSide.BACK);
        return builder.build();
    }

    private boolean isValidFluid(@Nonnull FluidStack stack) {
        return stack.getFluid().getAttributes().canBePlacedInWorld(getLevel(), worldPosition.below(), stack);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        inputSlot.fillTank(outputSlot);
        if (MekanismUtils.canFunction(this) && !fluidTank.isEmpty()) {
            FloatingLong energyPerTick = energyContainer.getEnergyPerTick();
            if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
                if (!finishedCalc) {
                    energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                }
                operatingTicks++;
                if (operatingTicks >= ticksRequired) {
                    operatingTicks = 0;
                    if (finishedCalc) {
                        BlockPos below = getBlockPos().below();
                        if (canReplace(below, false, false) && canExtractBucket() &&
                            WorldUtils.tryPlaceContainedLiquid(null, level, below, fluidTank.getFluid(), null)) {
                            level.gameEvent(GameEvent.FLUID_PLACE, below);
                            energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                            fluidTank.extract(FluidAttributes.BUCKET_VOLUME, Action.EXECUTE, AutomationType.INTERNAL);
                        }
                    } else {
                        doPlenish();
                    }
                }
            }
        }
    }

    private boolean canExtractBucket() {
        return fluidTank.extract(FluidAttributes.BUCKET_VOLUME, Action.SIMULATE, AutomationType.INTERNAL).getAmount() == FluidAttributes.BUCKET_VOLUME;
    }

    private void doPlenish() {
        if (usedNodes.size() >= MekanismConfig.general.maxPlenisherNodes.get()) {
            finishedCalc = true;
            return;
        }
        if (activeNodes.isEmpty()) {
            if (usedNodes.isEmpty()) {
                BlockPos below = getBlockPos().below();
                if (!canReplace(below, true, true)) {
                    finishedCalc = true;
                    return;
                }
                activeNodes.add(below);
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
                    level.gameEvent(GameEvent.FLUID_PLACE, nodePos);
                    fluidTank.extract(FluidAttributes.BUCKET_VOLUME, Action.EXECUTE, AutomationType.INTERNAL);
                }
                for (Direction dir : dirs) {
                    BlockPos sidePos = nodePos.relative(dir);
                    if (WorldUtils.isBlockLoaded(level, sidePos) && canReplace(sidePos, true, true)) {
                        activeNodes.add(sidePos);
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
            return WorldUtils.isValidReplaceableBlock(level, pos) || state.getBlock() instanceof LiquidBlockContainer;
        }
        Fluid fluid = stack.getFluid();
        if (state.canBeReplaced(fluid)) {
            //If we can replace the block then return so
            return true;
        }
        //Otherwise, just return if it is a liquid container that can support the type of fluid we are offering
        return state.getBlock() instanceof LiquidBlockContainer liquidBlockContainer && liquidBlockContainer.canPlaceLiquid(level, pos, state, fluid);
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag nbtTags) {
        super.saveAdditional(nbtTags);
        nbtTags.putInt(NBTConstants.PROGRESS, operatingTicks);
        nbtTags.putBoolean(NBTConstants.FINISHED, finishedCalc);
        if (!activeNodes.isEmpty()) {
            ListTag activeList = new ListTag();
            for (BlockPos wrapper : activeNodes) {
                activeList.add(NbtUtils.writeBlockPos(wrapper));
            }
            nbtTags.put(NBTConstants.ACTIVE_NODES, activeList);
        }
        if (!usedNodes.isEmpty()) {
            ListTag usedList = new ListTag();
            for (BlockPos obj : usedNodes) {
                usedList.add(NbtUtils.writeBlockPos(obj));
            }
            nbtTags.put(NBTConstants.USED_NODES, usedList);
        }
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        operatingTicks = nbt.getInt(NBTConstants.PROGRESS);
        finishedCalc = nbt.getBoolean(NBTConstants.FINISHED);
        if (nbt.contains(NBTConstants.ACTIVE_NODES, Tag.TAG_LIST)) {
            ListTag tagList = nbt.getList(NBTConstants.ACTIVE_NODES, Tag.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                activeNodes.add(NbtUtils.readBlockPos(tagList.getCompound(i)));
            }
        }
        if (nbt.contains(NBTConstants.USED_NODES, Tag.TAG_LIST)) {
            ListTag tagList = nbt.getList(NBTConstants.USED_NODES, Tag.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                usedNodes.add(NbtUtils.readBlockPos(tagList.getCompound(i)));
            }
        }
    }

    public void reset() {
        activeNodes.clear();
        usedNodes.clear();
        finishedCalc = false;
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        reset();
        player.sendMessage(MekanismUtils.logFormat(MekanismLang.PLENISHER_RESET), Util.NIL_UUID);
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

    @Override
    public List<Component> getInfo(Upgrade upgrade) {
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }

    @Override
    protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
        return type == SubstanceType.FLUID;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(() -> finishedCalc, value -> finishedCalc = value));
    }

    public MachineEnergyContainer<TileEntityFluidicPlenisher> getEnergyContainer() {
        return energyContainer;
    }

    //Methods relating to IComputerTile
    @ComputerMethod(nameOverride = "reset")
    private void resetPlenisher() throws ComputerException {
        validateSecurityIsPublic();
        reset();
    }
    //End methods IComputerTile
}