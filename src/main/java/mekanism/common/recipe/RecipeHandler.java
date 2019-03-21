package mekanism.common.recipe;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfuseType;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.inputs.ChemicalPairInput;
import mekanism.common.recipe.inputs.DoubleMachineInput;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.inputs.GasInput;
import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.recipe.inputs.IntegerInput;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.inputs.PressurizedInput;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.recipe.machines.AmbientGasRecipe;
import mekanism.common.recipe.machines.BasicMachineRecipe;
import mekanism.common.recipe.machines.ChanceMachineRecipe;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mekanism.common.recipe.machines.CombinerRecipe;
import mekanism.common.recipe.machines.CrusherRecipe;
import mekanism.common.recipe.machines.CrystallizerRecipe;
import mekanism.common.recipe.machines.DissolutionRecipe;
import mekanism.common.recipe.machines.DoubleMachineRecipe;
import mekanism.common.recipe.machines.EnrichmentRecipe;
import mekanism.common.recipe.machines.InjectionRecipe;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mekanism.common.recipe.machines.OsmiumCompressorRecipe;
import mekanism.common.recipe.machines.OxidationRecipe;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mekanism.common.recipe.machines.PurificationRecipe;
import mekanism.common.recipe.machines.SawmillRecipe;
import mekanism.common.recipe.machines.SeparatorRecipe;
import mekanism.common.recipe.machines.SmeltingRecipe;
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import mekanism.common.recipe.machines.WasherRecipe;
import mekanism.common.recipe.outputs.ChanceOutput;
import mekanism.common.recipe.outputs.ChemicalPairOutput;
import mekanism.common.recipe.outputs.FluidOutput;
import mekanism.common.recipe.outputs.GasOutput;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mekanism.common.recipe.outputs.MachineOutput;
import mekanism.common.recipe.outputs.PressurizedOutput;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Class used to handle machine recipes. This is used for both adding and fetching recipes.
 *
 * @author AidanBrady, unpairedbracket
 */
public final class RecipeHandler {

    public static void addRecipe(Recipe recipeMap, MachineRecipe recipe) {
        recipeMap.put(recipe);
    }

    public static void removeRecipe(Recipe recipeMap, MachineRecipe recipe) {
        List<MachineInput> toRemove = new ArrayList<>();

        for (Object o : recipeMap.get().keySet()) {
            MachineInput iterInput = (MachineInput) o;

            if (iterInput.testEquality(recipe.getInput())) {
                toRemove.add(iterInput);
            }
        }

        for (MachineInput iterInput : toRemove) {
            recipeMap.get().remove(iterInput);
        }
    }

    /**
     * Add an Enrichment Chamber recipe.
     *
     * @param input - input ItemStack
     * @param output - output ItemStack
     */
    public static void addEnrichmentChamberRecipe(ItemStack input, ItemStack output) {
        addRecipe(Recipe.ENRICHMENT_CHAMBER, new EnrichmentRecipe(input, output));
    }

    /**
     * Add an Osmium Compressor recipe.
     *
     * @param input - input ItemStack
     * @param output - output ItemStack
     */
    public static void addOsmiumCompressorRecipe(ItemStack input, ItemStack output) {
        addRecipe(Recipe.OSMIUM_COMPRESSOR, new OsmiumCompressorRecipe(input, output));
    }

    /**
     * Add a Combiner recipe.
     *
     * @param input - input ItemStack
     * @param output - output ItemStack
     * @deprecated Replaced by {@link #addCombinerRecipe(ItemStack, ItemStack, ItemStack)}. May be removed with
     * Minecraft 1.13.
     */
    @Deprecated
    public static void addCombinerRecipe(ItemStack input, ItemStack output) {
        addRecipe(Recipe.COMBINER, new CombinerRecipe(input, output));
    }

    /**
     * Add a Combiner recipe.
     *
     * @param input - input ItemStack
     * @param extra - extra ItemStack
     * @param output - output ItemStack
     */
    public static void addCombinerRecipe(ItemStack input, ItemStack extra, ItemStack output) {
        addRecipe(Recipe.COMBINER, new CombinerRecipe(input, extra, output));
    }

