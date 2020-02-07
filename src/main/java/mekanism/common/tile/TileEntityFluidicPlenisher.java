package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigurable;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.api.sustained.ISustainedTank;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class TileEntityFluidicPlenisher extends TileEntityMekanism implements IComputerIntegration, IConfigurable, IFluidHandlerWrapper, ISustainedTank {

    private static final String[] methods = new String[]{"reset"};
    private static EnumSet<Direction> dirs = EnumSet.complementOf(EnumSet.of(Direction.UP));
    public Set<BlockPos> activeNodes = new ObjectLinkedOpenHashSet<>();
    public Set<BlockPos> usedNodes = new ObjectOpenHashSet<>();
    public boolean finishedCalc;
    public FluidTank fluidTank;
    /**
     * How many ticks it takes to run an operation.
     */
    public int BASE_TICKS_REQUIRED = 20;
    public int ticksRequired = BASE_TICKS_REQUIRED;
    /**
     * How many ticks this machine has been operating for.
     */
    public int operatingTicks;

    private FluidInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityFluidicPlenisher() {
        super(MekanismBlocks.FLUIDIC_PLENISHER);
    }

    @Override
    protected void presetVariables() {
        fluidTank = new FluidTank(10_000);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        //TODO: Is there a better position to use
        builder.addSlot(inputSlot = FluidInventorySlot.fill(fluidTank, fluidStack ->
              fluidStack.getFluid().getAttributes().canBePlacedInWorld(getWorld(), BlockPos.ZERO, fluidStack), this, 28, 20), RelativeSide.TOP);
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 28, 51), RelativeSide.BOTTOM);
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 143, 35), RelativeSide.BACK);
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            energySlot.discharge(this);
            inputSlot.fillTank(outputSlot);

            if (MekanismUtils.canFunction(this) && getEnergy() >= getEnergyPerTick() && !fluidTank.isEmpty() &&
                fluidTank.getFluid().getFluid().getAttributes().canBePlacedInWorld(world, BlockPos.ZERO, fluidTank.getFluid())) {
                //TODO: Is there a better position to use
                if (!finishedCalc) {
                    setEnergy(getEnergy() - getEnergyPerTick());
                }
                if ((operatingTicks + 1) < ticksRequired) {
                    operatingTicks++;
                } else {
                    if (!finishedCalc) {
                        doPlenish();
                    } else {
                        BlockPos below = getPos().offset(Direction.DOWN);
                        if (canReplace(below, false, false) && fluidTank.getFluidAmount() >= FluidAttributes.BUCKET_VOLUME) {
                            if (fluidTank.getFluid().getFluid().getAttributes().canBePlacedInWorld(world, below, fluidTank.getFluid())) {
                                //TODO: Set fluid state??
                                world.setBlockState(below, MekanismUtils.getFlowingBlockState(fluidTank.getFluid()));
                                setEnergy(getEnergy() - getEnergyPerTick());
                                fluidTank.drain(FluidAttributes.BUCKET_VOLUME, FluidAction.EXECUTE);
                            }
                        }
                    }
                    operatingTicks = 0;
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
                FluidStack fluid = fluidTank.getFluid();
                if (canReplace(coordPos, true, false) && !fluid.isEmpty()) {
                    world.setBlockState(coordPos, MekanismUtils.getFlowingBlockState(fluid));
                    fluidTank.drain(FluidAttributes.BUCKET_VOLUME, FluidAction.EXECUTE);
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

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            finishedCalc = dataStream.readBoolean();
            fluidTank.setFluid(dataStream.readFluidStack());
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(finishedCalc);
        data.add(fluidTank.getFluid());
        return data;
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("operatingTicks", operatingTicks);
        nbtTags.putBoolean("finishedCalc", finishedCalc);

        if (!fluidTank.isEmpty()) {
            nbtTags.put("fluidTank", fluidTank.writeToNBT(new CompoundNBT()));
        }

        ListNBT activeList = new ListNBT();
        for (BlockPos wrapper : activeNodes) {
            activeList.add(NBTUtil.writeBlockPos(wrapper));
        }
        if (!activeList.isEmpty()) {
            nbtTags.put("activeNodes", activeList);
        }

        ListNBT usedList = new ListNBT();
        for (BlockPos obj : usedNodes) {
            activeList.add(NBTUtil.writeBlockPos(obj));
        }
        if (!activeList.isEmpty()) {
            nbtTags.put("usedNodes", usedList);
        }
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        operatingTicks = nbtTags.getInt("operatingTicks");
        finishedCalc = nbtTags.getBoolean("finishedCalc");

        if (nbtTags.contains("fluidTank")) {
            fluidTank.readFromNBT(nbtTags.getCompound("fluidTank"));
        }

        if (nbtTags.contains("activeNodes")) {
            ListNBT tagList = nbtTags.getList("activeNodes", NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                activeNodes.add(NBTUtil.readBlockPos(tagList.getCompound(i)));
            }
        }
        if (nbtTags.contains("usedNodes")) {
            ListNBT tagList = nbtTags.getList("usedNodes", NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.size(); i++) {
                usedNodes.add(NBTUtil.readBlockPos(tagList.getCompound(i)));
            }
        }
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return getOppositeDirection() == side;
    }

    @Override
    public IFluidTank[] getTankInfo(Direction direction) {
        if (direction == Direction.UP) {
            return new IFluidTank[]{fluidTank};
        }
        return PipeUtils.EMPTY;
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return getTankInfo(Direction.UP);
    }

    @Override
    public void setFluidStack(@Nonnull FluidStack fluidStack, Object... data) {
        fluidTank.setFluid(fluidStack);
    }

    @Nonnull
    @Override
    public FluidStack getFluidStack(Object... data) {
        return fluidTank.getFluid();
    }

    @Override
    public boolean hasTank(Object... data) {
        return true;
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        return fluidTank.fill(resource, fluidAction);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        //TODO: Is there a better position to use
        return from == Direction.UP && fluid.getFluid().getAttributes().canBePlacedInWorld(getWorld(), BlockPos.ZERO, fluid);
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
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, side)));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        if (method == 0) {
            activeNodes.clear();
            usedNodes.clear();
            finishedCalc = false;
            return new Object[]{"Plenisher calculation reset."};
        }
        throw new NoSuchMethodException();
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
}