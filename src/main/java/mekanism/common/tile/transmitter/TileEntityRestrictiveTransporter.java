package mekanism.common.tile.transmitter;

import mekanism.api.tier.AlloyTier;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;

public class TileEntityRestrictiveTransporter extends TileEntityLogisticalTransporter {

    public TileEntityRestrictiveTransporter() {
        super(MekanismBlocks.RESTRICTIVE_TRANSPORTER);
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.RESTRICTIVE_TRANSPORTER;
    }

    @Override
    public double getCost() {
        return 1000;
    }

    @Override
    protected ActionResultType onConfigure(PlayerEntity player, int part, Direction side) {
        return ActionResultType.PASS;
    }

    @Override
    protected boolean canUpgrade(AlloyTier tier) {
        return false;
    }
}