    /**
     * Add a Crusher recipe.
     *
     * @param input - input ItemStack
     * @param output - output ItemStack
     */
    public static void addCrusherRecipe(ItemStack input, ItemStack output) {
        addRecipe(Recipe.CRUSHER, new CrusherRecipe(input, output));
    }

    /**
     * Add a Purification Chamber recipe.
     *
     * @param input - input ItemStack
     * @param output - output ItemStack
     */
    public static void addPurificationChamberRecipe(ItemStack input, ItemStack output) {
        addRecipe(Recipe.PURIFICATION_CHAMBER, new PurificationRecipe(input, output));
    }

    /**
     * Add a Metallurgic Infuser recipe.
     *
     * @param infuse - which Infuse to use
     * @param amount - how much of the Infuse to use
     * @param input - input ItemStack
     * @param output - output ItemStack
     */
    public static void addMetallurgicInfuserRecipe(InfuseType infuse, int amount, ItemStack input, ItemStack output) {
        addRecipe(Recipe.METALLURGIC_INFUSER,
              new MetallurgicInfuserRecipe(new InfusionInput(infuse, amount, input), output));
    }

    /**
     * Add a Chemical Infuser recipe.
     *
     * @param leftInput - left GasStack to input
     * @param rightInput - right GasStack to input
     * @param output - output GasStack
     */
    public static void addChemicalInfuserRecipe(GasStack leftInput, GasStack rightInput, GasStack output) {
        addRecipe(Recipe.CHEMICAL_INFUSER, new ChemicalInfuserRecipe(leftInput, rightInput, output));
    }

    /**
     * Add a Chemical Oxidizer recipe.
     *
     * @param input - input ItemStack
     * @param output - output GasStack
     */
    public static void addChemicalOxidizerRecipe(ItemStack input, GasStack output) {
        addRecipe(Recipe.CHEMICAL_OXIDIZER, new OxidationRecipe(input, output));
    }

    /**
     * Add a Chemical Injection Chamber recipe.
     *
     * @param input - input ItemStack
     * @param output - output ItemStack
     */
    public static void addChemicalInjectionChamberRecipe(ItemStack input, Gas gas, ItemStack output) {
        addRecipe(Recipe.CHEMICAL_INJECTION_CHAMBER, new InjectionRecipe(input, gas, output));
    }

    /**
     * Add an Electrolytic Separator recipe.
     *
     * @param fluid - FluidStack to electrolyze
     * @param leftOutput - left gas to produce when the fluid is electrolyzed
     * @param rightOutput - right gas to produce when the fluid is electrolyzed
     */
    public static void addElectrolyticSeparatorRecipe(FluidStack fluid, double energy, GasStack leftOutput,
          GasStack rightOutput) {
        addRecipe(Recipe.ELECTROLYTIC_SEPARATOR, new SeparatorRecipe(fluid, energy, leftOutput, rightOutput));
    }

    /**
     * Add a Precision Sawmill recipe.
     *
     * @param input - input ItemStack
     * @param primaryOutput - guaranteed output
     * @param secondaryOutput - possible extra output
     * @param chance - probability of obtaining extra output
     */
    public static void addPrecisionSawmillRecipe(ItemStack input, ItemStack primaryOutput, ItemStack secondaryOutput,
          double chance) {
        addRecipe(Recipe.PRECISION_SAWMILL, new SawmillRecipe(input, primaryOutput, secondaryOutput, chance));
    }

    /**
     * Add a Precision Sawmill recipe with no chance output
     *
     * @param input - input ItemStack
     * @param primaryOutput - guaranteed output
     */
    public static void addPrecisionSawmillRecipe(ItemStack input, ItemStack primaryOutput) {
        addRecipe(Recipe.PRECISION_SAWMILL, new SawmillRecipe(input, primaryOutput));
    }

