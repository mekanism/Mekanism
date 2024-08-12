import mods.mekanism.api.ingredient.ChemicalStackIngredient;

//Adds a Chemical Infusing Recipe that uses 1 mB of Hydrogen Chloride and 1 mB of Water Vapor to produce 2 mB of Gaseous Brine.

// <recipetype:mekanism:chemical_infusing>.addRecipe(name as string, leftInput as ChemicalStackIngredient, rightInput as ChemicalStackIngredient, output as ICrTChemicalStack)

<recipetype:mekanism:chemical_infusing>.addRecipe("gaseous_brine", ChemicalStackIngredient.from(<chemical:mekanism:hydrogen_chloride>), ChemicalStackIngredient.from(<chemical:mekanism:water_vapor>), <chemical:mekanism:brine> * 2);
//Alternate implementations of the above recipe are shown commented below. These implementations make use of implicit casting to allow easier calling:
// <recipetype:mekanism:chemical_infusing>.addRecipe("gaseous_brine", <chemical:mekanism:hydrogen_chloride>, ChemicalStackIngredient.from(<chemical:mekanism:water_vapor>), <chemical:mekanism:brine> * 2);
// <recipetype:mekanism:chemical_infusing>.addRecipe("gaseous_brine", ChemicalStackIngredient.from(<chemical:mekanism:hydrogen_chloride>), <chemical:mekanism:water_vapor>, <chemical:mekanism:brine> * 2);
// <recipetype:mekanism:chemical_infusing>.addRecipe("gaseous_brine", <chemical:mekanism:hydrogen_chloride>, <chemical:mekanism:water_vapor>, <chemical:mekanism:brine> * 2);


//Removes the Chemical Infusing Recipe for producing Sulfur Trioxide from Oxygen and Sulfur Dioxide.

// <recipetype:mekanism:chemical_infusing>.removeByName(name as string)

<recipetype:mekanism:chemical_infusing>.removeByName("mekanism:chemical_infusing/sulfur_trioxide");