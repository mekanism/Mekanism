package mekanism.generators.common.tile.reactor;

import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.IHeatTransfer;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismFluids;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.generators.common.GeneratorsBlock;
import mekanism.generators.common.item.ItemHohlraum;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityReactorPort extends TileEntityReactorBlock implements IFluidHandlerWrapper, IGasHandler, IHeatTransfer, IConfigurable {

    public boolean fluidEject;

    public TileEntityReactorPort() {
        super(GeneratorsBlock.REACTOR_PORT);
    }

    @Override
    public void readFromNBT(CompoundNBT nbtTags) {
        super.readFromNBT(nbtTags);
        fluidEject = nbtTags.getBoolean("fluidEject");
    }

    @Nonnull
    @Override
    public CompoundNBT writeToNBT(CompoundNBT nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setBoolean("fluidEject", fluidEject);
        return nbtTags;
    }

    @Override
    public boolean isFrame() {
        return false;
    }

    @Override
    public void onUpdate() {
        if (changed) {
            world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
        }

        super.onUpdate();
        if (!world.isRemote) {
            CableUtils.emit(this);
            if (fluidEject && getReactor() != null && getReactor().getSteamTank().getFluid() != null) {
                IFluidTank tank = getReactor().getSteamTank();
                EmitUtils.forEachSide(getWorld(), getPos(), EnumSet.allOf(Direction.class), (tile, side) -> {
                    if (!(tile instanceof TileEntityReactorPort)) {
                        IFluidHandler handler = CapabilityUtils.getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
                        if (handler != null && PipeUtils.canFill(handler, tank.getFluid())) {
                            tank.drain(handler.fill(tank.getFluid(), true), true);
                        }
                    }
                });
            }
        }
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, boolean doFill) {
        return getReactor().getWaterTank().fill(resource, doFill);
    }

    @Override
    @Nullable
    public FluidStack drain(Direction from, int maxDrain, boolean doDrain) {
        return getReactor().getSteamTank().drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        return getReactor() != null && !fluidEject && fluid.getFluid() == FluidRegistry.WATER;
    }

    @Override
    public boolean canDrain(Direction from, @Nullable FluidStack fluid) {
        return getReactor() != null && (fluid == null || fluid.getFluid() == FluidRegistry.getFluid("steam"));
    }

    @Override
    public FluidTankInfo[] getTankInfo(Direction from) {
        if (getReactor() == null) {
            return PipeUtils.EMPTY;
        }
        return new FluidTankInfo[]{getReactor().getWaterTank().getInfo(), getReactor().getSteamTank().getInfo()};
    }

    @Override
    public FluidTankInfo[] getAllTanks() {
        return getTankInfo(null);
    }

    @Override
    public int receiveGas(Direction side, GasStack stack, boolean doTransfer) {
        if (getReactor() != null) {
            if (stack.getGas() == MekanismFluids.Deuterium) {
                return getReactor().getDeuteriumTank().receive(stack, doTransfer);
            } else if (stack.getGas() == MekanismFluids.Tritium) {
                return getReactor().getTritiumTank().receive(stack, doTransfer);
            } else if (stack.getGas() == MekanismFluids.FusionFuel) {
                return getReactor().getFuelTank().receive(stack, doTransfer);
            }
        }
        return 0;
    }

    @Override
    public GasStack drawGas(Direction side, int amount, boolean doTransfer) {
        return null;
    }

    @Override
    public boolean canReceiveGas(Direction side, Gas type) {
        return type == MekanismFluids.Deuterium || type == MekanismFluids.Tritium || type == MekanismFluids.FusionFuel;
    }

    @Override
    public boolean canDrawGas(Direction side, Gas type) {
        return false;
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return getReactor() != null ? new GasTankInfo[]{getReactor().getDeuteriumTank(), getReactor().getTritiumTank(), getReactor().getFuelTank()} : IGasHandler.NONE;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        }
        return capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == Capabilities.HEAT_TRANSFER_CAPABILITY ||
               capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == Capabilities.CONFIGURABLE_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        }
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == Capabilities.HEAT_TRANSFER_CAPABILITY || capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return (T) this;
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new FluidHandlerWrapper(this, side));
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

    @Override
    public IHeatTransfer getAdjacent(Direction side) {
        TileEntity adj = Coord4D.get(this).offset(side).getTileEntity(world);
        if (CapabilityUtils.hasCapability(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite())) {
            if (!(adj instanceof TileEntityReactorBlock)) {
                return CapabilityUtils.getCapability(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite());
            }
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
            return hohlraum.getGas(itemstack) != null && hohlraum.getGas(itemstack).amount == hohlraum.getMaxGas(itemstack);
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        if (getReactor() != null && getReactor().isFormed() && itemstack.getItem() instanceof ItemHohlraum) {
            ItemHohlraum hohlraum = (ItemHohlraum) itemstack.getItem();
            return hohlraum.getGas(itemstack) == null;
        }
        return false;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
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
    public EnumActionResult onSneakRightClick(PlayerEntity player, Direction side) {
        if (!world.isRemote) {
            fluidEject = !fluidEject;
            String modeText = " " + (fluidEject ? EnumColor.DARK_RED : EnumColor.DARK_GREEN) + LangUtils.transOutputInput(fluidEject) + ".";
            player.sendMessage(new TextComponentString(EnumColor.DARK_BLUE + Mekanism.LOG_TAG + " " + EnumColor.GREY +
                                                       LangUtils.localize("tooltip.configurator.reactorPortEject") + modeText));
            Mekanism.packetHandler.sendUpdatePacket(this);
            markDirty();
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public EnumActionResult onRightClick(PlayerEntity player, Direction side) {
        return EnumActionResult.PASS;
    }
}