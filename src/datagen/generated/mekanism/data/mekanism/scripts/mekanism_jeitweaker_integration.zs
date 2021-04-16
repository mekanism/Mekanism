#modloaded jeitweaker

//If JEITweaker is installed, Mekanism will add integration with it that allows for hiding our chemicals, and adding descriptions to them.

/*
 * Hides four chemicals (one of each type: Gas, Infuse Type, Pigment, Slurry) from JEI:
 * 1) Hides gaseous brine
 * 2) Hides the bio infuse type
 * 3) Hides dark red pigment
 * 4) Hides clean copper slurry
*/

//mods.jei.JEI.hideGas(stack as ICrTGasStack)
//mods.jei.JEI.hideInfuseType(stack as ICrTInfusionStack)
//mods.jei.JEI.hidePigment(stack as ICrTPigmentStack)
//mods.jei.JEI.hideSlurry(stack as ICrTSlurryStack)

mods.jei.JEI.hideGas(<gas:mekanism:brine>);
mods.jei.JEI.hideInfuseType(<infuse_type:mekanism:bio>);
mods.jei.JEI.hidePigment(<pigment:mekanism:dark_red>);
mods.jei.JEI.hideSlurry(<slurry:mekanism:clean_gold>);

//Adds a description to the passed in chemical. This example adds some basic text to JEI's information tab when looking at Hydrogen.

//mods.jei.JEI.addInfo(stack as ICrTGasStack, MCTextComponent...)
//mods.jei.JEI.addInfo(stack as ICrTInfusionStack, MCTextComponent...)
//mods.jei.JEI.addInfo(stack as ICrTPigmentStack, MCTextComponent...)
//mods.jei.JEI.addInfo(stack as ICrTSlurryStack, MCTextComponent...)

mods.jei.JEI.addInfo(<gas:mekanism:hydrogen>, "Hydrogen is a basic gas that is produced in an electrolytic separator");