    /**
     * Add a Chemical Dissolution Chamber recipe.
     *
     * @param input - input ItemStack
     * @param output - output GasStack
     */
    public static void addChemicalDissolutionChamberRecipe(ItemStack input, GasStack output) {
        addRecipe(Recipe.CHEMICAL_DISSOLUTION_CHAMBER, new DissolutionRecipe(input, output));
    }

    /**
     * Add a Chemical Washer recipe.
     *
     * @param input - input GasStack
     * @param output - output GasStack
     */
    public static void addChemicalWasherRecipe(GasStack input, GasStack output) {
        addRecipe(Recipe.CHEMICAL_WASHER, new WasherRecipe(input, output));
    }

    /**
     * Add a Chemical Crystallizer recipe.
     *
     * @param input - input GasStack
     * @param output - output ItemStack
     */
    public static void addChemicalCrystallizerRecipe(GasStack input, ItemStack output) {
        addRecipe(Recipe.CHEMICAL_CRYSTALLIZER, new CrystallizerRecipe(input, output));
    }

    /**
     * Add a Pressurized Reaction Chamber recipe.
     *
     * @param inputSolid - input ItemStack
     * @param inputFluid - input FluidStack
     * @param inputGas - input GasStack
     * @param outputSolid - output ItemStack
     * @param outputGas - output GasStack
     * @param extraEnergy - extra energy needed by the recipe
     * @param ticks - amount of ticks it takes for this recipe to complete
     */
    public static void addPRCRecipe(ItemStack inputSolid, FluidStack inputFluid, GasStack inputGas,
          ItemStack outputSolid, GasStack outputGas, double extraEnergy, int ticks) {
        addRecipe(Recipe.PRESSURIZED_REACTION_CHAMBER,
              new PressurizedRecipe(inputSolid, inputFluid, inputGas, outputSolid, outputGas, extraEnergy, ticks));
    }

    public static void addThermalEvaporationRecipe(FluidStack inputFluid, FluidStack outputFluid) {
        addRecipe(Recipe.THERMAL_EVAPORATION_PLANT, new ThermalEvaporationRecipe(inputFluid, outputFluid));
    }

    public static void addSolarNeutronRecipe(GasStack inputGas, GasStack outputGas) {
        addRecipe(Recipe.SOLAR_NEUTRON_ACTIVATOR, new SolarNeutronRecipe(inputGas, outputGas));
    }

    public static void addAmbientGas(int dimensionID, String ambientGasName) {
        addRecipe(Recipe.AMBIENT_ACCUMULATOR, new AmbientGasRecipe(dimensionID, ambientGasName));
    }

    /**
     * Gets the Metallurgic Infuser Recipe for the InfusionInput in the parameters.
     *
     * @param input - input Infusion
     * @return MetallurgicInfuserRecipe
     */
    public static MetallurgicInfuserRecipe getMetallurgicInfuserRecipe(InfusionInput input) {
        if (input.isValid()) {
            Map<InfusionInput, MetallurgicInfuserRecipe> recipes = Recipe.METALLURGIC_INFUSER.get();

            MetallurgicInfuserRecipe recipe = recipes.get(input);

            if (recipe == null) {
                recipe = recipes.get(input.wildCopy());
            }

            return recipe == null ? null : recipe.copy();
        }

        return null;
    }

    /**
     * Gets the Chemical Infuser Recipe of the ChemicalPairInput in the parameters.
     *
     * @param input - the pair of gases to infuse
     * @return ChemicalInfuserRecipe
     */
    public static ChemicalInfuserRecipe getChemicalInfuserRecipe(ChemicalPairInput input) {
        if (input.isValid()) {
            Map<ChemicalPairInput, ChemicalInfuserRecipe> recipes = Recipe.CHEMICAL_INFUSER.get();

            ChemicalInfuserRecipe recipe = recipes.get(input);
            return recipe == null ? null : recipe.copy();
        }

        return null;
    }

