package mekanism.common.recipe.ingredient.chemical;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.IngredientType;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public abstract class SingleChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
      implements ChemicalStackIngredient<CHEMICAL, STACK> {

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CLAZZ extends SingleChemicalStackIngredient<CHEMICAL, STACK>> Codec<CLAZZ>
    makeCodec(MapCodec<STACK> stackCodec, Function<STACK, CLAZZ> constructor) {
        return stackCodec.xmap(constructor, SingleChemicalStackIngredient::getChemicalInstance).codec();
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CLAZZ extends SingleChemicalStackIngredient<CHEMICAL, STACK>>
    StreamCodec<RegistryFriendlyByteBuf, CLAZZ> makeStreamCodec(StreamCodec<RegistryFriendlyByteBuf, STACK> stackCodec, Function<STACK, CLAZZ> constructor) {
        return stackCodec.map(constructor, SingleChemicalStackIngredient::getChemicalInstance);
    }

    private final List<STACK> representations;
    private final STACK chemicalInstance;

    public SingleChemicalStackIngredient(STACK chemicalInstance) {
        this.chemicalInstance = chemicalInstance;
        //Note: While callers of getRepresentations aren't supposed to mutate it we copy it anyway so that in case they do
        // then nothing bad happens to the actual recipe
        this.representations = Collections.singletonList(ChemicalUtil.copy(this.chemicalInstance));
    }

    @Override
    public boolean test(STACK chemicalStack) {
        return testType(chemicalStack) && chemicalStack.getAmount() >= chemicalInstance.getAmount();
    }

    @Override
    public boolean testType(STACK chemicalStack) {
        return chemicalInstance.isTypeEqual(Objects.requireNonNull(chemicalStack));
    }

    @Override
    public boolean testType(CHEMICAL chemical) {
        return chemicalInstance.is(Objects.requireNonNull(chemical));
    }

    @Override
    @SuppressWarnings("unchecked")
    public STACK getMatchingInstance(STACK chemicalStack) {
        if (test(chemicalStack)) {
            //Note: We manually "implement" the copy to ensure it returns the proper type as ChemicalStack#copy returns ChemicalStack<CHEMICAL> instead of STACK
            return ChemicalUtil.copy(chemicalInstance);
        }
        return getEmptyStack();
    }

    @Override
    public long getNeededAmount(STACK stack) {
        return testType(stack) ? chemicalInstance.getAmount() : 0;
    }

    @Override
    public boolean hasNoMatchingInstances() {
        return false;
    }

    @Override
    public List<@NotNull STACK> getRepresentations() {
        return this.representations;
    }

    /**
     * For use in recipe input caching.
     */
    public CHEMICAL getInputRaw() {
        return chemicalInstance.getChemical();
    }

    public STACK getChemicalInstance() {
        return chemicalInstance;
    }

    @Override
    public IngredientType getType() {
        return IngredientType.SINGLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return chemicalInstance.equals(((SingleChemicalStackIngredient<?, ?>) o).chemicalInstance);
    }

    @Override
    public int hashCode() {
        return chemicalInstance.hashCode();
    }
}