package mekanism.common.tile.prefab;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import mekanism.api.IContentsListener;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityRecipeMachine<RECIPE extends MekanismRecipe<?>> extends TileEntityConfigurableMachine implements IRecipeLookupHandler<RECIPE> {

    public static final int RECIPE_CHECK_FREQUENCY = 5 * SharedConstants.TICKS_PER_SECOND;

    protected final BooleanSupplier recheckAllRecipeErrors;
    private final List<RecipeError> errorTypes;
    private final boolean[] trackedErrors;

    protected RecipeCacheLookupMonitor<RECIPE> recipeCacheLookupMonitor;
    @Nullable
    private IContentsListener recipeCacheSaveOnlyListener;
    @Nullable
    private IContentsListener recipeCacheUnpauseListener;
    @Nullable
    private IContentsListener recipeCacheUnpauseSaveOnlyListener;

    protected TileEntityRecipeMachine(Holder<Block> blockProvider, BlockPos pos, BlockState state, List<RecipeError> errorTypes) {
        super(blockProvider, pos, state);
        //Copy the list if it is mutable to ensure it doesn't get changed, otherwise just use the list
        this.errorTypes = List.copyOf(errorTypes);
        recheckAllRecipeErrors = shouldRecheckAllErrors(this);
        trackedErrors = new boolean[this.errorTypes.size()];
        //Clear the memory if we didn't use it. Note: We can set this to null as we pass it by reference so if it is not used
        // then it will get GC'd otherwise the corresponding things will still have a reference to it
        recipeCacheSaveOnlyListener = null;
        recipeCacheUnpauseListener = null;
        recipeCacheUnpauseSaveOnlyListener = null;
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        recipeCacheLookupMonitor = createNewCacheMonitor();
    }

    protected RecipeCacheLookupMonitor<RECIPE> createNewCacheMonitor() {
        return new RecipeCacheLookupMonitor<>(this);
    }

    protected IContentsListener getRecipeCacheSaveOnlyListener() {
        //If we don't support comparators we can just skip having a special one that only marks for save as our
        // setChanged won't actually do anything so there is no reason to bother creating a save only listener
        if (supportsComparator()) {
            if (recipeCacheSaveOnlyListener == null) {
                recipeCacheSaveOnlyListener = () -> {
                    markForSave();
                    recipeCacheLookupMonitor.onChange();
                };
            }
            return recipeCacheSaveOnlyListener;
        }
        return recipeCacheLookupMonitor;
    }

    protected IContentsListener getRecipeCacheUnpauseListener(@Nullable IContentsListener listener) {
        if (listener == this) {
            if (recipeCacheUnpauseListener == null) {
                recipeCacheUnpauseListener = () -> {
                    onContentsChanged();
                    recipeCacheLookupMonitor.unpause();
                };
            }
            return recipeCacheUnpauseListener;
        }
        if (recipeCacheUnpauseSaveOnlyListener == null) {
            recipeCacheUnpauseSaveOnlyListener = () -> {
                markForSave();
                recipeCacheLookupMonitor.unpause();
            };
        }
        return recipeCacheUnpauseSaveOnlyListener;
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

    @Nullable
    @Override
    public final IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        return getInitialChemicalTanks(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener(), getRecipeCacheUnpauseListener(listener));
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    protected IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        return null;
    }

    @Nullable
    @Override
    protected final IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        return getInitialFluidTanks(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener(), getRecipeCacheUnpauseListener(listener));
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        return null;
    }

    @Nullable
    @Override
    protected final IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        return getInitialEnergyContainers(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener(), getRecipeCacheUnpauseListener(listener));
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        return null;
    }

    @Nullable
    @Override
    protected final IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        return getInitialInventory(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener(), getRecipeCacheUnpauseListener(listener));
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        return null;
    }

    @Nullable
    @Override
    protected final IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return getInitialHeatCapacitors(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener(), getRecipeCacheUnpauseListener(listener), ambientTemperature);
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener, CachedAmbientTemperature ambientTemperature) {
        return null;
    }
}