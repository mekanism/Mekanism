package mekanism.common.lib.security;

import mekanism.api.security.SecurityMode;
import net.minecraft.network.FriendlyByteBuf;

public record SecurityData(SecurityMode mode, boolean override) {

    public static final SecurityData DUMMY = new SecurityData(SecurityMode.PUBLIC, false);

    public SecurityData(SecurityFrequency frequency) {
        this(frequency.getSecurityMode(), frequency.isOverridden());
    }

    public static SecurityData read(FriendlyByteBuf dataStream) {
        return new SecurityData(dataStream.readEnum(SecurityMode.class), dataStream.readBoolean());
    }

    public void write(FriendlyByteBuf dataStream) {
        dataStream.writeEnum(mode);
        dataStream.writeBoolean(override);
    }
}