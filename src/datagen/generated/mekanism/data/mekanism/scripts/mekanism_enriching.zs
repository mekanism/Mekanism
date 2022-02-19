import mods.mekanism.api.ingredient.ItemStackIngredient;

//Adds an Enriching Recipe to convert 20 Oak Leaves into an Oak Sapling.

// <recipetype:mekanism:enriching>.addRecipe(name as string, input as ItemStackIngredient, output as IItemStack)

<recipetype:mekanism:enriching>.addRecipe("oak_leaves_to_saplings", ItemStackIngredient.from(<item:minecraft:oak_leaves> * 20), <item:minecraft:oak_sapling>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:enriching>.addRecipe("oak_leaves_to_saplings", <item:minecraft:oak_leaves> * 20, <item:minecraft:oak_sapling>);


//Removes the Enriching Recipe that creates Gold Dust from Gold Ore.

// <recipetype:mekanism:enriching>.removeByName(name as string)

<recipetype:mekanism:enriching>.removeByName("mekanism:processing/gold/dust/from_ore");