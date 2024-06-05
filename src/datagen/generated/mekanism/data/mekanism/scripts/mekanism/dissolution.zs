import mods.mekanism.api.ingredient.ChemicalStackIngredient.GasStackIngredient;

//Adds a Dissolution Recipe that uses 100 mB of Sulfuric Acid (1 mB per tick) to convert Salt into 10 mB of Hydrogen Chloride.

// <recipetype:mekanism:dissolution>.addRecipe(name as string, itemInput as IIngredientWithAmount, gasInput as GasStackIngredient, output as ICrTChemicalStack)

<recipetype:mekanism:dissolution>.addRecipe("salt_to_hydrogen_chloride", <item:mekanism:salt>, GasStackIngredient.from(<gas:mekanism:sulfuric_acid>), <gas:mekanism:hydrogen_chloride> * 10);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:dissolution>.addRecipe("salt_to_hydrogen_chloride", <item:mekanism:salt>, <gas:mekanism:sulfuric_acid>, <gas:mekanism:hydrogen_chloride> * 10);


/*
 * Removes two Dissolution Recipes:
 * 1) The recipe for producing Hydrofluoric Acid from Fluorite.
 * 2) The recipe for producing Dirty Lead Slurry from Lead Ore.
*/

// <recipetype:mekanism:dissolution>.removeByName(name as string)

<recipetype:mekanism:dissolution>.removeByName("mekanism:processing/uranium/hydrofluoric_acid");
<recipetype:mekanism:dissolution>.removeByName("mekanism:processing/lead/slurry/dirty/from_ore");