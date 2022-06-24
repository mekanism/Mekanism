package mekanism.common.recipe.ingredient.creator;

import java.util.Objects;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientInfo;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraft.tags.TagKey;

@NothingNullByDefault
public class InfusionStackIngredientCreator extends ChemicalStackIngredientCreator<InfuseType, InfusionStack, InfusionStackIngredient> {

    public static final InfusionStackIngredientCreator INSTANCE = new InfusionStackIngredientCreator();

    private InfusionStackIngredientCreator() {
    }

    @Override
    protected ChemicalIngredientDeserializer<InfuseType, InfusionStack, InfusionStackIngredient> getDeserializer() {
        return ChemicalIngredientDeserializer.INFUSION;
    }

    @Override
    public InfusionStackIngredient from(IChemicalProvider<InfuseType> provider, long amount) {
        Objects.requireNonNull(provider, "InfusionStackIngredients cannot be created from a null chemical provider.");
        InfuseType infuseType = provider.getChemical();
        assertNonEmpty(infuseType);
        assertPositiveAmount(amount);
        return new SingleInfusionStackIngredient(infuseType.getStack(amount));
    }

    @Override
    public InfusionStackIngredient from(TagKey<InfuseType> tag, long amount) {
        Objects.requireNonNull(tag, "InfusionStackIngredients cannot be created from a null tag.");
        assertPositiveAmount(amount);
        return new TaggedInfusionStackIngredient(tag, amount);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Converts a stream of ingredients into a single ingredient by converting the stream to an array and calling {@link
     * #createMulti(InfusionStackIngredient[])}.
     */
    @Override
    public InfusionStackIngredient from(Stream<InfusionStackIngredient> ingredients) {
        return createMulti(ingredients.toArray(InfusionStackIngredient[]::new));
    }

    public static class SingleInfusionStackIngredient extends SingleChemicalStackIngredient<InfuseType, InfusionStack> implements InfusionStackIngredient {

        private SingleInfusionStackIngredient(InfusionStack stack) {
            super(stack);
        }

        @Override
        protected ChemicalIngredientInfo<InfuseType, InfusionStack> getIngredientInfo() {
            return ChemicalIngredientInfo.INFUSION;
        }
    }

    public static class TaggedInfusionStackIngredient extends TaggedChemicalStackIngredient<InfuseType, InfusionStack> implements InfusionStackIngredient {

        private TaggedInfusionStackIngredient(TagKey<InfuseType> tag, long amount) {
            super(ChemicalTags.INFUSE_TYPE, tag, amount);
        }

        @Override
        protected ChemicalIngredientInfo<InfuseType, InfusionStack> getIngredientInfo() {
            return ChemicalIngredientInfo.INFUSION;
        }
    }
}