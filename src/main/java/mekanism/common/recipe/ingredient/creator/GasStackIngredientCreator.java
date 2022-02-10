package mekanism.common.recipe.ingredient.creator;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.inputs.ChemicalStackIngredient.GasStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientInfo;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.Tag;

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
    public GasStackIngredient from(Tag<Gas> tag, long amount) {
        Objects.requireNonNull(tag, "GasStackIngredients cannot be created from a null tag.");
        assertPositiveAmount(amount);
        return new TaggedGasStackIngredient(tag, amount);
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

        private TaggedGasStackIngredient(Tag<Gas> tag, long amount) {
            super(tag, amount);
        }

        @Override
        protected ChemicalIngredientInfo<Gas, GasStack> getIngredientInfo() {
            return ChemicalIngredientInfo.GAS;
        }
    }
}