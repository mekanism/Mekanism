package mekanism.common.content.evaporation;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class EvaporationCache extends MultiblockCache<EvaporationMultiblockData> {

    private Reference2IntMap<ResourceKey<Recipe<?>>> recipesUsed = Reference2IntMaps.emptyMap();

    @Override
    public void merge(MultiblockCache<EvaporationMultiblockData> mergeCache, RejectContents rejectContents) {
        super.merge(mergeCache, rejectContents);
        EvaporationCache evaporationMergeCache = (EvaporationCache) mergeCache;
        if (recipesUsed.isEmpty()) {
            recipesUsed = evaporationMergeCache.recipesUsed;
        } else {
            for (ObjectIterator<Reference2IntMap.Entry<ResourceKey<Recipe<?>>>> iterator = Reference2IntMaps.fastIterator(evaporationMergeCache.recipesUsed); iterator.hasNext(); ) {
                Reference2IntMap.Entry<ResourceKey<Recipe<?>>> entry = iterator.next();
                recipesUsed.mergeInt(entry.getKey(), entry.getIntValue(), Integer::sum);
            }
        }
    }

    @Override
    public void apply(EvaporationMultiblockData data, TransactionContext transaction) {
        super.apply(data, transaction);
        data.usedRecipesLoaded(recipesUsed);
    }

    @Override
    public void sync(EvaporationMultiblockData data, TransactionContext transaction) {
        super.sync(data, transaction);
        recipesUsed = new Reference2IntOpenHashMap<>(data.getRecipesUsed());
    }

    @Override
    public void load(ValueInput input) {
        super.load(input);
        recipesUsed = input.read(SerializationConstants.RECIPES_USED, RecipeCacheLookupMonitor.RECIPES_USED_CODEC)
              .<Reference2IntMap<ResourceKey<Recipe<?>>>>map(Reference2IntOpenHashMap::new)
              .orElse(Reference2IntMaps.emptyMap());
    }

    @Override
    public void save(ValueOutput output) {
        super.save(output);
        output.store(SerializationConstants.RECIPES_USED, RecipeCacheLookupMonitor.RECIPES_USED_CODEC, this.recipesUsed);
    }
}