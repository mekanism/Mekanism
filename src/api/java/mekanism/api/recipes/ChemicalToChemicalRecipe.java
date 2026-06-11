package mekanism.api.recipes;

import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.SingleInputRecipe.ChemicalInputRecipe;

/// Base class for defining chemical to chemical recipes.
///
/// Input: Chemical
///
/// Output: ChemicalStack
///
/// @apiNote There are currently two types of Chemical to Chemical recipe types:
/// - Activating: Can be processed in a Solar Neutron Activator.
/// - Centrifuging: Can be processed in an Isotopic Centrifuge.
public abstract class ChemicalToChemicalRecipe extends ChemicalInputRecipe<ChemicalStackTemplate> {
}