import mods.mekanism.api.ingredient.ChemicalStackIngredient;

//Adds a Metallurgic Infusing Recipe that uses 10 mB of Fungi Infuse Type to convert any Oak Planks into Crimson Planks.

// <recipetype:mekanism:metallurgic_infusing>.addRecipe(name as string, itemInput as IIngredientWithAmount, chemicalInput as ChemicalStackIngredient, output as IItemStack, perTickUsage as bool)

<recipetype:mekanism:metallurgic_infusing>.addRecipe("infuse_planks", <item:minecraft:oak_planks>, ChemicalStackIngredient.from(<chemical:mekanism:fungi> * 10), <item:minecraft:crimson_planks>, false);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:metallurgic_infusing>.addRecipe("infuse_planks", <item:minecraft:oak_planks>, <chemical:mekanism:fungi> * 10, <item:minecraft:crimson_planks>, false);


//Removes the Metallurgic Infusing Recipe that allows creating Dirt from Sand.

// <recipetype:mekanism:metallurgic_infusing>.removeByName(name as string)

<recipetype:mekanism:metallurgic_infusing>.removeByName("mekanism:metallurgic_infusing/sand_to_dirt");