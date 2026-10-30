package mekanism.common.recipe.lookup.monitor;

import mekanism.api.SerializationConstants;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class ProgressRecipeCacheLookupMonitor<RECIPE extends MekanismRecipe<?, ?, ?>> extends RecipeCacheLookupMonitor<RECIPE> {

    private int operatingTicks;

    public ProgressRecipeCacheLookupMonitor(IRecipeLookupHandler<RECIPE> handler) {
        super(handler);
    }

    public ProgressRecipeCacheLookupMonitor(IRecipeLookupHandler<RECIPE> handler, int cacheIndex) {
        super(handler, cacheIndex);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        operatingTicks = input.getIntOr(SerializationConstants.PROGRESS, 0);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(SerializationConstants.PROGRESS, operatingTicks);
    }

    @Override
    public int getSavedOperatingTicks(int cacheIndex) {
        return cachedIndexMatches(cacheIndex) ? operatingTicks : super.getSavedOperatingTicks(cacheIndex);
    }

    public void inheritSavedOperatingTicks(int operatingTicks) {
        this.operatingTicks = operatingTicks;
    }

    @Nullable
    @Override
    public CachedRecipe<RECIPE> createNewCachedRecipe(RecipeHolder<RECIPE> recipeHolder, int cacheIndex) {
        CachedRecipe<RECIPE> cachedRecipe = super.createNewCachedRecipe(recipeHolder, cacheIndex);
        if (cachedRecipe == null) {
            return null;
        }
        return cachedRecipe.setOperatingTicksChanged(operatingTicks -> this.operatingTicks = operatingTicks);
    }

    public SyncableInt operatingTicksAsSyncable() {
        return SyncableInt.create(() -> operatingTicks, value -> operatingTicks = value);
    }
}