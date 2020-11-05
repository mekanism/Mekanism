package mekanism.common.integration.crafttweaker;

//TODO: We are going to need to define a min CrT version of 7.0.0.50 for if CrT exists
public class CrTConstants {

    public static final String BRACKET_GAS = "gas";
    public static final String BRACKET_INFUSE_TYPE = "infuse_type";
    public static final String BRACKET_PIGMENT = "pigment";
    public static final String BRACKET_SLURRY = "slurry";

    public static final String CLASS_BRACKET_DUMPERS = "mekanism.api.BracketDumpers";
    public static final String CLASS_BRACKET_HANDLER = "mekanism.api.BracketHandlers";
    public static final String CLASS_BRACKET_VALIDATORS = "mekanism.api.BracketValidators";

    public static final String CLASS_CRT_TAG = "crafttweaker.api.tag.MCTag";

    //TODO: Figure out properly which things need registration and annotation and which don't in terms of concrete vs interfaces
    // We may be registering more things than needed currently, or at least naming more things than needed
    public static final String CLASS_CHEMICAL = "mekanism.api.chemical.IChemical";
    public static final String CLASS_CHEMICAL_STACK = "mekanism.api.chemical.IChemicalStack";

    public static final String CLASS_GAS = "mekanism.api.chemical.gas.IGas";
    public static final String CLASS_GAS_IMPL = "mekanism.api.chemical.gas.Gas";
    public static final String CLASS_GAS_STACK = "mekanism.api.chemical.gas.IGasStack";
    public static final String CLASS_GAS_STACK_IMPL = "mekanism.api.chemical.gas.GasStack";
    public static final String CLASS_GAS_STACK_MUTABLE = "mekanism.api.chemical.gas.MutableGasStack";

    public static final String CLASS_INFUSE_TYPE = "mekanism.api.chemical.infuse.IInfuseType";
    public static final String CLASS_INFUSE_TYPE_IMPL = "mekanism.api.chemical.infuse.InfuseType";
    public static final String CLASS_INFUSION_STACK = "mekanism.api.chemical.infuse.IInfusionStack";
    public static final String CLASS_INFUSION_STACK_IMPL = "mekanism.api.chemical.infuse.InfusionStack";
    public static final String CLASS_INFUSION_STACK_MUTABLE = "mekanism.api.chemical.infuse.MutableInfusionStack";

    public static final String CLASS_PIGMENT = "mekanism.api.chemical.pigment.IPigment";
    public static final String CLASS_PIGMENT_IMPL = "mekanism.api.chemical.pigment.Pigment";
    public static final String CLASS_PIGMENT_STACK = "mekanism.api.chemical.pigment.IPigmentStack";
    public static final String CLASS_PIGMENT_STACK_IMPL = "mekanism.api.chemical.pigment.PigmentStack";
    public static final String CLASS_PIGMENT_STACK_MUTABLE = "mekanism.api.chemical.pigment.MutablePigmentStack";

    public static final String CLASS_SLURRY = "mekanism.api.chemical.slurry.ISlurry";
    public static final String CLASS_SLURRY_IMPL = "mekanism.api.chemical.slurry.Slurry";
    public static final String CLASS_SLURRY_STACK = "mekanism.api.chemical.slurry.ISlurryStack";
    public static final String CLASS_SLURRY_STACK_IMPL = "mekanism.api.chemical.slurry.SlurryStack";
    public static final String CLASS_SLURRY_STACK_MUTABLE = "mekanism.api.chemical.slurry.MutableSlurryStack";

    public static final String CLASS_ITEM_STACK_INGREDIENT = "mekanism.api.ingredient.ItemStackIngredient";
    public static final String CLASS_FLUID_STACK_INGREDIENT = "mekanism.api.ingredient.FluidStackIngredient";
    public static final String CLASS_GAS_STACK_INGREDIENT = "mekanism.api.ingredient.GasStackIngredient";
    public static final String CLASS_INFUSION_STACK_INGREDIENT = "mekanism.api.ingredient.InfusionStackIngredient";
    public static final String CLASS_PIGMENT_STACK_INGREDIENT = "mekanism.api.ingredient.PigmentStackIngredient";
    public static final String CLASS_SLURRY_STACK_INGREDIENT = "mekanism.api.ingredient.SlurryStackIngredient";
}