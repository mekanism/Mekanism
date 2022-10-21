package mekanism.common;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.lib.FieldReflectionHelper;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Used for helping to persist specific integrations we have that aren't updated yet.
 */
public class PersistingDisabledProvidersProvider implements DataProvider {

    private static HashCache globalCache;

    //Called by a core mod
    public static void captureGlobalCache(HashCache cache) {
        globalCache = cache;
    }

    private static final Set<String> PATHS_TO_SKIP = Set.of(
          //"/scripts/"//CraftTweaker script files
          //, "/pe_custom_conversions/"//ProjectE custom conversion files
    );
    private static final Set<String> COMPAT_RECIPES_TO_SKIP = Set.of(
          //"ae2/"
          //, "biomesoplenty/"
          //, "byg/"//Biomes You'll Go
          //, "ilikewood/"
          //, "ilikewoodxbiomesoplenty/"//I Like Wood Biomes O' Plenty
          //, "ilikewoodxbyg/"//I Like Wood Biomes You'll Go
    );
    private static final List<DataProvider> FAKE_PROVIDERS = Stream.<String>of(
          //"CraftTweaker Examples: mekanism"
          //, "Custom EMC Conversions: mekanism"
    ).<DataProvider>map(name -> new DataProvider() {
        @Override
        public void run(@NotNull CachedOutput cache) {
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }
    }).toList();

    private final Path baseOutputPath;

    public PersistingDisabledProvidersProvider(DataGenerator gen) {
        baseOutputPath = gen.getOutputFolder();
    }

    @Override
    public void run(@NotNull CachedOutput cache) throws IOException {
        if (globalCache == null) {
            throw new RuntimeException("Failed to retrieve global cache");
        }
        tryPersist(globalCache);
    }

    private <PROVIDER_CACHE, CACHE_UPDATER> void tryPersist(HashCache cache) throws IOException {
        FieldReflectionHelper<HashCache, Map<DataProvider, CACHE_UPDATER>> cachesToWrite = new FieldReflectionHelper<>(HashCache.class, "f_236083_", () -> null);
        Map<DataProvider, CACHE_UPDATER> toWrite = cachesToWrite.getValue(cache);
        //Skip writing a cache for this data generator
        toWrite.remove(this);
        if (PATHS_TO_SKIP.isEmpty() && COMPAT_RECIPES_TO_SKIP.isEmpty() && FAKE_PROVIDERS.isEmpty()) {
            //Skip if we don't have any things to override and persist
            return;
        }

        FieldReflectionHelper<HashCache, Map<DataProvider, PROVIDER_CACHE>> existingCaches = new FieldReflectionHelper<>(HashCache.class, "f_236082_", () -> null);
        FieldReflectionHelper<HashCache, Set<Path>> cachePaths = new FieldReflectionHelper<>(HashCache.class, "f_236084_", () -> null);
        FieldReflectionHelper<HashCache, Integer> initialCount = new FieldReflectionHelper<>(HashCache.class, "f_236085_", () -> null);
        Class<CACHE_UPDATER> cacheUpdater;
        Class<PROVIDER_CACHE> providerCache;
        Method cacheReader;
        try {
            cacheUpdater = (Class<CACHE_UPDATER>) Class.forName("net.minecraft.data.HashCache$CacheUpdater");
            providerCache = (Class<PROVIDER_CACHE>) Class.forName("net.minecraft.data.HashCache$ProviderCache");
            cacheReader = ObfuscationReflectionHelper.findMethod(HashCache.class, "m_236092_", Path.class, Path.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        FieldReflectionHelper<CACHE_UPDATER, PROVIDER_CACHE> updaterOldCache = new FieldReflectionHelper<>(cacheUpdater, "f_236113_", () -> null);
        FieldReflectionHelper<CACHE_UPDATER, PROVIDER_CACHE> updaterNewCache = new FieldReflectionHelper<>(cacheUpdater, "f_236114_", () -> null);
        FieldReflectionHelper<PROVIDER_CACHE, Map<Path, HashCode>> providerCacheData = new FieldReflectionHelper<>(providerCache, "f_236127_", () -> null);

        Path cacheDir = baseOutputPath.resolve(".cache");

        Map<DataProvider, PROVIDER_CACHE> existing = existingCaches.getValue(cache);

        Set<Path> paths = cachePaths.getValue(cache);
        //Load and inject any providers we have that are fully disabled into the cache system
        int additional = 0;
        for (DataProvider fakeProvider : FAKE_PROVIDERS) {
            Path path = getProviderCachePath(cacheDir, fakeProvider);
            paths.add(path);
            PROVIDER_CACHE provider;
            try {
                provider = (PROVIDER_CACHE) cacheReader.invoke(null, baseOutputPath, path);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Mekanism.logger.error("Failed to manually load fake provider, skipping: {}", fakeProvider.getName());
                continue;
            }
            existing.put(fakeProvider, provider);
            additional += providerCacheData.getValue(provider).size();
            //Initialize the cache as one that should be written when we loop caches to write
            cache.getUpdater(fakeProvider);
        }
        //Technically this is unused except in a logging message but log it anyway
        int totalAdditional = additional;
        initialCount.transformValue(cache, ConstantPredicates.alwaysTrue(), c -> c + totalAdditional);

        //Persist data from previous runs that is in the correct format into the current run
        for (Map.Entry<DataProvider, CACHE_UPDATER> entry : toWrite.entrySet()) {
            CACHE_UPDATER updater = entry.getValue();
            PROVIDER_CACHE newCache = updaterNewCache.getValue(updater);
            PROVIDER_CACHE oldCache = updaterOldCache.getValue(updater);
            Map<Path, HashCode> newCacheData = providerCacheData.getValue(newCache);
            Map<Path, HashCode> oldCacheData = providerCacheData.getValue(oldCache);
            for (Map.Entry<Path, HashCode> oldEntry : oldCacheData.entrySet()) {
                Path dataPath = oldEntry.getKey();
                if (!newCacheData.containsKey(dataPath) && shouldPersist(dataPath) && Files.exists(dataPath)) {
                    newCacheData.put(dataPath, oldEntry.getValue());
                }
            }
        }
    }

    private boolean shouldPersist(Path path) {
        //Get the string representation of the path and sanitize it
        String stringPath = path.toString().replace('\\', '/');
        //Mekanism.logger.info("Evaluating path: {}", stringPath);
        if (PATHS_TO_SKIP.stream().anyMatch(stringPath::contains)) {
            return true;
        }
        int compatIndex = stringPath.indexOf("/recipes/compat/");
        if (compatIndex != -1) {
            //Compat recipes
            String compatPath = stringPath.substring(compatIndex + "/recipes/compat/".length());
            //Mekanism.logger.info("Evaluating compat path: {}", compatPath);
            return COMPAT_RECIPES_TO_SKIP.stream().anyMatch(compatPath::startsWith);
        }
        return false;
    }

    @NotNull
    @Override
    public String getName() {
        return "Persisting disabled provider";
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Path getProviderCachePath(Path cacheDir, DataProvider provider) {
        //Copy of HashCache#getProviderCache
        return cacheDir.resolve(Hashing.sha1().hashString(provider.getName(), StandardCharsets.UTF_8).toString());
    }
}