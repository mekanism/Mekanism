package mekanism.common.tile;

import java.util.HashMap;
import java.util.Map;

import mekanism.client.gui.GuiProgress.ProgressBar;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockMachine.MachineType;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;

@InterfaceList({
		@Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2API", striprefs = true),
		@Interface(iface = "ic2.api.tile.IEnergyStorage", modid = "IC2API", striprefs = true),
		@Interface(iface = "cofh.api.energy.IEnergyHandler", modid = "CoFHAPI|energy"),
		@Interface(iface = "buildcraft.api.power.IPowerReceptor", modid = "BuildCraftAPI|power"),
		@Interface(iface = "buildcraft.api.power.IPowerEmitter", modid = "BuildCraftAPI|power")
})
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
}
