package buildcraft.api.core;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraftforge.common.config.Property;

/** Use this to access the various config options. It is recommended that you use this as opposed to the variables in
 * the mod, as users may only install the modules that they want, and not the one you may have refereed to. */
@Deprecated
public final class ConfigAccessor {
    public enum EMod {
        CORE,
        BUILDERS,
        ENERGY,
        FACTORY,
        ROBITICS,
        SILICON,
        TRANSPORT
    }

    private static Map<EMod, IBuildCraftMod> mods = Maps.newHashMap();

    private ConfigAccessor() {}

    public static Property getOption(EMod mod, String name) {
        if (mods.containsKey(mod)) {
            return mods.get(mod).getOption(name);
        } else {
            return null;
        }
    }

    public static boolean getBoolean(EMod mod, String name, boolean defaultBoolean) {
        Property prop = getOption(mod, name);
        if (prop == null) {
            return defaultBoolean;
        } else {
            return prop.getBoolean(defaultBoolean);
        }
    }

    /** WARNING: INTERNAL USE ONLY! */
    public static void addMod(EMod mod, IBuildCraftMod actual) {
        mods.put(mod, actual);
    }
}
