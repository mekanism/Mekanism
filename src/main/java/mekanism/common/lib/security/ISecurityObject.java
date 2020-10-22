package mekanism.common.lib.security;

import java.util.UUID;
import javax.annotation.Nullable;

public interface ISecurityObject {

    ISecurityObject NO_SECURITY = new ISecurityObject() {
        @Override
        public boolean hasSecurity() {
            return false;
        }

        @Nullable
        @Override
        public UUID getOwnerUUID() {
            return null;
        }

        @Nullable
        @Override
        public String getOwnerName() {
            return null;
        }

        @Override
        public SecurityMode getSecurityMode() {
            return SecurityMode.PUBLIC;
        }

        @Override
        public void setSecurityMode(SecurityMode mode) {
        }
    };

    default boolean hasSecurity() {
        return true;
    }

    @Nullable
    UUID getOwnerUUID();

    @Nullable
    String getOwnerName();

    SecurityMode getSecurityMode();

    void setSecurityMode(SecurityMode mode);

    default void onSecurityChanged(SecurityMode old, SecurityMode mode) {
    }
}