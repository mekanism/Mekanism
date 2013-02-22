package mekanism.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityTheoreticalElementizer extends TileEntityAdvancedElectricMachine
{
	public TileEntityTheoreticalElementizer()
	{
		super("Elementizer.ogg", "Theoretical Elementizer", "/resources/mekanism/gui/GuiElementizer.png", 24, 1, 1000, 4800, 1000);
	}
	
	@Override
	public HashMap getRecipes()
	{
		return (HashMap)Collections.synchronizedMap(new HashMap<ItemStack, ItemStack>());
	}
	
    @Override
    public void operate()
    {
        if (!canOperate())
        {
            return;
        }

        ItemStack itemstack = new ItemStack(getRandomMagicItem());
        
        inventory[0].stackSize--;

        if (inventory[0].stackSize <= 0)
        {
            inventory[0] = null;
        }
        
        inventory[2] = itemstack;
    }

    @Override
    public boolean canOperate()
    {
        if (inventory[0] == null)
        {
            return false;
        }
        
        if(electricityStored < ENERGY_PER_TICK)
        {
        	return false;
        }
        
        if(secondaryEnergyStored < SECONDARY_ENERGY_PER_TICK)
        {
        	return false;
        }

        if (inventory[2] != null)
        {
            return false;
        }
        
        return true;
    }

	@Override
	public int getFuelTicks(ItemStack itemstack)
	{
		if (itemstack.itemID == Item.diamond.itemID) return 1000;
		return 0;
	}
	
    public static Item getRandomMagicItem()
    {
    	Random rand = new Random();
    	int random = rand.nextInt(2);
    	if(random == 0) return Mekanism.Stopwatch;
    	if(random == 1) return Mekanism.WeatherOrb;
    	return Mekanism.EnrichedAlloy;
    }
    
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
}