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
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

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
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, SawmillRecipe recipe, MekFakeGroupHelper fakeGroupHelper, ContextMap contextMap) {
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
            Object2IntMap<NormalizedSimpleStack> ingredients = fakeGroupHelper.forIngredient(recipe.getInput(), contextMap);
            if (ingredients.isEmpty()) {
                return false;
            } else if (primaryMultiplier > 1) {
                ingredients = insertScaled(new Object2IntArrayMap<>(ingredients.size()), ingredients, primaryMultiplier);
            }
            SawmillOutput output = SawmillOutput.create(basicRecipe.getMainOutputRaw().orElse(null),
                  basicRecipe.getSecondaryOutputRaw().orElse(null),
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
              output -> output.mainOutput() == null, representationGetter, SawmillRecipeMapper::addConversions);
    }

    private static boolean addConversions(IMappingCollector<NormalizedSimpleStack, Long> mapper, SawmillOutput output, Object2IntMap<NormalizedSimpleStack> inputs) {
        ItemStackTemplate mainOutput = output.mainOutput();
        if (inputs.isEmpty() || mainOutput == null) {
            return false;
        }
        ItemStackTemplate secondaryOutput = output.secondaryOutput();
        if (secondaryOutput == null) {
            return addConversion(mapper, mainOutput, inputs);
        }
        //Use bitwise or as we want to try and add both of them
        return addConversion(mapper, mainOutput, forIngredients(
              inputs,
              NSSItem.createItem(secondaryOutput.item(), secondaryOutput.components()), -secondaryOutput.count()
        )) | addConversion(mapper, secondaryOutput, forIngredients(
              inputs,
              NSSItem.createItem(mainOutput.item(), mainOutput.components()), -mainOutput.count()
        ));
    }

    private record SawmillOutput(@Nullable ItemStackTemplate mainOutput, @Nullable ItemStackTemplate secondaryOutput) {

        public static SawmillOutput create(@Nullable ItemStackTemplate mainOutput, @Nullable ItemStackTemplate secondaryOutput, int primaryMultiplier, int secondaryMultiplier) {
            if (secondaryOutput != null && secondaryMultiplier > 1) {
                secondaryOutput = secondaryOutput.withCount(secondaryMultiplier * secondaryOutput.count());
            }
            if (mainOutput == null) {
                //As we scale our values, we can just pretend the primary is the secondary
                return new SawmillOutput(secondaryOutput, null);
            } else if (primaryMultiplier > 1) {
                mainOutput = mainOutput.withCount(primaryMultiplier * mainOutput.count());
            }
            return new SawmillOutput(mainOutput, secondaryOutput);
        }

        public static SawmillOutput create(ChanceOutput output, int primaryMultiplier, int secondaryMultiplier) {
            return create(output.getMainOutput(), output.getMaxSecondaryOutput(), primaryMultiplier, secondaryMultiplier);
        }
    }
}