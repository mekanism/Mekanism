package mekanism.common.tile.transmitter;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.resolver.manager.ResourceHandlerManager;
import mekanism.common.content.network.transmitter.BufferedResourceTransmitter;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.DynamicBufferedResourceNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityResourceTransmitter<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>,
      NETWORK extends DynamicBufferedResourceNetwork<RESOURCE, CONTAINER, NETWORK, TRANSMITTER>,
      TRANSMITTER extends BufferedResourceTransmitter<RESOURCE, CONTAINER, NETWORK, TRANSMITTER>> extends TileEntityTransmitter implements IComputerTile {

    private final BlockCapability<ResourceHandler<RESOURCE>, @Nullable Direction> capability;

    public TileEntityResourceTransmitter(Holder<Block> blockProvider, BlockPos pos, BlockState state, MultiTypeCapability<ResourceHandler<RESOURCE>> capability) {
        super(blockProvider, pos, state);
        this.capability = capability.block();
        addCapabilityResolver(new ResourceHandlerManager<>(capability, new IContainerHolder<CONTAINER>() {
            @Override
            public @NotNull List<CONTAINER> getContainers(@Nullable Direction direction) {
                TRANSMITTER transmitter = TileEntityResourceTransmitter.this.getTransmitter();
                if (direction != null && (transmitter.getConnectionTypeRaw(direction) == ConnectionType.NONE) || transmitter.isRedstoneActivated()) {
                    //If we actually have a side, and our connection type on that side is none, or we are currently activated by redstone,
                    // then return that we have no tanks
                    return Collections.emptyList();
                }
                return transmitter.getContainers();
            }

            @Override
            public boolean canInsert(@Nullable Direction direction) {
                return TileEntityResourceTransmitter.this.canInsert(direction);
            }

            @Override
            public boolean canExtract(@Nullable Direction direction) {
                return TileEntityResourceTransmitter.this.canExtract(direction);
            }
        }));
    }

    @Override
    protected abstract TRANSMITTER createTransmitter(Holder<Block> blockProvider);

    @Override
    public TRANSMITTER getTransmitter() {
        return (TRANSMITTER) super.getTransmitter();
    }

    protected abstract Codec<RESOURCE> resourceCodec();

    @Override
    protected void onUpdateServer() {
        getTransmitter().pullFromAcceptors();
        super.onUpdateServer();
    }

    @Override
    public void sideChanged(@NotNull Direction side, @NotNull ConnectionType old, @NotNull ConnectionType type) {
        super.sideChanged(side, old, type);
        if (type == ConnectionType.NONE) {
            //We no longer have a capability, invalidate it, which will also notify the level
            invalidateCapability(capability, side);
        } else if (old == ConnectionType.NONE) {
            //Notify any listeners to our position that we now do have a capability
            //Note: We don't invalidate our impls because we know they are already invalid, so we can short circuit setting them to null from null
            invalidateCapabilities();
        }
    }

    @Override
    public void redstoneChanged(boolean powered) {
        super.redstoneChanged(powered);
        if (powered) {
            //The transmitter now is powered by redstone and previously was not
            //Note: While at first glance the below invalidation may seem over aggressive, it is not actually that aggressive as
            // if a cap has not been initialized yet on a side then invalidating it will just NO-OP
            invalidateCapabilityAll(capability);
        } else {
            //Notify any listeners to our position that we now do have a capability
            //Note: We don't invalidate our impls because we know they are already invalid, so we can short circuit setting them to null from null
            invalidateCapabilities();
        }
    }

    @Override
    protected void writeUpdatedTag(@NotNull ValueOutput output) {
        //Note: We add the stored information to the initial update tag and not to the one we sync on side changes which uses getReducedUpdateTag
        super.writeUpdatedTag(output);
        if (getTransmitter().hasTransmitterNetwork()) {
            NETWORK network = getTransmitter().getTransmitterNetwork();
            if (!network.getLastType().isEmpty()) {
                output.store(SerializationConstants.STORED, resourceCodec(), network.getLastType());
            }
            output.putFloat(SerializationConstants.SCALE, network.currentScale);
        }
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    LargeResourceStack<RESOURCE> getBuffer() {
        return getTransmitter().getBufferWithFallback();
    }

    @ComputerMethod
    long getCapacity() {
        BufferedResourceTransmitter<RESOURCE, CONTAINER, ?, ?> transmitter = getTransmitter();
        return transmitter.hasTransmitterNetwork() ? transmitter.getTransmitterNetwork().getCapacity() : transmitter.getCapacity();
    }

    @ComputerMethod
    long getNeeded() {
        return getCapacity() - getBuffer().amount();
    }

    @ComputerMethod
    double getFilledPercentage() {
        return getBuffer().amount() / (double) getCapacity();
    }
    //End methods IComputerTile
}