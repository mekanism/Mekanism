package mekanism.common.recipe.ingredient.creator;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.inputs.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientInfo;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.Tag;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
    public InfusionStackIngredient from(Tag<InfuseType> tag, long amount) {
        Objects.requireNonNull(tag, "InfusionStackIngredients cannot be created from a null tag.");
        assertPositiveAmount(amount);
        return new TaggedInfusionStackIngredient(tag, amount);
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

        private TaggedInfusionStackIngredient(Tag<InfuseType> tag, long amount) {
            super(tag, amount);
        }

        @Override
        protected ChemicalIngredientInfo<InfuseType, InfusionStack> getIngredientInfo() {
            return ChemicalIngredientInfo.INFUSION;
        }
    }
}