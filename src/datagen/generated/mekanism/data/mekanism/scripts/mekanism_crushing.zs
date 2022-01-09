import mods.mekanism.api.ingredient.ItemStackIngredient;

//Adds a Crushing Recipe to crush Brick Blocks into four Bricks.

// <recipetype:mekanism:crushing>.addRecipe(name as string, input as ItemStackIngredient, output as IItemStack)

<recipetype:mekanism:crushing>.addRecipe("crush_bricks", ItemStackIngredient.from(<item:minecraft:bricks>), <item:minecraft:brick> * 4);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:crushing>.addRecipe("crush_bricks", <item:minecraft:bricks>, <item:minecraft:brick> * 4);


//Removes the Crushing Recipe that produces String from Wool.

// <recipetype:mekanism:crushing>.removeByName(name as string)

<recipetype:mekanism:crushing>.removeByName("mekanism:crushing/wool_to_string");