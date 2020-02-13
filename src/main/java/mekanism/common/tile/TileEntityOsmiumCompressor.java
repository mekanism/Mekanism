package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.util.Direction;

public class TileEntityOsmiumCompressor extends TileEntityAdvancedElectricMachine {

    public TileEntityOsmiumCompressor() {
        super(MekanismMachines.OSMIUM_COMPRESSOR, BASE_TICKS_REQUIRED, BASE_GAS_PER_TICK);
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ItemStackGasToItemStackRecipe> getRecipeType() {
        return MekanismRecipeType.COMPRESSING;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        return gasTank.canReceive(type) && isValidGas(type);
    }
}