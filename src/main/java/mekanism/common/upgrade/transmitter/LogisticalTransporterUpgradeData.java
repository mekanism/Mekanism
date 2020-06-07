package mekanism.common.upgrade.transmitter;

import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.lib.transmitter.ConnectionType;
import net.minecraft.nbt.CompoundNBT;

public class LogisticalTransporterUpgradeData extends TransmitterUpgradeData {

    public final CompoundNBT nbt;

    //Note: Currently redstone reactive is always false here
    public LogisticalTransporterUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes, TileEntityLogisticalTransporterBase transmitter) {
        super(redstoneReactive, connectionTypes);
        transmitter.writeToNBT(this.nbt = new CompoundNBT());
    }
}