import mods.mekanism.api.ingredient.ItemStackIngredient;

//Adds a Pigment Extracting Recipe that extracts 6,912 mB of Blue Pigment from a Lapis Lazuli Block.

// <recipetype:mekanism:pigment_extracting>.addRecipe(name as string, input as ItemStackIngredient, output as ICrTPigmentStack)

<recipetype:mekanism:pigment_extracting>.addRecipe("extract_lapis_block_pigment", ItemStackIngredient.from(<tag:items:forge:storage_blocks/lapis>), <pigment:mekanism:blue> * 6912);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:pigment_extracting>.addRecipe("extract_lapis_block_pigment", <tag:items:forge:storage_blocks/lapis>, <pigment:mekanism:blue> * 6912);


//Removes the Pigment Extracting Recipe that extracts Brown Pigment from Brown Dye.

// <recipetype:mekanism:pigment_extracting>.removeByName(name as string)

<recipetype:mekanism:pigment_extracting>.removeByName("mekanism:pigment_extracting/dye/brown");