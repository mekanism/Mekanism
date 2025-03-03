package mekanism.common.integration.projecte.mappers;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.SequencedCollection;
import java.util.function.Function;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import mekanism.api.recipes.basic.BasicSawmillRecipe;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;

@RecipeTypeMapper
public class SawmillRecipeMapper extends TypedMekanismRecipeMapper<SawmillRecipe> {

    public SawmillRecipeMapper() {
        super(MekanismConfigTranslations.PE_MAPPER_SAWING, SawmillRecipe.class, MekanismRecipeType.SAWING);
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, SawmillRecipe recipe, MekFakeGroupHelper fakeGroupHelper) {
        int primaryMultiplier;
        int secondaryMultiplier;
        if (recipe.getSecondaryChance() > 0 && recipe.getSecondaryChance() < 1) {
            Fraction multiplier;
            try {
                multiplier = Fraction.getFraction(recipe.getSecondaryChance()).invert();
            } catch (ArithmeticException e) {
                //If we couldn't turn it into a fraction, then note we failed to convert the recipe
                return false;
            }
            primaryMultiplier = multiplier.getNumerator();
            secondaryMultiplier = multiplier.getDenominator();
        } else {
            primaryMultiplier = 1;
            secondaryMultiplier = 1;
        }

        if (OPTIMIZE_BASIC && recipe instanceof BasicSawmillRecipe basicRecipe) {
            //This will be the case for the majority of our recipes
            Object2IntMap<NormalizedSimpleStack> ingredients = fakeGroupHelper.forIngredient(recipe.getInput());
            if (ingredients.isEmpty()) {
                return false;
            } else if (primaryMultiplier > 1) {
                ingredients = insertScaled(new Object2IntArrayMap<>(ingredients.size()), ingredients, primaryMultiplier);
            }
            SawmillOutput output = SawmillOutput.create(basicRecipe.getMainOutputRaw().orElse(ItemStack.EMPTY),
                  basicRecipe.getSecondaryOutputRaw().orElse(ItemStack.EMPTY),
                  primaryMultiplier,
                  secondaryMultiplier
            );
            return addConversions(mapper, output, ingredients);
        }
        Function<SequencedCollection<ItemStack>, Object2IntMap<NormalizedSimpleStack>> representationGetter;
        if (primaryMultiplier == 1) {
            representationGetter = fakeGroupHelper::forItems;
        } else {
            representationGetter = representations -> {
                Object2IntMap<NormalizedSimpleStack> ingredients = fakeGroupHelper.forItems(representations);
                return insertScaled(new Object2IntArrayMap<>(ingredients.size()), ingredients, primaryMultiplier);
            };
        }
        return addConversions(mapper, recipe.getInput(), input -> SawmillOutput.create(recipe.getOutput(input), primaryMultiplier, secondaryMultiplier),
              output -> output.mainOutput().isEmpty(), representationGetter, null, SawmillRecipeMapper::addConversions);
    }

    private static boolean addConversions(IMappingCollector<NormalizedSimpleStack, Long> mapper, SawmillOutput output, Object2IntMap<NormalizedSimpleStack> inputs) {
        ItemStack mainOutput = output.mainOutput();
        if (inputs.isEmpty() || mainOutput.isEmpty()) {
            return false;
        }
        ItemStack secondaryOutput = output.secondaryOutput();
        if (secondaryOutput.isEmpty()) {
            return addConversion(mapper, mainOutput, inputs);
        }
        //Use bitwise or as we want to try and add both of them
        return addConversion(mapper, mainOutput, forIngredients(
              inputs,
              NSSItem.createItem(secondaryOutput), -secondaryOutput.getCount()
        )) | addConversion(mapper, secondaryOutput, forIngredients(
              inputs,
              NSSItem.createItem(mainOutput), -mainOutput.getCount()
        ));
    }

    private record SawmillOutput(ItemStack mainOutput, ItemStack secondaryOutput) {

        public static SawmillOutput create(ItemStack mainOutput, ItemStack secondaryOutput, int primaryMultiplier, int secondaryMultiplier) {
            if (!secondaryOutput.isEmpty() && secondaryMultiplier > 1) {
                secondaryOutput = secondaryOutput.copyWithCount(secondaryMultiplier * secondaryOutput.getCount());
            }
            if (mainOutput.isEmpty()) {
                //As we scale our values, we can just pretend the primary is the secondary
                return new SawmillOutput(secondaryOutput, ItemStack.EMPTY);
            } else if (primaryMultiplier > 1) {
                mainOutput = mainOutput.copyWithCount(primaryMultiplier * mainOutput.getCount());
            }
            return new SawmillOutput(mainOutput, secondaryOutput);
        }

        public static SawmillOutput create(ChanceOutput output, int primaryMultiplier, int secondaryMultiplier) {
            return create(output.getMainOutput(), output.getMaxSecondaryOutput(), primaryMultiplier, secondaryMultiplier);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SawmillOutput other = (SawmillOutput) o;
            return ItemStack.matches(mainOutput, other.mainOutput) && ItemStack.matches(secondaryOutput, other.secondaryOutput);
        }

        @Override
        public int hashCode() {
            int hash = ItemStack.hashItemAndComponents(mainOutput);
            hash = 31 * hash + mainOutput.getCount();
            if (!secondaryOutput.isEmpty()) {
                hash = 31 * hash + ItemStack.hashItemAndComponents(secondaryOutput);
                hash = 31 * hash + secondaryOutput.getCount();
            }
            return hash;
        }
    }
}