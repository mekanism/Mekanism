package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;

//Expose ChemicalStackIngredient to CrT so that we can use it as a "generic" input when any chemical ingredient is valid
@ZenRegister
@NativeTypeRegistration(value = ChemicalStackIngredient.class, zenCodeName = CrTConstants.CLASS_CHEMICAL_STACK_INGREDIENT)
public class CrTChemicalStackIngredient {

    private CrTChemicalStackIngredient() {
    }
}