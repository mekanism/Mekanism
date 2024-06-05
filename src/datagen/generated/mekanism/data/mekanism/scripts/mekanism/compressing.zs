import mods.mekanism.api.ingredient.ChemicalStackIngredient.GasStackIngredient;

//Adds a Compressing Recipe that compresses Emerald Dust into an Emerald.

// <recipetype:mekanism:compressing>.addRecipe(name as string, itemInput as IIngredientWithAmount, chemicalInput as GasStackIngredient, output as IItemStack)

<recipetype:mekanism:compressing>.addRecipe("compress_emerald", <tag:items:c:dusts/emerald>, GasStackIngredient.from(<gas:mekanism:osmium>), <item:minecraft:emerald>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:compressing>.addRecipe("compress_emerald", <tag:items:c:dusts/emerald>, <gas:mekanism:osmium>, <item:minecraft:emerald>);


//Removes the Compressing Recipe that creates Refined Obsidian Ingots.

// <recipetype:mekanism:compressing>.removeByName(name as string)

<recipetype:mekanism:compressing>.removeByName("mekanism:processing/refined_obsidian/ingot/from_dust");