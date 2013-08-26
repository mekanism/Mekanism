package mekanism.common.tileentity;

import java.util.HashMap;
import java.util.Random;

import mekanism.common.Mekanism;
import mekanism.common.block.BlockMachine.MachineType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityTheoreticalElementizer extends TileEntityAdvancedElectricMachine
{
	public TileEntityTheoreticalElementizer()
	{
		super("Elementizer.ogg", "Theoretical Elementizer", new ResourceLocation("mekanism", "gui/GuiElementizer.png"), Mekanism.theoreticalElementizerUsage, 1, 1000, MachineType.THEORETICAL_ELEMENTIZER.baseEnergy, 1000);
	}
	
	@Override
	public HashMap getRecipes()
	{
		return new HashMap<ItemStack, ItemStack>();
	}
	
    @Override
    public void operate()
    {
        if(!canOperate())
        {
            return;
        }

        ItemStack itemstack = new ItemStack(getRandomMagicItem());
        
        inventory[0].stackSize--;

        if(inventory[0].stackSize <= 0)
        {
            inventory[0] = null;
        }
        
        inventory[2] = itemstack;
    }

    @Override
    public boolean canOperate()
    {
        if(inventory[0] == null)
        {
            return false;
        }

        if(inventory[2] != null)
        {
            return false;
        }
        
        return true;
    }

	@Override
	public int getFuelTicks(ItemStack itemstack)
	{
		if(itemstack.itemID == Item.diamond.itemID) return 1000;
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