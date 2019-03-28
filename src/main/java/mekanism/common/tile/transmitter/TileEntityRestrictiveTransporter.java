package mekanism.common.tile.transmitter;

import mekanism.common.block.states.BlockStateTransmitter.TransmitterType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;

public class TileEntityRestrictiveTransporter extends TileEntityLogisticalTransporter {

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.RESTRICTIVE_TRANSPORTER;
    }

    @Override
    public double getCost() {
        return 1000;
    }

    @Override
    protected EnumActionResult onConfigure(EntityPlayer player, int part, EnumFacing side) {
        return EnumActionResult.PASS;
    }
}
