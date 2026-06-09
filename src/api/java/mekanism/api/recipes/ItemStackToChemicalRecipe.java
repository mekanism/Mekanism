package mekanism.api.recipes;

import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.SingleInputRecipe.ItemInputRecipe;

/**
 * Base class for defining ItemStack to chemical recipes.
 * <br>
 * Input: ItemStack
 * <br>
 * Output: ChemicalStack
 *
 * @apiNote There are currently three types of ItemStack to Chemical recipe types:
 *  <ul>
 *  <li>Oxidizing: Can be processed in a Chemical Oxidizer.</li>
 *  <li>Chemical Conversion: Can be processed by any slots in Mekanism machines that are able to convert items to chemicals, for example in the Osmium Compressor and a variety of other machines.</li>
 *  <li>Pigment Extracting: Can be processed in a Pigment Extractor.</li>
 * </ul>
 */
public abstract class ItemStackToChemicalRecipe extends ItemInputRecipe<ChemicalStackTemplate> {
}