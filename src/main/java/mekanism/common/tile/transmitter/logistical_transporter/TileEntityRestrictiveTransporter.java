package mekanism.common.tile.transmitter.logistical_transporter;

import mekanism.common.MekanismBlock;
import mekanism.common.block.states.TransmitterType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;

public class TileEntityRestrictiveTransporter extends TileEntityLogisticalTransporter {

    public TileEntityRestrictiveTransporter() {
        super(MekanismBlock.RESTRICTIVE_TRANSPORTER);
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
    protected EnumActionResult onConfigure(PlayerEntity player, int part, Direction side) {
        return EnumActionResult.PASS;
    }
}