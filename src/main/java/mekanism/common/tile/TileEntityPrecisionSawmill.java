package mekanism.common.tile;

import java.util.Map;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.SawmillRecipe;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityPrecisionSawmill extends TileEntityChanceMachine<SawmillRecipe> {

    public TileEntityPrecisionSawmill() {
        super("sawmill", "PrecisionSawmill", BlockStateMachine.MachineType.PRECISION_SAWMILL.baseEnergy,
              usage.precisionSawmillUsage, 200, MekanismUtils.getResource(ResourceType.GUI, "GuiBasicMachine.png"));
    }

    @Override
    public Map<ItemStackInput, SawmillRecipe> getRecipes() {
        return Recipe.PRECISION_SAWMILL.get();
    }
}
