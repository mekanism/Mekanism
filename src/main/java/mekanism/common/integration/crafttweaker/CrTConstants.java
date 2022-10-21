package mekanism.common.integration.crafttweaker;

import mekanism.common.Mekanism;
import net.minecraft.resources.ResourceLocation;

/**
 * Constants we use throughout our CraftTweaker integration.
 */
public class CrTConstants {

    public static final String CONTENT_LOADER = Mekanism.MODID + "content";
    public static final ResourceLocation CONTENT_LOADER_SOURCE_ID = Mekanism.rl("content");

    public static final String BRACKET_GAS = "gas";
    public static final String BRACKET_INFUSE_TYPE = "infuse_type";
    public static final String BRACKET_PIGMENT = "pigment";
    public static final String BRACKET_SLURRY = "slurry";
    public static final String BRACKET_ROBIT_SKIN = "robit_skin";
    public static final String BRACKET_MODULE_DATA = "module_data";

    public static final String CLASS_BRACKET_DUMPERS = "mods." + Mekanism.MODID + ".api.BracketDumpers";
    public static final String CLASS_BRACKET_HANDLER = "mods." + Mekanism.MODID + ".api.BracketHandlers";
    public static final String CLASS_BRACKET_VALIDATORS = "mods." + Mekanism.MODID + ".api.BracketValidators";

    public static final String CLASS_HAS_TRANSLATION = "mods." + Mekanism.MODID + ".api.text.HasTranslation";
    public static final String CLASS_HAS_TEXT_COMPONENT = "mods." + Mekanism.MODID + ".api.text.HasTextComponent";
    public static final String CLASS_BASE_PROVIDER = "mods." + Mekanism.MODID + ".api.provider.BaseProvider";

    public static final String CLASS_CHEMICAL = "mods." + Mekanism.MODID + ".api.chemical.Chemical";
    public static final String CLASS_GAS = "mods." + Mekanism.MODID + ".api.chemical.Gas";
    public static final String CLASS_INFUSE_TYPE = "mods." + Mekanism.MODID + ".api.chemical.InfuseType";
    public static final String CLASS_PIGMENT = "mods." + Mekanism.MODID + ".api.chemical.Pigment";
    public static final String CLASS_SLURRY = "mods." + Mekanism.MODID + ".api.chemical.Slurry";
    public static final String CLASS_CHEMICAL_STACK = "mods." + Mekanism.MODID + ".api.chemical.ChemicalStack";
    public static final String CLASS_GAS_STACK = "mods." + Mekanism.MODID + ".api.chemical.GasStack";
    public static final String CLASS_INFUSION_STACK = "mods." + Mekanism.MODID + ".api.chemical.InfusionStack";
    public static final String CLASS_PIGMENT_STACK = "mods." + Mekanism.MODID + ".api.chemical.PigmentStack";
    public static final String CLASS_SLURRY_STACK = "mods." + Mekanism.MODID + ".api.chemical.SlurryStack";

    public static final String CLASS_ROBIT = "mods." + Mekanism.MODID + ".api.entity.robit.Robit";
    public static final String CLASS_ROBIT_SKIN = "mods." + Mekanism.MODID + ".api.entity.robit.RobitSkin";

    public static final String CLASS_MODULE = "mods." + Mekanism.MODID + ".api.gear.Module";
    public static final String CLASS_MODULE_DATA = "mods." + Mekanism.MODID + ".api.gear.ModuleData";
    public static final String CLASS_MODULE_HELPER = "mods." + Mekanism.MODID + ".api.gear.ModuleHelper";
    public static final String CLASS_CUSTOM_MODULE = "mods." + Mekanism.MODID + ".api.gear.CustomModule";
    public static final String CLASS_MODULE_DATA_PROVIDER = "mods." + Mekanism.MODID + ".api.gear.ModuleDataProvider";

