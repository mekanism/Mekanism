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
	
    public void updateEntity()
    {
    	BlockTheoreticalElementizer.updateTexture(worldObj, xCoord, yCoord, zCoord);
    	
        boolean var1 = machineBurnTime > 0;
        boolean var2 = false;

        if(machineBurnTime > 0)
        {
        	isActive = true;
        }
        else if(machineBurnTime == 0 && !canSmelt())
        {
        	isActive = false;
        }
        
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
                BlockTheoreticalElementizer.updateBlock(machineBurnTime > 0, worldObj, xCoord, yCoord, zCoord);
            }
        }

        if (var2)
        {
            onInventoryChanged();
        }
        worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
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
    
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        NBTTagList var2 = par1NBTTagCompound.getTagList("Items");
        machineItemStacks = new ItemStack[getSizeInventory()];

        for (int var3 = 0; var3 < var2.tagCount(); ++var3)
        {
            NBTTagCompound var4 = (NBTTagCompound)var2.tagAt(var3);
            byte var5 = var4.getByte("Slot");

            if (var5 >= 0 && var5 < machineItemStacks.length)
            {
                machineItemStacks[var5] = ItemStack.loadItemStackFromNBT(var4);
            }
        }

        machineBurnTime = par1NBTTagCompound.getShort("BurnTime");
        machineCookTime = par1NBTTagCompound.getShort("CookTime");
        currentItemBurnTime = getItemBurnTime(machineItemStacks[1]);
    }
    
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setShort("BurnTime", (short)machineBurnTime);
        par1NBTTagCompound.setShort("CookTime", (short)machineCookTime);
        NBTTagList var2 = new NBTTagList();

        for (int var3 = 0; var3 < machineItemStacks.length; ++var3)
        {
            if (machineItemStacks[var3] != null)
            {
                NBTTagCompound var4 = new NBTTagCompound();
                var4.setByte("Slot", (byte)var3);
                machineItemStacks[var3].writeToNBT(var4);
                var2.appendTag(var4);
            }
        }

        par1NBTTagCompound.setTag("Items", var2);
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
