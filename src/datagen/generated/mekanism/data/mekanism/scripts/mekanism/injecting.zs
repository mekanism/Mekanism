import mods.mekanism.api.ingredient.ChemicalStackIngredient;

//Adds an Injecting Recipe that injects 1,000 mB of Water Vapor (5 mB per tick) into a Dry Sponge to make it Wet.

// <recipetype:mekanism:injecting>.addRecipe(name as string, itemInput as IIngredientWithAmount, chemicalInput as ChemicalStackIngredient, output as IItemStack, perTickUsage as bool)

<recipetype:mekanism:injecting>.addRecipe("inject_water_to_sponge", <item:minecraft:sponge>, ChemicalStackIngredient.from(<tag:mekanism/chemical:mekanism:water_vapor>, 5), <item:minecraft:wet_sponge>, true);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:injecting>.addRecipe("inject_water_to_sponge", <item:minecraft:sponge>, <tag:mekanism/chemical:mekanism:water_vapor> * 5, <item:minecraft:wet_sponge>, true);


//Removes the Injecting Recipe that creates Gold Shards from Gold Ore.

// <recipetype:mekanism:injecting>.removeByName(name as string)

<recipetype:mekanism:injecting>.removeByName("mekanism:processing/gold/shard/from_ore");