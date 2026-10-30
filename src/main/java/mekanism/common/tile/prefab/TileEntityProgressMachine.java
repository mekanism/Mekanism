package mekanism.common.tile.prefab;

import java.util.List;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.recipe.lookup.monitor.ProgressRecipeCacheLookupMonitor;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityProgressMachine<RECIPE extends MekanismRecipe<?, ?, ?>> extends TileEntityRecipeMachine<RECIPE, ProgressRecipeCacheLookupMonitor<RECIPE>> {

    protected int baseTicksRequired;
    public int ticksRequired;
    private int operationsPerTick = 1;//will increase for modified upgrade multipliers

    protected TileEntityProgressMachine(Holder<Block> blockProvider, BlockPos pos, BlockState state, List<RecipeError> errorTypes, int baseTicksRequired) {
        super(blockProvider, pos, state, errorTypes);
        this.baseTicksRequired = baseTicksRequired;
        ticksRequired = this.baseTicksRequired;
    }

    @Override
    protected ProgressRecipeCacheLookupMonitor<RECIPE> createNewCacheMonitor() {
        return new ProgressRecipeCacheLookupMonitor<>(this);
    }

    public float getScaledProgress() {
        return getRecipeProgress() / (float) ticksRequired;
    }

    @ComputerMethod
    public int getRecipeProgress() {
        return recipeCacheLookupMonitor.getSavedOperatingTicks(0);
    }

    @ComputerMethod
    public int getTicksRequired() {
        return ticksRequired;
    }

    @Override
    public void recalculateUpgrades(HolderGetter<Upgrade> upgrades, Holder<Upgrade> upgrade, int totalInstalled) {
        if (upgrade.is(UpgradeIds.SPEED)) {
            ticksRequired = UpgradeUtils.getTicks(baseTicksRequired, upgrade, totalInstalled);
            operationsPerTick = UpgradeUtils.getOperationsPerTick(baseTicksRequired, 1, upgrade, totalInstalled);
        }
        super.recalculateUpgrades(upgrades, upgrade, totalInstalled);
    }

    public int getOperationsPerTick() {
        return this.operationsPerTick;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(recipeCacheLookupMonitor.operatingTicksAsSyncable());
        container.track(SyncableInt.create(this::getTicksRequired, value -> ticksRequired = value));
    }
}