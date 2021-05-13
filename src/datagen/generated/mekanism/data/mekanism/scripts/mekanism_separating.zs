/*
 * Adds two Separating Recipes that do the following:
 * 1) Adds a recipe that separates 2 mB of Liquid Sulfur Trioxide into 1 mB of Oxygen and 2 mB of Sulfur Dioxide.
 * 2) Adds a recipe that separates 1 mB of Liquid Sulfur Acid into 1 mB of Water Vapor and 1 mB of Sulfur Trioxide, using one and a half times as much energy as it takes to separate Oxygen and Hydrogen from Water.
*/

// <recipetype:mekanism:separating>.addRecipe(arg0 as string, arg1 as FluidStackIngredient, arg2 as ICrTGasStack, arg3 as ICrTGasStack)
// <recipetype:mekanism:separating>.addRecipe(arg0 as string, arg1 as FluidStackIngredient, arg2 as ICrTGasStack, arg3 as ICrTGasStack, arg4 as FloatingLong)

<recipetype:mekanism:separating>.addRecipe("separator/sulfur_trioxide", mekanism.api.ingredient.FluidStackIngredient.from(<tag:fluids:forge:sulfur_trioxide>, 2), <gas:mekanism:oxygen>, <gas:mekanism:sulfur_dioxide> * 2);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:separating>.addRecipe("separator/sulfur_trioxide", <tag:fluids:forge:sulfur_trioxide> * 2, <gas:mekanism:oxygen>, <gas:mekanism:sulfur_dioxide> * 2);

<recipetype:mekanism:separating>.addRecipe("separator/sulfuric_acid", mekanism.api.ingredient.FluidStackIngredient.from(<tag:fluids:forge:sulfuric_acid>, 1), <gas:mekanism:water_vapor>, <gas:mekanism:sulfur_trioxide>, mekanism.api.FloatingLong.create(1.5000));
//Alternate implementations of the above recipe are shown commented below. These implementations make use of implicit casting to allow easier calling:
// <recipetype:mekanism:separating>.addRecipe("separator/sulfuric_acid", <tag:fluids:forge:sulfuric_acid>, <gas:mekanism:water_vapor>, <gas:mekanism:sulfur_trioxide>, mekanism.api.FloatingLong.create(1.5000));
// <recipetype:mekanism:separating>.addRecipe("separator/sulfuric_acid", mekanism.api.ingredient.FluidStackIngredient.from(<tag:fluids:forge:sulfuric_acid>, 1), <gas:mekanism:water_vapor>, <gas:mekanism:sulfur_trioxide>, 1.5000);
// <recipetype:mekanism:separating>.addRecipe("separator/sulfuric_acid", <tag:fluids:forge:sulfuric_acid>, <gas:mekanism:water_vapor>, <gas:mekanism:sulfur_trioxide>, 1.5000);


//Removes the Separating Recipe for separating Brine into Sodium and Chlorine.

// <recipetype:mekanism:separating>.removeByName(name as string)

<recipetype:mekanism:separating>.removeByName("mekanism:separator/brine");