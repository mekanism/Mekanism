package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.recipes.AmbientAccumulatorRecipe;
import mekanism.api.recipes.cache.AmbientAccumulatorCachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ICachedRecipeHolder;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityAmbientAccumulator extends TileEntityContainerBlock implements IGasHandler, ICachedRecipeHolder<AmbientAccumulatorRecipe> {

    public GasTank collectedGas = new GasTank(1000);
    public CachedRecipe<AmbientAccumulatorRecipe> cachedRecipe;

    public TileEntityAmbientAccumulator() {
        super("AmbientAccumulator");
        inventory = NonNullList.withSize(0, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            cachedRecipe = getUpdatedCache(cachedRecipe, 0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
        }
    }

    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        return 0;
    }

    @Override
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        return collectedGas.draw(amount, doTransfer);
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return false;
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return type == collectedGas.getGasType();
    }

    @Override
    @Nonnull
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{collectedGas};
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        TileUtils.addTankData(data, collectedGas);
        return data;
    }

    @Override
    public void handlePacketData(ByteBuf data) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            TileUtils.readTankData(data, collectedGas);
        }
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return InventoryUtils.EMPTY;
    }

    //Gas capability is never disabled here
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.GAS_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Nonnull
    @Override
    public Recipe<AmbientAccumulatorRecipe> getRecipes() {
        return Recipe.AMBIENT_ACCUMULATOR;
    }

    @Nullable
    @Override
    public AmbientAccumulatorRecipe getRecipe(int cacheIndex) {
        return getRecipes().findFirst(recipe -> recipe.test(world.provider.getDimension()));
    }

    @Nullable
    @Override
    public CachedRecipe<AmbientAccumulatorRecipe> createNewCachedRecipe(@Nonnull AmbientAccumulatorRecipe recipe, int cacheIndex) {
        return new AmbientAccumulatorCachedRecipe(recipe, () -> world.provider.getDimension(), OutputHelper.getOutputHandler(collectedGas))
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setOnFinish(this::markDirty);
    }
}