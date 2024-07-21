/*
 * Adds two Separating Recipes that do the following:
 * 1) Adds a recipe that separates 2 mB of Liquid Sulfur Trioxide into 1 mB of Oxygen and 2 mB of Sulfur Dioxide.
 * 2) Adds a recipe that separates 1 mB of Liquid Sulfur Acid into 1 mB of Water Vapor and 1 mB of Sulfur Trioxide, using twice as much energy as it takes to separate Oxygen and Hydrogen from Water.
*/

// <recipetype:mekanism:separating>.addRecipe(name as string, input as CTFluidIngredient, leftGasOutput as ICrTGasStack, rightGasOutput as ICrTGasStack)
// <recipetype:mekanism:separating>.addRecipe(name as string, input as CTFluidIngredient, leftGasOutput as ICrTGasStack, rightGasOutput as ICrTGasStack, energyMultiplier as long)

<recipetype:mekanism:separating>.addRecipe("separator/sulfur_trioxide", <tag:fluid:c:sulfur_trioxide> * 2, <gas:mekanism:oxygen>, <gas:mekanism:sulfur_dioxide> * 2);
<recipetype:mekanism:separating>.addRecipe("separator/sulfuric_acid", <tag:fluid:c:sulfuric_acid> * 1, <gas:mekanism:water_vapor>, <gas:mekanism:sulfur_trioxide>, 2);

//Removes the Separating Recipe for separating Brine into Sodium and Chlorine.

// <recipetype:mekanism:separating>.removeByName(name as string)

<recipetype:mekanism:separating>.removeByName("mekanism:separator/brine");