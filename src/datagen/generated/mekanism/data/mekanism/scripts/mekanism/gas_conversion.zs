//Adds a Gas Conversion Recipe that allows converting Osmium Nuggets into 22 mB of Osmium.

// <recipetype:mekanism:chemical_conversion>.addRecipe(name as string, input as IIngredientWithAmount, output as ICrTChemicalStack)

<recipetype:mekanism:chemical_conversion>.addRecipe("gas_conversion/osmium_from_nugget", <tag:item:c:nuggets/osmium>, <chemical:mekanism:osmium> * 22);

//Removes the Gas Conversion Recipe that allows converting Osmium Blocks into Osmium.

// <recipetype:mekanism:chemical_conversion>.removeByName(name as string)

<recipetype:mekanism:chemical_conversion>.removeByName("mekanism:chemical_conversion/osmium_from_block");