package mekanism.common.upgrade.transmitter;

import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import mekanism.common.transmitters.TransporterImpl;
import mekanism.common.util.TransporterUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

public class LogisticalTransporterUpgradeData extends TransmitterUpgradeData {

    public final CompoundNBT nbt;

    public LogisticalTransporterUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes, TransporterImpl transmitter) {
        super(redstoneReactive, connectionTypes);
        this.nbt = new CompoundNBT();
        if (transmitter.getColor() != null) {
            this.nbt.putInt("color", TransporterUtils.colors.indexOf(transmitter.getColor()));
        }
        ListNBT stacks = new ListNBT();
        for (TransporterStack stack : transmitter.getTransit()) {
            CompoundNBT tagCompound = new CompoundNBT();
            stack.write(tagCompound);
            stacks.add(tagCompound);
        }
        if (!stacks.isEmpty()) {
            this.nbt.put("stacks", stacks);
        }
    }
}