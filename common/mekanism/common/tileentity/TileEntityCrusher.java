package mekanism.common.tileentity;

import java.util.Map;

import net.minecraft.util.ResourceLocation;
import mekanism.common.Mekanism;
import mekanism.common.RecipeHandler.Recipe;
import mekanism.common.block.BlockMachine.MachineType;

public class TileEntityCrusher extends TileEntityElectricMachine
{
	public TileEntityCrusher()
	{
		super("Crusher.ogg", "Crusher", new ResourceLocation("mekanism", "gui/GuiCrusher.png"), Mekanism.crusherUsage, 200, MachineType.CRUSHER.baseEnergy);
	}
	
	@Override
	public Map getRecipes()
	{
		return Recipe.CRUSHER.get();
	}
	
	@Override
	public float getVolumeMultiplier()
	{
		return 0.5F;
	}
}
