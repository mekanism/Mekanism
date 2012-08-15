package net.uberkat.obsidian.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.src.*;

public class CrusherRecipes
{
    private static final CrusherRecipes smeltingBase = new CrusherRecipes();

    /** The list of smelting results. */
    private Map smeltingList = new HashMap();
    private Map metaSmeltingList = new HashMap();

    /**
     * Used to call methods addSmelting and getSmeltingResult.
     */
    public static final CrusherRecipes smelting()
    {
        return smeltingBase;
    }

    private CrusherRecipes()
    {
        addSmelting(ObsidianIngots.RedstoneIngot.shiftedIndex, new ItemStack(Item.redstone, 1));
        addSmelting(Item.ingotIron.shiftedIndex, new ItemStack(ObsidianIngots.IronDust, 1));
        addSmelting(Item.ingotGold.shiftedIndex, new ItemStack(ObsidianIngots.GoldDust, 1));
        addSmelting(ObsidianIngots.PlatinumIngot.shiftedIndex, new ItemStack(ObsidianIngots.PlatinumDust, 1));
        addSmelting(ObsidianIngots.GlowstoneIngot.shiftedIndex, new ItemStack(Item.lightStoneDust));
    }

    /**
     * Adds a smelting recipe.
     */
    public void addSmelting(int par1, ItemStack par2ItemStack)
    {
        this.smeltingList.put(Integer.valueOf(par1), par2ItemStack);
    }

    /**
     * Returns the smelting result of an item.
     * Deprecated in favor of a metadata sensitive version
     */
    @Deprecated
    public ItemStack getSmeltingResult(int par1)
    {
        return (ItemStack)this.smeltingList.get(Integer.valueOf(par1));
    }

    public Map getSmeltingList()
    {
        return this.smeltingList;
    }
    
    /**
     * Add a metadata-sensitive furnace recipe
     * @param itemID The Item ID
     * @param metadata The Item Metadata
     * @param itemstack The ItemStack for the result
     */
    public void addSmelting(int itemID, int metadata, ItemStack itemstack)
    {
        metaSmeltingList.put(Arrays.asList(itemID, metadata), itemstack);
    }
    
    /**
     * Used to get the resulting ItemStack form a source ItemStack
     * @param item The Source ItemStack
     * @return The result ItemStack
     */
    public ItemStack getSmeltingResult(ItemStack item) 
    {
        if (item == null)
        {
            return null;
        }
        ItemStack ret = (ItemStack)metaSmeltingList.get(Arrays.asList(item.itemID, item.getItemDamage()));
        if (ret != null) 
        {
            return ret;
        }
        return (ItemStack)smeltingList.get(Integer.valueOf(item.itemID));
    }
}
