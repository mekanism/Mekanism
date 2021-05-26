package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;

//Expose IChemicalStackIngredient to CrT so that we can use it as a "generic" input when any chemical ingredient is valid
@ZenRegister
@NativeTypeRegistration(value = IChemicalStackIngredient.class, zenCodeName = CrTConstants.CLASS_CHEMICAL_STACK_INGREDIENT)
public class CrTChemicalStackIngredient {

    private CrTChemicalStackIngredient() {
    }
}