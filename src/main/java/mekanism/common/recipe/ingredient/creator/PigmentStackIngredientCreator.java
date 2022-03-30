package mekanism.common.recipe.ingredient.creator;

import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientInfo;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.TagKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PigmentStackIngredientCreator extends ChemicalStackIngredientCreator<Pigment, PigmentStack, PigmentStackIngredient> {

    public static final PigmentStackIngredientCreator INSTANCE = new PigmentStackIngredientCreator();

    private PigmentStackIngredientCreator() {
    }

    @Override
    protected ChemicalIngredientDeserializer<Pigment, PigmentStack, PigmentStackIngredient> getDeserializer() {
        return ChemicalIngredientDeserializer.PIGMENT;
    }

    @Override
    public PigmentStackIngredient from(IChemicalProvider<Pigment> provider, long amount) {
        Objects.requireNonNull(provider, "PigmentStackIngredients cannot be created from a null chemical provider.");
        Pigment pigment = provider.getChemical();
        assertNonEmpty(pigment);
        assertPositiveAmount(amount);
        return new SinglePigmentStackIngredient(pigment.getStack(amount));
    }

    @Override
    public PigmentStackIngredient from(TagKey<Pigment> tag, long amount) {
        Objects.requireNonNull(tag, "PigmentStackIngredients cannot be created from a null tag.");
        assertPositiveAmount(amount);
        return new TaggedPigmentStackIngredient(tag, amount);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Converts a stream of ingredients into a single ingredient by converting the stream to an array and calling {@link
     * #createMulti(PigmentStackIngredient[])}.
     */
    @Override
    public PigmentStackIngredient from(Stream<PigmentStackIngredient> ingredients) {
        return createMulti(ingredients.toArray(PigmentStackIngredient[]::new));
    }

    public static class SinglePigmentStackIngredient extends SingleChemicalStackIngredient<Pigment, PigmentStack> implements PigmentStackIngredient {

        private SinglePigmentStackIngredient(PigmentStack stack) {
            super(stack);
        }

        @Override
        protected ChemicalIngredientInfo<Pigment, PigmentStack> getIngredientInfo() {
            return ChemicalIngredientInfo.PIGMENT;
        }
    }

    public static class TaggedPigmentStackIngredient extends TaggedChemicalStackIngredient<Pigment, PigmentStack> implements PigmentStackIngredient {

        private TaggedPigmentStackIngredient(TagKey<Pigment> tag, long amount) {
            super(ChemicalTags.PIGMENT, tag, amount);
        }

        @Override
        protected ChemicalIngredientInfo<Pigment, PigmentStack> getIngredientInfo() {
            return ChemicalIngredientInfo.PIGMENT;
        }
    }
}