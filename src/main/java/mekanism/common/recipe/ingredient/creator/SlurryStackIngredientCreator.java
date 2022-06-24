package mekanism.common.recipe.ingredient.creator;

import java.util.Objects;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientInfo;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraft.tags.TagKey;

@NothingNullByDefault
public class SlurryStackIngredientCreator extends ChemicalStackIngredientCreator<Slurry, SlurryStack, SlurryStackIngredient> {

    public static final SlurryStackIngredientCreator INSTANCE = new SlurryStackIngredientCreator();

    private SlurryStackIngredientCreator() {
    }

    @Override
    protected ChemicalIngredientDeserializer<Slurry, SlurryStack, SlurryStackIngredient> getDeserializer() {
        return ChemicalIngredientDeserializer.SLURRY;
    }

    @Override
    public SlurryStackIngredient from(IChemicalProvider<Slurry> provider, long amount) {
        Objects.requireNonNull(provider, "SlurryStackIngredients cannot be created from a null chemical provider.");
        Slurry slurry = provider.getChemical();
        assertNonEmpty(slurry);
        assertPositiveAmount(amount);
        return new SingleSlurryStackIngredient(slurry.getStack(amount));
    }

    @Override
    public SlurryStackIngredient from(TagKey<Slurry> tag, long amount) {
        Objects.requireNonNull(tag, "SlurryStackIngredients cannot be created from a null tag.");
        assertPositiveAmount(amount);
        return new TaggedSlurryStackIngredient(tag, amount);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Converts a stream of ingredients into a single ingredient by converting the stream to an array and calling {@link
     * #createMulti(SlurryStackIngredient[])}.
     */
    @Override
    public SlurryStackIngredient from(Stream<SlurryStackIngredient> ingredients) {
        return createMulti(ingredients.toArray(SlurryStackIngredient[]::new));
    }

    public static class SingleSlurryStackIngredient extends SingleChemicalStackIngredient<Slurry, SlurryStack> implements SlurryStackIngredient {

        private SingleSlurryStackIngredient(SlurryStack stack) {
            super(stack);
        }

        @Override
        protected ChemicalIngredientInfo<Slurry, SlurryStack> getIngredientInfo() {
            return ChemicalIngredientInfo.SLURRY;
        }
    }

    public static class TaggedSlurryStackIngredient extends TaggedChemicalStackIngredient<Slurry, SlurryStack> implements SlurryStackIngredient {

        private TaggedSlurryStackIngredient(TagKey<Slurry> tag, long amount) {
            super(ChemicalTags.SLURRY, tag, amount);
        }

        @Override
        protected ChemicalIngredientInfo<Slurry, SlurryStack> getIngredientInfo() {
            return ChemicalIngredientInfo.SLURRY;
        }
    }
}