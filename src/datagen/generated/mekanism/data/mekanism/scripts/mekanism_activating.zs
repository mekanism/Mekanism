import mods.mekanism.api.ingredient.ChemicalStackIngredient.GasStackIngredient;

//Adds an Activating Recipe that converts 1 mB of Water Vapor to 1 mB of Gaseous Brine.

// <recipetype:mekanism:activating>.addRecipe(name as string, input as GasStackIngredient, output as ICrTGasStack)

<recipetype:mekanism:activating>.addRecipe("activate_water_vapor", GasStackIngredient.from(<gas:mekanism:water_vapor>), <gas:mekanism:brine>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:activating>.addRecipe("activate_water_vapor", <gas:mekanism:water_vapor>, <gas:mekanism:brine>);


//Removes the Activating Recipe for producing Polonium from Nuclear Waste.

// <recipetype:mekanism:activating>.removeByName(name as string)

<recipetype:mekanism:activating>.removeByName("mekanism:processing/lategame/polonium");