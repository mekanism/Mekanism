import mods.mekanism.api.FloatingLong;

//Adds an Energy Conversion Recipe that allows converting Redstone Ore into 45 kJ of power.

// <recipetype:mekanism:energy_conversion>.addRecipe(name as string, input as IIngredientWithAmount, output as FloatingLong)

<recipetype:mekanism:energy_conversion>.addRecipe("redstone_ore_to_power", <tag:items:c:ores/redstone>, FloatingLong.create(45000));
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:energy_conversion>.addRecipe("redstone_ore_to_power", <tag:items:c:ores/redstone>, 45000);


//Removes the Energy Conversion Recipe that allows converting Redstone Blocks into Power.

// <recipetype:mekanism:energy_conversion>.removeByName(name as string)

<recipetype:mekanism:energy_conversion>.removeByName("mekanism:energy_conversion/redstone_block");