package mekanism.chemistry.common;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.datagen.recipe.builder.ChemicalChemicalToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ChemicalCrystallizerRecipeBuilder;
import mekanism.api.datagen.recipe.builder.DistillingRecipeBuilder;
import mekanism.api.datagen.recipe.builder.PressurizedReactionRecipeBuilder;
import mekanism.api.datagen.recipe.builder.RotaryRecipeBuilder;
import mekanism.api.providers.IFluidProvider;
import mekanism.api.providers.IGasProvider;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.chemistry.common.registries.ChemistryFluids;
import mekanism.chemistry.common.registries.ChemistryGases;
import mekanism.chemistry.common.registries.ChemistryItems;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

@ParametersAreNonnullByDefault
public class ChemistryRecipeProvider extends BaseRecipeProvider {

    public ChemistryRecipeProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
        super(gen, existingFileHelper, MekanismChemistry.MODID);
    }

    @Override
    protected void addRecipes(Consumer<FinishedRecipe> consumer) {
        addCraftingRecipes(consumer);
        addRotaryCondensentratorRecipes(consumer);
        addDistillingRecipes(consumer);
        addChemicalInfuserRecipes(consumer);
        addPressurizedReactionChamberRecipes(consumer);
    }

    private void addPressurizedReactionChamberRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "reaction/";
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(ItemTags.COALS),
              IngredientCreatorAccess.fluid().from(ChemistryFluids.AMMONIA, 100),
              IngredientCreatorAccess.gas().from(MekanismGases.OXYGEN, 100),
              100,
              ChemistryItems.AMMONIUM.getItemStack())
              .build(consumer, MekanismChemistry.rl(basePath + "ammonium_with_coals"));
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.createIngredient(Arrays.asList(
                    Tags.Items.STORAGE_BLOCKS_COAL,
                    MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL
              ))),
              IngredientCreatorAccess.fluid().from(ChemistryFluids.AMMONIA, 1000),
              IngredientCreatorAccess.gas().from(MekanismGases.OXYGEN, 100),
              900,
              ChemistryItems.AMMONIUM.getItemStack(10))
              .build(consumer, MekanismChemistry.rl(basePath + "ammonium_with_blocks_coals"));
    }

    private void addCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        //Fertilizer
        ExtendedShapelessRecipeBuilder.shapelessRecipe(ChemistryItems.FERTILIZER, 2)
              .addIngredient(ChemistryItems.AMMONIUM)
              .addIngredient(MekanismItems.SULFUR_DUST)
              .build(consumer);
    }

    private void addChemicalInfuserRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "chemical_infusing/";
        //Ammonia
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.gas().from(ChemistryGases.NITROGEN, 1),
              IngredientCreatorAccess.gas().from(MekanismGases.HYDROGEN, 3),
              ChemistryGases.AMMONIA.getStack(1)
        ).build(consumer, MekanismChemistry.rl(basePath + "ammonia"));
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

    private void addDistillingRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "distilling/";
        //Air
        DistillingRecipeBuilder.distilling(IngredientCreatorAccess.fluid().from(ChemistryFluids.AIR, 100), List.of(
                    ChemistryFluids.NITROGEN.getFluidStack(78),
                    MekanismFluids.OXYGEN.getFluidStack(21)
              )
        ).build(consumer, MekanismChemistry.rl(basePath + "air"));
    }
}
