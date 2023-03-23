import mods.mekanism.api.ingredient.ChemicalStackIngredient.GasStackIngredient;
import mods.mekanism.api.ingredient.ItemStackIngredient;

//Adds a Nucleosynthesizing Recipe that converts a Block of Coal to a Block of Diamond in 9,000 ticks (7 minutes 30 seconds).

// <recipetype:mekanism:nucleosynthesizing>.addRecipe(name as string, itemInput as ItemStackIngredient, gasInput as GasStackIngredient, output as IItemStack, duration as int)

<recipetype:mekanism:nucleosynthesizing>.addRecipe("coal_block_to_diamond_block", ItemStackIngredient.from(<tag:items:forge:storage_blocks/coal>), GasStackIngredient.from(<gas:mekanism:antimatter> * 36), <item:minecraft:diamond_block>, 9000);
//Alternate implementations of the above recipe are shown commented below. These implementations make use of implicit casting to allow easier calling:
// <recipetype:mekanism:nucleosynthesizing>.addRecipe("coal_block_to_diamond_block", <tag:items:forge:storage_blocks/coal>, GasStackIngredient.from(<gas:mekanism:antimatter> * 36), <item:minecraft:diamond_block>, 9000);
// <recipetype:mekanism:nucleosynthesizing>.addRecipe("coal_block_to_diamond_block", ItemStackIngredient.from(<tag:items:forge:storage_blocks/coal>), <gas:mekanism:antimatter> * 36, <item:minecraft:diamond_block>, 9000);
// <recipetype:mekanism:nucleosynthesizing>.addRecipe("coal_block_to_diamond_block", <tag:items:forge:storage_blocks/coal>, <gas:mekanism:antimatter> * 36, <item:minecraft:diamond_block>, 9000);


//Removes the Nucleosynthesizing Recipe that converts Tin Ingots into Iron Ingots.

// <recipetype:mekanism:nucleosynthesizing>.removeByName(name as string)

<recipetype:mekanism:nucleosynthesizing>.removeByName("mekanism:nucleosynthesizing/iron");