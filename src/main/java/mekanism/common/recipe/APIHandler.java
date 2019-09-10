package mekanism.common.recipe;

import com.google.common.base.Preconditions;
import mekanism.api.MekanismRecipeHelper;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfuseType;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismFluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

public class APIHandler implements MekanismRecipeHelper {

    private static void checkPhase() {
        Preconditions.checkState(Loader.instance().getLoaderState().ordinal() < LoaderState.POSTINITIALIZATION.ordinal(),
              "Recipes should be registered before PostInit. Try net.minecraftforge.event.RegistryEvent.Register<IRecipe>");
    }

    @Override
    public void addEnrichmentChamberRecipe(ItemStack input, ItemStack output) {
        checkPhase();
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(input), output);
    }

    @Override
    public void addOsmiumCompressorRecipe(ItemStack input, ItemStack output) {
        checkPhase();
        RecipeHandler.addOsmiumCompressorRecipe(ItemStackIngredient.from(input), GasIngredient.fromInstance(MekanismFluids.LiquidOsmium), output);
    }

    @Override
    @Deprecated
    public void addCombinerRecipe(ItemStack input, ItemStack output) {
        checkPhase();
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(input), ItemStackIngredient.from("cobblestone"), output);
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
        RecipeHandler.addChemicalInfuserRecipe(GasStackIngredient.fromInstance(leftInput), GasStackIngredient.fromInstance(rightInput), output);
    }

    @Override
    public void addChemicalOxidizerRecipe(ItemStack input, GasStack output) {
        checkPhase();
        RecipeHandler.addChemicalOxidizerRecipe(ItemStackIngredient.from(input), output);
    }

    @Override
    public void addChemicalInjectionChamberRecipe(ItemStack input, Gas gas, ItemStack output) {
        checkPhase();
        RecipeHandler.addChemicalInjectionChamberRecipe(ItemStackIngredient.from(input), GasIngredient.fromInstance(gas), output);
    }

    @Override
    public void addElectrolyticSeparatorRecipe(FluidStack fluid, double energy, GasStack leftOutput, GasStack rightOutput) {
        checkPhase();
        RecipeHandler.addElectrolyticSeparatorRecipe(FluidStackIngredient.fromInstance(fluid), energy, leftOutput, rightOutput);
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
        RecipeHandler.addChemicalDissolutionChamberRecipe(ItemStackIngredient.from(input), GasIngredient.fromInstance(MekanismFluids.SulfuricAcid), output);
    }

    @Override
    public void addChemicalWasherRecipe(GasStack input, GasStack output) {
        checkPhase();
        RecipeHandler.addChemicalWasherRecipe(FluidStackIngredient.fromInstance(FluidRegistry.WATER, 5), GasStackIngredient.fromInstance(input), output);
    }

    @Override
    public void addChemicalCrystallizerRecipe(GasStack input, ItemStack output) {
        checkPhase();
        RecipeHandler.addChemicalCrystallizerRecipe(GasStackIngredient.fromInstance(input), output);
    }

    @Override
    public void addPRCRecipe(ItemStack inputSolid, FluidStack inputFluid, GasStack inputGas, ItemStack outputSolid, GasStack outputGas, double extraEnergy, int ticks) {
        checkPhase();
        RecipeHandler.addPRCRecipe(ItemStackIngredient.from(Ingredient.fromStacks(inputSolid), inputSolid.getCount()), FluidStackIngredient.fromInstance(inputFluid),
              GasStackIngredient.fromInstance(inputGas), outputSolid, outputGas, extraEnergy, ticks);
    }

    @Override
    public void addThermalEvaporationRecipe(FluidStack inputFluid, FluidStack outputFluid) {
        checkPhase();
        RecipeHandler.addThermalEvaporationRecipe(FluidStackIngredient.fromInstance(inputFluid), outputFluid);
    }

    @Override
    public void addSolarNeutronRecipe(GasStack inputGas, GasStack outputGas) {
        checkPhase();
        RecipeHandler.addSolarNeutronRecipe(GasStackIngredient.fromInstance(inputGas), outputGas);
    }
}