package mekanism.common.security;

import mekanism.common.security.ISecurityTile.SecurityMode;
import net.minecraft.network.PacketBuffer;

public class SecurityData {

    public SecurityMode mode = SecurityMode.PUBLIC;
    public boolean override;

    public SecurityData() {
    }

    public SecurityData(SecurityFrequency frequency) {
        mode = frequency.securityMode;
        override = frequency.override;
    }

    public static SecurityData read(PacketBuffer dataStream) {
        SecurityData data = new SecurityData();
        data.mode = SecurityMode.values()[dataStream.readInt()];
        data.override = dataStream.readBoolean();
        return data;
    }

    public void write(PacketBuffer dataStream) {
        dataStream.writeInt(mode.ordinal());
        dataStream.writeBoolean(override);
    }
}