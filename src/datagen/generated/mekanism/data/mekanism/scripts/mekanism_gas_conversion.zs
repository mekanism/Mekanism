import mods.mekanism.api.ingredient.ItemStackIngredient;

//Adds a Gas Conversion Recipe that allows converting Osmium Nuggets into 22 mB of Osmium.

// <recipetype:mekanism:gas_conversion>.addRecipe(name as string, input as ItemStackIngredient, output as ICrTGasStack)

<recipetype:mekanism:gas_conversion>.addRecipe("gas_conversion/osmium_from_nugget", ItemStackIngredient.from(<tag:items:forge:nuggets/osmium>), <gas:mekanism:osmium> * 22);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:gas_conversion>.addRecipe("gas_conversion/osmium_from_nugget", <tag:items:forge:nuggets/osmium>, <gas:mekanism:osmium> * 22);


//Removes the Gas Conversion Recipe that allows converting Osmium Blocks into Osmium.

// <recipetype:mekanism:gas_conversion>.removeByName(name as string)

<recipetype:mekanism:gas_conversion>.removeByName("mekanism:gas_conversion/osmium_from_block");