    /**
     * Gets the Chemical Crystallizer Recipe for the defined Gas input.
     *
     * @param input - GasInput
     * @return CrystallizerRecipe
     */
    public static CrystallizerRecipe getChemicalCrystallizerRecipe(GasInput input) {
        if (input.isValid()) {
            Map<GasInput, CrystallizerRecipe> recipes = Recipe.CHEMICAL_CRYSTALLIZER.get();

            CrystallizerRecipe recipe = recipes.get(input);
            return recipe == null ? null : recipe.copy();
        }

        return null;
    }

    /**
     * Gets the Chemical Washer Recipe for the defined Gas input.
     *
     * @param input - GasInput
     * @return WasherRecipe
     */
    public static WasherRecipe getChemicalWasherRecipe(GasInput input) {
        if (input.isValid()) {
            Map<GasInput, WasherRecipe> recipes = Recipe.CHEMICAL_WASHER.get();

            WasherRecipe recipe = recipes.get(input);
            return recipe == null ? null : recipe.copy();
        }

        return null;
    }

    /**
     * Gets the Chemical Dissolution Chamber of the ItemStackInput in the parameters
     *
     * @param input - ItemStackInput
     * @return DissolutionRecipe
     */
    public static DissolutionRecipe getDissolutionRecipe(ItemStackInput input) {
        if (input.isValid()) {
            Map<ItemStackInput, DissolutionRecipe> recipes = Recipe.CHEMICAL_DISSOLUTION_CHAMBER.get();

            DissolutionRecipe recipe = getRecipeTryWildcard(input, recipes);
            return recipe == null ? null : recipe.copy();
        }

        return null;
    }

    /**
     * Gets the Chemical Oxidizer Recipe for the ItemStackInput in the parameters.
     *
     * @param input - ItemStackInput
     * @return OxidationRecipe
     */
    public static OxidationRecipe getOxidizerRecipe(ItemStackInput input) {
        if (input.isValid()) {
            Map<ItemStackInput, OxidationRecipe> recipes = Recipe.CHEMICAL_OXIDIZER.get();

            OxidationRecipe recipe = getRecipeTryWildcard(input, recipes);
            return recipe == null ? null : recipe.copy();
        }

        return null;
    }

    /**
     * Gets the ChanceMachineRecipe of the ItemStackInput in the parameters, using the map in the parameters.
     *
     * @param input - ItemStackInput
     * @param recipes - Map of recipes
     * @return ChanceRecipe
     */
    public static <RECIPE extends ChanceMachineRecipe<RECIPE>> RECIPE getChanceRecipe(ItemStackInput input,
          Map<ItemStackInput, RECIPE> recipes) {
        if (input.isValid()) {
            RECIPE recipe = getRecipeTryWildcard(input, recipes);
            return recipe == null ? null : recipe.copy();
        }

        return null;
    }

    /**
     * Gets the BasicMachineRecipe of the ItemStackInput in the parameters, using the map in the parameters.
     *
     * @param input - ItemStackInput
     * @param recipes - Map of recipes
     * @return BasicMachineRecipe
     */
    public static <RECIPE extends BasicMachineRecipe<RECIPE>> RECIPE getRecipe(ItemStackInput input,
          Map<ItemStackInput, RECIPE> recipes) {
        if (input.isValid()) {
            RECIPE recipe = getRecipeTryWildcard(input, recipes);
            return recipe == null ? null : recipe.copy();
        }

        return null;
    }

    /**
     * Gets the AdvancedMachineRecipe of the AdvancedInput in the parameters, using the map in the paramaters.
     *
     * @param input - AdvancedInput
     * @param recipes - Map of recipes
     * @return AdvancedMachineRecipe
     */
    public static <RECIPE extends AdvancedMachineRecipe<RECIPE>> RECIPE getRecipe(AdvancedMachineInput input,
          Map<AdvancedMachineInput, RECIPE> recipes) {
        if (input.isValid()) {
            RECIPE recipe = recipes.get(input);

            if (recipe == null) {
                recipe = recipes.get(input.wildCopy());
            }

            return recipe == null ? null : recipe.copy();
        }

        return null;
    }


