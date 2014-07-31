package mekanism.generators.common;

import mekanism.common.item.ItemMekanism;

import net.minecraft.item.Item;
import cpw.mods.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("MekanismGenerators")
public class GeneratorsItems
{
	//Items
	public static final Item SolarPanel = new ItemMekanism().setUnlocalizedName("SolarPanel");;
}
