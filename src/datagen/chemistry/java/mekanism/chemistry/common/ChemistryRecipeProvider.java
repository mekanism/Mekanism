package mekanism.chemistry.common;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.datagen.recipe.builder.ChemicalChemicalToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.DistillingRecipeBuilder;
import mekanism.api.datagen.recipe.builder.PressurizedReactionRecipeBuilder;
import mekanism.api.datagen.recipe.builder.RotaryRecipeBuilder;
import mekanism.api.providers.IFluidProvider;
import mekanism.api.providers.IGasProvider;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.chemistry.common.registries.ChemistryBlocks;
import mekanism.chemistry.common.registries.ChemistryFluids;
import mekanism.chemistry.common.registries.ChemistryGases;
import mekanism.chemistry.common.registries.ChemistryItems;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.recipe.builder.MekDataShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.DoubleLine;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Items;
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
        //Ammonium
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
                    IngredientCreatorAccess.gas().from(MekanismGases.OXYGEN, 1000),
                    900,
                    ChemistryItems.AMMONIUM.getItemStack(10))
              .build(consumer, MekanismChemistry.rl(basePath + "ammonium_with_blocks_coals"));
        //Explosives
        PressurizedReactionRecipeBuilder.reaction(
                    IngredientCreatorAccess.item().from(MekanismItems.SUBSTRATE),
                    IngredientCreatorAccess.fluid().from(FluidTags.WATER, 100),
                    IngredientCreatorAccess.gas().from(ChemistryGases.NITROGEN_DIOXIDE, 100),
                    100,
                    ChemistryItems.EXPLOSIVES.getItemStack(1))
              .build(consumer, MekanismChemistry.rl(basePath + "explosives"));
    }

    private void addCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        //Fertilizer
        ExtendedShapelessRecipeBuilder.shapelessRecipe(ChemistryItems.FERTILIZER, 2)
              .addIngredient(ChemistryItems.AMMONIUM)
              .addIngredient(MekanismItems.SULFUR_DUST)
              .build(consumer);
        //TNT from explosives
        ExtendedShapedRecipeBuilder.shapedRecipe(Items.TNT, 1).pattern(RecipePattern.createPattern(
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT),
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT)))
              .key(Pattern.CONSTANT, ChemistryItems.EXPLOSIVES)
              .build(consumer);
        //Air Compressor
        MekDataShapedRecipeBuilder.shapedRecipe(ChemistryBlocks.AIR_COMPRESSOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.TANK, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.STEEL_CASING, Pattern.ALLOY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.OSMIUM, Pattern.OSMIUM))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .build(consumer);
        //Fractionating Distiller Block
        ExtendedShapedRecipeBuilder.shapedRecipe(ChemistryBlocks.FRACTIONATING_DISTILLER_BLOCK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.INGOT, MekanismTags.Items.INGOTS_BRONZE)
              .build(consumer);
        //Fractionating Distiller Valve
        ExtendedShapedRecipeBuilder.shapedRecipe(ChemistryBlocks.FRACTIONATING_DISTILLER_VALVE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, ChemistryBlocks.FRACTIONATING_DISTILLER_BLOCK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .build(consumer);
        //Fractionating Distiller Controller
        ExtendedShapedRecipeBuilder.shapedRecipe(ChemistryBlocks.FRACTIONATING_DISTILLER_CONTROLLER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, Pattern.GLASS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.CONSTANT, Pattern.TANK, Pattern.CONSTANT),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, ChemistryBlocks.FRACTIONATING_DISTILLER_BLOCK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .key(Pattern.GLASS, Tags.Items.GLASS_PANES)
              .key(Pattern.TANK, MekanismBlocks.BASIC_FLUID_TANK)
              .build(consumer);
    }

    private void addChemicalInfuserRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "chemical_infusing/";
        addChemicalInfuserRecipe(consumer, basePath, "ammonia", ChemistryGases.NITROGEN.getStack(1), MekanismGases.HYDROGEN.getStack(3), ChemistryGases.AMMONIA.getStack(1));
        addChemicalInfuserRecipe(consumer, basePath, "nitric_oxide", ChemistryGases.AMMONIA.getStack(1), MekanismGases.OXYGEN.getStack(1), ChemistryGases.NITRIC_OXIDE.getStack(1));
        addChemicalInfuserRecipe(consumer, basePath, "nitrogen_dioxide", ChemistryGases.NITRIC_OXIDE.getStack(1), MekanismGases.OXYGEN.getStack(1), ChemistryGases.NITROGEN_DIOXIDE.getStack(1));
    }

    private void addChemicalInfuserRecipe(Consumer<FinishedRecipe> consumer, String basePath, String name, GasStack gasIn1, GasStack gasIn2, GasStack gasOut) {
        //Ammonia
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.gas().from(gasIn1),
              IngredientCreatorAccess.gas().from(gasIn2),
              gasOut
        ).build(consumer, MekanismChemistry.rl(basePath + name));
    }

    private void addRotaryCondensentratorRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "rotary/";
        addRotaryCondensentratorRecipe(consumer, basePath, ChemistryGases.AMMONIA, ChemistryFluids.AMMONIA, ChemistryTags.Fluids.AMMONIA, ChemistryTags.Gases.AMMONIA);
        addRotaryCondensentratorRecipe(consumer, basePath, ChemistryGases.NITROGEN, ChemistryFluids.NITROGEN, ChemistryTags.Fluids.NITROGEN, ChemistryTags.Gases.NITROGEN);
        addRotaryCondensentratorRecipe(consumer, basePath, ChemistryGases.AIR, ChemistryFluids.AIR, ChemistryTags.Fluids.AIR, ChemistryTags.Gases.AIR);
        addRotaryCondensentratorRecipe(consumer, basePath, ChemistryGases.NITRIC_OXIDE, ChemistryFluids.NITRIC_OXIDE, ChemistryTags.Fluids.NITRIC_OXIDE, ChemistryTags.Gases.NITRIC_OXIDE);
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