    public static final String CLASS_FLOATING_LONG = "mods." + Mekanism.MODID + ".api.FloatingLong";
    public static final String CLASS_ITEM_STACK_INGREDIENT = "mods." + Mekanism.MODID + ".api.ingredient.ItemStackIngredient";
    public static final String CLASS_FLUID_STACK_INGREDIENT = "mods." + Mekanism.MODID + ".api.ingredient.FluidStackIngredient";
    public static final String CLASS_CHEMICAL_STACK_INGREDIENT = "mods." + Mekanism.MODID + ".api.ingredient.ChemicalStackIngredient";
    public static final String CLASS_GAS_STACK_INGREDIENT = CLASS_CHEMICAL_STACK_INGREDIENT + ".GasStackIngredient";
    public static final String CLASS_INFUSION_STACK_INGREDIENT = CLASS_CHEMICAL_STACK_INGREDIENT + ".InfusionStackIngredient";
    public static final String CLASS_PIGMENT_STACK_INGREDIENT = CLASS_CHEMICAL_STACK_INGREDIENT + ".PigmentStackIngredient";
    public static final String CLASS_SLURRY_STACK_INGREDIENT = CLASS_CHEMICAL_STACK_INGREDIENT + ".SlurryStackIngredient";

    public static final String CLASS_RECIPE_MANAGER = "mods." + Mekanism.MODID + ".recipe.manager.MekanismRecipe";
    public static final String CLASS_RECIPE_MANAGER_ITEM_STACK_TO_ITEM_STACK = "mods." + Mekanism.MODID + ".recipe.manager.ItemStackToItemStack";
    public static final String CLASS_RECIPE_MANAGER_CRUSHING = CLASS_RECIPE_MANAGER_ITEM_STACK_TO_ITEM_STACK + ".Crushing";
    public static final String CLASS_RECIPE_MANAGER_ENRICHING = CLASS_RECIPE_MANAGER_ITEM_STACK_TO_ITEM_STACK + ".Enriching";
    public static final String CLASS_RECIPE_MANAGER_SMELTING = CLASS_RECIPE_MANAGER_ITEM_STACK_TO_ITEM_STACK + ".Smelting";
    public static final String CLASS_RECIPE_MANAGER_CHEMICAL_CHEMICAL_TO_CHEMICAL = "mods." + Mekanism.MODID + ".recipe.manager.ChemicalChemicalToChemical";
    public static final String CLASS_RECIPE_MANAGER_CHEMICAL_INFUSING = CLASS_RECIPE_MANAGER_CHEMICAL_CHEMICAL_TO_CHEMICAL + ".ChemicalInfusing";
    public static final String CLASS_RECIPE_MANAGER_PIGMENT_MIXING = CLASS_RECIPE_MANAGER_CHEMICAL_CHEMICAL_TO_CHEMICAL + ".PigmentMixing";
    public static final String CLASS_RECIPE_MANAGER_COMBINING = "mods." + Mekanism.MODID + ".recipe.manager.Combining";
    public static final String CLASS_RECIPE_MANAGER_SEPARATING = "mods." + Mekanism.MODID + ".recipe.manager.Separating";
    public static final String CLASS_RECIPE_MANAGER_FLUID_SLURRY_TO_SLURRY = "mods." + Mekanism.MODID + ".recipe.manager.FluidSlurryToSlurry";
    public static final String CLASS_RECIPE_MANAGER_WASHING = CLASS_RECIPE_MANAGER_FLUID_SLURRY_TO_SLURRY + ".Washing";
    public static final String CLASS_RECIPE_MANAGER_FLUID_TO_FLUID = "mods." + Mekanism.MODID + ".recipe.manager.FluidToFluid";
    public static final String CLASS_RECIPE_MANAGER_EVAPORATING = CLASS_RECIPE_MANAGER_FLUID_TO_FLUID + ".Evaporating";
    public static final String CLASS_RECIPE_MANAGER_GAS_TO_GAS = "mods." + Mekanism.MODID + ".recipe.manager.GasToGas";
    public static final String CLASS_RECIPE_MANAGER_ACTIVATING = CLASS_RECIPE_MANAGER_GAS_TO_GAS + ".Activating";
    public static final String CLASS_RECIPE_MANAGER_CENTRIFUGING = CLASS_RECIPE_MANAGER_GAS_TO_GAS + ".Centrifuging";
    public static final String CLASS_RECIPE_MANAGER_CRYSTALLIZING = "mods." + Mekanism.MODID + ".recipe.manager.Crystallizing";
    public static final String CLASS_RECIPE_MANAGER_DISSOLUTION = "mods." + Mekanism.MODID + ".recipe.manager.Dissolution";
    public static final String CLASS_RECIPE_MANAGER_ITEM_STACK_CHEMICAL_TO_ITEM_STACK = "mods." + Mekanism.MODID + ".recipe.manager.ItemStackChemicalToItemStack";
    public static final String CLASS_RECIPE_MANAGER_COMPRESSING = CLASS_RECIPE_MANAGER_ITEM_STACK_CHEMICAL_TO_ITEM_STACK + ".Compressing";
    public static final String CLASS_RECIPE_MANAGER_PURIFYING = CLASS_RECIPE_MANAGER_ITEM_STACK_CHEMICAL_TO_ITEM_STACK + ".Purifying";
    public static final String CLASS_RECIPE_MANAGER_INJECTING = CLASS_RECIPE_MANAGER_ITEM_STACK_CHEMICAL_TO_ITEM_STACK + ".Injecting";
    public static final String CLASS_RECIPE_MANAGER_METALLURGIC_INFUSING = CLASS_RECIPE_MANAGER_ITEM_STACK_CHEMICAL_TO_ITEM_STACK + ".MetallurgicInfusing";
    public static final String CLASS_RECIPE_MANAGER_PAINTING = CLASS_RECIPE_MANAGER_ITEM_STACK_CHEMICAL_TO_ITEM_STACK + ".Painting";
    public static final String CLASS_RECIPE_MANAGER_NUCLEOSYNTHESIZING = "mods." + Mekanism.MODID + ".recipe.manager.Nucleosynthesizing";
    public static final String CLASS_RECIPE_MANAGER_ITEM_STACK_TO_ENERGY = "mods." + Mekanism.MODID + ".recipe.manager.ItemStackToEnergy";
    public static final String CLASS_RECIPE_MANAGER_ENERGY_CONVERSION = CLASS_RECIPE_MANAGER_ITEM_STACK_TO_ENERGY + ".EnergyConversion";
    public static final String CLASS_RECIPE_MANAGER_ITEM_STACK_TO_CHEMICAL = "mods." + Mekanism.MODID + ".recipe.manager.ItemStackToChemical";
    public static final String CLASS_RECIPE_MANAGER_ITEM_STACK_TO_GAS = CLASS_RECIPE_MANAGER_ITEM_STACK_TO_CHEMICAL + ".Gas";
    public static final String CLASS_RECIPE_MANAGER_GAS_CONVERSION = CLASS_RECIPE_MANAGER_ITEM_STACK_TO_GAS + ".GasConversion";
    public static final String CLASS_RECIPE_MANAGER_OXIDIZING = CLASS_RECIPE_MANAGER_ITEM_STACK_TO_GAS + ".Oxidizing";
    public static final String CLASS_RECIPE_MANAGER_ITEM_STACK_TO_INFUSE_TYPE = CLASS_RECIPE_MANAGER_ITEM_STACK_TO_CHEMICAL + ".InfuseType";
    public static final String CLASS_RECIPE_MANAGER_INFUSION_CONVERSION = CLASS_RECIPE_MANAGER_ITEM_STACK_TO_INFUSE_TYPE + ".InfusionConversion";
    public static final String CLASS_RECIPE_MANAGER_ITEM_STACK_TO_PIGMENT = CLASS_RECIPE_MANAGER_ITEM_STACK_TO_CHEMICAL + ".Pigment";
    public static final String CLASS_RECIPE_MANAGER_PIGMENT_EXTRACTING = CLASS_RECIPE_MANAGER_ITEM_STACK_TO_PIGMENT + ".PigmentExtracting";
    public static final String CLASS_RECIPE_MANAGER_REACTION = "mods." + Mekanism.MODID + ".recipe.manager.Reaction";
    public static final String CLASS_RECIPE_MANAGER_ROTARY = "mods." + Mekanism.MODID + ".recipe.manager.Rotary";
    public static final String CLASS_RECIPE_MANAGER_SAWING = "mods." + Mekanism.MODID + ".recipe.manager.Sawing";

