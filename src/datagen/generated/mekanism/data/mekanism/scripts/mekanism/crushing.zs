//Adds a Crushing Recipe to crush Brick Blocks into four Bricks.

// <recipetype:mekanism:crushing>.addRecipe(name as string, input as IIngredientWithAmount, output as IItemStack)

<recipetype:mekanism:crushing>.addRecipe("crush_bricks", <item:minecraft:bricks>, <item:minecraft:brick> * 4);

//Removes the Crushing Recipe that produces String from Wool.

// <recipetype:mekanism:crushing>.removeByName(name as string)

<recipetype:mekanism:crushing>.removeByName("mekanism:crushing/wool_to_string");