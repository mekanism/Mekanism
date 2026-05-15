package mekanism.common.tile.transmitter;

import java.util.Collections;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.resolver.manager.FluidHandlerManager;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityMechanicalPipe extends TileEntityTransmitter implements IComputerTile {

    public TileEntityMechanicalPipe(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        addCapabilityResolver(new FluidHandlerManager(new IFluidTankHolder() {
            @Override
            public @NotNull List<IFluidTank> getTanks(@Nullable Direction direction) {
                MechanicalPipe pipe = TileEntityMechanicalPipe.this.getTransmitter();
                if (direction != null && (pipe.getConnectionTypeRaw(direction) == ConnectionType.NONE) || pipe.isRedstoneActivated()) {
                    //If we actually have a side, and our connection type on that side is none, or we are currently activated by redstone,
                    // then return that we have no tanks
                    return Collections.emptyList();
                }
                return pipe.getContainers();
            }

            @Override
            public boolean canInsert(@Nullable Direction direction) {
                return TileEntityMechanicalPipe.this.canInsert(direction);
            }

            @Override
            public boolean canExtract(@Nullable Direction direction) {
                return TileEntityMechanicalPipe.this.canExtract(direction);
            }
        }, null));
    }

    @Override
    protected MechanicalPipe createTransmitter(Holder<Block> blockProvider) {
        return new MechanicalPipe(blockProvider, this);
    }

    @Override
    public MechanicalPipe getTransmitter() {
        return (MechanicalPipe) super.getTransmitter();
    }

    @Override
    protected void onUpdateServer() {
        getTransmitter().pullFromAcceptors();
        super.onUpdateServer();
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.MECHANICAL_PIPE;
    }

    @NotNull
    @Override
    protected BlockState upgradeResult(@NotNull BlockState current, @NotNull BaseTier tier) {
        return BlockStateHelper.copyStateData(current, switch (tier) {
            case BASIC -> MekanismBlocks.BASIC_MECHANICAL_PIPE;
            case ADVANCED -> MekanismBlocks.ADVANCED_MECHANICAL_PIPE;
            case ELITE -> MekanismBlocks.ELITE_MECHANICAL_PIPE;
            case ULTIMATE -> MekanismBlocks.ULTIMATE_MECHANICAL_PIPE;
            default -> null;
        });
    }

    @Override
    protected void writeUpdatedTag(@NotNull ValueOutput output) {
        //Note: We add the stored information to the initial update tag and not to the one we sync on side changes which uses getReducedUpdateTag
        super.writeUpdatedTag(output);
        if (getTransmitter().hasTransmitterNetwork()) {
            FluidNetwork network = getTransmitter().getTransmitterNetwork();
            if (!network.getLastType().isEmpty()) {
                output.store(SerializationConstants.FLUID, FluidResource.CODEC, network.getLastType());
            }
            output.putFloat(SerializationConstants.SCALE, network.currentScale);
        }
    }

    @Override
    public void sideChanged(@NotNull Direction side, @NotNull ConnectionType old, @NotNull ConnectionType type) {
        super.sideChanged(side, old, type);
        if (type == ConnectionType.NONE) {
            //We no longer have a capability, invalidate it, which will also notify the level
            invalidateCapability(Capabilities.FLUID.block(), side);
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
            invalidateCapabilityAll(Capabilities.FLUID.block());
        } else {
            //Notify any listeners to our position that we now do have a capability
            //Note: We don't invalidate our impls because we know they are already invalid, so we can short circuit setting them to null from null
            invalidateCapabilities();
        }
    }

    //Methods relating to IComputerTile
    @Override
    public String getComputerName() {
        return getTransmitter().getTier().getBaseTier().getLowerName() + "MechanicalPipe";
    }

    //@ComputerMethod//TODO - 26.1: Figure this out
    LargeResourceStack<FluidResource> getBuffer() {
        return getTransmitter().getBufferWithFallback();
    }

    @ComputerMethod
    long getCapacity() {
        MechanicalPipe pipe = getTransmitter();
        return pipe.hasTransmitterNetwork() ? pipe.getTransmitterNetwork().getCapacity() : pipe.getCapacity();
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