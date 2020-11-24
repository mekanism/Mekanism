package mekanism.common.tile.transmitter;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.fluid.DynamicFluidHandler;
import mekanism.common.capabilities.resolver.manager.FluidHandlerManager;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntityMechanicalPipe extends TileEntityTransmitter {

    private final FluidHandlerManager fluidHandlerManager;

    public TileEntityMechanicalPipe(IBlockProvider blockProvider) {
        super(blockProvider);
        addCapabilityResolver(fluidHandlerManager = new FluidHandlerManager(direction -> {
            MechanicalPipe pipe = getTransmitter();
            if (direction != null && pipe.getConnectionTypeRaw(direction) == ConnectionType.NONE) {
                //If we actually have a side, and our connection type on that side is none, then return that we have no tanks
                return Collections.emptyList();
            }
            return pipe.getFluidTanks(direction);
        }, new DynamicFluidHandler(this::getFluidTanks, getExtractPredicate(), getInsertPredicate(), null)));
    }

    @Override
    protected MechanicalPipe createTransmitter(IBlockProvider blockProvider) {
        return new MechanicalPipe(blockProvider, this);
    }

    @Override
    public MechanicalPipe getTransmitter() {
        return (MechanicalPipe) super.getTransmitter();
    }

    @Override
    public void tick() {
        if (!isRemote()) {
            getTransmitter().pullFromAcceptors();
        }
        super.tick();
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.MECHANICAL_PIPE;
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

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        //Note: We add the stored information to the initial update tag and not to the one we sync on side changes which uses getReducedUpdateTag
        CompoundNBT updateTag = super.getUpdateTag();
        if (getTransmitter().hasTransmitterNetwork()) {
            FluidNetwork network = getTransmitter().getTransmitterNetwork();
            updateTag.put(NBTConstants.FLUID_STORED, network.lastFluid.writeToNBT(new CompoundNBT()));
            updateTag.putFloat(NBTConstants.SCALE, network.currentScale);
        }
        return updateTag;
    }

    private List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidHandlerManager.getContainers(side);
    }

    @Override
    public void sideChanged(@Nonnull Direction side, @Nonnull ConnectionType old, @Nonnull ConnectionType type) {
        super.sideChanged(side, old, type);
        if (type == ConnectionType.NONE) {
            invalidateCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
            //Notify the neighbor on that side our state changed and we no longer have a capability
            WorldUtils.notifyNeighborOfChange(world, side, pos);
        } else if (old == ConnectionType.NONE) {
            //Notify the neighbor on that side our state changed and we now do have a capability
            WorldUtils.notifyNeighborOfChange(world, side, pos);
        }
    }
}