package mekanism.fabric;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric bootstrap entry point. Phase 0: proves the toolchain boots. Phase 1 moves
 * {@code mekanism.common.Mekanism}'s loader-neutral init here (see PORTING.md).
 */
public class MekanismFabric implements ModInitializer {

    public static final String MODID = "mekanism";
    public static final Logger LOGGER = LoggerFactory.getLogger("Mekanism");

    @Override
    public void onInitialize() {
        LOGGER.info("Mekanism Fabric bootstrap loaded (Phase 0 - no content yet)");
    }
}
