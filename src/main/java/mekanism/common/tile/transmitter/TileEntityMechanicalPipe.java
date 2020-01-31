package mekanism.common.tile.transmitter;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.transmitter.BlockMechanicalPipe;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.PipeTier;
import mekanism.common.transmitters.grid.FluidNetwork;
import mekanism.common.upgrade.transmitter.MechanicalPipeUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class TileEntityMechanicalPipe extends TileEntityTransmitter<IFluidHandler, FluidNetwork, FluidStack> implements IFluidHandlerWrapper {

    public final PipeTier tier;

    public float currentScale;

    public FluidTank buffer;

    @Nonnull
    private FluidStack lastWrite = FluidStack.EMPTY;
    public CapabilityWrapperManager<IFluidHandlerWrapper, FluidHandlerWrapper> manager = new CapabilityWrapperManager<>(IFluidHandlerWrapper.class, FluidHandlerWrapper.class);

    public TileEntityMechanicalPipe(IBlockProvider blockProvider) {
        super(((IHasTileEntity<TileEntityMechanicalPipe>) blockProvider.getBlock()).getTileType());
        this.tier = ((BlockMechanicalPipe) blockProvider.getBlock()).getTier();
        buffer = new FluidTank(getCapacity());
    }

    @Override
    public void tick() {
        if (!isRemote()) {
            updateShare();
            List<Direction> connections = getConnections(ConnectionType.PULL);
            if (!connections.isEmpty()) {
                IFluidHandler[] connectedAcceptors = PipeUtils.getConnectedAcceptors(getPos(), getWorld());
                for (Direction side : connections) {
                    IFluidHandler container = connectedAcceptors[side.ordinal()];
                    if (container != null) {
                        FluidStack received;
                        //Note: We recheck the buffer each time in case we ended up accepting fluid somewhere
                        // and our buffer changed and is no longer empty
                        FluidStack bufferWithFallback = getBufferWithFallback();
                        if (bufferWithFallback.isEmpty()) {
                            //If we don't have a fluid stored try pulling as much as we are able to
                            received = container.drain(getAvailablePull(), FluidAction.SIMULATE);
                        } else {
                            //Otherwise try draining the same type of fluid we have stored requesting up to as much as we are able to pull
                            // We do this to better support multiple tanks in case the fluid we have stored we could pull out of a block's
                            // second tank but just asking to drain a specific amount
                            received = container.drain(new FluidStack(bufferWithFallback, getAvailablePull()), FluidAction.SIMULATE);
                        }
                        if (received.getAmount() > 0 && takeFluid(received, FluidAction.SIMULATE) == received.getAmount()) {
                            container.drain(takeFluid(received, FluidAction.EXECUTE), FluidAction.EXECUTE);
                        }
                    }
                }
            }
        }
        super.tick();
    }

    @Override
    public void updateShare() {
        if (getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetworkSize() > 0) {
            FluidStack last = getSaveShare();
            if ((!last.isEmpty() && !(!lastWrite.isEmpty() && lastWrite.getAmount() == last.getAmount() && lastWrite.getFluid() == last.getFluid())) ||
                (last.isEmpty() && !lastWrite.isEmpty())) {
                lastWrite = last;
                markDirty();
            }
        }
    }

    @Nonnull
    private FluidStack getSaveShare() {
        FluidNetwork transmitterNetwork = getTransmitter().getTransmitterNetwork();
        if (getTransmitter().hasTransmitterNetwork() && !transmitterNetwork.buffer.isEmpty()) {
            int remain = transmitterNetwork.buffer.getAmount() % transmitterNetwork.transmittersSize();
            int toSave = transmitterNetwork.buffer.getAmount() / transmitterNetwork.transmittersSize();
            if (transmitterNetwork.firstTransmitter().equals(getTransmitter())) {
                toSave += remain;
            }
            return PipeUtils.copy(transmitterNetwork.buffer, toSave);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public void onChunkUnloaded() {
        if (!isRemote() && getTransmitter().hasTransmitterNetwork() && !lastWrite.isEmpty()) {
            FluidStack buffer = getTransmitter().getTransmitterNetwork().buffer;
            if (!buffer.isEmpty()) {
                buffer.setAmount(buffer.getAmount() - lastWrite.getAmount());
                if (buffer.isEmpty()) {
                    //TODO: Evaluate
                    getTransmitter().getTransmitterNetwork().buffer = FluidStack.EMPTY;
                }
            }
        }
        super.onChunkUnloaded();
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        buffer.setCapacity(getCapacity());
        if (nbtTags.contains("cacheFluid")) {
            buffer.setFluid(FluidStack.loadFluidStackFromNBT(nbtTags.getCompound("cacheFluid")));
        } else {
            buffer.setFluid(FluidStack.EMPTY);
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (lastWrite.getAmount() > 0) {
            nbtTags.put("cacheFluid", lastWrite.writeToNBT(new CompoundNBT()));
        } else {
            nbtTags.remove("cacheFluid");
        }
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
    public boolean isValidAcceptor(TileEntity acceptor, Direction side) {
        return PipeUtils.isValidAcceptorOnSide(acceptor, side);
    }

    @Override
    public boolean isValidTransmitter(TileEntity tile) {
        if (!super.isValidTransmitter(tile)) {
            return false;
        }
        if (!(tile instanceof TileEntityMechanicalPipe)) {
            return true;
        }
        FluidStack buffer = getBufferWithFallback();
        FluidStack otherBuffer = ((TileEntityMechanicalPipe) tile).getBufferWithFallback();
        return buffer.isEmpty() || otherBuffer.isEmpty() || buffer.isFluidEqual(otherBuffer);
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
    protected boolean canHaveIncompatibleNetworks() {
        return true;
    }

    @Override
    public int getCapacity() {
        return tier.getPipeCapacity();
    }

    @Nonnull
    @Override
    public FluidStack getBuffer() {
        return buffer == null ? FluidStack.EMPTY : buffer.getFluid();
    }

    @Nonnull
    @Override
    public FluidStack getBufferWithFallback() {
        FluidStack buffer = getBuffer();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer.isEmpty() && getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().getBuffer();
        }
        return buffer;
    }

    @Override
    public void takeShare() {
        if (getTransmitter().hasTransmitterNetwork()) {
            FluidNetwork network = getTransmitter().getTransmitterNetwork();
            if (!network.buffer.isEmpty() && !lastWrite.isEmpty()) {
                network.buffer.shrink(lastWrite.getAmount());
                buffer.setFluid(lastWrite);
            }
        }
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        return takeFluid(resource, fluidAction);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        return getConnectionType(from) == ConnectionType.NORMAL;
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        if (from != null && getConnectionType(from) != ConnectionType.NONE) {
            //Our buffer or the network's buffer if we have a network
            return getAllTanks();
        }
        return PipeUtils.EMPTY;
    }

    @Override
    public IFluidTank[] getAllTanks() {
        if (getTransmitter().hasTransmitterNetwork()) {
            FluidNetwork network = getTransmitter().getTransmitterNetwork();
            //TODO: Have FluidNetwork actually have a tank?
            FluidTank info = new FluidTank(network.getCapacity());
            info.setFluid(network.getBuffer());
            return new IFluidTank[]{info};
        }
        return new IFluidTank[]{buffer};
    }

    public int getPullAmount() {
        return tier.getPipePullAmount();
    }

    @Override
    public IFluidHandler getCachedAcceptor(Direction side) {
        return MekanismUtils.toOptional(CapabilityUtils.getCapability(getCachedTile(side), CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite())).orElse(null);
    }

    public int getAvailablePull() {
        if (getTransmitter().hasTransmitterNetwork()) {
            return Math.min(getPullAmount(), getTransmitter().getTransmitterNetwork().getFluidNeeded());
        }
        return Math.min(getPullAmount(), buffer.getCapacity() - buffer.getFluidAmount());
    }

    public int takeFluid(@Nonnull FluidStack fluid, FluidAction fluidAction) {
        if (getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().emit(fluid, fluidAction);
        }
        return buffer.fill(fluid, fluidAction);
    }

    @Override
    protected boolean canUpgrade(AlloyTier alloyTier) {
        return alloyTier.getBaseTier().ordinal() == tier.getBaseTier().ordinal() + 1;
    }

    @Nonnull
    @Override
    protected BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
        switch (tier) {
            case BASIC:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.BASIC_MECHANICAL_PIPE.getBlock().getDefaultState());
            case ADVANCED:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ADVANCED_MECHANICAL_PIPE.getBlock().getDefaultState());
            case ELITE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ELITE_MECHANICAL_PIPE.getBlock().getDefaultState());
            case ULTIMATE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ULTIMATE_MECHANICAL_PIPE.getBlock().getDefaultState());
        }
        return current;
    }

    @Nullable
    @Override
    protected MechanicalPipeUpgradeData getUpgradeData() {
        return new MechanicalPipeUpgradeData(redstoneReactive, connectionTypes, getBuffer());
    }

    @Override
    protected void parseUpgradeData(@Nonnull TransmitterUpgradeData upgradeData) {
        if (upgradeData instanceof MechanicalPipeUpgradeData) {
            MechanicalPipeUpgradeData data = (MechanicalPipeUpgradeData) upgradeData;
            redstoneReactive = data.redstoneReactive;
            connectionTypes = data.connectionTypes;
            takeFluid(data.contents, FluidAction.EXECUTE);
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> manager.getWrapper(this, side)));
        }
        return super.getCapability(capability, side);
    }
}