import mods.mekanism.api.ingredient.ChemicalStackIngredient.GasStackIngredient;
import mods.mekanism.api.ingredient.ItemStackIngredient;

//Adds a Compressing Recipe that compresses Emerald Dust into an Emerald.

// <recipetype:mekanism:compressing>.addRecipe(name as string, itemInput as ItemStackIngredient, chemicalInput as ChemicalStackIngredient, output as IItemStack)

<recipetype:mekanism:compressing>.addRecipe("compress_emerald", ItemStackIngredient.from(<tag:items:forge:dusts/emerald>), GasStackIngredient.from(<gas:mekanism:osmium>), <item:minecraft:emerald>);
//Alternate implementations of the above recipe are shown commented below. These implementations make use of implicit casting to allow easier calling:
// <recipetype:mekanism:compressing>.addRecipe("compress_emerald", <tag:items:forge:dusts/emerald>, GasStackIngredient.from(<gas:mekanism:osmium>), <item:minecraft:emerald>);
// <recipetype:mekanism:compressing>.addRecipe("compress_emerald", ItemStackIngredient.from(<tag:items:forge:dusts/emerald>), <gas:mekanism:osmium>, <item:minecraft:emerald>);
// <recipetype:mekanism:compressing>.addRecipe("compress_emerald", <tag:items:forge:dusts/emerald>, <gas:mekanism:osmium>, <item:minecraft:emerald>);


//Removes the Compressing Recipe that creates Refined Obsidian Ingots.

// <recipetype:mekanism:compressing>.removeByName(name as string)

<recipetype:mekanism:compressing>.removeByName("mekanism:processing/refined_obsidian/ingot/from_dust");