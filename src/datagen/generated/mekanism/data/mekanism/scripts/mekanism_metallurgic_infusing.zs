//Adds a Metallurgic Infusing Recipe that uses 10 mB of Fungi Infuse Type to convert any Oak Planks into Crimson Planks.

// <recipetype:mekanism:metallurgic_infusing>.addRecipe(arg0 as string, arg1 as ItemStackIngredient, arg2 as IChemicalStackIngredient, arg3 as IItemStack)

<recipetype:mekanism:metallurgic_infusing>.addRecipe("infuse_planks", mekanism.api.ingredient.ItemStackIngredient.from(<item:minecraft:oak_planks>), mekanism.api.ingredient.ChemicalStackIngredient.InfusionStackIngredient.from(<infuse_type:mekanism:fungi> * 10), <item:minecraft:crimson_planks>);
//Alternate implementations of the above recipe are shown commented below. These implementations make use of implicit casting to allow easier calling:
// <recipetype:mekanism:metallurgic_infusing>.addRecipe("infuse_planks", <item:minecraft:oak_planks>, mekanism.api.ingredient.ChemicalStackIngredient.InfusionStackIngredient.from(<infuse_type:mekanism:fungi> * 10), <item:minecraft:crimson_planks>);
// <recipetype:mekanism:metallurgic_infusing>.addRecipe("infuse_planks", mekanism.api.ingredient.ItemStackIngredient.from(<item:minecraft:oak_planks>), <infuse_type:mekanism:fungi> * 10, <item:minecraft:crimson_planks>);
// <recipetype:mekanism:metallurgic_infusing>.addRecipe("infuse_planks", <item:minecraft:oak_planks>, <infuse_type:mekanism:fungi> * 10, <item:minecraft:crimson_planks>);


//Removes the Metallurgic Infusing Recipe that allows creating Dirt from Sand.

// <recipetype:mekanism:metallurgic_infusing>.removeByName(name as string)

<recipetype:mekanism:metallurgic_infusing>.removeByName("mekanism:metallurgic_infusing/sand_to_dirt");