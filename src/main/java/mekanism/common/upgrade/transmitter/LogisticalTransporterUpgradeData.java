package mekanism.common.upgrade.transmitter;

import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import mekanism.common.transmitters.TransporterImpl;
import net.minecraft.nbt.CompoundNBT;

public class LogisticalTransporterUpgradeData extends TransmitterUpgradeData {

    public final CompoundNBT nbt;

    public LogisticalTransporterUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes, TransporterImpl transmitter) {
        super(redstoneReactive, connectionTypes);
        transmitter.writeToNBT(this.nbt = new CompoundNBT());
    }
}