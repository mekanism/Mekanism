package net.uberkat.obsidian.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.src.*;

public class CombinerRecipes
{
    private static final CombinerRecipes smeltingBase = new CombinerRecipes();

    /** The list of smelting results. */
    private Map smeltingList = new HashMap();
    private Map metaSmeltingList = new HashMap();

    /**
     * Used to call methods addSmelting and getSmeltingResult.
     */
    public static final CombinerRecipes smelting()
    {
        return smeltingBase;
    }

    private CombinerRecipes()
    {
        addSmelting(Item.redstone.shiftedIndex, new ItemStack(Block.oreRedstone, 1));
        addSmelting(ObsidianIngots.ObsidianDust.shiftedIndex, new ItemStack(Block.obsidian, 1));
        addSmelting(ObsidianIngots.IronDust.shiftedIndex, new ItemStack(Block.oreIron, 1));
        addSmelting(ObsidianIngots.GoldDust.shiftedIndex, new ItemStack(Block.oreGold, 1));
        addSmelting(Item.diamond.shiftedIndex, new ItemStack(Block.oreDiamond, 1));
        addSmelting(new ItemStack(Item.dyePowder, 1, 4).getItem().shiftedIndex, new ItemStack(Block.oreLapis, 1));
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
