//Adds a Smelting Recipe that works in Mekanism machines but won't work in a regular furnace to smelt Stone Slabs into Smooth Stone Slabs.

// <recipetype:mekanism:smelting>.addRecipe(arg0 as string, arg1 as ItemStackIngredient, arg2 as IItemStack)

<recipetype:mekanism:smelting>.addRecipe("smelt_stone_slab", mekanism.api.ingredient.ItemStackIngredient.from(<item:minecraft:stone_slab>), <item:minecraft:smooth_stone_slab>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:smelting>.addRecipe("smelt_stone_slab", <item:minecraft:stone_slab>, <item:minecraft:smooth_stone_slab>);

