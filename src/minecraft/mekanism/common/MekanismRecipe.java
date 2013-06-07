package mekanism.common;

import java.util.ArrayList;
import java.util.HashMap;

import mekanism.api.IEnergizedItem;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Code originally from Eloraam and her work on the Ore Dictionary.  Cleaned up and modified to work well with energized items.
 * @author Eloraam, aidancbrady
 *
 */
public class MekanismRecipe implements IRecipe
{
    private static final int MAX_CRAFT_GRID_WIDTH = 3;
    private static final int MAX_CRAFT_GRID_HEIGHT = 3;

    private ItemStack output = null;
    private Object[] input = null;
    public int width = 0;
    public int height = 0;
    private boolean mirrored = true;
    
    public MekanismRecipe(ItemStack result, Object... recipe)
    {
        output = result.copy();

        String shape = "";
        int idx = 0;

        if(recipe[idx] instanceof Boolean)
        {
            mirrored = (Boolean)recipe[idx];
            
            if(recipe[idx+1] instanceof Object[])
            {
                recipe = (Object[])recipe[idx+1];
            }
            else {
                idx = 1;
            }
        }

        if(recipe[idx] instanceof String[])
        {
            String[] parts = ((String[])recipe[idx++]);

            for(String s : parts)
            {
                width = s.length();
                shape += s;
            }

            height = parts.length;
        }
        else {
            while(recipe[idx] instanceof String)
            {
                String s = (String)recipe[idx++];
                shape += s;
                width = s.length();
                height++;
            }
        }

        if(width * height != shape.length())
        {
            String ret = "Invalid shaped ore recipe: ";
            
            for(Object tmp :  recipe)
            {
                ret += tmp + ", ";
            }
            
            ret += output;
            
            throw new RuntimeException(ret);
        }

        HashMap<Character, Object> itemMap = new HashMap<Character, Object>();

        for(; idx < recipe.length; idx += 2)
        {
            Character chr = (Character)recipe[idx];
            Object in = recipe[idx + 1];

            if(in instanceof ItemStack)
            {
                itemMap.put(chr, ((ItemStack)in).copy());
            }
            else if(in instanceof Item)
            {
                itemMap.put(chr, new ItemStack((Item)in));
            }
            else if(in instanceof Block)
            {
                itemMap.put(chr, new ItemStack((Block)in));
            }
            else if(in instanceof String)
            {
                itemMap.put(chr, OreDictionary.getOres((String)in));
            }
            else {
                String ret = "Invalid shaped ore recipe: ";
                
                for(Object tmp :  recipe)
                {
                    ret += tmp + ", ";
                }
                
                ret += output;
                throw new RuntimeException(ret);
            }
        }

        input = new Object[width * height];
        int x = 0;
        
        for(char chr : shape.toCharArray())
        {
            input[x++] = itemMap.get(chr);
        }
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
    	ItemStack toReturn = output.copy();
    	
    	if(toReturn.getItem() instanceof IEnergizedItem)
    	{
    		double energyFound = 0;
    		
    		for(ItemStack itemstack : inv.stackList)
    		{
    			if(itemstack != null)
    			{
	    			if(itemstack.getItem() instanceof IEnergizedItem)
	    			{
	    				energyFound += ((IEnergizedItem)itemstack.getItem()).getEnergy(itemstack);
	    			}
    			}
    		}
    		
    		((IEnergizedItem)toReturn.getItem()).setEnergy(toReturn, Math.min(((IEnergizedItem)toReturn.getItem()).getMaxEnergy(toReturn), energyFound));
    	}
    	
    	return toReturn;
    }

    @Override
    public int getRecipeSize()
    { 
    	return input.length;
    }

    @Override
    public ItemStack getRecipeOutput()
    { 
    	return output; 
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world)
    {
        for(int x = 0; x <= MAX_CRAFT_GRID_WIDTH - width; x++)
        {
            for(int y = 0; y <= MAX_CRAFT_GRID_HEIGHT - height; ++y)
            {
                if(checkMatch(inv, x, y, true))
                {
                    return true;
                }

                if(mirrored && checkMatch(inv, x, y, false))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkMatch(InventoryCrafting inv, int startX, int startY, boolean mirror)
    {
        for(int x = 0; x < MAX_CRAFT_GRID_WIDTH; x++)
        {
            for(int y = 0; y < MAX_CRAFT_GRID_HEIGHT; y++)
            {
                int subX = x - startX;
                int subY = y - startY;
                Object target = null;

                if(subX >= 0 && subY >= 0 && subX < width && subY < height)
                {
                    if(mirror)
                    {
                        target = input[width - subX - 1 + subY * width];
                    }
                    else {
                        target = input[subX + subY * width];
                    }
                }

                ItemStack slot = inv.getStackInRowAndColumn(x, y);

                if(target instanceof ItemStack)
                {
                    if(!checkItemEquals((ItemStack)target, slot))
                    {
                        return false;
                    }
                }
                else if(target instanceof ArrayList)
                {
                    boolean matched = false;

                    for(ItemStack item : (ArrayList<ItemStack>)target)
                    {
                        matched = matched || checkItemEquals(item, slot);
                    }

                    if(!matched)
                    {
                        return false;
                    }
                }
                else if(target == null && slot != null)
                {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkItemEquals(ItemStack target, ItemStack input)
    {
        if(input == null && target != null || input != null && target == null)
        {
        	System.out.println("False");
            return false;
        }
        else if(input == null && target == null)
        {
        	System.out.println("True");
        	return true;
        }
        
        if(target.itemID != input.itemID)
        {
        	System.out.println("False");
        	return false;
        }
        
        if(!(target.getItem() instanceof IEnergizedItem) && !(input.getItem() instanceof IEnergizedItem))
        {
	        if(target.getItemDamage() != input.getItemDamage())
	        {
	        	System.out.println("False");
	        	return false;
	        }
        }
        else {
        	if(((IEnergizedItem)target.getItem()).isMetadataSpecific() && ((IEnergizedItem)input.getItem()).isMetadataSpecific())
        	{
        		if(target.getItemDamage() != input.getItemDamage())
        		{
        			System.out.println("False");
        			return false;
        		}
        	}
        	
            if(target.getItem() instanceof IEnergyCube && input.getItem() instanceof IEnergyCube)
            {
            	if(((IEnergyCube)target.getItem()).getEnergyCubeTier(target) != ((IEnergyCube)input.getItem()).getEnergyCubeTier(input))
            	{
            		System.out.println("False");
            		return false;
            	}
            }
            else if(target.getItem() instanceof IFactory && input.getItem() instanceof IFactory)
            {
            	if(((IFactory)target.getItem()).isFactory(target) && ((IFactory)input.getItem()).isFactory(input))
            	{
            		if(((IFactory)target.getItem()).getRecipeType(target) != ((IFactory)input.getItem()).getRecipeType(input))
            		{
            			System.out.println("False");
            			return false;
            		}
            	}
            }
        }
        
        System.out.println("True");
        return true;
    }

    public MekanismRecipe setMirrored(boolean mirror)
    {
        mirrored = mirror;
        return this;
    }

    public Object[] getInput()
    {
        return input;
    }
}
