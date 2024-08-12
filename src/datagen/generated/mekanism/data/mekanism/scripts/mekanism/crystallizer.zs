import mods.mekanism.api.ingredient.ChemicalStackIngredient;

/*
 * Adds two Crystallizing Recipes that do the following:
 * 1) Adds a recipe that produces one Osmium Ingot out of 200 mB of Osmium.
 * 2) Adds a recipe that produces one Gold Nugget out of 9 mB of the Gold Infuse Type.
*/

// <recipetype:mekanism:crystallizing>.addRecipe(name as string, input as ChemicalStackIngredient, output as IItemStack)

<recipetype:mekanism:crystallizing>.addRecipe("osmium_ingotification", ChemicalStackIngredient.from(<chemical:mekanism:osmium> * 200), <item:mekanism:ingot_osmium>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:crystallizing>.addRecipe("osmium_ingotification", <chemical:mekanism:osmium> * 200, <item:mekanism:ingot_osmium>);

<recipetype:mekanism:crystallizing>.addRecipe("gold_infusion_to_gold", ChemicalStackIngredient.from(<tag:mekanism/chemical:mekanism:gold>, 9), <item:minecraft:gold_nugget>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:crystallizing>.addRecipe("gold_infusion_to_gold", <tag:mekanism/chemical:mekanism:gold> * 9, <item:minecraft:gold_nugget>);


/*
 * Removes two Crystallizing Recipes:
 * 1) The recipe for producing Lithium Dust.
 * 2) The recipe for producing Antimatter Pellets.
*/

// <recipetype:mekanism:crystallizing>.removeByName(name as string)

<recipetype:mekanism:crystallizing>.removeByName("mekanism:crystallizing/lithium");
<recipetype:mekanism:crystallizing>.removeByName("mekanism:processing/lategame/antimatter_pellet/from_gas");