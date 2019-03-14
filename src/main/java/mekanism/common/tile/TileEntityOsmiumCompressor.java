package mekanism.common.tile;

import java.util.Map;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.machines.OsmiumCompressorRecipe;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.util.EnumFacing;

public class TileEntityOsmiumCompressor extends TileEntityAdvancedElectricMachine<OsmiumCompressorRecipe> {

    public TileEntityOsmiumCompressor() {
        super("compressor", "OsmiumCompressor", BlockStateMachine.MachineType.OSMIUM_COMPRESSOR.baseEnergy,
              usage.osmiumCompressorUsage, BASE_TICKS_REQUIRED, BASE_GAS_PER_TICK);
    }

    @Override
    public Map<AdvancedMachineInput, OsmiumCompressorRecipe> getRecipes() {
        return Recipe.OSMIUM_COMPRESSOR.get();
    }

    @Override
    public boolean isValidGas(Gas gas) {
        return Recipe.OSMIUM_COMPRESSOR.containsRecipe(gas);
    }

    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        if (canReceiveGas(side, stack.getGas())) {
            return gasTank.receive(stack, doTransfer);
        }
        return 0;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return gasTank.canReceive(type) && isValidGas(type);
    }

    @Override
    public boolean canTubeConnect(EnumFacing side) {
        return true;
    }
}
