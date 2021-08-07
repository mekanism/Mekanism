import mods.mekanism.api.FloatingLong;
import mods.mekanism.api.ingredient.ItemStackIngredient;

//Adds an Energy Conversion Recipe that allows converting Redstone Ore into 45 kJ of power.

// <recipetype:mekanism:energy_conversion>.addRecipe(name as string, input as ItemStackIngredient, output as FloatingLong)

<recipetype:mekanism:energy_conversion>.addRecipe("redstone_ore_to_power", ItemStackIngredient.from(<tag:items:forge:ores/redstone>), FloatingLong.create(45000));
//Alternate implementations of the above recipe are shown commented below. These implementations make use of implicit casting to allow easier calling:
// <recipetype:mekanism:energy_conversion>.addRecipe("redstone_ore_to_power", <tag:items:forge:ores/redstone>, FloatingLong.create(45000));
// <recipetype:mekanism:energy_conversion>.addRecipe("redstone_ore_to_power", ItemStackIngredient.from(<tag:items:forge:ores/redstone>), 45000);
// <recipetype:mekanism:energy_conversion>.addRecipe("redstone_ore_to_power", <tag:items:forge:ores/redstone>, 45000);


//Removes the Energy Conversion Recipe that allows converting Redstone Blocks into Power.

// <recipetype:mekanism:energy_conversion>.removeByName(name as string)

<recipetype:mekanism:energy_conversion>.removeByName("mekanism:energy_conversion/redstone_block");