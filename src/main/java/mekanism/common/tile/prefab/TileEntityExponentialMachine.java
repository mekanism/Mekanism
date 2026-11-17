package mekanism.common.tile.prefab;

import com.google.common.primitives.Ints;
import java.util.List;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.integration.computer.ComputerConstants;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityExponentialMachine<RECIPE extends MekanismRecipe<?, ?, ?>, CACHE_LOOKUP extends RecipeCacheLookupMonitor<RECIPE>>
      extends TileEntityRecipeMachine<RECIPE, CACHE_LOOKUP> {

    private final boolean hasVariableEnergyUsage;
    private int clientEnergyUsed = 0;
    private int baselineMaxOperations = 1;

    protected TileEntityExponentialMachine(Holder<Block> blockProvider, BlockPos pos, BlockState state, List<RecipeError> errorTypes) {
        this(blockProvider, pos, state, errorTypes, false);
    }

    protected TileEntityExponentialMachine(Holder<Block> blockProvider, BlockPos pos, BlockState state, List<RecipeError> errorTypes, boolean hasVariableEnergyUsage) {
        this.hasVariableEnergyUsage = hasVariableEnergyUsage;
        super(blockProvider, pos, state, errorTypes);
    }

    protected void updateAndProcessRecipe(ServerLevel level) {
        if (hasVariableEnergyUsage) {
            MachineEnergyContainer<?> energyContainer = energyContainer();
            long prev = energyContainer.getAmountAsLong();
            if (recipeCacheLookupMonitor.updateAndProcess(level)) {
                //Update amount of energy that actually got used, as if we are "near" full we may not have performed our max number of operations
                clientEnergyUsed = Math.max(0, Ints.saturatedCast(prev - energyContainer.getAmountAsLong()));
            } else {
                //If we don't have a cached recipe so didn't process anything at all just return zero
                clientEnergyUsed = 0;
            }
        } else {
            recipeCacheLookupMonitor.updateAndProcess(level);
        }
    }

    @ComputerMethod(nameOverride = "getEnergyUsage", methodDescription = ComputerConstants.DESCRIPTION_GET_ENERGY_USAGE)
    public int getEnergyUsed() {
        if (hasVariableEnergyUsage) {
            return clientEnergyUsed;
        }
        return getActive() ? energyContainer().getEnergyPerTick() : 0;
    }

    @Override
    public void recalculateUpgrades(HolderGetter<Upgrade> upgrades, Holder<Upgrade> upgrade, int totalInstalled) {
        if (upgrade.is(UpgradeIds.SPEED)) {
            baselineMaxOperations = (int) Math.pow(2, totalInstalled);
        }
        super.recalculateUpgrades(upgrades, upgrade, totalInstalled);
    }

    @Override
    public boolean upgradeInfoIsExponential(Holder<Upgrade> upgrade) {
        return upgrade.is(UpgradeIds.SPEED);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        if (hasVariableEnergyUsage) {
            container.track(SyncableInt.create(this::getEnergyUsed, value -> clientEnergyUsed = value));
        }
    }

    @Override
    protected CachedRecipe<RECIPE> setCommonSettings(CachedRecipe<RECIPE> recipe) {
        MachineEnergyContainer<?> energyContainer = energyContainer();
        return super.setCommonSettings(recipe)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setBaselineMaxOperations(() -> baselineMaxOperations);
    }

    public abstract MachineEnergyContainer<?> energyContainer();

    public int getBaselineMaxOperations() {
        return baselineMaxOperations;
    }

    public boolean hasVariableEnergyUsage() {
        return hasVariableEnergyUsage;
    }
}