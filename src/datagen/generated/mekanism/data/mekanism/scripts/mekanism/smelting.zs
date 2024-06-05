//Adds a Smelting Recipe that works in Mekanism machines but won't work in a regular furnace to smelt Stone Slabs into Smooth Stone Slabs.

// <recipetype:mekanism:smelting>.addRecipe(name as string, input as IIngredientWithAmount, output as IItemStack)

<recipetype:mekanism:smelting>.addRecipe("smelt_stone_slab", <item:minecraft:stone_slab>, <item:minecraft:smooth_stone_slab>);
