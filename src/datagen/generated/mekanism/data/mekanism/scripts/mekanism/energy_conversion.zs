//Adds an Energy Conversion Recipe that allows converting Redstone Ore into 45 kJ of power.

// <recipetype:mekanism:energy_conversion>.addRecipe(name as string, input as IIngredientWithAmount, output as long)

<recipetype:mekanism:energy_conversion>.addRecipe("redstone_ore_to_power", <tag:item:c:ores/redstone>, 45000);

//Removes the Energy Conversion Recipe that allows converting Redstone Blocks into Power.

// <recipetype:mekanism:energy_conversion>.removeByName(name as string)

<recipetype:mekanism:energy_conversion>.removeByName("mekanism:energy_conversion/redstone_block");