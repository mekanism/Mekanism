package railcraft.common.api.crafting;

import java.util.List;
import java.util.Map;
import net.minecraft.src.ItemStack;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public interface IRockCrusherRecipe
{

    public ItemStack getInput();

    /**
     * Returns a map containing each output entry and its chance of being included.
     *
     * @return
     */
    public Map<ItemStack, Float> getOutputs();

    /**
     * Returns a list of all possible outputs.
     * This is basically a condensed version of getOutputs().keySet().
     *
     * @return
     */
    public List<ItemStack> getPossibleOuput();

    /**
     * Returns a list of outputs after it has passed through the randomizer.
     *
     * @return
     */
    public List<ItemStack> getRandomizedOuput();
}
