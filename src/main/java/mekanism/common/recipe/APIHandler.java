package mekanism.common.recipe;

import com.google.common.base.Preconditions;
import mekanism.api.MekanismRecipeHelper;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfuseType;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

public class APIHandler implements MekanismRecipeHelper {

    private static void checkPhase() {
        Preconditions
              .checkState(Loader.instance().getLoaderState().ordinal() < LoaderState.POSTINITIALIZATION.ordinal(),
                    "Recipes should be registered before PostInit. Try net.minecraftforge.event.RegistryEvent.Register<IRecipe>");
    }

    @Override
    public void addEnrichmentChamberRecipe(ItemStack input, ItemStack output) {
        checkPhase();
        RecipeHandler.addEnrichmentChamberRecipe(input, output);
    }

    @Override
    public void addOsmiumCompressorRecipe(ItemStack input, ItemStack output) {
        checkPhase();
        RecipeHandler.addOsmiumCompressorRecipe(input, output);
    }

    @Override
    @Deprecated
    public void addCombinerRecipe(ItemStack input, ItemStack output) {
        addCombinerRecipe(input, new ItemStack(Blocks.COBBLESTONE), output);
    }

    @Override
    public void addCombinerRecipe(ItemStack input, ItemStack extra, ItemStack output) {
        checkPhase();
        RecipeHandler.addCombinerRecipe(input, extra, output);
    }

    @Override
    public void addCrusherRecipe(ItemStack input, ItemStack output) {
        checkPhase();
        RecipeHandler.addCrusherRecipe(input, output);
    }

    @Override
    public void addPurificationChamberRecipe(ItemStack input, ItemStack output) {
        checkPhase();
        RecipeHandler.addPurificationChamberRecipe(input, output);
    }

    @Override
    public void addMetallurgicInfuserRecipe(InfuseType infuse, int amount, ItemStack input, ItemStack output) {
        checkPhase();
        RecipeHandler.addMetallurgicInfuserRecipe(infuse, amount, input, output);
    }

    @Override
    public void addChemicalInfuserRecipe(GasStack leftInput, GasStack rightInput, GasStack output) {
        checkPhase();
        RecipeHandler.addChemicalInfuserRecipe(leftInput, rightInput, output);
    }

    @Override
    public void addChemicalOxidizerRecipe(ItemStack input, GasStack output) {
        checkPhase();
        RecipeHandler.addChemicalOxidizerRecipe(input, output);
    }

    @Override
    public void addChemicalInjectionChamberRecipe(ItemStack input, Gas gas, ItemStack output) {
        checkPhase();
        RecipeHandler.addChemicalInjectionChamberRecipe(input, gas, output);
    }

    @Override
    public void addElectrolyticSeparatorRecipe(FluidStack fluid, double energy, GasStack leftOutput,
          GasStack rightOutput) {
        checkPhase();
        RecipeHandler.addElectrolyticSeparatorRecipe(fluid, energy, leftOutput, rightOutput);
    }

    @Override
    public void addPrecisionSawmillRecipe(ItemStack input, ItemStack primaryOutput, ItemStack secondaryOutput,
          double chance) {
        checkPhase();
        RecipeHandler.addPrecisionSawmillRecipe(input, primaryOutput, secondaryOutput, chance);
    }

    @Override
    public void addPrecisionSawmillRecipe(ItemStack input, ItemStack primaryOutput) {
        checkPhase();
        RecipeHandler.addPrecisionSawmillRecipe(input, primaryOutput);
    }

    @Override
    public void addChemicalDissolutionChamberRecipe(ItemStack input, GasStack output) {
        checkPhase();
        RecipeHandler.addChemicalDissolutionChamberRecipe(input, output);
    }

    @Override
    public void addChemicalWasherRecipe(GasStack input, GasStack output) {
        checkPhase();
        RecipeHandler.addChemicalWasherRecipe(input, output);
    }

    @Override
    public void addChemicalCrystallizerRecipe(GasStack input, ItemStack output) {
        checkPhase();
        RecipeHandler.addChemicalCrystallizerRecipe(input, output);
    }

    @Override
    public void addPRCRecipe(ItemStack inputSolid, FluidStack inputFluid, GasStack inputGas, ItemStack outputSolid,
          GasStack outputGas, double extraEnergy, int ticks) {
        checkPhase();
        RecipeHandler.addPRCRecipe(inputSolid, inputFluid, inputGas, outputSolid, outputGas, extraEnergy, ticks);
    }

    @Override
    public void addThermalEvaporationRecipe(FluidStack inputFluid, FluidStack outputFluid) {
        checkPhase();
        RecipeHandler.addThermalEvaporationRecipe(inputFluid, outputFluid);
    }

    @Override
    public void addSolarNeutronRecipe(GasStack inputGas, GasStack outputGas) {
        checkPhase();
        RecipeHandler.addSolarNeutronRecipe(inputGas, outputGas);
    }
}
