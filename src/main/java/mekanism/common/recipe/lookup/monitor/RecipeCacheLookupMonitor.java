package mekanism.common.recipe.lookup.monitor;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ICachedRecipeHolder;
import mekanism.api.recipes.cache.ItemStackConstantChemicalToObjectCachedRecipe;
import mekanism.api.resource.IResourceContainer;
import mekanism.common.Mekanism;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

//TODO: Is there a clean way we could support the doLimitedCrafting gamerule based on the player that "owns" the machine?
public class RecipeCacheLookupMonitor<RECIPE extends MekanismRecipe<?, ?, ?>> implements ICachedRecipeHolder<RECIPE>, IContentsListener, RecipeCraftingHolder {

    /// From [net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity]
    public static final Codec<Map<ResourceKey<Recipe<?>>, Integer>> RECIPES_USED_CODEC = Codec.unboundedMap(Recipe.KEY_CODEC, Codec.INT);

    private final Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed = new Reference2IntOpenHashMap<>();
    private final boolean hasItemOutput;
    @Nullable
    private ResourceKey<Recipe<?>> lastRecipeUsed;
    private int lastRecipeCraftedAmount;
    private boolean resetLastRecipe;
    private boolean hadContainment;

    private final IRecipeLookupHandler<RECIPE> handler;
    protected final int cacheIndex;
    @Nullable
    protected CachedRecipe<RECIPE> cachedRecipe;
    protected boolean hasNoRecipe;
    protected boolean shouldUnpause;

    public RecipeCacheLookupMonitor(IRecipeLookupHandler<RECIPE> handler, boolean hasItemOutput) {
        this(handler, hasItemOutput, 0);
    }

    public RecipeCacheLookupMonitor(IRecipeLookupHandler<RECIPE> handler, boolean hasItemOutput, int cacheIndex) {
        this.handler = handler;
        this.hasItemOutput = hasItemOutput;
        this.cacheIndex = cacheIndex;
    }

    protected boolean cachedIndexMatches(int cacheIndex) {
        return this.cacheIndex == cacheIndex;
    }

    @Override
    public final void onContentsChanged() {
        handler.onContentsChanged();
        onChange();
    }

    public void onChange() {
        //Mark that we may have a recipe again
        hasNoRecipe = false;
        unpause();
    }

    public void unpause() {
        shouldUnpause = true;
    }

    /// Helper that wraps [#updateAndProcess(HolderLookup.Provider)] inside of a brief check to calculate how much energy actually got used.
    public int updateAndProcess(HolderLookup.Provider registries, IEnergyContainer energyContainer) {
        //Copy this so that if it changes we still have the original amount. Don't bother making it a constant though as this way
        // we can then use minusEqual instead of subtract to remove an extra copy call
        long prev = energyContainer.getAmountAsLong();
        if (updateAndProcess(registries)) {
            //Update amount of energy that actually got used, as if we are "near" full we may not have performed our max number of operations
            return Math.max(0, Ints.saturatedCast(prev - energyContainer.getAmountAsLong()));
        }
        //If we don't have a cached recipe so didn't process anything at all just return zero
        return 0;
    }

    public boolean updateAndProcess(HolderLookup.Provider registries) {
        CachedRecipe<RECIPE> oldCache = cachedRecipe;
        cachedRecipe = getUpdatedCache(cacheIndex);
        if (cachedRecipe != oldCache) {
            handler.onCachedRecipeChanged(registries, cachedRecipe, cacheIndex);
        }
        if (cachedRecipe != null) {
            if (shouldUnpause) {
                shouldUnpause = false;
                cachedRecipe.unpauseErrors();
            }
            cachedRecipe.process();
            return true;
        }
        return false;
    }

    @Override
    public void loadSavedData(CachedRecipe<RECIPE> cached, int cacheIndex) {
        if (cachedIndexMatches(cacheIndex)) {
            ICachedRecipeHolder.super.loadSavedData(cached, cacheIndex);
            if (cached instanceof ItemStackConstantChemicalToObjectCachedRecipe<?, ?> c &&
                handler instanceof IRecipeLookupHandler.ConstantUsageRecipeLookupHandler lookupHandler) {
                c.loadSavedUsageSoFar(lookupHandler.getSavedUsedSoFar(cacheIndex));
            }
        }
    }

    public void usedRecipesLoaded(Map<ResourceKey<Recipe<?>>, Integer> loadedUsage, @Nullable ResourceKey<Recipe<?>> lastRecipeUsed, int lastRecipeCraftedAmount) {
        this.recipesUsed.clear();
        this.recipesUsed.putAll(loadedUsage);
        this.resetLastRecipe = false;
        this.lastRecipeUsed = lastRecipeUsed;
        this.lastRecipeCraftedAmount = lastRecipeUsed == null ? 0 : lastRecipeCraftedAmount;
    }

