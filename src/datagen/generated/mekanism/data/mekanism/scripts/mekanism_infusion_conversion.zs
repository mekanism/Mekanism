import mods.mekanism.api.ingredient.ItemStackIngredient;

//Adds an Infusion Conversion Recipe that allows converting Gold Ingots into 10 mB Gold Infuse Type.

// <recipetype:mekanism:infusion_conversion>.addRecipe(name as string, input as ItemStackIngredient, output as ICrTInfusionStack)

<recipetype:mekanism:infusion_conversion>.addRecipe("infusion_conversion/gold/from_ingot", ItemStackIngredient.from(<tag:items:forge:ingots/gold>), <infuse_type:mekanism:gold> * 10);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:infusion_conversion>.addRecipe("infusion_conversion/gold/from_ingot", <tag:items:forge:ingots/gold>, <infuse_type:mekanism:gold> * 10);


//Removes the Infusion Conversion Recipe that allows converting Bio Fuel into the Bio Infuse Type.

// <recipetype:mekanism:infusion_conversion>.removeByName(name as string)

<recipetype:mekanism:infusion_conversion>.removeByName("mekanism:infusion_conversion/bio/from_bio_fuel");