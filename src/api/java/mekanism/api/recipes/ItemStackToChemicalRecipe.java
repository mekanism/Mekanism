package mekanism.api.recipes;

import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.SingleInputRecipe.ItemInputRecipe;

/// Base class for defining ItemStack to chemical recipes.
///
/// Input: ItemStack
///
/// Output: ChemicalStack
///
/// @apiNote There are currently three types of ItemStack to Chemical recipe types:
/// - Oxidizing: Can be processed in a Chemical Oxidizer.
/// - Chemical Conversion: Can be processed by any slots in Mekanism machines that are able to convert items to chemicals, for example in the Osmium Compressor and a
/// variety of other machines.
/// - Pigment Extracting: Can be processed in a Pigment Extractor.
public abstract class ItemStackToChemicalRecipe extends ItemInputRecipe<ChemicalStackTemplate> {
}