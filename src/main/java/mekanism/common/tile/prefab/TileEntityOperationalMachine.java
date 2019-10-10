package mekanism.common.tile.prefab;

import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.Upgrade;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.util.MekanismUtils;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public abstract class TileEntityOperationalMachine<RECIPE extends MekanismRecipe> extends TileEntityMachine implements IComparatorSupport,
      ITileCachedRecipeHolder<RECIPE> {

    private int operatingTicks;

    public int BASE_TICKS_REQUIRED;

    public int ticksRequired;
    //TODO: Protected?
    public CachedRecipe<RECIPE> cachedRecipe = null;

    protected TileEntityOperationalMachine(IBlockProvider blockProvider, int baseTicksRequired) {
        super(blockProvider);
        ticksRequired = BASE_TICKS_REQUIRED = baseTicksRequired;
    }

    public double getScaledProgress() {
        return (double) getOperatingTicks() / (double) ticksRequired;
    }

    public int getOperatingTicks() {
        if (isRemote()) {
            return operatingTicks;
        }
        if (cachedRecipe == null) {
            return 0;
        }
        return cachedRecipe.getOperatingTicks();
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            operatingTicks = dataStream.readInt();
            ticksRequired = dataStream.readInt();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(getOperatingTicks());
        data.add(ticksRequired);
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        //TODO: Save/Load operating ticks properly given the variable is stored in the CachedRecipe
        operatingTicks = nbtTags.getInt("operatingTicks");
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        //TODO: Save/Load operating ticks properly given the variable is stored in the CachedRecipe
        nbtTags.putInt("operatingTicks", getOperatingTicks());
        return nbtTags;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
            setEnergyPerTick(MekanismUtils.getEnergyPerTick(this, getBaseUsage()));
        }
    }

    @Override
    public int getRedstoneLevel() {
        return Container.calcRedstoneFromInventory(this);
    }
}