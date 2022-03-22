package mekanism.chemistry.common;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.datagen.recipe.builder.RotaryRecipeBuilder;
import mekanism.api.providers.IFluidProvider;
import mekanism.api.providers.IGasProvider;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.chemistry.common.ChemistryTags.Fluids;
import mekanism.chemistry.common.ChemistryTags.Gases;
import mekanism.chemistry.common.registries.ChemistryFluids;
import mekanism.chemistry.common.registries.ChemistryGases;
import mekanism.common.recipe.BaseRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.data.ExistingFileHelper;

@ParametersAreNonnullByDefault
public class ChemistryRecipeProvider extends BaseRecipeProvider {

    public ChemistryRecipeProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
        super(gen, existingFileHelper, MekanismChemistry.MODID);
    }

    @Override
    protected void addRecipes(Consumer<FinishedRecipe> consumer) {
        addRotaryCondensentratorRecipes(consumer);
    }

    private void addRotaryCondensentratorRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "rotary/";
        addRotaryCondensentratorRecipe(consumer, basePath, ChemistryGases.AMMONIA, ChemistryFluids.AMMONIA, ChemistryTags.Fluids.AMMONIA, ChemistryTags.Gases.AMMONIA);
        addRotaryCondensentratorRecipe(consumer, basePath, ChemistryGases.NITROGEN, ChemistryFluids.NITROGEN, ChemistryTags.Fluids.NITROGEN, ChemistryTags.Gases.NITROGEN);
        addRotaryCondensentratorRecipe(consumer, basePath, ChemistryGases.AIR, ChemistryFluids.AIR, ChemistryTags.Fluids.AIR, ChemistryTags.Gases.AIR);
    }

    private void addRotaryCondensentratorRecipe(Consumer<FinishedRecipe> consumer, String basePath, IGasProvider gas, IFluidProvider fluidOutput,
          Tag<Fluid> fluidInput, Tag<Gas> gasInput) {
        RotaryRecipeBuilder.rotary(
              IngredientCreatorAccess.fluid().from(fluidInput, 1),
              IngredientCreatorAccess.gas().from(gasInput, 1),
              gas.getStack(1),
              fluidOutput.getFluidStack(1)
        ).build(consumer, MekanismChemistry.rl(basePath + gas.getName()));
    }
}