    public static final String CLASS_RECIPE_ITEM_STACK_TO_ITEM_STACK = "mods." + Mekanism.MODID + ".recipe.ItemStackToItemStack";
    public static final String CLASS_RECIPE_CHEMICAL_CHEMICAL_TO_CHEMICAL = "mods." + Mekanism.MODID + ".recipe.ChemicalChemicalToChemical";
    public static final String CLASS_RECIPE_CHEMICAL_INFUSING = CLASS_RECIPE_CHEMICAL_CHEMICAL_TO_CHEMICAL + ".ChemicalInfusing";
    public static final String CLASS_RECIPE_PIGMENT_MIXING = "mods." + Mekanism.MODID + ".recipe.PigmentMixing";
    public static final String CLASS_RECIPE_COMBINING = "mods." + Mekanism.MODID + ".recipe.Combining";
    public static final String CLASS_RECIPE_SEPARATING = "mods." + Mekanism.MODID + ".recipe.Separating";
    public static final String CLASS_RECIPE_SEPARATING_OUTPUT = CLASS_RECIPE_SEPARATING + ".Output";
    public static final String CLASS_RECIPE_FLUID_CHEMICAL_TO_CHEMICAL = "mods." + Mekanism.MODID + ".recipe.FluidChemicalToChemical";
    public static final String CLASS_RECIPE_FLUID_SLURRY_TO_SLURRY = CLASS_RECIPE_FLUID_CHEMICAL_TO_CHEMICAL + ".Slurry";
    public static final String CLASS_RECIPE_FLUID_TO_FLUID = "mods." + Mekanism.MODID + ".recipe.FluidToFluid";
    public static final String CLASS_RECIPE_CHEMICAL_TO_CHEMICAL = "mods." + Mekanism.MODID + ".recipe.ChemicalToChemical";
    public static final String CLASS_RECIPE_GAS_TO_GAS = CLASS_RECIPE_CHEMICAL_TO_CHEMICAL + ".GasToGas";
    public static final String CLASS_RECIPE_CRYSTALLIZING = "mods." + Mekanism.MODID + ".recipe.Crystallizing";
    public static final String CLASS_RECIPE_DISSOLUTION = "mods." + Mekanism.MODID + ".recipe.Dissolution";
    public static final String CLASS_RECIPE_ITEM_STACK_CHEMICAL_TO_ITEM_STACK = "mods." + Mekanism.MODID + ".recipe.ItemStackChemicalToItemStack";
    public static final String CLASS_RECIPE_ITEM_STACK_GAS_TO_ITEM_STACK = CLASS_RECIPE_ITEM_STACK_CHEMICAL_TO_ITEM_STACK + ".Gas";
    public static final String CLASS_RECIPE_METALLURGIC_INFUSING = CLASS_RECIPE_ITEM_STACK_CHEMICAL_TO_ITEM_STACK + ".MetallurgicInfusing";
    public static final String CLASS_RECIPE_PAINTING = CLASS_RECIPE_ITEM_STACK_CHEMICAL_TO_ITEM_STACK + ".Painting";
    public static final String CLASS_RECIPE_NUCLEOSYNTHESIZING = CLASS_RECIPE_ITEM_STACK_GAS_TO_ITEM_STACK + ".Nucleosynthesizing";
    public static final String CLASS_RECIPE_ITEM_STACK_TO_ENERGY = "mods." + Mekanism.MODID + ".recipe.ItemStackToEnergy";
    public static final String CLASS_RECIPE_ITEM_STACK_TO_CHEMICAL = "mods." + Mekanism.MODID + ".recipe.ItemStackToChemical";
    public static final String CLASS_RECIPE_ITEM_STACK_TO_GAS = CLASS_RECIPE_ITEM_STACK_TO_CHEMICAL + ".ItemStackToGas";
    public static final String CLASS_RECIPE_ITEM_STACK_TO_INFUSE_TYPE = CLASS_RECIPE_ITEM_STACK_TO_CHEMICAL + ".ItemStackToInfuseType";
    public static final String CLASS_RECIPE_ITEM_STACK_TO_PIGMENT = CLASS_RECIPE_ITEM_STACK_TO_CHEMICAL + ".ItemStackToPigment";
    public static final String CLASS_RECIPE_REACTION = "mods." + Mekanism.MODID + ".recipe.Reaction";
    public static final String CLASS_RECIPE_REACTION_OUTPUT = CLASS_RECIPE_REACTION + ".Output";
    public static final String CLASS_RECIPE_ROTARY = "mods." + Mekanism.MODID + ".recipe.Rotary";
    public static final String CLASS_RECIPE_ROTARY_GAS_TO_FLUID = CLASS_RECIPE_ROTARY + ".GasToFluid";
    public static final String CLASS_RECIPE_ROTARY_FLUID_TO_GAS = CLASS_RECIPE_ROTARY + ".FluidToGas";
    public static final String CLASS_RECIPE_SAWING = "mods." + Mekanism.MODID + ".recipe.Sawing";