    /**
     * Gets the DoubleMachineRecipe of the DoubleInput in the parameters, using the map in the paramaters.
     *
     * @param input - DoubleInput
     * @param recipes - Map of recipes
     * @return DoubleMachineRecipe
     */
    public static <RECIPE extends DoubleMachineRecipe<RECIPE>> RECIPE getRecipe(DoubleMachineInput input,
          Map<DoubleMachineInput, RECIPE> recipes) {
        if (input.isValid()) {
            RECIPE recipe = recipes.get(input);

            if (recipe == null) {
                recipe = recipes.get(input.wildCopy());
            }

            return recipe == null ? null : recipe.copy();
        }

        return null;
    }

    /**
     * Get the Electrolytic Separator Recipe corresponding to electrolysing a given fluid.
     *
     * @param input - the FluidInput to electrolyse fluid from
     * @return SeparatorRecipe
     */
    public static SeparatorRecipe getElectrolyticSeparatorRecipe(FluidInput input) {
        if (input.isValid()) {
            Map<FluidInput, SeparatorRecipe> recipes = Recipe.ELECTROLYTIC_SEPARATOR.get();

            SeparatorRecipe recipe = recipes.get(input);
            return recipe == null ? null : recipe.copy();
        }

        return null;
    }

    public static ThermalEvaporationRecipe getThermalEvaporationRecipe(FluidInput input) {
        if (input.isValid()) {
            Map<FluidInput, ThermalEvaporationRecipe> recipes = Recipe.THERMAL_EVAPORATION_PLANT.get();

            ThermalEvaporationRecipe recipe = recipes.get(input);
            return recipe == null ? null : recipe.copy();
        }

        return null;
    }

    public static SolarNeutronRecipe getSolarNeutronRecipe(GasInput input) {
        if (input.isValid()) {
            Map<GasInput, SolarNeutronRecipe> recipes = Recipe.SOLAR_NEUTRON_ACTIVATOR.get();

            SolarNeutronRecipe recipe = recipes.get(input);
            return recipe == null ? null : recipe.copy();
        }

        return null;
    }

    public static PressurizedRecipe getPRCRecipe(PressurizedInput input) {
        if (input.isValid()) {
            Map<PressurizedInput, PressurizedRecipe> recipes = Recipe.PRESSURIZED_REACTION_CHAMBER.get();

            PressurizedRecipe recipe = recipes.get(input);

            if (recipe == null) {
                recipe = recipes.get(input.wildCopy());
            }

            return recipe == null ? null : recipe.copy();
        }

        return null;
    }

    public static AmbientGasRecipe getDimensionGas(IntegerInput input) {
        Map<IntegerInput, AmbientGasRecipe> recipes = Recipe.AMBIENT_ACCUMULATOR.get();
        AmbientGasRecipe recipe = recipes.get(input);

        return recipe == null ? null : recipe.copy();
    }

