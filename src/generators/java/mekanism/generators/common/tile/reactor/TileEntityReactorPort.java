package mekanism.generators.common.tile.reactor;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.IHeatTransfer;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismGases;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.text.BooleanStateDisplay.OutputInput;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.generators.common.GeneratorsBlock;
import mekanism.generators.common.item.ItemHohlraum;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityReactorPort extends TileEntityReactorBlock implements IFluidHandlerWrapper, IGasHandler, IHeatTransfer, IConfigurable {

    public boolean fluidEject;

    public TileEntityReactorPort() {
        super(GeneratorsBlock.REACTOR_PORT);
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        fluidEject = nbtTags.getBoolean("fluidEject");
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean("fluidEject", fluidEject);
        return nbtTags;
    }

    @Override
    public boolean isFrame() {
        return false;
    }

    @Override
    public void onUpdate() {
        if (changed) {
            world.notifyNeighborsOfStateChange(getPos(), getBlockType());
        }

        super.onUpdate();
        if (!world.isRemote) {
            CableUtils.emit(this);
            if (fluidEject && getReactor() != null && !getReactor().getSteamTank().getFluid().isEmpty()) {
                IFluidTank tank = getReactor().getSteamTank();
                EmitUtils.forEachSide(getWorld(), getPos(), EnumSet.allOf(Direction.class), (tile, side) -> {
                    if (!(tile instanceof TileEntityReactorPort)) {
                        CapabilityUtils.getCapabilityHelper(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()).ifPresent(handler -> {
                            if (PipeUtils.canFill(handler, tank.getFluid())) {
                                tank.drain(handler.fill(tank.getFluid(), FluidAction.EXECUTE), FluidAction.EXECUTE);
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        return getReactor().getWaterTank().fill(resource, fluidAction);
    }

    @Nonnull
    @Override
    public FluidStack drain(Direction from, int maxDrain, FluidAction fluidAction) {
        return getReactor().getSteamTank().drain(maxDrain, fluidAction);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        return getReactor() != null && !fluidEject && fluid.getFluid() == Fluids.WATER;
    }

    @Override
    public boolean canDrain(Direction from, @Nonnull FluidStack fluid) {
        return getReactor() != null && (fluid.isEmpty() || fluid.getFluid() == MekanismGases.STEAM.getFluid());
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        if (getReactor() == null) {
            return PipeUtils.EMPTY;
        }
        return new IFluidTank[]{getReactor().getWaterTank(), getReactor().getSteamTank()};
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return getTankInfo(null);
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, boolean doTransfer) {
        if (getReactor() != null) {
            if (stack.getGas().isIn(MekanismTags.DEUTERIUM)) {
                return getReactor().getDeuteriumTank().receive(stack, doTransfer);
            } else if (stack.getGas().isIn(MekanismTags.TRITIUM)) {
                return getReactor().getTritiumTank().receive(stack, doTransfer);
            } else if (stack.getGas().isIn(MekanismTags.FUSION_FUEL)) {
                return getReactor().getFuelTank().receive(stack, doTransfer);
            }
        }
        return 0;
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, boolean doTransfer) {
        return null;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        return type.isIn(MekanismTags.DEUTERIUM) || type.isIn(MekanismTags.TRITIUM) || type.isIn(MekanismTags.FUSION_FUEL);
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        return false;
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return getReactor() != null ? new GasTankInfo[]{getReactor().getDeuteriumTank(), getReactor().getTritiumTank(), getReactor().getFuelTank()} : IGasHandler.NONE;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        }
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, side)));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public double getEnergy() {
        if (getReactor() == null) {
            return 0;
        }
        return getReactor().getBufferedEnergy();
    }

    @Override
    public void setEnergy(double energy) {
        if (getReactor() != null) {
            getReactor().setBufferedEnergy(energy);
        }
    }

    @Override
    public double getMaxEnergy() {
        if (getReactor() == null) {
            return 0;
        }
        return getReactor().getBufferSize();
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        return true;
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return false;
    }

    @Override
    public double getMaxOutput() {
        return Integer.MAX_VALUE;
    }

    @Override
    public double getTemp() {
        if (getReactor() != null) {
            return getReactor().getTemp();
        }
        return 0;
    }

    @Override
    public double getInverseConductionCoefficient() {
        return 5;
    }

    @Override
    public double getInsulationCoefficient(Direction side) {
        if (getReactor() != null) {
            return getReactor().getInsulationCoefficient(side);
        }
        return 0;
    }

    @Override
    public void transferHeatTo(double heat) {
        if (getReactor() != null) {
            getReactor().transferHeatTo(heat);
        }
    }

    @Override
    public double[] simulateHeat() {
        return HeatUtils.simulate(this);
    }

    @Override
    public double applyTemperatureChange() {
        if (getReactor() != null) {
            return getReactor().applyTemperatureChange();
        }
        return 0;
    }

    @Override
    public boolean canConnectHeat(Direction side) {
        return getReactor() != null;
    }

    @Nullable
    @Override
    public IHeatTransfer getAdjacent(Direction side) {
        TileEntity adj = Coord4D.get(this).offset(side).getTileEntity(world);
        if (!(adj instanceof TileEntityReactorBlock)) {
            return CapabilityUtils.getCapabilityHelper(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()).getValue();
        }
        return null;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slotID) {
        return getReactor() != null && getReactor().isFormed() ? getReactor().getInventory().get(slotID) : ItemStack.EMPTY;
    }

    @Override
    public int getSizeInventory() {
        return getReactor() != null && getReactor().isFormed() ? 1 : 0;
    }

    @Override
    public void setInventorySlotContents(int slotID, @Nonnull ItemStack itemstack) {
        if (getReactor() != null && getReactor().isFormed()) {
            getReactor().getInventory().set(slotID, itemstack);
            if (!itemstack.isEmpty() && itemstack.getCount() > getInventoryStackLimit()) {
                itemstack.setCount(getInventoryStackLimit());
            }
        }
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        return getReactor() != null && getReactor().isFormed() ? new int[]{0} : InventoryUtils.EMPTY;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            //Allow inserting
            return false;
        } else if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return getReactor() == null;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (getReactor() != null && getReactor().isFormed() && itemstack.getItem() instanceof ItemHohlraum) {
            ItemHohlraum hohlraum = (ItemHohlraum) itemstack.getItem();
            return !hohlraum.getGas(itemstack).isEmpty() && hohlraum.getGas(itemstack).getAmount() == hohlraum.getMaxGas(itemstack);
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        if (getReactor() != null && getReactor().isFormed() && itemstack.getItem() instanceof ItemHohlraum) {
            ItemHohlraum hohlraum = (ItemHohlraum) itemstack.getItem();
            return hohlraum.getGas(itemstack).isEmpty();
        }
        return false;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (world.isRemote) {
            boolean prevEject = fluidEject;
            fluidEject = dataStream.readBoolean();
            if (prevEject != fluidEject) {
                MekanismUtils.updateBlock(world, getPos());
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(fluidEject);
        return data;
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        if (!world.isRemote) {
            fluidEject = !fluidEject;
            player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GRAY,
                  Translation.of("tooltip.configurator.reactorPortEject"), " ", (fluidEject ? EnumColor.DARK_RED : EnumColor.DARK_GREEN), OutputInput.of(fluidEject), "."));
            Mekanism.packetHandler.sendUpdatePacket(this);
            markDirty();
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }
}