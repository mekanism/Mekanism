package mekanism.common.lib;

import net.minecraft.server.level.ServerPlayer;

public interface SupportsXpContainment {

    /// {@return if this lookup handler supports containing experience, or if the corresponding upgrade is installed}
    default boolean hasXpContainment() {
        return true;
    }

    void popExperience(ServerPlayer player);
}