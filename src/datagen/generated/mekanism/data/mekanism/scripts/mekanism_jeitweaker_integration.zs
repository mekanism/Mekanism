#modloaded jeitweaker

import mods.jei.JEI;

//If JEITweaker is installed, Mekanism will add integration with it that allows for hiding our chemicals, and adding descriptions to them.

/*
 * Hides four chemicals (one of each type: Gas, Infuse Type, Pigment, Slurry) from JEI:
 * 1) Hides gaseous brine
 * 2) Hides the bio infuse type
 * 3) Hides dark red pigment
 * 4) Hides clean copper slurry
*/

//JEI.hideIngredient(stack as ICrTGasStack)
//JEI.hideIngredient(stack as ICrTInfusionStack)
//JEI.hideIngredient(stack as ICrTPigmentStack)
//JEI.hideIngredient(stack as ICrTSlurryStack)

JEI.hideIngredient(<gas:mekanism:brine> * 1000);
JEI.hideIngredient(<infuse_type:mekanism:bio> * 1000);
JEI.hideIngredient(<pigment:mekanism:dark_red> * 1000);
JEI.hideIngredient(<slurry:mekanism:clean_gold> * 1000);

//Adds a description to the passed in chemical. This example adds some basic text to JEI's information tab when looking at Hydrogen.

//JEI.addDescription(stack as ICrTGasStack, Component...)
//JEI.addDescription(stack as ICrTInfusionStack, Component...)
//JEI.addDescription(stack as ICrTPigmentStack, Component...)
//JEI.addDescription(stack as ICrTSlurryStack, Component...)

JEI.addDescription(<gas:mekanism:hydrogen> * 1000, "Hydrogen is a basic gas that is produced in an electrolytic separator");