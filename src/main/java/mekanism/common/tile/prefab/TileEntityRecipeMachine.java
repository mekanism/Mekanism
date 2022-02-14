package mekanism.common.tile.prefab;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityRecipeMachine<RECIPE extends MekanismRecipe> extends TileEntityConfigurableMachine implements IRecipeLookupHandler<RECIPE> {

    public static final int RECIPE_CHECK_FREQUENCY = 100;//Every five seconds

    protected final BooleanSupplier recheckAllRecipeErrors;
    private final List<RecipeError> errorTypes;
    private final boolean[] trackedErrors;

    protected RecipeCacheLookupMonitor<RECIPE> recipeCacheLookupMonitor;

    protected TileEntityRecipeMachine(IBlockProvider blockProvider, BlockPos pos, BlockState state, List<RecipeError> errorTypes) {
        super(blockProvider, pos, state);
        this.errorTypes = errorTypes;
        recheckAllRecipeErrors = shouldRecheckAllErrors(this);
        trackedErrors = new boolean[this.errorTypes.size()];
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        recipeCacheLookupMonitor = createNewCacheMonitor();
    }

    protected RecipeCacheLookupMonitor<RECIPE> createNewCacheMonitor() {
        return new RecipeCacheLookupMonitor<>(this);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.trackArray(trackedErrors);
    }

    @Override
    public void clearRecipeErrors(int cacheIndex) {
        Arrays.fill(trackedErrors, false);
    }

    protected void onErrorsChanged(Set<RecipeError> errors) {
        for (int i = 0; i < trackedErrors.length; i++) {
            trackedErrors[i] = errors.contains(errorTypes.get(i));
        }
    }

    public BooleanSupplier getWarningCheck(RecipeError error) {
        int errorIndex = errorTypes.indexOf(error);
        if (errorIndex == -1) {
            //Something went wrong
            return () -> false;
        }
        return () -> trackedErrors[errorIndex];
    }

    public static BooleanSupplier shouldRecheckAllErrors(TileEntityMekanism tile) {
        // Choose a random offset to check for all errors. We do this to ensure that not every tile tries to recheck errors for every
        // recipe the same tick and thus create uneven spikes of CPU usage
        int checkOffset = ThreadLocalRandom.current().nextInt(RECIPE_CHECK_FREQUENCY);
        return () -> !tile.playersUsing.isEmpty() && tile.hasLevel() && tile.getLevel().getGameTime() % RECIPE_CHECK_FREQUENCY == checkOffset;
    }
}