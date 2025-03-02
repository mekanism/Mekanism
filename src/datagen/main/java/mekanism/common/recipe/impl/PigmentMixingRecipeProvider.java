package mekanism.common.recipe.impl;

import mekanism.api.datagen.recipe.builder.ChemicalChemicalToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismChemicals;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;

class PigmentMixingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "pigment_mixing/";
        //blue + red -> 2 purple
        addMix(consumer, EnumColor.DARK_BLUE, 1, EnumColor.RED, 1, EnumColor.PURPLE, basePath);
        //blue + green -> 2 cyan
        addMix(consumer, EnumColor.DARK_BLUE, 1, EnumColor.DARK_GREEN, 1, EnumColor.DARK_AQUA, basePath);
        //gray + white -> 2 light gray
        addMix(consumer, EnumColor.DARK_GRAY, 1, EnumColor.WHITE, 1, EnumColor.GRAY, basePath);
        //black + white -> 2 gray
        addMix(consumer, EnumColor.BLACK, 1, EnumColor.WHITE, 1, EnumColor.DARK_GRAY, basePath);
        //red + white -> 2 pink
        addMix(consumer, EnumColor.RED, 1, EnumColor.WHITE, 1, EnumColor.BRIGHT_PINK, basePath);
        //green + white -> 2 lime
        addMix(consumer, EnumColor.DARK_GREEN, 1, EnumColor.WHITE, 1, EnumColor.BRIGHT_GREEN, basePath);
        //blue + white -> 2 light blue
        addMix(consumer, EnumColor.DARK_BLUE, 1, EnumColor.WHITE, 1, EnumColor.INDIGO, basePath);
        //purple + pink -> 2 magenta
        addMix(consumer, EnumColor.PURPLE, 1, EnumColor.BRIGHT_PINK, 1, EnumColor.PINK, basePath);
        //red + yellow -> 2 orange
        addMix(consumer, EnumColor.RED, 1, EnumColor.YELLOW, 1, EnumColor.ORANGE, basePath);

        //Custom types (ones in EnumColor, but without a corresponding dye color)
        //cyan + white -> 2 aqua
        addMix(consumer, EnumColor.DARK_AQUA, 1, EnumColor.WHITE, 1, EnumColor.AQUA, basePath);
        //gray + 2 red -> 3 dark red
        addMix(consumer, EnumColor.DARK_GRAY, 1, EnumColor.RED, 2, EnumColor.DARK_RED, basePath);
        //black + 4 red -> 5 dark red
        addMix(consumer, EnumColor.BLACK, 1, EnumColor.RED, 4, EnumColor.DARK_RED, basePath);

        //Recipes calculated based on combining and reducing other recipes
        //light blue + lime -> 2 aqua
        addMix(consumer, EnumColor.INDIGO, 1, EnumColor.BRIGHT_GREEN, 1, EnumColor.AQUA, basePath);
        //light blue + red -> 2 magenta
        addMix(consumer, EnumColor.INDIGO, 1, EnumColor.RED, 1, EnumColor.PINK, basePath);

        //Recipes that don't exist for vanilla dye mixing, but we add
        //blue + yellow -> 2 green
        addMix(consumer, EnumColor.DARK_BLUE, 1, EnumColor.YELLOW, 1, EnumColor.DARK_GREEN, basePath);
        //aqua + yellow -> 2 lime
        addMix(consumer, EnumColor.AQUA, 1, EnumColor.YELLOW, 1, EnumColor.BRIGHT_GREEN, basePath);
        //TODO: Come up with more mixtures, and figure out the numbers for them
        //black + 2 pink -> 3 purple
        /*addMix(consumer, EnumColor.BLACK, 1, EnumColor.BRIGHT_PINK, 2, EnumColor.PURPLE, basePath);
        //black + 4 magenta -> 5 purple
        addMix(consumer, EnumColor.BLACK, 1, EnumColor.PINK, 4, EnumColor.PURPLE, basePath);
        //gray + 2 magenta -> 3 purple
        addMix(consumer, EnumColor.GRAY, 1, EnumColor.PINK, 4, EnumColor.PURPLE, basePath);
        //gray + 2 pink -> 3 magenta
        addMix(consumer, EnumColor.GRAY, 1, EnumColor.BRIGHT_PINK, 4, EnumColor.PINK, basePath);*/
    }

    private static void addMix(RecipeOutput consumer, EnumColor leftInput, long leftInputAmount, EnumColor rightInput, long rightInputAmount,
          EnumColor output, String basePath) {
        ChemicalChemicalToChemicalRecipeBuilder.pigmentMixing(
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(leftInput), leftInputAmount),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(rightInput), rightInputAmount),
              MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(output).asStack(leftInputAmount + rightInputAmount)
        ).build(consumer, Mekanism.rl(basePath + leftInput.getRegistryPrefix() + "_" + rightInput.getRegistryPrefix() + "_to_" + output.getRegistryPrefix()));
    }
}