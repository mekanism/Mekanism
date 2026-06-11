package mekanism.api.recipes;

import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.SingleInputRecipe.ChemicalInputRecipe;

/**
 * Base class for defining chemical to chemical recipes.
 * <br>
 * Input: Chemical
 * <br>
 * Output: ChemicalStack
 *
 * @apiNote There are currently two types of Chemical to Chemical recipe types:
 * <ul>
 *     <li>Activating: Can be processed in a Solar Neutron Activator.</li>
 *     <li>Centrifuging: Can be processed in an Isotopic Centrifuge.</li>
 * </ul>
 */
public abstract class ChemicalToChemicalRecipe extends ChemicalInputRecipe<ChemicalStackTemplate> {
}