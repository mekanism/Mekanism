package mekanism.common.lib.security;

import mekanism.common.lib.security.ISecurityTile.SecurityMode;
import net.minecraft.network.PacketBuffer;

public class SecurityData {

    public SecurityMode mode = SecurityMode.PUBLIC;
    public boolean override;

    public SecurityData() {
    }

    public SecurityData(SecurityFrequency frequency) {
        mode = frequency.getSecurityMode();
        override = frequency.isOverridden();
    }

    public static SecurityData read(PacketBuffer dataStream) {
        SecurityData data = new SecurityData();
        data.mode = dataStream.readEnumValue(SecurityMode.class);
        data.override = dataStream.readBoolean();
        return data;
    }

    public void write(PacketBuffer dataStream) {
        dataStream.writeEnumValue(mode);
        dataStream.writeBoolean(override);
    }
}