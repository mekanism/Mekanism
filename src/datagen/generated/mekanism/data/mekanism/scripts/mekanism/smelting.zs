import mods.mekanism.api.ingredient.ItemStackIngredient;

//Adds a Smelting Recipe that works in Mekanism machines but won't work in a regular furnace to smelt Stone Slabs into Smooth Stone Slabs.

// <recipetype:mekanism:smelting>.addRecipe(name as string, input as ItemStackIngredient, output as IItemStack)

<recipetype:mekanism:smelting>.addRecipe("smelt_stone_slab", ItemStackIngredient.from(<item:minecraft:stone_slab>), <item:minecraft:smooth_stone_slab>);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:smelting>.addRecipe("smelt_stone_slab", <item:minecraft:stone_slab>, <item:minecraft:smooth_stone_slab>);

