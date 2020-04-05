package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class GasRecipeData implements RecipeUpgradeData<GasRecipeData> {

    private final List<IChemicalTank<Gas, GasStack>> gasTanks;

    GasRecipeData(ListNBT tanks) {
        int count = DataHandlerUtils.getMaxId(tanks, NBTConstants.TANK);
        gasTanks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            gasTanks.add(BasicGasTank.create(Integer.MAX_VALUE, null));
        }
        DataHandlerUtils.readTanks(gasTanks, tanks);
    }

    GasRecipeData(List<IChemicalTank<Gas, GasStack>> gasTanks) {
        this.gasTanks = gasTanks;
    }

    @Nullable
    @Override
    public GasRecipeData merge(GasRecipeData other) {
        List<IChemicalTank<Gas, GasStack>> allTanks = new ArrayList<>(gasTanks.size() + other.gasTanks.size());
        allTanks.addAll(gasTanks);
        allTanks.addAll(other.gasTanks);
        return new GasRecipeData(allTanks);
    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        if (gasTanks.isEmpty()) {
            return true;
        }
        Item item = stack.getItem();
        Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        List<IChemicalTank<Gas, GasStack>> gasTanks = new ArrayList<>();
        if (capability.isPresent()) {
            IGasHandler gasHandler = capability.get();
            for (int i = 0; i < gasHandler.getGasTankCount(); i++) {
                int tank = i;
                gasTanks.add(BasicGasTank.create(gasHandler.getGasTankCapacity(tank), gas -> gasHandler.isGasValid(tank, new GasStack(gas, 1)), null));
            }
        } else if (item instanceof BlockItem) {
            TileEntityMekanism tile = null;
            Block block = ((BlockItem) item).getBlock();
            if (block instanceof IHasTileEntity<?>) {
                TileEntity tileEntity = ((IHasTileEntity<?>) block).getTileType().create();
                if (tileEntity instanceof TileEntityMekanism) {
                    tile = (TileEntityMekanism) tileEntity;
                }
            }
            if (tile == null || !tile.handlesGas()) {
                //Something went wrong
                return false;
            }
            TileEntityMekanism mekTile = tile;
            for (int i = 0; i < tile.getGasTankCount(); i++) {
                int tank = i;
                gasTanks.add(BasicGasTank.create(tile.getGasTankCapacity(tank), gas -> mekTile.isGasValid(tank, new GasStack(gas, 1)), null));
            }
        } else {
            return false;
        }
        if (gasTanks.isEmpty()) {
            //We don't actually have any tanks in the output
            return true;
        }
        //TODO: Improve the logic used so that it tries to batch similar types of gases together first
        // and maybe make it try multiple slot combinations
        IMekanismGasHandler outputHandler = new IMekanismGasHandler() {
            @Nonnull
            @Override
            public List<? extends IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
                return gasTanks;
            }

            @Override
            public void onContentsChanged() {
            }
        };
        boolean hasData = false;
        for (IChemicalTank<Gas, GasStack> gasTank : this.gasTanks) {
            if (!gasTank.isEmpty()) {
                if (!outputHandler.insertGas(gasTank.getStack(), Action.EXECUTE).isEmpty()) {
                    //If we have a remainder something failed so bail
                    return false;
                }
                hasData = true;
            }
        }
        if (hasData) {
            //We managed to transfer it all into valid slots, so save it to the stack
            ItemDataUtils.setList(stack, NBTConstants.GAS_TANKS, DataHandlerUtils.writeTanks(gasTanks));
        }
        return true;
    }
}