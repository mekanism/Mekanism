package universalelectricity.recipe;

import net.minecraft.src.ItemStack;

public class CraftingRecipe
{
    public ItemStack output;
    public Object[] input;

    public CraftingRecipe(ItemStack output, Object[] input)
    {
        this.output = output;
        this.input = input;
    }
    
    public boolean isEqual(CraftingRecipe comparingRecipe)
    {
    	if(this.input == comparingRecipe.input && this.output == comparingRecipe.output)
    	{
    		return true;
    	}
    	
    	return false;
    }
}
