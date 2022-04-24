package mekanism.common.recipe.ingredient.creator;

import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientInfo;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.TagKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasStackIngredientCreator extends ChemicalStackIngredientCreator<Gas, GasStack, GasStackIngredient> {

    public static final GasStackIngredientCreator INSTANCE = new GasStackIngredientCreator();

    private GasStackIngredientCreator() {
    }

    @Override
    protected ChemicalIngredientDeserializer<Gas, GasStack, GasStackIngredient> getDeserializer() {
        return ChemicalIngredientDeserializer.GAS;
    }

    @Override
    public GasStackIngredient from(IChemicalProvider<Gas> provider, long amount) {
        Objects.requireNonNull(provider, "GasStackIngredients cannot be created from a null chemical provider.");
        Gas gas = provider.getChemical();
        assertNonEmpty(gas);
        assertPositiveAmount(amount);
        return new SingleGasStackIngredient(gas.getStack(amount));
    }

    @Override
    public GasStackIngredient from(TagKey<Gas> tag, long amount) {
        Objects.requireNonNull(tag, "GasStackIngredients cannot be created from a null tag.");
        assertPositiveAmount(amount);
        return new TaggedGasStackIngredient(tag, amount);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Converts a stream of ingredients into a single ingredient by converting the stream to an array and calling {@link #createMulti(GasStackIngredient[])}.
     */
    @Override
    public GasStackIngredient from(Stream<GasStackIngredient> ingredients) {
        return createMulti(ingredients.toArray(GasStackIngredient[]::new));
    }

    public static class SingleGasStackIngredient extends SingleChemicalStackIngredient<Gas, GasStack> implements GasStackIngredient {

        private SingleGasStackIngredient(GasStack stack) {
            super(stack);
        }

        @Override
        protected ChemicalIngredientInfo<Gas, GasStack> getIngredientInfo() {
            return ChemicalIngredientInfo.GAS;
        }
    }

    public static class TaggedGasStackIngredient extends TaggedChemicalStackIngredient<Gas, GasStack> implements GasStackIngredient {

        private TaggedGasStackIngredient(TagKey<Gas> tag, long amount) {
            super(ChemicalTags.GAS, tag, amount);
        }

        @Override
        protected ChemicalIngredientInfo<Gas, GasStack> getIngredientInfo() {
            return ChemicalIngredientInfo.GAS;
        }
    }
}