package buildcraft.api;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

import buildcraft.api.core.BCLog;

public class APIHelper {
    public static <T> T getInstance(String clsName, Class<T> baseVersion) {
        return getNamedInstance(clsName, "INSTANCE", baseVersion);
    }

    public static <T> T getInstance(String clsName, Class<T> baseVersion, T nullVersion) {
        return getNamedInstance(clsName, "INSTANCE", baseVersion, nullVersion);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getNamedInstance(String clsName, String enumName, Class<T> baseVersion) {
        T instance = getNamedInstance(clsName, enumName, baseVersion, null);
        if (instance != null) return instance;

        Class<?>[] types = baseVersion.getClasses();
        Class<?> voidType = null;
        for (Class<?> class1 : types) {
            if (baseVersion.isAssignableFrom(class1)) voidType = class1;
        }
        if (voidType != null) {
            Object[] arr = voidType.getEnumConstants();
            if (arr == null) return null;
            Enum<?>[] eArr = ((Class<Enum<?>>) voidType).getEnumConstants();
            for (Enum<?> e : eArr) {
                if (e.name().equals(enumName) && baseVersion.isInstance(e)) return (T) e;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getNamedInstance(String clsName, String enumName, Class<T> baseVersion, T nullVersion) {
        if (!Loader.instance().hasReachedState(LoaderState.PREINITIALIZATION)) throw new IllegalStateException(
                "Called this API too early, you are not allowed to use BC api before pre init!");
        try {
            Class<?> cls = Class.forName(clsName);
            Object[] arr = cls.getEnumConstants();
            if (arr == null) {
                return fail(clsName, nullVersion);
            }
            Enum<?>[] enumArr = ((Class<Enum<?>>) cls).getEnumConstants();

            for (Enum<?> e : enumArr) {
                if (e.name().equals(enumName) && baseVersion.isInstance(e)) return (T) e;
            }
            return fail(clsName, nullVersion);
        } catch (ClassNotFoundException e) {
            return fail(clsName, nullVersion);
        }
    }

    private static <M> M fail(String clsName, M failure) {
        String[] split = clsName.split("\\.");
        String module;
        if (split.length < 2) {
            BCLog.logger.warn("Tried and failed to get the module name from " + Arrays.toString(split) + " (" + clsName + ")!");
            module = "invalid";
        } else {
            module = split[1];
        }
        module = StringUtils.capitalize(module);
        String bcMod = "BuildCraft|" + module;
        if (Loader.isModLoaded(bcMod)) {
            BCLog.logger.warn("Failed to load the  " + clsName + " dispite the appropriate buildcraft module being installed (" + module + ")");
        }
        return failure;
    }
}
