//Adds an Oxidizing Recipe that allows converting Salt Blocks into 60 mB of Gaseous Brine.

// <recipetype:mekanism:oxidizing>.addRecipe(name as string, input as IIngredientWithAmount, output as ICrTChemicalStack)

<recipetype:mekanism:oxidizing>.addRecipe("oxidize_salt_block", <item:mekanism:block_salt>, <chemical:mekanism:brine> * 60);

//Removes the Oxidizing Recipe that allows Sulfur Dioxide from Sulfur Dust.

// <recipetype:mekanism:oxidizing>.removeByName(name as string)

<recipetype:mekanism:oxidizing>.removeByName("mekanism:oxidizing/sulfur_dioxide");