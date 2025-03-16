package mekanism.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import mekanism.client.integration.emi.EmiAliasProvider;
import mekanism.client.integration.emi.MekanismEmiDefaults;
import mekanism.client.recipe_viewer.alias.IAliasMapping;
import mekanism.client.recipe_viewer.alias.MekanismAliasMapping;
import mekanism.common.integration.crafttweaker.MekanismCrTExampleProvider;
import mekanism.common.integration.projecte.MekanismCustomConversions;
import mekanism.common.lib.FieldReflectionHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.HashCache.ProviderCache;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
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

    public static void addDisableableProviders(GatherDataEvent event, CompletableFuture<HolderLookup.Provider> lookupProvider, Set<String> disabledCompats) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        Set<String> pathsToSkip = new HashSet<>();
        List<String> fakeProviders = new ArrayList<>();
        if (Mekanism.hooks.emi.isLoaded()) {
            gen.addProvider(event.includeClient(), new EmiAliasProvider(output, lookupProvider, Mekanism.MODID, MekanismAliasMapping::new));
            gen.addProvider(event.includeClient(), new MekanismEmiDefaults(output, existingFileHelper, lookupProvider));
        } else {
            pathsToSkip.add("emi/aliases");
            pathsToSkip.add("emi/recipes/defaults");
            fakeProviders.add("EMI Alias Provider: mekanism");
            fakeProviders.add("EMI Default Recipe Provider: mekanism");
        }
        if (Mekanism.hooks.projecte.isLoaded()) {
            gen.addProvider(event.includeServer(), new MekanismCustomConversions(output, lookupProvider));
        } else {
            pathsToSkip.add("pe_custom_conversions");
            fakeProviders.add("Custom EMC Conversions: mekanism");
        }
        if (Mekanism.hooks.craftTweaker.isLoaded()) {
            gen.addProvider(event.includeServer(), new MekanismCrTExampleProvider(output, existingFileHelper));
        } else {
            pathsToSkip.add("scripts");
            fakeProviders.add("CraftTweaker Examples: mekanism");
        }

        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        gen.addProvider(true, new PersistingDisabledProvidersProvider(output, disabledCompats, pathsToSkip, fakeProviders));
    }

    public static void addDisabledEmiProvider(GatherDataEvent event, CompletableFuture<HolderLookup.Provider> lookupProvider, String modid,
          Supplier<IAliasMapping> mappings, Supplier<ExistingFileProvider> defaultsProviderFunction) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        Set<String> pathsToSkip = new HashSet<>();
        List<String> fakeProviders = new ArrayList<>();
        if (Mekanism.hooks.emi.isLoaded()) {
            gen.addProvider(event.includeClient(), new EmiAliasProvider(output, lookupProvider, modid, mappings));
            gen.addProvider(event.includeClient(), defaultsProviderFunction.get().create(output, event.getExistingFileHelper(), lookupProvider));
        } else {
            pathsToSkip.add("emi/aliases");
            pathsToSkip.add("emi/recipes/defaults");
            fakeProviders.add("EMI Alias Provider: " + modid);
            fakeProviders.add("EMI Default Recipe Provider: " + modid);
        }
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        gen.addProvider(true, new PersistingDisabledProvidersProvider(output, Collections.emptySet(), pathsToSkip, fakeProviders));
    }

    private final Set<String> compatRecipesToSkip;
    private final Set<String> pathsToSkip;
    private final List<String> fakeProviders;
    private final Path baseOutputPath;

    private PersistingDisabledProvidersProvider(PackOutput output, Set<String> disabledCompats, Set<String> pathsToSkip, List<String> fakeProviders) {
        this.baseOutputPath = output.getOutputFolder();
        this.compatRecipesToSkip = disabledCompats.stream().map(compat -> compat + "/").collect(Collectors.toUnmodifiableSet());
        this.pathsToSkip = pathsToSkip.stream().map(path -> "/" + path + "/").collect(Collectors.toUnmodifiableSet());
        this.fakeProviders = fakeProviders;
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
        if (compatRecipesToSkip.isEmpty() && pathsToSkip.isEmpty() && fakeProviders.isEmpty()) {
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
        for (String fakeProvider : fakeProviders) {
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
        if (pathsToSkip.stream().anyMatch(stringPath::contains)) {
            return true;
        }
        int compatIndex = stringPath.indexOf("/recipe/compat/");
        if (compatIndex != -1) {
            //Compat recipes
            String compatPath = stringPath.substring(compatIndex + "/recipe/compat/".length());
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

    @FunctionalInterface
    public interface ExistingFileProvider {

        DataProvider create(PackOutput packOutput, ExistingFileHelper existingFileHelper, CompletableFuture<HolderLookup.Provider> registries);
    }
}
