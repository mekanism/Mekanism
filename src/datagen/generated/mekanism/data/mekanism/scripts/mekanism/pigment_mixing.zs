import mods.mekanism.api.ingredient.ChemicalStackIngredient;

//Adds a Pigment Mixing Recipe that mixes 1 mB of White Pigment with 4 mB of Dark Red Pigment to produce 5 mB of Red Pigment.

// <recipetype:mekanism:pigment_mixing>.addRecipe(name as string, leftInput as ChemicalStackIngredient, rightInput as ChemicalStackIngredient, output as ICrTChemicalStack)

<recipetype:mekanism:pigment_mixing>.addRecipe("pigment_mixing/white_dark_red_to_red", ChemicalStackIngredient.from(<chemical:mekanism:white>), ChemicalStackIngredient.from(<chemical:mekanism:dark_red> * 4), <chemical:mekanism:red> * 5);
//Alternate implementations of the above recipe are shown commented below. These implementations make use of implicit casting to allow easier calling:
// <recipetype:mekanism:pigment_mixing>.addRecipe("pigment_mixing/white_dark_red_to_red", <chemical:mekanism:white>, ChemicalStackIngredient.from(<chemical:mekanism:dark_red> * 4), <chemical:mekanism:red> * 5);
// <recipetype:mekanism:pigment_mixing>.addRecipe("pigment_mixing/white_dark_red_to_red", ChemicalStackIngredient.from(<chemical:mekanism:white>), <chemical:mekanism:dark_red> * 4, <chemical:mekanism:red> * 5);
// <recipetype:mekanism:pigment_mixing>.addRecipe("pigment_mixing/white_dark_red_to_red", <chemical:mekanism:white>, <chemical:mekanism:dark_red> * 4, <chemical:mekanism:red> * 5);


//Removes the Pigment Mixing Recipe that produces Dark Red Pigment from Black and Red Pigment.

// <recipetype:mekanism:pigment_mixing>.removeByName(name as string)

<recipetype:mekanism:pigment_mixing>.removeByName("mekanism:pigment_mixing/black_red_to_dark_red");