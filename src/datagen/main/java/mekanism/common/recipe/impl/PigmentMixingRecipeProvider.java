package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ChemicalChemicalToChemicalRecipeBuilder;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismPigments;
import net.minecraft.data.IFinishedRecipe;

class PigmentMixingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
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
    }

    private static void addMix(Consumer<IFinishedRecipe> consumer, EnumColor leftInput, long leftInputAmount, EnumColor rightInput, long rightInputAmount,
          EnumColor output, String basePath) {
        ChemicalChemicalToChemicalRecipeBuilder.pigmentMixing(
              PigmentStackIngredient.from(MekanismPigments.PIGMENT_COLOR_LOOKUP.get(leftInput), leftInputAmount),
              PigmentStackIngredient.from(MekanismPigments.PIGMENT_COLOR_LOOKUP.get(rightInput), rightInputAmount),
              MekanismPigments.PIGMENT_COLOR_LOOKUP.get(output).getStack(leftInputAmount + rightInputAmount)
        ).build(consumer, Mekanism.rl(basePath + leftInput.getRegistryPrefix() + "_" + rightInput.getRegistryPrefix() + "_to_" + output.getRegistryPrefix()));
    }
}