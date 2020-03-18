package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.inventory.AutomationType;
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
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;

public class TileEntityFluidicPlenisher extends TileEntityMekanism implements IConfigurable {

    private static EnumSet<Direction> dirs = EnumSet.complementOf(EnumSet.of(Direction.UP));
    private Set<BlockPos> activeNodes = new ObjectLinkedOpenHashSet<>();
    private Set<BlockPos> usedNodes = new ObjectOpenHashSet<>();
    public boolean finishedCalc;
    public BasicFluidTank fluidTank;
    /**
     * How many ticks it takes to run an operation.
     */
    public int BASE_TICKS_REQUIRED = 20;
    public int ticksRequired = BASE_TICKS_REQUIRED;
    /**
     * How many ticks this machine has been operating for.
     */
    public int operatingTicks;

    private MachineEnergyContainer<TileEntityFluidicPlenisher> energyContainer;
    private FluidInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityFluidicPlenisher() {
        super(MekanismBlocks.FLUIDIC_PLENISHER);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
        //TODO: Is there a better position to use, should it maybe get the default fluid state
        builder.addTank(fluidTank = BasicFluidTank.input(10_000,
              fluid -> fluid.getFluid().getAttributes().canBePlacedInWorld(getWorld(), BlockPos.ZERO, fluid), this), RelativeSide.TOP);
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

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        inputSlot.fillTank(outputSlot);

        if (MekanismUtils.canFunction(this) && !fluidTank.isEmpty()) {
            double energyPerTick = energyContainer.getEnergyPerTick();
            if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL) == energyPerTick) {
                if (!finishedCalc) {
                    energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                }
                operatingTicks++;
                if (operatingTicks >= ticksRequired) {
                    operatingTicks = 0;
                    if (finishedCalc) {
                        BlockPos below = getPos().offset(Direction.DOWN);
                        if (canReplace(below, false, false) && fluidTank.getFluidAmount() >= FluidAttributes.BUCKET_VOLUME) {
                            if (fluidTank.getFluid().getFluid().getAttributes().canBePlacedInWorld(world, below, fluidTank.getFluid())) {
                                //TODO: Set fluid state??
                                world.setBlockState(below, MekanismUtils.getFlowingBlockState(fluidTank.getFluid()));
                                energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                                fluidTank.extract(FluidAttributes.BUCKET_VOLUME, Action.EXECUTE, AutomationType.INTERNAL);
                            }
                        }
                    } else {
                        doPlenish();
                    }
                }
            }
        }
    }

    private void doPlenish() {
        if (usedNodes.size() >= MekanismConfig.general.maxPlenisherNodes.get()) {
            finishedCalc = true;
            return;
        }
        if (activeNodes.isEmpty()) {
            if (usedNodes.isEmpty()) {
                BlockPos below = getPos().offset(Direction.DOWN);
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
        for (BlockPos coordPos : activeNodes) {
            if (MekanismUtils.isBlockLoaded(world, coordPos)) {
                if (canReplace(coordPos, true, false) && !fluidTank.isEmpty()) {
                    world.setBlockState(coordPos, MekanismUtils.getFlowingBlockState(fluidTank.getFluid()));
                    fluidTank.extract(FluidAttributes.BUCKET_VOLUME, Action.EXECUTE, AutomationType.INTERNAL);
                }

                for (Direction dir : dirs) {
                    BlockPos sidePos = coordPos.offset(dir);
                    if (MekanismUtils.isBlockLoaded(world, sidePos) && canReplace(sidePos, true, true)) {
                        activeNodes.add(sidePos);
                    }
                }
                toRemove.add(coordPos);
                break;
            } else {
                toRemove.add(coordPos);
            }
        }
        usedNodes.addAll(toRemove);
        activeNodes.removeAll(toRemove);
    }

    public boolean canReplace(BlockPos pos, boolean checkNodes, boolean isPathfinding) {
        if (checkNodes && usedNodes.contains(pos)) {
            return false;
        }
        if (world.isAirBlock(pos) || MekanismUtils.isDeadFluid(world, pos)) {
            return true;
        }
        if (MekanismUtils.isFluid(world, pos)) {
            return isPathfinding;
        }
        return MekanismUtils.isValidReplaceableBlock(world, pos);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt(NBTConstants.PROGRESS, operatingTicks);
        nbtTags.putBoolean(NBTConstants.FINISHED, finishedCalc);

        ListNBT activeList = new ListNBT();
        for (BlockPos wrapper : activeNodes) {
            activeList.add(NBTUtil.writeBlockPos(wrapper));
        }
        if (!activeList.isEmpty()) {
            nbtTags.put(NBTConstants.ACTIVE_NODES, activeList);
        }

        ListNBT usedList = new ListNBT();
        for (BlockPos obj : usedNodes) {
            activeList.add(NBTUtil.writeBlockPos(obj));
        }
        if (!activeList.isEmpty()) {
            nbtTags.put(NBTConstants.USED_NODES, usedList);
        }
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
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
        player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, MekanismLang.PLENISHER_RESET.translateColored(EnumColor.GRAY)));
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapabilityIfEnabled(capability, side);
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