package railcraft.common.api.crafting;

import java.util.HashMap;
import java.util.List;
import net.minecraft.src.ItemStack;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public interface IRockCrusherCraftingManager
{

    /**
     *
     * @param inputId
     * @param inputDamage metadata or -1 for wildcard
     * @param output A map of outputs and chances. If more than 9 types of items, there will be unexpected behavior.
     */
    void addRecipe(int inputId, int inputDamage, HashMap<ItemStack, Float> output);

    /**
     *
     * @param input
     * @param output A map of outputs and chances.  If more than 9 types of items, there will be unexpected behavior.
     */
    void addRecipe(ItemStack input, HashMap<ItemStack, Float> output);

    IRockCrusherRecipe getRecipe(ItemStack input);

    IRockCrusherRecipe getRecipe(int inputId, int inputDamage);

    List<IRockCrusherRecipe> getRecipes();

}
