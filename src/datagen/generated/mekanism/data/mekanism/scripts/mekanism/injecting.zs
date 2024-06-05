import mods.mekanism.api.ingredient.ChemicalStackIngredient.GasStackIngredient;

//Adds an Injecting Recipe that injects 1,000 mB of Water Vapor (5 mB per tick) into a Dry Sponge to make it Wet.

// <recipetype:mekanism:injecting>.addRecipe(name as string, itemInput as IIngredientWithAmount, chemicalInput as GasStackIngredient, output as IItemStack)

<recipetype:mekanism:injecting>.addRecipe("inject_water_to_sponge", <item:minecraft:sponge>, GasStackIngredient.from(<tag:mekanism/gas:mekanism:water_vapor>, 5), <item:minecraft:wet_sponge>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:injecting>.addRecipe("inject_water_to_sponge", <item:minecraft:sponge>, <tag:mekanism/gas:mekanism:water_vapor> * 5, <item:minecraft:wet_sponge>);


//Removes the Injecting Recipe that creates Gold Shards from Gold Ore.

// <recipetype:mekanism:injecting>.removeByName(name as string)

<recipetype:mekanism:injecting>.removeByName("mekanism:processing/gold/shard/from_ore");