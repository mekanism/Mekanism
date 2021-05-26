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

//JEI.hideGas(stack as ICrTGasStack)
//JEI.hideInfuseType(stack as ICrTInfusionStack)
//JEI.hidePigment(stack as ICrTPigmentStack)
//JEI.hideSlurry(stack as ICrTSlurryStack)

JEI.hideGas(<gas:mekanism:brine>);
JEI.hideInfuseType(<infuse_type:mekanism:bio>);
JEI.hidePigment(<pigment:mekanism:dark_red>);
JEI.hideSlurry(<slurry:mekanism:clean_gold>);

//Adds a description to the passed in chemical. This example adds some basic text to JEI's information tab when looking at Hydrogen.

//JEI.addInfo(stack as ICrTGasStack, MCTextComponent...)
//JEI.addInfo(stack as ICrTInfusionStack, MCTextComponent...)
//JEI.addInfo(stack as ICrTPigmentStack, MCTextComponent...)
//JEI.addInfo(stack as ICrTSlurryStack, MCTextComponent...)

JEI.addInfo(<gas:mekanism:hydrogen>, "Hydrogen is a basic gas that is produced in an electrolytic separator");