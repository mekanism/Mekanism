//Adds an Evaporating Recipe that evaporates 10 mB of Lithium and produces 1 mB of Chlorine.

// <recipetype:mekanism:evaporating>.addRecipe(name as string, input as CTFluidIngredient, output as IFluidStack)

<recipetype:mekanism:evaporating>.addRecipe("evaporate_lithium", <tag:fluid:c:lithium> * 10, <fluid:mekanism:chlorine>);

//Removes the Evaporating Recipe for producing Lithium from Brine.

// <recipetype:mekanism:evaporating>.removeByName(name as string)

<recipetype:mekanism:evaporating>.removeByName("mekanism:evaporating/lithium");