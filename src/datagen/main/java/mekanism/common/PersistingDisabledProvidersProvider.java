package mekanism.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import mekanism.client.integration.emi.IMekEmiDatagen;
import mekanism.client.integration.gender.IMekGenderDatagen;
import mekanism.client.recipe_viewer.alias.IAliasMapping;
import mekanism.client.recipe_viewer.alias.MekanismAliasMapping;
import mekanism.common.integration.IMekCrTDatagen;
import mekanism.common.integration.IMekProjectEDatagen;
import mekanism.common.lib.FieldReflectionHelper;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.HashCache.ProviderCache;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.data.event.PackGenerator;
import org.jspecify.annotations.Nullable;

/// Used for helping to persist specific integrations we have that aren't updated yet.
public class PersistingDisabledProvidersProvider implements DataProvider {

    private static final FieldReflectionHelper<PackGenerator, DataGenerator.@Nullable PackGenerator> INTERNAL_GENERATOR_FIELD =
          new FieldReflectionHelper<PackGenerator, DataGenerator.@Nullable PackGenerator>(PackGenerator.class, "internalGenerator", () -> null);
    @Nullable
    private static HashCache globalCache;

    //Called by a core mod
    public static void captureGlobalCache(HashCache cache) {
        globalCache = cache;
    }

    public static void addDisableableProviders(PackGenerator packGenerator, ModContainer modContainer) {
        String modid = modContainer.getModId();
        Set<String> pathsToSkip = new HashSet<>();
        List<String> fakeProviders = new ArrayList<>();
        if (Mekanism.hooks.emi.isLoaded()) {
            packGenerator.createProvider((output, lookup) -> IMekEmiDatagen.INSTANCE.aliasProvider(output, lookup, modid, MekanismAliasMapping::new));
        } else {
            skipEmi(modid, pathsToSkip, fakeProviders);
        }
        addGender(packGenerator, modid, pathsToSkip, fakeProviders);
        if (Mekanism.hooks.projecte.isLoaded()) {
            packGenerator.createProvider(IMekProjectEDatagen.INSTANCE::customConversionProvider);
        } else {
            Mekanism.logger.warn("Skipping and persisting existing {} data generated files for ProjectE", modid);
            pathsToSkip.add("pe_custom_conversions");
            fakeProviders.add("Custom EMC Conversions: " + modid);
        }
        if (Mekanism.hooks.craftTweaker.isLoaded()) {
            packGenerator.createProvider(IMekCrTDatagen.INSTANCE::exampleProvider);
        } else {
            Mekanism.logger.warn("Skipping and persisting existing {} data generated files for CraftTweaker", modid);
            pathsToSkip.add("scripts");
            fakeProviders.add("CraftTweaker Examples: " + modid);
        }

        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        packGenerator.createProvider(output -> new PersistingDisabledProvidersProvider(output, pathsToSkip, fakeProviders));
    }

    public static void addDisabledEmiProvider(PackGenerator packGenerator, ModContainer modContainer, Supplier<IAliasMapping> mappings) {
        String modid = modContainer.getModId();
        Set<String> pathsToSkip = new HashSet<>();
        List<String> fakeProviders = new ArrayList<>();
        if (Mekanism.hooks.emi.isLoaded()) {
            packGenerator.createProvider((output, lookup) -> IMekEmiDatagen.INSTANCE.aliasProvider(output, lookup, modid, mappings));
        } else {
            skipEmi(modid, pathsToSkip, fakeProviders);
        }
        addGender(packGenerator, modid, pathsToSkip, fakeProviders);
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        packGenerator.createProvider(output -> new PersistingDisabledProvidersProvider(output, pathsToSkip, fakeProviders));
    }

    private static void skipEmi(String modid, Set<String> pathsToSkip, List<String> fakeProviders) {
        Mekanism.logger.warn("Skipping and persisting existing {} data generated files for EMI", modid);
        pathsToSkip.add("emi/aliases");
        fakeProviders.add("EMI Alias Provider: " + modid);
    }

