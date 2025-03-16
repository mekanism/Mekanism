package mekanism.common.tile.transmitter;

import java.util.Collections;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.MathUtils;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.energy.DynamicStrictEnergyHandler;
import mekanism.common.capabilities.resolver.manager.EnergyHandlerManager;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityUniversalCable extends TileEntityTransmitter implements IComputerTile {

    private final EnergyHandlerManager energyHandlerManager;

    public TileEntityUniversalCable(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        addCapabilityResolver(energyHandlerManager = new EnergyHandlerManager(direction -> {
            UniversalCable cable = getTransmitter();
            if (direction != null && (cable.getConnectionTypeRaw(direction) == ConnectionType.NONE) || cable.isRedstoneActivated()) {
                //If we actually have a side, and our connection type on that side is none, or we are currently activated by redstone,
                // then return that we have no containers
                return Collections.emptyList();
            }
            return cable.getEnergyContainers(direction);
        }, new DynamicStrictEnergyHandler(this::getEnergyContainers, getExtractPredicate(), getInsertPredicate(), null)));
    }

    @Override
    protected UniversalCable createTransmitter(Holder<Block> blockProvider) {
        return new UniversalCable(blockProvider, this);
    }

    @Override
    public UniversalCable getTransmitter() {
        return (UniversalCable) super.getTransmitter();
    }

    @Override
    protected void onUpdateServer() {
        getTransmitter().pullFromAcceptors();
        super.onUpdateServer();
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.UNIVERSAL_CABLE;
    }

    @NotNull
    @Override
    protected BlockState upgradeResult(@NotNull BlockState current, @NotNull BaseTier tier) {
        return BlockStateHelper.copyStateData(current, switch (tier) {
            case BASIC -> MekanismBlocks.BASIC_UNIVERSAL_CABLE;
            case ADVANCED -> MekanismBlocks.ADVANCED_UNIVERSAL_CABLE;
            case ELITE -> MekanismBlocks.ELITE_UNIVERSAL_CABLE;
            case ULTIMATE -> MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE;
            default -> null;
        });
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider) {
        //Note: We add the stored information to the initial update tag and not to the one we sync on side changes which uses getReducedUpdateTag
        CompoundTag updateTag = super.getUpdateTag(provider);
        if (getTransmitter().hasTransmitterNetwork()) {
            EnergyNetwork network = getTransmitter().getTransmitterNetwork();
            updateTag.putLong(SerializationConstants.ENERGY, network.energyContainer.getEnergy());
            updateTag.putFloat(SerializationConstants.SCALE, network.currentScale);
        }
        return updateTag;
    }

    private List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return energyHandlerManager.getContainers(side);
    }

    @Override
    public void sideChanged(@NotNull Direction side, @NotNull ConnectionType old, @NotNull ConnectionType type) {
        super.sideChanged(side, old, type);
        if (type == ConnectionType.NONE) {
            //We no longer have a capability, invalidate it, which will also notify the level
            invalidateCapabilities(EnergyCompatUtils.getLoadedEnergyCapabilities(), side);
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
            invalidateCapabilitiesAll(EnergyCompatUtils.getLoadedEnergyCapabilities());
        } else {
            //Notify any listeners to our position that we now do have a capability
            //Note: We don't invalidate our impls because we know they are already invalid, so we can short circuit setting them to null from null
            invalidateCapabilities();
        }
    }

    //Methods relating to IComputerTile
    @Override
    public String getComputerName() {
        return getTransmitter().getTier().getBaseTier().getLowerName() + "UniversalCable";
    }

    @ComputerMethod
    long getBuffer() {
        return getTransmitter().getBufferWithFallback();
    }

    @ComputerMethod
    long getCapacity() {
        UniversalCable cable = getTransmitter();
        return cable.hasTransmitterNetwork() ? cable.getTransmitterNetwork().getCapacity() : cable.getCapacity();
    }

    @ComputerMethod
    long getNeeded() {
        return getCapacity() - getBuffer();
    }

    @ComputerMethod
    double getFilledPercentage() {
        return MathUtils.divideToLevel(getBuffer(), getCapacity());
    }
    //End methods IComputerTile
}