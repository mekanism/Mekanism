package mekanism.fabric_shim.common;

import net.minecraft.network.chat.Component;

/**
 * An enum with a translated display name (stand-in for NeoForge's TranslatableEnum; same surface).
 */
public interface TranslatableEnum {

    default Component getTranslatedName() {
        return Component.literal(((Enum<?>) this).name());
    }
}
