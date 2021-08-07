import mods.mekanism.api.ingredient.ChemicalStackIngredient.GasStackIngredient;

//Adds a Chemical Infusing Recipe that uses 1 mB of Hydrogen Chloride and 1 mB of Water Vapor to produce 2 mB of Gaseous Brine.

// <recipetype:mekanism:chemical_infusing>.addRecipe(name as string, leftInput as GasStackIngredient, rightInput as GasStackIngredient, output as ICrTGasStack)

<recipetype:mekanism:chemical_infusing>.addRecipe("gaseous_brine", GasStackIngredient.from(<gas:mekanism:hydrogen_chloride>), GasStackIngredient.from(<gas:mekanism:water_vapor>), <gas:mekanism:brine> * 2);
//Alternate implementations of the above recipe are shown commented below. These implementations make use of implicit casting to allow easier calling:
// <recipetype:mekanism:chemical_infusing>.addRecipe("gaseous_brine", <gas:mekanism:hydrogen_chloride>, GasStackIngredient.from(<gas:mekanism:water_vapor>), <gas:mekanism:brine> * 2);
// <recipetype:mekanism:chemical_infusing>.addRecipe("gaseous_brine", GasStackIngredient.from(<gas:mekanism:hydrogen_chloride>), <gas:mekanism:water_vapor>, <gas:mekanism:brine> * 2);
// <recipetype:mekanism:chemical_infusing>.addRecipe("gaseous_brine", <gas:mekanism:hydrogen_chloride>, <gas:mekanism:water_vapor>, <gas:mekanism:brine> * 2);


//Removes the Chemical Infusing Recipe for producing Sulfur Trioxide from Oxygen and Sulfur Dioxide.

// <recipetype:mekanism:chemical_infusing>.removeByName(name as string)

<recipetype:mekanism:chemical_infusing>.removeByName("mekanism:chemical_infusing/sulfur_trioxide");