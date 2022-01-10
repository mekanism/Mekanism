import mods.mekanism.api.ingredient.ChemicalStackIngredient.PigmentStackIngredient;
import mods.mekanism.api.ingredient.ItemStackIngredient;

//Adds a Painting Recipe that uses 256 mB Red Pigment to convert Clear Sand into Red Sand.

// <recipetype:mekanism:painting>.addRecipe(name as string, itemInput as ItemStackIngredient, chemicalInput as ChemicalStackIngredient, output as IItemStack)

<recipetype:mekanism:painting>.addRecipe("paint_sand", ItemStackIngredient.from(<tag:items:forge:sand/colorless>), PigmentStackIngredient.from(<pigment:mekanism:red> * 256), <item:minecraft:red_sand>);
//Alternate implementations of the above recipe are shown commented below. These implementations make use of implicit casting to allow easier calling:
// <recipetype:mekanism:painting>.addRecipe("paint_sand", <tag:items:forge:sand/colorless>, PigmentStackIngredient.from(<pigment:mekanism:red> * 256), <item:minecraft:red_sand>);
// <recipetype:mekanism:painting>.addRecipe("paint_sand", ItemStackIngredient.from(<tag:items:forge:sand/colorless>), <pigment:mekanism:red> * 256, <item:minecraft:red_sand>);
// <recipetype:mekanism:painting>.addRecipe("paint_sand", <tag:items:forge:sand/colorless>, <pigment:mekanism:red> * 256, <item:minecraft:red_sand>);


//Removes the Painting Recipe that allows creating White Dye.

// <recipetype:mekanism:painting>.removeByName(name as string)

<recipetype:mekanism:painting>.removeByName("mekanism:painting/dye/white");