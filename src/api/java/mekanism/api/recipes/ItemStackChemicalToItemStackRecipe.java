package mekanism.api.recipes;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for defining item chemical to item recipes.
 * <br>
 * Input: ItemStack
 * <br>
 * Input: Chemical
 * <br>
 * Output: ItemStack
 *
 * @apiNote There are currently six types of ItemStack Chemical to ItemStack recipe types:
 * <ul>
 *     <li>Compressing: Can be processed in Osmium Compressors and Compressing Factories.</li>
 *     <li>Injecting: Can be processed in Chemical Injection Chambers and Injecting Factories.</li>
 *     <li>Purifying: Can be processed in Purification Chambers and Purifying Factories.</li>
 *     <li>Infusing: Can be processed in Metallurgic Infusers and Infusing Factories.</li>
 *     <li>Painting: Can be processed in Painting Machines.</li>
 *     <li>Nucleosynthesizing: Can be processed in the Antiprotonic Nucleosynthesizer.</li>
 * </ul>
 */
@NothingNullByDefault
public abstract class ItemStackChemicalToItemStackRecipe extends ItemStackChemicalToObjectRecipe<ItemStackTemplate> {

    @NotNull
    @Override
    public ItemStack assemble(SingleItemChemicalRecipeInput input) {
        if (!isIncomplete() && test(input.item(), input.chemical())) {
            return getOutput(input.item(), input.chemical()).create();
        }
        return ItemStack.EMPTY;
    }
}