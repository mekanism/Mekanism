package mekanism.common.tile.prefab;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

public abstract class TileEntityProgressMachine<RECIPE extends MekanismRecipe> extends TileEntityRecipeMachine<RECIPE> {

    private int operatingTicks;
    protected int baseTicksRequired;
    public int ticksRequired;

    protected TileEntityProgressMachine(IBlockProvider blockProvider, int baseTicksRequired) {
        super(blockProvider);
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
    public void load(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.load(state, nbtTags);
        operatingTicks = nbtTags.getInt(NBTConstants.PROGRESS);
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT nbtTags) {
        super.save(nbtTags);
        nbtTags.putInt(NBTConstants.PROGRESS, getOperatingTicks());
        return nbtTags;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            ticksRequired = MekanismUtils.getTicks(this, baseTicksRequired);
        }
    }

    @Override
    public List<ITextComponent> getInfo(Upgrade upgrade) {
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(this::getOperatingTicks, this::setOperatingTicks));
        container.track(SyncableInt.create(this::getTicksRequired, value -> ticksRequired = value));
    }
}