package mekanism.common.lib.security;

import java.util.UUID;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.tile.component.TileComponentSecurity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ISecurityTile extends ISecurityObject {

    TileComponentSecurity getSecurity();

    default boolean hasSecurity() {
        return true;
    }

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

    @NotNull
    @Override
    default SecurityMode getSecurityMode() {
        TileComponentSecurity security = getSecurity();
        return security == null ? SecurityMode.PUBLIC : security.getMode();
    }

    @Override
    default void setSecurityMode(@NotNull SecurityMode mode) {
        TileComponentSecurity security = getSecurity();
        if (security != null) {
            security.setMode(mode);
        }
    }

    @Override
    default void setOwnerUUID(@Nullable UUID owner) {
        TileComponentSecurity security = getSecurity();
        if (security != null) {
            security.setOwnerUUID(owner);
        }
    }
}