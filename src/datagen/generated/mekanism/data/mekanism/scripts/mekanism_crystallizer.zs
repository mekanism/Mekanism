import mods.mekanism.api.ingredient.ChemicalStackIngredient.GasStackIngredient;
import mods.mekanism.api.ingredient.ChemicalStackIngredient.InfusionStackIngredient;

/*
 * Adds two Crystallizing Recipes that do the following:
 * 1) Adds a recipe that produces one Carrot out of 150 mB of Nutritional Paste.
 * 2) Adds a recipe that produces one Gold Nugget out of 9 mB of the Gold Infuse Type.
*/

// <recipetype:mekanism:crystallizing>.addRecipe(name as string, input as IChemicalStackIngredient, output as IItemStack)

<recipetype:mekanism:crystallizing>.addRecipe("paste_to_carrots", GasStackIngredient.from(<gas:mekanism:nutritional_paste> * 150), <item:minecraft:carrot>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:crystallizing>.addRecipe("paste_to_carrots", <gas:mekanism:nutritional_paste> * 150, <item:minecraft:carrot>);

<recipetype:mekanism:crystallizing>.addRecipe("gold_infusion_to_gold", InfusionStackIngredient.from(<tag:infuse_types:mekanism:gold>, 9), <item:minecraft:gold_nugget>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:crystallizing>.addRecipe("gold_infusion_to_gold", <tag:infuse_types:mekanism:gold> * 9, <item:minecraft:gold_nugget>);


/*
 * Removes two Crystallizing Recipes:
 * 1) The recipe for producing Lithium Dust.
 * 2) The recipe for producing Antimatter Pellets.
*/

// <recipetype:mekanism:crystallizing>.removeByName(name as string)

<recipetype:mekanism:crystallizing>.removeByName("mekanism:crystallizing/lithium");
<recipetype:mekanism:crystallizing>.removeByName("mekanism:processing/lategame/antimatter_pellet/from_gas");