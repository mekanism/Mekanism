package net.uberkat.obsidian.common;

import java.util.List;
import java.util.Vector;

import cpw.mods.fml.common.FMLCommonHandler;

import net.minecraftforge.common.ISidedInventory;
import net.minecraftforge.common.ForgeDirection;
import net.minecraft.src.*;

public class TileEntityEnrichmentChamber extends TileEntityMachine
{
	public static List recipes = new Vector();
	
	public TileEntityEnrichmentChamber()
	{
		super(200, "Enrichment Chamber");
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
                currentItemBurnTime = machineBurnTime = getItemBurnTime(inventory[1]);

                if (machineBurnTime > 0)
                {
                    var2 = true;

                    if (inventory[1] != null)
                    {
                        --inventory[1].stackSize;

                        if (inventory[1].stackSize == 0)
                        {
                            inventory[1] = null;
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
        if (inventory[0] == null)
        {
            return false;
        }

        ItemStack itemstack = MachineRecipes.getOutput(inventory[0], false, recipes);

        if (itemstack == null)
        {
            return false;
        }

        if (inventory[2] == null)
        {
            return true;
        }

        if (!inventory[2].isItemEqual(itemstack))
        {
            return false;
        }
        else
        {
            return inventory[2].stackSize + itemstack.stackSize <= inventory[2].getMaxStackSize();
        }
    }

    public void smeltItem()
    {
        if (!canSmelt())
        {
            return;
        }

        ItemStack itemstack;

        if (inventory[0].getItem().hasContainerItem())
        {
            itemstack = MachineRecipes.getOutput(inventory[0], false, recipes).copy();
            inventory[0] = new ItemStack(inventory[0].getItem().getContainerItem());
        }
        else
        {
            itemstack = MachineRecipes.getOutput(inventory[0], true, recipes).copy();
        }

        if (inventory[0].stackSize <= 0)
        {
            inventory[0] = null;
        }

        if (inventory[2] == null)
        {
            inventory[2] = itemstack;
        }
        else
        {
            inventory[2].stackSize += itemstack.stackSize;
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
            if (var1 == Item.redstone.shiftedIndex) return 600;
        }
        return 0;
    }

    public static boolean isItemFuel(ItemStack par0ItemStack)
    {
        return getItemBurnTime(par0ItemStack) > 0;
    }
}
