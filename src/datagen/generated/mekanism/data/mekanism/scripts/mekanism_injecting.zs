//Adds an Injecting Recipe that injects 1,000 mB of Water Vapor (5 mB per tick) into a Dry Sponge to make it Wet.

// <recipetype:mekanism:injecting>.addRecipe(arg0 as string, arg1 as ItemStackIngredient, arg2 as IChemicalStackIngredient, arg3 as IItemStack)

<recipetype:mekanism:injecting>.addRecipe("inject_water_to_sponge", mekanism.api.ingredient.ItemStackIngredient.from(<item:minecraft:sponge>), mekanism.api.ingredient.ChemicalStackIngredient.GasStackIngredient.from(<tag:gases:mekanism:water_vapor>, 5), <item:minecraft:wet_sponge>);
//Alternate implementations of the above recipe are shown commented below. These implementations make use of implicit casting to allow easier calling:
// <recipetype:mekanism:injecting>.addRecipe("inject_water_to_sponge", <item:minecraft:sponge>, mekanism.api.ingredient.ChemicalStackIngredient.GasStackIngredient.from(<tag:gases:mekanism:water_vapor>, 5), <item:minecraft:wet_sponge>);
// <recipetype:mekanism:injecting>.addRecipe("inject_water_to_sponge", mekanism.api.ingredient.ItemStackIngredient.from(<item:minecraft:sponge>), <tag:gases:mekanism:water_vapor> * 5, <item:minecraft:wet_sponge>);
// <recipetype:mekanism:injecting>.addRecipe("inject_water_to_sponge", <item:minecraft:sponge>, <tag:gases:mekanism:water_vapor> * 5, <item:minecraft:wet_sponge>);


//Removes the Injecting Recipe that creates Gold Shards from Gold Ore.

// <recipetype:mekanism:injecting>.removeByName(name as string)

<recipetype:mekanism:injecting>.removeByName("mekanism:processing/gold/shard/from_ore");