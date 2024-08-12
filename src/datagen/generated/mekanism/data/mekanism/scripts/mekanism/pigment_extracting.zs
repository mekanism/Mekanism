//Adds a Pigment Extracting Recipe that extracts 6,912 mB of Blue Pigment from a Lapis Lazuli Block.

// <recipetype:mekanism:pigment_extracting>.addRecipe(name as string, input as IIngredientWithAmount, output as ICrTChemicalStack)

<recipetype:mekanism:pigment_extracting>.addRecipe("extract_lapis_block_pigment", <tag:item:c:storage_blocks/lapis>, <chemical:mekanism:blue> * 6912);

//Removes the Pigment Extracting Recipe that extracts Brown Pigment from Brown Dye.

// <recipetype:mekanism:pigment_extracting>.removeByName(name as string)

<recipetype:mekanism:pigment_extracting>.removeByName("mekanism:pigment_extracting/dye/brown");