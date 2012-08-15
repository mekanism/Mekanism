package net.uberkat.obsidian.common;

import net.minecraft.src.*;

public class ContainerCombiner extends Container
{
    private TileEntityCombiner combiner;
    private int lastCookTime = 0;
    private int lastBurnTime = 0;
    private int lastItemBurnTime = 0;

    public ContainerCombiner(InventoryPlayer par1InventoryPlayer, TileEntityCombiner par2TileEntityCombiner)
    {
        this.combiner = par2TileEntityCombiner;
        this.addSlotToContainer(new Slot(par2TileEntityCombiner, 0, 56, 17));
        this.addSlotToContainer(new Slot(par2TileEntityCombiner, 1, 56, 53));
        this.addSlotToContainer(new SlotObsidian(par1InventoryPlayer.player, par2TileEntityCombiner, 2, 116, 35));
        int var3;

        for (var3 = 0; var3 < 3; ++var3)
        {
            for (int var4 = 0; var4 < 9; ++var4)
            {
                this.addSlotToContainer(new Slot(par1InventoryPlayer, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
            }
        }

        for (var3 = 0; var3 < 9; ++var3)
        {
            this.addSlotToContainer(new Slot(par1InventoryPlayer, var3, 8 + var3 * 18, 142));
        }
    }

    /**
     * Updates crafting matrix; called from onCraftMatrixChanged. Args: none
     */
    public void updateCraftingResults()
    {
        super.updateCraftingResults();

        for (int var1 = 0; var1 < this.crafters.size(); ++var1)
        {
            ICrafting var2 = (ICrafting)this.crafters.get(var1);

            if (this.lastCookTime != this.combiner.combinerCookTime)
            {
                var2.updateCraftingInventoryInfo(this, 0, this.combiner.combinerCookTime);
            }

            if (this.lastBurnTime != this.combiner.combinerBurnTime)
            {
                var2.updateCraftingInventoryInfo(this, 1, this.combiner.combinerBurnTime);
            }

            if (this.lastItemBurnTime != this.combiner.currentItemBurnTime)
            {
                var2.updateCraftingInventoryInfo(this, 2, this.combiner.currentItemBurnTime);
            }
        }

        this.lastCookTime = this.combiner.combinerCookTime;
        this.lastBurnTime = this.combiner.combinerBurnTime;
        this.lastItemBurnTime = this.combiner.currentItemBurnTime;
    }

    public void updateProgressBar(int par1, int par2)
    {
        if (par1 == 0)
        {
            this.combiner.combinerCookTime = par2;
        }

        if (par1 == 1)
        {
            this.combiner.combinerBurnTime = par2;
        }

        if (par1 == 2)
        {
            this.combiner.currentItemBurnTime = par2;
        }
    }

    public boolean canInteractWith(EntityPlayer par1EntityPlayer)
    {
        return this.combiner.isUseableByPlayer(par1EntityPlayer);
    }

    /**
     * Called to transfer a stack from one inventory to the other eg. when shift clicking.
     */
    public ItemStack transferStackInSlot(int par1)
    {
        ItemStack var2 = null;
        Slot var3 = (Slot)this.inventorySlots.get(par1);

        if (var3 != null && var3.getHasStack())
        {
            ItemStack var4 = var3.getStack();
            var2 = var4.copy();

            if (par1 == 2)
            {
                if (!this.mergeItemStack(var4, 3, 39, true))
                {
                    return null;
                }

                var3.onSlotChange(var4, var2);
            }
            else if (par1 != 1 && par1 != 0)
            {
                if (CombinerRecipes.smelting().getSmeltingResult(var4) != null)
                {
                    if (!this.mergeItemStack(var4, 0, 1, false))
                    {
                        return null;
                    }
                }
                else if (TileEntityCombiner.isItemFuel(var4))
                {
                    if (!this.mergeItemStack(var4, 1, 2, false))
                    {
                        return null;
                    }
                }
                else if (par1 >= 3 && par1 < 30)
                {
                    if (!this.mergeItemStack(var4, 30, 39, false))
                    {
                        return null;
                    }
                }
                else if (par1 >= 30 && par1 < 39 && !this.mergeItemStack(var4, 3, 30, false))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(var4, 3, 39, false))
            {
                return null;
            }

            if (var4.stackSize == 0)
            {
                var3.putStack((ItemStack)null);
            }
            else
            {
                var3.onSlotChanged();
            }

            if (var4.stackSize == var2.stackSize)
            {
                return null;
            }

            var3.onPickupFromSlot(var4);
        }

        return var2;
    }
}
