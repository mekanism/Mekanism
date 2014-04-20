package mekanism.common.tile;

import java.util.HashMap;
import java.util.Map;

import mekanism.client.gui.GuiProgress.ProgressBar;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockMachine.MachineType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.sun.javaws.progress.Progress;

public class TileEntityEnergizedSmelter extends TileEntityElectricMachine
{
	public static Map<ItemStack, ItemStack> furnaceRecipes = new HashMap<ItemStack, ItemStack>();

	public TileEntityEnergizedSmelter()
	{
		super("Smelter.ogg", "EnergizedSmelter", Mekanism.energizedSmelterUsage, 200, MachineType.ENERGIZED_SMELTER.baseEnergy);
	}

	@Override
	public Map getRecipes()
	{
		return furnaceRecipes;
	}

	@Override
	public ProgressBar getProgressType()
	{
		return ProgressBar.GREEN;
	}
}
