import mods.mekanism.api.ingredient.ChemicalStackIngredient.GasStackIngredient;
import mods.mekanism.api.ingredient.FluidStackIngredient;

/*
 * Removes three Rotary Recipes:
 * 1) The recipe for converting between Liquid Lithium and Lithium.
 * 2) The recipe for converting between Liquid Sulfur Dioxide and Sulfur Dioxide.
 * 3) The recipe for converting between Liquid Sulfur Trioxide and Sulfur Trioxide.
*/

// <recipetype:mekanism:rotary>.removeByName(name as string)

<recipetype:mekanism:rotary>.removeByName("mekanism:rotary/lithium");
<recipetype:mekanism:rotary>.removeByName("mekanism:rotary/sulfur_dioxide");
<recipetype:mekanism:rotary>.removeByName("mekanism:rotary/sulfur_trioxide");
/*
 * Adds back three Rotary Recipes that correspond to the ones removed above:
 * 1) Adds a recipe to condensentrate Lithium to Liquid Lithium.
 * 2) Adds a recipe to decondensentrate Liquid Sulfur Dioxide to Sulfur Dioxide.
 * 3) Adds a recipe to convert between Liquid Sulfur Trioxide and Sulfur Trioxide.
*/

// <recipetype:mekanism:rotary>.addRecipe(name as string, gasInput as GasStackIngredient, fluidOutput as IFluidStack)
// <recipetype:mekanism:rotary>.addRecipe(name as string, fluidInput as FluidStackIngredient, gasOutput as ICrTGasStack)
// <recipetype:mekanism:rotary>.addRecipe(name as string, fluidInput as FluidStackIngredient, gasInput as GasStackIngredient, gasOutput as ICrTGasStack, fluidOutput as IFluidStack)

<recipetype:mekanism:rotary>.addRecipe("condensentrate_lithium", GasStackIngredient.from(<gas:mekanism:lithium>), <fluid:mekanism:lithium>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:rotary>.addRecipe("condensentrate_lithium", <gas:mekanism:lithium>, <fluid:mekanism:lithium>);

<recipetype:mekanism:rotary>.addRecipe("decondensentrate_sulfur_dioxide", FluidStackIngredient.from(<tag:fluids:forge:sulfur_dioxide>, 1), <gas:mekanism:sulfur_dioxide>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:rotary>.addRecipe("decondensentrate_sulfur_dioxide", <tag:fluids:forge:sulfur_dioxide> * 1, <gas:mekanism:sulfur_dioxide>);

<recipetype:mekanism:rotary>.addRecipe("rotary_sulfur_trioxide", FluidStackIngredient.from(<tag:fluids:forge:sulfur_trioxide>, 1), GasStackIngredient.from(<gas:mekanism:sulfur_trioxide>), <gas:mekanism:sulfur_trioxide>, <fluid:mekanism:sulfur_trioxide>);
//Alternate implementations of the above recipe are shown commented below. These implementations make use of implicit casting to allow easier calling:
// <recipetype:mekanism:rotary>.addRecipe("rotary_sulfur_trioxide", <tag:fluids:forge:sulfur_trioxide> * 1, GasStackIngredient.from(<gas:mekanism:sulfur_trioxide>), <gas:mekanism:sulfur_trioxide>, <fluid:mekanism:sulfur_trioxide>);
// <recipetype:mekanism:rotary>.addRecipe("rotary_sulfur_trioxide", FluidStackIngredient.from(<tag:fluids:forge:sulfur_trioxide>, 1), <gas:mekanism:sulfur_trioxide>, <gas:mekanism:sulfur_trioxide>, <fluid:mekanism:sulfur_trioxide>);
// <recipetype:mekanism:rotary>.addRecipe("rotary_sulfur_trioxide", <tag:fluids:forge:sulfur_trioxide> * 1, <gas:mekanism:sulfur_trioxide>, <gas:mekanism:sulfur_trioxide>, <fluid:mekanism:sulfur_trioxide>);

