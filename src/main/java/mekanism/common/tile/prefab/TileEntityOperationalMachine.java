package mekanism.common.tile.prefab;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.api.recipes.IMekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.Upgrade;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;

public abstract class TileEntityOperationalMachine<RECIPE extends IMekanismRecipe> extends TileEntityMachine implements IComparatorSupport {

    public int operatingTicks;

    public int BASE_TICKS_REQUIRED;

    public int ticksRequired;
    //TODO: Protected?
    public CachedRecipe<RECIPE> cachedRecipe = null;

    protected TileEntityOperationalMachine(String sound, MachineType type, int upgradeSlot, int baseTicksRequired) {
        super(sound, type, upgradeSlot);
        ticksRequired = BASE_TICKS_REQUIRED = baseTicksRequired;
    }

    public double getScaledProgress() {
        return (double) operatingTicks / (double) ticksRequired;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            operatingTicks = dataStream.readInt();
            ticksRequired = dataStream.readInt();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(operatingTicks);
        data.add(ticksRequired);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        operatingTicks = nbtTags.getInteger("operatingTicks");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setInteger("operatingTicks", operatingTicks);
        return nbtTags;
    }

    @Override
    public void recalculateUpgradables(Upgrade upgrade) {
        super.recalculateUpgradables(upgrade);
        switch (upgrade) {
            case ENERGY:
                energyPerTick = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_PER_TICK); // incorporate speed upgrades
                break;
            case SPEED:
                ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
                energyPerTick = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_PER_TICK);
                break;
            default:
                break;
        }
    }

    @Override
    public int getRedstoneLevel() {
        return Container.calcRedstoneFromInventory(this);
    }

    //TODO: Move the declaration of this stuff to an interface
    @Nonnull
    public abstract Recipe<RECIPE> getRecipes();

    //TODO: Maybe rename this method
    @Nullable
    protected CachedRecipe<RECIPE> getOrFindCachedRecipe() {
        //If there is no cached recipe or the input doesn't match, attempt to get the recipe based on the input
        if (cachedRecipe == null || !cachedRecipe.hasResourcesForTick()) {
            //TODO: Should this use a separate method than hasResourcesForTick?
            RECIPE recipe = getRecipe();
            if (recipe != null) {
                CachedRecipe<RECIPE> cached = createNewCachedRecipe(recipe);
                if (cachedRecipe == null || cached != null) {
                    //Only override our cached recipe if we were able to find a recipe that matches, or we don't have a cached recipe.
                    // This way if we end up getting back to the same recipe we won't have to recalculate quite as much
                    cachedRecipe = cached;
                }
            }
        }
        return cachedRecipe;
    }

    //TODO: JavaDoc this and other recipe methods. This one gets recipe from inputs in machine ignoring what is currently cached
    // mainly for purposes of creating the new cached recipe
    @Nullable
    protected abstract RECIPE getRecipe();

    protected abstract CachedRecipe<RECIPE> createNewCachedRecipe(@Nonnull RECIPE recipe);
}