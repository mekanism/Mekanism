//Adds a Gas Conversion Recipe that allows converting Osmium Nuggets into 22 mB of Osmium.

// <recipetype:mekanism:gas_conversion>.addRecipe(name as string, input as IIngredientWithAmount, output as ICrTGasStack)

<recipetype:mekanism:gas_conversion>.addRecipe("gas_conversion/osmium_from_nugget", <tag:item:c:nuggets/osmium>, <gas:mekanism:osmium> * 22);

//Removes the Gas Conversion Recipe that allows converting Osmium Blocks into Osmium.

// <recipetype:mekanism:gas_conversion>.removeByName(name as string)

<recipetype:mekanism:gas_conversion>.removeByName("mekanism:gas_conversion/osmium_from_block");