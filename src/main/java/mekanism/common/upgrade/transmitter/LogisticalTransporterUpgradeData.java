package mekanism.common.upgrade.transmitter;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mekanism.api.text.EnumColor;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.transmitter.ConnectionType;
import org.jetbrains.annotations.Nullable;

public class LogisticalTransporterUpgradeData extends TransmitterUpgradeData {

    @Nullable
    public final EnumColor color;
    public final Int2ObjectMap<TransporterStack> transit;
    public final Int2ObjectMap<TransporterStack> needsSync;
    public final int nextId;
    public final int delay;
    public final int delayCount;

    //Note: Currently redstone reactive is always false here
    public LogisticalTransporterUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes, @Nullable EnumColor color, Int2ObjectMap<TransporterStack> transit,
          Int2ObjectMap<TransporterStack> needsSync, int nextId, int delay, int delayCount) {
        super(redstoneReactive, connectionTypes);
        this.color = color;
        this.transit = transit;
        this.needsSync = needsSync;
        this.nextId = nextId;
        this.delay = delay;
        this.delayCount = delayCount;
    }
}