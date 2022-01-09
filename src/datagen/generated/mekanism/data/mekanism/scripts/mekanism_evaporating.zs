import mods.mekanism.api.ingredient.FluidStackIngredient;

//Adds an Evaporating Recipe that evaporates 10 mB of Lithium and produces 1 mB of Chlorine.

// <recipetype:mekanism:evaporating>.addRecipe(name as string, input as FluidStackIngredient, output as IFluidStack)

<recipetype:mekanism:evaporating>.addRecipe("evaporate_lithium", FluidStackIngredient.from(<tag:fluids:forge:lithium>, 10), <fluid:mekanism:chlorine>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:evaporating>.addRecipe("evaporate_lithium", <tag:fluids:forge:lithium> * 10, <fluid:mekanism:chlorine>);


//Removes the Evaporating Recipe for producing Lithium from Brine.

// <recipetype:mekanism:evaporating>.removeByName(name as string)

<recipetype:mekanism:evaporating>.removeByName("mekanism:evaporating/lithium");