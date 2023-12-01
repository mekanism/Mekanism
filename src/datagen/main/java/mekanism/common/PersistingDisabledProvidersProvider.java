package mekanism.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import mekanism.common.lib.FieldReflectionHelper;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.HashCache.ProviderCache;
import net.minecraft.data.PackOutput;
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
          "/scripts/",//CraftTweaker script files
          "/pe_custom_conversions/"//ProjectE custom conversion files
    );
    private static final List<String> FAKE_PROVIDERS = List.of(
          "CraftTweaker Examples: mekanism",
          "Custom EMC Conversions: mekanism"
    );


    private final Set<String> compatRecipesToSkip;
    private final Path baseOutputPath;

    public PersistingDisabledProvidersProvider(PackOutput output, Set<String> disabledCompats) {
        baseOutputPath = output.getOutputFolder();
        compatRecipesToSkip = disabledCompats.stream().map(compat -> compat + "/").collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(@NotNull CachedOutput cache) {
        if (globalCache == null) {
            throw new RuntimeException("Failed to retrieve global cache");
        }
        return CompletableFuture.runAsync(() -> tryPersist(globalCache));
    }

    private void tryPersist(HashCache cache) {
        if (compatRecipesToSkip.isEmpty() && PATHS_TO_SKIP.isEmpty() && FAKE_PROVIDERS.isEmpty()) {
            //Skip if we don't have any things to override and persist
            return;
        }

        //NeoForge added field so we can't just AT it
        FieldReflectionHelper<HashCache, Map<String, ProviderCache>> originalCachesField = new FieldReflectionHelper<>(HashCache.class, "originalCaches", () -> null);
        Map<String, ProviderCache> originalCaches = originalCachesField.getValue(cache);

        int additionalWrites = 0;
        //Persist data from previous runs that is in the correct format into the current run
        for (Map.Entry<String, ProviderCache> entry : cache.caches.entrySet()) {
            String id = entry.getKey();
            ProviderCache newCache = cache.caches.get(id);
            ProviderCache oldCache = originalCaches.get(id);
            Map<Path, HashCode> newCacheData = new HashMap<>(newCache.data());
            boolean changed = false;
            ImmutableMap<Path, HashCode> oldCacheData = oldCache.data();
            for (Map.Entry<Path, HashCode> oldEntry : oldCacheData.entrySet()) {
                Path dataPath = oldEntry.getKey();
                if (!newCacheData.containsKey(dataPath) && shouldPersist(dataPath) && Files.exists(dataPath)) {
                    newCacheData.put(dataPath, oldEntry.getValue());
                    changed = true;
                    additionalWrites++;
                }
            }
            if (changed) {
                //Update the value with a new ProvideCache as we cannot mutate fields in records
                entry.setValue(new ProviderCache(id, ImmutableMap.copyOf(newCacheData)));
            }
        }

        //Technically this is unused except in a logging message but log it anyway, if we didn't end up having any caches to add though we can ignore it
        cache.writes += additionalWrites;

        Path cacheDir = baseOutputPath.resolve(".cache");
        //Load and inject any providers we have that are fully disabled into the cache system
        // We do this after copying things to persist, so we don't have to copy these as well
        for (String fakeProvider : FAKE_PROVIDERS) {
            Path path = getProviderCachePath(cacheDir, fakeProvider);
            ProviderCache provider = HashCache.readCache(baseOutputPath, path);
            cache.cachePaths.add(path);
            cache.caches.put(fakeProvider, provider);
            //Technically this is unused except in a logging message but log it anyway, if we didn't end up having any caches to add though we can ignore it
            cache.initialCount += provider.count();
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
            return compatRecipesToSkip.stream().anyMatch(compatPath::startsWith);
        }
        return false;
    }

    @NotNull
    @Override
    public String getName() {
        return "Persisting disabled provider";
    }

    @SuppressWarnings("deprecation")
    private static Path getProviderCachePath(Path cacheDir, String providerName) {
        //Copy of HashCache#getProviderCachePath
        return cacheDir.resolve(Hashing.sha1().hashString(providerName, StandardCharsets.UTF_8).toString());
    }
}
