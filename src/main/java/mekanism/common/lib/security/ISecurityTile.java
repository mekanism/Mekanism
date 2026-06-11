package mekanism.common.lib.security;

import java.util.UUID;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.tile.component.TileComponentSecurity;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public interface ISecurityTile extends ISecurityObject {

    @Nullable
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

    @Override
    default SecurityMode getSecurityMode() {
        TileComponentSecurity security = getSecurity();
        return security == null ? SecurityMode.PUBLIC : security.getMode();
    }

    @Override
    default void setSecurityMode(SecurityMode mode, @Nullable TransactionContext transaction) {
        TileComponentSecurity security = getSecurity();
        if (security != null) {
            security.setMode(mode);
        }
    }

    /// Called from [#setSecurityMode(SecurityMode, TransactionContext)] when the security mode changes.
    ///
    /// @param old  The old security mode.
    /// @param mode The new security mode.
    ///
    /// @apiNote It is on the implementer to call this method if it is useful to them.
    default void onSecurityChanged(SecurityMode old, SecurityMode mode) {
    }

    @Override
    default void setOwnerUUID(@Nullable UUID owner, @Nullable TransactionContext transaction) {
        TileComponentSecurity security = getSecurity();
        if (security != null) {
            security.setOwnerUUID(owner);
        }
    }
}