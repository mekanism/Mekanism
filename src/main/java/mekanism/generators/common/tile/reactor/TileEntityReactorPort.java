package mekanism.generators.common.tile.reactor;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.IHeatTransfer;
import mekanism.api.Range4D;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.Mekanism;
import mekanism.common.MekanismFluids;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.generators.common.item.ItemHohlraum;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityReactorPort extends TileEntityReactorBlock implements IFluidHandlerWrapper, IGasHandler,
      ITubeConnection, IHeatTransfer, IConfigurable {

    public boolean fluidEject;

    public TileEntityReactorPort() {
        super("name", 1);

        inventory = NonNullList.withSize(0, ItemStack.EMPTY);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        fluidEject = nbtTags.getBoolean("fluidEject");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
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

                for (EnumFacing side : EnumFacing.values()) {
                    TileEntity tile = Coord4D.get(this).offset(side).getTileEntity(world);

                    if (tile != null && !(tile instanceof TileEntityReactorPort) && CapabilityUtils
                          .hasCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite())) {
                        IFluidHandler handler = CapabilityUtils
                              .getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());

                        if (PipeUtils.canFill(handler, tank.getFluid())) {
                            tank.drain(handler.fill(tank.getFluid(), true), true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int fill(EnumFacing from, @Nullable FluidStack resource, boolean doFill) {
        if (resource != null && resource.getFluid() == FluidRegistry.WATER && getReactor() != null && !fluidEject) {
            return getReactor().getWaterTank().fill(resource, doFill);
        }

        return 0;
    }

    @Override
    public FluidStack drain(EnumFacing from, @Nullable FluidStack resource, boolean doDrain) {
        if (resource != null && resource.getFluid() == FluidRegistry.getFluid("steam") && getReactor() != null) {
            getReactor().getSteamTank().drain(resource.amount, doDrain);
        }

        return null;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        if (getReactor() != null) {
            return getReactor().getSteamTank().drain(maxDrain, doDrain);
        }

        return null;
    }

    @Override
    public boolean canFill(EnumFacing from, @Nullable FluidStack fluid) {
        return getReactor() != null && fluid != null && fluid.getFluid().equals(FluidRegistry.WATER) && !fluidEject;
    }

    @Override
    public boolean canDrain(EnumFacing from, @Nullable FluidStack fluid) {
        return getReactor() != null && fluid != null && fluid.getFluid() == FluidRegistry.getFluid("steam");
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
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
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
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
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        return null;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return (type == MekanismFluids.Deuterium || type == MekanismFluids.Tritium
              || type == MekanismFluids.FusionFuel);
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return false;
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return getReactor() != null ? new GasTankInfo[]{getReactor().getDeuteriumTank(), getReactor().getTritiumTank(),
              getReactor().getFuelTank()} : IGasHandler.NONE;
    }

    @Override
    public boolean canTubeConnect(EnumFacing side) {
        return getReactor() != null;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.GAS_HANDLER_CAPABILITY
              || capability == Capabilities.TUBE_CONNECTION_CAPABILITY
              || capability == Capabilities.HEAT_TRANSFER_CAPABILITY
              || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
              || capability == Capabilities.CONFIGURABLE_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == Capabilities.TUBE_CONNECTION_CAPABILITY
              || capability == Capabilities.HEAT_TRANSFER_CAPABILITY
              || capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return (T) this;
        }

        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) new FluidHandlerWrapper(this, side);
        }

        return super.getCapability(capability, side);
    }

    @Override
    public boolean canOutputEnergy(EnumFacing side) {
        return true;
    }

    @Override
    public double getEnergy() {
        if (getReactor() == null) {
            return 0;
        } else {
            return getReactor().getBufferedEnergy();
        }
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
        } else {
            return getReactor().getBufferSize();
        }
    }

    @Override
    public boolean sideIsOutput(EnumFacing side) {
        return true;
    }

    @Override
    public boolean sideIsConsumer(EnumFacing side) {
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
    public double getInsulationCoefficient(EnumFacing side) {
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
    public boolean canConnectHeat(EnumFacing side) {
        return getReactor() != null;
    }

    @Override
    public IHeatTransfer getAdjacent(EnumFacing side) {
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
        return getReactor() != null && getReactor().isFormed() ? getReactor().getInventory().get(slotID)
              : ItemStack.EMPTY;
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
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return getReactor() != null && getReactor().isFormed() ? new int[]{0} : InventoryUtils.EMPTY;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (getReactor() != null && getReactor().isFormed() && itemstack.getItem() instanceof ItemHohlraum) {
            ItemHohlraum hohlraum = (ItemHohlraum) itemstack.getItem();

            return hohlraum.getGas(itemstack) != null && hohlraum.getGas(itemstack).amount == hohlraum
                  .getMaxGas(itemstack);
        }

        return false;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
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
    public EnumActionResult onSneakRightClick(EntityPlayer player, EnumFacing side) {
        if (!world.isRemote) {
            fluidEject = !fluidEject;
            String modeText = " " + (fluidEject ? EnumColor.DARK_RED : EnumColor.DARK_GREEN) + LangUtils
                  .transOutputInput(fluidEject) + ".";
            player.sendMessage(
                  new TextComponentString(EnumColor.DARK_BLUE + Mekanism.LOG_TAG + " " + EnumColor.GREY + LangUtils
                        .localize("tooltip.configurator.reactorPortEject") + modeText));
            Mekanism.packetHandler
                  .sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                        new Range4D(Coord4D.get(this)));
            markDirty();
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public EnumActionResult onRightClick(EntityPlayer player, EnumFacing side) {
        return EnumActionResult.PASS;
    }
}
