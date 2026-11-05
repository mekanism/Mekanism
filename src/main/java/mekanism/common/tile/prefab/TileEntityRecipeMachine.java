package mekanism.common.tile.prefab;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public abstract class TileEntityRecipeMachine<RECIPE extends MekanismRecipe<?, ?, ?>, CACHE_LOOKUP extends RecipeCacheLookupMonitor<RECIPE>>
      extends TileEntityConfigurableMachine implements IRecipeLookupHandler<RECIPE> {

    public static final int RECIPE_CHECK_FREQUENCY = 5 * SharedConstants.TICKS_PER_SECOND;

    protected final BooleanSupplier recheckAllRecipeErrors;
    private final List<RecipeError> errorTypes;
    private final boolean[] trackedErrors;

    @UnknownNullability//Initialized in presetVariables
    protected CACHE_LOOKUP recipeCacheLookupMonitor;
    @Nullable
    private IContentsListener recipeCacheSaveOnlyListener;
    @Nullable
    private IContentsListener recipeCacheUnpauseListener;
    @Nullable
    private IContentsListener recipeCacheUnpauseSaveOnlyListener;
    @Nullable
    private Holder<Upgrade> xpContainmentUpgrade;
    private boolean clientHasContainmentUpgrade;

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

    @SuppressWarnings("unchecked")
    protected CACHE_LOOKUP createNewCacheMonitor() {
        return (CACHE_LOOKUP) new RecipeCacheLookupMonitor<>(this, hasItemOutput());
    }

    protected abstract boolean hasItemOutput();

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        recipeCacheLookupMonitor.loadAdditional(input);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        recipeCacheLookupMonitor.saveAdditional(output);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level instanceof ServerLevel serverLevel) {
            popExperience(serverLevel, Vec3.atCenterOf(pos));
        }
    }

    @Override
    public void popExperience(ServerLevel level, Vec3 position) {
        recipeCacheLookupMonitor.popExperienceAndClearUsed(level, position);
    }

    @Override
    public void popExperience(ServerPlayer player) {
        recipeCacheLookupMonitor.awardUsedRecipesAndPopExperience(player);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (upgradesRegistry == null) {
            xpContainmentUpgrade = null;
        } else {
            //Note: We don't have to reset this on tag reload, as datapack registries do not support being reloaded without the server restarting
            xpContainmentUpgrade = upgradesRegistry.get(UpgradeIds.XP_CONTAINMENT).orElse(null);
        }
    }

    @Override
    public void recalculateUpgrades(HolderGetter<Upgrade> upgrades, Holder<Upgrade> upgrade, int totalInstalled) {
        super.recalculateUpgrades(upgrades, upgrade, totalInstalled);
        if (upgrade.is(UpgradeIds.XP_CONTAINMENT) && level instanceof ServerLevel serverLevel) {
            recipeCacheLookupMonitor.xpContainmentChanged(serverLevel, Vec3.atCenterOf(worldPosition), totalInstalled > 0);
        }
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
        container.track(SyncableBoolean.create(this::hasXpContainment, value -> clientHasContainmentUpgrade = value));
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
            return ConstantPredicates.ALWAYS_FALSE;
        }
        return () -> trackedErrors[errorIndex];
    }

    public static BooleanSupplier shouldRecheckAllErrors(TileEntityMekanism tile) {
        // Choose a random offset to check for all errors. We do this to ensure that not every tile tries to recheck errors for every
        // recipe the same tick and thus create uneven spikes of CPU usage
        int checkOffset = ThreadLocalRandom.current().nextInt(RECIPE_CHECK_FREQUENCY);
        return () -> !tile.playersUsing.isEmpty() && tile.getLevel() != null && tile.getLevel().getGameTime() % RECIPE_CHECK_FREQUENCY == checkOffset;
    }

    @Override
    public boolean hasXpContainment() {
        if (level != null && level.isClientSide()) {
            return clientHasContainmentUpgrade;
        }
        //If the xp containment upgrade has been removed as an upgrade, or if there is at least one installed
        return xpContainmentUpgrade == null || getUpgrades(xpContainmentUpgrade) > 0;
    }

    @Nullable
    @Override
    public final IContainerHolder<IChemicalTank> getInitialChemicalTanks(IContentsListener listener) {
        return getInitialChemicalTanks(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener(), getRecipeCacheUnpauseListener(listener));
    }

    /// @apiNote Do not call directly, only override implementation
    @Nullable
    protected IContainerHolder<IChemicalTank> getInitialChemicalTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        return null;
    }

    @Nullable
    @Override
    protected final IContainerHolder<IFluidTank> getInitialFluidTanks(IContentsListener listener) {
        return getInitialFluidTanks(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener(), getRecipeCacheUnpauseListener(listener));
    }

    /// @apiNote Do not call directly, only override implementation
    @Nullable
    protected IContainerHolder<IFluidTank> getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        return null;
    }

    @Nullable
    @Override
    protected final ISingleContainerHolder<IEnergyContainer> getInitialEnergyContainer(IContentsListener listener) {
        return getInitialEnergyContainer(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener(), getRecipeCacheUnpauseListener(listener));
    }

    /// @apiNote Do not call directly, only override implementation
    @Nullable
    protected ISingleContainerHolder<IEnergyContainer> getInitialEnergyContainer(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        return null;
    }

    @Nullable
    @Override
    protected final IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        return getInitialInventory(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener(), getRecipeCacheUnpauseListener(listener));
    }

    /// @apiNote Do not call directly, only override implementation
    @Nullable
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        return null;
    }

    @Nullable
    @Override
    protected final ISingleContainerHolder<IHeatCapacitor> getInitialHeatCapacitor(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return getInitialHeatCapacitor(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener(), getRecipeCacheUnpauseListener(listener), ambientTemperature);
    }

    /// @apiNote Do not call directly, only override implementation
    @Nullable
    protected ISingleContainerHolder<IHeatCapacitor> getInitialHeatCapacitor(IContentsListener listener, IContentsListener recipeCacheListener,
          IContentsListener recipeCacheUnpauseListener, CachedAmbientTemperature ambientTemperature) {
        return null;
    }
}