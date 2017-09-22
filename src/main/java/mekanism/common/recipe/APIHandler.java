package mekanism.common.recipe;

import mekanism.api.MekanismRecipeHelper;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfuseType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class APIHandler implements MekanismRecipeHelper {
    @Override
    public void addEnrichmentChamberRecipe(ItemStack input, ItemStack output) {
        RecipeHandler.addEnrichmentChamberRecipe(input, output);
    }

    @Override
    public void addOsmiumCompressorRecipe(ItemStack input, ItemStack output) {
        RecipeHandler.addOsmiumCompressorRecipe(input, output);
    }

    @Override
    public void addCombinerRecipe(ItemStack input, ItemStack output) {
        RecipeHandler.addCombinerRecipe(input, output);
    }

    @Override
    public void addCrusherRecipe(ItemStack input, ItemStack output) {
        RecipeHandler.addCrusherRecipe(input, output);
    }

    @Override
    public void addPurificationChamberRecipe(ItemStack input, ItemStack output) {
        RecipeHandler.addPurificationChamberRecipe(input, output);
    }

    @Override
    public void addMetallurgicInfuserRecipe(InfuseType infuse, int amount, ItemStack input, ItemStack output) {
        RecipeHandler.addMetallurgicInfuserRecipe(infuse, amount, input, output);
    }

    @Override
    public void addChemicalInfuserRecipe(GasStack leftInput, GasStack rightInput, GasStack output) {
        RecipeHandler.addChemicalInfuserRecipe(leftInput, rightInput, output);
    }

    @Override
    public void addChemicalOxidizerRecipe(ItemStack input, GasStack output) {
        RecipeHandler.addChemicalOxidizerRecipe(input, output);
    }

    @Override
    public void addChemicalInjectionChamberRecipe(ItemStack input, Gas gas, ItemStack output) {
        RecipeHandler.addChemicalInjectionChamberRecipe(input, gas, output);
    }

    @Override
    public void addElectrolyticSeparatorRecipe(FluidStack fluid, double energy, GasStack leftOutput, GasStack rightOutput) {
        RecipeHandler.addElectrolyticSeparatorRecipe(fluid, energy, leftOutput, rightOutput);
    }

    @Override
    public void addPrecisionSawmillRecipe(ItemStack input, ItemStack primaryOutput, ItemStack secondaryOutput, double chance) {
        RecipeHandler.addPrecisionSawmillRecipe(input, primaryOutput, secondaryOutput, chance);
    }

    @Override
    public void addPrecisionSawmillRecipe(ItemStack input, ItemStack primaryOutput) {
        RecipeHandler.addPrecisionSawmillRecipe(input, primaryOutput);
    }

    @Override
    public void addChemicalDissolutionChamberRecipe(ItemStack input, GasStack output) {
        RecipeHandler.addChemicalDissolutionChamberRecipe(input, output);
    }

    @Override
    public void addChemicalWasherRecipe(GasStack input, GasStack output) {
        RecipeHandler.addChemicalWasherRecipe(input, output);
    }

    @Override
    public void addChemicalCrystallizerRecipe(GasStack input, ItemStack output) {
        RecipeHandler.addChemicalCrystallizerRecipe(input, output);
    }

    @Override
    public void addPRCRecipe(ItemStack inputSolid, FluidStack inputFluid, GasStack inputGas, ItemStack outputSolid, GasStack outputGas, double extraEnergy, int ticks) {
        RecipeHandler.addPRCRecipe(inputSolid, inputFluid, inputGas, outputSolid, outputGas, extraEnergy, ticks);
    }

    @Override
    public void addThermalEvaporationRecipe(FluidStack inputFluid, FluidStack outputFluid) {
        RecipeHandler.addThermalEvaporationRecipe(inputFluid, outputFluid);
    }

    @Override
    public void addSolarNeutronRecipe(GasStack inputGas, GasStack outputGas) {
        RecipeHandler.addSolarNeutronRecipe(inputGas, outputGas);
    }
}