    public Reference2IntMap<ResourceKey<Recipe<?>>> getRecipesUsed() {
        return this.recipesUsed;
    }

    public void loadAdditional(ValueInput input) {
        this.hadContainment = input.getBooleanOr(SerializationConstants.HAS_CONTAINMENT, false);
        usedRecipesLoaded(
              input.read(SerializationConstants.RECIPES_USED, RECIPES_USED_CODEC).orElse(Collections.emptyMap()),
              hasItemOutput ? input.read(SerializationConstants.LAST_RECIPE_USED, Recipe.KEY_CODEC).orElse(null) : null,
              hasItemOutput ? input.getIntOr(SerializationConstants.LAST_RECIPE_AMOUNT, 0) : 0
        );
    }

    public void saveAdditional(ValueOutput output) {
        output.store(SerializationConstants.RECIPES_USED, RECIPES_USED_CODEC, this.recipesUsed);
        output.putBoolean(SerializationConstants.HAS_CONTAINMENT, this.hadContainment);
        if (this.hasItemOutput) {
            output.storeNullable(SerializationConstants.LAST_RECIPE_USED, Recipe.KEY_CODEC, this.lastRecipeUsed);
            if (this.lastRecipeCraftedAmount > 0) {
                output.putInt(SerializationConstants.LAST_RECIPE_AMOUNT, this.lastRecipeCraftedAmount);
            }
        }
    }

    @Nullable
    @Override
    public CachedRecipe<RECIPE> getCachedRecipe(int cacheIndex) {
        return cachedIndexMatches(cacheIndex) ? cachedRecipe : null;
    }

    @Nullable
    @Override
    public RecipeHolder<RECIPE> getRecipe(int cacheIndex) {
        return cachedIndexMatches(cacheIndex) ? handler.getRecipe(cacheIndex) : null;
    }

    @Nullable
    @Override
    public CachedRecipe<RECIPE> createNewCachedRecipe(RecipeHolder<RECIPE> recipeHolder, int cacheIndex) {
        if (cachedIndexMatches(cacheIndex)) {
            return handler.createNewCachedRecipe(recipeHolder.value(), cacheIndex)
                  .setOnFinish(operations -> {
                      handler.markForSave();
                      setRecipeUsed(recipeHolder, operations);
                  });
        }
        return null;
    }

    @Override
    public boolean invalidateCache() {
        return Mekanism.worldTickHandler.flushTagAndRecipeCaches;
    }

    @Override
    public void setHasNoRecipe(int cacheIndex) {
        if (cachedIndexMatches(cacheIndex)) {
            hasNoRecipe = true;
        }
    }

    @Override
    public boolean hasNoRecipe(int cacheIndex) {
        return cachedIndexMatches(cacheIndex) ? hasNoRecipe : ICachedRecipeHolder.super.hasNoRecipe(cacheIndex);
    }

    @Override
    public final void setRecipeUsed(@Nullable RecipeHolder<?> recipeUsed) {
        setRecipeUsed(recipeUsed, 1);
    }

    private void setRecipeUsed(@Nullable RecipeHolder<?> recipeUsed, int operations) {
        //Filter out synthetic recipe ids
        if (recipeUsed != null) {
            ResourceKey<Recipe<?>> id = recipeUsed.id();
            String path = id.identifier().getPath();
            if (path.startsWith("/mekanism_generated/")) {
                //Synthetic generated recipe that was wrapped around a vanilla recipe
                id = ResourceKey.create(Registries.RECIPE, id.identifier().withPath(path.substring("/mekanism_generated/".length())));
            } else if (path.startsWith("/")) {
                //Synthetic recipe that isn't actually persisted
                return;
            }
            this.recipesUsed.addTo(id, operations);
            if (hasItemOutput) {
                if (!resetLastRecipe && this.lastRecipeUsed == id) {
                    this.lastRecipeCraftedAmount += operations;
                } else {
                    this.lastRecipeUsed = id;
                    this.lastRecipeCraftedAmount = operations;
                    this.resetLastRecipe = false;
                }
            }
        }
    }

    public void outputDecreased() {
        resetLastRecipe = true;
    }

    @Nullable
    @Override
    public RecipeHolder<?> getRecipeUsed() {
        return null;
    }

    @Override
    public final void awardUsedRecipes(Player player, List<ItemStack> itemStacks) {
    }

    public void awardUsedRecipesAndPopExperience(ServerPlayer player) {
        awardUsedRecipesAndPopExperience(player, recipesUsed, handler.hasXpContainment(), this);
    }