    /**
     * Gets the whether the input ItemStack is in a recipe
     *
     * @param itemstack - input ItemStack
     * @param recipes - Map of recipes
     * @return whether the item can be used in a recipe
     */
    public static <RECIPE extends MachineRecipe<ItemStackInput, ?, RECIPE>> boolean isInRecipe(ItemStack itemstack,
          Map<ItemStackInput, RECIPE> recipes) {
        if (!itemstack.isEmpty()) {
            for (RECIPE recipe : recipes.values()) {
                ItemStackInput required = recipe.getInput();
                NonNullList<ItemStack> list = NonNullList.create();
                list.add(itemstack);

                if (required.useItemStackFromInventory(list, 0, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isInPressurizedRecipe(ItemStack stack) {
        if (!stack.isEmpty()) {
            for (PressurizedInput key : Recipe.PRESSURIZED_REACTION_CHAMBER.get().keySet()) {
                if (key.containsType(stack)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static <RECIPE extends MachineRecipe<ItemStackInput, ?, RECIPE>> RECIPE getRecipeTryWildcard(ItemStack stack,
          Map<ItemStackInput, RECIPE> recipes) {
        return getRecipeTryWildcard(new ItemStackInput(stack), recipes);
    }

    public static <RECIPE extends MachineRecipe<ItemStackInput, ?, RECIPE>> RECIPE getRecipeTryWildcard(
          ItemStackInput input, Map<ItemStackInput, RECIPE> recipes) {
        RECIPE recipe = recipes.get(input);

        if (recipe == null) {
            recipe = recipes.get(input.wildCopy());
        }

        return recipe;
    }

    public static class Recipe<INPUT extends MachineInput<INPUT>, OUTPUT extends MachineOutput<OUTPUT>, RECIPE extends MachineRecipe<INPUT, OUTPUT, RECIPE>> {

        private static final List<Recipe> values = new ArrayList<>();

        public static final Recipe<ItemStackInput, ItemStackOutput, SmeltingRecipe> ENERGIZED_SMELTER = new Recipe<>(
              MachineType.ENERGIZED_SMELTER.blockName, "ENERGIZED_SMELTER", ItemStackInput.class, ItemStackOutput.class,
              SmeltingRecipe.class);

        public static final Recipe<ItemStackInput, ItemStackOutput, EnrichmentRecipe> ENRICHMENT_CHAMBER = new Recipe<>(
              MachineType.ENRICHMENT_CHAMBER.blockName, "ENRICHMENT_CHAMBER", ItemStackInput.class,
              ItemStackOutput.class, EnrichmentRecipe.class);

        public static final Recipe<AdvancedMachineInput, ItemStackOutput, OsmiumCompressorRecipe> OSMIUM_COMPRESSOR = new Recipe<>(
              MachineType.OSMIUM_COMPRESSOR.blockName, "OSMIUM_COMPRESSOR", AdvancedMachineInput.class,
              ItemStackOutput.class, OsmiumCompressorRecipe.class);

        public static final Recipe<DoubleMachineInput, ItemStackOutput, CombinerRecipe> COMBINER = new Recipe<>(
              MachineType.COMBINER.blockName, "COMBINER", DoubleMachineInput.class, ItemStackOutput.class,
              CombinerRecipe.class);

        public static final Recipe<ItemStackInput, ItemStackOutput, CrusherRecipe> CRUSHER = new Recipe<>(
              MachineType.CRUSHER.blockName, "CRUSHER", ItemStackInput.class, ItemStackOutput.class,
              CrusherRecipe.class);

        public static final Recipe<AdvancedMachineInput, ItemStackOutput, PurificationRecipe> PURIFICATION_CHAMBER = new Recipe<>(
              MachineType.PURIFICATION_CHAMBER.blockName, "PURIFICATION_CHAMBER", AdvancedMachineInput.class,
              ItemStackOutput.class, PurificationRecipe.class);

        public static final Recipe<InfusionInput, ItemStackOutput, MetallurgicInfuserRecipe> METALLURGIC_INFUSER = new Recipe<>(
              MachineType.METALLURGIC_INFUSER.blockName, "METALLURGIC_INFUSER", InfusionInput.class,
              ItemStackOutput.class, MetallurgicInfuserRecipe.class);

        public static final Recipe<ChemicalPairInput, GasOutput, ChemicalInfuserRecipe> CHEMICAL_INFUSER = new Recipe<>(
              MachineType.CHEMICAL_INFUSER.blockName, "CHEMICAL_INFUSER", ChemicalPairInput.class, GasOutput.class,
              ChemicalInfuserRecipe.class);

        public static final Recipe<ItemStackInput, GasOutput, OxidationRecipe> CHEMICAL_OXIDIZER = new Recipe<>(
              MachineType.CHEMICAL_OXIDIZER.blockName, "CHEMICAL_OXIDIZER", ItemStackInput.class, GasOutput.class,
              OxidationRecipe.class);

        public static final Recipe<AdvancedMachineInput, ItemStackOutput, InjectionRecipe> CHEMICAL_INJECTION_CHAMBER = new Recipe<>(
              MachineType.CHEMICAL_INJECTION_CHAMBER.blockName, "CHEMICAL_INJECTION_CHAMBER",
              AdvancedMachineInput.class, ItemStackOutput.class, InjectionRecipe.class);

        public static final Recipe<FluidInput, ChemicalPairOutput, SeparatorRecipe> ELECTROLYTIC_SEPARATOR = new Recipe<>(
              MachineType.ELECTROLYTIC_SEPARATOR.blockName, "ELECTROLYTIC_SEPARATOR", FluidInput.class,
              ChemicalPairOutput.class, SeparatorRecipe.class);

        public static final Recipe<ItemStackInput, ChanceOutput, SawmillRecipe> PRECISION_SAWMILL = new Recipe<>(
              MachineType.PRECISION_SAWMILL.blockName, "PRECISION_SAWMILL", ItemStackInput.class, ChanceOutput.class,
              SawmillRecipe.class);

        public static final Recipe<ItemStackInput, GasOutput, DissolutionRecipe> CHEMICAL_DISSOLUTION_CHAMBER = new Recipe<>(
              MachineType.CHEMICAL_DISSOLUTION_CHAMBER.blockName, "CHEMICAL_DISSOLUTION_CHAMBER", ItemStackInput.class,
              GasOutput.class, DissolutionRecipe.class);

        public static final Recipe<GasInput, GasOutput, WasherRecipe> CHEMICAL_WASHER = new Recipe<>(
              MachineType.CHEMICAL_WASHER.blockName, "CHEMICAL_WASHER", GasInput.class, GasOutput.class,
              WasherRecipe.class);

        public static final Recipe<GasInput, ItemStackOutput, CrystallizerRecipe> CHEMICAL_CRYSTALLIZER = new Recipe<>(
              MachineType.CHEMICAL_CRYSTALLIZER.blockName, "CHEMICAL_CRYSTALLIZER", GasInput.class,
              ItemStackOutput.class, CrystallizerRecipe.class);

        public static final Recipe<PressurizedInput, PressurizedOutput, PressurizedRecipe> PRESSURIZED_REACTION_CHAMBER = new Recipe<>(
              MachineType.PRESSURIZED_REACTION_CHAMBER.blockName, "PRESSURIZED_REACTION_CHAMBER",
              PressurizedInput.class, PressurizedOutput.class, PressurizedRecipe.class);

        public static final Recipe<IntegerInput, GasOutput, AmbientGasRecipe> AMBIENT_ACCUMULATOR = new Recipe<>(
              MachineType.AMBIENT_ACCUMULATOR.blockName, "AMBIENT_ACCUMULATOR", IntegerInput.class, GasOutput.class,
              AmbientGasRecipe.class);

        public static final Recipe<FluidInput, FluidOutput, ThermalEvaporationRecipe> THERMAL_EVAPORATION_PLANT = new Recipe<>(
              "ThermalEvaporationPlant", "THERMAL_EVAPORATION_PLANT", FluidInput.class, FluidOutput.class,
              ThermalEvaporationRecipe.class);

        public static final Recipe<GasInput, GasOutput, SolarNeutronRecipe> SOLAR_NEUTRON_ACTIVATOR = new Recipe<>(
              MachineType.SOLAR_NEUTRON_ACTIVATOR.blockName, "SOLAR_NEUTRON_ACTIVATOR", GasInput.class, GasOutput.class,
              SolarNeutronRecipe.class);

        //TODO: Replace this with Recipe being Iterable
        public static List<Recipe> values() {
            return values;
        }

        private final Map<INPUT, RECIPE> recipes = new HashMap<>();
        private final String recipeName;
        private final String oldRecipeName;
        private final String jeiCategory;

        private Class<? extends MachineInput> inputClass;
        private Class<? extends MachineOutput> outputClass;
        private Class<? extends MachineRecipe> recipeClass;


        private Recipe(String name, String oldName, Class<INPUT> input, Class<OUTPUT> output, Class<RECIPE> recipe) {
            recipeName = name;
            oldRecipeName = oldName;
            jeiCategory = "mekanism." + recipeName.toLowerCase(Locale.ROOT);

            inputClass = input;
            outputClass = output;
            recipeClass = recipe;

            values.add(this);
        }

        public void put(RECIPE recipe) {
            recipes.put(recipe.getInput(), recipe);
        }

        public void remove(RECIPE recipe) {
            recipes.remove(recipe.getInput());
        }

        public String getRecipeName() {
            return recipeName;
        }

        public String getOldRecipeName() {
            return oldRecipeName;
        }

        public String getJEICategory() {
            return jeiCategory;
        }

        public INPUT createInput(NBTTagCompound nbtTags) {
            try {
                MachineInput input = inputClass.newInstance();
                input.load(nbtTags);

                return (INPUT) input;
            } catch (Exception e) {
                return null;
            }
        }

        public RECIPE createRecipe(INPUT input, NBTTagCompound nbtTags) {
            try {
                MachineOutput output = outputClass.newInstance();
                output.load(nbtTags);

                try {
                    Constructor<? extends MachineRecipe> construct = recipeClass
                          .getDeclaredConstructor(inputClass, outputClass);
                    return (RECIPE) construct.newInstance(input, output);
                } catch (Exception e) {
                    Constructor<? extends MachineRecipe> construct = recipeClass
                          .getDeclaredConstructor(inputClass, outputClass, NBTTagCompound.class);
                    return (RECIPE) construct.newInstance(input, output, nbtTags);
                }
            } catch (Exception e) {
                return null;
            }
        }

        public boolean containsRecipe(ItemStack input) {
            //TODO: Support other input types
            for (Entry<INPUT, RECIPE> entry : recipes.entrySet()) {
                if (entry.getKey() instanceof ItemStackInput) {
                    ItemStack stack = ((ItemStackInput) entry.getKey()).ingredient;

                    if (StackUtils.equalsWildcard(stack, input)) {
                        return true;
                    }
                } else if (entry.getKey() instanceof FluidInput) {
                    if (((FluidInput) entry.getKey()).ingredient.isFluidEqual(input)) {
                        return true;
                    }
                } else if (entry.getKey() instanceof AdvancedMachineInput) {
                    ItemStack stack = ((AdvancedMachineInput) entry.getKey()).itemStack;

                    if (StackUtils.equalsWildcard(stack, input)) {
                        return true;
                    }
                }
            }

            return false;
        }

        public boolean containsRecipe(Fluid input) {
            //TODO: Support other input types
            for (Entry<INPUT, RECIPE> entry : recipes.entrySet()) {
                if (entry.getKey() instanceof FluidInput) {
                    if (((FluidInput) entry.getKey()).ingredient.getFluid() == input) {
                        return true;
                    }
                }
            }

            return false;
        }

        public boolean containsRecipe(Gas input) {
            //TODO: Support other input types
            for (Entry<INPUT, RECIPE> entry : recipes.entrySet()) {
                Gas toCheck = null;
                if (entry.getKey() instanceof GasInput) {
                    toCheck = ((GasInput) entry.getKey()).ingredient.getGas();
                } else if (entry.getKey() instanceof AdvancedMachineInput) {
                    toCheck = ((AdvancedMachineInput) entry.getKey()).gasType;
                }
                if (toCheck == input) {
                    return true;
                }
            }

            return false;
        }

        public Map<INPUT, RECIPE> get() {
            return recipes;
        }

        /*@Nullable
        public RECIPE getRecipe(INPUT input) {
            if (input.isValid()) {
                RECIPE recipe;
                recipe = recipes.get(input);
                if (recipe == null && input instanceof ItemStackInput) {
                    recipe = recipes.get(((ItemStackInput) input).wildCopy());
                }
                return recipe == null ? null : recipe.copy();
            }
            return null;
        }*/
    }
}