    public static final String CLASS_ATTRIBUTE_CHEMICAL = "mods." + Mekanism.MODID + ".attribute.ChemicalAttribute";
    public static final String CLASS_ATTRIBUTE_COOLANT = "mods." + Mekanism.MODID + ".attribute.gas.CoolantAttribute";
    public static final String CLASS_ATTRIBUTE_COOLED_COOLANT = "mods." + Mekanism.MODID + ".attribute.gas.CooledCoolantAttribute";
    public static final String CLASS_ATTRIBUTE_HEATED_COOLANT = "mods." + Mekanism.MODID + ".attribute.gas.HeatedCoolantAttribute";
    public static final String CLASS_ATTRIBUTE_FUEL = "mods." + Mekanism.MODID + ".attribute.gas.FuelAttribute";
    public static final String CLASS_ATTRIBUTE_RADIATION = "mods." + Mekanism.MODID + ".attribute.gas.RadiationAttribute";

    public static final String CLASS_BUILDER_CHEMICAL = "mods." + Mekanism.MODID + ".content.builder.ChemicalBuilder";
    public static final String CLASS_BUILDER_GAS = "mods." + Mekanism.MODID + ".content.builder.GasBuilder";
    public static final String CLASS_BUILDER_INFUSE_TYPE = "mods." + Mekanism.MODID + ".content.builder.InfuseTypeBuilder";
    public static final String CLASS_BUILDER_PIGMENT = "mods." + Mekanism.MODID + ".content.builder.PigmentBuilder";
    public static final String CLASS_BUILDER_SLURRY = "mods." + Mekanism.MODID + ".content.builder.SlurryBuilder";

