package mekanism.common.tile.transmitter;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.manager.HeatHandlerManager;
import mekanism.common.content.network.transmitter.ThermodynamicConductor;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public class TileEntityThermodynamicConductor extends TileEntityTransmitter {

    private final HeatHandlerManager heatHandlerManager;

    public TileEntityThermodynamicConductor(IBlockProvider blockProvider) {
        super(blockProvider);
        addCapabilityResolver(heatHandlerManager = new HeatHandlerManager(direction -> {
            ThermodynamicConductor conductor = getTransmitter();
            if (direction != null && conductor.getConnectionTypeRaw(direction) == ConnectionType.NONE) {
                //If we actually have a side, and our connection type on that side is none, then return that we have no capacitors
                return Collections.emptyList();
            }
            return conductor.getHeatCapacitors(direction);
        }, new IMekanismHeatHandler() {
            @Nonnull
            @Override
            public List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction side) {
                return heatHandlerManager.getContainers(side);
            }

            @Override
            public void onContentsChanged() {
            }
        }));
    }

    @Override
    protected ThermodynamicConductor createTransmitter(IBlockProvider blockProvider) {
        return new ThermodynamicConductor(blockProvider, this);
    }

    @Override
    public ThermodynamicConductor getTransmitter() {
        return (ThermodynamicConductor) super.getTransmitter();
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.THERMODYNAMIC_CONDUCTOR;
    }

    @Nonnull
    @Override
    protected BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
        switch (tier) {
            case BASIC:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR.getBlock().getDefaultState());
            case ADVANCED:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR.getBlock().getDefaultState());
            case ELITE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR.getBlock().getDefaultState());
            case ULTIMATE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR.getBlock().getDefaultState());
        }
        return current;
    }

    @Override
    public void sideChanged(@Nonnull Direction side, @Nonnull ConnectionType old, @Nonnull ConnectionType type) {
        super.sideChanged(side, old, type);
        if (type == ConnectionType.NONE) {
            invalidateCapability(Capabilities.HEAT_HANDLER_CAPABILITY, side);
            //Notify the neighbor on that side our state changed and we no longer have a capability
            WorldUtils.notifyNeighborOfChange(world, side, pos);
        } else if (old == ConnectionType.NONE) {
            //Notify the neighbor on that side our state changed and we now do have a capability
            WorldUtils.notifyNeighborOfChange(world, side, pos);
        }
    }
}