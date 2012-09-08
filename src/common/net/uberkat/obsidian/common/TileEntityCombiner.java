package net.uberkat.obsidian.common;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import net.minecraft.src.*;

public class TileEntityCombiner extends TileEntityMachine
{
	public TileEntityCombiner()
	{
		super(200, "Combiner");
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
        else
        {
            ItemStack var1 = CombinerRecipes.smelting().getSmeltingResult(machineItemStacks[0]);
            if (var1 == null) return false;
            if (machineItemStacks[2] == null) return true;
            if (!machineItemStacks[2].isItemEqual(var1)) return false;
            int result = machineItemStacks[2].stackSize + var1.stackSize;
            return (result <= getInventoryStackLimit() && result <= var1.getMaxStackSize());
        }
    }

    public void smeltItem()
    {
        if (canSmelt())
        {
            ItemStack var1 = CombinerRecipes.smelting().getSmeltingResult(machineItemStacks[0]);

            if (machineItemStacks[2] == null)
            {
                machineItemStacks[2] = var1.copy();
            }
            else if (machineItemStacks[2].isItemEqual(var1))
            {
                machineItemStacks[2].stackSize += var1.stackSize;
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
            if (par1ItemStack.getItem() instanceof ItemBlock && var1 == Block.cobblestone.blockID) return 200;
        }
        return 0;
    }

    public static boolean isItemFuel(ItemStack par0ItemStack)
    {
        return getItemBurnTime(par0ItemStack) > 0;
    }
}
