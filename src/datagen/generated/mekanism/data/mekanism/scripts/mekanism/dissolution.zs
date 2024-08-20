import mods.mekanism.api.ingredient.ChemicalStackIngredient;

//Adds a Dissolution Recipe that uses 100 mB of Sulfuric Acid (1 mB per tick) to convert Salt into 10 mB of Hydrogen Chloride.

// <recipetype:mekanism:dissolution>.addRecipe(name as string, itemInput as IIngredientWithAmount, chemicalInput as ChemicalStackIngredient, output as ICrTChemicalStack, perTickUsage as bool)

<recipetype:mekanism:dissolution>.addRecipe("salt_to_hydrogen_chloride", <item:mekanism:salt>, ChemicalStackIngredient.from(<chemical:mekanism:sulfuric_acid>), <chemical:mekanism:hydrogen_chloride> * 10, true);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:dissolution>.addRecipe("salt_to_hydrogen_chloride", <item:mekanism:salt>, <chemical:mekanism:sulfuric_acid>, <chemical:mekanism:hydrogen_chloride> * 10, true);


/*
 * Removes two Dissolution Recipes:
 * 1) The recipe for producing Hydrofluoric Acid from Fluorite.
 * 2) The recipe for producing Dirty Lead Slurry from Lead Ore.
*/

// <recipetype:mekanism:dissolution>.removeByName(name as string)

<recipetype:mekanism:dissolution>.removeByName("mekanism:processing/uranium/hydrofluoric_acid");
<recipetype:mekanism:dissolution>.removeByName("mekanism:processing/lead/slurry/dirty/from_ore");