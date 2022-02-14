package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;

//Expose ChemicalStackIngredient to CrT so that we can use it as a "generic" input when any chemical ingredient is valid
@ZenRegister
@NativeMethod(name = "testType", parameters = Chemical.class)
@NativeTypeRegistration(value = ChemicalStackIngredient.class, zenCodeName = CrTConstants.CLASS_CHEMICAL_STACK_INGREDIENT)
public class CrTChemicalStackIngredient {

    private CrTChemicalStackIngredient() {
    }
}