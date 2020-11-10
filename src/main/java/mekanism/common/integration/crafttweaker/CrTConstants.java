package mekanism.common.integration.crafttweaker;

//TODO - 10.1: We are going to need to define a min CrT version of 7.0.0.56 for if CrT exists
public class CrTConstants {

    public static final String BRACKET_GAS = "gas";
    public static final String BRACKET_INFUSE_TYPE = "infuse_type";
    public static final String BRACKET_PIGMENT = "pigment";
    public static final String BRACKET_SLURRY = "slurry";

    public static final String CLASS_BRACKET_DUMPERS = "mekanism.api.BracketDumpers";
    public static final String CLASS_BRACKET_HANDLER = "mekanism.api.BracketHandlers";
    public static final String CLASS_BRACKET_VALIDATORS = "mekanism.api.BracketValidators";

    public static final String EXPANSION_TARGET_MCTAG = "crafttweaker.api.tag.MCTag";
    public static final String EXPANSION_TARGET_INGREDIENT = "crafttweaker.api.item.IIngredient";
    public static final String EXPANSION_TARGET_IITEM_STACK = "crafttweaker.api.item.IItemStack";
    public static final String EXPANSION_TARGET_INGREDIENT_LIST = "crafttweaker.api.item.IngredientList";
    public static final String EXPANSION_TARGET_IFLUID_STACK = "crafttweaker.api.fluid.IFluidStack";

    //TODO: Figure out properly which things need registration and annotation and which don't in terms of concrete vs interfaces
    // We may be registering more things than needed currently, or at least naming more things than needed
    //TODO: We also should re-evaluate all the paths we use, as the impl stuff potentially shouldn't be in an api package
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
    public static final String CLASS_CHEMICAL_STACK_INGREDIENT = "mekanism.api.ingredient.ChemicalStackIngredient";
    //TODO: Do we want these to be just like .Gas or .GasStack
    public static final String CLASS_GAS_STACK_INGREDIENT = "mekanism.api.ingredient.ChemicalStackIngredient.GasStackIngredient";
    public static final String CLASS_INFUSION_STACK_INGREDIENT = "mekanism.api.ingredient.ChemicalStackIngredient.InfusionStackIngredient";
    public static final String CLASS_PIGMENT_STACK_INGREDIENT = "mekanism.api.ingredient.ChemicalStackIngredient.PigmentStackIngredient";
    public static final String CLASS_SLURRY_STACK_INGREDIENT = "mekanism.api.ingredient.ChemicalStackIngredient.SlurryStackIngredient";

    public static final String CLASS_RECIPE_MANAGER = "mekanism.recipe.MekanismRecipeManager";
    public static final String CLASS_RECIPE_ITEM_STACK_TO_ITEM_STACK = "mekanism.recipe.ItemStackToItemStack";
    public static final String CLASS_RECIPE_CRUSHING = "mekanism.recipe.ItemStackToItemStack.Crushing";
    public static final String CLASS_RECIPE_ENRICHING = "mekanism.recipe.ItemStackToItemStack.Enriching";
    public static final String CLASS_RECIPE_SMELTING = "mekanism.recipe.ItemStackToItemStack.Smelting";
    public static final String CLASS_RECIPE_CHEMICAL_INFUSING = "mekanism.recipe.ChemicalInfusing";
    public static final String CLASS_RECIPE_COMBINING = "mekanism.recipe.Combining";
    public static final String CLASS_RECIPE_SEPARATING = "mekanism.recipe.Separating";
    public static final String CLASS_RECIPE_FLUID_SLURRY_TO_SLURRY = "mekanism.recipe.FluidSlurryToSlurry";
    public static final String CLASS_RECIPE_WASHING = "mekanism.recipe.FluidSlurryToSlurry.Washing";
    public static final String CLASS_RECIPE_FLUID_TO_FLUID = "mekanism.recipe.FluidToFluid";
    public static final String CLASS_RECIPE_EVAPORATING = "mekanism.recipe.FluidToFluid.Evaporating";
    public static final String CLASS_RECIPE_GAS_TO_GAS = "mekanism.recipe.GasToGas";
    public static final String CLASS_RECIPE_ACTIVATING = "mekanism.recipe.GasToGas.Activating";
    public static final String CLASS_RECIPE_CENTRIFUGING = "mekanism.recipe.GasToGas.Centrifuging";
    public static final String CLASS_RECIPE_CRYSTALLIZING = "mekanism.recipe.Crystallizing";
    public static final String CLASS_RECIPE_DISSOLUTION = "mekanism.recipe.Dissolution";
    public static final String CLASS_RECIPE_ITEM_STACK_GAS_TO_ITEM_STACK = "mekanism.recipe.ItemStackGasToItemStack";
    public static final String CLASS_RECIPE_COMPRESSING = "mekanism.recipe.ItemStackGasToItemStack.Compressing";
    public static final String CLASS_RECIPE_PURIFYING = "mekanism.recipe.ItemStackGasToItemStack.Purifying";
    public static final String CLASS_RECIPE_INJECTING = "mekanism.recipe.ItemStackGasToItemStack.Injecting";
    public static final String CLASS_RECIPE_NUCLEOSYNTHESIZING = "mekanism.recipe.ItemStackGasToItemStack.Nucleosynthesizing";
    public static final String CLASS_RECIPE_ITEM_STACK_TO_ENERGY = "mekanism.recipe.ItemStackToEnergy";
    public static final String CLASS_RECIPE_ENERGY_CONVERSION = "mekanism.recipe.ItemStackToEnergy.EnergyConversion";
    public static final String CLASS_RECIPE_ITEM_STACK_TO_GAS = "mekanism.recipe.ItemStackToGas";
    public static final String CLASS_RECIPE_GAS_CONVERSION = "mekanism.recipe.ItemStackToGas.GasConversion";
    public static final String CLASS_RECIPE_OXIDIZING = "mekanism.recipe.ItemStackToGas.Oxidizing";
    public static final String CLASS_RECIPE_ITEM_STACK_TO_INFUSE_TYPE = "mekanism.recipe.ItemStackToInfuseType";
    public static final String CLASS_RECIPE_INFUSION_CONVERSION = "mekanism.recipe.ItemStackToInfuseType.InfusionConversion";
    public static final String CLASS_RECIPE_METALLURGIC_INFUSING = "mekanism.recipe.MetallurgicInfusing";
    public static final String CLASS_RECIPE_REACTION = "mekanism.recipe.Reaction";
    public static final String CLASS_RECIPE_ROTARY = "mekanism.recipe.Rotary";
    public static final String CLASS_RECIPE_SAWING = "mekanism.recipe.Sawing";
}