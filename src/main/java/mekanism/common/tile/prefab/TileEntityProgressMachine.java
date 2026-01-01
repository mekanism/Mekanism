package mekanism.common.tile.prefab;

import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.Upgrade;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityProgressMachine<RECIPE extends MekanismRecipe<?>> extends TileEntityRecipeMachine<RECIPE> {

    private int operatingTicks;
    protected int baseTicksRequired;
    public int ticksRequired;
    private int operationsPerTick = 1;//will increase for modified upgrade multipliers

    protected TileEntityProgressMachine(Holder<Block> blockProvider, BlockPos pos, BlockState state, List<RecipeError> errorTypes, int baseTicksRequired) {
        super(blockProvider, pos, state, errorTypes);
        this.baseTicksRequired = baseTicksRequired;
        ticksRequired = this.baseTicksRequired;
    }

    public double getScaledProgress() {
        return getOperatingTicks() / (double) ticksRequired;
    }

    protected void setOperatingTicks(int ticks) {
        this.operatingTicks = ticks;
    }

    @ComputerMethod(nameOverride = "getRecipeProgress")
    public int getOperatingTicks() {
        return operatingTicks;
    }

    @ComputerMethod
    public int getTicksRequired() {
        return ticksRequired;
    }

    @Override
    public int getSavedOperatingTicks(int cacheIndex) {
        return getOperatingTicks();
    }

    @Override
    public void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        operatingTicks = input.getIntOr(SerializationConstants.PROGRESS, operatingTicks);
    }

    @Override
    public void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(SerializationConstants.PROGRESS, getOperatingTicks());
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        if (upgrade == Upgrade.SPEED) {
            ticksRequired = MekanismUtils.getTicks(this, baseTicksRequired);
            operationsPerTick = MekanismUtils.getOperationsPerTick(this, baseTicksRequired, 1);
        }
        super.recalculateUpgrades(upgrade);
    }

    public int getOperationsPerTick() {
        return this.operationsPerTick;
    }

    @NotNull
    @Override
    public List<Component> getInfo(@NotNull Upgrade upgrade) {
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(this::getOperatingTicks, this::setOperatingTicks));
        container.track(SyncableInt.create(this::getTicksRequired, value -> ticksRequired = value));
    }
}