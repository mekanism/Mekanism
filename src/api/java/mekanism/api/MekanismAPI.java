package mekanism.api;

import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.ServiceLoader;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.slf4j.Logger;

public class MekanismAPI {

    private MekanismAPI() {
    }

    /// The version of the api classes - may not always match the mod's version
    public static final String API_VERSION = "10.8.0";
    /// Mekanism's Mod ID
    public static final String MEKANISM_MODID = "mekanism";
    /// Mekanism debug mode
    public static boolean debug = false;
    /// Logger for use in Mekanism's API classes
    public static final Logger logger = LogUtils.getLogger();

    @Internal
    private static final ClassLoader SERVICE_CL = MekanismAPI.class.getClassLoader();

    /// Loads a Mekanism service from ServiceLoader, ensuring that the correct classloader is used instead of relying on the context classloader, which may not be
    /// correct
    ///
    /// @param serviceClass the interface class to search for
    ///
    /// @return the concrete implementation
    ///
    /// @throws IllegalStateException when an implementation is not found
    @Internal
    public static <SERVICE> SERVICE getService(Class<SERVICE> serviceClass) {
        Iterator<SERVICE> service = ServiceLoader.load(serviceClass, SERVICE_CL).iterator();
        if (service.hasNext()) {
            return service.next();
        }

        IllegalStateException illegalStateException = new IllegalStateException("No valid ServiceImpl for " + serviceClass.getSimpleName() + " found");
        logger.error("Failed to load service", illegalStateException);
        logger.error("CL: {} CCL: {}", SERVICE_CL, Thread.currentThread().getContextClassLoader());
        throw illegalStateException;
    }
}