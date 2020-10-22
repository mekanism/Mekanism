package mekanism.common.lib.security;

import java.util.UUID;
import javax.annotation.Nullable;
import mekanism.common.tile.component.TileComponentSecurity;

public interface ISecurityTile extends ISecurityObject {

    TileComponentSecurity getSecurity();

    @Nullable
    @Override
    default UUID getOwnerUUID() {
        TileComponentSecurity security = getSecurity();
        return security == null ? null : security.getOwnerUUID();
    }

    @Nullable
    @Override
    default String getOwnerName() {
        TileComponentSecurity security = getSecurity();
        return security == null ? null : security.getOwnerName();
    }

    @Override
    default SecurityMode getSecurityMode() {
        TileComponentSecurity security = getSecurity();
        return security == null ? SecurityMode.PUBLIC : security.getMode();
    }

    @Override
    default void setSecurityMode(SecurityMode mode) {
        TileComponentSecurity security = getSecurity();
        if (security != null) {
            security.setMode(mode);
        }
    }
}