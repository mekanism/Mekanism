#modloaded jeitweaker

import mods.jeitweaker.Jei;

//If JEITweaker is installed, Mekanism will add integration with it that allows for hiding our chemicals, and adding descriptions to them.

/*
 * Hides four chemicals (one of each type: Gas, Infuse Type, Pigment, Slurry) from JEI:
 * 1) Hides gaseous brine
 * 2) Hides the bio infuse type
 * 3) Hides dark red pigment
 * 4) Hides clean copper slurry
*/

//Jei.hideIngredient(stack as ICrTGasStack)
//Jei.hideIngredient(stack as ICrTInfusionStack)
//Jei.hideIngredient(stack as ICrTPigmentStack)
//Jei.hideIngredient(stack as ICrTSlurryStack)

Jei.hideIngredient(<gas:mekanism:brine> * 1000);
Jei.hideIngredient(<infuse_type:mekanism:bio> * 1000);
Jei.hideIngredient(<pigment:mekanism:dark_red> * 1000);
Jei.hideIngredient(<slurry:mekanism:clean_gold> * 1000);

//Adds a description to the passed in chemical. This example adds some basic text to JEI's information tab when looking at Hydrogen.

//Jei.addIngredientInformation(stack as ICrTGasStack, Component...)
//Jei.addIngredientInformation(stack as ICrTInfusionStack, Component...)
//Jei.addIngredientInformation(stack as ICrTPigmentStack, Component...)
//Jei.addIngredientInformation(stack as ICrTSlurryStack, Component...)

Jei.addIngredientInformation(<gas:mekanism:hydrogen> * 1000, "Hydrogen is a basic gas that is produced in an electrolytic separator");