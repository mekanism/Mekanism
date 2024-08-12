import mods.mekanism.api.ingredient.ChemicalStackIngredient;

//Adds a Centrifuging Recipe that converts 1 mB of Gaseous Brine into 1 mB of Hydrogen Chloride.

// <recipetype:mekanism:centrifuging>.addRecipe(name as string, input as ChemicalStackIngredient, output as ICrTChemicalStack)

<recipetype:mekanism:centrifuging>.addRecipe("centrifuge_brine", ChemicalStackIngredient.from(<chemical:mekanism:brine>), <chemical:mekanism:hydrogen_chloride>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:centrifuging>.addRecipe("centrifuge_brine", <chemical:mekanism:brine>, <chemical:mekanism:hydrogen_chloride>);


//Removes the Centrifuging Recipe for producing Plutonium from Nuclear Waste.

// <recipetype:mekanism:centrifuging>.removeByName(name as string)

<recipetype:mekanism:centrifuging>.removeByName("mekanism:processing/lategame/plutonium");