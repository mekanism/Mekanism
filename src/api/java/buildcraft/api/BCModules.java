package buildcraft.api;

import java.util.Locale;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

import buildcraft.api.core.BCLog;

public enum BCModules {
    LIB,
    // Base module for all BC.
    CORE,
    // Potentially optional modules for adding more BC functionality
    BUILDERS,
    ENERGY,
    FACTORY,
    ROBOTICS,
    SILICON,
    TRANSPORT,
    // Optional module for compatibility with other mods
    COMPAT;

    public static final BCModules[] VALUES = values();

    private static final String MODID_START = "buildcraft";
    private final String modid, part;

    BCModules() {
        part = name().toLowerCase(Locale.ROOT);
        this.modid = MODID_START + part;
    }

    public static void fmlPreInit() {}

    public static boolean isBcMod(String modid) {
        if (!modid.startsWith(MODID_START)) return false;
        String post = modid.substring(MODID_START.length());
        for (BCModules module : VALUES) {
            if (post.equals(module.part)) {
                return true;
            }
        }
        return false;
    }

    public boolean isLoaded() {
        return Loader.isModLoaded(modid);
    }

    public String getModId() {
        return modid;
    }

    static {
        if (!Loader.instance().hasReachedState(LoaderState.CONSTRUCTING)) {
            throw new RuntimeException("Accessed BC modules too early! You can only use them from construction onwards!");
        }
        for (BCModules module : values()) {
            if (module.isLoaded()) {
                BCLog.logger.info("[api.modules] Module " + module.name().toLowerCase(Locale.ROOT) + " is loaded!");
            } else {
                BCLog.logger.warn("[api.modules] Module " + module.name().toLowerCase(Locale.ROOT) + " is NOT loaded!");
            }
        }
    }
}
