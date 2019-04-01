package mekanism.common.tile.transmitter;

import io.netty.buffer.ByteBuf;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Tier;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.PipeTier;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.api.TileNetworkList;
import mekanism.common.block.states.BlockStateTransmitter.TransmitterType;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.transmitters.grid.FluidNetwork;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class TileEntityMechanicalPipe extends TileEntityTransmitter<IFluidHandler, FluidNetwork> implements
      IFluidHandlerWrapper {

    public Tier.PipeTier tier = Tier.PipeTier.BASIC;

    public float currentScale;

    public FluidTank buffer = new FluidTank(Fluid.BUCKET_VOLUME);

    public FluidStack lastWrite;
    public CapabilityWrapperManager<IFluidHandlerWrapper, FluidHandlerWrapper> manager = new CapabilityWrapperManager<>(
          IFluidHandlerWrapper.class, FluidHandlerWrapper.class);

    @Override
    public BaseTier getBaseTier() {
        return tier.getBaseTier();
    }

    @Override
    public void setBaseTier(BaseTier baseTier) {
        tier = Tier.PipeTier.get(baseTier);
        buffer.setCapacity(getCapacity());
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            updateShare();

            IFluidHandler[] connectedAcceptors = PipeUtils.getConnectedAcceptors(getPos(), getWorld());

            for (EnumFacing side : getConnections(ConnectionType.PULL)) {
                if (connectedAcceptors[side.ordinal()] != null) {
                    IFluidHandler container = connectedAcceptors[side.ordinal()];

                    if (container != null) {
                        FluidStack received = container.drain(getPullAmount(), false);

                        if (received != null && received.amount != 0) {
                            container.drain(takeFluid(received, true), true);
                        }
                    }
                }
            }
        }

        super.update();
    }

    @Override
    public void updateShare() {
        if (getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetworkSize() > 0) {
            FluidStack last = getSaveShare();

            if ((last != null && !(lastWrite != null && lastWrite.amount == last.amount && lastWrite.getFluid() == last
                  .getFluid())) || (last == null && lastWrite != null)) {
                lastWrite = last;
                markDirty();
            }
        }
    }

    private FluidStack getSaveShare() {
        if (getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetwork().buffer != null) {
            int remain = getTransmitter().getTransmitterNetwork().buffer.amount % getTransmitter()
                  .getTransmitterNetwork().transmitters.size();
            int toSave = getTransmitter().getTransmitterNetwork().buffer.amount / getTransmitter()
                  .getTransmitterNetwork().transmitters.size();

            if (getTransmitter().getTransmitterNetwork().transmitters.iterator().next().equals(getTransmitter())) {
                toSave += remain;
            }

            return PipeUtils.copy(getTransmitter().getTransmitterNetwork().buffer, toSave);
        }

        return null;
    }

    @Override
    public void onChunkUnload() {
        if (!getWorld().isRemote && getTransmitter().hasTransmitterNetwork()) {
            if (lastWrite != null && getTransmitter().getTransmitterNetwork().buffer != null) {
                getTransmitter().getTransmitterNetwork().buffer.amount -= lastWrite.amount;

                if (getTransmitter().getTransmitterNetwork().buffer.amount <= 0) {
                    getTransmitter().getTransmitterNetwork().buffer = null;
                }
            }
        }

        super.onChunkUnload();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        if (nbtTags.hasKey("tier")) {
            tier = Tier.PipeTier.values()[nbtTags.getInteger("tier")];
        }
        buffer.setCapacity(getCapacity());

        if (nbtTags.hasKey("cacheFluid")) {
            buffer.setFluid(FluidStack.loadFluidStackFromNBT(nbtTags.getCompoundTag("cacheFluid")));
        } else {
            buffer.setFluid(null);
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        if (lastWrite != null && lastWrite.amount > 0) {
            nbtTags.setTag("cacheFluid", lastWrite.writeToNBT(new NBTTagCompound()));
        } else {
            nbtTags.removeTag("cacheFluid");
        }

        nbtTags.setInteger("tier", tier.ordinal());

        return nbtTags;
    }

    @Override
    public TransmissionType getTransmissionType() {
        return TransmissionType.FLUID;
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.MECHANICAL_PIPE;
    }

    @Override
    public boolean isValidAcceptor(TileEntity acceptor, EnumFacing side) {
        return PipeUtils.isValidAcceptorOnSide(acceptor, side);
    }

    @Override
    public FluidNetwork createNewNetwork() {
        return new FluidNetwork();
    }

    @Override
    public FluidNetwork createNetworkByMerging(Collection<FluidNetwork> networks) {
        return new FluidNetwork(networks);
    }

    @Override
    public int getCapacity() {
        return tier.pipeCapacity;
    }

    @Override
    public FluidStack getBuffer() {
        return buffer == null ? null : buffer.getFluid();
    }

    @Override
    public void takeShare() {
        if (getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetwork().buffer != null
              && lastWrite != null) {
            getTransmitter().getTransmitterNetwork().buffer.amount -= lastWrite.amount;
            buffer.setFluid(lastWrite);
        }
    }

    @Override
    public int fill(EnumFacing from, @Nullable FluidStack resource, boolean doFill) {
        if (getConnectionType(from) == ConnectionType.NORMAL) {
            return takeFluid(resource, doFill);
        }

        return 0;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(EnumFacing from, @Nullable FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canFill(EnumFacing from, @Nullable FluidStack fluid) {
        return getConnectionType(from) == ConnectionType.NORMAL;
    }

    @Override
    public boolean canDrain(EnumFacing from, @Nullable FluidStack fluid) {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        if (from != null && getConnectionType(from) != ConnectionType.NONE) {
            return new FluidTankInfo[]{buffer.getInfo()};
        }

        return PipeUtils.EMPTY;
    }

    @Override
    public FluidTankInfo[] getAllTanks() {
        return new FluidTankInfo[]{buffer.getInfo()};
    }

    public int getPullAmount() {
        return tier.pipePullAmount;
    }

    @Override
    public IFluidHandler getCachedAcceptor(EnumFacing side) {
        TileEntity tile = getCachedTile(side);

        if (CapabilityUtils.hasCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite())) {
            return CapabilityUtils
                  .getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
        }

        return null;
    }

    public int takeFluid(FluidStack fluid, boolean doEmit) {
        if (getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().emit(fluid, doEmit);
        } else {
            return buffer.fill(fluid, doEmit);
        }
    }

    @Override
    public boolean upgrade(int tierOrdinal) {
        if (tier.ordinal() < BaseTier.ULTIMATE.ordinal() && tierOrdinal == tier.ordinal() + 1) {
            tier = PipeTier.values()[tier.ordinal() + 1];

            markDirtyTransmitters();
            sendDesc = true;

            return true;
        }

        return false;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) throws Exception {
        tier = PipeTier.values()[dataStream.readInt()];

        super.handlePacketData(dataStream);
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        data.add(tier.ordinal());

        super.getNetworkedData(data);

        return data;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(manager.getWrapper(this, side));
        }

        return super.getCapability(capability, side);
    }
}