package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import mekanism.common.Mekanism;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Logger;

/**
 * Constants we use throughout our CraftTweaker integration.
 */
public class CrTConstants {

    public static final Logger CRT_LOGGER = CraftTweakerAPI.getLogger(Mekanism.MOD_NAME);

    public static final String CONTENT_LOADER = Mekanism.MODID + "content";
    public static final ResourceLocation CONTENT_LOADER_SOURCE_ID = Mekanism.rl("content");
    public static final String JEI_PLUGIN_NAME = Mekanism.MODID + ":crt_jei";

    public static final String BRACKET_CHEMICAL = "chemical";
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

    public static final String CLASS_CHEMICAL_PROVIDER = "mods." + Mekanism.MODID + ".api.chemical.ChemicalProvider";
    public static final String CLASS_CHEMICAL = "mods." + Mekanism.MODID + ".api.chemical.Chemical";
    public static final String CLASS_CHEMICAL_STACK = "mods." + Mekanism.MODID + ".api.chemical.ChemicalStack";

    public static final String CLASS_ROBIT = "mods." + Mekanism.MODID + ".api.entity.robit.Robit";
    public static final String CLASS_ROBIT_SKIN = "mods." + Mekanism.MODID + ".api.entity.robit.RobitSkin";

    public static final String CLASS_MODULE = "mods." + Mekanism.MODID + ".api.gear.Module";
    public static final String CLASS_MODULE_DATA = "mods." + Mekanism.MODID + ".api.gear.ModuleData";
    public static final String CLASS_MODULE_HELPER = "mods." + Mekanism.MODID + ".api.gear.ModuleHelper";
    public static final String CLASS_CUSTOM_MODULE = "mods." + Mekanism.MODID + ".api.gear.CustomModule";
    public static final String CLASS_MODULE_DATA_PROVIDER = "mods." + Mekanism.MODID + ".api.gear.ModuleDataProvider";

    //TODO: Eventually we might want to expose these as actually CrT objects. And then make the stack variant basically just be an OR of them,
    // and also make the stacks be instances of an interface so that they can be used directly in place of the ingredients? (Or at least implicit cast to them)
    public static final String CLASS_CHEMICAL_INGREDIENT = "mods." + Mekanism.MODID + ".api.ingredient.ChemicalIngredient";

    public static final String CLASS_CHEMICAL_STACK_INGREDIENT = "mods." + Mekanism.MODID + ".api.ingredient.ChemicalStackIngredient";

    public static final String CLASS_VANILLA_INPUT_BI_CHEMICAL = "mods." + Mekanism.MODID + ".recipe.input.BiChemical";
    public static final String CLASS_VANILLA_INPUT_CHEMICAL = "mods." + Mekanism.MODID + ".recipe.input.Chemical";
    public static final String CLASS_VANILLA_INPUT_FLUID_CHEMICAL = "mods." + Mekanism.MODID + ".recipe.input.FluidChemical";
    public static final String CLASS_VANILLA_INPUT_FLUID = "mods." + Mekanism.MODID + ".recipe.input.Fluid";
    public static final String CLASS_VANILLA_INPUT_ITEM_CHEMICAL = "mods." + Mekanism.MODID + ".recipe.input.ItemChemical";
    public static final String CLASS_VANILLA_INPUT_REACTION = "mods." + Mekanism.MODID + ".recipe.input.Reaction";
    public static final String CLASS_VANILLA_INPUT_ROTARY = "mods." + Mekanism.MODID + ".recipe.input.Rotary";
    public static final String CLASS_VANILLA_INPUT_SINGLE_BOXED = "mods." + Mekanism.MODID + ".recipe.input.SingleBoxedChemical";
    public static final String CLASS_VANILLA_INPUT_SINGLE_CHEMICAL = "mods." + Mekanism.MODID + ".recipe.input.SingleChemical";
    public static final String CLASS_VANILLA_INPUT_SINGLE_FLUID_CHEMICAL = "mods." + Mekanism.MODID + ".recipe.input.SingleFluidChemical";
    public static final String CLASS_VANILLA_INPUT_SINGLE_FLUID = "mods." + Mekanism.MODID + ".recipe.input.SingleFluid";
    public static final String CLASS_VANILLA_INPUT_SINGLE_ITEM_CHEMICAL = "mods." + Mekanism.MODID + ".recipe.input.SingleItemChemical";

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
    public static final String CLASS_RECIPE_MANAGER_CHEMICAL_TO_CHEMICAL = "mods." + Mekanism.MODID + ".recipe.manager.ChemicalToChemical";
    public static final String CLASS_RECIPE_MANAGER_ACTIVATING = CLASS_RECIPE_MANAGER_CHEMICAL_TO_CHEMICAL + ".Activating";
    public static final String CLASS_RECIPE_MANAGER_CENTRIFUGING = CLASS_RECIPE_MANAGER_CHEMICAL_TO_CHEMICAL + ".Centrifuging";
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
    public static final String CLASS_RECIPE_MANAGER_CHEMICAL_CONVERSION = CLASS_RECIPE_MANAGER_ITEM_STACK_TO_GAS + ".ChemicalConversion";
    public static final String CLASS_RECIPE_MANAGER_OXIDIZING = CLASS_RECIPE_MANAGER_ITEM_STACK_TO_GAS + ".Oxidizing";
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

    //Expansions, declared below classes we can reference the partial string parts of our other types
    private static final String EXPANSION_TARGET_MANY = "crafttweaker.api.util.Many";
    private static final String EXPANSION_TARGET_TAG = "crafttweaker.api.tag.type.KnownTag";
    public static final String EXPANSION_TARGET_NSS_RESOLVER = "mods.projecte.NSSResolver";
    public static final String EXPANSION_TARGET_CHEMICAL_TAG = EXPANSION_TARGET_TAG + "<" + CLASS_CHEMICAL + ">";
    public static final String EXPANSION_TARGET_CHEMICAL_AMOUNT_TAG = EXPANSION_TARGET_MANY + "<" + EXPANSION_TARGET_CHEMICAL_TAG + ">";
}