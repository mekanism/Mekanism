package mekanism.generators.common;

import mekanism.common.item.ItemMekanism;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("MekanismGenerators")
public class GeneratorsItems
{
	public static final Item SolarPanel = new ItemMekanism().setUnlocalizedName("SolarPanel");

	public static void register()
	{
		GameRegistry.registerItem(SolarPanel, "SolarPanel");
	}
}
