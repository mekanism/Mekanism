package mekanism.api;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfuseType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface MekanismRecipeHelper {

    /**
     * Add an Enrichment Chamber recipe.
     *
     * @param input  - input ItemStack
     * @param output - output ItemStack
     */
    void addEnrichmentChamberRecipe(ItemStack input, ItemStack output);

    /**
     * Add an Osmium Compressor recipe.
     *
     * @param input  - input ItemStack
     * @param output - output ItemStack
     */
    void addOsmiumCompressorRecipe(ItemStack input, ItemStack output);

    /**
     * Add a Combiner recipe.
     *
     * @param input  - input ItemStack
     * @param output - output ItemStack
     */
    @Deprecated
    void addCombinerRecipe(ItemStack input, ItemStack output);

    /**
     * Add a Combiner recipe.
     *
     * @param input  - input ItemStack
     * @param extra  - extra ItemStack
     * @param output - output ItemStack
     */
    void addCombinerRecipe(ItemStack input, ItemStack extra, ItemStack output);

    /**
     * Add a Crusher recipe.
     *
     * @param input  - input ItemStack
     * @param output - output ItemStack
     */
    void addCrusherRecipe(ItemStack input, ItemStack output);

    /**
     * Add a Purification Chamber recipe.
     *
     * @param input  - input ItemStack
     * @param output - output ItemStack
     */
    void addPurificationChamberRecipe(ItemStack input, ItemStack output);

    /**
     * Add a Metallurgic Infuser recipe.
     *
     * @param infuse - which Infuse to use
     * @param amount - how much of the Infuse to use
     * @param input  - input ItemStack
     * @param output - output ItemStack
     */
    void addMetallurgicInfuserRecipe(InfuseType infuse, int amount, ItemStack input, ItemStack output);

    /**
     * Add a Chemical Infuser recipe.
     *
     * @param leftInput  - left GasStack to input
     * @param rightInput - right GasStack to input
     * @param output     - output GasStack
     */
    void addChemicalInfuserRecipe(GasStack leftInput, GasStack rightInput, GasStack output);

    /**
     * Add a Chemical Oxidizer recipe.
     *
     * @param input  - input ItemStack
     * @param output - output GasStack
     */
    void addChemicalOxidizerRecipe(ItemStack input, GasStack output);

    /**
     * Add a Chemical Injection Chamber recipe.
     *
     * @param input  - input ItemStack
     * @param output - output ItemStack
     */
    void addChemicalInjectionChamberRecipe(ItemStack input, Gas gas, ItemStack output);

    /**
     * Add an Electrolytic Separator recipe.
     *
     * @param fluid       - FluidStack to electrolyze
     * @param leftOutput  - left gas to produce when the fluid is electrolyzed
     * @param rightOutput - right gas to produce when the fluid is electrolyzed
     */
    void addElectrolyticSeparatorRecipe(FluidStack fluid, double energy, GasStack leftOutput, GasStack rightOutput);

    /**
     * Add a Precision Sawmill recipe.
     *
     * @param input           - input ItemStack
     * @param primaryOutput   - guaranteed output
     * @param secondaryOutput - possible extra output
     * @param chance          - probability of obtaining extra output
     */
    void addPrecisionSawmillRecipe(ItemStack input, ItemStack primaryOutput, ItemStack secondaryOutput, double chance);

    /**
     * Add a Precision Sawmill recipe with no chance output
     *
     * @param input         - input ItemStack
     * @param primaryOutput - guaranteed output
     */
    void addPrecisionSawmillRecipe(ItemStack input, ItemStack primaryOutput);

    /**
     * Add a Chemical Dissolution Chamber recipe.
     *
     * @param input  - input ItemStack
     * @param output - output GasStack
     */
    void addChemicalDissolutionChamberRecipe(ItemStack input, GasStack output);

    /**
     * Add a Chemical Washer recipe.
     *
     * @param input  - input GasStack
     * @param output - output GasStack
     */
    void addChemicalWasherRecipe(GasStack input, GasStack output);

    /**
     * Add a Chemical Crystallizer recipe.
     *
     * @param input  - input GasStack
     * @param output - output ItemStack
     */
    void addChemicalCrystallizerRecipe(GasStack input, ItemStack output);

    /**
     * Add a Pressurized Reaction Chamber recipe.
     *
     * @param inputSolid  - input ItemStack
     * @param inputFluid  - input FluidStack
     * @param inputGas    - input GasStack
     * @param outputSolid - output ItemStack
     * @param outputGas   - output GasStack
     * @param extraEnergy - extra energy needed by the recipe
     * @param ticks       - amount of ticks it takes for this recipe to complete
     */
    void addPRCRecipe(ItemStack inputSolid, FluidStack inputFluid, GasStack inputGas, ItemStack outputSolid,
          GasStack outputGas, double extraEnergy, int ticks);

    void addThermalEvaporationRecipe(FluidStack inputFluid, FluidStack outputFluid);

    void addSolarNeutronRecipe(GasStack inputGas, GasStack outputGas);
}