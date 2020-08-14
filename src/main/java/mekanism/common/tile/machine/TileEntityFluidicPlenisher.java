package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
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
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;
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
    public BasicFluidTank fluidTank;
    private FluidInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityFluidicPlenisher() {
        super(MekanismBlocks.FLUIDIC_PLENISHER);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE_CAPABILITY, this));
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
        builder.addTank(fluidTank = BasicFluidTank.input(10_000, this::isValidFluid, this), RelativeSide.TOP);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this), RelativeSide.BACK);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(inputSlot = FluidInventorySlot.fill(fluidTank, this, 28, 20), RelativeSide.TOP);
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 28, 51), RelativeSide.BOTTOM);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 143, 35), RelativeSide.BACK);
        return builder.build();
    }

    private boolean isValidFluid(@Nonnull FluidStack stack) {
        return stack.getFluid().getAttributes().canBePlacedInWorld(getWorld(), pos.down(), stack);
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
                        BlockPos below = getPos().down();
                        if (canReplace(below, false, false) && canExtractBucket() &&
                            MekanismUtils.tryPlaceContainedLiquid(null, world, below, fluidTank.getFluid(), null)) {
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
                BlockPos below = getPos().down();
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
            if (MekanismUtils.isBlockLoaded(world, nodePos)) {
                if (canReplace(nodePos, true, false) && canExtractBucket() &&
                    MekanismUtils.tryPlaceContainedLiquid(null, world, nodePos, fluidTank.getFluid(), null)) {
                    fluidTank.extract(FluidAttributes.BUCKET_VOLUME, Action.EXECUTE, AutomationType.INTERNAL);
                }
                for (Direction dir : dirs) {
                    BlockPos sidePos = nodePos.offset(dir);
                    if (MekanismUtils.isBlockLoaded(world, sidePos) && canReplace(sidePos, true, true)) {
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
        if (world.isAirBlock(pos)) {
            return true;
        }
        FluidState currentFluidState = world.getFluidState(pos);
        if (!currentFluidState.isEmpty()) {
            //There is currently a fluid in the spot
            if (currentFluidState.isSource()) {
                //If it is a source return based on if we are path finding
                return isPathfinding;
            }
            //Always return true if it is not a source block
            return true;
        }
        BlockState state = world.getBlockState(pos);
        FluidStack stack = fluidTank.getFluid();
        if (stack.isEmpty()) {
            //If we are empty, base it off of if it is replaceable in general or if it is a liquid container
            return MekanismUtils.isValidReplaceableBlock(world, pos) || state.getBlock() instanceof ILiquidContainer;
        }
        Fluid fluid = stack.getFluid();
        if (state.isReplaceable(fluid)) {
            //If we can replace the block then return so
            return true;
        }
        //Otherwise just return if it is a liquid container that can support the type of fluid we are offering
        return state.getBlock() instanceof ILiquidContainer && ((ILiquidContainer) state.getBlock()).canContainFluid(world, pos, state, fluid);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt(NBTConstants.PROGRESS, operatingTicks);
        nbtTags.putBoolean(NBTConstants.FINISHED, finishedCalc);
        if (!activeNodes.isEmpty()) {
            ListNBT activeList = new ListNBT();
            for (BlockPos wrapper : activeNodes) {
                activeList.add(NBTUtil.writeBlockPos(wrapper));
            }
            nbtTags.put(NBTConstants.ACTIVE_NODES, activeList);
        }
        if (!usedNodes.isEmpty()) {
            ListNBT usedList = new ListNBT();
            for (BlockPos obj : usedNodes) {
                usedList.add(NBTUtil.writeBlockPos(obj));
            }
            nbtTags.put(NBTConstants.USED_NODES, usedList);
        }
        return nbtTags;
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);
        operatingTicks = nbtTags.getInt(NBTConstants.PROGRESS);
        finishedCalc = nbtTags.getBoolean(NBTConstants.FINISHED);
        if (nbtTags.contains(NBTConstants.ACTIVE_NODES, NBT.TAG_LIST)) {
            ListNBT tagList = nbtTags.getList(NBTConstants.ACTIVE_NODES, NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                activeNodes.add(NBTUtil.readBlockPos(tagList.getCompound(i)));
            }
        }
        if (nbtTags.contains(NBTConstants.USED_NODES, NBT.TAG_LIST)) {
            ListNBT tagList = nbtTags.getList(NBTConstants.USED_NODES, NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                usedNodes.add(NBTUtil.readBlockPos(tagList.getCompound(i)));
            }
        }
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        activeNodes.clear();
        usedNodes.clear();
        finishedCalc = false;
        player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, EnumColor.GRAY, MekanismLang.PLENISHER_RESET),
              Util.DUMMY_UUID);
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
        }
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(() -> finishedCalc, value -> finishedCalc = value));
    }

    public MachineEnergyContainer<TileEntityFluidicPlenisher> getEnergyContainer() {
        return energyContainer;
    }
}