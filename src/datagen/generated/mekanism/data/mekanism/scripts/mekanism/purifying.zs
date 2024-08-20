import mods.mekanism.api.ingredient.ChemicalStackIngredient;

//Adds a Purifying Recipe that uses 200 mB of Oxygen (1 mB per tick) Basalt into Polished Basalt.

// <recipetype:mekanism:purifying>.addRecipe(name as string, itemInput as IIngredientWithAmount, chemicalInput as ChemicalStackIngredient, output as IItemStack, perTickUsage as bool)

<recipetype:mekanism:purifying>.addRecipe("purify_basalt", <item:minecraft:basalt>, ChemicalStackIngredient.from(<chemical:mekanism:oxygen>), <item:minecraft:polished_basalt>, true);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:purifying>.addRecipe("purify_basalt", <item:minecraft:basalt>, <chemical:mekanism:oxygen>, <item:minecraft:polished_basalt>, true);


//Removes the Purifying Recipe that creates Gold Clumps from Gold Ore.

// <recipetype:mekanism:purifying>.removeByName(name as string)

<recipetype:mekanism:purifying>.removeByName("mekanism:processing/gold/clump/from_ore");