    public static final String CLASS_BUILDER_ROBIT_SKIN = "mods." + Mekanism.MODID + ".content.builder.RobitSkinBuilder";

    //Expansions, declared below classes we can reference the partial string parts of our other types
    private static final String EXPANSION_TARGET_MANY = "crafttweaker.api.util.Many";
    private static final String EXPANSION_TARGET_TAG = "crafttweaker.api.tag.type.KnownTag";
    public static final String EXPANSION_TARGET_NSS_RESOLVER = "mods.projecte.NSSResolver";
    public static final String EXPANSION_TARGET_ITEM_TAG = EXPANSION_TARGET_TAG + "<crafttweaker.api.item.ItemDefinition>";
    public static final String EXPANSION_TARGET_GAS_TAG = EXPANSION_TARGET_TAG + "<" + CLASS_GAS + ">";
    public static final String EXPANSION_TARGET_INFUSE_TYPE_TAG = EXPANSION_TARGET_TAG + "<" + CLASS_INFUSE_TYPE + ">";
    public static final String EXPANSION_TARGET_PIGMENT_TAG = EXPANSION_TARGET_TAG + "<" + CLASS_PIGMENT + ">";
    public static final String EXPANSION_TARGET_SLURRY_TAG = EXPANSION_TARGET_TAG + "<" + CLASS_SLURRY + ">";
    public static final String EXPANSION_TARGET_ITEM_AMOUNT_TAG = EXPANSION_TARGET_MANY + "<" + EXPANSION_TARGET_ITEM_TAG + ">";
    public static final String EXPANSION_TARGET_FLUID_AMOUNT_TAG = EXPANSION_TARGET_MANY + "<" + EXPANSION_TARGET_TAG + "<crafttweaker.api.fluid.Fluid>>";
    public static final String EXPANSION_TARGET_GAS_AMOUNT_TAG = EXPANSION_TARGET_MANY + "<" + EXPANSION_TARGET_GAS_TAG + ">";
    public static final String EXPANSION_TARGET_INFUSE_TYPE_AMOUNT_TAG = EXPANSION_TARGET_MANY + "<" + EXPANSION_TARGET_INFUSE_TYPE_TAG + ">";
    public static final String EXPANSION_TARGET_PIGMENT_AMOUNT_TAG = EXPANSION_TARGET_MANY + "<" + EXPANSION_TARGET_PIGMENT_TAG + ">";
    public static final String EXPANSION_TARGET_SLURRY_AMOUNT_TAG = EXPANSION_TARGET_MANY + "<" + EXPANSION_TARGET_SLURRY_TAG + ">";
}