    public static void skipRecipeProvider(PackGenerator packGenerator, String directory) {
        DataGenerator.PackGenerator internalGenerator = Objects.requireNonNull(INTERNAL_GENERATOR_FIELD.getValue(packGenerator));
        //Go up one level as the cache files for subpacks are not stored
        packGenerator.createProvider(output -> new PersistingDisabledProvidersProvider(output.getOutputFolder().getParent(), Collections.singleton(directory), Collections.singletonList(
              internalGenerator.providerPrefix + "/Registries for reloadable"
        )));
    }

    private static void addGender(PackGenerator packGenerator, String modid, Set<String> pathsToSkip, List<String> fakeProviders) {
        if (Mekanism.hooks.genderMod.isLoaded()) {
            IMekGenderDatagen genderDatagen = IMekGenderDatagen.INSTANCES.get(modid);
            if (genderDatagen != null) {
                packGenerator.createProvider(genderDatagen::armorProvider);
            }
        } else {
            Mekanism.logger.warn("Skipping and persisting existing {} data generated files for Female Gender Mod", modid);
            pathsToSkip.add("female_gender_mod/armor_data");
            fakeProviders.add("Gender Armor Provider: " + modid);
        }
    }

    private final Set<String> pathsToSkip;
    private final List<String> fakeProviders;
    private final Path baseOutputPath;

    private PersistingDisabledProvidersProvider(PackOutput output, Set<String> pathsToSkip, List<String> fakeProviders) {
        this(output.getOutputFolder(), pathsToSkip, fakeProviders);
    }

    private PersistingDisabledProvidersProvider(Path baseOutputPath, Set<String> pathsToSkip, List<String> fakeProviders) {
        this.baseOutputPath = baseOutputPath;
        this.pathsToSkip = pathsToSkip.stream().map(path -> "/" + path + "/").collect(Collectors.toUnmodifiableSet());
        this.fakeProviders = fakeProviders;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        if (globalCache == null) {
            throw new RuntimeException("Failed to retrieve global cache");
        }
        return CompletableFuture.runAsync(() -> tryPersist(globalCache));
    }

    private void tryPersist(HashCache cache) {
        if (!pathsToSkip.isEmpty()) {
            //NeoForge added field so we can't just AT it
            FieldReflectionHelper<HashCache, Map<String, ProviderCache>> originalCachesField = new FieldReflectionHelper<>(HashCache.class, "originalCaches", Collections::emptyMap);
            Map<String, ProviderCache> originalCaches = originalCachesField.getValue(cache);

            //Persist data from previous runs that is in the correct format into the current run
            for (Map.Entry<String, ProviderCache> entry : cache.caches.entrySet()) {
                String id = entry.getKey();
                ProviderCache newCache = entry.getValue();
                ProviderCache oldCache = originalCaches.get(id);
                if (oldCache == null) {
                    //Skip any caches that are new and didn't used to exist
                    continue;
                }
                Map<Path, HashCode> newCacheData = new HashMap<>(newCache.data());
                boolean changed = false;
                for (Map.Entry<Path, HashCode> oldEntry : oldCache.data().entrySet()) {
                    Path dataPath = oldEntry.getKey();
                    if (!newCacheData.containsKey(dataPath) && shouldPersist(dataPath) && Files.exists(dataPath)) {
                        newCacheData.put(dataPath, oldEntry.getValue());
                        changed = true;
                        //Technically this is unused except in a logging message but log it anyway, if we didn't end up having any caches to add though we can ignore it
                        cache.writes++;
                    }
                }
                if (changed) {
                    //Update the value with a new ProvideCache as we cannot mutate fields in records
                    entry.setValue(new ProviderCache(id, ImmutableMap.copyOf(newCacheData)));
                }
            }
        }

        //Load and inject any providers we have that are fully disabled into the cache system
        // We do this after copying things to persist, so we don't have to copy these as well
        for (String fakeProvider : fakeProviders) {
            Path path = cache.getProviderCachePath(fakeProvider);
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
        return pathsToSkip.stream().anyMatch(stringPath::contains);
    }

    @Override
    public String getName() {
        return "Persisting disabled provider";
    }
}
