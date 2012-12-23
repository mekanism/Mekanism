package mekanism.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TileEntityTheoreticalElementizer extends TileEntityAdvancedElectricMachine
{
	public static Map<ItemStack, ItemStack> recipes = new HashMap<ItemStack, ItemStack>();
	
	public TileEntityTheoreticalElementizer()
	{
		super("Elementizer.ogg", "Theoretical Elementizer", "/resources/mekanism/gui/GuiElementizer.png", 50, 1, 1000, 10000, 1000);
	}
	
	@Override
	public Map getRecipes()
	{
		return recipes;
	}

	@Override
	public int getFuelTicks(ItemStack itemstack)
	{
		if (itemstack.itemID == Item.diamond.shiftedIndex) return 1000;
		return 0;
	}
	
    public static Item getRandomMagicItem()
    {
    	Random rand = new Random();
    	int random = rand.nextInt(3);
    	if(random == 0) return Mekanism.LightningRod;
    	if(random == 1) return Mekanism.Stopwatch;
    	if(random == 2) return Mekanism.WeatherOrb;
    	return Mekanism.EnrichedAlloy;
    }
}
