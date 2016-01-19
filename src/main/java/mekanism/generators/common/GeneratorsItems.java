package mekanism.generators.common;

import mekanism.common.item.ItemMekanism;
import mekanism.generators.common.item.ItemHohlraum;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("MekanismGenerators")
public class GeneratorsItems
{
	public static final Item SolarPanel = new ItemMekanism().setUnlocalizedName("SolarPanel");
	public static final ItemHohlraum Hohlraum = (ItemHohlraum)new ItemHohlraum().setUnlocalizedName("Hohlraum");
	public static final Item TurbineBlade = new ItemMekanism().setUnlocalizedName("TurbineBlade");

	public static void register()
	{
		GameRegistry.registerItem(SolarPanel, "SolarPanel");
		GameRegistry.registerItem(Hohlraum, "Hohlraum");
		GameRegistry.registerItem(TurbineBlade, "TurbineBlade");
	}
}
