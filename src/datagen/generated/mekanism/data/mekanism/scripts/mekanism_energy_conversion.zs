//Adds an Energy Conversion Recipe that allows converting Redstone Ore into 45 kJ of power.

// <recipetype:mekanism:energy_conversion>.addRecipe(arg0 as string, arg1 as ItemStackIngredient, arg2 as FloatingLong)

<recipetype:mekanism:energy_conversion>.addRecipe("redstone_ore_to_power", mekanism.api.ingredient.ItemStackIngredient.from(<tag:items:forge:ores/redstone>), 45000);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:energy_conversion>.addRecipe("redstone_ore_to_power", <tag:items:forge:ores/redstone>, 45000);


//Removes the Energy Conversion Recipe that allows converting Redstone Blocks into Power.

// <recipetype:mekanism:energy_conversion>.removeByName(name as string)

<recipetype:mekanism:energy_conversion>.removeByName("mekanism:energy_conversion/redstone_block");