package mekanism.common.recipe;

import javax.annotation.Nonnull;
import mekanism.api.MekanismRecipeHelper;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfuseType;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismGases;
import mekanism.common.tags.MekanismTags;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

public class APIHandler implements MekanismRecipeHelper {

    private static void checkPhase() {
        //TODO: Make this not true anymore, and make recipes be reloadable
        /*Preconditions.checkState(Loader.instance().getLoaderState().ordinal() < LoaderState.POSTINITIALIZATION.ordinal(),
              "Recipes should be registered before PostInit. Try net.minecraftforge.event.RegistryEvent.Register<IRecipe>");*/
    }

    @Override
    public void addEnrichmentChamberRecipe(ItemStack input, ItemStack output) {
        checkPhase();
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(input), output);
    }

    @Override
    public void addOsmiumCompressorRecipe(ItemStack input, ItemStack output) {
        checkPhase();
        RecipeHandler.addOsmiumCompressorRecipe(ItemStackIngredient.from(input), GasStackIngredient.from(MekanismGases.LIQUID_OSMIUM, 1), output);
    }

    @Override
    public void addCombinerRecipe(ItemStack input, ItemStack extra, ItemStack output) {
        checkPhase();
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(input), ItemStackIngredient.from(extra), output);
    }

    @Override
    public void addCrusherRecipe(ItemStack input, ItemStack output) {
        checkPhase();
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(input), output);
    }

    @Override
    public void addPurificationChamberRecipe(ItemStack input, ItemStack output) {
        checkPhase();
        RecipeHandler.addPurificationChamberRecipe(ItemStackIngredient.from(input), output);
    }

    @Override
    public void addMetallurgicInfuserRecipe(InfuseType infuse, int amount, ItemStack input, ItemStack output) {
        checkPhase();
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(infuse, amount), ItemStackIngredient.from(Ingredient.fromStacks(input), input.getCount()), output);
    }

    @Override
    public void addChemicalInfuserRecipe(GasStack leftInput, GasStack rightInput, GasStack output) {
        checkPhase();
        RecipeHandler.addChemicalInfuserRecipe(GasStackIngredient.from(leftInput), GasStackIngredient.from(rightInput), output);
    }

    @Override
    public void addChemicalOxidizerRecipe(ItemStack input, GasStack output) {
        checkPhase();
        RecipeHandler.addChemicalOxidizerRecipe(ItemStackIngredient.from(input), output);
    }

    @Override
    public void addChemicalInjectionChamberRecipe(ItemStack input, Gas gas, ItemStack output) {
        checkPhase();
        RecipeHandler.addChemicalInjectionChamberRecipe(ItemStackIngredient.from(input), GasStackIngredient.from(gas, 1), output);
    }

    @Override
    public void addElectrolyticSeparatorRecipe(@Nonnull FluidStack fluid, double energy, GasStack leftOutput, GasStack rightOutput) {
        checkPhase();
        RecipeHandler.addElectrolyticSeparatorRecipe(FluidStackIngredient.from(fluid), energy, leftOutput, rightOutput);
    }

    @Override
    public void addPrecisionSawmillRecipe(ItemStack input, ItemStack primaryOutput, ItemStack secondaryOutput, double chance) {
        checkPhase();
        RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(input), primaryOutput, secondaryOutput, chance);
    }

    @Override
    public void addPrecisionSawmillRecipe(ItemStack input, ItemStack primaryOutput) {
        checkPhase();
        RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(input), primaryOutput);
    }

    @Override
    public void addChemicalDissolutionChamberRecipe(ItemStack input, GasStack output) {
        checkPhase();
        RecipeHandler.addChemicalDissolutionChamberRecipe(ItemStackIngredient.from(input), GasStackIngredient.from(MekanismTags.SULFURIC_ACID, 1), output);
    }

    @Override
    public void addChemicalWasherRecipe(GasStack input, GasStack output) {
        checkPhase();
        RecipeHandler.addChemicalWasherRecipe(FluidStackIngredient.from(Fluids.WATER, 5), GasStackIngredient.from(input), output);
    }

    @Override
    public void addChemicalCrystallizerRecipe(GasStack input, ItemStack output) {
        checkPhase();
        RecipeHandler.addChemicalCrystallizerRecipe(GasStackIngredient.from(input), output);
    }

    @Override
    public void addPRCRecipe(ItemStack inputSolid, @Nonnull FluidStack inputFluid, GasStack inputGas, ItemStack outputSolid, GasStack outputGas, double extraEnergy, int ticks) {
        checkPhase();
        RecipeHandler.addPRCRecipe(ItemStackIngredient.from(Ingredient.fromStacks(inputSolid), inputSolid.getCount()), FluidStackIngredient.from(inputFluid),
              GasStackIngredient.from(inputGas), outputSolid, outputGas, extraEnergy, ticks);
    }

    @Override
    public void addThermalEvaporationRecipe(@Nonnull FluidStack inputFluid, @Nonnull FluidStack outputFluid) {
        checkPhase();
        RecipeHandler.addThermalEvaporationRecipe(FluidStackIngredient.from(inputFluid), outputFluid);
    }

    @Override
    public void addSolarNeutronRecipe(GasStack inputGas, GasStack outputGas) {
        checkPhase();
        RecipeHandler.addSolarNeutronRecipe(GasStackIngredient.from(inputGas), outputGas);
    }
}