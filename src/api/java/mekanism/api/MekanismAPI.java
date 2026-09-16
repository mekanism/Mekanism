package mekanism.api;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class MekanismAPI {

    private MekanismAPI() {
    }

    /// The version of the api classes - may not always match the mod's version
    public static final String API_VERSION = "10.8.0";
    /// Mekanism's Mod ID
    public static final String MEKANISM_MODID = "mekanism";
    /// Mekanism Additions' Mod ID
    ///
    /// @since 10.8.0
    public static final String ADDITIONS_MODID = "mekanismadditions";
    /// Mekanism Generators' Mod ID
    ///
    /// @since 10.8.0
    public static final String GENERATORS_MODID = "mekanismgenerators";
    /// Mekanism Tools' Mod ID
    ///
    /// @since 10.8.0
    public static final String TOOLS_MODID = "mekanismtools";
    /// Logger for use in Mekanism's API classes
    public static final Logger logger = LogUtils.getLogger();

    /// Mekanism debug mode
    public static boolean debug = false;

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
        SERVICE service = getOptionalService(serviceClass);
        if (service == null) {
            throw serviceImplException(serviceClass);
        }
        return service;
    }

    /// Loads a Mekanism service from ServiceLoader, ensuring that the correct classloader is used instead of relying on the context classloader, which may not be
    /// correct
    ///
    /// @param serviceClass the interface class to search for
    ///
    /// @return the concrete implementation, or `null` if no implementation is found
    @Nullable
    @Internal
    public static <SERVICE> SERVICE getOptionalService(Class<SERVICE> serviceClass) {
        Iterator<SERVICE> service = ServiceLoader.load(serviceClass, SERVICE_CL).iterator();
        if (service.hasNext()) {
            return service.next();
        }
        return null;
    }

    /// Loads a Mekanism service from ServiceLoader for each modid, ensuring that the correct classloader is used instead of relying on the context classloader, which may
    /// not be correct
    ///
    /// @param serviceClass the interface class to search for
    ///
    /// @return the concrete implementations
    ///
    /// @throws IllegalStateException when an implementation is not found, or multiple services have the same modid.
    @Internal
    public static <SERVICE extends ModBasedService> Map<String, SERVICE> getModBasedServices(Class<SERVICE> serviceClass) {
        Map<String, SERVICE> map = new HashMap<>();
        for (SERVICE service : ServiceLoader.load(serviceClass, SERVICE_CL)) {
            if (map.put(service.modid(), service) != null) {
                throw serviceImplException("Multiple ServiceImpls for " + serviceClass.getSimpleName() + " found with modid " + service.modid());
            }
        }
        if (map.isEmpty()) {
            throw serviceImplException(serviceClass);
        }
        return Collections.unmodifiableMap(map);
    }

    private static IllegalStateException serviceImplException(Class<?> serviceClass) {
        return serviceImplException("No valid ServiceImpl for " + serviceClass.getSimpleName() + " found");
    }

    private static IllegalStateException serviceImplException(String message) {
        IllegalStateException illegalStateException = new IllegalStateException(message);
        logger.error("Failed to load service", illegalStateException);
        logger.error("CL: {} CCL: {}", SERVICE_CL, Thread.currentThread().getContextClassLoader());
        return illegalStateException;
    }
}