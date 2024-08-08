//Adds an Infusion Conversion Recipe that allows converting Gold Ingots into 10 mB Gold Infuse Type.

// <recipetype:mekanism:chemical_conversion>.addRecipe(name as string, input as IIngredientWithAmount, output as ICrTChemicalStack)

<recipetype:mekanism:chemical_conversion>.addRecipe("chemical_conversion/gold/from_ingot", <tag:item:c:ingots/gold>, <chemical:mekanism:gold> * 10);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:chemical_conversion>.addRecipe("chemical_conversion/gold/from_ingot", <tag:item:c:ingots/gold>, <chemical:mekanism:gold> * 10);


//Removes the Infusion Conversion Recipe that allows converting Bio Fuel into the Bio Infuse Type.

// <recipetype:mekanism:chemical_conversion>.removeByName(name as string)

<recipetype:mekanism:chemical_conversion>.removeByName("mekanism:chemical_conversion/bio/from_bio_fuel");