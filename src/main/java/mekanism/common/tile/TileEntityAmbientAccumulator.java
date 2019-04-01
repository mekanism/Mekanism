package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.IntegerInput;
import mekanism.common.recipe.machines.AmbientGasRecipe;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityAmbientAccumulator extends TileEntityContainerBlock implements IGasHandler {

    public static Random gasRand = new Random();
    public GasTank collectedGas = new GasTank(1000);
    public int cachedDimensionId = 0;
    public AmbientGasRecipe cachedRecipe;

    public TileEntityAmbientAccumulator() {
        super("AmbientAccumulator");
        inventory = NonNullList.withSize(0, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            if (cachedRecipe == null || world.provider.getDimension() != cachedDimensionId) {
                cachedDimensionId = world.provider.getDimension();
                cachedRecipe = RecipeHandler.getDimensionGas(new IntegerInput(cachedDimensionId));
            }

            if (cachedRecipe != null && gasRand.nextDouble() < 0.05 && cachedRecipe.getOutput()
                  .applyOutputs(collectedGas, false, 1)) {
                cachedRecipe.getOutput().applyOutputs(collectedGas, true, 1);
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
            return (T) this;
        }

        return super.getCapability(capability, side);
    }
}
