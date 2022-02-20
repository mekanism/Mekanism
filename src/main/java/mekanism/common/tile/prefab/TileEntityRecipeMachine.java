package mekanism.common.tile.prefab;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.providers.IBlockProvider;
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
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityRecipeMachine<RECIPE extends MekanismRecipe> extends TileEntityConfigurableMachine implements IRecipeLookupHandler<RECIPE> {

    public static final int RECIPE_CHECK_FREQUENCY = 100;//Every five seconds

    protected final BooleanSupplier recheckAllRecipeErrors;
    private final List<RecipeError> errorTypes;
    private final boolean[] trackedErrors;

    protected RecipeCacheLookupMonitor<RECIPE> recipeCacheLookupMonitor;
    @Nullable
    private IContentsListener recipeCacheSaveOnlyListener;

    protected TileEntityRecipeMachine(IBlockProvider blockProvider, BlockPos pos, BlockState state, List<RecipeError> errorTypes) {
        super(blockProvider, pos, state);
        this.errorTypes = errorTypes;
        recheckAllRecipeErrors = shouldRecheckAllErrors(this);
        trackedErrors = new boolean[this.errorTypes.size()];
        //Clear the memory if we didn't use it. Note: We can set this to null as we pass it by reference so if it is not used
        // then it will get GC'd otherwise the corresponding things will still have a reference to it
        recipeCacheSaveOnlyListener = null;
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
    public final IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        return getInitialGasTanks(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener());
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    protected IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
        return null;
    }

    @Nullable
    @Override
    public final IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks(IContentsListener listener) {
        return getInitialInfusionTanks(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener());
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    protected IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
        return null;
    }

    @Nullable
    @Override
    public final IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks(IContentsListener listener) {
        return getInitialPigmentTanks(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener());
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    protected IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
        return null;
    }

    @Nullable
    @Override
    public final IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks(IContentsListener listener) {
        return getInitialSlurryTanks(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener());
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    protected IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
        return null;
    }

    @Nullable
    @Override
    protected final IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        return getInitialFluidTanks(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener());
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
        return null;
    }

    @Nullable
    @Override
    protected final IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        return getInitialEnergyContainers(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener());
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener) {
        return null;
    }

    @Nullable
    @Override
    protected final IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        return getInitialInventory(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener());
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener) {
        return null;
    }

    @Nullable
    @Override
    protected final IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return getInitialHeatCapacitors(listener, listener == this ? recipeCacheLookupMonitor : getRecipeCacheSaveOnlyListener(), ambientTemperature);
    }

    /**
     * @apiNote Do not call directly, only override implementation
     */
    @Nullable
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, IContentsListener recipeCacheListener, CachedAmbientTemperature ambientTemperature) {
        return null;
    }
}