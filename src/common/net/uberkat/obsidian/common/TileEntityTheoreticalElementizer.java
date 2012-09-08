package net.uberkat.obsidian.common;

import java.util.Random;

import net.minecraftforge.common.ISidedInventory;
import net.minecraftforge.common.ForgeDirection;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import net.minecraft.src.*;

public class TileEntityTheoreticalElementizer extends TileEntityMachine
{
	public TileEntityTheoreticalElementizer()
	{
		super(1000, "Theoretical Elementizer");
	}
	
    public void onUpdate()
    {
        boolean var1 = machineBurnTime > 0;
        boolean var2 = false;

        if (machineBurnTime > 0)
        {
            --machineBurnTime;
        }

        if (!worldObj.isRemote)
        {
            if (machineBurnTime == 0 && canSmelt())
            {
                currentItemBurnTime = machineBurnTime = getItemBurnTime(machineItemStacks[1]);

                if (machineBurnTime > 0)
                {
                    var2 = true;

                    if (machineItemStacks[1] != null)
                    {
                        --machineItemStacks[1].stackSize;

                        if (machineItemStacks[1].stackSize == 0)
                        {
                            machineItemStacks[1] = null;
                        }
                    }
                }
            }

            if (isBurning() && canSmelt())
            {
                ++machineCookTime;

                if (machineCookTime == maxBurnTime)
                {
                    machineCookTime = 0;
                    smeltItem();
                    var2 = true;
                }
            }
            else
            {
                machineCookTime = 0;
            }

            if (var1 != machineBurnTime > 0)
            {
                var2 = true;
                setActive(isBurning());
            }
        }

        if (var2)
        {
            onInventoryChanged();
        }
    }
    
    public boolean canSmelt()
    {
        if (machineItemStacks[0] == null)
        {
            return false;
        }
        else if(machineItemStacks[2] != null)
        {
        	return false;
        }
        else
        {
            if (machineItemStacks[0].getItem().shiftedIndex != ObsidianIngots.EnrichedAlloy.shiftedIndex) return false;
            if (machineItemStacks[2] == null) return true;
        }
        return false;
    }

    public void smeltItem()
    {
        if (canSmelt())
        {
            ItemStack itemstack = new ItemStack(getRandomMagicItem(), 1);

            if (machineItemStacks[2] == null)
            {
                machineItemStacks[2] = itemstack.copy();
            }

            --machineItemStacks[0].stackSize;

            if (machineItemStacks[0].stackSize <= 0)
            {
                machineItemStacks[0] = null;
            }
        }
    }

    public static int getItemBurnTime(ItemStack par1ItemStack)
    {
        if (par1ItemStack == null)
        {
            return 0;
        }
        else
        {
            int var1 = par1ItemStack.getItem().shiftedIndex;
            if (var1 == Item.diamond.shiftedIndex) return 1000;
        }
        return 0;
    }
    
    public static boolean isItemFuel(ItemStack par0ItemStack)
    {
        return getItemBurnTime(par0ItemStack) > 0;
    }
    
    public Item getRandomMagicItem()
    {
    	Random rand = new Random();
    	int random = rand.nextInt(3);
    	if(random == 0) return ObsidianIngots.LightningRod;
    	if(random == 1) return ObsidianIngots.Stopwatch;
    	if(random == 2) return ObsidianIngots.WeatherOrb;
    	return ObsidianIngots.EnrichedAlloy;
    }
}
