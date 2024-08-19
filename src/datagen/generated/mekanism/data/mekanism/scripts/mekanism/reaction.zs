import mods.mekanism.api.ingredient.ChemicalStackIngredient;

/*
 * Adds six Reaction Recipes that do the following:
 * 1) Adds a recipe that uses 350 mB of Water, 50 mB of Hydrogen Chloride, and a piece of Sawdust to create two pieces of Paper in 45 ticks, using an extra 25 Joules.
 * 2) Adds a recipe that uses 100 mB of Liquid Chlorine, 100 mB of Hydrogen, and a Block of Sand to create a Salt Block in 300 ticks.
 * 3) Adds a recipe that uses 50 mB of Water, 50 mB of Oxygen, and eight Wooden Pressure Plates to create 50 mB of Hydrogen in 74 ticks, using an extra 100 Joules.
 * 4) Adds a recipe that uses 25 mB of Water, 25 mB of Oxygen, and eight Wooden Buttons to create 25 mB of Hydrogen in 37 ticks.
 * 5) Adds a recipe that uses 400 mB of Water, 400 mB of Oxygen, and twenty Wooden Fence to create a Charcoal Dust and 400 mB of Hydrogen in 600 ticks, using an extra 300 Joules.
 * 6) Adds a recipe that uses 400 mB of Water, 400 mB of Oxygen, and four Boats to create a Charcoal Dust and 400 mB of Hydrogen in 600 ticks.
*/

// <recipetype:mekanism:reaction>.addRecipe(name as string, inputSolid as IIngredientWithAmount, inputFluid as CTFluidIngredient, inputChemical as ChemicalStackIngredient, duration as int, outputItem as IItemStack)
// <recipetype:mekanism:reaction>.addRecipe(name as string, inputSolid as IIngredientWithAmount, inputFluid as CTFluidIngredient, inputChemical as ChemicalStackIngredient, duration as int, outputChemical as ICrTChemicalStack)
// <recipetype:mekanism:reaction>.addRecipe(name as string, inputSolid as IIngredientWithAmount, inputFluid as CTFluidIngredient, inputChemical as ChemicalStackIngredient, duration as int, outputItem as IItemStack, energyRequired as long)
// <recipetype:mekanism:reaction>.addRecipe(name as string, inputSolid as IIngredientWithAmount, inputFluid as CTFluidIngredient, inputChemical as ChemicalStackIngredient, duration as int, outputItem as IItemStack, outputChemical as ICrTChemicalStack)
// <recipetype:mekanism:reaction>.addRecipe(name as string, inputSolid as IIngredientWithAmount, inputFluid as CTFluidIngredient, inputChemical as ChemicalStackIngredient, duration as int, outputChemical as ICrTChemicalStack, energyRequired as long)
// <recipetype:mekanism:reaction>.addRecipe(name as string, inputSolid as IIngredientWithAmount, inputFluid as CTFluidIngredient, inputChemical as ChemicalStackIngredient, duration as int, outputItem as IItemStack, outputChemical as ICrTChemicalStack, energyRequired as long)

<recipetype:mekanism:reaction>.addRecipe("reaction/sawdust", <tag:item:c:dusts/wood>, <tag:fluid:minecraft:water> * 350, ChemicalStackIngredient.from(<chemical:mekanism:hydrogen_chloride> * 50), 45, <item:minecraft:paper> * 2, 25);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:reaction>.addRecipe("reaction/sawdust", <tag:item:c:dusts/wood>, <tag:fluid:minecraft:water> * 350, <chemical:mekanism:hydrogen_chloride> * 50, 45, <item:minecraft:paper> * 2, 25);

<recipetype:mekanism:reaction>.addRecipe("reaction/sand", <tag:item:c:sands>, <tag:fluid:c:chlorine> * 100, ChemicalStackIngredient.from(<chemical:mekanism:hydrogen> * 100), 300, <item:mekanism:block_salt>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:reaction>.addRecipe("reaction/sand", <tag:item:c:sands>, <tag:fluid:c:chlorine> * 100, <chemical:mekanism:hydrogen> * 100, 300, <item:mekanism:block_salt>);

<recipetype:mekanism:reaction>.addRecipe("reaction/wooden_buttons", (<tag:item:minecraft:wooden_buttons>) * 8, <tag:fluid:minecraft:water> * 25, ChemicalStackIngredient.from(<chemical:mekanism:oxygen> * 25), 37, <chemical:mekanism:hydrogen> * 25);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:reaction>.addRecipe("reaction/wooden_buttons", (<tag:item:minecraft:wooden_buttons>) * 8, <tag:fluid:minecraft:water> * 25, <chemical:mekanism:oxygen> * 25, 37, <chemical:mekanism:hydrogen> * 25);

<recipetype:mekanism:reaction>.addRecipe("reaction/wooden_pressure_plates", (<tag:item:minecraft:wooden_pressure_plates>) * 8, <tag:fluid:minecraft:water> * 50, ChemicalStackIngredient.from(<chemical:mekanism:oxygen> * 50), 74, <chemical:mekanism:hydrogen> * 50, 100);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:reaction>.addRecipe("reaction/wooden_pressure_plates", (<tag:item:minecraft:wooden_pressure_plates>) * 8, <tag:fluid:minecraft:water> * 50, <chemical:mekanism:oxygen> * 50, 74, <chemical:mekanism:hydrogen> * 50, 100);

<recipetype:mekanism:reaction>.addRecipe("reaction/wooden_fences", (<tag:item:minecraft:wooden_fences>) * 20, <tag:fluid:minecraft:water> * 400, ChemicalStackIngredient.from(<chemical:mekanism:oxygen> * 400), 600, <item:mekanism:dust_charcoal>, <chemical:mekanism:hydrogen> * 400, 300);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:reaction>.addRecipe("reaction/wooden_fences", (<tag:item:minecraft:wooden_fences>) * 20, <tag:fluid:minecraft:water> * 400, <chemical:mekanism:oxygen> * 400, 600, <item:mekanism:dust_charcoal>, <chemical:mekanism:hydrogen> * 400, 300);

<recipetype:mekanism:reaction>.addRecipe("reaction/boat", (<tag:item:minecraft:boats>) * 4, <tag:fluid:minecraft:water> * 400, ChemicalStackIngredient.from(<chemical:mekanism:oxygen> * 400), 600, <item:mekanism:dust_charcoal>, <chemical:mekanism:hydrogen> * 400);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:reaction>.addRecipe("reaction/boat", (<tag:item:minecraft:boats>) * 4, <tag:fluid:minecraft:water> * 400, <chemical:mekanism:oxygen> * 400, 600, <item:mekanism:dust_charcoal>, <chemical:mekanism:hydrogen> * 400);


//Removes the Reaction Recipe for producing Substrate from Bio Fuel.

// <recipetype:mekanism:reaction>.removeByName(name as string)

<recipetype:mekanism:reaction>.removeByName("mekanism:reaction/substrate/water_hydrogen");