    public void xpContainmentChanged(ServerLevel level, Vec3 position, boolean hasContainment) {
        if (hasContainment != this.hadContainment) {
            if (this.hadContainment) {
                //We lost containment, drop stored xp on the ground
                getRecipesToAwardAndPopExperience(level, position, recipesUsed, true);
            }
            //Note: Regardless of the direction we need to void the cached recipe information to avoid installing and uninstalled the upgrade letting you gain xp from previous ones
            this.recipesUsed.clear();
            if (!this.hadContainment && this.lastRecipeUsed != null && this.lastRecipeCraftedAmount > 0) {
                //Persist the last recipe that was being crafted for how much xp we have from it so far
                this.recipesUsed.put(this.lastRecipeUsed, this.lastRecipeCraftedAmount);
            }
            this.lastRecipeUsed = null;
            this.lastRecipeCraftedAmount = 0;
            this.hadContainment = hasContainment;
        }
    }

    public static void awardUsedRecipesAndPopExperience(ServerPlayer player, Reference2IntMap<ResourceKey<Recipe<?>>> recipesUsed, boolean dropXp, RecipeCacheLookupMonitor<?>... monitors) {
        ServerLevel level = player.level();
        List<RecipeHolder<?>> recipesToAward = getRecipesToAwardAndPopExperience(level, player.position(), recipesUsed, dropXp);
        player.awardRecipes(recipesToAward);
        List<ItemStack> items = new ArrayList<>();
        for (RecipeCacheLookupMonitor<?> monitor : monitors) {
            if (monitor.hasItemOutput && !dropXp) {
                //Note: We skip checking if resetLastRecipe is true or false, as in general it likely will be true here if the item was just removed
                if (monitor.lastRecipeUsed != null && monitor.lastRecipeCraftedAmount > 0) {
                    Optional<RecipeHolder<?>> holder = level.recipeAccess().byKey(monitor.lastRecipeUsed);
                    //noinspection OptionalIsPresent - Capturing lambda
                    if (holder.isPresent()) {
                        awardExperience(level, player.position(), holder.get(), monitor.lastRecipeCraftedAmount);
                    }
                }
                monitor.resetLastRecipe = false;
                monitor.lastRecipeUsed = null;
                monitor.lastRecipeCraftedAmount = 0;
            }
            //Collect any input items
            //TODO: Vanilla's usage of the items for triggerRecipeCrafted makes no sense outside of instant craft recipes.
            // This is somewhere in between what vanilla does for furnaces and other containers in terms of furnaces use whatever current items including result item,
            // other containers just use inputs, but also the inputs might be out of date (or empty) compared to what it was crafted with for the furnace (just like ours)
            for (IResourceContainer<?> inputContainer : monitor.handler.getRecipeContainers(monitor.cacheIndex).inputs()) {
                if (inputContainer instanceof IInventorySlot slot) {
                    //Note: Similar to vanilla we add the stacks even if they are empty
                    items.add(slot.asItemStack());
                }
            }
            monitor.recipesUsed.clear();
        }
        for (RecipeHolder<?> recipe : recipesToAward) {
            player.triggerRecipeCrafted(recipe, items);
        }
    }

    //Note: We don't call this from preRemoveSideEffects for the TEP as there is no concept of a multiblock being removed
    public void popExperienceAndClearUsed(ServerLevel level, Vec3 position) {
        popExperience(level, position, recipesUsed, handler.hasXpContainment());
        recipesUsed.clear();
    }

    public static void popExperience(ServerLevel level, Vec3 position, Reference2IntMap<ResourceKey<Recipe<?>>> recipesUsed, boolean hasXpContainment) {
        if (hasXpContainment) {
            getRecipesToAwardAndPopExperience(level, position, recipesUsed, true);
        }
    }

    private static List<RecipeHolder<?>> getRecipesToAwardAndPopExperience(ServerLevel level, Vec3 position, Reference2IntMap<ResourceKey<Recipe<?>>> recipesUsed, boolean dropXp) {
        //Note: We only drop xp if the upgrade is present, even when the block/entity is destroyed
        List<RecipeHolder<?>> recipesToAward = new ArrayList<>();
        for (ObjectIterator<Reference2IntMap.Entry<ResourceKey<Recipe<?>>>> iterator = Reference2IntMaps.fastIterator(recipesUsed); iterator.hasNext(); ) {
            Reference2IntMap.Entry<ResourceKey<Recipe<?>>> entry = iterator.next();
            Optional<RecipeHolder<?>> holder = level.recipeAccess().byKey(entry.getKey());
            if (holder.isPresent()) {
                RecipeHolder<?> recipeHolder = holder.get();
                recipesToAward.add(recipeHolder);
                if (dropXp) {
                    awardExperience(level, position, recipeHolder, entry.getIntValue());
                }
            }
        }
        return recipesToAward;
    }

    private static void awardExperience(ServerLevel level, Vec3 position, RecipeHolder<?> recipeHolder, int crafted) {
        float experience = switch (recipeHolder.value()) {
            case MekanismRecipe<?, ?, ?> recipe -> recipe.experience();
            case SmeltingRecipe recipe -> recipe.experience();
            default -> 0;
        };
        if (experience > 0) {
            AbstractFurnaceBlockEntity.createExperience(level, position, crafted, experience);
        }
    }
}