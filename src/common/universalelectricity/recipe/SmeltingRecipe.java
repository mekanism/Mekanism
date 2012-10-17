package universalelectricity.recipe;

import net.minecraft.src.ItemStack;

public class SmeltingRecipe
{
    public ItemStack input;
    public ItemStack output;

    public SmeltingRecipe(ItemStack input, ItemStack output)
    {
        this.input = input;
        this.output = output;
    }
    
    public boolean isEqual(SmeltingRecipe comparingRecipe)
    {
    	if(this.input == comparingRecipe.input && this.output == comparingRecipe.output)
    	{
    		return true;
    	}
    	
    	return false;
    }
}
