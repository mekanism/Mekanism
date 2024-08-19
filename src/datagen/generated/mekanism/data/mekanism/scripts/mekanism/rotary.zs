import mods.mekanism.api.ingredient.ChemicalStackIngredient;

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

// <recipetype:mekanism:rotary>.addRecipe(name as string, fluidInput as CTFluidIngredient, chemicalOutput as ICrTChemicalStack)
// <recipetype:mekanism:rotary>.addRecipe(name as string, chemicalInput as ChemicalStackIngredient, fluidOutput as IFluidStack)
// <recipetype:mekanism:rotary>.addRecipe(name as string, fluidInput as CTFluidIngredient, chemicalInput as ChemicalStackIngredient, chemicalOutput as ICrTChemicalStack, fluidOutput as IFluidStack)

<recipetype:mekanism:rotary>.addRecipe("condensentrate_lithium", ChemicalStackIngredient.from(<chemical:mekanism:lithium>), <fluid:mekanism:lithium>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:rotary>.addRecipe("condensentrate_lithium", <chemical:mekanism:lithium>, <fluid:mekanism:lithium>);

<recipetype:mekanism:rotary>.addRecipe("decondensentrate_sulfur_dioxide", <tag:fluid:c:sulfur_dioxide> * 1, <chemical:mekanism:sulfur_dioxide>);
<recipetype:mekanism:rotary>.addRecipe("rotary_sulfur_trioxide", <tag:fluid:c:sulfur_trioxide> * 1, ChemicalStackIngredient.from(<chemical:mekanism:sulfur_trioxide>), <chemical:mekanism:sulfur_trioxide>, <fluid:mekanism:sulfur_trioxide>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:rotary>.addRecipe("rotary_sulfur_trioxide", <tag:fluid:c:sulfur_trioxide> * 1, <chemical:mekanism:sulfur_trioxide>, <chemical:mekanism:sulfur_trioxide>, <fluid:mekanism:sulfur_trioxide>);

