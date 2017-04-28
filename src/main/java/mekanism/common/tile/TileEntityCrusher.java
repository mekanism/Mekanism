package mekanism.common.tile;

import java.util.Map;

import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.CrusherRecipe;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityCrusher extends TileEntityElectricMachine<CrusherRecipe>
{
	public TileEntityCrusher()
	{
		super("crusher", "Crusher", 200, BlockStateMachine.MachineType.CRUSHER.baseEnergy, usage.crusherUsage);
	}

	@Override
	public Map getRecipes()
	{
		return Recipe.CRUSHER.get();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getVolume()
	{
		return 0.5F*super.getVolume();
	}
}
