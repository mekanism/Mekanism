package mekanism.common.advancements;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public abstract class BaseAdvancementProvider implements DataProvider {

    private final CompletableFuture<HolderLookup.Provider> registries;
    private final PackOutput.PathProvider pathProvider;
    private final ExistingFileHelper existingFileHelper;
    private final String advancementFolder;
    private final String modid;

    public BaseAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper existingFileHelper, String modid) {
        this.modid = modid;
        this.registries = provider;
        this.existingFileHelper = existingFileHelper;
        this.advancementFolder = Registries.elementsDirPath(Registries.ADVANCEMENT);
        this.pathProvider = output.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
    }

    @NotNull
    @Override
    public String getName() {
        return "Advancements: " + modid;
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(@NotNull CachedOutput cache) {
        return this.registries.thenCompose(lookupProvider -> {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            registerAdvancements(advancement -> {
                ResourceLocation id = advancement.id();
                if (existingFileHelper.exists(id, PackType.SERVER_DATA, ".json", advancementFolder)) {
                    throw new IllegalStateException("Duplicate advancement " + id);
                }
                Path path = this.pathProvider.json(id);
                existingFileHelper.trackGenerated(id, PackType.SERVER_DATA, ".json", advancementFolder);
                futures.add(DataProvider.saveStable(cache, lookupProvider, Advancement.CODEC, advancement.value(), path));
            });
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    protected abstract void registerAdvancements(@NotNull Consumer<AdvancementHolder> consumer);

    protected ExtendedAdvancementBuilder advancement(MekanismAdvancement advancement) {
        return ExtendedAdvancementBuilder.advancement(advancement, existingFileHelper);
    }

    public static Criterion<TriggerInstance> hasItems(ItemPredicate... predicates) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(predicates);
    }

    @SafeVarargs
    public static Criterion<InventoryChangeTrigger.TriggerInstance> hasAllItems(Holder<Item>... items) {
        //return InventoryChangeTrigger.TriggerInstance.hasItems(items);
        return hasItems(Arrays.stream(items).map(BaseAdvancementProvider::predicate).toArray(ItemPredicate[]::new));
    }

    @SafeVarargs
    protected static ItemPredicate predicate(Holder<Item>... items) {
        //return ItemPredicate.Builder.item().of(items).build();
        return new ItemPredicate(Optional.of(HolderSet.direct(items)), MinMaxBounds.Ints.ANY, DataComponentPredicate.EMPTY, Collections.emptyMap());
    }

    @SafeVarargs
    protected static Criterion<TriggerInstance> hasItems(TagKey<Item>... tags) {
        return hasItems(Arrays.stream(tags)
              .map(tag -> ItemPredicate.Builder.item().of(tag).build())
              .toArray(ItemPredicate[]::new));
    }

    protected static Item[] getItems(Collection<? extends Holder<Item>> items, Predicate<Item> matcher) {
        return items.stream()
              .map(Holder::value)
              .filter(matcher)
              .toArray(Item[]::new);
    }
}